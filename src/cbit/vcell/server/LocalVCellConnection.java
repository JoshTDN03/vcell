/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.server;
import java.io.FileNotFoundException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.vcell.util.BeanUtils;
import org.vcell.util.DataAccessException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;

import cbit.rmi.event.DataJobEvent;
import cbit.rmi.event.DataJobListener;
import cbit.rmi.event.ExportEvent;
import cbit.rmi.event.ExportListener;
import cbit.rmi.event.MessageCollector;
import cbit.rmi.event.MessageEvent;
import cbit.rmi.event.MessageService;
import cbit.rmi.event.PerformanceMonitorEvent;
import cbit.rmi.event.SimpleMessageCollector;
import cbit.rmi.event.SimpleMessageService;
import cbit.sql.ConnectionFactory;
import cbit.sql.KeyFactory;
import cbit.vcell.export.server.ExportServiceImpl;
import cbit.vcell.message.server.dispatcher.SimulationDatabase;
import cbit.vcell.modeldb.LocalUserMetaDbServer;
import cbit.vcell.simdata.DataSetControllerImpl;
import cbit.vcell.simdata.LocalDataSetController;
import cbit.vcell.visit.VisitConnectionInfo;
/**
 * The user's connection to the Virtual Cell.  It is obtained from the VCellServer
 * after the user has been authenticated.
 * Creation date: (Unknown)
 * @author: Jim Schaff.
 */
@SuppressWarnings("serial")
public class LocalVCellConnection extends UnicastRemoteObject implements VCellConnection, ExportListener, DataJobListener {
	private SimulationController simulationController = null;
	private SimulationControllerImpl simulationControllerImpl = null;
	private ExportServiceImpl exportServiceImpl = null;
	private DataSetControllerImpl dataSetControllerImpl = null;
	private UserMetaDbServer userMetaDbServer = null;
	private SimpleMessageService messageService = new SimpleMessageService();
	private SimpleMessageCollector messageCollector = new SimpleMessageCollector();
	//
	private UserLoginInfo userLoginInfo;

	//
	// database resources
	//
	private static ConnectionFactory conFactory = null;
	private static KeyFactory keyFactory = null;

	
	private SessionLog fieldSessionLog = null;
	private String fieldHost = null;
	private PerformanceMonitoringFacility performanceMonitoringFacility;
	private LocalDataSetController localDataSetController;

/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public LocalVCellConnection(UserLoginInfo userLoginInfo, String host, SessionLog sessionLog, SimulationDatabase simulationDatabase, DataSetControllerImpl dataSetControllerImpl, ExportServiceImpl exportServiceImpl) throws RemoteException, java.sql.SQLException, FileNotFoundException {
	super(PropertyLoader.getIntProperty(PropertyLoader.rmiPortVCellConnection,0));
	this.userLoginInfo = userLoginInfo;
	this.fieldHost = host;
	this.fieldSessionLog = sessionLog;
	this.simulationControllerImpl = new SimulationControllerImpl(sessionLog, simulationDatabase, this);
	sessionLog.print("new LocalVCellConnection(" + userLoginInfo.getUserName() + ")");
	
	messageCollector.addMessageListener(messageService);
	
	this.exportServiceImpl = exportServiceImpl;
	this.dataSetControllerImpl = dataSetControllerImpl;
	this.exportServiceImpl.addExportListener(this);
	this.dataSetControllerImpl.addDataJobListener(this);

	performanceMonitoringFacility = new PerformanceMonitoringFacility(this.userLoginInfo.getUser(), sessionLog);	
}


/**
 * Insert the method's description here.
 * Creation date: (4/2/2001 2:59:05 AM)
 * @param event cbit.rmi.event.ExportEvent
 */
public void exportMessage(ExportEvent event) {
	// if it's from one of our jobs, pass it along so it will reach the client
	if (getUserLoginInfo().getUser().equals(event.getUser())) {
		messageService.messageEvent(event);
	}
}

public VisitConnectionInfo createNewVisitConnection() {
	VisitConnectionInfo visitConnectionInfo = VisitConnectionInfo.createHardCodedVisitConnectionInfo(getUserLoginInfo().getUser());
	
	return visitConnectionInfo;
}

/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
public DataSetController getDataSetController() throws RemoteException, DataAccessException {
	getSessionLog().print("LocalVCellConnection.getDataSetController()");
	if (localDataSetController == null) {
		localDataSetController = new LocalDataSetController(this, getSessionLog(), dataSetControllerImpl, exportServiceImpl, getUserLoginInfo().getUser());
	}

	return localDataSetController;
}


/**
 * Insert the method's description here.
 * Creation date: (1/29/2003 5:07:46 PM)
 * @return java.lang.String
 */
public String getHost() {
	return fieldHost;
}


/**
 * Insert the method's description here.
 * Creation date: (6/29/01 10:33:49 AM)
 * @return cbit.rmi.event.SimpleMessageService
 */
MessageService getMessageService() {
	return messageService;
}

MessageCollector getMessageCollector() {
	return messageCollector;
}

/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 * @param simIdentifier java.lang.String
 */
private SessionLog getSessionLog() {
	return (fieldSessionLog);
}

/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.solvers.MathController
 * @param mathDesc cbit.vcell.math.MathDescription
 * @exception java.rmi.RemoteException The exception description.
 */
public SimulationController getSimulationController() throws RemoteException {
	if (simulationController == null){
		simulationController = new LocalSimulationController(getUserLoginInfo().getUser(),simulationControllerImpl,getSessionLog());
	}
	return simulationController;
}


/**
 * Insert the method's description here.
 * Creation date: (3/2/01 11:15:49 PM)
 * @return cbit.vcell.server.URLFinder
 * @exception java.rmi.RemoteException The exception description.
 */
public URLFinder getURLFinder() throws java.rmi.RemoteException {
	try {
		return new URLFinder(	new URL(PropertyLoader.getRequiredProperty(PropertyLoader.tutorialURLProperty)),
								new URL(PropertyLoader.getRequiredProperty(PropertyLoader.userGuideURLProperty)));
	}catch (java.net.MalformedURLException e){
		getSessionLog().exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public UserLoginInfo getUserLoginInfo() {
	return userLoginInfo;
}


/**
 * This method was created by a SmartGuide.
 * @return DBManager
 * @param userid java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
public UserMetaDbServer getUserMetaDbServer() throws RemoteException, DataAccessException {
	getSessionLog().print("LocalVCellConnection.getUserMetaDbServer(" + getUserLoginInfo().getUser() + ")");
	if (userMetaDbServer == null) {
		userMetaDbServer = new LocalUserMetaDbServer(conFactory, keyFactory, getUserLoginInfo().getUser(), getSessionLog());
	}
	return userMetaDbServer;
}


/**
 * This method was created in VisualAge.
 * @param conFactory cbit.sql.ConnectionFactory
 */
static void setDatabaseResources(ConnectionFactory argConFactory, KeyFactory argKeyFactory) {
	conFactory = argConFactory;
	keyFactory = argKeyFactory;
}


public void dataJobMessage(DataJobEvent event) {
	if (getUserLoginInfo().getUser().equals(event.getUser())) {
		messageService.messageEvent(event);
	}
}


public void sendErrorReport(Throwable exception) throws RemoteException {
	BeanUtils.sendErrorReport(exception);
}

public MessageEvent[] getMessageEvents() throws RemoteException {
	return messageService.getMessageEvents();
}


public void reportPerformanceMonitorEvent(PerformanceMonitorEvent performanceMonitorEvent) throws RemoteException {
	performanceMonitoringFacility.performanceMonitorEvent(performanceMonitorEvent);
	
}
}
