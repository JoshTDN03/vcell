/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.server.bionetgen;
import cbit.vcell.resource.ResourceUtil;
import cbit.vcell.server.bionetgen.BNGOutput;
import cbit.vcell.server.bionetgen.BNGInput;
import java.io.*;

import org.vcell.util.FileUtils;

/**
 * Insert the type's description here.
 * Creation date: (9/13/2006 9:26:26 AM)
 * @author: Fei Gao
 */
public class BNGUtils {
	private final static String EXE_SUFFIX = ResourceUtil.bWindows ? ".exe" : (ResourceUtil.bMacPpc ? "_ppc" : "");
	private final static String EXE_BNG = "BNG2" + EXE_SUFFIX;
	private final static String RES_EXE_BNG = ResourceUtil.RES_PACKAGE + "/" + EXE_BNG;
	private final static String DLL_CYGWIN = "cygwin1.dll";
	private final static String RES_DLL_CYGWIN = ResourceUtil.RES_PACKAGE + "/" + DLL_CYGWIN;
	// run_network is called from within BNG2.exe, so the extension is not controlled from here
	private final static String EXE_RUN_NETWORK = "run_network" + (ResourceUtil.bWindows ? ".exe" : "");
	private final static String RES_EXE_RUN_NETWORK = ResourceUtil.RES_PACKAGE + "/" + EXE_RUN_NETWORK;

	private final static String suffix_input = ".bngl";
	private final static String prefix = "vcell_bng_";

	private static File workingDir = null;
	private static org.vcell.util.Executable executable = null;
	private static boolean bFirstTime = true;

	private static File file_exe_bng = null;
	private static File file_dll_cygwin = null;
	private static File file_exe_run_network = null;

/**
 * BNGUtils constructor comment.
 */
public BNGUtils() {
	super();
}


///**
// * Insert the method's description here.
// * Creation date: (7/11/2006 3:46:33 PM)
// * @return java.io.File
// * @param parentDir java.io.File
// */
//private static File createTempDirectory(String prefix, File parentDir) {
//	while (true) {
//		int  counter = new java.util.Random().nextInt() & 0xffff;
//		
//		File tempDir = new File(parentDir, prefix + Integer.toString(counter));
//		if (!tempDir.exists()) {
//			tempDir.mkdir();
//			return tempDir;
//		}
//	}
//}

/**
 * Insert the method's description here.
 * Creation date: (6/23/2005 3:57:30 PM)
 */
public static BNGOutput executeBNG(BNGInput bngRules) throws Exception {
	if (executable != null) {
		throw new RuntimeException("You can only run BNG one at a time!");
	}

	BNGOutput bngOutput = null;
	try {
		// prepare executables and working directory;
		
		initialize();
		
		File bngInputFile = null;
		FileOutputStream fos = null;


		String tempFilePrefix = workingDir.getName();
		try {
			bngInputFile = new File(workingDir, tempFilePrefix + suffix_input);
			fos = new java.io.FileOutputStream(bngInputFile);
		}catch (java.io.IOException e){
			e.printStackTrace(System.out);
			throw new RuntimeException("error opening input file '"+bngInputFile.getName()+": "+e.getMessage());
		}	
			
		PrintWriter inputFile = new PrintWriter(fos);
		inputFile.print(bngRules.getInputString());
		inputFile.close();
		
		System.out.println("-------------Starting BNG ...-------------------------------");
		// run BNG
		String[] cmd = new String[] {file_exe_bng.getAbsolutePath(), bngInputFile.getAbsolutePath()};
		executable = new org.vcell.util.Executable(cmd);
		executable.setWorkingDir(workingDir);
		executable.start();
		
		String stdoutString = executable.getStdoutString();
		if (executable.getStatus() == org.vcell.util.ExecutableStatus.STOPPED && stdoutString.length() == 0) {
			stdoutString = "Stopped by user. No output from BioNetGen.";	
		}
		
		File[] files = workingDir.listFiles();
		String[] filenames = new String[files.length];
		String[] filecontents = new String[files.length];
		
		for (int i = 0; i < files.length; i ++) {
			filenames[i] = files[i].getName();
			filecontents[i] = FileUtils.readFileToString(files[i]);
//			files[i].delete();
		}		
//		workingDir.delete();
		
		bngOutput = new BNGOutput(stdoutString, filenames, filecontents);
		System.out.println("--------------Finished BNG----------------------------");
		
	} catch(org.vcell.util.ExecutableException ex ){
		if (executable.getStderrString().trim().length() == 0) {
			throw ex;
		}
		throw new org.vcell.util.ExecutableException(executable.getStderrString());
	} finally {
		if (workingDir != null && workingDir.exists()) {
			File[] files = workingDir.listFiles();
			
			for (int i = 0; i < files.length; i ++) {
				files[i].delete();
			}		
//			workingDir.delete();
		}
		
		workingDir = null;
		executable = null;		
	}

	return bngOutput;
}


/**
 * Insert the method's description here.
 * Creation date: (9/19/2006 11:36:49 AM)
 */
private static void initialize() throws Exception {
	File bngHome = new File(ResourceUtil.getVcellHome(), "BioNetGen");
	if (!bngHome.exists()) {
		bngHome.mkdirs();
	}
	
	workingDir = bngHome;//createTempDirectory(prefix, bngHome);	
	System.out.println("BNG working directory is " + bngHome.getAbsolutePath());
	
	file_exe_bng = new java.io.File(bngHome, EXE_BNG);
	file_dll_cygwin = new java.io.File(bngHome, DLL_CYGWIN);
	file_exe_run_network = new java.io.File(bngHome, EXE_RUN_NETWORK);
	if (bFirstTime) {
		ResourceUtil.writeFileFromResource(RES_EXE_BNG, file_exe_bng);
		if (ResourceUtil.bWindows) {
			ResourceUtil.writeFileFromResource(RES_DLL_CYGWIN, file_dll_cygwin);
		}
		ResourceUtil.writeFileFromResource(RES_EXE_RUN_NETWORK, file_exe_run_network);
		bFirstTime = false;		
	} else {			
		if (!file_exe_bng.exists()) {
			ResourceUtil.writeFileFromResource(RES_EXE_BNG, file_exe_bng);
		}
		if (ResourceUtil.bWindows && !file_dll_cygwin.exists()) {
			ResourceUtil.writeFileFromResource(RES_DLL_CYGWIN, file_dll_cygwin);
		}
		if (!file_exe_run_network.exists()) {	
			ResourceUtil.writeFileFromResource(RES_EXE_RUN_NETWORK, file_exe_run_network);
		}
	}	
}

/**
 * Insert the method's description here.
 * Creation date: (6/23/2005 3:57:30 PM)
 */
public static void stopBNG() throws Exception {
	if (executable != null) {
		executable.stop();
	}
}
}
