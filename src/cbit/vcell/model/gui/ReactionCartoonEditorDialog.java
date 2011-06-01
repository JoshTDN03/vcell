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

import org.vcell.util.gui.JInternalFrameEnhanced;

import cbit.vcell.clientdb.DocumentManager;
import cbit.vcell.graph.ReactionCartoonEditorPanel;
import cbit.vcell.graph.StructureCartoonTool;
import cbit.vcell.model.Model;
import cbit.vcell.model.Structure;
/**
 * This type was created in VisualAge.
 */
public class ReactionCartoonEditorDialog extends JInternalFrameEnhanced {
	private javax.swing.JPanel ivjContentsPane = null;
	private ReactionCartoonEditorPanel ivjReactionCartoonEditorPanel = null;
	private StructureCartoonTool structureCartoonTool = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public ReactionCartoonEditorDialog(StructureCartoonTool sct) {
	super();
	structureCartoonTool = sct;
	initialize();
}
/**
 * Insert the method's description here.
 * Creation date: (6/14/2005 4:16:19 PM)
 */
public void cleanupOnClose() {

	getReactionCartoonEditorPanel().cleanupOnClose();
}
/**
 * Return the ContentsPane property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getContentsPane() {
	if (ivjContentsPane == null) {
		try {
			ivjContentsPane = new javax.swing.JPanel();
			ivjContentsPane.setName("ContentsPane");
			ivjContentsPane.setLayout(new java.awt.BorderLayout());
			getContentsPane().add(getReactionCartoonEditorPanel(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjContentsPane;
}
/**
 * Return the ReactionCartoonEditorPanel property value.
 * @return cbit.vcell.graph.ReactionCartoonEditorPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ReactionCartoonEditorPanel getReactionCartoonEditorPanel() {
	if (ivjReactionCartoonEditorPanel == null) {
		try {
			ivjReactionCartoonEditorPanel = new ReactionCartoonEditorPanel(structureCartoonTool);
			ivjReactionCartoonEditorPanel.setName("ReactionCartoonEditorPanel");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjReactionCartoonEditorPanel;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}
/**
 * This method was created in VisualAge.
 * @param model cbit.vcell.model.Model
 * @param structure cbit.vcell.model.Structure
 */
public void init(Model model, Structure structure, DocumentManager documentManager) {
	getReactionCartoonEditorPanel().setModel(model);
	getReactionCartoonEditorPanel().setStructure(structure);
	getReactionCartoonEditorPanel().setDocumentManager(documentManager);
	setTitle("Reactions for "+structure.getName());
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("ReactionCartoonEditorDialog");
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setClosable(true);
		setSize(600, 495);
		setResizable(true);
		setContentPane(getContentsPane());
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		ReactionCartoonEditorDialog aReactionCartoonEditorDialog;
		aReactionCartoonEditorDialog = new ReactionCartoonEditorDialog(null);
		frame.setContentPane(aReactionCartoonEditorDialog);
		frame.setSize(aReactionCartoonEditorDialog.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JInternalFrame");
		exception.printStackTrace(System.out);
	}
}

}
