/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.solvers;
import java.rmi.RemoteException;

import org.vcell.util.DataAccessException;
import org.vcell.util.SessionLog;

import cbit.vcell.solver.SimulationJob;
import cbit.vcell.solver.SolverNotRunningException;
import cbit.vcell.solver.SolverStatus;
/**
 * This type was created in VisualAge.
 */
public class SolverProxy {
	private SolverController solverController = null;
	private SessionLog sessionLog = null;
	private SimulationJob simulationJob = null;
	private java.util.Date startDate = null;

/**
 * LocalMathController constructor comment.
 * @exception java.rmi.RemoteException The exception description.
 */
public SolverProxy(SessionLog argSessionLog, SimulationJob argSimulationJob) throws java.rmi.RemoteException {
	super();
	this.sessionLog = argSessionLog;
	this.solverController = null;
	this.simulationJob = argSimulationJob;
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
public String getHost() throws java.rmi.RemoteException, DataAccessException {
	return solverController.getHost();
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.solvers.ExecutableStatus
 */
public double getProgress() throws RemoteException, SolverNotRunningException, DataAccessException {
	if (solverController==null){
		throw new SolverNotRunningException("solver not running");
	}
	double progress = solverController.getProgress();
	//
	// store info in state machine
	//
	return progress;
}


/**
 * getMathDescriptionVCML method comment.
 */
public SimulationJob getSimulationJob() throws java.rmi.RemoteException {
	return simulationJob;
}


/**
 * Insert the method's description here.
 * Creation date: (6/28/01 12:14:21 PM)
 * @return cbit.vcell.solvers.SolverController
 */
public SolverController getSolverController() {
	return solverController;
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.solvers.ExecutableStatus
 */
public SolverStatus getSolverStatus() throws RemoteException, DataAccessException {
	if (solverController==null){
		return null;
	}
	SolverStatus status = solverController.getSolverStatus();
	//
	// store info in state machine
	//
	return status;
}


/**
 * Insert the method's description here.
 * Creation date: (7/18/01 12:28:19 PM)
 * @return java.util.Date
 */
public java.util.Date getStartDate() {
	return startDate;
}


/**
 * Insert the method's description here.
 * Creation date: (6/28/01 12:14:21 PM)
 * @param newController cbit.vcell.solvers.SolverController
 */
public void setSolverController(SolverController newSolverController) {
	solverController = newSolverController;
	//
	// default start time (best estimate if solverController actually started directly from primary server).
	// solverProxy is a dummy container on slave servers.
	//
	startDate = new java.util.Date();
}


/**
 * startSimulation method comment.
 */
public void startSimulationJob() throws java.rmi.RemoteException, SimExecutionException, DataAccessException {
	sessionLog.print("SolverProxy.startSimulationJob()");
	if (solverController==null){
		throw new SimExecutionException("proxied solver controller is null");
	}
	solverController.startSimulationJob();
	startDate = new java.util.Date();
	//
	// store info in state machine
	//
}


/**
 * stopSimulation method comment.
 */
public void stopSimulationJob() throws java.rmi.RemoteException, SimExecutionException, DataAccessException {
	sessionLog.print("SolverProxy.stopSimulationJob()");
	if (solverController == null){
		throw new SimExecutionException("proxied solver controller is null");
	}
	solverController.stopSimulationJob();
	//
	// store info in state machine
	//
}
}
