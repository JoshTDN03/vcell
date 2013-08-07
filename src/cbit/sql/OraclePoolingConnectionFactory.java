/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.vcell.util.ConfigurationException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.StdoutSessionLog;

/**
 * This type was created in VisualAge.y
 */
public final class OraclePoolingConnectionFactory implements ConnectionFactory  {

//	private UniversalConnectionPoolManager connectionPoolManaager = null;
	private String connectionCacheName = null;
	private PoolDataSource poolDataSource = null;
	private SessionLog log = null;
//	private TimerTask refreshConnectionTask = new TimerTask() {
//		public void run() {
//			refreshConnections();
//		}
//	};

public OraclePoolingConnectionFactory(SessionLog sessionLog) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException, ConfigurationException, UniversalConnectionPoolException {
	this(sessionLog, PropertyLoader.getRequiredProperty(PropertyLoader.dbDriverName), 
			PropertyLoader.getRequiredProperty(PropertyLoader.dbConnectURL), 
			PropertyLoader.getRequiredProperty(PropertyLoader.dbUserid), 
			PropertyLoader.getRequiredProperty(PropertyLoader.dbPassword));	
}

public OraclePoolingConnectionFactory(SessionLog sessionLog, String argDriverName, String argConnectURL, String argUserid, String argPassword) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException, UniversalConnectionPoolException {
	this.log = sessionLog;
	connectionCacheName = "UCP_ManagedPool_" + System.nanoTime();

//	connectionPoolManaager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
//	connectionPoolManaager.setJmxEnabled(true);
	poolDataSource = PoolDataSourceFactory.getPoolDataSource();
	poolDataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
	poolDataSource.setConnectionPoolName(connectionCacheName);
	// set DataSource properties
	poolDataSource.setURL(argConnectURL);
	poolDataSource.setUser(argUserid);
	poolDataSource.setPassword(argPassword);

//	connectionPoolManaager.createConnectionPool((UniversalConnectionPoolAdapter)poolDataSource);

	// set cache properties    
	poolDataSource.setMinPoolSize(2);
	poolDataSource.setMaxPoolSize(5);
	poolDataSource.setInitialPoolSize(2);
	
	testConnection();
	
//	Timer timer = new Timer();
//	timer.schedule(refreshConnectionTask, 2*60*1000, 2*60*1000);
}

public synchronized void closeAll() throws java.sql.SQLException {
}
/**
 * This method was created in VisualAge.
 * @param con java.sql.Connection
 */
public void failed(Connection con, Object lock) throws SQLException {
	log.print("OraclePoolingConnectionFactory.failed("+con+")");
	release(con, lock);
	// Get singleton ConnectionCacheManager instance
//	try {
//		connectionPoolManaager.refreshConnectionPool(connectionCacheName);
//	} catch (UniversalConnectionPoolException e) {
//		log.exception(e);
//	}
}

//private synchronized void refreshConnections() {
//	try {
//		connectionPoolManaager.refreshConnectionPool(connectionCacheName);
//	} catch (UniversalConnectionPoolException e) {
//		log.exception(e);
//	}
//}

public synchronized Connection getConnection(Object lock) throws SQLException {
	Connection conn = null;
	try {
		conn = poolDataSource.getConnection();
	} catch (SQLException ex) {
		// might be invalid or stale connection
		log.exception(ex);
		// refresh cache
//		try {
//			connectionPoolManaager.refreshConnectionPool(connectionCacheName);
//		} catch (UniversalConnectionPoolException e) {
//			log.exception(e);
//		}
		// get connection again.
		conn = poolDataSource.getConnection();
	}
	if (conn == null) {
		throw new SQLException("Cannot get a connection to the database. This could be caused by\n" +
				"1. Max connection limit has reached. No connections are available.\n" +
				"2. there is a problem with database server.\n" +
				"3. there is a problem with network.\n");
	}
	return conn;
}
/**
 * This method was created in VisualAge.
 * @param con java.sql.Connection
 */
public void release(Connection con, Object lock) throws SQLException {
	if (con != null) {
		con.close();
	}
}

public void testConnection() throws SQLException{
	Object lock = new Object();
	Connection con = null;
	try {
		con = getConnection(lock);
		String sql = " SELECT DUMMY FROM DUAL";
		Statement stmt = con.createStatement();
		try {
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next()) {
				String value = rset.getString(1);
			} else {
				throw new RuntimeException("Could not get new Key value");
			}
		} finally {
			stmt.close(); // Release resources include resultset
		}
	}finally{
		if (con!=null){
			release(con, lock);
		}
		
	}
}

public static void main(String[] args) {
	try {
		PropertyLoader.loadProperties();
	
		StdoutSessionLog sessionLog = new StdoutSessionLog("aa");
		OraclePoolingConnectionFactory fac = new OraclePoolingConnectionFactory(sessionLog);
		System.out.println("test worked");
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
