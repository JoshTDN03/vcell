/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.document;
import java.beans.PropertyVetoException;

import cbit.vcell.math.MathDescription;
import cbit.vcell.math.OutputFunctionContext;
import cbit.vcell.solver.Simulation;
/**
 * Insert the type's description here.
 * Creation date: (6/4/2004 1:56:12 AM)
 * @author: Ion Moraru
 */
public interface SimulationOwner extends GeometryOwner {
	Simulation addNewSimulation() throws PropertyVetoException;
	Simulation copySimulation(Simulation simulation) throws PropertyVetoException;
	Simulation[] getSimulations();
	void removeSimulation(Simulation simulation) throws PropertyVetoException;
	OutputFunctionContext getOutputFunctionContext();
	MathDescription getMathDescription();
	void refreshMathDescription();
	String getName();
}

