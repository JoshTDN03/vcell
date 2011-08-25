/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.geometry.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.vcell.util.TokenMangler;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.ScrollTable;
import org.vcell.util.gui.UtilCancelException;
import org.vcell.util.gui.ZEnforcer;

import cbit.vcell.client.desktop.biomodel.DocumentEditorSubPanel;
import cbit.vcell.client.desktop.biomodel.IssueManager;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.geometry.AnalyticSubVolume;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.GeometrySpec;
import cbit.vcell.geometry.ImageSubVolume;
import cbit.vcell.geometry.SubVolume;
import cbit.vcell.parser.Expression;
/**
 * This type was created in VisualAge.
 */
@SuppressWarnings("serial")
public class GeometrySubVolumePanel extends DocumentEditorSubPanel {
	private javax.swing.JButton ivjBackButton = null;
	private javax.swing.JButton ivjDeleteButton = null;
	private javax.swing.JButton ivjFrontButton = null;
	private Geometry ivjGeometry = null;
	private boolean ivjConnPtoP2Aligning = false;
	private GeometrySubVolumeTableModel ivjgeometrySubVolumeTableModel = null;
	private javax.swing.JPanel buttonPanel = null;
	private ScrollTable ivjScrollPaneTable = null;
	private javax.swing.ListSelectionModel ivjselectionModel1 = null;
	private SubVolume ivjSelectedSubVolume = null;
	private javax.swing.JLabel ivjJWarningLabel = null;
	private IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private GeometrySpec ivjGeometrySpec = null;
	private JButton addShapeButton = null;

	private AddShapeJPanel addShapeJPanel = null;


class IvjEventHandler implements java.awt.event.ActionListener, java.beans.PropertyChangeListener, javax.swing.event.ListSelectionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == GeometrySubVolumePanel.this.getFrontButton()) 
				moveSubvolumeFront();
			if (e.getSource() == GeometrySubVolumePanel.this.getBackButton()) 
				moveBack();
			if (e.getSource() == GeometrySubVolumePanel.this.getDeleteButton()) 
				deleteSubvolume();
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == GeometrySubVolumePanel.this.getScrollPaneTable() && (evt.getPropertyName().equals("selectionModel"))) 
				connPtoP2SetTarget();
			if (evt.getSource() == GeometrySubVolumePanel.this.getGeometrySpec() && (evt.getPropertyName().equals("subVolumes"))) 
				connEtoC2(evt);
			if (evt.getSource() == GeometrySubVolumePanel.this.getGeometrySpec() && (evt.getPropertyName().equals("subVolumes"))) 
				connEtoM7(evt);
			if (evt.getSource() == GeometrySubVolumePanel.this.getGeometrySpec() && (evt.getPropertyName().equals("warningMessage"))) 
				connEtoM10(evt);
		};
		public void valueChanged(javax.swing.event.ListSelectionEvent e) {
			if (e.getSource() == GeometrySubVolumePanel.this.getselectionModel1()) 
				connEtoM3(e);
		};
	};
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public GeometrySubVolumePanel() {
	super();
	initialize();
}

/**
 * connEtoC1:  (SelectedSubVolume.this --> GeometrySubVolumePanel.refreshButtons()V)
 * @param value cbit.vcell.geometry.SubVolume
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(SubVolume value) {
	try {
		// user code begin {1}
		// user code end
		this.refreshButtons();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC10:  (GeometrySubVolumePanel.initialize() --> GeometrySubVolumePanel.geometrySubVolumePanel_Initialize()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC10() {
	try {
		// user code begin {1}
		// user code end
		this.geometrySubVolumePanel_Initialize();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC2:  (Geometry.subVolumes --> GeometrySubVolumePanel.refreshButtons()V)
 * @param arg1 java.beans.PropertyChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.beans.PropertyChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.refreshButtons();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoM1:  (DeleteButton.action.actionPerformed(java.awt.event.ActionEvent) --> Geometry.removeAnalyticSubVolume(Lcbit.vcell.geometry.AnalyticSubVolume;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void deleteSubvolume() {
	try {
		// user code begin {1}
		// user code end
		if ((getSelectedSubVolume() != null)) {
			AsynchClientTask task1 = new AsynchClientTask("removing subdomain", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
				@Override
				public void run(Hashtable<String, Object> hashTable) throws Exception {
					getGeometrySpec().removeAnalyticSubVolume((AnalyticSubVolume)getSelectedSubVolume());
					getGeometry().precomputeAll();
				}
			};
			ClientTaskDispatcher.dispatch(GeometrySubVolumePanel.this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1}, false);			
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM10:  (Geometry.warningMessage --> JWarningLabel.text)
 * @param arg1 java.beans.PropertyChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM10(java.beans.PropertyChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getJWarningLabel().setVisible(false);
		if (getGeometrySpec().getWarningMessage() != null && getGeometrySpec().getWarningMessage().length() > 0) {		
			getJWarningLabel().setVisible(true);
			getJWarningLabel().setText(String.valueOf(getGeometrySpec().getWarningMessage()));
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM3:  (selectionModel1.listSelection.valueChanged(javax.swing.event.ListSelectionEvent) --> SelectedSubVolume.this)
 * @param arg1 javax.swing.event.ListSelectionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM3(javax.swing.event.ListSelectionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		setSelectedSubVolume(this.findSubVolume());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM6:  (FrontButton.action.actionPerformed(java.awt.event.ActionEvent) --> Geometry.bringForward(Lcbit.vcell.geometry.AnalyticSubVolume;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void moveSubvolumeFront() {
	try {
		// user code begin {1}
		// user code end
		if ((getSelectedSubVolume() != null)) {
			AsynchClientTask task1 = new AsynchClientTask("moving to front", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
				@Override
				public void run(Hashtable<String, Object> hashTable) throws Exception {
					getGeometrySpec().bringForward((AnalyticSubVolume)getSelectedSubVolume());
					getGeometry().precomputeAll();
				}
			};
			ClientTaskDispatcher.dispatch(GeometrySubVolumePanel.this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1}, false);
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM7:  (Geometry.subVolumes --> SelectedSubVolume.this)
 * @param arg1 java.beans.PropertyChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM7(java.beans.PropertyChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		setSelectedSubVolume(this.findSubVolume());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM9:  (BackButton.action.actionPerformed(java.awt.event.ActionEvent) --> Geometry.sendBackward(Lcbit.vcell.geometry.AnalyticSubVolume;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void moveBack() {
	try {
		// user code begin {1}
		// user code end
		if ((getSelectedSubVolume() != null)) {
			AsynchClientTask task1 = new AsynchClientTask("moving to back", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
				@Override
				public void run(Hashtable<String, Object> hashTable) throws Exception {
					getGeometrySpec().sendBackward((AnalyticSubVolume)getSelectedSubVolume());
					getGeometry().precomputeAll();
				}
			};
			ClientTaskDispatcher.dispatch(GeometrySubVolumePanel.this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1}, false);
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connPtoP1SetTarget:  (ScrollPaneTable.model <--> geometrySubVolumeTableModel.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP1SetTarget() {
	/* Set the target from the source */
	try {
		getScrollPaneTable().setModel(getgeometrySubVolumeTableModel());
		// user code begin {1}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connPtoP2SetSource:  (ScrollPaneTable.selectionModel <--> selectionModel1.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP2SetSource() {
	/* Set the source from the target */
	try {
		if (ivjConnPtoP2Aligning == false) {
			// user code begin {1}
			// user code end
			ivjConnPtoP2Aligning = true;
			if ((getselectionModel1() != null)) {
				getScrollPaneTable().setSelectionModel(getselectionModel1());
			}
			// user code begin {2}
			// user code end
			ivjConnPtoP2Aligning = false;
		}
	} catch (java.lang.Throwable ivjExc) {
		ivjConnPtoP2Aligning = false;
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connPtoP2SetTarget:  (ScrollPaneTable.selectionModel <--> selectionModel1.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP2SetTarget() {
	/* Set the target from the source */
	try {
		if (ivjConnPtoP2Aligning == false) {
			// user code begin {1}
			// user code end
			ivjConnPtoP2Aligning = true;
			setselectionModel1(getScrollPaneTable().getSelectionModel());
			// user code begin {2}
			// user code end
			ivjConnPtoP2Aligning = false;
		}
	} catch (java.lang.Throwable ivjExc) {
		ivjConnPtoP2Aligning = false;
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Comment
 */
private SubVolume findSubVolume() {
	int selectedIndex = getselectionModel1().getMinSelectionIndex();
	if (selectedIndex>=0 && getGeometry()!=null && selectedIndex<getGeometry().getGeometrySpec().getNumSubVolumes()){
		return getGeometry().getGeometrySpec().getSubVolumes(selectedIndex);
	}else{
		return null;
	}
}

/**
 * Comment
 */
private void geometrySubVolumePanel_Initialize() {
	
	getScrollPaneTable().setDefaultRenderer(SubVolume.class,new GeometrySubVolumeTableCellRenderer());
	getScrollPaneTable().setDefaultEditor(SubVolume.class,new DefaultCellEditor(new JTextField()) {
			private int lastRow = -1;
			private int lastCol = -1;
		   public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			   lastRow = row;
			   lastCol = column;
			   	delegate.setValue(((SubVolume)value).getName());
			   	return editorComponent;
		   }
		   public final boolean stopCellEditing() {

				//
				//Three things can happen:
				//1.  The current editor contains a value that is validated OK,
				//		continue normally.
				//2.  The current editor contains a value that is validated NOT OK,
				//		user re-enters value until VALIDATE_OK -OR- user CANCELS and edit is lost.
				//3.  The current editor contains a value that CANNOT be validated (Exceptions outside verify,verify not implemented,etc...),
				//		validation is UNKNOWN, keep unvalidated value and continue.
				//
				try{
					String name = (String)delegate.getCellEditorValue();
					while(true){
						if(name.equals(TokenMangler.fixTokenStrict(name))){
							break;
						}
						name = DialogUtils.showInputDialog0(getComponent(), "Subdomain name "+name+" has illegal characters." +"\nProvide new value.", name);
					}
					delegate.setValue(name);//VALIDATE_OK, delegate gets New Good value
				}catch(UtilCancelException e){					
					delegate.setValue(((SubVolume)getScrollPaneTable().getValueAt(lastRow, lastCol)).getName());//delegate gets Last Good value
				}catch(Throwable e){//Delegate keeps UNVALIDATED value
				}			
				return super.stopCellEditing();
			}
	});
	
	getScrollPaneTable().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	refreshButtons();
}

/**
 * Return the BackButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getBackButton() {
	if (ivjBackButton == null) {
		try {
			ivjBackButton = new javax.swing.JButton();
			ivjBackButton.setName("BackButton");
			ivjBackButton.setText("Back");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjBackButton;
}
/**
 * Return the DeleteButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getDeleteButton() {
	if (ivjDeleteButton == null) {
		try {
			ivjDeleteButton = new javax.swing.JButton();
			ivjDeleteButton.setName("DeleteButton");
			ivjDeleteButton.setText("Delete");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDeleteButton;
}
/**
 * Return the FrontButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getFrontButton() {
	if (ivjFrontButton == null) {
		try {
			ivjFrontButton = new javax.swing.JButton();
			ivjFrontButton.setName("FrontButton");
			ivjFrontButton.setText("Front");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjFrontButton;
}
/**
 * Return the Geometry property value.
 * @return cbit.vcell.geometry.Geometry
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public Geometry getGeometry() {
	// user code begin {1}
	// user code end
	return ivjGeometry;
}
/**
 * Return the GeometrySpec property value.
 * @return cbit.vcell.geometry.GeometrySpec
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private GeometrySpec getGeometrySpec() {
	// user code begin {1}
	// user code end
	return ivjGeometrySpec;
}
/**
 * Return the geometrySubVolumeTableModel property value.
 * @return cbit.vcell.geometry.gui.GeometrySubVolumeTableModel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private GeometrySubVolumeTableModel getgeometrySubVolumeTableModel() {
	if (ivjgeometrySubVolumeTableModel == null) {
		try {
			ivjgeometrySubVolumeTableModel = new GeometrySubVolumeTableModel(getScrollPaneTable());
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjgeometrySubVolumeTableModel;
}
/**
 * Return the JPanel1 property value.
 * @return javax.swing.JPanel
 */
private javax.swing.JPanel getButtonPanel() {
	if (buttonPanel == null) {
		try {
			addShapeButton = new JButton("Add Subdomain...");
			
			getFrontButton().putClientProperty("JButton.buttonType", "roundRect");
			getBackButton().putClientProperty("JButton.buttonType", "roundRect");
			addShapeButton.putClientProperty("JButton.buttonType", "roundRect");
			getDeleteButton().putClientProperty("JButton.buttonType", "roundRect");
			
			buttonPanel = new javax.swing.JPanel();
			buttonPanel.setName("JPanel1");
			final java.awt.GridBagLayout gridBagLayout = new java.awt.GridBagLayout();
			buttonPanel.setLayout(gridBagLayout);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			buttonPanel.add(getFrontButton(), gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			buttonPanel.add(getBackButton(), gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			buttonPanel.add(addShapeButton, gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			buttonPanel.add(getDeleteButton(), gbc);

			addShapeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(addShapeJPanel == null){
						addShapeJPanel = new AddShapeJPanel();
						addShapeJPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
						addShapeJPanel.setDefaultCenter(
								getGeometry().getOrigin().getX()+getGeometry().getExtent().getX()/2,
								(getGeometry().getDimension() > 1?getGeometry().getOrigin().getY()+getGeometry().getExtent().getY()/2:null),
								(getGeometry().getDimension() > 2?getGeometry().getOrigin().getZ()+getGeometry().getExtent().getZ()/2:null));
						addShapeJPanel.setDimension(getGeometry().getDimension());
					}
					while(true){
						try {
							final boolean[] acceptFlag = new boolean[] {false};
							final JDialog d = new JDialog();
							d.setTitle("Define New Subdomain Shape");
							
							JPanel main = new JPanel();
							BoxLayout mainBoxLayout = new BoxLayout(main,BoxLayout.Y_AXIS);
							main.setLayout(mainBoxLayout);
							
							JPanel addCancelJPanel = new JPanel();
							addCancelJPanel.setBorder(new EmptyBorder(10,10,10,10));
							BoxLayout addCancelBoxLayout = new BoxLayout(addCancelJPanel,BoxLayout.X_AXIS);
							addCancelJPanel.setLayout(addCancelBoxLayout);
							final JButton addJButton = new JButton("Add New Subdomain");
							addJButton.addActionListener(new ActionListener(){
								public void actionPerformed(ActionEvent e) {
									d.dispose();
									acceptFlag[0] = true;
								}
							});
							addCancelJPanel.add(addJButton);
							JButton cancelJButton = new JButton("Cancel");
							cancelJButton.addActionListener(new ActionListener(){
								public void actionPerformed(ActionEvent e) {
									d.dispose();
								}
							});
							addCancelJPanel.add(cancelJButton);
							
							main.add(addShapeJPanel);
							main.add(Box.createVerticalStrut(10));
							main.add(addCancelJPanel);
							main.add(Box.createVerticalStrut(10));
							
							addShapeJPanel.addPropertyChangeListener(new PropertyChangeListener(){
								public void propertyChange(PropertyChangeEvent evt) {
									if(evt.getPropertyName().equals(AddShapeJPanel.PROPCHANGE_VALID_ANALYTIC)){
										addJButton.setEnabled(((Boolean)evt.getNewValue()));
									}
								}
							});
							d.setModal(true);
							d.getContentPane().add(main);
							d.pack();
							ZEnforcer.showModalDialogOnTop(d, GeometrySubVolumePanel.this);

							if(acceptFlag[0]){
								AsynchClientTask task1 = new AsynchClientTask("adding subdomain", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
									@Override
									public void run(Hashtable<String, Object> hashTable) throws Exception {
										//AddShapeJPanel.addSubVolumeToGeometrySpec(addShapeJPanel,getGeometrySpec());
										getGeometrySpec().addSubVolume(
											new AnalyticSubVolume(
													null, getGeometrySpec().getFreeSubVolumeName(),
													new Expression(addShapeJPanel.getCurrentAnalyticExpression()),
													-1),true);
										getGeometry().precomputeAll();
									}
								};
								ClientTaskDispatcher.dispatch(GeometrySubVolumePanel.this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1}, true, false, false, null, true);
							}
							break;
						} catch (Exception e1) {
							e1.printStackTrace();
							DialogUtils.showErrorDialog(GeometrySubVolumePanel.this, "Error adding shape:\n"+e1.getMessage(), e1);
						}
					}
				}
			});
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return buttonPanel;
}

/**
 * Return the JWarningLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJWarningLabel() {
	if (ivjJWarningLabel == null) {
		try {
			ivjJWarningLabel = new javax.swing.JLabel();
			ivjJWarningLabel.setName("JWarningLabel");
			ivjJWarningLabel.setText(" ");
			ivjJWarningLabel.setVisible(false);
			ivjJWarningLabel.setForeground(new java.awt.Color(255,0,1));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJWarningLabel;
}
/**
 * Return the ScrollPaneTable property value.
 * @return javax.swing.JTable
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ScrollTable getScrollPaneTable() {
	if (ivjScrollPaneTable == null) {
		try {
			ivjScrollPaneTable = new ScrollTable();
			ivjScrollPaneTable.setName("ScrollPaneTable");
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjScrollPaneTable;
}
/**
 * Comment
 */
private SubVolume getSelectedSubVolume() {
	// user code begin {1}
	// user code end
	return ivjSelectedSubVolume;
}
/**
 * Return the selectionModel1 property value.
 * @return javax.swing.ListSelectionModel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.ListSelectionModel getselectionModel1() {
	// user code begin {1}
	// user code end
	return ivjselectionModel1;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	System.out.println("--------- UNCAUGHT EXCEPTION --------- in GeometrySubVolumePanel");
	exception.printStackTrace(System.out);
}
/**
 * Initializes connections
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getFrontButton().addActionListener(ivjEventHandler);
	getBackButton().addActionListener(ivjEventHandler);
	getDeleteButton().addActionListener(ivjEventHandler);
	getScrollPaneTable().addPropertyChangeListener(ivjEventHandler);
	connPtoP1SetTarget();
	connPtoP2SetTarget();
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("GeometrySubVolumePanel");
		setLayout(new java.awt.GridBagLayout());
		setSize(426, 185);

		java.awt.GridBagConstraints constraintsJScrollPane1 = new java.awt.GridBagConstraints();
		constraintsJScrollPane1.gridx = 0; constraintsJScrollPane1.gridy = 0;
		constraintsJScrollPane1.fill = java.awt.GridBagConstraints.BOTH;
		constraintsJScrollPane1.weightx = 1.0;
		constraintsJScrollPane1.weighty = 1.0;
		constraintsJScrollPane1.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getScrollPaneTable().getEnclosingScrollPane(), constraintsJScrollPane1);

		java.awt.GridBagConstraints constraintsJPanel1 = new java.awt.GridBagConstraints();
		constraintsJPanel1.gridx = 1; constraintsJPanel1.gridy = 0;
		constraintsJPanel1.fill = java.awt.GridBagConstraints.BOTH;
		constraintsJPanel1.weighty = 1.0;
		constraintsJPanel1.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getButtonPanel(), constraintsJPanel1);
		
		java.awt.GridBagConstraints constraintsJWarningLabel = new java.awt.GridBagConstraints();
		constraintsJWarningLabel.gridx = 0; constraintsJWarningLabel.gridy = 1;
		constraintsJWarningLabel.gridwidth = 2;
		constraintsJWarningLabel.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsJWarningLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getJWarningLabel(), constraintsJWarningLabel);
		initConnections();
		connEtoC10();
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
		GeometrySubVolumePanel aGeometrySubVolumePanel;
		aGeometrySubVolumePanel = new GeometrySubVolumePanel();
		frame.setContentPane(aGeometrySubVolumePanel);
		frame.setSize(aGeometrySubVolumePanel.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JPanel");
		exception.printStackTrace(System.out);
	}
}
/**
 * This method was created in VisualAge.
 */
private void refreshButtons() {
	boolean bHasGeometry = getGeometry()!= null;
	boolean bHasGeomSpec = (bHasGeometry?getGeometry().getGeometrySpec()!= null:false);
	boolean bImageBased = (bHasGeomSpec?getGeometry().getGeometrySpec().getImage() != null:false);

	addShapeButton.setEnabled(true);
	SubVolume selectedSubVolume = getSelectedSubVolume();
	if (!bHasGeomSpec  || getGeometry().getDimension()==0){
		getFrontButton().setEnabled(false);
		getBackButton().setEnabled(false);
		getDeleteButton().setEnabled(false);
		addShapeButton.setEnabled(false);
	}else if (selectedSubVolume == null){
		getFrontButton().setEnabled(false);
		getBackButton().setEnabled(false);
		getDeleteButton().setEnabled(false);
	}else{
		GeometrySpec geometrySpec = getGeometry().getGeometrySpec();
		int numAnalyticSubVolumes = geometrySpec.getNumAnalyticSubVolumes();
		if (numAnalyticSubVolumes>1){
			getFrontButton().setEnabled(geometrySpec.getSubVolumeIndex(selectedSubVolume)>0);
			getBackButton().setEnabled(geometrySpec.getSubVolumeIndex(selectedSubVolume)<(numAnalyticSubVolumes-1));
		}else{
			getFrontButton().setEnabled(false);
			getBackButton().setEnabled(false);
		}
		if (selectedSubVolume instanceof ImageSubVolume || (!bImageBased && (geometrySpec.getNumSubVolumes() <= 1))){
			getDeleteButton().setEnabled(false);
		}else{
			getDeleteButton().setEnabled(true);
		}
	}
}
/**
 * Set the Geometry to a new value.
 * @param newValue cbit.vcell.geometry.Geometry
 */
public void setGeometry(Geometry newValue) {
	if (ivjGeometry != newValue) {
		try {
			Geometry oldValue = getGeometry();
			ivjGeometry = newValue;
			if (ivjGeometry != null) {
				getgeometrySubVolumeTableModel().setGeometry(ivjGeometry);
				setGeometrySpec(ivjGeometry.getGeometrySpec());
			}
			firePropertyChange("geometry", oldValue, newValue);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	};
}
/**
 * Set the GeometrySpec to a new value.
 * @param newValue cbit.vcell.geometry.GeometrySpec
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setGeometrySpec(GeometrySpec newValue) {
	if (ivjGeometrySpec != newValue) {
		try {
			/* Stop listening for events from the current object */
			if (ivjGeometrySpec != null) {
				ivjGeometrySpec.removePropertyChangeListener(ivjEventHandler);
			}
			ivjGeometrySpec = newValue;

			/* Listen for events from the new object */
			if (ivjGeometrySpec != null) {
				ivjGeometrySpec.addPropertyChangeListener(ivjEventHandler);
			}
			this.refreshButtons();
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}
/**
 * Set the SelectedSubVolume to a new value.
 * @param newValue cbit.vcell.geometry.SubVolume
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setSelectedSubVolume(SubVolume newValue) {
	if (ivjSelectedSubVolume != newValue) {
		try {
			ivjSelectedSubVolume = newValue;
			connEtoC1(ivjSelectedSubVolume);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}
/**
 * Set the selectionModel1 to a new value.
 * @param newValue javax.swing.ListSelectionModel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setselectionModel1(javax.swing.ListSelectionModel newValue) {
	if (ivjselectionModel1 != newValue) {
		try {
			/* Stop listening for events from the current object */
			if (ivjselectionModel1 != null) {
				ivjselectionModel1.removeListSelectionListener(ivjEventHandler);
			}
			ivjselectionModel1 = newValue;

			/* Listen for events from the new object */
			if (ivjselectionModel1 != null) {
				ivjselectionModel1.addListSelectionListener(ivjEventHandler);
			}
			connPtoP2SetSource();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}

@Override
public void setIssueManager(IssueManager issueManager) {
	super.setIssueManager(issueManager);
	getgeometrySubVolumeTableModel().setIssueManager(issueManager);
}

@Override
protected void onSelectedObjectsChange(Object[] selectedObjects) {
}
}
