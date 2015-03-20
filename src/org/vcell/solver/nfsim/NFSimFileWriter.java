/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.solver.nfsim;

import java.io.PrintWriter;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.jdom.Element;

import cbit.util.xml.XmlUtil;
import cbit.vcell.messaging.server.SimulationTask;
import cbit.vcell.solver.NFsimSimulationOptions;
import cbit.vcell.solver.server.SolverFileWriter;


/**
 * The function reads model information from simulation and
 * generates the stochastic input file for simulation engine.
 * Creation date: (6/22/2006 4:22:59 PM)
 * @author: Tracy LI
 */
public class NFSimFileWriter extends SolverFileWriter 
{
	private long randomSeed = 0; //value assigned in the constructor
	private RandomDataGenerator dist = new RandomDataGenerator();
	
	
public NFSimFileWriter(PrintWriter pw, SimulationTask simTask, boolean bMessaging) 
{
	super(pw, simTask, bMessaging);
	
	//get user defined random seed. If it doesn't exist, we assign system time (in millisecond) to it.
	NFsimSimulationOptions nfsimOptions = simTask.getSimulation().getSolverTaskDescription().getNFSimSimulationOptions();
	if (nfsimOptions.getRandomSeed() != null) {
		this.randomSeed = nfsimOptions.getRandomSeed();
	} else {
		this.randomSeed = System.currentTimeMillis();
	}
	//We add jobindex to the random seed in case there is a parameter scan.
	randomSeed = randomSeed + simTask.getSimulationJob().getJobIndex();
	dist.reSeed(randomSeed);
}

@Override
public void write(String[] parameterNames) throws Exception {	
	WriterOutputStream wos = new WriterOutputStream(printWriter);
	NFsimSimulationOptions nfsimSimulationOptions = simTask.getSimulation().getSolverTaskDescription().getNFSimSimulationOptions();
	Element root = NFsimXMLWriter.writeNFsimXML(simTask, randomSeed, nfsimSimulationOptions);
	if (bUseMessaging) {
		Element jms = super.xmlJMSParameters(); 
		root.addContent(jms);
	}
	XmlUtil.writeXmlToStream(root, false, wos);
}


}
