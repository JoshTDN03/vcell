/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.util.gui;

/*   SybilButton  --- by Oliver Ruebenacker, UCHC --- September 2008
 *   A JButton adapted for a unified look
 */

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.vcell.sybil.util.gui.ButtonFormatter;

public class ToolBar extends JToolBar {

	private static final long serialVersionUID = 7877434777811064324L;

	public static class Button extends JButton {

		private static final long serialVersionUID = 7037568719325601524L;

		public Button(Action action) {
			super(action);
			ButtonFormatter.format(this);
			setMaximumSize(new Dimension(2000, 28));
			setPreferredSize(new Dimension(180, 28));
			setFont(new Font("Arial", 1, 10));
			setMinimumSize(new Dimension(120, 28));
			setMargin(new Insets(2, 2, 2, 2));
			setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
		}
		
	}

	public ToolBar() {
		setFloatable(true);
		setBorder(new javax.swing.border.EtchedBorder());
		setOrientation(javax.swing.SwingConstants.HORIZONTAL);
	}
	
	public Button add(Action action) { return (Button) add(new Button(action)); }
	
}

