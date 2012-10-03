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
import javax.jms.*;

import org.vcell.util.MessageConstants;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.MessageConstants.ServiceType;

import cbit.vcell.messaging.admin.ManageUtils;
import cbit.vcell.messaging.server.Worker;
import cbit.vcell.messaging.server.SimulationTask;
import cbit.vcell.solver.SimulationMessage;

/**
 * Insert the type's description here.
 * Creation date: (7/2/2003 3:00:59 PM)
 * @author: Fei Gao
 */
public class WorkerMessaging extends JmsServiceProviderMessaging implements ControlTopicListener {
	private JmsSession jobRetriever = null;
	private JmsSession workerEventSession = null;
	private String jobSelector = null;
	private Worker myWorker = null;
	private SimulationTask currentTask = null;	
	private long lastMsgTimeStamp;
	private boolean bProgress = true;
	
	class KeepAliveThread extends Thread {
		public KeepAliveThread() {
			super();
			setName("KeepAliveThread_Worker");
		}	
		public void run() {
			while (true) {
				try {
					sleep(MessageConstants.INTERVAL_PING_SERVER);
				} catch (InterruptedException ex) {
				}
		
				long t = System.currentTimeMillis();
				if (myWorker.isRunning() && lastMsgTimeStamp != 0 && t - lastMsgTimeStamp > MessageConstants.INTERVAL_PING_SERVER) {
					log.print("@@@@Worker:Sending alive message");
					sendWorkerAlive();
				}
			}
		}	
	}	

/**
 * WorkerMessaging constructor comment.
 */
public WorkerMessaging(Worker worker0, SessionLog log0) throws JMSException {
	super(worker0, log0);
	myWorker = worker0;
	reconnect();
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
public SimulationTask getNextTask() { 	
	//
	// create a transactional receive/send to get a "task" object (that this worker can handle) 
	// and send an "accept" status message to the SchedulerControl queue
	//
	//log.print("==GNT");
	currentTask = null;
	
	try {			
		//log.print("Created receiver with filter = " + jobSelector);		
		Message message = jobRetriever.receiveMessage(JmsUtils.getQueueSimJob(), jobSelector, 100);
		if (message == null) { // no message
			try {
				jobRetriever.rollback(); 
			} catch (Exception ex) {
				log.exception(ex);
			}
			currentTask = null;
			
		} else { 
			log.print("received message " + JmsUtils.toString(message));
			SimulationTaskMessage taskMsg = new SimulationTaskMessage(message);
			currentTask = taskMsg.getSimulationTask();
			
			log.print("Job accepted: " + currentTask);
			WorkerEventMessage.sendAccepted(jobRetriever, this, currentTask, ManageUtils.getHostName());
			jobRetriever.commit();
			
			lastMsgTimeStamp = System.currentTimeMillis();
		}
		
	} catch (Exception ex) {
		try {
			jobRetriever.rollback(); 
		} catch (Exception e) {
			log.exception(e);
		}
		currentTask = null;
	}
	
	return currentTask;
}


/**
 * Insert the method's description here.
 * Creation date: (7/2/2003 3:06:25 PM)
 */
protected void reconnect() throws JMSException {
	jobSelector = myWorker.getJobSelector();	
	
	super.reconnect();
	log.print("Job Selector : " + jobSelector);
	jobRetriever = jmsConn.getTransactedSession(); // transactional
	int workerPrefetchCount = Integer.parseInt(PropertyLoader.getProperty(PropertyLoader.jmsWorkerPrefetchCount, "-1"));
	if (workerPrefetchCount > 0) {
		jobRetriever.setPrefetchCount(workerPrefetchCount); // get messages one by one
		jobRetriever.setPrefetchThreshold(0);
	}
	workerEventSession = jmsConn.getAutoSession();		
	
	JmsSession serviceListenTopicSession = jmsConn.getAutoSession();
	serviceListenTopicSession.setupTopicListener(JmsUtils.getTopicServiceControl(), null, new ControlMessageCollector(myWorker));
	jmsConn.startConnection();
	
	if (myWorker.getServiceType() == ServiceType.LOCALCOMPUTE) { // only start the keepalive thread for local worker
		log.print("Start keep alive thread");
		new KeepAliveThread().start();
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
public void sendCompleted(double progress, double timeSec, SimulationMessage simulationMessage) {
	if (currentTask == null) {
		return;
	}

	// have to keep sending the messages because it's important
	try {
		log.print("sendComplete(" + currentTask.getSimulationJobID() + ")");
		WorkerEventMessage.sendCompleted(workerEventSession, this, currentTask, ManageUtils.getHostName(),  progress, timeSec, simulationMessage);
		
		lastMsgTimeStamp = System.currentTimeMillis();
	} catch (JMSException jmse) {
        log.exception(jmse);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
public void sendFailed(SimulationMessage failureMessage) {
	if (currentTask == null) {
		return;
	}
		
	try {
		log.print("sendFailure(" + currentTask.getSimulationJobID() + "," + failureMessage +")");
		WorkerEventMessage.sendFailed(workerEventSession, this, currentTask, ManageUtils.getHostName(), failureMessage);
		
		lastMsgTimeStamp = System.currentTimeMillis();
	} catch (JMSException ex) {
        log.exception(ex);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
public void sendNewData(double progress, double timeSec, SimulationMessage simulationMessage) {
	if (currentTask == null) {
		return;
	}
	
	try {
		long t = System.currentTimeMillis();
		if (bProgress || t - lastMsgTimeStamp > MessageConstants.INTERVAL_PROGRESS_MESSAGE) { // don't send data message too frequently
			log.print("sendNewData(" + currentTask.getSimulationJobID() + "," + (progress * 100) + "%," + timeSec + ")");		
			WorkerEventMessage.sendNewData(workerEventSession, this, currentTask, ManageUtils.getHostName(), progress, timeSec, simulationMessage);
		
			lastMsgTimeStamp = System.currentTimeMillis();
			bProgress = false;
		}
	} catch (JMSException e) {
        log.exception(e);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
public void sendProgress(double progress, double timeSec, SimulationMessage simulationMessage) {
	if (currentTask == null) {
		return;
	}

	try {
		long t = System.currentTimeMillis();
	if (!bProgress || t - lastMsgTimeStamp > MessageConstants.INTERVAL_PROGRESS_MESSAGE 
		|| ((int)(progress * 100)) % 25 == 0) { // don't send progress message too frequently
			log.print("sendProgress(" + currentTask.getSimulationJobID() + "," + (progress * 100) + "%," + timeSec + ")");
			WorkerEventMessage.sendProgress(workerEventSession, this, currentTask, ManageUtils.getHostName(), progress, timeSec, simulationMessage);
			
			lastMsgTimeStamp = System.currentTimeMillis();
			bProgress = true;
		}
	} catch (JMSException e) {
        log.exception(e);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
public void sendStarting(SimulationMessage startingMessage) {
	if (currentTask == null) {
		return;
	}
	
	try {
		log.print("sendStarting(" + currentTask.getSimulationJobID() + ")");
		WorkerEventMessage.sendStarting(workerEventSession, this, currentTask, ManageUtils.getHostName(), startingMessage);
		
		lastMsgTimeStamp = System.currentTimeMillis();
	} catch (JMSException e) {
        log.exception(e);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
void sendWorkerAlive() {
	if (currentTask == null) {
		return;
	}

	// have to keep sending the messages because it's important
	try {
		log.print("sendWorkerAlive(" + currentTask.getSimulationJobID() + ")");
		WorkerEventMessage.sendWorkerAlive(workerEventSession, this, currentTask, ManageUtils.getHostName(), SimulationMessage.MESSAGE_WORKEREVENT_WORKERALIVE);
		
		lastMsgTimeStamp = System.currentTimeMillis();
	} catch (JMSException jmse) {
        log.exception(jmse);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (8/19/2004 11:21:59 AM)
 */
public void startReceiving() throws JMSException {
	jmsConn.startConnection();
}


/**
 * Insert the method's description here.
 * Creation date: (8/19/2004 11:21:59 AM)
 */
public void stopReceiving() throws JMSException {
	jmsConn.stopConnection();
}
}
