/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.desktop.biomodel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.ScrollTable;

import cbit.gui.AutoCompleteSymbolFilter;
import cbit.gui.ScopedExpression;
import cbit.vcell.mapping.BioEvent;
import cbit.vcell.mapping.BioEvent.Delay;
import cbit.vcell.mapping.BioEvent.EventAssignment;
import cbit.vcell.model.ReservedBioSymbolEntries;
import cbit.vcell.parser.SymbolTable;

@SuppressWarnings("serial")
public class EventsSummaryTableModel extends BioModelEditorApplicationRightSideTableModel<BioEvent> implements PropertyChangeListener{

	public final static int COLUMN_EVENT_NAME = 0;
	public final static int COLUMN_EVENT_TRIGGER_EXPR = 1;
	public final static int COLUMN_EVENT_DELAY_EXPR = 2;
	public final static int COLUMN_EVENT_ASSIGN_VARS_LIST = 3;
	
	private static String[] columnNames = new String[] {"Name", "Trigger", "Delay", "Event Assignment Vars"};

	public EventsSummaryTableModel(ScrollTable table) {
		super(table, columnNames);
	}
	
	public Class<?> getColumnClass(int column) {
		switch (column){
			case COLUMN_EVENT_NAME:{
				return String.class;
			}
			case COLUMN_EVENT_TRIGGER_EXPR:{
				return ScopedExpression.class;
			}
			case COLUMN_EVENT_DELAY_EXPR:{
				return ScopedExpression.class;
			}
			case COLUMN_EVENT_ASSIGN_VARS_LIST:{
				return String.class;
			}
			default:{
				return Object.class;
			}
		}
	}

	protected List<BioEvent> computeData() {
		if (simulationContext == null || simulationContext.getBioEvents() == null){
			return null;
		}
		List<BioEvent> bioEventList = new ArrayList<BioEvent>();
		for (BioEvent bioEvent : simulationContext.getBioEvents()) {
			if (searchText == null || searchText.length() == 0) {
				bioEventList.add(bioEvent);
			} else {
				String lowerCaseSearchText = searchText.toLowerCase();	
				if (bioEvent.getName().toLowerCase().contains(lowerCaseSearchText)
					|| bioEvent.getTriggerExpression() != null && bioEvent.getTriggerExpression().infix().toLowerCase().contains(lowerCaseSearchText)
					|| bioEvent.getDelay() != null && bioEvent.getDelay().getDurationExpression().infix().toLowerCase().contains(lowerCaseSearchText)) {					
					bioEventList.add(bioEvent);
				}
			}
		}
		return bioEventList;
	}

	public Object getValueAt(int row, int column) {
		try{
			BioEvent event = getValueAt(row);
			if (event != null) {
				switch (column) {
					case COLUMN_EVENT_NAME: {
						return event.getName();
					} 
					case COLUMN_EVENT_TRIGGER_EXPR: {
						if (event.getTriggerExpression() == null) {
							return null; 
						} else {
							return new ScopedExpression(event.getTriggerExpression(), event.getNameScope());
						}
					}
					case COLUMN_EVENT_DELAY_EXPR: {
						Delay delay = event.getDelay();
						if (delay == null) {
							return "None"; 
						} else {
							return new ScopedExpression(delay.getDurationExpression(), event.getNameScope());
						}
					}
					case COLUMN_EVENT_ASSIGN_VARS_LIST: {
						ArrayList<EventAssignment> eas = event.getEventAssignments();
						if (eas.size() == 0) {
							return "None";
						} 
						String varNames = "";
						for (EventAssignment ea : eas) {
							varNames = varNames.concat(ea.getTarget().getName() + ", ");
						}
						varNames = varNames.substring(0, varNames.lastIndexOf(","));
						return varNames;
					} 
				}
			} else {
				if (column == COLUMN_EVENT_NAME) {
					return BioModelEditorRightSideTableModel.ADD_NEW_HERE_TEXT;
				}
			}
		} catch(Exception e){
			e.printStackTrace(System.out);
		}
		return null;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == COLUMN_EVENT_NAME;
	}

	@Override
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		if (evt.getPropertyName().equals("trigger") || evt.getPropertyName().equals("delay") || evt.getPropertyName().equals("eventAssignments")) {
			fireTableRowsUpdated(0, getRowCount()-1);
		} else {
			if (evt.getPropertyName().equals("bioevents")) {
				BioEvent[] oldValue = (BioEvent[])evt.getOldValue();
				if (oldValue != null) {			
					for (BioEvent be : oldValue) {
						be.removePropertyChangeListener(this);						
					}
				}
				BioEvent[] newValue = (BioEvent[])evt.getNewValue();
				if (newValue != null) {			
					for (BioEvent be : newValue) {
						be.addPropertyChangeListener(this);						
					}
				}
			}
			refreshData();
		}
	}
	
	public void setValueAt(Object value, int row, int column) {
		try{
			if (value == null || value.toString().length() == 0 || BioModelEditorRightSideTableModel.ADD_NEW_HERE_TEXT.equals(value)) {
				return;
			}
			BioEvent bioEvent = getValueAt(row);
			if (bioEvent == null) {
				bioEvent = simulationContext.createBioEvent();
			} else {
				bioEvent = getValueAt(row);
			}
			switch (column) {
				case COLUMN_EVENT_NAME: {
					bioEvent.setName((String)value);
				} 
			}
		} catch(Exception e){
			e.printStackTrace(System.out);
			DialogUtils.showErrorDialog(ownerTable, e.getMessage());
		}
	}

	public Comparator<BioEvent> getComparator(int col, boolean ascending) {
		return null;
	}

	@Override
	public boolean isSortable(int col) {
		return false;
	}

	public String checkInputValue(String inputValue, int row, int column) {
		BioEvent bioEvent = getValueAt(row);
		switch (column) {
		case COLUMN_EVENT_NAME: {
			if (bioEvent == null || !bioEvent.getName().equals(inputValue)) {
				if (simulationContext.getBioEvent(inputValue) != null) {
					return "An event with name '" + inputValue + "' already exists!";
				}
				if (ReservedBioSymbolEntries.getEntry(inputValue) != null) {
					return "Cannot use reserved symbol '" + inputValue + "' as an event name";
				}
			}
		}
		}
		return null;
	}

	public SymbolTable getSymbolTable(int row, int column) {
		return null;
	}

	public AutoCompleteSymbolFilter getAutoCompleteSymbolFilter(int row, int column) {
		return null;
	}

	public Set<String> getAutoCompletionWords(int row, int column) {
		return null;
	}
	
	@Override
	public int getRowCount() {
		return getRowCountWithAddNew();
	}
}
