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
import cbit.vcell.mathmodel.*;
import java.util.*;

import org.vcell.util.document.MathModelInfo;

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
public class MathModelMetaDataTreePanelTest extends cbit.vcell.client.test.ClientTester {
/**
 * Insert the method's description here.
 * Creation date: (11/28/00 1:14:34 PM)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	try {		
		javax.swing.JFrame frame = new javax.swing.JFrame();
		MathModelMetaDataPanel aMathModelMetaDataPanel;
		aMathModelMetaDataPanel = new MathModelMetaDataPanel();
		frame.setContentPane(aMathModelMetaDataPanel);
		frame.setSize(aMathModelMetaDataPanel.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});

		cbit.vcell.client.server.ClientServerManager managerManager = mainInit(args,"MathModelTreeDbPanelTest",frame);
		DocumentManager docManager = managerManager.getDocumentManager();
		
		frame.show();
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);

		MathModelInfo mathModelInfos[] = docManager.getMathModelInfos();
		for (int i=0;i<mathModelInfos.length;i++){
			System.out.println("displaying mathModelInfo = "+mathModelInfos[i].toString());
			aMathModelMetaDataPanel.setMathModelInfo(mathModelInfos[i]);
			break;
		}
		
		
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of MathModelMetaDataPanelTest");
		exception.printStackTrace(System.out);
	}
}
}
