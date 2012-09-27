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
import java.util.Date;

import javax.jms.*;

import org.vcell.util.DataAccessException;
import org.vcell.util.MessageConstants.ServiceType;
import org.vcell.util.document.VCellServerID;

import cbit.vcell.messaging.admin.ManageUtils;
import cbit.vcell.messaging.admin.ServiceInstanceStatus;
import cbit.vcell.messaging.RpcServerMessaging;

/**
 * Insert the type's description here.
 * Creation date: (10/18/2001 4:31:11 PM)
 * @author: Jim Schaff
 */
public abstract class JmsRpcServer extends AbstractJmsServiceProvider implements RpcServer {	
	protected RpcServerMessaging rpcServerMessaging = null;	

/**
 * Scheduler constructor comment.
 */
public JmsRpcServer(ServiceType serviceType, int serviceOrdinal, String queueName, String filter, String logdir) throws Exception {
	serviceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(), serviceType, serviceOrdinal, ManageUtils.getHostName(), new Date(), true);
	initLog(logdir);
	
	log = new org.vcell.util.StdoutSessionLog(serviceInstanceStatus.getID());	
	rpcServerMessaging = new RpcServerMessaging(this, queueName, filter, log);	
}


/**
 * Insert the method's description here.
 * Creation date: (1/3/2002 5:41:53 PM)
 * @return java.lang.Object
 * @param user cbit.vcell.server.User
 * @param methodName java.lang.String
 * @param args java.lang.Object[]
 * @exception java.lang.Exception The exception description.
 */
public final Object dispatchRPC(RpcRequest request) throws Exception {
	return getRpcServerImpl().rpc(request);
}

/**
 * Insert the method's description here.
 * Creation date: (10/22/2003 2:01:52 PM)
 * @return cbit.vcell.messaging.RpcServerImpl
 */
public abstract RpcServerImpl getRpcServerImpl() throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (5/9/2003 12:07:28 PM)
 */
public final void start() throws JMSException {
	log.print(getClass().getName() + " starting");
	rpcServerMessaging.startListening();
	
	log.print("Start PropertyLoader thread...");
	new PropertyLoaderThread().start();	
}


/**
 * Insert the method's description here.
 * Creation date: (10/23/2001 4:28:05 PM)
 */
public final void stop() {
	log.print(this.getClass().getName() + " ending");
}
}
