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

import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;

import org.vcell.util.gui.VCellIcons;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.client.desktop.biomodel.DocumentEditorTreeModel.DocumentEditorTreeFolderClass;
import cbit.vcell.client.desktop.biomodel.DocumentEditorTreeModel.DocumentEditorTreeFolderNode;
import cbit.vcell.desktop.BioModelNode;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.xml.gui.MiriamTreeModel.LinkNode;
 
@SuppressWarnings("serial")
public class BioModelEditorTreeCellRenderer extends DocumentEditorTreeCellRenderer  {
	private BioModel bioModel = null;
	
	public BioModelEditorTreeCellRenderer() {
		super();
	}
	
	public void setBioModel(BioModel newValue) {
		if (newValue == bioModel) {
			return;
		}
		bioModel = newValue;
	}

	public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (regularFont == null) {
			regularFont = getFont();
			boldFont = regularFont.deriveFont(Font.BOLD);
		}
		Font font = regularFont;
		Icon icon = null;
    	String labelText = null;
    	String toolTipPrefix = "";
    	String toolTipSuffix = "";
		if (value instanceof LinkNode){
			LinkNode ln = (LinkNode)value;
			String link = ln.getLink();
			String text = ln.getText();
			String qualifier = ln.getMiriamQualifier().getDescription();
			if (link != null) {
				String colorString = (sel)?"white":"blue";
				toolTipPrefix = "double-click to open link " + link;
				labelText = "<html>"+qualifier+"&nbsp;<font color=\""+colorString+"\"><a href=" + link + ">" + text + "</a></font></html>";
			}else{
				String colorString = (sel)?"white":"black";
				labelText = "<html>"+qualifier+"&nbsp;<font color=\""+colorString+"\">" + text + "</font></html>";
			}
		} else if (value instanceof BioModelNode) {
	        BioModelNode node = (BioModelNode)value;
	        Object userObj = node.getUserObject();
	    	if (userObj instanceof BioModel) {
	    		font = boldFont;
	    		icon = VCellIcons.documentIcon;
	    		labelText = ((BioModel)userObj).getName();
	    		toolTipPrefix = "BioModel: ";
	    	} else if (userObj instanceof SimulationContext) {		// --- root: application name
	    		font = boldFont;
	    		icon = VCellIcons.applicationIcon;
	    		labelText = "Application: " + ((SimulationContext)userObj).getName();
	    	} else if (userObj instanceof DocumentEditorTreeFolderNode) {		// --- 1st level folders
	    		DocumentEditorTreeFolderNode folder = (DocumentEditorTreeFolderNode)userObj;
	    		labelText = folder.getName();
	    		if (folder.isBold()) {
	    			font = boldFont;
	    		}
	    		DocumentEditorTreeFolderClass folderClass = folder.getFolderClass();
	    		switch(folderClass) {
//	    		case PATHWAY_NODE:
//	    			if (bioModel == null) {
//	    				labelText = folder.getName() + "(00000)";
//	    			} else {
//	    				labelText = folder.getName() + " (" + bioModel.getPathwayModel().getBiopaxObjects().size() + ")";
//	    			}
//	    			break;
	    		case REACTIONS_NODE:
	    			icon = VCellIcons.tableIcon;
	    			if (bioModel == null) {
	    				labelText = folder.getName() + "(00000)";
	    			} else {
	    				labelText = folder.getName() + " (" + bioModel.getModel().getNumReactions() + ")";
	    			}
	    			break;
	    		case STRUCTURES_NODE:
	    			icon = VCellIcons.tableIcon;
	    			if (bioModel == null) {
	    				labelText = folder.getName() + "(00000)";
	    			} else {
	    				labelText = folder.getName() + " (" + bioModel.getModel().getNumStructures() + ")";
	    			}
	    			break;
	    		case SPECIES_NODE:
	    			icon = VCellIcons.tableIcon;
	    			if (bioModel == null) {
	    				labelText = folder.getName() + "(00000)";
	    			} else {
	    				labelText = folder.getName() + " (" + bioModel.getModel().getNumSpeciesContexts() + ")";
	    			}
	    			break;
	    		case APPLICATIONS_NODE:
	    			if (bioModel == null) {
	    				labelText = folder.getName() + "(00000)";
	    			} else {
	    				labelText = folder.getName() + " (" + bioModel.getNumSimulationContexts() + ")";
	    			}
	    			break;
	    		case REACTION_DIAGRAM_NODE:
	    			icon = VCellIcons.diagramIcon;
	    			break;
	    		case STRUCTURE_DIAGRAM_NODE:
	    			icon = VCellIcons.structureIcon;
	    			break;
	    		case GEOMETRY_NODE:
	    			icon = VCellIcons.geometryIcon;
	    			break;
	    		case SPECIFICATIONS_NODE:
	    			icon = VCellIcons.settingsIcon;
	    			break;
	    		case PROTOCOLS_NODE:
	    			icon = VCellIcons.protocolsIcon;
	    			break;
	    		case SIMULATIONS_NODE:
	    			icon = VCellIcons.simulationIcon;
	    			break;
	    		case PARAMETER_ESTIMATION_NODE:
	    			icon = VCellIcons.fittingIcon;
	    			break;
	    		case PATHWAY_DIAGRAM_NODE:
	    			icon = VCellIcons.diagramIcon;
	    			break;
	    		case PATHWAY_OBJECTS_NODE:
	    			icon = VCellIcons.tableIcon;
	    			if (bioModel == null) {
	    				labelText = folder.getName() + "(00000)";
	    			} else {
	    				labelText = folder.getName() + " (" + bioModel.getPathwayModel().getBiopaxObjects().size() + ")";
	    			}
	    			break;
	    		case BIOPAX_SUMMARY_NODE:
	    			icon = VCellIcons.textNotesIcon;
	    			break;
	    		case BIOPAX_TREE_NODE:
	    			icon = VCellIcons.tableIcon;
	    			break;
	    		}
	    	}
		}
    	setIcon(icon);
    	setFont(font);
    	setText(labelText);
    	if (toolTipSuffix.length() == 0) {
			toolTipSuffix = labelText;
		}
    	setToolTipText(toolTipPrefix + toolTipSuffix);
        return this;
    }
}
