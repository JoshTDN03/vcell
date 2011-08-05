/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.server;
import org.vcell.util.DataAccessException;

import cbit.vcell.solver.ode.gui.SimulationStatus;

import cbit.vcell.client.data.OutputContext;
import cbit.vcell.export.server.*;
import cbit.vcell.messaging.db.*;
import cbit.vcell.server.*;
import cbit.vcell.solver.*;

public interface JobManager extends cbit.rmi.event.SimulationJobStatusListener, cbit.vcell.client.SimStatusSender {

/**
 * Insert the method's description here.
 * Creation date: (6/4/2004 3:22:42 PM)
 * @param exportSpecs cbit.vcell.export.server.ExportSpecs
 */
ExportJobStatus getExportJobStatus(ExportSpecs exportSpecs) throws DataAccessException;


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
SimulationStatus getServerSimulationStatus(VCSimulationIdentifier vcSimulationIdentifier) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (6/2/2004 1:42:28 AM)
 * @param exportSpecs cbit.vcell.export.server.ExportSpecs
 */
void startExport(OutputContext outputContext,ExportSpecs exportSpecs) throws DataAccessException;


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
void startSimulation(VCSimulationIdentifier vcSimulationIdentifier) throws DataAccessException;


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
void stopSimulation(VCSimulationIdentifier vcSimulationIdentifier) throws DataAccessException;
}
