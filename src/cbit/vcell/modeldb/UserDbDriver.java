/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.modeldb;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.vcell.util.BeanUtils;
import org.vcell.util.DataAccessException;
import org.vcell.util.ObjectNotFoundException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;
import org.vcell.util.document.UserInfo;

import cbit.vcell.server.UserLoginInfo;
/**
 * This type was created in VisualAge.
 */
public class UserDbDriver /*extends DbDriver*/ {
	public static final UserTable userTable = UserTable.table;
	private SessionLog log = null;
/**
 * LocalDBManager constructor comment.
 */
public UserDbDriver(SessionLog sessionLog) {
	super();
	this.log = sessionLog;
}
/**
 * This method was created in VisualAge.
 * @return int
 * @param user java.lang.String
 * @param imageName java.lang.String
 */
public User getUserFromUserid(Connection con, String userid) throws SQLException {
	Statement stmt;
	String sql;
	ResultSet rset;
	log.print("UserDbDriver.getUserFromUserid(userid=" + userid + ")");
	sql = 	"SELECT " + UserTable.table.id + 
			" FROM " + userTable.getTableName() + 
			" WHERE " + UserTable.table.userid + " = '" + userid + "'";
			
	//System.out.println(sql);
	stmt = con.createStatement();
	User user = null;
	try {
		rset = stmt.executeQuery(sql);
		if (rset.next()) {
			KeyValue userKey = new KeyValue(rset.getBigDecimal(UserTable.table.id.toString()));
			user = new User(userid, userKey);
		}
	} finally {
		stmt.close();
	}
	return user;
}
/**
 * This method was created in VisualAge.
 * @return int
 * @param user java.lang.String
 * @param imageName java.lang.String
 */
public User getUserFromUseridAndPassword(Connection con, String userid, UserLoginInfo.DigestedPassword digestedPassword) throws SQLException {
	Statement stmt = null;
	String sql;
	ResultSet rset;
	log.print("UserDbDriver.getUserFromUseridAndPassword(userid='" + userid+",xxx)");
	sql = 	"SELECT " + UserTable.table.id + ","+UserTable.table.digestPW+","+UserTable.table.password+
			" FROM " + userTable.getTableName() + 
			" WHERE " + UserTable.table.userid + " = '" + userid + "'";
			
	//System.out.println(sql);
	
	User user = null;
	try {
		stmt = con.createStatement();
		rset = stmt.executeQuery(sql);
		if (rset.next()) {
			boolean bUserAuthenticated = false;
			KeyValue userKey = new KeyValue(rset.getBigDecimal(UserTable.table.id.toString()));
			UserLoginInfo.DigestedPassword userDBDigestedPassword = null;
			String dbpwStr = rset.getString(UserTable.table.digestPW.toString());
			if(rset.wasNull() || dbpwStr == null || dbpwStr.length() == 0){
				//user created account with VCell version that didn't make digestPassword, so make one
				String clearTextPassword = rset.getString(UserTable.table.password.toString());
				if(rset.wasNull() || clearTextPassword == null || clearTextPassword.length() == 0){
					//this should never happen
					throw new SQLException("Database contains no password for user "+userid);
				}
				//create digestedPassword
				userDBDigestedPassword = updatePasswords(con, userKey, clearTextPassword);
			}else{
				userDBDigestedPassword = UserLoginInfo.DigestedPassword.createAlreadyDigested(dbpwStr);
			}
			if(digestedPassword.equals(userDBDigestedPassword)){
				bUserAuthenticated = true;
			}else{
				//lookup administrator password and match for any user
				rset.close();
				sql = "SELECT "+UserTable.table.digestPW +" FROM "+userTable.getTableName()+" WHERE "+UserTable.table.id +" = "+PropertyLoader.ADMINISTRATOR_ID;
				ResultSet adminRset = stmt.executeQuery(sql);
				if(adminRset.next()){
					String adminDBDigestPassword = adminRset.getString(UserTable.table.digestPW.toString());
					bUserAuthenticated = digestedPassword.equals(UserLoginInfo.DigestedPassword.createAlreadyDigested(adminDBDigestPassword));
				}
			}
			if(bUserAuthenticated){
				user = new User(userid, userKey);
			}
		}
	} finally {
		if(stmt != null){try{stmt.close();}catch(Exception e){e.printStackTrace();}}
	}
	return user;
}
private UserLoginInfo.DigestedPassword updatePasswords(Connection con,KeyValue id,String clearTextPassword) throws SQLException{
	UserLoginInfo.DigestedPassword dbDigestedPassword = new UserLoginInfo.DigestedPassword(clearTextPassword);
	DbDriver.updateCleanSQL(
			con,"UPDATE " + userTable.getTableName() +
			" SET " + 
				userTable.digestPW.getUnqualifiedColName()+" = '" + dbDigestedPassword.getString() + "',"+
				userTable.password.getUnqualifiedColName()+" = '" + clearTextPassword +"' WHERE " + userTable.id + " = " + id);

	return dbDigestedPassword;
}

public void sendLostPassword(Connection con,String userid) throws SQLException, DataAccessException, ObjectNotFoundException {
	User user = getUserFromUserid(con, userid);
	if(user == null){
		throw new ObjectNotFoundException("User name "+userid+" not found.");
	}
	UserInfo userInfo = getUserInfo(con, user.getID());
	String clearTextPassword = "VC"+System.currentTimeMillis()+"";
	try {
		//Reset User Password
		updatePasswords(con,userInfo.id,clearTextPassword);
		//Send new password to user
		PropertyLoader.loadProperties();
		BeanUtils.sendSMTP(
			PropertyLoader.getRequiredProperty(PropertyLoader.vcellSMTPHostName),
			new Integer(PropertyLoader.getRequiredProperty(PropertyLoader.vcellSMTPPort)).intValue(),
			PropertyLoader.getRequiredProperty(PropertyLoader.vcellSMTPEmailAddress),
			userInfo.email,
			"re: VCell Info",
			"Your password has been reset to '"+clearTextPassword+"'.  Login with the new password and change your password as soon as possible."
		);
	} catch (Exception e) {
		e.printStackTrace();
		throw new DataAccessException("Error sending lost password\n"+e.getMessage(),e);
	}
	
}

/**
 * getModel method comment.
 */
public UserInfo getUserInfo(Connection con, KeyValue key) throws SQLException, DataAccessException, ObjectNotFoundException {
	log.print("UserDbDriver.getUserInfo(key=" + key + ")");
	String sql;
	sql = 	" SELECT " + " * " + 
			" FROM " + userTable.getTableName() + 
			" WHERE " + userTable.id + " = " + key;

	//System.out.println(sql);

	//Connection con = conFact.getConnection();
	Statement stmt = con.createStatement();
	UserInfo userInfo = null;
	try {
		ResultSet rset = stmt.executeQuery(sql);

		//  	ResultSetMetaData metaData = rset.getMetaData();
		//  	for (int i=1;i<=metaData.getColumnCount();i++){
		//	  	System.out.println("column("+i+") = "+metaData.getColumnName(i));
		//  	}
		if (rset.next()){
			userInfo = userTable.getUserInfo(rset);
		}
	} finally {
		stmt.close(); // Release resources include resultset
	}
	if (userInfo == null) {
		throw new org.vcell.util.ObjectNotFoundException("UserInfo with id = '" + key + "' not found");
	}
	return userInfo;
}
/**
 * getModel method comment.
 */
public UserInfo[] getUserInfos(Connection con) throws SQLException, DataAccessException, ObjectNotFoundException {
	log.print("UserDbDriver.getUserInfos()");
	String sql;
	sql = " SELECT " + " * " + " FROM " + userTable.getTableName();

	//System.out.println(sql);

	//Connection con = conFact.getConnection();
	Statement stmt = con.createStatement();
	java.util.Vector<UserInfo> userList = null;
	try {
		ResultSet rset = stmt.executeQuery(sql);

		//  	ResultSetMetaData metaData = rset.getMetaData();
		//  	for (int i=1;i<=metaData.getColumnCount();i++){
		//	  	System.out.println("column("+i+") = "+metaData.getColumnName(i));
		//  	}
		userList = new java.util.Vector<UserInfo>();
		UserInfo userInfo;
		while (rset.next()){
			userInfo = userTable.getUserInfo(rset);
			userList.addElement(userInfo);
		}
	} finally {
		stmt.close(); // Release resources include resultset
	}
	if (userList.size() > 0) {
		UserInfo users[] = new UserInfo[userList.size()];
		userList.copyInto(users);
		return users;
	} else {
		return null;
	}
}
/**
 * addModel method comment.
 */
public KeyValue insertUserInfo(Connection con, UserInfo userInfo) throws SQLException {
	if (userInfo == null){
		throw new SQLException("Improper parameters for insertModel");
	}
	log.print("UserDbDriver.insertUserInfo(userinfo="+userInfo+")");
	//Connection con = conFact.getConnection();
	KeyValue key = DbDriver.getNewKey(con);
	insertUserInfoSQL(con,key,userInfo);
	return key;
}
/**
 * This method was created in VisualAge.
 * @param vcimage cbit.image.VCImage
 * @param userid java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
private void insertUserInfoSQL(Connection con, KeyValue key, UserInfo userInfo) throws SQLException {
	if (userInfo == null || con == null){
		throw new IllegalArgumentException("Improper parameters for insertUserSQL");
	}

	String sql;
	sql = "INSERT INTO " + userTable.getTableName() + " " +
			userTable.getSQLColumnList() + " VALUES " +
			userTable.getSQLValueList(key, userInfo);
	DbDriver.updateCleanSQL(con,sql);
	
	sql = "INSERT INTO " + UserStatTable.table.getTableName() + " " +
		UserStatTable.table.getSQLColumnList() + " VALUES " +
		UserStatTable.table.getSQLValueList(key);
	DbDriver.updateCleanSQL(con,sql);
}
/**
 * removeModel method comment.
 */
public void removeUserInfo(Connection con, User user) throws SQLException {
	if (user == null){
		throw new IllegalArgumentException("Improper parameters for removeUser");
	}
	log.print("UserDbDriver.removeUserInfo(user="+user+")");
	//Connection con = conFact.getConnection();
	removeUserInfoSQL(con,user);
}
/**
 * only the owner can delete a Model
 */
private void removeUserInfoSQL(Connection con, User user) throws SQLException {
	if (user == null){
		throw new IllegalArgumentException("Improper parameters for removeUserInfoSQL");
	}

	//Delete geomname row from GeomMainTable
	String sql;
	sql = "DELETE FROM " + userTable.getTableName() +
			" WHERE " + userTable.id+" = "+user.getID();
			
//System.out.println(sql);

	DbDriver.updateCleanSQL(con, sql);
}
/**
 * addModel method comment.
 */
public void updateUserInfo(Connection con, UserInfo userInfo) throws SQLException {
	if (userInfo == null){
		throw new SQLException("Improper parameters for insertModel");
	}
	log.print("UserDbDriver.updateUserInfo(userinfo="+userInfo+")");
	//Connection con = conFact.getConnection();
	updateUserInfoSQL(con,userInfo);
}
/**
 * This method was created in VisualAge.
 * @param vcimage cbit.image.VCImage
 * @param userid java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
private void updateUserInfoSQL(Connection con, UserInfo userInfo) throws SQLException {
	if (userInfo == null || con == null){
		throw new IllegalArgumentException("Improper parameters for updateUserInfoSQL");
	}

	String sql;
	sql =	"UPDATE " + userTable.getTableName() +
			" SET "   + userTable.getSQLUpdateList(userInfo) + 
			" WHERE " + userTable.id     + " = " + userInfo.id;

//System.out.println(sql);
			
	DbDriver.updateCleanSQL(con,sql);
}

public void updateUserStat(Connection con,UserLoginInfo userLoginInfo) throws SQLException {
	User user = getUserFromUserid(con, userLoginInfo.getUserName());
	String sql;
	sql =	"UPDATE " + UserStatTable.table.getTableName() +
			" SET "   +
				UserStatTable.table.loginCount +" = "+
				UserStatTable.table.loginCount+" + 1"+","+
				UserStatTable.table.lastLogin + " = SYSDATE"+
			" WHERE " + UserStatTable.table.userRef + " = " + user.getID();
	DbDriver.updateCleanSQL(con,sql);
	//
	//Find if we have UserLoginInfo entry
	//
	sql = 	" SELECT " + UserLoginInfoTable.table.id.getUnqualifiedColName()+ 
			" FROM " + UserLoginInfoTable.table.getTableName() + 
			" WHERE " + UserLoginInfoTable.getSQLWhereCondition(user.getID(), userLoginInfo);

	Statement stmt = null;
	BigDecimal userLoginInfoID = null;
	try {
		stmt = con.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		if (rset.next()){
			userLoginInfoID = rset.getBigDecimal(UserLoginInfoTable.table.id.getUnqualifiedColName());
		}
	} finally {
		if(stmt != null){stmt.close();} // Release resources include resultset
	}
	if(userLoginInfoID == null){
		sql = 
			"INSERT INTO " + UserLoginInfoTable.table.getTableName() + " " +
			UserLoginInfoTable.table.getSQLColumnList() + " VALUES " +
			UserLoginInfoTable.getSQLValueList(user.getID(), 1,userLoginInfo);
		DbDriver.updateCleanSQL(con, sql);
	}else{
		DbDriver.updateCleanSQL(con, UserLoginInfoTable.getSQLUpdateLoginCount(user.getID(), userLoginInfo));
	}
}

}
