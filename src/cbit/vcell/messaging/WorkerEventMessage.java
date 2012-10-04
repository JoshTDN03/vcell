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
import java.sql.SQLException;

import cbit.vcell.messaging.server.SimulationDispatcher;
import cbit.vcell.messaging.server.SimulationTask;
import javax.jms.*;

import org.vcell.util.DataAccessException;
import org.vcell.util.MessageConstants;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;

import cbit.vcell.solver.SimulationInfo;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationMessage;
import cbit.rmi.event.WorkerEvent;

/**
 * Insert the type's description here.
 * Creation date: (2/5/2004 12:35:20 PM)
 * @author: Fei Gao
 */
public class WorkerEventMessage {
	private WorkerEvent workerEvent = null;	
	private static final String MESSAGE_TYPE_WORKEREVENT_VALUE	= "WorkerEvent";

	private static final String WORKEREVENT_STATUS = "WorkerEvent_Status";
	private static final String WORKEREVENT_PROGRESS = "WorkerEvent_Progress";
	private static final String WORKEREVENT_TIMEPOINT = "WorkerEvent_TimePoint";
	private static final String WORKEREVENT_STATUSMSG = "WorkerEvent_StatusMsg";
	
/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public WorkerEventMessage(WorkerEvent event) {
	workerEvent = event;
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public WorkerEventMessage(SimulationDispatcher dispatcher, Message message0) throws JMSException, DataAccessException {
	parseMessage(dispatcher, message0);
}


/**
 * Insert the method's description here.
 * Creation date: (3/11/2004 11:32:33 AM)
 * @return cbit.rmi.event.WorkerEvent
 */
public cbit.rmi.event.WorkerEvent getWorkerEvent() {
	return workerEvent;
}


/**
 * Insert the method's description here.
 * Creation date: (2/5/2004 2:19:48 PM)
 * @param message javax.jms.Message
 */
private void parseMessage(SimulationDispatcher dispatcher, Message message) throws JMSException {
	if (message == null) {
		throw new RuntimeException("Null message");
	}	

	try {
		String msgType = (String)JmsUtils.parseProperty(message, MessageConstants.MESSAGE_TYPE_PROPERTY, String.class);
		if (msgType != null && !msgType.equals(MESSAGE_TYPE_WORKEREVENT_VALUE)) {
			throw new RuntimeException("Wrong message");
		}
	} catch (MessagePropertyNotFoundException ex) {
		throw new RuntimeException("Wrong message");
	}
			
	if (message instanceof ObjectMessage) {
		Object obj = ((ObjectMessage)message).getObject();
		if (!(obj instanceof WorkerEvent)) {
			throw new IllegalArgumentException("Expecting " + SimulationInfo.class.getName() + " in message.");
		}
		workerEvent = (WorkerEvent)obj;

		// from c++ executable
	} else if (message instanceof TextMessage) {
		try {
			String msgType = (String)JmsUtils.parseProperty(message, MessageConstants.MESSAGE_TYPE_PROPERTY, String.class);
			if (msgType != null && !msgType.equals(MESSAGE_TYPE_WORKEREVENT_VALUE)) {
				throw new RuntimeException("Wrong message"); // wrong message
			}
			int status = ((Integer)JmsUtils.parseProperty(message, WORKEREVENT_STATUS, int.class)).intValue();
			String hostname = (String)JmsUtils.parseProperty(message, MessageConstants.HOSTNAME_PROPERTY, String.class);
			String username = (String)JmsUtils.parseProperty(message, MessageConstants.USERNAME_PROPERTY, String.class);
			int taskID = ((Integer)JmsUtils.parseProperty(message, MessageConstants.TASKID_PROPERTY, int.class)).intValue();
			int jobIndex = ((Integer)JmsUtils.parseProperty(message, MessageConstants.JOBINDEX_PROPERTY, int.class)).intValue();
			Long longkey = (Long)JmsUtils.parseProperty(message, MessageConstants.SIMKEY_PROPERTY, long.class);

			KeyValue simKey = new KeyValue(longkey + "");
			Simulation sim = null;
			try {
				User user = dispatcher.getUser(simKey, username);
				sim = dispatcher.getSimulation(user, simKey);			
				if (sim == null) {
					throw new RuntimeException("Null Simulation"); //wrong message	
				}
			} catch (DataAccessException ex) {
				throw new RuntimeException("Null Simulation"); // wrong message
			} catch (SQLException e) {
				throw new RuntimeException("Null Simulation"); // wrong message
			}
			
			String statusMessage = null;
			Double progress = null;
			Double timepoint = null;
			
			try {
				statusMessage = (String)JmsUtils.parseProperty(message, WORKEREVENT_STATUSMSG, String.class);
			} catch (MessagePropertyNotFoundException ex) {
				// it's OK not to have status message
			}

			try {
				progress = (Double)JmsUtils.parseProperty(message, WORKEREVENT_PROGRESS, double.class);
				timepoint = (Double)JmsUtils.parseProperty(message, WORKEREVENT_TIMEPOINT, double.class);
			} catch (MessagePropertyNotFoundException ex) {
				// it's OK not to have progress or timepoint
			}
			
			SimulationMessage simulationMessage = SimulationMessage.fromSerializedMessage(statusMessage);
			if (simulationMessage == null) {			
				switch (status) {
				case WorkerEvent.JOB_ACCEPTED:
					throw new RuntimeException("unexpected job_accepted status");
				case WorkerEvent.JOB_STARTING:
					if (statusMessage == null) {
						simulationMessage = SimulationMessage.MESSAGE_WORKEREVENT_STARTING;
					} else {
						simulationMessage = SimulationMessage.workerStarting(statusMessage);
					}
					break;
				case WorkerEvent.JOB_DATA:
					simulationMessage = SimulationMessage.workerData(timepoint);
					break;
				case WorkerEvent.JOB_PROGRESS:
					simulationMessage = SimulationMessage.workerProgress(progress);
					break;
				case WorkerEvent.JOB_FAILURE:
					if (statusMessage == null) {
						simulationMessage = SimulationMessage.MESSAGE_WORKEREVENT_FAILURE;
					} else {
						simulationMessage = SimulationMessage.workerFailure(statusMessage);
					}
					break;
				case WorkerEvent.JOB_COMPLETED:
					if (statusMessage == null) {
						simulationMessage = SimulationMessage.MESSAGE_WORKEREVENT_COMPLETED;
					} else {
						simulationMessage = SimulationMessage.workerCompleted(statusMessage);
					}
					break;
				case WorkerEvent.JOB_WORKER_ALIVE:
					simulationMessage = SimulationMessage.MESSAGE_WORKEREVENT_WORKERALIVE;
					break;
				default:
					throw new RuntimeException("unexpected worker event status : " + status);
				}
			}

			workerEvent = new WorkerEvent(status, dispatcher, sim.getSimulationInfo().getAuthoritativeVCSimulationIdentifier(), jobIndex, hostname, taskID, progress, timepoint, simulationMessage);
					
		} catch (MessagePropertyNotFoundException ex) {
			throw new RuntimeException("Wrong message"); //wrong message
		} 
	} else {
		throw new IllegalArgumentException("Expecting object message.");
	}

	
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public static WorkerEventMessage sendAccepted(JmsSession session, Object source, SimulationTask simTask, String hostName) throws JMSException {
	WorkerEvent workerEvent = new WorkerEvent(WorkerEvent.JOB_ACCEPTED, source, simTask, hostName, SimulationMessage.MESSAGE_JOB_ACCEPTED);
	WorkerEventMessage workerEventMessage = new WorkerEventMessage(workerEvent);
	workerEventMessage.sendWorkerEvent(session);

	return workerEventMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public static WorkerEventMessage sendCompleted(JmsSession session, Object source, SimulationTask simTask, String hostName, double progress, double timePoint, SimulationMessage simulationMessage) throws JMSException {
	WorkerEvent workerEvent = new WorkerEvent(WorkerEvent.JOB_COMPLETED, source, simTask, hostName, new Double(progress), new Double(timePoint), simulationMessage);		
	WorkerEventMessage workerEventMessage = new WorkerEventMessage(workerEvent);
	workerEventMessage.sendWorkerEvent(session);

	return workerEventMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public static WorkerEventMessage sendFailed(JmsSession session, Object source, SimulationTask simTask, String hostName, SimulationMessage failMessage) throws JMSException {
	WorkerEvent workerEvent = new WorkerEvent(WorkerEvent.JOB_FAILURE, source, simTask,	hostName, failMessage);
	WorkerEventMessage workerEventMessage = new WorkerEventMessage(workerEvent);
	workerEventMessage.sendWorkerEvent(session);

	return workerEventMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public static WorkerEventMessage sendNewData(JmsSession session, Object source, SimulationTask simTask, String hostName, double progress, double timePoint, SimulationMessage simulationMessage) throws JMSException {
	WorkerEvent workerEvent = new WorkerEvent(WorkerEvent.JOB_DATA, source, simTask, hostName, new Double(progress), new Double(timePoint), simulationMessage);		
	WorkerEventMessage workerEventMessage = new WorkerEventMessage(workerEvent);
	workerEventMessage.sendWorkerEvent(session);

	return workerEventMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public static WorkerEventMessage sendProgress(JmsSession session, Object source, SimulationTask simTask, String hostName, double progress, double timePoint, SimulationMessage simulationMessage) throws JMSException {
	WorkerEvent workerEvent = new WorkerEvent(WorkerEvent.JOB_PROGRESS, source, simTask, hostName, new Double(progress), new Double(timePoint), simulationMessage);		
	WorkerEventMessage workerEventMessage = new WorkerEventMessage(workerEvent);
	workerEventMessage.sendWorkerEvent(session);

	return workerEventMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public static WorkerEventMessage sendStarting(JmsSession session, Object source, SimulationTask simTask, String hostName, SimulationMessage startMessage) throws JMSException {
	WorkerEvent workerEvent = new WorkerEvent(WorkerEvent.JOB_STARTING, source, simTask, hostName, startMessage);
	WorkerEventMessage workerEventMessage = new WorkerEventMessage(workerEvent);
	workerEventMessage.sendWorkerEvent(session);

	return workerEventMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (12/31/2003 12:53:34 PM)
 * @param param javax.jms.Message
 */
public static WorkerEventMessage sendWorkerAlive(JmsSession session, Object source, SimulationTask simTask, String hostName, SimulationMessage simulationMessage) throws JMSException {
	WorkerEvent workerEvent = new WorkerEvent(WorkerEvent.JOB_WORKER_ALIVE, source, simTask, hostName, simulationMessage);
	WorkerEventMessage workerEventMessage = new WorkerEventMessage(workerEvent);
	workerEventMessage.sendWorkerEvent(session);

	return workerEventMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 11:20:37 PM)
 */
private void sendWorkerEvent(JmsSession session) throws JMSException {
	session.sendMessage(JmsUtils.getQueueWorkerEvent(), toMessage(session), DeliveryMode.PERSISTENT, MessageConstants.INTERVAL_SERVER_FAIL);
}


/**
 * Insert the method's description here.
 * Creation date: (5/20/2003 1:36:36 PM)
 * @return javax.jms.Message
 */
private Message toMessage(JmsSession session) throws JMSException {		
	Message message = session.createObjectMessage(workerEvent);
	message.setStringProperty(MessageConstants.MESSAGE_TYPE_PROPERTY, MESSAGE_TYPE_WORKEREVENT_VALUE);
	
	return message;
}
}
