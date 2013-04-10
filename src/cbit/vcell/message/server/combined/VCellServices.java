/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.message.server.combined;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Date;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.StdoutSessionLog;
import org.vcell.util.document.VCellServerID;

import cbit.rmi.event.DataJobListener;
import cbit.rmi.event.ExportListener;
import cbit.sql.ConnectionFactory;
import cbit.sql.KeyFactory;
import cbit.sql.OracleKeyFactory;
import cbit.sql.OraclePoolingConnectionFactory;
import cbit.vcell.export.server.ExportServiceImpl;
import cbit.vcell.message.VCMessage;
import cbit.vcell.message.VCMessageSession;
import cbit.vcell.message.VCMessagingConstants;
import cbit.vcell.message.VCMessagingException;
import cbit.vcell.message.VCMessagingService;
import cbit.vcell.message.VCellTopic;
import cbit.vcell.message.messages.MessageConstants;
import cbit.vcell.message.server.ManageUtils;
import cbit.vcell.message.server.ServerMessagingDelegate;
import cbit.vcell.message.server.ServiceInstanceStatus;
import cbit.vcell.message.server.ServiceProvider;
import cbit.vcell.message.server.ServiceSpec.ServiceType;
import cbit.vcell.message.server.cmd.CommandService;
import cbit.vcell.message.server.cmd.CommandServiceLocal;
import cbit.vcell.message.server.cmd.CommandServiceSsh;
import cbit.vcell.message.server.data.SimDataServer;
import cbit.vcell.message.server.db.DatabaseServer;
import cbit.vcell.message.server.dispatcher.SimulationDatabase;
import cbit.vcell.message.server.dispatcher.SimulationDatabaseDirect;
import cbit.vcell.message.server.dispatcher.SimulationDispatcher;
import cbit.vcell.message.server.htc.HtcJobID.BatchSystemType;
import cbit.vcell.message.server.htc.HtcProxy;
import cbit.vcell.message.server.htc.pbs.PbsProxy;
import cbit.vcell.message.server.htc.sge.SgeProxy;
import cbit.vcell.message.server.jmx.VCellServiceMXBean;
import cbit.vcell.message.server.jmx.VCellServiceMXBeanImpl;
import cbit.vcell.message.server.sim.HtcSimulationWorker;
import cbit.vcell.modeldb.AdminDBTopLevel;
import cbit.vcell.modeldb.DatabaseServerImpl;
import cbit.vcell.modeldb.DbDriver;
import cbit.vcell.modeldb.ResultSetDBTopLevel;
import cbit.vcell.mongodb.VCMongoMessage;
import cbit.vcell.mongodb.VCMongoMessage.ServiceName;
import cbit.vcell.simdata.Cachetable;
import cbit.vcell.simdata.DataServerImpl;
import cbit.vcell.simdata.DataSetControllerImpl;
import cbit.vcell.solvers.AbstractSolver;

/**
 * Insert the type's description here.
 * Creation date: (10/18/2001 4:31:11 PM)
 * @author: Jim Schaff
 */
public class VCellServices extends ServiceProvider implements ExportListener, DataJobListener {
	
	private SimulationDispatcher simulationDispatcher = null;
	private DatabaseServer databaseServer = null;
	private SimDataServer simDataServer = null;
	private SimDataServer exportDataServer = null;
	private HtcSimulationWorker htcSimulationWorker = null;
	
	private DataServerImpl dataServerImpl = null;
	private DatabaseServerImpl databaseServerImpl = null;
	private SimulationDatabase simulationDatabase = null;
	private HtcProxy htcProxy = null;
	/**
	 * Scheduler constructor comment.
	 */
	public VCellServices(HtcProxy htcProxy, VCMessagingService vcMessagingService, ServiceInstanceStatus serviceInstanceStatus, DatabaseServerImpl databaseServerImpl, DataServerImpl dataServerImpl, SimulationDatabase simulationDatabase, SessionLog log) throws Exception {
		super(vcMessagingService,serviceInstanceStatus,log,false);
		this.htcProxy = htcProxy;
		this.vcMessagingService = vcMessagingService;
		this.databaseServerImpl = databaseServerImpl;
		this.dataServerImpl = dataServerImpl;
		this.simulationDatabase = simulationDatabase;
	}


	public void init() throws Exception{
		initControlTopicListener();
		
		ServiceInstanceStatus dispatcherServiceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(),ServiceType.DISPATCH,99,ManageUtils.getHostName(), new Date(), true);
		simulationDispatcher = new SimulationDispatcher(htcProxy, vcMessagingService, dispatcherServiceInstanceStatus, simulationDatabase, new StdoutSessionLog("DISPATCH"), true);
		simulationDispatcher.init();

		ServiceInstanceStatus databaseServiceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(),ServiceType.DB,99,ManageUtils.getHostName(), new Date(), true);
		databaseServer = new DatabaseServer(databaseServiceInstanceStatus,databaseServerImpl,vcMessagingService,new StdoutSessionLog("DB"), true);
		databaseServer.init();

		ServiceInstanceStatus simDataServiceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(),ServiceType.DATA,99,ManageUtils.getHostName(), new Date(), true);
		simDataServer = new SimDataServer(simDataServiceInstanceStatus,dataServerImpl,vcMessagingService,new StdoutSessionLog("DATA"), true);
		simDataServer.init();

		ServiceInstanceStatus dataExportServiceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(),ServiceType.DATAEXPORT,99,ManageUtils.getHostName(), new Date(), true);
		exportDataServer = new SimDataServer(dataExportServiceInstanceStatus,dataServerImpl,vcMessagingService,new StdoutSessionLog("EXPORTDATA"), true);
		exportDataServer.init();

		ServiceInstanceStatus htcServiceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(),ServiceType.PBSCOMPUTE,99,ManageUtils.getHostName(), new Date(), true);
		htcSimulationWorker = new HtcSimulationWorker(htcProxy, vcMessagingService, htcServiceInstanceStatus,new StdoutSessionLog("PBSCOMPUTE"), true);
		htcSimulationWorker.init();
	}

	@Override
	public void stopService(){
		super.stopService();
		
		simulationDispatcher.stopService();
		databaseServer.stopService();
		simDataServer.stopService();
		exportDataServer.stopService();
		htcSimulationWorker.stopService();
	}


	/**
	 * Starts the application.
	 * @param args an array of command-line arguments
	 */
	public static void main(java.lang.String[] args) {
		if (args.length != 3 && args.length != 6) {
			System.out.println("Missing arguments: " + SimulationDispatcher.class.getName() + " serviceOrdinal (logdir|-) (PBS|SGE) [pbshost userid pswd] ");
			System.exit(1);
		}

		try {
			PropertyLoader.loadProperties();
			CommandService.bQuiet = true;

			int serviceOrdinal = Integer.parseInt(args[0]);
			String logdir = null;
			if (args.length > 1) {
				logdir = args[1];
			}
			
			BatchSystemType batchSystemType = BatchSystemType.valueOf(args[2]);
			CommandService commandService = null;
			if (args.length==6){
				String pbsHost = args[3];
				String pbsUser = args[4];
				String pbsPswd = args[5];
				commandService = new CommandServiceSsh(pbsHost,pbsUser,pbsPswd);
				AbstractSolver.bMakeUserDirs = false; // can't make user directories, they are remote.
			}else{
				commandService = new CommandServiceLocal();
			}
			HtcProxy htcProxy = null;
			switch(batchSystemType){
				case PBS:{
					htcProxy = new PbsProxy(commandService, PropertyLoader.getRequiredProperty(PropertyLoader.htcUser));
					break;
				}
				case SGE:{
					htcProxy = new SgeProxy(commandService, PropertyLoader.getRequiredProperty(PropertyLoader.htcUser));
					break;
				}
				default: {
					throw new RuntimeException("unrecognized batch scheduling option :"+batchSystemType);
				}
			}
			
			VCMongoMessage.serviceStartup(ServiceName.dispatch, new Integer(serviceOrdinal), args);

			//
			// JMX registration
			//
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.registerMBean(new VCellServiceMXBeanImpl(), new ObjectName(VCellServiceMXBean.jmxObjectName));
 			
			ServiceInstanceStatus serviceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(), 
					ServiceType.MASTER, serviceOrdinal, ManageUtils.getHostName(), new Date(), true);	
			
			initLog(serviceInstanceStatus, logdir);

			final SessionLog log = new StdoutSessionLog(serviceInstanceStatus.getID());

			KeyFactory keyFactory = new OracleKeyFactory();
			DbDriver.setKeyFactory(keyFactory);
			ConnectionFactory conFactory = new OraclePoolingConnectionFactory(log);
			DatabaseServerImpl databaseServerImpl = new DatabaseServerImpl(conFactory, keyFactory, log);
			AdminDBTopLevel adminDbTopLevel = new AdminDBTopLevel(conFactory, log);
			ResultSetDBTopLevel resultSetDbTopLevel = new ResultSetDBTopLevel(conFactory, log);
			SimulationDatabase simulationDatabase = new SimulationDatabaseDirect(resultSetDbTopLevel, adminDbTopLevel, databaseServerImpl,log);

			Cachetable cacheTable = new Cachetable(MessageConstants.MINUTE_IN_MS * 20);
			DataSetControllerImpl dataSetControllerImpl = new DataSetControllerImpl(log, cacheTable, 
					new File(PropertyLoader.getRequiredProperty(PropertyLoader.primarySimDataDirProperty)), 
					new File(PropertyLoader.getRequiredProperty(PropertyLoader.secondarySimDataDirProperty)));
			
			ExportServiceImpl exportServiceImpl = new ExportServiceImpl(log);
			
			DataServerImpl dataServerImpl = new DataServerImpl(log, dataSetControllerImpl, exportServiceImpl);        //add dataJobListener

			VCMessagingService vcMessagingService = VCMessagingService.createInstance(new ServerMessagingDelegate());
			
			VCellServices vcellServices = new VCellServices(htcProxy, vcMessagingService, serviceInstanceStatus, databaseServerImpl, dataServerImpl, simulationDatabase, log);

			dataSetControllerImpl.addDataJobListener(vcellServices);
	        exportServiceImpl.addExportListener(vcellServices);

			vcellServices.init();

		} catch (Throwable e) {
			e.printStackTrace(System.out);
		}
	}

	public void dataJobMessage(cbit.rmi.event.DataJobEvent event) {
		try {
			VCMessageSession dataSession = vcMessagingService.createProducerSession();
			VCMessage dataEventMessage = dataSession.createObjectMessage(event);
			dataEventMessage.setStringProperty(VCMessagingConstants.MESSAGE_TYPE_PROPERTY, MessageConstants.MESSAGE_TYPE_DATA_EVENT_VALUE);
			dataEventMessage.setStringProperty(VCMessagingConstants.USERNAME_PROPERTY, event.getUser().getName());
			
			dataSession.sendTopicMessage(VCellTopic.ClientStatusTopic, dataEventMessage);
			dataSession.close();
		} catch (VCMessagingException ex) {
			log.exception(ex);
		}
	}

	public void exportMessage(cbit.rmi.event.ExportEvent event) {
		try {
			VCMessageSession dataSession = vcMessagingService.createProducerSession();
			VCMessage exportEventMessage = dataSession.createObjectMessage(event);
			exportEventMessage.setStringProperty(VCMessagingConstants.MESSAGE_TYPE_PROPERTY, MessageConstants.MESSAGE_TYPE_EXPORT_EVENT_VALUE);
			exportEventMessage.setStringProperty(VCMessagingConstants.USERNAME_PROPERTY, event.getUser().getName());
			
			dataSession.sendTopicMessage(VCellTopic.ClientStatusTopic, exportEventMessage);
			dataSession.close();
		} catch (VCMessagingException ex) {
			log.exception(ex);
		}
	}


}
