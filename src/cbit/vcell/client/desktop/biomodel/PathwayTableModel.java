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

/*   EntitySelectionTableModel  --- by Oliver Ruebenacker, UCHC --- November (?) 2008 to December 2009
 *   Model for table to select entities from an SBBox
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.vcell.pathway.BioPaxObject;
import org.vcell.pathway.Control;
import org.vcell.pathway.Conversion;
import org.vcell.pathway.Interaction;
import org.vcell.pathway.PathwayEvent;
import org.vcell.pathway.PathwayListener;
import org.vcell.pathway.PathwayModel;
import org.vcell.pathway.PhysicalEntity;
import org.vcell.util.gui.ScrollTable;

@SuppressWarnings("serial")
public class PathwayTableModel extends VCellSortTableModel<PhysiologyRelationshipTableRow> implements PathwayListener {


	public static final int colCount = 3;
	public static final int iColSelected = 0;
	public static final int iColEntity = 1;
	public static final int iColType = 2;
	
	// filtering variables 
	protected String searchText = null;
	//done

	private PathwayModel pathwayModel;

	public PathwayTableModel(ScrollTable table) {
		super(table, new String[] {"Select", "Entity Name", "Type"});
	}
	
	public void setPathwayModel(PathwayModel newValue){
		if (this.pathwayModel == newValue){
			return;
		}
		PathwayModel oldValue = pathwayModel;
		if (oldValue != null) {
			oldValue.removePathwayListener(this);
		}
		if (newValue != null) {
			newValue.addPathwayListener(this);
		}
		this.pathwayModel = newValue;
		refreshData();
	}
	
	public Class<?> getColumnClass(int iCol) {
		if(iCol == iColSelected) { return Boolean.class; }
		else { return String.class; }
	}
	
	public Object getValueAt(int iRow, int iCol) {
		PhysiologyRelationshipTableRow entitySelectionTableRow = getValueAt(iRow);
		BioPaxObject bpObject = entitySelectionTableRow.getBioPaxObject();
		switch(iCol) {		
			case iColSelected:{
				return entitySelectionTableRow.selected();
			}
			case iColEntity:{
				return getLabel(bpObject);
			}
			case iColType:{
				return getType(bpObject);
			}
			default:{
				return null;
			}
		}
	}
	
	public boolean isCellEditable(int iRow, int iCol) {
		return iCol == iColSelected;
	}
	
	public void setValueAt(Object valueNew, int iRow, int iCol) {
		if(valueNew instanceof Boolean && iCol == iColSelected) {
			PhysiologyRelationshipTableRow entitySelectionTableRow = getValueAt(iRow);
			entitySelectionTableRow.setSelected((Boolean) valueNew);
		}
	}
	
	// generate the sortable table. Set up the functions for each column
	public Comparator<PhysiologyRelationshipTableRow> getComparator(final int col, final boolean ascending) {
		return new Comparator<PhysiologyRelationshipTableRow>() {
		    public int compare(PhysiologyRelationshipTableRow o1, PhysiologyRelationshipTableRow o2){
		    	if (col == iColSelected) {
		    		int c  = o1.selected().compareTo(o2.selected());
		    		return ascending ? c : -c;
		    	} else 

		    	if (col == iColEntity) {// only sortable on entity column
		    		int c  = getLabel(o1.getBioPaxObject()).compareToIgnoreCase(getLabel(o2.getBioPaxObject()));
		    		return ascending ? c : -c;
		    	} else 
		    		
		    	if (col == iColType) {
		    		int c  = getType(o1.getBioPaxObject()).compareToIgnoreCase(getType(o2.getBioPaxObject()));
		    		return ascending ? c : -c;
		    	}

		    	return 0;
		    }
		};
	}
	
	private String getType(BioPaxObject bpObject){
		return bpObject.getTypeLabel();
	}
	
	private String getLabel(BioPaxObject bpObject){
		if (bpObject instanceof Conversion){
			Conversion conversion =(Conversion)bpObject;
			if (conversion.getName().size()>0){
				return conversion.getName().get(0);
			}else{
				return conversion.getIDShort();
			}
		}else if (bpObject instanceof PhysicalEntity){
			PhysicalEntity physicalEntity =(PhysicalEntity)bpObject;
			if (physicalEntity.getName().size()>0){
				return physicalEntity.getName().get(0);
			}else{
				return physicalEntity.getIDShort();
			}
		}else if (bpObject instanceof Interaction){
			Interaction interaction =(Interaction)bpObject;
			if (interaction.getName().size()>0){
				return interaction.getName().get(0);
			}else{
				return interaction.getIDShort();
			}
		}else{
			return bpObject.getIDShort();
		}
	}
	
	// filtering functions
	public void setSearchText(String newValue) {
		if (searchText == newValue) {
			return;
		}
		searchText = newValue;
		refreshData();
	}

	private void refreshData() {
		if (pathwayModel == null) {
			setData(null);
			return;
		}
		
		List<PhysiologyRelationshipTableRow> allPathwayObjectList = new ArrayList<PhysiologyRelationshipTableRow>();
		for (BioPaxObject bpObject1 : pathwayModel.getBiopaxObjects()){
			if (bpObject1 instanceof PhysicalEntity || (bpObject1 instanceof Interaction && !(bpObject1 instanceof Control))){
				allPathwayObjectList.add(new PhysiologyRelationshipTableRow(bpObject1));
			}
		}
		ArrayList<PhysiologyRelationshipTableRow> pathwayObjectList = new ArrayList<PhysiologyRelationshipTableRow>();
		for (PhysiologyRelationshipTableRow rs : allPathwayObjectList){
			BioPaxObject bpObject = rs.getBioPaxObject();
			if (searchText == null || searchText.length() == 0 
					|| getLabel(bpObject).toLowerCase().contains(searchText.toLowerCase())
					|| getType(bpObject).toLowerCase().contains(searchText.toLowerCase()) ) {
				pathwayObjectList.add(rs);
			}
		}
		setData(pathwayObjectList);
	}

	public void pathwayChanged(PathwayEvent event) {
		refreshData();		
	}
}
