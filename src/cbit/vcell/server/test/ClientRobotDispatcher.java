/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.server.test;

import cbit.vcell.server.*;
import java.io.*;
import java.util.*;

import org.vcell.util.StdoutSessionLog;
/**
 * Insert the type's description here.
 * Creation date: (3/8/01 3:01:11 PM)
 * @author: Jim Schaff
 */
public class ClientRobotDispatcher {
	private Vector clientRobotList = new Vector();
/**
 * ClientRobotDispatcher constructor comment.
 */
public ClientRobotDispatcher() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (3/8/01 5:31:10 PM)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	try {
		if (args.length!=4){
			System.out.println("usage:\n  ClientRobotDispatcher  numRobots  serverHost userid password");
			System.exit(1);
		}
		int numRobots = Integer.parseInt(args[0]);
		String host = args[1];
		String userid = args[2];
		String password = args[3];

		ClientRobotDispatcher clientRobotDispatcher = new ClientRobotDispatcher();
		
		clientRobotDispatcher.test(numRobots,host,userid,password);

	}catch (Throwable e){
		e.printStackTrace(System.out);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (3/8/01 5:50:53 PM)
 * @param numRobots int
 * @param host java.lang.String
 * @param userid java.lang.String
 * @param password java.lang.String
 */
private void test(int numRobots, String host, String userid, String password) {
	
	StdoutSessionLog globalLog = new StdoutSessionLog("globalLog");
	Thread threads[] = new Thread[numRobots];

	for (int i=0;i<numRobots;i++){
		String robotName = "robot"+i;
		MixedSessionLog robotLog = new MixedSessionLog(new StringSessionLog(robotName),globalLog);
		this.clientRobotList.addElement(new ClientRobot(robotName,host,userid,password,robotLog));
	}

	for (int i=0;i<clientRobotList.size();i++){
		ClientRobot clientRobot = (ClientRobot)clientRobotList.elementAt(i);
		threads[i] = new Thread(clientRobot);
		threads[i].run();
	}

	//
	// wait for robots to finish
	//
	for (int i=0;i<numRobots;i++){
		try {
			threads[i].wait();
		}catch (InterruptedException e){
			e.printStackTrace(System.out);
		}
	}
	
	System.out.println("printing Robot Summaries by robot");
	
	for (int i=0;i<clientRobotList.size();i++){
		ClientRobot clientRobot = (ClientRobot)clientRobotList.elementAt(i);
		System.out.println(clientRobot.getActivityLog());
	}
}
}
