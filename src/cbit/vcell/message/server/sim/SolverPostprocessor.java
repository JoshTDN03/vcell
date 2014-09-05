/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.message.server.sim;
import java.lang.management.ManagementFactory;
import java.util.Collection;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.vcell.util.PropertyLoader;
import org.vcell.util.PropertyLoader.Context;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;
import org.vcell.util.logging.Log4jSessionLog;
import org.vcell.util.logging.Logging;
import org.vcell.util.logging.Logging.ConsoleDestination;

import cbit.vcell.message.VCMessageSession;
import cbit.vcell.message.VCMessagingException;
import cbit.vcell.message.VCMessagingService;
import cbit.vcell.message.messages.WorkerEventMessage;
import cbit.vcell.message.server.ManageUtils;
import cbit.vcell.message.server.ServerMessagingDelegate;
import cbit.vcell.message.server.jmx.VCellServiceMXBean;
import cbit.vcell.message.server.jmx.VCellServiceMXBeanImpl;
import cbit.vcell.mongodb.VCMongoMessage;
import cbit.vcell.mongodb.VCMongoMessage.ServiceName;
import cbit.vcell.solver.VCSimulationIdentifier;
import cbit.vcell.solver.server.SimulationMessage;
import cbit.vcell.tools.PortableCommand;
import cbit.vcell.tools.PortableCommandWrapper;
/**
 * Insert the type's description here.
 * Creation date: (10/25/2001 4:14:09 PM)
 * @author: Jim Schaff
 */
public class SolverPostprocessor  {
	private static final int NUM_STD_ARGS = 6;
	private static final String LOG_NAME = "solverPostprocessor";

	public static void main(java.lang.String[] args) {
		if (args.length < NUM_STD_ARGS) {
			System.out.println("Usage: " + SolverPostprocessor.class.getName() + " simKey username userKey jobindex taskid solverExitCode [postProcessorCommandFile]");
			System.exit(1);
		}

		Logging.init();
		Logging.changeConsoleLogging(ConsoleDestination.STD_ERR, ConsoleDestination.STD_OUT); 
		Log4jSessionLog log = new Log4jSessionLog(LOG_NAME);
		Logger lg = log.getLogger( );
		VCMessagingService vcMessagingService = null;
		
		try {
			
			PropertyLoader.loadProperties(Context.SERVER,true);
			
			KeyValue simKey = new KeyValue(args[0]);
			String userName = args[1];
			KeyValue userKey = new KeyValue(args[2]);
			int jobIndex = Integer.parseInt(args[3]);
			int taskID = Integer.parseInt(args[4]);
			int solverExitCode = Integer.parseInt(args[5]);
			
			User owner = new User(userName,userKey);
			VCSimulationIdentifier vcSimID = new VCSimulationIdentifier(simKey, owner);
			String hostName = ManageUtils.getHostName();
						
			VCMongoMessage.serviceStartup(ServiceName.solverPostprocessor, Integer.valueOf(simKey.toString()), args);

			//
			// JMX registration
			//
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.registerMBean(new VCellServiceMXBeanImpl(), new ObjectName(VCellServiceMXBean.jmxObjectName));
 
	        vcMessagingService = VCMessagingService.createInstance(new ServerMessagingDelegate());
			VCMessageSession session = vcMessagingService.createProducerSession();
			WorkerEventMessage workerEventMessage;
			if (solverExitCode==0){
				
				Exception postProcessingException = null;
				if (args.length > NUM_STD_ARGS) {
					String fname = args[NUM_STD_ARGS];
					if (lg.isTraceEnabled()) {
						lg.trace("processing " + fname);
					}
					postProcessingException = runPostprocessingCommands(fname,lg);
				}
				if (lg.isTraceEnabled()) {
					lg.trace("postProcessingException is " + postProcessingException);
				}
			 
				if (postProcessingException == null) {
					lg.trace("sendWorkerExitNormal");
					workerEventMessage = WorkerEventMessage.sendWorkerExitNormal(session, SolverPostprocessor.class.getName(), hostName, vcSimID, jobIndex, taskID, solverExitCode);
				}
				else {
					lg.trace("sendWorkerExitError postprocessing");
					workerEventMessage = WorkerEventMessage.sendWorkerExitError(session, postProcessingException, hostName, vcSimID, jobIndex, taskID, 
							SimulationMessage.WorkerExited(postProcessingException)); 
				}
				
			}else{ //solverExitCode != 0
				lg.trace("sendWorkerExitError solverExitCode");
				workerEventMessage = WorkerEventMessage.sendWorkerExitError(session, SolverPostprocessor.class.getName(), hostName, vcSimID, jobIndex, taskID, solverExitCode);
			}
			lg.trace(workerEventMessage);
			VCMongoMessage.sendWorkerEvent(workerEventMessage);
			try {
				Thread.sleep(2000);
			}catch (InterruptedException e){
			}
		} catch (Throwable e) {
			log.exception(e);
		} finally {
			if (vcMessagingService!=null){
				try {
					vcMessagingService.closeAll();
				} catch (VCMessagingException e) {
					e.printStackTrace();
				}
			}
			VCMongoMessage.flush();
			System.exit(0);
		}
	}
	
	/**
	 * find any {@link PortableCommand}s in file, execute
	 * @param filename
	 * @param lg logger to use
	 * @return exception, if any, null if all command succeeded
	 */
	private static Exception runPostprocessingCommands(String filename, Logger lg) {
		Collection<PortableCommand> commands = PortableCommandWrapper.getCommands(filename);
		for (PortableCommand cmd : commands) {
				if (lg.isTraceEnabled()) {
					lg.trace("processing " + cmd.getClass( ).getName());
				}
			if (cmd.execute() != 0) {
				if (lg.isEnabledFor(Level.WARN)) {
					lg.warn("post processing auxiliary command failed",cmd.exception());
				}
				return cmd.exception();
			}
		}
		return null;
	}

}
