/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.mapping.gui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cbit.vcell.data.DataSymbol;
import cbit.vcell.mapping.MicroscopeMeasurement;
import cbit.vcell.mapping.MicroscopeMeasurement.ExperimentalPSF;
import cbit.vcell.mapping.MicroscopeMeasurement.ProjectionZKernel;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.parser.Expression;
import cbit.vcell.psf.PointSpreadFunctionManagement;

@SuppressWarnings("serial")
public class MicroscopeMeasurementPanel extends javax.swing.JPanel {
	
	private JTextField nameTextField;
	private JRadioButton rdbtnZprojection = null;
	private JRadioButton rdbtnExperimental = null;
	JComboBox pointSpreadFunctionsComboBox = null;
	private SimulationContext simulationContext = null;
	private AllPointSpreadFunctionsComboModel pointSpreadFunctionsComboModel = new AllPointSpreadFunctionsComboModel();
	private AllSpeciesContextListModel allSpeciesContextListModel = new AllSpeciesContextListModel();
	private FluorescenceSpeciesContextListModel fluorescenceSpeciesContextListModel = new FluorescenceSpeciesContextListModel();
	private JButton removeButton;
	private JButton addButton;
	private JButton importPsfButton;
	private JList allSpeciesContextList;
	private JList fluorescentSpeciesContextList;
	private ListCellRenderer speciesContextCellRenderer = new DefaultListCellRenderer(){
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof SpeciesContext && component instanceof JLabel){
				SpeciesContext sc = (SpeciesContext)value;
				((JLabel)component).setText(sc.getName());
			}
			return component;
		}
	};
	
	public SimulationContext getSimulationContext() {
		return simulationContext;
	}

	public void setSimulationContext(SimulationContext simulationContext) {
		if (this.simulationContext == simulationContext) {
			return;
		}
		this.simulationContext = simulationContext;
		allSpeciesContextListModel.setSimulationContext(simulationContext);
		fluorescenceSpeciesContextListModel.setSimulationContext(simulationContext);

		pointSpreadFunctionsComboModel.setSimulationContext(simulationContext);

		refreshButtons();
	}

	public MicroscopeMeasurementPanel() {
		super();
		initialize();
	}
	
	private void initialize() {
		try {
			// top panel
			JPanel topPanel = new JPanel();
			JLabel lblFluoescenceFunctionName = new JLabel("fluorescence function name ");			
			topPanel.add(lblFluoescenceFunctionName);			
			nameTextField = new JTextField();
			nameTextField.setColumns(20);
			topPanel.add(nameTextField);
			
			// middle panel
			JPanel middlePanel = new JPanel();
			middlePanel.setLayout(new GridBagLayout());
			
			JLabel label = new JLabel("all species");
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(4, 4, 4, 4);
			gbc_label.gridx = 0;
			gbc_label.gridy = 0;
			middlePanel.add(label, gbc_label);
			
			JLabel label_1 = new JLabel("fluorescent species");
			GridBagConstraints gbc_label_1 = new GridBagConstraints();
			gbc_label_1.insets = new Insets(4, 4, 4, 4);
			gbc_label_1.gridx = 2;
			gbc_label_1.gridy = 0;
			middlePanel.add(label_1, gbc_label_1);
			
			allSpeciesContextList = new JList();
			allSpeciesContextList.setModel(allSpeciesContextListModel );
			allSpeciesContextList.setCellRenderer(speciesContextCellRenderer);
			allSpeciesContextList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					refreshButtons();
				}
			});
			GridBagConstraints gbc_allSpeciesContextList = new GridBagConstraints();
			gbc_allSpeciesContextList.insets = new Insets(4, 4, 4, 4);
			gbc_allSpeciesContextList.weighty = 1.0;
			gbc_allSpeciesContextList.weightx = 1.0;
			gbc_allSpeciesContextList.fill = GridBagConstraints.BOTH;
			gbc_allSpeciesContextList.gridx = 0;
			gbc_allSpeciesContextList.gridy = 1;
			JScrollPane allSpeciesScrollPane = new JScrollPane(allSpeciesContextList);
			allSpeciesScrollPane.setPreferredSize(new Dimension(250, 80));
			middlePanel.add(allSpeciesScrollPane, gbc_allSpeciesContextList);
			
			JPanel buttonPanel = new JPanel(new GridBagLayout());
			addButton = new JButton(">>");
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addButtonActionPerformed();
				}
			});
			GridBagConstraints gbc_btnAddButton = new GridBagConstraints();
			gbc_btnAddButton.weightx = 1.0;
			gbc_btnAddButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnAddButton.gridx = 0;
			gbc_btnAddButton.gridy = 0;
			gbc_btnAddButton.insets = new Insets(4, 4, 4, 4);
			buttonPanel.add(addButton, gbc_btnAddButton);
			
			removeButton = new JButton("<<");
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeButtonActionPerformed();
				}
			});
			GridBagConstraints gbc_removeButton = new GridBagConstraints();
			gbc_removeButton.weightx = 1.0;
			gbc_removeButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_removeButton.gridx = 0;
			gbc_removeButton.gridy = 1;
			gbc_removeButton.insets = new Insets(4, 4, 4, 4);
			buttonPanel.add(removeButton, gbc_removeButton);
			
			GridBagConstraints gbc_panel_buttons = new GridBagConstraints();
			gbc_panel_buttons.insets = new Insets(4, 4, 4, 4);
			gbc_panel_buttons.gridx = 1;
			gbc_panel_buttons.gridy = 1;
			middlePanel.add(buttonPanel, gbc_panel_buttons);
			
			fluorescentSpeciesContextList = new JList();
			fluorescentSpeciesContextList.setModel(fluorescenceSpeciesContextListModel);
			fluorescentSpeciesContextList.setCellRenderer(speciesContextCellRenderer);
			fluorescentSpeciesContextList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					refreshButtons();
				}
			});
			GridBagConstraints gbc_fluorescentSpeciesContextList = new GridBagConstraints();
			gbc_fluorescentSpeciesContextList.fill = GridBagConstraints.BOTH;
			gbc_fluorescentSpeciesContextList.weighty = 1.0;
			gbc_fluorescentSpeciesContextList.weightx = 1.0;
			gbc_fluorescentSpeciesContextList.gridx = 2;
			gbc_fluorescentSpeciesContextList.gridy = 1;
			gbc_fluorescentSpeciesContextList.insets = new Insets(4, 4, 4, 4);
			JScrollPane fluorSpeciesScrollPane = new JScrollPane(fluorescentSpeciesContextList);
			fluorSpeciesScrollPane.setPreferredSize(new Dimension(250, 80));
			middlePanel.add(fluorSpeciesScrollPane, gbc_fluorescentSpeciesContextList);
					
			// bottom panel
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridBagLayout());			
			JLabel lblConvolutionKernel = new JLabel("point spread function");
			GridBagConstraints gbc_lblConvolutionKernel = new GridBagConstraints();
			gbc_lblConvolutionKernel.gridx = 0;
			gbc_lblConvolutionKernel.gridy = 0;
			gbc_lblConvolutionKernel.insets = new Insets(4, 4, 5, 5);
			bottomPanel.add(lblConvolutionKernel, gbc_lblConvolutionKernel);		
			
			rdbtnZprojection = new JRadioButton("z-projection");
			rdbtnZprojection.setSelected(true);
			rdbtnZprojection.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					rdbtnZprojectionActionPerformed(e);
				}
			});
			GridBagConstraints gbc_rdbtnZprojection = new GridBagConstraints();
			gbc_rdbtnZprojection.gridx = 1;
			gbc_rdbtnZprojection.gridy = 0;
			gbc_rdbtnZprojection.anchor = GridBagConstraints.LINE_START;
			gbc_rdbtnZprojection.insets = new Insets(4, 4, 5, 5);
			bottomPanel.add(rdbtnZprojection, gbc_rdbtnZprojection);
			
			rdbtnExperimental = new JRadioButton("experimental");
			rdbtnExperimental.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					rdbtnExperimentalActionPerformed(e);
				}
			});
			rdbtnExperimental.setEnabled(false);
			GridBagConstraints gbc_rdbtnExperimental = new GridBagConstraints();
			gbc_rdbtnExperimental.gridx = 1;
			gbc_rdbtnExperimental.gridy = 1;
			gbc_rdbtnExperimental.anchor = GridBagConstraints.LINE_START;
			gbc_rdbtnExperimental.insets = new Insets(4, 4, 4, 5);
			bottomPanel.add(rdbtnExperimental, gbc_rdbtnExperimental);		
			
			pointSpreadFunctionsComboBox = new JComboBox();
			pointSpreadFunctionsComboBox.setModel(pointSpreadFunctionsComboModel);
			pointSpreadFunctionsComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pointSpreadFunctionsComboBoxActionPerformed(e);
				}
			});
			pointSpreadFunctionsComboBox.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					pointSpreadFunctionsComboBoxPropertyChange(e);
					
				}
			});
			pointSpreadFunctionsComboBox.setEnabled(false);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.gridx = 2;
			gbc_comboBox.gridy = 1;
			gbc_comboBox.insets = new Insets(4, 4, 4, 5);
			gbc_comboBox.ipadx = 30;
			bottomPanel.add(pointSpreadFunctionsComboBox, gbc_comboBox);

			setName("MicroscopeMeasurementPanel");
			setLayout(new GridBagLayout());			
			GridBagConstraints gbc_top = new GridBagConstraints();
			gbc_top.weightx = 1.0;
			gbc_top.weighty = 0.1;
			gbc_top.fill = GridBagConstraints.BOTH;
			gbc_top.gridx = 0;
			gbc_top.gridy = 0;
			add(topPanel, gbc_top);
			
			GridBagConstraints gbc_middle = new GridBagConstraints();
			gbc_middle.weighty = 1.0;
			gbc_middle.weightx = 1.0;
			gbc_middle.fill = GridBagConstraints.BOTH;
			gbc_middle.gridx = 0;
			gbc_middle.gridy = 1;
			add(middlePanel, gbc_middle);
			
			GridBagConstraints gbc_bottom = new GridBagConstraints();
			gbc_bottom.weightx = 1.0;
			gbc_bottom.weighty = 0.1;
			gbc_bottom.fill = GridBagConstraints.BOTH;
			gbc_bottom.gridx = 0;
			gbc_bottom.gridy = 2;
			add(bottomPanel, gbc_bottom);
			
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(rdbtnZprojection);
			buttonGroup.add(rdbtnExperimental);
			
			importPsfButton = new JButton("Import PSF");
			importPsfButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					importPSFButtonActionPerformed();
				}
			});
			GridBagConstraints gbc_btnImportPsf = new GridBagConstraints();
			gbc_btnImportPsf.gridx = 3;
			gbc_btnImportPsf.gridy = 1;
			bottomPanel.add(importPsfButton, gbc_btnImportPsf);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	
	// selection changed in combobox
	protected void pointSpreadFunctionsComboBoxActionPerformed(ActionEvent e) {
//		System.out.println("ComboBoxActionPerformed   source:  " + e.getSource().getClass().getName() + ",  name: " + e.getActionCommand());
		if(pointSpreadFunctionsComboModel.getSize() == 0) {
			if(!rdbtnZprojection.isSelected()) {
				rdbtnZprojection.setSelected(true);
			}
			if(rdbtnExperimental.isEnabled()) {
				rdbtnExperimental.setEnabled(false);
			}
			if(pointSpreadFunctionsComboBox.isEnabled()) {
				pointSpreadFunctionsComboBox.setEnabled(false);
			}
//			createProjectionZConvolutionKernel();	// new ProjectionZ kernel
		} else {
			if(!rdbtnExperimental.isEnabled()) {
				rdbtnExperimental.setEnabled(true);
			}
//			createExperimentalConvolutionKernel();	// new ExperimentalPSF kernel
		}
	}
	
	// combo content needs to be updated
	public void pointSpreadFunctionsComboBoxPropertyChange(PropertyChangeEvent e) {
//		System.out.println("ComboBoxPropertyChange   source:  " + e.getSource().getClass().getName() + ",  name: " + e.getPropertyName());
		pointSpreadFunctionsComboModel.refresh();
		if(pointSpreadFunctionsComboModel.getSize() == 0) {
			if(!rdbtnZprojection.isSelected()) {
				rdbtnZprojection.setSelected(true);
			}
			if(rdbtnExperimental.isEnabled()) {
				rdbtnExperimental.setEnabled(false);
			}
			if(pointSpreadFunctionsComboBox.isEnabled()) {
				pointSpreadFunctionsComboBox.setEnabled(false);
			}
		} else {
			if(!rdbtnExperimental.isEnabled()) {
				rdbtnExperimental.setEnabled(true);
			}
		}
	}
	
	// new ProjectionZ kernel
	private void createProjectionZConvolutionKernel() {
		simulationContext.getMicroscopeMeasurement().setConvolutionKernel(new ProjectionZKernel());
//		System.out.println("       create ProjectionZ kernel");
	}
	// new ExperimentalPSF kernel
	private void createExperimentalConvolutionKernel() {
		String psfName = (String)pointSpreadFunctionsComboBox.getSelectedItem();
		for (DataSymbol dataSymbol : simulationContext.getDataContext().getDataSymbols()){
			if (dataSymbol.getName().equals(psfName)){
				simulationContext.getMicroscopeMeasurement().setConvolutionKernel(new ExperimentalPSF(dataSymbol));
//				System.out.println("       create Experimental kernel");
				break;
			}
		}
	}
	
	protected void rdbtnZprojectionActionPerformed(ActionEvent e) {
		createProjectionZConvolutionKernel();	// new ProjectionZ kernel
		if(pointSpreadFunctionsComboBox.isEnabled()) {
			pointSpreadFunctionsComboBox.setEnabled(false);
		}
	}
	protected void rdbtnExperimentalActionPerformed(ActionEvent e) {
		createExperimentalConvolutionKernel();	// new ExperimentalPSF kernel
		if(!pointSpreadFunctionsComboBox.isEnabled()) {
			pointSpreadFunctionsComboBox.setEnabled(true);
		}
	}

	protected void refreshButtons() {
		boolean bFluorescentSpeciesSelected = fluorescentSpeciesContextList.getSelectedValue()!=null;
		boolean bNonFluorescentSpeciesSelected = allSpeciesContextList.getSelectedValue()!=null;
		removeButton.setEnabled(bFluorescentSpeciesSelected);
		addButton.setEnabled(bNonFluorescentSpeciesSelected);
	}

	protected void addButtonActionPerformed() {
		SpeciesContext selectedSpeciesContext = (SpeciesContext)allSpeciesContextList.getSelectedValue();
		if (selectedSpeciesContext != null && simulationContext != null && simulationContext.getMicroscopeMeasurement() != null){
			simulationContext.getMicroscopeMeasurement().addFluorescentSpecies(selectedSpeciesContext);
		}
	}

	protected void removeButtonActionPerformed() {
		SpeciesContext selectedSpeciesContext = (SpeciesContext)fluorescentSpeciesContextList.getSelectedValue();
		if (selectedSpeciesContext != null && simulationContext != null && simulationContext.getMicroscopeMeasurement() != null){
			simulationContext.getMicroscopeMeasurement().removeFluorescentSpecies(selectedSpeciesContext);
		}
	}
	
	protected void importPSFButtonActionPerformed() {
		PointSpreadFunctionManagement psfManager = new PointSpreadFunctionManagement(MicroscopeMeasurementPanel.this, 
				getSimulationContext());
		psfManager.importPointSpreadFunction();
	}


	private void handleException(Throwable exception) {
		System.out.println("--------- UNCAUGHT EXCEPTION --------- in MicroscopeMeasurementPanel");
		exception.printStackTrace(System.out);
	}
	

	public static void main(java.lang.String[] args) {
		try {
			JFrame frame = new javax.swing.JFrame("SimulationListPanel");
			MicroscopeMeasurementPanel aPanel = new MicroscopeMeasurementPanel();
			frame.setContentPane(aPanel);
			frame.setSize(aPanel.getSize());
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					System.exit(0);
				};
			});
			java.awt.Insets insets = frame.getInsets();
			frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
			frame.setVisible(true);
		} catch (Throwable exception) {
			System.err.println("Exception occurred in main() of javax.swing.JPanel");
			exception.printStackTrace(System.out);
		}
	}


	
}
