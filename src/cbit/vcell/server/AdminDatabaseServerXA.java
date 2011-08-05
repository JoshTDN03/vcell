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
import cbit.vcell.messaging.db.*;
import java.sql.*;

import org.vcell.util.DataAccessException;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;
import org.vcell.util.document.VCellServerID;

import cbit.sql.*;
public interface AdminDatabaseServerXA {
/**
 * Insert the method's description here.
 * Creation date: (10/4/2005 1:06:44 PM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus[]
 * @param con java.sql.Connection
 * @exception org.vcell.util.DataAccessException The exception description.
 */
SimulationJobStatusInfo[] getActiveJobs(java.sql.Connection con, VCellServerID[] serverIDs) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (10/4/2005 1:01:00 PM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus
 * @param con java.sql.Connection
 * @param intervalSeconds long
 * @exception org.vcell.util.DataAccessException The exception description.
 */
SimulationJobStatus getNextObsoleteSimulation(Connection con, long intervalSeconds) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (10/5/2005 1:06:45 PM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus
 * @param con java.sql.Connection
 * @param simKey cbit.sql.KeyValue
 * @param jobIndex int
 * @exception org.vcell.util.DataAccessException The exception description.
 */
SimulationJobStatus getSimulationJobStatus(Connection con, KeyValue simKey, int jobIndex) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (2/14/2006 4:08:59 PM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus[]
 * @param con java.sql.Connection
 * @param bActiveOnly boolean
 * @param userOnly cbit.vcell.server.User
 * @exception org.vcell.util.DataAccessException The exception description.
 */
SimulationJobStatus[] getSimulationJobStatus(Connection con, boolean bActiveOnly, User userOnly) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (10/4/2005 1:43:20 PM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus
 * @param con java.sql.Connection
 * @param simulationJobStatus cbit.vcell.messaging.db.SimulationJobStatus
 * @exception org.vcell.util.DataAccessException The exception description.
 */
SimulationJobStatus insertSimulationJobStatus(Connection con, SimulationJobStatus simulationJobStatus) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (10/4/2005 1:03:06 PM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus
 * @param con java.sql.Connection
 * @param oldSimulationJobStatus cbit.vcell.messaging.db.SimulationJobStatus
 * @param newSimulationJobStatus cbit.vcell.messaging.db.SimulationJobStatus
 * @exception org.vcell.util.DataAccessException The exception description.
 */
SimulationJobStatus updateSimulationJobStatus(Connection con, SimulationJobStatus oldSimulationJobStatus, SimulationJobStatus newSimulationJobStatus) throws DataAccessException;
}
