/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.messaging;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.vcell.util.DataAccessException;
import org.vcell.util.MessageConstants;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;
import org.vcell.util.document.VCellServerID;

import cbit.sql.ConnectionFactory;
import cbit.sql.KeyFactory;
import cbit.vcell.field.FieldDataIdentifierSpec;
import cbit.vcell.messaging.db.SimulationJobStatus;
import cbit.vcell.messaging.db.SimulationJobStatusInfo;
import cbit.vcell.messaging.db.UpdateSynchronizationException;
import cbit.vcell.messaging.server.RpcRequest;
import cbit.vcell.messaging.server.SimulationDispatcher;
import cbit.vcell.messaging.server.SimulationTask;
import cbit.vcell.modeldb.AbstractDBTopLevel;
import cbit.vcell.modeldb.AdminDatabaseServerXAImpl;
import cbit.vcell.mongodb.VCMongoMessage;
import cbit.vcell.server.AdminDatabaseServerXA;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationJob;
import cbit.vcell.solver.SimulationMessage;
import cbit.vcell.solver.VCSimulationIdentifier;
import cbit.vcell.transaction.JtaDbConnection;
import cbit.vcell.transaction.JtaOracleConnection;
import cbit.vcell.transaction.JtaTransactionManager;
import cbit.vcell.xml.XmlParseException;

/**
 * Insert the type's description here.
 * Creation date: (10/18/2001 4:31:45 PM)
 * @author: Jim Schaff
 */
public class SimulationDispatcherMessaging extends JmsServiceProviderMessaging implements ControlTopicListener {

	public static final String METHOD_NAME_STARTSIMULATION = "startSimulation";
	public static final String METHOD_NAME_STOPSIMULATION = "stopSimulation";
	
	private ConnectionFactory conFactory = null;
	private KeyFactory keyFactory = null;
		
	private JmsXAConnection jmsXAConn = null;

	private SimulationDispatcher simDispatcher = null;
	private JmsXASession mainJobDispatcher = null;

	private AdminDatabaseServerXA jobAdminXA = null;
	private String jobSelector = null;
	

	class SimulationMonitorThread extends Thread {
	
		public SimulationMonitorThread() {
			super();
			setName(simDispatcher.getServiceInstanceID() + "_MT");
		}
		
		public void run() {
			javax.transaction.TransactionManager tm = new JtaTransactionManager();
			
			JtaDbConnection obsoleteJobDbConnection = null;
			JmsXASession obsoleteJobDispatcher = null;	
			boolean join = true;
			SimulationJobStatus jobStatus = null;
				
			while (true) {
				try {
					obsoleteJobDispatcher = jmsXAConn.getXASession();
					break;
					
				} catch (Exception e) {
					log.exception(e);
					try {
						Thread.sleep(MessageConstants.SECOND_IN_MS);
					} catch (InterruptedException ex) {
						log.exception(ex);
					}
				}
			}
			
			while (true) { // first while(true);
				log.print("##MT");
				while (true) { // second while(true), check one by one
					try {	
						
						//
						// for first 10 minutes of dispatcher uptime, don't check for obsolete messages.
						// as a startup transient, let the dispatchers catch up with worker messages before passing
						// judgement on the health of jobs.
						//
						long uptime = System.currentTimeMillis() - VCMongoMessage.getServiceStartupTime();
						final int UPTIME_WAIT = 1000*60*10;
						if (uptime < UPTIME_WAIT){
							try {
								Thread.sleep(UPTIME_WAIT - uptime);
							}catch (Exception e){
							}
							continue;  // for first 10 minutes of uptime, don't obsolete
						}
						
						obsoleteJobDbConnection = new JtaOracleConnection(conFactory);

						jobStatus = jobAdminXA.getNextObsoleteSimulation(obsoleteJobDbConnection.getConnection(), MessageConstants.INTERVAL_DATABASE_SERVER_FAIL);								
						if (jobStatus == null) {
							log.print("##MT OK");
							break; // no obsolete simulation, no transaction here. go back to sleep
						}				

						tm.begin();	
						join = obsoleteJobDbConnection.joinTransaction(tm);

						if (!join) {
							throw new RuntimeException("##MT: join failed");
						} else {
							join = obsoleteJobDispatcher.joinTransaction(tm);

							if (!join) {
								throw new RuntimeException("##MT: join failed");
							} else {
								// too many retries
								if ((jobStatus.getTaskID() & MessageConstants.TASKID_RETRYCOUNTER_MASK) >= MessageConstants.TASKID_MAX_RETRIES) {							
									log.print("##MT too many retries " + jobStatus);

									// new job status is failed.
									SimulationJobStatus	newJobStatus = new SimulationJobStatus(VCellServerID.getSystemServerID(), jobStatus.getVCSimulationIdentifier(), jobStatus.getJobIndex(), jobStatus.getSubmitDate(),
										SimulationJobStatus.SCHEDULERSTATUS_FAILED, jobStatus.getTaskID(),
										SimulationMessage.MESSAGE_JOB_FAILED_TOOMANYRETRIES,
										jobStatus.getSimulationQueueEntryStatus(), jobStatus.getSimulationExecutionStatus());
									//update the database
									jobAdminXA.updateSimulationJobStatus(obsoleteJobDbConnection.getConnection(), jobStatus, newJobStatus);
									// tell client
									StatusMessage statusMsg = new StatusMessage(newJobStatus, jobStatus.getVCSimulationIdentifier().getOwner().getName(), null, null);
									statusMsg.sendToClient(obsoleteJobDispatcher);
									
								} else {
									SimulationTask simTask = simDispatcher.getSimulationTask(jobStatus);
									
									log.print("##MT requeued " + simTask);

									// increment taskid, new job status is queued
									SimulationJobStatus newJobStatus = new SimulationJobStatus(VCellServerID.getSystemServerID(), jobStatus.getVCSimulationIdentifier(), jobStatus.getJobIndex(), jobStatus.getSubmitDate(), 
										SimulationJobStatus.SCHEDULERSTATUS_QUEUED, jobStatus.getTaskID() + 1, 
										SimulationMessage.MESSAGE_JOB_QUEUED_RETRY, jobStatus.getSimulationQueueEntryStatus(), null);
									
									//update the database
									jobAdminXA.updateSimulationJobStatus(obsoleteJobDbConnection.getConnection(), jobStatus, newJobStatus);
									// send to simulation queue
									Simulation sim = simTask.getSimulationJob().getSimulation();
									SimulationTask newSimTask = new SimulationTask(new SimulationJob(sim, newJobStatus.getJobIndex(), simDispatcher.getFieldDataIdentifierSpecs(sim)), newJobStatus.getTaskID());
									SimulationTaskMessage taskMsg = new SimulationTaskMessage(newSimTask);
									taskMsg.sendSimulationTask(obsoleteJobDispatcher);
									// tell client
									StatusMessage statusMsg = new StatusMessage(newJobStatus, newSimTask.getUserName(), null, null);
									statusMsg.sendToClient(obsoleteJobDispatcher);
								}
								tm.commit();
								
								yield();
								continue;
							}
						}
					} catch (Exception e){
						log.exception(e);
						
						try {
							tm.rollback();
						} catch (Exception ex) {
							log.exception(ex);
						}

						try {
							if (obsoleteJobDbConnection != null && AbstractDBTopLevel.isBadConnection(obsoleteJobDbConnection.getConnection(), log)) {
								obsoleteJobDbConnection.closeOnFailure();
								obsoleteJobDbConnection = null;
							}
						} catch (SQLException sqlex) {
							log.exception(sqlex);
						}
					} finally {
						try {
							if (obsoleteJobDbConnection != null) {
								obsoleteJobDbConnection.close();
								obsoleteJobDbConnection = null;
							}
						} catch (SQLException ex) {
							log.exception(ex);
						}
					}
				} // second while (true)
				
				// start next check after some time
				try {
					sleep(MessageConstants.INTERVAL_PING_SERVER);
				} catch (InterruptedException ex) {
					log.exception(ex);
				}
				
			} // first while (true);
		}
	}

	class DispatchThread extends Thread {
		public DispatchThread() {
			super();
			setName(simDispatcher.getServiceInstanceID() + "_DT");
		}
	
		public void run() {
			SimulationJobStatus jobStatus = null;
			SimulationTask simTask = null;
			boolean foundOne = false;
			
			javax.transaction.TransactionManager tm = null;
			JmsXASession waitingJobDispatcher = null;
			JtaDbConnection waitingJobDbConnection = null;	
			SimulationJobStatusInfo[] allActiveJobs = null;
		
			while (true) {
				try {
					waitingJobDispatcher = jmsXAConn.getXASession();
					break;
					
				} catch (Exception e) {
					log.exception(e);
					try {
						Thread.sleep(MessageConstants.SECOND_IN_MS);
					} catch (InterruptedException ex) {
						log.exception(ex);
					}
				}
			}
			tm = new JtaTransactionManager();
			boolean join = true;
			
			while (true) {
				foundOne = false;	
				jobStatus = null;
				
				try {			
					waitingJobDbConnection = new JtaOracleConnection(conFactory);
					allActiveJobs = jobAdminXA.getActiveJobs(waitingJobDbConnection.getConnection(), getHTCPartitionShareServerIDs());
					
					if (allActiveJobs != null && allActiveJobs.length > 0) {				
						SimulationJobStatusInfo firstQualifiedJob = BatchScheduler.schedule(allActiveJobs, getHTCPartitionMaximumJobs(), 
							JmsUtils.getMaxOdeJobsPerUser(), JmsUtils.getMaxPdeJobsPerUser(), VCellServerID.getSystemServerID(), log);
						if (firstQualifiedJob != null) {
							foundOne = true;					
							jobStatus = firstQualifiedJob.getSimJobStatus();					
							Simulation sim = simDispatcher.getSimulation(firstQualifiedJob.getUser(), jobStatus.getVCSimulationIdentifier().getSimulationKey());							
							simTask = new SimulationTask(new SimulationJob(sim, jobStatus.getJobIndex(), simDispatcher.getFieldDataIdentifierSpecs(sim)), jobStatus.getTaskID());
							log.print("**DT: going to dispatch " + simTask);
						}
					}
				} catch (Exception ex) {
					log.exception(ex);
					allActiveJobs = null;
					
					try {
						if (waitingJobDbConnection != null && AbstractDBTopLevel.isBadConnection(waitingJobDbConnection.getConnection(), log)) {
							waitingJobDbConnection.closeOnFailure();
							waitingJobDbConnection = null;
						}
					} catch (java.sql.SQLException sqlex) {
						log.exception(sqlex);
					}
				} finally {
					try {
						if (waitingJobDbConnection != null) {
							waitingJobDbConnection.close();
							waitingJobDbConnection = null;
						}
					} catch (SQLException ex) {
						log.exception(ex);				
					}			
				}
				
					
				if (foundOne) {
					try {
						// A Distributed Transaction for dispatcher change the status of a waiting job in the database and sends it to simulation queue 							
						tm.begin();
						
						waitingJobDbConnection = new JtaOracleConnection(conFactory);
						
						join = waitingJobDbConnection.joinTransaction(tm) && waitingJobDispatcher.joinTransaction(tm);

						if (!join) {
							throw new RuntimeException("**DT: join failed");
						} else {
							double requiredMemMB = simTask.getEstimatedMemorySizeMB();
							if (requiredMemMB > Double.parseDouble(PropertyLoader.getRequiredProperty(PropertyLoader.limitJobMemoryMB))) {						
								SimulationJobStatus newJobStatus = simDispatcher.updateEndStatus(jobStatus, jobAdminXA, waitingJobDbConnection.getConnection(), 
										jobStatus.getVCSimulationIdentifier(), jobStatus.getJobIndex(), null, SimulationJobStatus.SCHEDULERSTATUS_FAILED, 
										SimulationMessage.jobFailed("Simulation [" + simTask.getSimulationInfo().getName() + ", " + jobStatus.getJobIndex() + "] requires approximately " + requiredMemMB + "mb memory. Exceeds current memory limit."));
								
								// tell client
								StatusMessage message = new StatusMessage(newJobStatus, simTask.getUserName(), null, null);
								message.sendToClient(waitingJobDispatcher);
							} else {
								SimulationTaskMessage taskMsg = new SimulationTaskMessage(simTask);
								// send the job the job queue
								taskMsg.sendSimulationTask(waitingJobDispatcher);
								//update database
								SimulationJobStatus newJobStatus = simDispatcher.updateQueueStatus(jobStatus, jobAdminXA, waitingJobDbConnection.getConnection(), 
									jobStatus.getVCSimulationIdentifier(), jobStatus.getJobIndex(), MessageConstants.QUEUE_ID_SIMULATIONJOB, simTask.getTaskID(), false);						
								// tell client
								StatusMessage statusMsg = new StatusMessage(newJobStatus, simTask.getUserName(), null, null);
								statusMsg.sendToClient(waitingJobDispatcher);
								
								log.print("**DT: queued " + simTask);
							}
						
							tm.commit();
							yield();
							continue;
						}
					} catch (Exception ex) { // transaction exception
						log.exception(ex);
						
						try {
							tm.rollback();
						} catch (Exception ex1) {
							log.exception(ex1);
						}
						try {
							if (waitingJobDbConnection != null && AbstractDBTopLevel.isBadConnection(waitingJobDbConnection.getConnection(), log)) {
								waitingJobDbConnection.closeOnFailure();
								waitingJobDbConnection = null;
							}
						} catch (SQLException sqlex) {
							log.exception(sqlex);
						}						
					} finally {
						try {
							if (waitingJobDbConnection != null) {
								waitingJobDbConnection.close();
								waitingJobDbConnection = null;
							}
						} catch (java.sql.SQLException ex) {
							log.exception(ex);
						}
					}
						
				} // if (foundOne)

				// if there are no messages or no qualified jobs or exceptions, sleep for a while
				try {
					if (foundOne) {
						sleep(100);
					} else {
						sleep(1 * MessageConstants.SECOND_IN_MS);
					}
				} catch (InterruptedException ex) {
					log.exception(ex);
				}
			} // while(true)
		
		}
	}

	public class StatusThread extends Thread {
		public StatusThread() {
			super();
			setName(simDispatcher.getServiceInstanceID() + "_ST");
		}

	
		public void run() {
			javax.transaction.TransactionManager tm = null;
			JmsXASession statusReceiver = null;
			JtaDbConnection statusDbConnection = null;
			
			while (true) {
				try {
					statusReceiver = jmsXAConn.getXASession();
					break;			
				} catch (Exception e) {
					log.exception(e);
					try {
						Thread.sleep(MessageConstants.SECOND_IN_MS);
					} catch (InterruptedException ex) {
						log.exception(ex);
					}
				}
			}
		
			tm = new JtaTransactionManager();
			Message recievedMsg = null;
			boolean join = true;
			
			while (true) {
				try {
					//log.print("--ST");
					tm.begin();
					
					join = statusReceiver.joinTransaction(tm);

					if (!join) {
						throw new RuntimeException("--ST: join failed");
					} else {
						recievedMsg = statusReceiver.receiveMessage(JmsUtils.getQueueWorkerEvent(), 100);

						if (recievedMsg == null) {
							try {
								tm.rollback();
							} catch (Exception ex) {
								log.exception(ex);
							}
						} else {
							statusDbConnection = new JtaOracleConnection(conFactory);							
							join = statusDbConnection.joinTransaction(tm);
							if (!join) {
								throw new RuntimeException("--ST: join failed");
							} else {
								simDispatcher.onWorkerEventMessage(jobAdminXA, statusDbConnection.getConnection(), statusReceiver, recievedMsg);
								tm.commit();

								yield();
								continue;
							}
						}
					}

				} catch (Exception ex) { // transaction error
					log.exception(ex);
					try {
						tm.rollback();
					} catch (Exception ex1) {
						log.exception(ex1);
					}

					try {
						if (statusDbConnection != null && AbstractDBTopLevel.isBadConnection(statusDbConnection.getConnection(), log)) {
							statusDbConnection.closeOnFailure();
							statusDbConnection = null;
						}
					} catch (java.sql.SQLException sqlex) {
						log.exception(sqlex);
					}
				} finally {
					try {
						if (statusDbConnection != null) {
							statusDbConnection.close();
							statusDbConnection = null;
						}
					} catch (SQLException ex) {
						log.exception(ex);
					}
				}
				
				// if there are no messages or exceptions, sleep for a while
				try {
					sleep(5 * MessageConstants.SECOND_IN_MS);
				} catch (InterruptedException ex) {
					log.exception(ex);
				}
			} // while (true)
		}
	}

/**
 * Client constructor comment.
 */
public SimulationDispatcherMessaging(SimulationDispatcher simDispatcher0, ConnectionFactory conFactory0, KeyFactory keyFactory0, SessionLog log0) 
	throws java.sql.SQLException, JMSException, DataAccessException {
	super(simDispatcher0, log0);
	simDispatcher = simDispatcher0;
	conFactory = conFactory0;
	keyFactory = keyFactory0;
	jobAdminXA = new AdminDatabaseServerXAImpl(keyFactory, log);
		
	reconnect();

	log.print("Starting dispatch thread..");
	new DispatchThread().start();	

	log.print("Starting status thread..");
	new StatusThread().start();

	log.print("Starting monitor thread...");
	new SimulationMonitorThread().start();		
	
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/2001 11:08:09 PM)
 * @param simulation cbit.vcell.solver.Simulation
 */
private void do_failed(Connection con, SimulationJobStatus oldJobStatus, String username, VCSimulationIdentifier vcSimID, 
		int jobIndex, SimulationMessage failMsg) throws JMSException, DataAccessException, UpdateSynchronizationException {
	
	// if the job is in simJob queue, get it out	
	
	// update database
	SimulationJobStatus newJobStatus = simDispatcher.updateEndStatus(oldJobStatus, jobAdminXA, con, vcSimID, jobIndex, null, SimulationJobStatus.SCHEDULERSTATUS_FAILED, failMsg);
	
	// tell client
	StatusMessage message = new StatusMessage(newJobStatus, username, null, null);
	message.sendToClient(mainJobDispatcher);
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/2001 11:08:09 PM)
 * @param simulation cbit.vcell.solver.Simulation
 */
private void do_start(Connection con, SimulationJobStatus oldJobStatus, SimulationTask simTask, int queueID) 
	throws JMSException, DataAccessException, XmlParseException {
	// send to simulation queue, for waiting jobs, only update the database
	if (queueID == MessageConstants.QUEUE_ID_SIMULATIONJOB) {
		SimulationTaskMessage taskMsg = new SimulationTaskMessage(simTask);
		taskMsg.sendSimulationTask(mainJobDispatcher);
		log.print("do_start(): send job " + simTask.getSimulationJobIdentifier() + " to simJob queue");
	} else {
		log.print("do_start(): job " + simTask.getSimulationJobIdentifier() + " pending");
	}

	// update database
	VCSimulationIdentifier vcSimID = new VCSimulationIdentifier(simTask.getSimKey(), simTask.getSimulationJob().getSimulation().getVersion().getOwner());
	SimulationJobStatus newJobStatus = simDispatcher.updateQueueStatus(oldJobStatus, jobAdminXA, con, vcSimID, simTask.getSimulationJob().getJobIndex(), queueID, simTask.getTaskID(), true);

	// tell client
	if (!newJobStatus.compareEqual(oldJobStatus)) {
		StatusMessage message = new StatusMessage(newJobStatus, simTask.getUserName(), null, null);
		message.sendToClient(mainJobDispatcher);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/2001 11:08:09 PM)
 * @param simulation cbit.vcell.solver.Simulation
 */
private void do_stop(Connection con, SimulationJobStatus oldJobStatus, String username, VCSimulationIdentifier vcSimID, 
		int jobIndex) throws JMSException, DataAccessException, UpdateSynchronizationException {
	
	// if the job is in simJob queue, get it out
	KeyValue simKey = vcSimID.getSimulationKey();
	if (oldJobStatus.isQueued()) {
		String queueName = JmsUtils.getQueueSimJob();
		String filter =  MessageConstants.USERNAME_PROPERTY + "='" + username + "' AND " + MessageConstants.SIMKEY_PROPERTY + "=" + simKey
			+ " AND " + MessageConstants.JOBINDEX_PROPERTY + "=" + jobIndex;		
		log.print("Remove job from " + queueName + " queue [" + filter + "]");	
		// get the message out
		mainJobDispatcher.receiveMessage(queueName, filter, 100);
	}
	
	// update database
	SimulationJobStatus newJobStatus = simDispatcher.updateEndStatus(oldJobStatus, jobAdminXA, con, vcSimID, jobIndex, null, 
			SimulationJobStatus.SCHEDULERSTATUS_STOPPED, SimulationMessage.MESSAGE_JOB_STOPPED);
	
	// tell client
	if (!newJobStatus.compareEqual(oldJobStatus)) {
		StatusMessage message = new StatusMessage(newJobStatus, username, null, null);
		message.sendToClient(mainJobDispatcher);
	}

	// send stopSimulation to serviceControl topic
	log.print("send " + MessageConstants.MESSAGE_TYPE_STOPSIMULATION_VALUE + " to " + JmsUtils.getTopicServiceControl() + " topic");
	Message msg = mainJobDispatcher.createMessage();		
	msg.setStringProperty(MessageConstants.MESSAGE_TYPE_PROPERTY, MessageConstants.MESSAGE_TYPE_STOPSIMULATION_VALUE);
	msg.setLongProperty(MessageConstants.SIMKEY_PROPERTY, Long.parseLong(simKey + ""));
	msg.setIntProperty(MessageConstants.JOBINDEX_PROPERTY, jobIndex);
	mainJobDispatcher.publishMessage(JmsUtils.getTopicServiceControl(), msg);	
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/2001 11:08:09 PM)
 * @param simulation cbit.vcell.solver.Simulation
 */
public VCSimulationIdentifier processNextRequest() {
	// A Distributed Transaction for dispatcher sends job to simulation queue directly
	javax.transaction.TransactionManager tm = new JtaTransactionManager();
	boolean join = true;
	JtaDbConnection mainJobDbConnection = null;
	
	//log.print("++PNR");
	
	try {	
		tm.begin();
	
		join = mainJobDispatcher.joinTransaction(tm);
		if (!join) {
			throw new RuntimeException("++PNR: join failed");
		} else {
			Message message = mainJobDispatcher.receiveMessage(JmsUtils.getQueueSimReq(), jobSelector, 100);
			if (message == null) {			
				try {
					tm.rollback();
				} catch (Exception ex) {
					log.exception(ex);
				}

				return null;
			} 
			
			if (!(message instanceof ObjectMessage)) {
				tm.commit(); // ignore the bad messages
				return null;
			}

			Object obj = ((ObjectMessage) message).getObject();
			if (!(obj instanceof RpcRequest)) {
				tm.commit(); // ignore the bad messages
				return null;
			}			
			RpcRequest request = (RpcRequest)obj;			
			VCSimulationIdentifier vcSimID = (VCSimulationIdentifier)request.getArguments()[0];
			User user = request.getUser();

			VCMongoMessage.sendRpcRequestReceived(request);
			log.print("++PNR: " + request);

			mainJobDbConnection = new JtaOracleConnection(conFactory);	
			
			join = mainJobDbConnection.joinTransaction(tm);

			if (!join) {
				throw new RuntimeException("++PNR: join failed");
			} else {
				if (request.getMethodName().equals(METHOD_NAME_STARTSIMULATION)) {
					startSimulation(mainJobDbConnection.getConnection(), user, vcSimID);
				} else if (request.getMethodName().equals(METHOD_NAME_STOPSIMULATION)) {
					stopSimulation(mainJobDbConnection.getConnection(), user, vcSimID);
				}	

				tm.commit();
			}
			
			return vcSimID;
		}
			
	} catch (Exception e){
		log.exception(e);
		
		try {
			tm.rollback();
		} catch (Exception ex) {
			log.exception(ex);
		}

		try {
			if (mainJobDbConnection != null && AbstractDBTopLevel.isBadConnection(mainJobDbConnection.getConnection(), log)) {
				mainJobDbConnection.closeOnFailure();
				mainJobDbConnection = null;
			}
		} catch (java.sql.SQLException sqlex) {
			log.exception(sqlex);
		}
	} finally {
		try {
			if (mainJobDbConnection != null) {
				mainJobDbConnection.close();
				mainJobDbConnection = null;
			}
		} catch (SQLException ex) {
			log.exception(ex);
		}
	}

	return null;
}


/**
 * Insert the method's description here.
 * Creation date: (11/19/2001 5:29:47 PM)
 */
protected void reconnect() throws JMSException {
	// msg filter selector 
	jobSelector =  MessageConstants.MESSAGE_TYPE_PROPERTY + "='" + MessageConstants.MESSAGE_TYPE_RPC_SERVICE_VALUE  + "' AND " 
		+ MessageConstants.SERVICE_TYPE_PROPERTY + "='" + simDispatcher.getServiceType() + "'";	
	
	super.reconnect();
	
	jmsXAConn = jmsConnFactory.createXAConnection();
	mainJobDispatcher = jmsXAConn.getXASession();		
	jmsXAConn.startConnection();
	
	jmsConn.startConnection();
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/2001 11:08:09 PM)
 * @param simulation cbit.vcell.solver.Simulation
 */
private void startSimulation(Connection con, User user, VCSimulationIdentifier vcSimID) throws JMSException, DataAccessException, XmlParseException {
	if (!user.equals(vcSimID.getOwner())) {
		log.alert(user + " is not authorized to start simulation " + vcSimID);
		StatusMessage message = new StatusMessage(new SimulationJobStatus(VCellServerID.getSystemServerID(), vcSimID, 0, null, 
			SimulationJobStatus.SCHEDULERSTATUS_FAILED, 0, SimulationMessage.MESSAGE_JOB_FAILED_NOTAUTHORIZED, null, null), user.getName(), null, null);
		message.sendToClient(mainJobDispatcher);
	} else {
		KeyValue simKey = vcSimID.getSimulationKey();
		Simulation simulation = null;
		FieldDataIdentifierSpec[] fdis = null;
		try {
			simulation = simDispatcher.getSimulation(user, simKey);
		} catch (DataAccessException ex) {
			log.alert("Bad simulation " + vcSimID);
			StatusMessage message = new StatusMessage(new SimulationJobStatus(VCellServerID.getSystemServerID(), vcSimID, -1, null, 
				SimulationJobStatus.SCHEDULERSTATUS_FAILED, 0, SimulationMessage.jobFailed("Failed to dispatch simuation: " + ex.getMessage()), null, null), user.getName(), null, null);
			message.sendToClient(mainJobDispatcher);
			return;
		}
		if (simulation != null) {
			if (simulation.getScanCount() > Integer.parseInt(PropertyLoader.getRequiredProperty(PropertyLoader.maxJobsPerScan))) {
				log.alert("Too many simulations (" + simulation.getScanCount() + ") for parameter scan." + vcSimID);
				StatusMessage message = new StatusMessage(new SimulationJobStatus(VCellServerID.getSystemServerID(), vcSimID, -1, null, 
					SimulationJobStatus.SCHEDULERSTATUS_FAILED, 0, SimulationMessage.jobFailed("Too many simulations (" + simulation.getScanCount() 
							+ ") for parameter scan."), null, null), user.getName(), null, null);
				message.sendToClient(mainJobDispatcher);
				return;
			}
			for (int i = 0; i < simulation.getScanCount(); i++){
				// right now, we submit a regular task for each scan job...
				// should get smarter in the future for load balancing, quotas, priorities...
				SimulationJobStatus oldJobStatus = jobAdminXA.getSimulationJobStatus(con, simKey, i);
				try {
					fdis = simDispatcher.getFieldDataIdentifierSpecs(simulation);
				} catch (DataAccessException ex) {
					do_failed(con, oldJobStatus, user.getName(), vcSimID, i, SimulationMessage.jobFailed(ex.getMessage()));
					return;
				}
				// if already started by another thread
				if (oldJobStatus != null && !oldJobStatus.isDone()) {
					log.alert("Can't start, simulation[" + vcSimID + "] job [" + i + "] is running already");
				} else {
					int newTaskID = oldJobStatus == null ? 0 : (oldJobStatus.getTaskID() & MessageConstants.TASKID_USERCOUNTER_MASK) + MessageConstants.TASKID_USERINCREMENT;
					SimulationTask simTask = new SimulationTask(new SimulationJob(simulation, i, fdis), newTaskID);
					int queueID = MessageConstants.QUEUE_ID_WAITING;
					// put all the jobs to waiting first, let dispatch thread decide which to dispatch
					do_start(con, oldJobStatus, simTask, queueID);
				}
			}
		} else {
			log.alert("Can't start, simulation [" + vcSimID + "] doesn't exist in database");
			StatusMessage message = new StatusMessage(new SimulationJobStatus(VCellServerID.getSystemServerID(), vcSimID, -1, null, 
				SimulationJobStatus.SCHEDULERSTATUS_FAILED, 0, SimulationMessage.jobFailed("Can't start, simulation [" + vcSimID + "] doesn't exist"), null, null), user.getName(), null, null);
			message.sendToClient(mainJobDispatcher);
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (2/2/2004 2:16:06 PM)
 */
protected void stopService() {	
	try {
		if (jmsXAConn != null) {
			jmsXAConn.close();
		}
	} catch (JMSException ex) {
		log.exception(ex);
	}
	super.stopService();
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/2001 11:08:09 PM)
 * @param simulation cbit.vcell.solver.Simulation
 */
private void stopSimulation(java.sql.Connection con, User user, VCSimulationIdentifier vcSimID) throws SQLException, JMSException, DataAccessException {
	log.print("Stopping simulation");
	if (!user.equals(vcSimID.getOwner())) {
		log.alert(user + " is not authorized to stop simulation " + vcSimID);
		StatusMessage message = new StatusMessage(new SimulationJobStatus(VCellServerID.getSystemServerID(), vcSimID, 0, null, 
			SimulationJobStatus.SCHEDULERSTATUS_FAILED, 0, SimulationMessage.jobFailed("You are not authorized to stop this simulation!"), null, null), user.getName(), null, null);
		message.sendToClient(mainJobDispatcher);			
	} else {
		KeyValue simKey = vcSimID.getSimulationKey();
		Simulation simulation = null;
		try {
			simulation = simDispatcher.getSimulation(user, simKey);
		} catch (DataAccessException ex) {
			log.alert("Bad simulation " + vcSimID);
			StatusMessage message = new StatusMessage(new SimulationJobStatus(VCellServerID.getSystemServerID(), vcSimID, -1, null, 
				SimulationJobStatus.SCHEDULERSTATUS_FAILED, 0, SimulationMessage.jobFailed(ex.getMessage()), null, null), user.getName(), null, null);
			message.sendToClient(mainJobDispatcher);
			return;
		}
		if (simulation != null) {
			for (int i = 0; i < simulation.getScanCount(); i++){
				SimulationJobStatus jobStatus = jobAdminXA.getSimulationJobStatus(con, vcSimID.getSimulationKey(), i);

				if (jobStatus != null) {
					if (!jobStatus.isDone()) {
						do_stop(con, jobStatus, user.getName(), vcSimID, i);
					} else {
						log.alert("Can't stop, simulation [" + vcSimID + "] job [" + i + "] already finished");
					}			
				}  else {
					log.alert("Can't stop, simulation [" + vcSimID + "] job [" + i + "] never ran");
				}
			}
		} else {
			log.alert("Can't stop, simulation [" + vcSimID + "] doesn't exist in database");
			StatusMessage message = new StatusMessage(new SimulationJobStatus(VCellServerID.getSystemServerID(), vcSimID, -1, null, 
				SimulationJobStatus.SCHEDULERSTATUS_FAILED, 0, SimulationMessage.jobFailed("Can't stop, simulation [" + 
						vcSimID + "] doesn't exist"), null, null), user.getName(), null, null);
			message.sendToClient(mainJobDispatcher);
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (2/21/2006 8:59:36 AM)
 * @return int
 */
private static int getHTCPartitionMaximumJobs() {
	return Integer.parseInt(PropertyLoader.getRequiredProperty(PropertyLoader.htcPartitionMaximumJobs));
}


/**
 * Insert the method's description here.
 * Creation date: (2/21/2006 9:01:20 AM)
 * @return cbit.vcell.messaging.db.VCellServerID[]
 */
private static VCellServerID[] getHTCPartitionShareServerIDs() {
	try {
		String lsfPartitionShareServerIDs = PropertyLoader.getRequiredProperty(PropertyLoader.htcPartitionShareServerIDs);
		StringTokenizer st = new StringTokenizer(lsfPartitionShareServerIDs, " ,");
		VCellServerID[] serverIDs = new VCellServerID[st.countTokens() + 1]; // include the current system ServerID
		serverIDs[0] = VCellServerID.getSystemServerID();
		
		int count = 1;
		while (st.hasMoreTokens()) {			
			serverIDs[count] = VCellServerID.getServerID(st.nextToken());
			count ++;			
		}
		return serverIDs;
	} catch (Exception ex) {
		return null;
	}
}
}
