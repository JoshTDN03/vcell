/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.math.gui;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.vcell.util.Extent;
import org.vcell.util.ISize;
import org.vcell.util.gui.DialogUtils;

import cbit.vcell.client.GuiConstants;
import cbit.vcell.solver.MeshSpecification;

/**
 * Insert the type's description here.
 * Creation date: (1/9/01 8:56:10 AM)
 * @author: Jim Schaff
 */
public class MeshSpecificationPanel extends javax.swing.JPanel {
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private javax.swing.JTextField ivjGeometrySizeTextField = null;
	private javax.swing.JLabel ivjXLabel = null;
	private javax.swing.JLabel ivjYLabel = null;
	private javax.swing.JLabel ivjZLabel = null;
	private javax.swing.JTextField ivjXTextField = null;
	private javax.swing.JTextField ivjYTextField = null;
	private javax.swing.JTextField ivjZTextField = null;
	private javax.swing.JLabel ivjGeometrySizeLabel = null;
	private javax.swing.JLabel ivjMeshSizeLabel = null;
	private MeshSpecification fieldMeshSpecification = null;
	private javax.swing.JLabel ivjJLabelTitle = null;
	private JCheckBox autoMeshSizeCheckBox = null;
	private boolean bInProgress = false;
	private JTextField totalSizeTextField = new JTextField();
	private JTextField ivjDxTextField = new JTextField();
	private JTextField ivjDyTextField = new JTextField();
	private JTextField ivjDzTextField = new JTextField();
	private JLabel ivjDyLabel = new JLabel("\u0394y");
	private JLabel ivjDzLabel = new JLabel("\u0394z");

class IvjEventHandler implements java.awt.event.FocusListener, java.beans.PropertyChangeListener, ItemListener, DocumentListener {
		public void focusGained(java.awt.event.FocusEvent e) {};
		public void focusLost(java.awt.event.FocusEvent e) {
			if (e.isTemporary()) {
				return;
			}
			if (e.getSource() == MeshSpecificationPanel.this.getXTextField()) 
				connEtoC2(e);
			if (e.getSource() == MeshSpecificationPanel.this.getYTextField()) 
				connEtoC3(e);
			if (e.getSource() == MeshSpecificationPanel.this.getZTextField()) 
				connEtoC4(e);
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == MeshSpecificationPanel.this && (evt.getPropertyName().equals("meshSpecification"))) 
				updateDisplay();
		}
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED && e.getSource() == getAutoMeshSizeCheckBox()) {
				autoUpdateSizes(e);
			}
			
		}
		public void changedUpdate(DocumentEvent e) {
			autoUpdateSizes(e);			
		}		
		public void insertUpdate(DocumentEvent e) {
			autoUpdateSizes(e);			
		}
		public void removeUpdate(DocumentEvent e) {
			autoUpdateSizes(e);			
		};
	};

/**
 * MeshSpecificationPanel constructor comment.
 */
public MeshSpecificationPanel() {
	super();
	addPropertyChangeListener(ivjEventHandler);
	initialize();
}

/**
 * connEtoC2:  (XTextField.focus.focusLost(java.awt.event.FocusEvent) --> MeshSpecificationPanel.updateSize()V)
 * @param arg1 java.awt.event.FocusEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.FocusEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.updateSize();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC3:  (YTextField.focus.focusLost(java.awt.event.FocusEvent) --> MeshSpecificationPanel.updateSize()V)
 * @param arg1 java.awt.event.FocusEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3(java.awt.event.FocusEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.updateSize();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC4:  (ZTextField.focus.focusLost(java.awt.event.FocusEvent) --> MeshSpecificationPanel.updateSize()V)
 * @param arg1 java.awt.event.FocusEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4(java.awt.event.FocusEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.updateSize();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * Return the GeometrySizeLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getGeometrySizeLabel() {
	if (ivjGeometrySizeLabel == null) {
		try {
			ivjGeometrySizeLabel = new javax.swing.JLabel();
			ivjGeometrySizeLabel.setName("GeometrySizeLabel");
			ivjGeometrySizeLabel.setText("Geometry Size (�m)");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjGeometrySizeLabel;
}

/**
 * Return the GeometrySizeLabel property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getGeometrySizeTextField() {
	if (ivjGeometrySizeTextField == null) {
		try {
			ivjGeometrySizeTextField = new javax.swing.JTextField();
			ivjGeometrySizeTextField.setName("GeometrySizeTextField");
			ivjGeometrySizeTextField.setForeground(java.awt.Color.blue);
			ivjGeometrySizeTextField.setEditable(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjGeometrySizeTextField;
}

/**
 * Return the JLabelTitle property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabelTitle() {
	if (ivjJLabelTitle == null) {
		try {
			ivjJLabelTitle = new javax.swing.JLabel();
			ivjJLabelTitle.setName("JLabelTitle");
			ivjJLabelTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
			ivjJLabelTitle.setText("Specify mesh size:");
			ivjJLabelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			ivjJLabelTitle.setFont(ivjJLabelTitle.getFont().deriveFont(java.awt.Font.BOLD));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabelTitle;
}

/**
 * Return the MeshSizeLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getMeshSizeLabel() {
	if (ivjMeshSizeLabel == null) {
		try {
			ivjMeshSizeLabel = new javax.swing.JLabel();
			ivjMeshSizeLabel.setName("MeshSizeLabel");
			ivjMeshSizeLabel.setText("Mesh Size (elements)");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMeshSizeLabel;
}

/**
 * Gets the meshSpecification property (cbit.vcell.mesh.MeshSpecification) value.
 * @return The meshSpecification property value.
 * @see #setMeshSpecification
 */
private MeshSpecification getMeshSpecification() {
	return fieldMeshSpecification;
}


/**
 * Return the XLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getXLabel() {
	if (ivjXLabel == null) {
		try {
			ivjXLabel = new javax.swing.JLabel();
			ivjXLabel.setName("XLabel");
			ivjXLabel.setText("X");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjXLabel;
}

/**
 * Return the JTextField1 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getXTextField() {
	if (ivjXTextField == null) {
		try {
			ivjXTextField = new javax.swing.JTextField();
			ivjXTextField.setName("XTextField");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjXTextField;
}

/**
 * Return the YLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getYLabel() {
	if (ivjYLabel == null) {
		try {
			ivjYLabel = new javax.swing.JLabel();
			ivjYLabel.setName("YLabel");
			ivjYLabel.setText("Y");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjYLabel;
}

/**
 * Return the JTextField2 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getYTextField() {
	if (ivjYTextField == null) {
		try {
			ivjYTextField = new javax.swing.JTextField();
			ivjYTextField.setName("YTextField");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjYTextField;
}
private javax.swing.JCheckBox getAutoMeshSizeCheckBox() {
	if (autoMeshSizeCheckBox == null) {
		try {
			autoMeshSizeCheckBox = new javax.swing.JCheckBox("Lock aspect ratio");
			autoMeshSizeCheckBox.setSelected(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return autoMeshSizeCheckBox;
}

/**
 * Return the ZLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getZLabel() {
	if (ivjZLabel == null) {
		try {
			ivjZLabel = new javax.swing.JLabel();
			ivjZLabel.setName("ZLabel");
			ivjZLabel.setText("Z");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjZLabel;
}

/**
 * Return the JTextField3 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getZTextField() {
	if (ivjZTextField == null) {
		try {
			ivjZTextField = new javax.swing.JTextField();
			ivjZTextField.setName("ZTextField");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjZTextField;
}

/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	exception.printStackTrace(System.out);
}


/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
private void initConnections() {
	// user code begin {1}
	// user code end
	getXTextField().addFocusListener(ivjEventHandler);
	getYTextField().addFocusListener(ivjEventHandler);
	getZTextField().addFocusListener(ivjEventHandler);
	getXTextField().getDocument().addDocumentListener(ivjEventHandler);
	getYTextField().getDocument().addDocumentListener(ivjEventHandler);
	getZTextField().getDocument().addDocumentListener(ivjEventHandler);
	getAutoMeshSizeCheckBox().addItemListener(ivjEventHandler);
	
	InputVerifier iv = new InputVerifier() {
		
		@Override
		public boolean verify(JComponent input) {
			return false;
		}

		@Override
		public boolean shouldYieldFocus(final JComponent input) {
			boolean bValid = true;
			JTextField jtf = (JTextField)input;
			try {
				Integer.parseInt(jtf.getText());
			} catch (NumberFormatException ex) {
				DialogUtils.showErrorDialog(MeshSpecificationPanel.this, "Wrong number format " + ex.getMessage().toLowerCase());
				bValid = false;
			}
			if (bValid) {
				input.setBorder(UIManager.getBorder("TextField.border"));
			} else {
				input.setBorder(GuiConstants.ProblematicTextFieldBorder);
				SwingUtilities.invokeLater(new Runnable() { 
				    public void run() { 
				    	input.requestFocusInWindow();
				    }
				});
			}
			return bValid;
		}		
	};
	getXTextField().setInputVerifier(iv);
	getYTextField().setInputVerifier(iv);
	getZTextField().setInputVerifier(iv);
}

/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("MeshSpecificationPanel");
		setLayout(new java.awt.GridBagLayout());
		setSize(324, 173);
		setEnabled(false);
		
		totalSizeTextField.setEditable(false);
		ivjDxTextField.setEditable(false);
		ivjDyTextField.setEditable(false);
		ivjDzTextField.setEditable(false);

		// 0
		int gridy = 0;
		java.awt.GridBagConstraints constraintsJLabelTitle = new java.awt.GridBagConstraints();
		constraintsJLabelTitle.gridx = 0; constraintsJLabelTitle.gridy = gridy;
		constraintsJLabelTitle.gridwidth = 4;
		constraintsJLabelTitle.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsJLabelTitle.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getJLabelTitle(), constraintsJLabelTitle);

		//
		gridy ++;
		java.awt.GridBagConstraints constraintsGeometrySizeLabel = new java.awt.GridBagConstraints();
		constraintsGeometrySizeLabel.gridx = 0; constraintsGeometrySizeLabel.gridy = gridy;
		constraintsGeometrySizeLabel.anchor = java.awt.GridBagConstraints.LINE_END;
		constraintsGeometrySizeLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getGeometrySizeLabel(), constraintsGeometrySizeLabel);

		java.awt.GridBagConstraints constraintsGeometrySizeTextField = new java.awt.GridBagConstraints();
		constraintsGeometrySizeTextField.gridx = 2; constraintsGeometrySizeTextField.gridy = gridy;
		constraintsGeometrySizeTextField.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsGeometrySizeTextField.weightx = 1.0;
		constraintsGeometrySizeTextField.gridwidth = 2;
		constraintsGeometrySizeTextField.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getGeometrySizeTextField(), constraintsGeometrySizeTextField);

		//
		gridy ++;
		java.awt.GridBagConstraints constraintsMeshSizeLabel = new java.awt.GridBagConstraints();
		constraintsMeshSizeLabel.gridx = 0; constraintsMeshSizeLabel.gridy = gridy;
		constraintsMeshSizeLabel.anchor = java.awt.GridBagConstraints.LINE_END;
		constraintsMeshSizeLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getMeshSizeLabel(), constraintsMeshSizeLabel);
		
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 2; gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(4, 0, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		add(getAutoMeshSizeCheckBox(), gbc);
		
		//
		gridy ++;
		java.awt.GridBagConstraints constraintsXLabel = new java.awt.GridBagConstraints();
		constraintsXLabel.gridx = 1; constraintsXLabel.gridy = gridy;
		constraintsXLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getXLabel(), constraintsXLabel);

		java.awt.GridBagConstraints constraintsXTextField = new java.awt.GridBagConstraints();
		constraintsXTextField.gridx = 2; constraintsXTextField.gridy = gridy;
		constraintsXTextField.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsXTextField.weightx = 1.0;
		constraintsXTextField.insets = new java.awt.Insets(4, 4, 4, 4);
		constraintsXTextField.gridwidth = 2;
		add(getXTextField(), constraintsXTextField);
		
		// 
		gridy ++;
		java.awt.GridBagConstraints constraintsYLabel = new java.awt.GridBagConstraints();
		constraintsYLabel.gridx = 1; constraintsYLabel.gridy = gridy;
		constraintsYLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getYLabel(), constraintsYLabel);

		java.awt.GridBagConstraints constraintsYTextField = new java.awt.GridBagConstraints();
		constraintsYTextField.gridx = 2; constraintsYTextField.gridy = gridy;
		constraintsYTextField.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsYTextField.weightx = 1.0;
		constraintsYTextField.gridwidth = 2;
		constraintsYTextField.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getYTextField(), constraintsYTextField);

		//
		gridy ++;
		java.awt.GridBagConstraints constraintsZLabel = new java.awt.GridBagConstraints();
		constraintsZLabel.gridx = 1; constraintsZLabel.gridy = gridy;
		constraintsZLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getZLabel(), constraintsZLabel);

		java.awt.GridBagConstraints constraintsZTextField = new java.awt.GridBagConstraints();
		constraintsZTextField.gridx = 2; constraintsZTextField.gridy = gridy;
		constraintsZTextField.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsZTextField.weightx = 1.0;
		constraintsZTextField.gridwidth = 2;
		constraintsZTextField.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getZTextField(), constraintsZTextField);
		
		//
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = gridy;
		gbc.anchor = java.awt.GridBagConstraints.LINE_END;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(new JLabel("Total Size (elements)"), gbc);

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		totalSizeTextField.setForeground(Color.blue);
		add(totalSizeTextField, gbc);
		
		//
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.anchor = java.awt.GridBagConstraints.LINE_END;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(new JLabel("Spatial Step (�m)"), gbc);

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.anchor = java.awt.GridBagConstraints.LINE_END;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(new JLabel("\u0394x"), gbc);

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 2; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		ivjDxTextField.setForeground(Color.blue);
		add(ivjDxTextField, gbc);
		
		//
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.anchor = java.awt.GridBagConstraints.LINE_END;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(ivjDyLabel, gbc);

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 2; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		ivjDyTextField.setForeground(Color.blue);
		add(ivjDyTextField, gbc);
		
		//
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.anchor = java.awt.GridBagConstraints.LINE_END;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);		
		add(ivjDzLabel, gbc);

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 2; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		ivjDzTextField.setForeground(Color.blue);
		add(ivjDzTextField, gbc);
		
		initConnections();		
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
		MeshSpecificationPanel aMeshSpecificationPanel = new MeshSpecificationPanel();
		frame.setContentPane(aMeshSpecificationPanel);
		frame.setSize(aMeshSpecificationPanel.getSize());
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

/**
 * Sets the meshSpecification property (cbit.vcell.mesh.MeshSpecification) value.
 * @param meshSpecification The new value for the property.
 * @see #getMeshSpecification
 */
public void setMeshSpecification(MeshSpecification meshSpecification) {
	MeshSpecification oldValue = fieldMeshSpecification;
	fieldMeshSpecification = meshSpecification;
	firePropertyChange("meshSpecification", oldValue, meshSpecification);
}


/**
 * Comment
 */
private void updateDisplay() {
	if (getMeshSpecification() == null) {
		return;
	}
		
	if (getMeshSpecification().getGeometry() == null || getMeshSpecification().getGeometry().getExtent() == null) {
		return;
	}
	Extent extent = getMeshSpecification().getGeometry().getExtent();
	ISize samplingSize = getMeshSpecification().getSamplingSize();
	if (samplingSize == null) {
		return;
	}
	bInProgress = true;
	int dim = getMeshSpecification().getGeometry().getDimension();
	long numX = samplingSize.getX();
	long numY = samplingSize.getY();
	switch (dim) {
	case 0:
		setVisible(false);
		break;
	case 1:
		getAutoMeshSizeCheckBox().setEnabled(false);
		getGeometrySizeTextField().setText(""+extent.getX());
		getXTextField().setText(String.valueOf(numX));
		
		getYLabel().setVisible(false);
		getYTextField().setVisible(false);
		ivjDyLabel.setVisible(false);
		ivjDyTextField.setVisible(false);
		
		getZLabel().setVisible(false);
		getZTextField().setVisible(false);
		ivjDzLabel.setVisible(false);
		ivjDzTextField.setVisible(false);
		break;
	case 2:
		getGeometrySizeTextField().setText("("+extent.getX()+", "+extent.getY()+")");
		getXTextField().setText(String.valueOf(numX));
		getYTextField().setText(String.valueOf(numY));
		
		getZLabel().setVisible(false);
		getZTextField().setVisible(false);
		ivjDzLabel.setVisible(false);
		ivjDzTextField.setVisible(false);		
		break;
	case 3:
		getGeometrySizeTextField().setText("("+extent.getX()+", "+extent.getY()+", "+extent.getZ()+")");
		getXTextField().setText(String.valueOf(numX));
		getYTextField().setText(String.valueOf(numY));
		long numZ = samplingSize.getZ();
		getZTextField().setText(String.valueOf(numZ));
		break;
	}
	updateTotalSizeAndSpatialStep();
	
	if (getMeshSpecification().isAspectRatioOK()) {
		getAutoMeshSizeCheckBox().setSelected(true);
	}
	bInProgress = false;
}


/**
 * Comment
 */
private void updateSize() {
	String error = null;
	String sx = getXTextField().getText();
	String sy = getYTextField().getText();
	String sz = getZTextField().getText();
	sx = (sx == null || sx.equals("")) ? "1" : sx;
	sy = (sy == null || sy.equals("")) ? "1" : sy;
	sz = (sz == null || sz.equals("")) ? "1" : sz;
	try {
		ISize iSize = new ISize(sx, sy, sz);
		getMeshSpecification().setSamplingSize(iSize);
//		updateDisplay();
		return;
	} catch (NumberFormatException nexc) {
		error = "NumberFormatException " + nexc.getMessage();
	} catch (java.beans.PropertyVetoException pexc) {
		error = pexc.getMessage();
	}
	DialogUtils.showErrorDialog(this, "Error setting mesh size : " + error);
}

public void autoUpdateSizes(ItemEvent e) {
	if (bInProgress) {
		return;
	}
	int dimension = getMeshSpecification().getGeometry().getDimension();
	if (dimension < 2) {
		return;
	}
	try {
		bInProgress = true;
		String xtext = getXTextField().getText();
		if (xtext == null || xtext.trim().length() == 0) {
			getXTextField().setText(getMeshSpecification().getSamplingSize().getX() + "");
		}
		xtext = getXTextField().getText();
		int numX = Integer.parseInt(xtext);
		Extent extent = getMeshSpecification().getGeometry().getExtent();
		switch (dimension){
			case 2:{
				double yxRatio = extent.getY()/extent.getX();
				long numY = Math.max(3, Math.round(yxRatio * (numX - 1) + 1));
				getYTextField().setText("" + Math.round(numY));
				break;
			}
			case 3:{
				double yxRatio = extent.getY()/extent.getX();
				double zxRatio = extent.getZ()/extent.getX();
				long numY = Math.max(3, Math.round(yxRatio * (numX - 1) + 1));
				long numZ = Math.max(3, Math.round(zxRatio * (numX - 1) + 1));
				getYTextField().setText("" + numY);
				getZTextField().setText("" + numZ);
				break;
			}
		}
		updateSize();
		updateTotalSizeAndSpatialStep();
	} finally {
		bInProgress = false;
	}
}

private void autoUpdateSizes(DocumentEvent e) {
	if (bInProgress) {
		return;
	}
	final int dimension = getMeshSpecification().getGeometry().getDimension();
	if (!getAutoMeshSizeCheckBox().isSelected()) {
		updateTotalSizeAndSpatialStep();
		return;
	}

	JTextField input = null;
	try {
		bInProgress = true;
		Extent extent = getMeshSpecification().getGeometry().getExtent();
		if (e.getDocument() == getXTextField().getDocument()) {
			input = getXTextField();
			String xtext = getXTextField().getText();
			if (xtext == null || xtext.trim().length() == 0) {
				getYTextField().setText(xtext);
				getZTextField().setText(xtext);
				clearTotalSizeAndSpatialStep();	
				return;
			}
			int numX = Integer.parseInt(xtext);
			switch (dimension){
			case 1: {
				break;
			}
			case 2:{
				double yxRatio = extent.getY()/extent.getX();
				long numY = Math.max(3, Math.round(yxRatio * (numX - 1) + 1));
				getYTextField().setText("" + numY);
				break;
			}
			case 3:{
				double yxRatio = extent.getY()/extent.getX();
				double zxRatio = extent.getZ()/extent.getX();
				long numY = Math.max(3, Math.round(yxRatio * (numX - 1) + 1));
				long numZ = Math.max(3, Math.round(zxRatio * (numX - 1) + 1));
				getYTextField().setText("" + numY);
				getZTextField().setText("" + numZ);
				break;
			}
			}
		} else if (e.getDocument() == getYTextField().getDocument()) {
			input = getYTextField();
			String ytext = getYTextField().getText();
			if (ytext == null || ytext.trim().length() == 0) {
				getXTextField().setText(ytext);
				getZTextField().setText(ytext);
				clearTotalSizeAndSpatialStep();
				return;
			}
			int numY = Integer.parseInt(ytext);
			switch (dimension){		
				case 2:{
					double xyRatio = extent.getX()/extent.getY();
					long numX = Math.max(3, Math.round(xyRatio * (numY - 1) + 1));
					getXTextField().setText("" + numX);
					break;
				}
				case 3:{
					double xyRatio = extent.getX()/extent.getY();
					double zyRatio = extent.getZ()/extent.getY();
					long numX = Math.max(3, Math.round(xyRatio * (numY - 1) + 1));
					long numZ = Math.max(3, Math.round(zyRatio * (numY - 1) + 1));
					getXTextField().setText("" + numX);
					getZTextField().setText("" + numZ);
					break;
				}
			}
		} else if (e.getDocument() == getZTextField().getDocument()) {
			input = getZTextField();
			String ztext = getZTextField().getText();
			if (ztext == null || ztext.trim().length() == 0) {
				getXTextField().setText(ztext);
				getYTextField().setText(ztext);
				clearTotalSizeAndSpatialStep();
				return;
			}
			int numZ = Integer.parseInt(ztext);
			switch (dimension){		
				case 3:{
					double xzRatio = extent.getX()/extent.getZ();
					double yzRatio = extent.getY()/extent.getZ();
					long numX = Math.max(3, Math.round(xzRatio * (numZ - 1) + 1));
					long numY = Math.max(3, Math.round(yzRatio * (numZ - 1) + 1));
					getXTextField().setText("" + numX);
					getYTextField().setText("" + numY);
					break;
				}
			}
		}
		input.setBorder(UIManager.getBorder("TextField.border"));
		updateTotalSizeAndSpatialStep();
	} catch (NumberFormatException ex) {
		DialogUtils.showErrorDialog(this, "Wrong number format " + ex.getMessage().toLowerCase());
		input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.red), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		clearTotalSizeAndSpatialStep();
	} finally {
		bInProgress = false;
	}
}

private void updateTotalSizeAndSpatialStep() {
	if (getMeshSpecification() == null) {
		return;
	}
		
	if (getMeshSpecification().getGeometry() == null || getMeshSpecification().getGeometry().getExtent() == null) {
		return;
	}
	ISize samplingSize = getMeshSpecification().getSamplingSize();
	if (samplingSize == null) {
		return;
	}
	Extent extent = getMeshSpecification().getGeometry().getExtent();
	int dim = getMeshSpecification().getGeometry().getDimension();
	try {
		String xtext = getXTextField().getText();
		int numX = Integer.parseInt(xtext);	
		int numY = 1;
		int numZ = 1;
		String totalSizeText = "";
		long totalSizeValue = numX;
		if (dim > 1) {
			String ytext = getYTextField().getText();
			numY = Integer.parseInt(ytext);
			totalSizeText += numX + " x " + numY;
			totalSizeValue *= numY;
			if (dim > 2) {
				String ztext = getZTextField().getText();
				numZ = Integer.parseInt(ztext);
				totalSizeText += " x " + numZ;
				totalSizeValue *= numZ;
			}
		}
		totalSizeTextField.setText(totalSizeText + (totalSizeText.length() == 0 ? "" : " = ") + totalSizeValue);
		ivjDxTextField.setText(extent.getX()/(numX - 1) + "");
		ivjDyTextField.setText(extent.getY()/(numY - 1) + "");
		ivjDzTextField.setText(extent.getZ()/(numZ - 1) + "");
	} catch (NumberFormatException ex) {
		clearTotalSizeAndSpatialStep();
	}
}

private void clearTotalSizeAndSpatialStep() {
	totalSizeTextField.setText(null);
	ivjDxTextField.setText(null);
	ivjDyTextField.setText(null);
	ivjDzTextField.setText(null);	
}

}
