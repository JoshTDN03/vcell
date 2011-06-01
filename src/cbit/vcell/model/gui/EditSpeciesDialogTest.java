/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.model.gui;
/**
 * This type was created in VisualAge.
 */
public class EditSpeciesDialogTest extends cbit.vcell.client.test.ClientTester {
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	//
	if(args.length < 4 || (!args[args.length-1].equalsIgnoreCase("edit") && !args[args.length-1].equalsIgnoreCase("add"))){
		System.out.println("Must specify 'edit' || 'add' as last argument");
	}
	try {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		EditSpeciesDialog aEditSpeciesDialog;
		aEditSpeciesDialog = new EditSpeciesDialog(frame);
		frame.setContentPane(aEditSpeciesDialog);
		frame.setSize(aEditSpeciesDialog.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});

		String mode = args[args.length-1];
		String[] args2 = new String[args.length-1];
		System.arraycopy(args,0,args2,0,args.length-1);
		cbit.vcell.model.Model model = cbit.vcell.model.ModelTest.getExample_Bound();
		if(mode.equalsIgnoreCase("edit")){
			aEditSpeciesDialog.initEditSpecies(model.getSpeciesContexts(model.getStructures(0))[0], model);
		}else if(mode.equalsIgnoreCase("add")){
			aEditSpeciesDialog.initAddSpecies(model,model.getStructures(0));
		}else{
			throw new IllegalArgumentException("unknown test type="+args[args.length-1]);
		}

		frame.show();
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of cbit.vcell.model.CreateSpeciesDialog");
		exception.printStackTrace(System.out);
	}
}
}
