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

import cbit.sql.DBCacheTable;
import cbit.vcell.simdata.*;
/**
 * This type was created in VisualAge.
 */
public class LocalVCellServerFactory implements VCellServerFactory {
	private VCellServer vcServer = null;
/**
 * LocalVCellConnectionFactory constructor comment.
 */
public LocalVCellServerFactory(String userid, String password, String hostName, cbit.sql.ConnectionFactory conFactory, cbit.sql.KeyFactory keyFactory, SessionLog sessionLog) throws java.sql.SQLException, java.io.FileNotFoundException, DataAccessException {
	this(userid, password, hostName, null, conFactory, keyFactory, sessionLog);
}
/**
 * LocalVCellConnectionFactory constructor comment.
 */
public LocalVCellServerFactory(String userid, String password, String hostName, cbit.vcell.messaging.JmsConnectionFactory jmsConnFactory, cbit.sql.ConnectionFactory conFactory, cbit.sql.KeyFactory keyFactory, SessionLog sessionLog) throws java.sql.SQLException, java.io.FileNotFoundException, DataAccessException {
	try {
		AdminDatabaseServer adminDbServer = new cbit.vcell.modeldb.LocalAdminDbServer(conFactory,keyFactory,sessionLog);
		User adminUser = null;
		if (userid!=null && password!=null){			
			adminUser = adminDbServer.getUser(userid,password);
			if (adminUser==null){
				throw new PermissionException("failed to authenticate user userid "+userid);
			}
			if (!adminUser.getName().equals(PropertyLoader.ADMINISTRATOR_ACCOUNT)){
				throw new PermissionException("userid "+userid+" does not have sufficient privilage");
			}
		}
		cbit.vcell.modeldb.ResultSetCrawler rsCrawler = new cbit.vcell.modeldb.ResultSetCrawler(conFactory,adminDbServer,sessionLog);
		vcServer = new LocalVCellServer(true, hostName, jmsConnFactory, adminDbServer, rsCrawler, false);
	} catch (java.rmi.RemoteException e){
	}
}
/**
 * getVCellConnection method comment.
 */
public VCellServer getVCellServer() throws AuthenticationException, ConnectionException {
	return vcServer;
}
}
