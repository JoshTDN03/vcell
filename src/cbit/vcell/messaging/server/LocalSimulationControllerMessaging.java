/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.messaging.server;
import org.vcell.util.DataAccessException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.document.User;

import cbit.vcell.server.*;
import cbit.vcell.solver.*;
import cbit.vcell.messaging.db.SimulationJobStatus;

/**
 * Insert the type's description here.
 * Creation date: (2/4/2003 11:08:14 PM)
 * @author: Jim Schaff
 */
public class LocalSimulationControllerMessaging extends java.rmi.server.UnicastRemoteObject implements cbit.vcell.server.SimulationController {
	private User fieldUser = null;
	private org.vcell.util.SessionLog fieldSessionLog = null;
	private RpcSimServerProxy simServerProxy = null;

/**
 * MessagingSimulationController constructor comment.
 * @exception java.rmi.RemoteException The exception description.
 */
public LocalSimulationControllerMessaging(User user, cbit.vcell.messaging.JmsClientMessaging clientMessaging, org.vcell.util.SessionLog log) throws java.rmi.RemoteException, DataAccessException {
	super(PropertyLoader.getIntProperty(PropertyLoader.rmiPortSimulationController,0));
	this.fieldUser = user;
	this.fieldSessionLog = log;

	try {
		simServerProxy = new RpcSimServerProxy(user, clientMessaging, fieldSessionLog);
	} catch (javax.jms.JMSException e){
		e.printStackTrace(System.out);
		throw new RuntimeException("JMS exception creating SimServerProxy: "+e.getMessage());
	}
}


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public void startSimulation(VCSimulationIdentifier vcSimID) {
	fieldSessionLog.print("LocalSimulationControllerMessaging.startSimulation(" + vcSimID + ")");
	simServerProxy.startSimulation(vcSimID);
}


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public void stopSimulation(VCSimulationIdentifier vcSimID) {
	fieldSessionLog.print("LocalSimulationControllerMessaging.stopSimulation(" + vcSimID + ")");
	simServerProxy.stopSimulation(vcSimID);
}
}
