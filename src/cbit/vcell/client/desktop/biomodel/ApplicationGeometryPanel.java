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

import javax.swing.Icon;
import javax.swing.JComponent;

import cbit.vcell.client.BioModelWindowManager;
import cbit.vcell.client.GuiConstants;
import cbit.vcell.client.desktop.biomodel.DocumentEditorTreeModel.DocumentEditorTreeFolderClass;
import cbit.vcell.client.desktop.biomodel.SelectionManager.ActiveView;
import cbit.vcell.client.desktop.biomodel.SelectionManager.ActiveViewID;
import cbit.vcell.geometry.gui.GeometryViewer;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.gui.StructureMappingCartoonPanel;

@SuppressWarnings("serial")
public class ApplicationGeometryPanel extends ApplicationSubPanel {
	private GeometryViewer geometryViewer;
	private StructureMappingCartoonPanel structureMappingCartoonPanel;	
		
	private enum GeometryPanelTabID {
		structure_mapping("Structure Mapping"),
		geometry_definition("Geometry Definition");
		
		String title = null;
		GeometryPanelTabID(String name) {
			this.title = name;
		}
	}
	
	private class GeometryPanelTab {
		GeometryPanelTabID id;
		JComponent component = null;
		Icon icon = null;
		GeometryPanelTab(GeometryPanelTabID id, JComponent component, Icon icon) {
			this.id = id;
			this.component = component;
			this.icon = icon;
		}		
	}
	
	public ApplicationGeometryPanel() {
		super();
		initialize();
	}

	@Override
	protected void initialize(){
		super.initialize();
		geometryViewer = new GeometryViewer();
		structureMappingCartoonPanel = new StructureMappingCartoonPanel();
		
		GeometryPanelTab geometryPanelTabs[] = new GeometryPanelTab[GeometryPanelTabID.values().length]; 
		geometryPanelTabs[GeometryPanelTabID.structure_mapping.ordinal()] = new GeometryPanelTab(GeometryPanelTabID.structure_mapping, structureMappingCartoonPanel, null);
		geometryPanelTabs[GeometryPanelTabID.geometry_definition.ordinal()] = new GeometryPanelTab(GeometryPanelTabID.geometry_definition, geometryViewer, null);
		
		for (GeometryPanelTab tab : geometryPanelTabs) {
			tab.component.setBorder(GuiConstants.TAB_PANEL_BORDER);
			tabbedPane.addTab(tab.id.title, tab.icon, tab.component);
		}
	}
	
	@Override
	public void setSimulationContext(SimulationContext newValue) {
		super.setSimulationContext(newValue);
		structureMappingCartoonPanel.setSimulationContext(simulationContext);		
		updateGeometryViewerOwner();
	}

	@Override
	public void setBioModelWindowManager(BioModelWindowManager newValue) {
		super.setBioModelWindowManager(newValue);
		geometryViewer.addActionListener(bioModelWindowManager);
		updateGeometryViewerOwner();
	}	

	@Override
	public void setSelectionManager(SelectionManager selectionManager) {
		super.setSelectionManager(selectionManager);
		structureMappingCartoonPanel.setSelectionManager(selectionManager);
	}

	@Override
	protected void onActiveViewChange(ActiveView activeView) {
		super.onActiveViewChange(activeView);
		if (DocumentEditorTreeFolderClass.GEOMETRY_NODE.equals(activeView.getDocumentEditorTreeFolderClass())) {
			if (activeView.getActiveViewID() != null) {
				if (activeView.getActiveViewID().equals(ActiveViewID.geometry_definition)) {
					tabbedPane.setSelectedIndex(GeometryPanelTabID.geometry_definition.ordinal());
				} else if (activeView.getActiveViewID().equals(ActiveViewID.structure_mapping)) {
					tabbedPane.setSelectedIndex(GeometryPanelTabID.structure_mapping.ordinal());
				}
			}
		}
	}

	@Override
	public ActiveView getActiveView() {
		Component selectedComponent = tabbedPane.getSelectedComponent();
		ActiveViewID activeViewID = null;
		if (selectedComponent == structureMappingCartoonPanel) {
			activeViewID = ActiveViewID.structure_mapping;
		} else if (selectedComponent == geometryViewer) {
			activeViewID = ActiveViewID.geometry_definition;
		}
		return new ActiveView(simulationContext, DocumentEditorTreeFolderClass.GEOMETRY_NODE, activeViewID);
	}
	
	@Override
	public void setIssueManager(IssueManager issueManager) {
		super.setIssueManager(issueManager);
		structureMappingCartoonPanel.setIssueManager(issueManager);
		geometryViewer.setIssueManager(issueManager);
	}
	
	private void updateGeometryViewerOwner(){
		geometryViewer.setGeometryOwner(simulationContext,(bioModelWindowManager==null?null:bioModelWindowManager.getUserPreferences()));
	}
}
