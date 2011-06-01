/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.gui.port;

/*   CompartmentTable  --- by Oliver Ruebenacker, UCHC --- August 2008
 *   The table to edit compartments
 */

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.vcell.sybil.models.bpimport.table.CompartmentTableModel;

public class CompartmentTable extends JTable {

	private static final long serialVersionUID = -2520577139535635777L;

	public CompartmentTable(CompartmentTableModel model) {
		super(model);
		setCellEditors();
	}
	
	public void setModel(TableModel model) {
		super.setModel(model);
		if(model instanceof CompartmentTableModel) { setCellEditors(); }
	}
	
	public CompartmentTableModel model() { return (CompartmentTableModel) getModel(); }
	
	protected void setCellEditors() {
		TableColumn dimsCol = getColumnModel().getColumn(1);
		JComboBox dimsBox = new JComboBox();
		for(int ind = 0; ind < 4; ++ind) { dimsBox.addItem(new Integer(ind)); }
		dimsCol.setCellEditor(new DefaultCellEditor(dimsBox));
		TableColumn outLocCol = getColumnModel().getColumn(2);
		JComboBox outLocBox = new JComboBox();
		// TODO
		// outLocBox.addItem("inside"); 
		for(String locLabel : model().locDir().keySet()) { 
			if(!locLabel.equals("inside") && !locLabel.equals("outside"))
			outLocBox.addItem(locLabel); 
		}
		outLocBox.addItem("outside"); 
		outLocCol.setCellEditor(new DefaultCellEditor(outLocBox));
	}
	
}
