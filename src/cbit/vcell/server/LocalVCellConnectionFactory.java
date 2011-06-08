package cbit.vcell.server;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import org.vcell.util.SessionLog;

import cbit.sql.ConnectionFactory;
import cbit.sql.KeyFactory;
import cbit.sql.OracleKeyFactory;
import cbit.sql.OraclePoolingConnectionFactory;
/**
 * This type was created in VisualAge.
 */
public class LocalVCellConnectionFactory implements VCellConnectionFactory {
	private UserLoginInfo userLoginInfo;
	private SessionLog sessionLog = null;
	private ConnectionFactory connectionFactory = null;

/**
 * LocalVCellConnectionFactory constructor comment.
 */
public LocalVCellConnectionFactory(UserLoginInfo userLoginInfo, SessionLog sessionLog) {
	this.userLoginInfo = userLoginInfo;
	this.sessionLog = sessionLog;
}
/**
 * Insert the method's description here.
 * Creation date: (8/9/2001 12:08:06 PM)
 * @param userID java.lang.String
 * @param password java.lang.String
 */
public void changeUser(UserLoginInfo userLoginInfo) {
	this.userLoginInfo = userLoginInfo;
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.server.VCellConnection
 */
public VCellConnection createVCellConnection() throws AuthenticationException, ConnectionException {
	try {
		if (connectionFactory == null) {
			connectionFactory = new OraclePoolingConnectionFactory(sessionLog);
		}
		KeyFactory keyFactory = new OracleKeyFactory();
		LocalVCellConnection.setDatabaseResources(connectionFactory, keyFactory);
		LocalVCellServer vcServer = (LocalVCellServer)(new LocalVCellServerFactory(null,null,"<<local>>",null,connectionFactory, keyFactory, sessionLog)).getVCellServer();
		return vcServer.getVCellConnection(userLoginInfo);
	} catch (AuthenticationException exc) {
		sessionLog.exception(exc);
		throw exc;
	} catch (Throwable exc) {
		sessionLog.exception(exc);
		throw new ConnectionException(exc.getMessage());
	}
}
/**
 * Insert the method's description here.
 * Creation date: (8/9/2001 12:34:14 PM)
 * @param newConFactory cbit.sql.ConnectionFactory
 */
public void setConnectionFactory(cbit.sql.ConnectionFactory newConnectionFactory) {
	connectionFactory = newConnectionFactory;
}
}
