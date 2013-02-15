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

import org.vcell.util.DataAccessException;
import org.vcell.util.PermissionException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.document.User;

import cbit.sql.ConnectionFactory;
import cbit.sql.KeyFactory;
import cbit.vcell.message.VCMessagingService;
import cbit.vcell.message.server.dispatcher.SimulationDatabase;
import cbit.vcell.message.server.dispatcher.SimulationDatabaseDirect;
import cbit.vcell.modeldb.AdminDBTopLevel;
import cbit.vcell.modeldb.DatabaseServerImpl;
import cbit.vcell.modeldb.LocalAdminDbServer;
import cbit.vcell.modeldb.ResultSetDBTopLevel;
/**
 * This type was created in VisualAge.
 */
public class LocalVCellServerFactory implements VCellServerFactory {
	private VCellServer vcServer = null;
/**
 * LocalVCellConnectionFactory constructor comment.
 */
public LocalVCellServerFactory(String userid, UserLoginInfo.DigestedPassword digestedPassword, String hostName, VCMessagingService vcMessagingService, ConnectionFactory conFactory, KeyFactory keyFactory, SessionLog sessionLog) throws java.sql.SQLException, java.io.FileNotFoundException, DataAccessException {
	try {
		AdminDatabaseServer adminDbServer = new LocalAdminDbServer(conFactory,keyFactory,sessionLog);
		User adminUser = null;
		if (userid!=null && digestedPassword!=null){			
			adminUser = adminDbServer.getUser(userid,digestedPassword);
			if (adminUser==null){
				throw new PermissionException("failed to authenticate user userid "+userid);
			}
			if (!adminUser.getName().equals(PropertyLoader.ADMINISTRATOR_ACCOUNT)){
				throw new PermissionException("userid "+userid+" does not have sufficient privilage");
			}
		}
		AdminDBTopLevel adminDbTopLevel = new AdminDBTopLevel(conFactory, sessionLog);
		ResultSetDBTopLevel resultSetDbTopLevel = new ResultSetDBTopLevel(conFactory, sessionLog);
		DatabaseServerImpl databaseServerImpl = new DatabaseServerImpl(conFactory, keyFactory, sessionLog);
		SimulationDatabase simulationDatabase = new SimulationDatabaseDirect(resultSetDbTopLevel, adminDbTopLevel, databaseServerImpl, sessionLog);
		vcServer = new LocalVCellServer(hostName, vcMessagingService, adminDbServer, simulationDatabase, 0);
	} catch (java.rmi.RemoteException e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage(),e);
	}
}
/**
 * getVCellConnection method comment.
 */
public VCellServer getVCellServer() throws AuthenticationException, ConnectionException {
	return vcServer;
}
}
