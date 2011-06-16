/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.desktop.simulation;
import java.awt.*;
import java.beans.*;

import org.vcell.util.document.VCDataIdentifier;

import cbit.vcell.client.data.*;
import cbit.vcell.solver.*;
import cbit.vcell.document.*;
import cbit.vcell.server.*;
public class SimulationCompareWindow {
	private org.vcell.util.document.VCDataIdentifier vcDataIdentifier = null;
	private javax.swing.JInternalFrame frame = null;

/**
 * Insert the method's description here.
 * Creation date: (7/20/2004 11:38:49 AM)
 * @param vcDataIdentifier cbit.vcell.server.VCDataIdentifier
 * @param simulation cbit.vcell.solver.Simulation
 * @param simOwner cbit.vcell.document.SimulationOwner
 * @param dataViewer cbit.vcell.client.data.DataViewer
 */
public SimulationCompareWindow(VCDataIdentifier vcDataIdentifier, DataViewer dataViewer) {
	setVcDataIdentifier(vcDataIdentifier);
	initialize(dataViewer);
}


/**
 * Insert the method's description here.
 * Creation date: (7/16/2004 5:16:51 PM)
 * @return javax.swing.JInternalFrame
 */
public javax.swing.JInternalFrame getFrame() {
	return frame;
}


/**
 * Insert the method's description here.
 * Creation date: (7/16/2004 5:16:51 PM)
 * @return cbit.vcell.server.VCDataIdentifier
 */
public org.vcell.util.document.VCDataIdentifier getVcDataIdentifier() {
	return vcDataIdentifier;
}


/**
 * Insert the method's description here.
 * Creation date: (7/20/2004 11:50:03 AM)
 * @param dataViewer cbit.vcell.client.data.DataViewer
 */
private void initialize(DataViewer dataViewer) {
	// create frame
	setFrame(new org.vcell.util.gui.JInternalFrameEnhanced("Comparing ... "+getVcDataIdentifier().getID(), true, true, true, true));
	getFrame().setContentPane(dataViewer);
	getFrame().setSize(450, 450);
	getFrame().setMinimumSize(new Dimension(400, 400));
}


/**
 * Insert the method's description here.
 * Creation date: (7/16/2004 5:16:51 PM)
 * @param newFrame javax.swing.JInternalFrame
 */
private void setFrame(javax.swing.JInternalFrame newFrame) {
	frame = newFrame;
}


/**
 * Insert the method's description here.
 * Creation date: (7/16/2004 5:16:51 PM)
 * @param newVcDataIdentifier cbit.vcell.server.VCDataIdentifier
 */
private void setVcDataIdentifier(org.vcell.util.document.VCDataIdentifier newVcDataIdentifier) {
	vcDataIdentifier = newVcDataIdentifier;
}
}
