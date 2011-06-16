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
import cbit.rmi.event.*;
import java.rmi.*;

import org.vcell.util.DataAccessException;

import cbit.vcell.server.*;
import cbit.vcell.client.data.OutputContext;
import cbit.vcell.export.server.*;
/**
 * Insert the type's description here.
 * Creation date: (6/15/2004 2:15:24 AM)
 * @author: Ion Moraru
 */
public class ClientExportController implements ExportController {
	private ClientServerManager clientServerManager = null;

/**
 * Insert the method's description here.
 * Creation date: (6/15/2004 2:18:14 AM)
 * @param csm cbit.vcell.client.server.ClientServerManager
 */
public ClientExportController(ClientServerManager csm) {
	clientServerManager = csm;
}


/**
 * Insert the method's description here.
 * Creation date: (6/15/2004 2:23:57 AM)
 * @return cbit.vcell.client.server.ClientServerManager
 */
private ClientServerManager getClientServerManager() {
	return clientServerManager;
}


/**
 * Insert the method's description here.
 * Creation date: (6/15/2004 2:15:24 AM)
 * @param exportSpecs cbit.vcell.export.server.ExportSpecs
 */
public cbit.vcell.messaging.db.ExportJobStatus getExportJobStatus(ExportSpecs exportSpecs) throws RemoteException {
	return null;
}


/**
 * Insert the method's description here.
 * Creation date: (6/15/2004 2:23:57 AM)
 * @param newClientServerManager cbit.vcell.client.server.ClientServerManager
 */
public void setClientServerManager(ClientServerManager newClientServerManager) {
	clientServerManager = newClientServerManager;
}


/**
 * Insert the method's description here.
 * Creation date: (6/15/2004 2:15:24 AM)
 * @param exportSpecs cbit.vcell.export.server.ExportSpecs
 */
public void startExport(OutputContext outputContext,ExportSpecs exportSpecs) throws RemoteException {
	try {
		ExportEvent event = getClientServerManager().getDataSetController().makeRemoteFile(outputContext,exportSpecs);
		// ignore; we'll get two downloads otherwise... getClientServerManager().getAsynchMessageManager().fireExportEvent(event);
	} catch (DataAccessException exc) {
		throw new RemoteException(exc.getMessage());
	}
}
}
