/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.desktop;
import cbit.vcell.desktop.controls.*;
import java.util.*;
import cbit.vcell.mapping.*;
import cbit.vcell.solver.*;
import cbit.vcell.biomodel.*;
import cbit.vcell.clientdb.*;
import cbit.vcell.server.*;
/**
 * Insert the type's description here.
 * Creation date: (11/28/00 1:14:05 PM)
 * @author: Jim Schaff
 */
public class MathModelDbTreePanelTest extends cbit.vcell.client.test.ClientTester {
/**
 * Insert the method's description here.
 * Creation date: (11/28/00 1:14:34 PM)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	try {		
		javax.swing.JFrame frame = new javax.swing.JFrame();
		MathModelDbTreePanel aMathModelDbTreePanel;
		aMathModelDbTreePanel = new MathModelDbTreePanel();
		frame.setContentPane(aMathModelDbTreePanel);
		frame.setSize(aMathModelDbTreePanel.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});

		cbit.vcell.client.server.ClientServerManager managerManager = mainInit(args,"BioModelTreeDbPanelTest",frame);
		DocumentManager docManager = managerManager.getDocumentManager();
		
/*
		MathModel bioModel = MathModelTest.getExample();
		mathModel = docManager.save(mathModel);
		Simulation sim = new Simulation(bioModel.getMathDescription());
		sim.setName("sim1_"+Integer.toHexString((new Random()).nextInt()));
		mathModel.setSimulations(new Simulation[] { sim });
		mathModel = docManager.save(mathModel);
*/


		
		frame.show();
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);

		aMathModelDbTreePanel.setDocumentManager(docManager);
		
		
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of BioModelDbTreePanelTest");
		exception.printStackTrace(System.out);
	}
}
}
