/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.export.server;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.vcell.util.ClientTaskStatusSupport;
import org.vcell.util.DataAccessException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.UserCancelException;
import org.vcell.util.document.User;
import org.vcell.util.document.VCDataIdentifier;

import cbit.rmi.event.ExportEvent;
import cbit.rmi.event.ExportListener;
import cbit.vcell.client.data.OutputContext;
import cbit.vcell.export.nrrd.NrrdInfo;
import cbit.vcell.simdata.DataServerImpl;

/**
 * This type was created in VisualAge.
 */
public class ExportServiceImpl implements ExportConstants, ExportService {
	
	private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

	private Hashtable<Long, User> jobRequestIDs = new Hashtable<Long, User>();
	private Hashtable<ExportSpecs, JobRequest> completedExportRequests = new Hashtable<ExportSpecs, JobRequest>();
	
	private ASCIIExporter asciiExporter = new ASCIIExporter(this);
	private IMGExporter imgExporter = new IMGExporter(this);
	private RasterExporter rrExporter = new RasterExporter(this);

	private SessionLog log = null;

	
		



/**
 * Insert the method's description here.
 * Creation date: (3/29/2001 4:09:29 PM)
 * @param log cbit.vcell.server.SessionLog
 */
public ExportServiceImpl(SessionLog log) {
	this.log = log;
}


/**
 * Insert the method's description here.
 * Creation date: (3/29/2001 5:18:16 PM)
 * @param listener ExportListener
 */
public synchronized void addExportListener(ExportListener listener) {
	listenerList.add(ExportListener.class, listener);
}


/**
 * Insert the method's description here.
 * Creation date: (4/1/2001 11:20:45 AM)
 * @deprecated
 */
protected ExportEvent fireExportCompleted(long jobID, VCDataIdentifier vcdID, String format, String location) {
	User user = null;
	Object object = jobRequestIDs.get(new Long(jobID));
	if (object != null) {
		user = (User)object;
	}
	ExportEvent event = new ExportEvent(this, jobID, user, vcdID, ExportEvent.EXPORT_COMPLETE, format, location, null);
	fireExportEvent(event);
	return event;
}


/**
 * Insert the method's description here.
 * Creation date: (11/17/2000 11:43:22 AM)
 * @param event cbit.rmi.event.ExportEvent
 */
protected void fireExportEvent(ExportEvent event) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Reset the source to allow proper wiring
	event.setSource(this);
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ExportListener.class) {
		((ExportListener)listeners[i+1]).exportMessage(event);
	    }	       
	}
}


/**
 * Insert the method's description here.
 * Creation date: (4/1/2001 11:20:45 AM)
 * @deprecated
 */
protected void fireExportFailed(long jobID, VCDataIdentifier vcdID, String format, String message) {
	User user = null;
	Object object = jobRequestIDs.get(new Long(jobID));
	if (object != null) {
		user = (User)object;
	}
	ExportEvent event = new ExportEvent(this, jobID, user, vcdID, ExportEvent.EXPORT_FAILURE, format, message, null);
	fireExportEvent(event);
}


/**
 * Insert the method's description here.
 * Creation date: (4/1/2001 11:20:45 AM)
 * @param exportSpecs cbit.vcell.export.server.ExportSpecs
 * @param progress double
 * @deprecated
 */
protected void fireExportProgress(long jobID, VCDataIdentifier vcdID, String format, double progress) {
	User user = null;
	Object object = jobRequestIDs.get(new Long(jobID));
	if (object != null) {
		user = (User)object;
	}
	ExportEvent event = new ExportEvent(this, jobID, user, vcdID, ExportEvent.EXPORT_PROGRESS, format, null, new Double(progress));
	fireExportEvent(event);
}


/**
 * Insert the method's description here.
 * Creation date: (4/1/2001 11:20:45 AM)
 * @deprecated
 */
protected void fireExportStarted(long jobID, VCDataIdentifier vcdID, String format) {
	User user = null;
	Object object = jobRequestIDs.get(new Long(jobID));
	if (object != null) {
		user = (User)object;
	}
	ExportEvent event = new ExportEvent(this, jobID, user, vcdID, ExportEvent.EXPORT_START, format, null, null);
	fireExportEvent(event);
}

public ExportEvent makeRemoteFile(OutputContext outputContext,User user, DataServerImpl dataServerImpl, ExportSpecs exportSpecs)throws DataAccessException
{
	return makeRemoteFile(outputContext,user, dataServerImpl, exportSpecs, true);
}

public ExportEvent makeRemoteFile(OutputContext outputContext,User user, DataServerImpl dataServerImpl, ExportSpecs exportSpecs, boolean bSaveAsZip) throws DataAccessException
{
	return makeRemoteFile(outputContext, user, dataServerImpl, exportSpecs, bSaveAsZip, null);
}

public ExportEvent makeRemoteFile(OutputContext outputContext,User user, DataServerImpl dataServerImpl, ExportSpecs exportSpecs, boolean bSaveAsZip, ClientTaskStatusSupport clientTaskStatusSupport) throws DataAccessException {
	// if export completes successfully, we return the generated event for logging
	if (user == null) {
		throw new DataAccessException("ERROR: user is null");
	}
	JobRequest newExportJob = JobRequest.createExportJobRequest(user);
	jobRequestIDs.put(new Long(newExportJob.getJobID()), user);
	log.print("ExportServiceImpl.makeRemoteFile(): " + newExportJob + ", " + exportSpecs);
	String fileFormat = null;
	switch (exportSpecs.getFormat()) {
		case FORMAT_CSV:
			fileFormat = "CSV";
			break;
		case FORMAT_QUICKTIME:
			fileFormat = "MOV";
			break;
		case FORMAT_GIF:
		case FORMAT_ANIMATED_GIF:
			fileFormat = "GIF";
			break;
		case FORMAT_JPEG:
			fileFormat = "JPEG";
			break;
		case FORMAT_NRRD:
			fileFormat = "NRRD";
			break;
	}
	fireExportStarted(newExportJob.getJobID(), exportSpecs.getVCDataIdentifier(), fileFormat);

	try {

		// check required properties first
		String exportBaseURL = null;
		String exportBaseDir = null;
		try {
			exportBaseURL = System.getProperty(PropertyLoader.exportBaseURLProperty);
		} catch (Exception e){
			throw new RuntimeException("required System property \""+PropertyLoader.exportBaseURLProperty+"\" not defined");
		}
		try {
			exportBaseDir = System.getProperty(PropertyLoader.exportBaseDirProperty);
		} catch (Exception e){
			throw new RuntimeException("required System property \""+PropertyLoader.exportBaseDirProperty+"\" not defined");
		}

		
		// see if we've done this before, and try to get it
		// for now, works only for the life of the server (eventually will be persistent)
		Object completed = completedExportRequests.get(exportSpecs);
		if (completed != null) {
			try {
				JobRequest previousRequest = (JobRequest)completedExportRequests.get(exportSpecs);
				File file = new File(exportBaseDir + previousRequest.getJobID() + ".zip");
				if (file.exists()) {
					URL url = new URL(exportBaseURL + file.getName());
					log.print("ExportServiceImpl.makeRemoteFile(): Found previously made file: " + file.getName());
					fireExportCompleted(newExportJob.getJobID(), exportSpecs.getVCDataIdentifier(), fileFormat, url.toString());
					// now that we start logging exports, we should do the persistence thing soon...
					// so far retutn null, so we at least don't double log during the same server session
					return null;
				}
			} catch (Throwable exc) {
				completedExportRequests.remove(exportSpecs);
				log.print("ExportServiceImpl.makeRemoteFile(): WARNING: did not find previous export output; attempting to export again");
			}
		}

		// we need to make new output
		log.print("ExportServiceImpl.makeRemoteFile(): Starting new export job: " + newExportJob);
		FileDataContainerManager fileDataContainerManager = new FileDataContainerManager();
		try{
			ExportOutput[] exportOutputs = null;
			switch (exportSpecs.getFormat()) {
				case FORMAT_CSV:
					exportOutputs = asciiExporter.makeASCIIData(outputContext,newExportJob, user, dataServerImpl, exportSpecs,fileDataContainerManager);
					return makeRemoteFile(fileFormat, exportBaseDir, exportBaseURL, exportOutputs, exportSpecs, newExportJob,fileDataContainerManager);
				case FORMAT_QUICKTIME:
				case FORMAT_GIF:
				case FORMAT_JPEG:
				case FORMAT_ANIMATED_GIF:
					exportOutputs = imgExporter.makeMediaData(outputContext,newExportJob, user, dataServerImpl, exportSpecs,clientTaskStatusSupport,fileDataContainerManager);
					boolean bOverrideZip = exportOutputs.length == 1;
					if(bSaveAsZip && !bOverrideZip){
						return makeRemoteFile(fileFormat, exportBaseDir, exportBaseURL, exportOutputs, exportSpecs, newExportJob,fileDataContainerManager);
					}else{
						return makeRemoteFile_Unzipped(fileFormat, exportBaseDir, exportBaseURL, exportOutputs, exportSpecs, newExportJob,fileDataContainerManager);
					}
				case FORMAT_NRRD:
					NrrdInfo[] nrrdInfos = rrExporter.makeRasterData(outputContext,newExportJob, user, dataServerImpl, exportSpecs, fileDataContainerManager);
					return makeRemoteFile(fileFormat, exportBaseDir, exportBaseURL, nrrdInfos, exportSpecs, newExportJob, fileDataContainerManager);
				case FORMAT_UCD:
					exportOutputs = rrExporter.makeUCDData(outputContext,newExportJob, user, dataServerImpl, exportSpecs,fileDataContainerManager);
					return makeRemoteFile(fileFormat, exportBaseDir, exportBaseURL, exportOutputs, exportSpecs, newExportJob,fileDataContainerManager);
				case FORMAT_VTK_IMAGE:
					exportOutputs = rrExporter.makeVTKImageData(outputContext,newExportJob, user, dataServerImpl, exportSpecs,fileDataContainerManager);
					return makeRemoteFile(fileFormat, exportBaseDir, exportBaseURL, exportOutputs, exportSpecs, newExportJob,fileDataContainerManager);
				case FORMAT_VTK_UNSTRUCT:
					exportOutputs = rrExporter.makeVTKUnstructuredData0(outputContext,newExportJob, user, dataServerImpl, exportSpecs,fileDataContainerManager);
					return makeRemoteFile(fileFormat, exportBaseDir, exportBaseURL, exportOutputs, exportSpecs, newExportJob,fileDataContainerManager);
				default:
					throw new DataAccessException("Unknown export format requested");
			}
		}finally{
			fileDataContainerManager.closeAllAndDelete();
		}
	}catch(UserCancelException ex)
	{
		throw ex;
	}
	catch (Throwable exc) {
		log.exception(exc);
		fireExportFailed(newExportJob.getJobID(), exportSpecs.getVCDataIdentifier(), fileFormat, exc.getMessage());
		throw new DataAccessException(exc.getMessage());
	}
}


/**
 * Insert the method's description here.
 * Creation date: (4/26/2004 6:47:56 PM)
 */
private ExportEvent makeRemoteFile(String fileFormat, String exportBaseDir, String exportBaseURL, NrrdInfo[] nrrdInfos, ExportSpecs exportSpecs, JobRequest newExportJob, FileDataContainerManager fileDataContainerManager) throws DataFormatException, IOException, MalformedURLException {
			boolean exportValid = true;

	// check outputs and package into zip file
	File zipFile = new File(exportBaseDir + newExportJob.getJobID() + ".zip");
	BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(zipFile));
	ZipOutputStream zipOut = new ZipOutputStream(bout);
	try {
		for (int i=0;i<nrrdInfos.length;i++) {
			if (nrrdInfos[i].isHasData()) {
				// nrrdInfo 'header' file contains either just the header or both the header and data together
				ZipEntry zipEntry = new ZipEntry(nrrdInfos[i].getCanonicalFilename((nrrdInfos[i].isSeparateHeader()?true:false)));
				zipOut.putNextEntry(zipEntry);
				BufferedInputStream in = null;
				byte[] bytes = new byte[65536];
				try {
					in = new BufferedInputStream(new FileInputStream(fileDataContainerManager.getFile(nrrdInfos[i].getHeaderFileID())));
					int b = in.read(bytes);
					while (b != -1) {
						zipOut.write(bytes, 0, b);
						b = in.read(bytes);
					}
				} finally {
					if(in != null){try{in.close();}catch(Exception e){e.printStackTrace();}}
				}
				if (nrrdInfos[i].isSeparateHeader()) {
					// The data was not saved with the 'header' file so save it separately
					zipEntry = new ZipEntry(nrrdInfos[i].getCanonicalFilename(false));
					zipOut.putNextEntry(zipEntry);
					File datafile = fileDataContainerManager.getFile(nrrdInfos[i].getDataFileID());
					in = null;
					try {
						in = new BufferedInputStream(new FileInputStream(datafile));
						int b = in.read(bytes);
						while (b != -1) {
							zipOut.write(bytes, 0, b);
							b = in.read(bytes);
						}
					} finally {
						if(in != null){try{in.close();}catch(Exception e){e.printStackTrace();}}
					}
				}
			} else {
				exportValid = false;
				break;
			}
		}
	} catch (IOException ioexc) {
		throw ioexc;
	} finally {
		zipOut.close();
	}
	
	if (exportValid) {
		completedExportRequests.put(exportSpecs, newExportJob);
		log.print("ExportServiceImpl.makeRemoteFile(): Successfully exported to file: " + zipFile.getName());
		URL url = new URL(exportBaseURL + zipFile.getName());
		return fireExportCompleted(newExportJob.getJobID(), exportSpecs.getVCDataIdentifier(), fileFormat, url.toString());
	}
	else {
		throw new DataFormatException("Export Server could not produce valid data !");
	}
}


/**
 * This function saves the remote file into a compressed zip file.
 * Creation date: (4/26/2004 6:47:56 PM)
 */
private ExportEvent makeRemoteFile(String fileFormat, String exportBaseDir, String exportBaseURL, ExportOutput[] exportOutputs, ExportSpecs exportSpecs, JobRequest newExportJob,FileDataContainerManager fileDataContainerManager) throws DataFormatException, IOException, MalformedURLException {
			boolean exportValid = true;

			// check outputs and package into zip file
			File zipFile = new File(exportBaseDir + newExportJob.getJobID() + ".zip");
			FileOutputStream fileOut = new FileOutputStream(zipFile);
			ZipOutputStream zipOut = new ZipOutputStream(fileOut);
			for (int i=0;i<exportOutputs.length;i++) {
				if (exportOutputs[i].isValid()) {
					String filename = exportOutputs[i].getSimID() + exportOutputs[i].getDataID();
					if (!filename.endsWith(exportOutputs[i].getDataType())){
						filename = filename + exportOutputs[i].getDataType();
					}
					ZipEntry zipEntry = new ZipEntry(filename);
					zipOut.putNextEntry(zipEntry);
					exportOutputs[i].writeDataToOutputStream(zipOut,fileDataContainerManager);
					//zipOut.write(exportOutputs[i].getData());
				} else {
					exportValid = false;
					break;
				}
			}
			zipOut.close();
			
			if (exportValid) {
				completedExportRequests.put(exportSpecs, newExportJob);
				log.print("ExportServiceImpl.makeRemoteFile(): Successfully exported to file: " + zipFile.getName());
				URL url = new URL(exportBaseURL + zipFile.getName());
				return fireExportCompleted(newExportJob.getJobID(), exportSpecs.getVCDataIdentifier(), fileFormat, url.toString());
			}
			else {
				throw new DataFormatException("Export Server could not produce valid data !");
			}
}

/**
 * Save remote file in it original format without compression.
 */
private ExportEvent makeRemoteFile_Unzipped(String fileFormat, String exportBaseDir, String exportBaseURL, ExportOutput[] exportOutputs, ExportSpecs exportSpecs, JobRequest newExportJob,FileDataContainerManager fileDataContainerManager) throws DataFormatException, IOException, MalformedURLException
{
	boolean exportValid = true;
	String fileNames = "";
	if(exportOutputs.length > 0  && exportOutputs[0].isValid())
	{
		//do the first file of exportOutputs separately (for VFRAP, there is only one export output)
		String extStr = "." + fileFormat;
		File file = new File(exportBaseDir + newExportJob.getJobID() + extStr);
		FileOutputStream fileOut = new FileOutputStream(file);
		BufferedOutputStream out= new BufferedOutputStream(fileOut);
		exportOutputs[0].writeDataToOutputStream(out,fileDataContainerManager);
		//out.write(exportOutputs[0].getData());
		out.close();
		fileNames = fileNames + file.getName();
		//if there are more export outputs, loops through the second till the last.
		for (int i=1;i<exportOutputs.length;i++)
		{
			if (exportOutputs[i].isValid()) 
			{
				File moreFile = new File(exportBaseDir + newExportJob.getJobID()+"_"+ i + extStr);
				FileOutputStream moreFileOut = new FileOutputStream(moreFile);
				ObjectOutputStream moreOut= new ObjectOutputStream(moreFileOut);
				exportOutputs[i].writeDataToOutputStream(moreOut,fileDataContainerManager);
				//moreOut.writeObject(exportOutputs[i].getData());
				moreOut.close();
				fileNames = "\t"+fileNames + moreFile.getName();
			} else {
				exportValid = false;
				break;
			}
		}
	}
	else
	{
		exportValid = false;
	}
	if (exportValid) {
		completedExportRequests.put(exportSpecs, newExportJob);
		log.print("ExportServiceImpl.makeRemoteFile(): Successfully exported to file: " + fileNames);
		URL url = new URL(exportBaseURL + fileNames);
		return fireExportCompleted(newExportJob.getJobID(), exportSpecs.getVCDataIdentifier(), fileFormat, url.toString());
	}
	else {
		throw new DataFormatException("Export Server could not produce valid data !");
	}
}


/**
 * Insert the method's description here.
 * Creation date: (3/29/2001 5:18:16 PM)
 * @param listener ExportListener
 */
public synchronized void removeExportListener(ExportListener listener) {
	listenerList.remove(ExportListener.class, listener);
}
}
