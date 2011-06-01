/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.mapping.gui;

import cbit.vcell.mapping.*;
/**
 * This type was created in VisualAge.
 */
public class StructureMappingCartoonPanelTest {
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		java.awt.Frame frame = new java.awt.Frame();
		new org.vcell.util.gui.WindowCloser(frame,true);
		MathMapping mathMapping = MathMappingTest.getExample(2);
		//cbit.vcell.mapping.SimulationContext simContext = cbit.vcell.mapping.SimulationContextTest.getExample(2);
		//cbit.vcell.model.Model model = simContext.getReactionContext().getModel();
		//cbit.vcell.geometry.Geometry geo = simContext.getGeometryContext().getGeometry();
		//simContext = new SimulationContext(model,geo);
		//cbit.vcell.mapping.GeometryContext geometryContext = simContext.getGeometryContext();
		cbit.vcell.mapping.SimulationContext simContext = mathMapping.getSimulationContext();

		
		cbit.vcell.mapping.gui.StructureMappingCartoonPanel aStructureMappingCartoonPanel = new cbit.vcell.mapping.gui.StructureMappingCartoonPanel();
		frame.add("Center", aStructureMappingCartoonPanel);
		frame.setSize(aStructureMappingCartoonPanel.getSize());
		frame.setVisible(true);
		aStructureMappingCartoonPanel.setSimulationContext(simContext);
//		Thread.sleep(15000);
//		System.out.println("setting simContext to null");
//		aStructureMappingCartoonPanel.setSimulationContext(null);
	//	geometryContext.getGeometry().setDimension(0);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Panel");
		exception.printStackTrace(System.out);
	}
}
}
