package org.vcell.rest;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.restlet.Client;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.ext.wadl.WadlComponent;
import org.restlet.util.Series;
import org.vcell.rest.server.RestDatabaseService;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.StdoutSessionLog;
import org.vcell.util.document.UserLoginInfo;
import org.vcell.util.document.VCellServerID;
import org.vcell.util.logging.WatchLogging;

import cbit.sql.ConnectionFactory;
import cbit.sql.KeyFactory;
import cbit.sql.OracleKeyFactory;
import cbit.sql.OraclePoolingConnectionFactory;
import cbit.vcell.message.VCDestination;
import cbit.vcell.message.VCMessage;
import cbit.vcell.message.VCMessagingService;
import cbit.vcell.message.VCMessagingService.VCMessagingDelegate;
import cbit.vcell.message.VCRpcRequest;
import cbit.vcell.message.server.ManageUtils;
import cbit.vcell.message.server.ServiceInstanceStatus;
import cbit.vcell.message.server.ServiceProvider;
import cbit.vcell.message.server.ServiceSpec.ServiceType;
import cbit.vcell.modeldb.AdminDBTopLevel;
import cbit.vcell.modeldb.DatabaseServerImpl;
import cbit.vcell.modeldb.DbDriver;
import cbit.vcell.modeldb.LocalAdminDbServer;
import cbit.vcell.mongodb.VCMongoMessage;
import cbit.vcell.mongodb.VCMongoMessage.ServiceName;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public class VCellApiMain {
	  /**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WatchLogging.init(TimeUnit.MINUTES.toMillis(5), "vcell.watchLog4JInterval");
			if (args.length!=4){
				System.out.println("usage: VCellApiMain keystorePath keystorePassword javascriptDir (-|logDir)");
				System.exit(1);
			}
			File keystorePath = new File(args[0]);
			if (!keystorePath.isFile()){
				throw new RuntimeException("keystorePath '"+args[0]+"' file not found");
			}
			String keystorePassword = args[1];
			File javascriptDir = new File(args[2]);
			if (!javascriptDir.isDirectory()){
				throw new RuntimeException("javascriptDir '"+args[2]+"' is not a directory");
			}

			PropertyLoader.loadProperties();
			
			//don't use static field -- want to initialize logging first
			Logger lg = Logger.getLogger(VCellApiMain.class);
			lg.trace("properties loaded");
			
			//
			// Redirect output to the logfile (append if exists)
			//
			String logdir = args[3];
			ServiceInstanceStatus serviceInstanceStatus = new ServiceInstanceStatus(VCellServerID.getSystemServerID(), ServiceType.API, 1, ManageUtils.getHostName(), new Date(), true);
			if (lg.isTraceEnabled()) {
				lg.trace("log redirection to " + logdir);
			}
			ServiceProvider.initLog(serviceInstanceStatus, logdir);
			
		    System.out.println("connecting to database");

			VCMessagingService vcMessagingService = null;
			RestDatabaseService restDatabaseService = null;
			UserVerifier userVerifier = null;

			final SessionLog log = new StdoutSessionLog("VCellWebApi");
			KeyFactory keyFactory = new OracleKeyFactory();
			DbDriver.setKeyFactory(keyFactory);
			lg.trace("oracle factory (next)");
			ConnectionFactory conFactory = new OraclePoolingConnectionFactory(log);
			lg.trace("database impl (next)");
			DatabaseServerImpl databaseServerImpl = new DatabaseServerImpl(conFactory, keyFactory, log);
			lg.trace("local db server (next)");
			LocalAdminDbServer localAdminDbServer = new LocalAdminDbServer(conFactory, keyFactory, log);
			lg.trace("admin db server (next)");
			AdminDBTopLevel adminDbTopLevel = new AdminDBTopLevel(conFactory, log);
			
			lg.trace("messaging service (next)");
			vcMessagingService = VCMessagingService.createInstance(new VCMessagingDelegate() {
				
				@Override
				public void onTraceEvent(String string) {
					System.out.println("Trace: "+string);
				}
				
				@Override
				public void onRpcRequestSent(VCRpcRequest vcRpcRequest, UserLoginInfo userLoginInfo, VCMessage vcRpcRequestMessage) {
					System.out.println("request sent:");
				}
				
				@Override
				public void onRpcRequestProcessed(VCRpcRequest vcRpcRequest, VCMessage rpcVCMessage) {
					System.out.println("request processed:");
				}
				
				@Override
				public void onMessageSent(VCMessage message, VCDestination desintation) {
					System.out.println("message sent:");
				}
				
				@Override
				public void onMessageReceived(VCMessage vcMessage, VCDestination vcDestination) {
					System.out.println("message received");
				}
				
				@Override
				public void onException(Exception e) {
					System.out.println("Exception: "+e.getMessage());
					e.printStackTrace();
				}
			});
							
			lg.trace("rest database service (next)");
			restDatabaseService = new RestDatabaseService(databaseServerImpl, localAdminDbServer, vcMessagingService, log);
			
			lg.trace("use verifier (next)");
			userVerifier = new UserVerifier(adminDbTopLevel);

			lg.trace("mongo (next)");
			VCMongoMessage.enabled=true;
			VCMongoMessage.serviceStartup(ServiceName.unknown, new Integer(8080), args);

			System.out.println("setting up server configuration");

			lg.trace("register engine (next)");
			Engine.register(true);

			WadlComponent component = new WadlComponent();
			//Server httpServer = component.getServers().add(Protocol.HTTP, 80);
			//Server httpsServer = component.getServers().add(Protocol.HTTPS, 443);
			
//			Client httpsClient = component.getClients().add(Protocol.HTTPS);
//			Client httpClient = component.getClients().add(Protocol.HTTP);
			lg.trace("adding FILE protcol");
			@SuppressWarnings("unused")
			Client httpClient = component.getClients().add(Protocol.FILE);
			lg.trace("adding CLAP protcol");
			@SuppressWarnings("unused")
			Client clapClient = component.getClients().add(Protocol.CLAP);
			
			lg.trace("adding CLAP https");
	        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES"); 
	        SecretKey key = kf.generateSecret(new PBEKeySpec(PropertyLoader.getRequiredProperty(PropertyLoader.dbPassword).toCharArray())); 
	        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES"); 
	        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(new byte[] {32,11,55,121,01,42,89,11}, 20)); 
	        keystorePassword = new String(pbeCipher.doFinal(DatatypeConverter.parseBase64Binary(keystorePassword))); 
			Server httpsServer = component.getServers().add(Protocol.HTTPS,8080);
			Series<Parameter> parameters = httpsServer.getContext().getParameters();
			parameters.add("keystorePath", keystorePath.toString());
			parameters.add("keystorePassword", keystorePassword);
			parameters.add("keystoreType", "JKS");
			parameters.add("keyPassword", keystorePassword);
			parameters.add("disabledCipherSuites",
					"SSL_RSA_WITH_3DES_EDE_CBC_SHA "
					+ "SSL_DHE_RSA_WITH_DES_CBC_SHA "
					+ "SSL_DHE_DSS_WITH_DES_CBC_SHA");
			parameters.add("enabledCipherSuites",
					"TLS_DHE_DSS_WITH_AES_128_CBC_SHA "
					+ "TLS_DHE_RSA_WITH_AES_128_CBC_SHA "
					+ "TLS_RSA_WITH_AES_128_CBC_SHA "
					+ "TLS_DHE_DSS_WITH_AES_256_CBC_SHA "
					+ "TLS_DHE_RSA_WITH_AES_256_CBC_SHA "
					+ "TLS_RSA_WITH_AES_256_CBC_SHA");
			
			lg.trace("create config");
			Configuration templateConfiguration = new Configuration();
			templateConfiguration.setObjectWrapper(new DefaultObjectWrapper());
			
			lg.trace("create app");
			WadlApplication app = new VCellApiApplication(restDatabaseService, userVerifier,templateConfiguration,javascriptDir);
			lg.trace("attach app");
			component.getDefaultHost().attach(app);  

			System.out.println("component start()");
			lg.trace("start component");
			component.start();
			System.out.println("component ended.");
			lg.trace("component started");

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
