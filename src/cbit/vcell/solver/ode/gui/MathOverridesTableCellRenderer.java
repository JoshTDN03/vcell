/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.solver.ode.gui;

import javax.swing.table.*;
import java.awt.*;
import javax.swing.*;
/**
 * Insert the type's description here.
 * Creation date: (8/7/2001 1:10:01 PM)
 * @author: Ion Moraru
 */
public class MathOverridesTableCellRenderer extends DefaultTableCellRenderer {
	private MathOverridesTableModel fieldMathOverridesTableModel = null;
/**
 * MathOverridesTableCellRenderer constructor comment.
 */
public MathOverridesTableCellRenderer() {
	super();
}
/**
 * Gets the mathOverridesTableModel property (cbit.vcell.solver.ode.gui.MathOverridesTableModel) value.
 * @return The mathOverridesTableModel property value.
 * @see #setMathOverridesTableModel
 */
public MathOverridesTableModel getMathOverridesTableModel() {
	return fieldMathOverridesTableModel;
}
/**
 * Insert the method's description here.
 * Creation date: (8/7/2001 1:11:37 PM)
 * @return java.awt.Component
 * @param table javax.swing.JTable
 * @param value java.lang.Object
 * @param isSelected boolean
 * @param hasFocus boolean
 * @param row int
 * @param column int
 */
public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	if (!getMathOverridesTableModel().isDefaultValue(row) && column != getMathOverridesTableModel().COLUMN_DEFAULT) {
		setForeground(Color.red);
	} else {
		if (getBackground().equals(table.getSelectionBackground())){
			setForeground(table.getSelectionForeground());
		}else{
			setForeground(table.getForeground());
		}
		if (column == getMathOverridesTableModel().COLUMN_ACTUAL) {
			setText("");
		}
	}
	return this;
}
/**
 * Sets the mathOverridesTableModel property (cbit.vcell.solver.ode.gui.MathOverridesTableModel) value.
 * @param mathOverridesTableModel The new value for the property.
 * @see #getMathOverridesTableModel
 */
public void setMathOverridesTableModel(MathOverridesTableModel mathOverridesTableModel) {
	MathOverridesTableModel oldValue = fieldMathOverridesTableModel;
	fieldMathOverridesTableModel = mathOverridesTableModel;
	firePropertyChange("mathOverridesTableModel", oldValue, mathOverridesTableModel);
}
}
