package cbit.vcell.solver.ode.gui;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vcell.util.Compare;
import org.vcell.util.Extent;
import org.vcell.util.ISize;
import org.vcell.util.gui.CollapsiblePanel;
import org.vcell.util.gui.VCellIcons;

import cbit.vcell.client.desktop.biomodel.DocumentEditorSubPanel;
import cbit.vcell.client.desktop.simulation.SimulationWorkspace;
import cbit.vcell.math.Constant;
import cbit.vcell.solver.DefaultOutputTimeSpec;
import cbit.vcell.solver.ErrorTolerance;
import cbit.vcell.solver.MeshSpecification;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SolverDescription;
import cbit.vcell.solver.SolverTaskDescription;
import cbit.vcell.solver.TimeStep;
import cbit.vcell.solver.stoch.StochSimOptions;
/**
 * Insert the type's description here.
 * Creation date: (5/2/2001 12:17:49 PM)
 * @author: Ion Moraru
 */
@SuppressWarnings("serial")
public class SimulationSummaryPanel extends DocumentEditorSubPanel {
	private Simulation fieldSimulation = null;
	private IvjEventHandler ivjEventHandler = new IvjEventHandler();
//	private JLabel labelSimKey = null;
	private JLabel ivjJLabel11 = null;
	private JLabel ivjJLabel12 = null;
	private JLabel ivjJLabel8 = null;
	private MathOverridesPanel ivjMathOverridesPanel1 = null;
	private JLabel ivjJLabelGeometrySize = null;
	private JLabel ivjJLabelMesh = null;
	private JLabel ivjJLabelTimestep = null;
	private JTextArea ivjJTextAreaDescription = null;
	private JLabel ivjJLabel10 = null;
	private JLabel ivjJLabelSensitivity = null;
	private JLabel ivjJLabelOutput = null;
	private JLabel labelRelTol = null;
	private JLabel labelAbsTol = null;
	private JLabel labelRelTolValue = null;
	private JLabel labelAbsTolValue = null;
	private JPanel settingsPanel;

	private class IvjEventHandler implements java.beans.PropertyChangeListener, FocusListener {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (fieldSimulation == null) {
				return;
			}
			if (event.getSource() == fieldSimulation) {
				// name is not displayed by this panel so we only need to take care of the rest
				if (event.getPropertyName().equals("description")) {
					displayAnnotation();
				}
				if (event.getPropertyName().equals("solverTaskDescription")) {
					displayTask();
				}
				if (event.getPropertyName().equals("meshSpecification")) {
					displayMesh();
				}
				if (event.getPropertyName().equals("mathOverrides")) {
					displayOverrides();
				}
				// lots can happen here, so just do it all
				if (event.getPropertyName().equals("mathDescription")) {
					refreshDisplay();
				}
			} else if(event.getSource() == fieldSimulation.getMeshSpecification()){
				if(event.getPropertyName().equals("geometry")){
					displayMesh();
				}
			} else if(event.getSource() == fieldSimulation.getSolverTaskDescription()){
				refreshDisplay();				
			}
		};
		public void focusGained(FocusEvent e) {			
		}
		public void focusLost(FocusEvent e) {
			updateAnnotation();	
		}
	};

/**
 * SimulationSummaryPanel constructor comment.
 */
public SimulationSummaryPanel() {
	super();
	initialize();
}


/**
 * Comment
 */
private void displayAnnotation() {
	if(Compare.isEqualOrNull(getJTextAreaDescription().getText(),getSimulation().getDescription())){
		return;
	}
	try {
		getJTextAreaDescription().setText(getSimulation().getDescription());
		getJTextAreaDescription().setCaretPosition(0);
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		getJTextAreaDescription().setText("");
	}
}


/**
 * Comment
 */
private void displayMesh() {
    try {
    	boolean isSpatial = getSimulation().isSpatial();
    	getJLabel11().setVisible(isSpatial);
    	getJLabelMesh().setVisible(isSpatial);
        if (getSimulation()!=null && getSimulation().getMeshSpecification() != null) {
            ISize samplingSize = getSimulation().getMeshSpecification().getSamplingSize();
            String labelText = "";
            switch (getSimulation().getMathDescription().getGeometry().getDimension()) {
                case 0 :
                    {
                        labelText = "error: no mesh expected";
                        break;
                    }
                case 1 :
                    {
                        labelText = samplingSize.getX() + " elements";
                        break;
                    }
                case 2 :
                    {
                        // 06/12/2002 JMW Replaced this line...
                        //labelText = "("+samplingSize.getX()+","+samplingSize.getY()+") elements";
                        labelText = samplingSize.getX() + "x" + samplingSize.getY() + " = " +
							samplingSize.getX() * samplingSize.getY() + " elements";
                        break;
                    }
                case 3 :
                    {
                        // 06/12/2002 JMW Replaced this line...
                        //labelText = "("+samplingSize.getX()+","+samplingSize.getY()+","+samplingSize.getZ()+") elements";
                        labelText = samplingSize.getX() + "x" + samplingSize.getY() + "x" + samplingSize.getZ() + " = " +
							samplingSize.getX() * samplingSize.getY() * samplingSize.getZ() + " elements";
                        break;
                    }
            }
            getJLabelMesh().setText(labelText);
        }
    } catch (Exception exc) {
        exc.printStackTrace(System.out);
        getJLabelMesh().setText("");
    }
}


/**
 * Comment
 */
private void displayOther() {
	boolean isSpatial = getSimulation().isSpatial();

	try {
		getJLabel8().setVisible(isSpatial);
		getJLabelGeometrySize().setVisible(isSpatial);
		Extent extent = getSimulation().getMathDescription().getGeometry().getExtent();
		String labelText = "";
		switch (getSimulation().getMathDescription().getGeometry().getDimension()) {
			case 0: {
				break;
			}
			case 1: {
				labelText = extent.getX()+" microns";
				break;
			}
			case 2: {
				labelText = "("+extent.getX()+","+extent.getY()+") microns";
				break;
			}
			case 3: {
				labelText = "("+extent.getX()+","+extent.getY()+","+extent.getZ()+") microns";
				break;
			}
		}
		getJLabelGeometrySize().setText(labelText);
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		getJLabelGeometrySize().setText("");
	}
}


/**
 * Comment
 */
private void displayOverrides() {
	try {
		getMathOverridesPanel1().setMathOverrides(getSimulation().getMathOverrides());
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		getMathOverridesPanel1().setMathOverrides(null);
	}
}


/**
 * Comment
 */
private void displayTask() {
	SolverTaskDescription solverTaskDescription = getSimulation().getSolverTaskDescription();
	try {
		StochSimOptions stochOpt = solverTaskDescription.getStochOpt();
		if(stochOpt != null && stochOpt.getNumOfTrials() > 1 )
		{
			getJLabelOutput().setText("Histogram with "+stochOpt.getNumOfTrials()+" Trials(@last time point)");
		}
		else
		{
			String text = solverTaskDescription.getOutputTimeSpec().getShortDescription();
			if (solverTaskDescription.getOutputTimeSpec().isDefault() && !solverTaskDescription.getSolverDescription().isSemiImplicitPdeSolver() 
					&& !solverTaskDescription.getSolverDescription().equals(SolverDescription.StochGibson)) {
				text += ", at most " + ((DefaultOutputTimeSpec)solverTaskDescription.getOutputTimeSpec()).getKeepAtMost();
			}
			getJLabelOutput().setText(text);
		}
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		getJLabelOutput().setText("");
	}
	SolverDescription solverDescription = solverTaskDescription.getSolverDescription();
	try {
		ErrorTolerance errorTolerance = solverTaskDescription.getErrorTolerance();
		TimeStep timeStep = solverTaskDescription.getTimeStep();
		if (solverDescription.equals(SolverDescription.StochGibson)) {
			getJLabel12().setEnabled(false);
			getJLabelTimestep().setText("");
		} else if (solverDescription.isNonSpatialStochasticSolver()) {
			getJLabel12().setEnabled(true);
			getJLabel12().setText("timestep");		
			getJLabelTimestep().setText(timeStep.getDefaultTimeStep()+ "s");
		} else if (solverDescription.hasVariableTimestep()) {
			getJLabel12().setEnabled(true);
			getJLabel12().setText("max timestep");
			getJLabelTimestep().setText(timeStep.getMaximumTimeStep()+ "s");
			getJLabelRelTol().setEnabled(true);
			getJLabelRelTolValue().setText("" + errorTolerance.getRelativeErrorTolerance());
			getJLabelAbsTol().setEnabled(true);
			getJLabelAbsTolValue().setText("" + errorTolerance.getAbsoluteErrorTolerance());
		} else {
			getJLabel12().setEnabled(true);
			getJLabel12().setText("timestep");
			getJLabelTimestep().setText(timeStep.getDefaultTimeStep() + "s");
			if (solverDescription.isSemiImplicitPdeSolver()) {
				getJLabelRelTol().setEnabled(true);
				getJLabelRelTolValue().setText("" + errorTolerance.getRelativeErrorTolerance());
			} else {
				getJLabelRelTol().setEnabled(false);
				getJLabelRelTolValue().setText("");
			}
			getJLabelAbsTol().setEnabled(false);
			getJLabelAbsTolValue().setText("");			
		}			
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		getJLabelTimestep().setText("");
		getJLabelRelTolValue().setText("");
		getJLabelAbsTolValue().setText("");
	}
	try {
		if (getSimulation().isSpatial() || solverDescription.isNonSpatialStochasticSolver()) {
			getJLabelSensitivity().setVisible(false);
			getJLabel10().setVisible(false);
		} else {
			getJLabelSensitivity().setVisible(true);
			getJLabel10().setVisible(true);
			Constant param = solverTaskDescription.getSensitivityParameter();
			if (param == null) {
				getJLabelSensitivity().setText("no");
			} else {
				getJLabelSensitivity().setText(param.getName());
			}
		}
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		getJLabelSensitivity().setText("");
	}
	if (solverDescription.isNonSpatialStochasticSolver() || solverDescription.isSpatialStochasticSolver()) {
		getJLabelRelTol().setVisible(false);
		getJLabelAbsTol().setVisible(false);
		getJLabelRelTolValue().setText("");
		getJLabelAbsTolValue().setText("");
	} else {
		getJLabelRelTol().setVisible(true);
		getJLabelAbsTol().setVisible(true);
	}
}


/**
 * Return the JLabel1 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
//private javax.swing.JLabel getJLabelSimKey() {
//	if (labelSimKey == null) {
//		try {
//			labelSimKey = new javax.swing.JLabel();
//			labelSimKey.setName("JLabelSimKey");
//			labelSimKey.addMouseListener(
//					new MouseAdapter(){				
//						JPopupMenu jPopup;
//						ActionListener copyAction =
//							new ActionListener(){
//								public void actionPerformed(ActionEvent e) {
//									if(getSimulation() != null && getSimulation().getKey() != null){
//										VCellTransferable.sendToClipboard(getSimulation().getKey().toString());
//									}
//								}
//							};
//						public void mouseClicked(MouseEvent e) {
//							super.mouseClicked(e);
//							checkMenu(e);
//						}
//						public void mousePressed(MouseEvent e) {
//							super.mousePressed(e);
//							checkMenu(e);
//						}
//						public void mouseReleased(MouseEvent e) {
//							super.mouseReleased(e);
//							checkMenu(e);
//						}
//						private void checkMenu(MouseEvent e){
//							if(getSimulation() != null && e.isPopupTrigger()){
//								if(jPopup == null){
//									jPopup = new JPopupMenu();
//									JMenuItem jMenu = new JMenuItem("Copy SimID");
//									jMenu.addActionListener(copyAction);
//									jPopup.add(jMenu);
//								}
//								jPopup.show(e.getComponent(), e.getX(), e.getY());
//							}
//						}
//					}
//			);
//		} catch (java.lang.Throwable ivjExc) {
//			handleException(ivjExc);
//		}
//	}
//	return labelSimKey;
//}


/**
 * Return the JLabel10 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel10() {
	if (ivjJLabel10 == null) {
		try {
			ivjJLabel10 = new javax.swing.JLabel("Sensitivity Analysis");
			ivjJLabel10.setName("JLabel10");
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabel10;
}


private javax.swing.JLabel getJLabelRelTol() {
	if (labelRelTol == null) {
		try {
			labelRelTol = new javax.swing.JLabel("rel tol");
			labelRelTol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return labelRelTol;
}

private javax.swing.JLabel getJLabelAbsTol() {
	if (labelAbsTol == null) {
		try {
			labelAbsTol = new javax.swing.JLabel("abs tol");
			labelAbsTol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return labelAbsTol;
}

/**
 * Return the JLabel11 property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabel11() {
	if (ivjJLabel11 == null) {
		try {
			ivjJLabel11 = new javax.swing.JLabel("Mesh:");
			ivjJLabel11.setName("JLabel11");
			ivjJLabel11.setVisible(false);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabel11;
}


/**
 * Return the JLabel12 property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabel12() {
	if (ivjJLabel12 == null) {
		try {
			ivjJLabel12 = new javax.swing.JLabel("timestep");
			ivjJLabel12.setName("JLabel12");
			ivjJLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabel12;
}

/**
 * Return the JLabel8 property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabel8() {
	if (ivjJLabel8 == null) {
		try {
			ivjJLabel8 = new javax.swing.JLabel("Geometry size:");
			ivjJLabel8.setName("JLabel8");
			ivjJLabel8.setVisible(false);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabel8;
}

private javax.swing.JLabel getJLabelRelTolValue() {
	if (labelRelTolValue == null) {
		try {
			labelRelTolValue = new javax.swing.JLabel();
			labelRelTolValue.setForeground(java.awt.Color.blue);
			labelRelTolValue.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			labelRelTolValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return labelRelTolValue;
}

private javax.swing.JLabel getJLabelAbsTolValue() {
	if (labelAbsTolValue == null) {
		try {
			labelAbsTolValue = new javax.swing.JLabel();
			labelAbsTolValue.setForeground(java.awt.Color.blue);
			labelAbsTolValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return labelAbsTolValue;
}
/**
 * Return the JLabelGeometrySize property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabelGeometrySize() {
	if (ivjJLabelGeometrySize == null) {
		try {
			ivjJLabelGeometrySize = new javax.swing.JLabel();
			ivjJLabelGeometrySize.setName("JLabelGeometrySize");
			ivjJLabelGeometrySize.setForeground(java.awt.Color.blue);
			ivjJLabelGeometrySize.setVisible(false);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabelGeometrySize;
}


/**
 * Return the JLabelMesh property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabelMesh() {
	if (ivjJLabelMesh == null) {
		try {
			ivjJLabelMesh = new javax.swing.JLabel();
			ivjJLabelMesh.setName("JLabelMesh");
			ivjJLabelMesh.setForeground(java.awt.Color.blue);
			ivjJLabelMesh.setVisible(false);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabelMesh;
}


/**
 * Return the JLabelSaveEvery property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabelOutput() {
	if (ivjJLabelOutput == null) {
		try {
			ivjJLabelOutput = new javax.swing.JLabel();
			ivjJLabelOutput.setName("JLabelOutput");
			ivjJLabelOutput.setForeground(java.awt.Color.blue);
			ivjJLabelOutput.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabelOutput;
}

/**
 * Return the JLabelSensitivity property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabelSensitivity() {
	if (ivjJLabelSensitivity == null) {
		try {
			ivjJLabelSensitivity = new javax.swing.JLabel();
			ivjJLabelSensitivity.setName("JLabelSensitivity");
			ivjJLabelSensitivity.setForeground(java.awt.Color.blue);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabelSensitivity;
}

/**
 * Return the JLabel13 property value.
 * @return javax.swing.JLabel
 */
private javax.swing.JLabel getJLabelTimestep() {
	if (ivjJLabelTimestep == null) {
		try {
			ivjJLabelTimestep = new javax.swing.JLabel();
			ivjJLabelTimestep.setName("JLabelTimestep");
			ivjJLabelTimestep.setForeground(java.awt.Color.blue);
			ivjJLabelTimestep.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJLabelTimestep;
}

/**
 * Return the JTextArea1 property value.
 * @return javax.swing.JTextArea
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextArea getJTextAreaDescription() {
	if (ivjJTextAreaDescription == null) {
		try {
			ivjJTextAreaDescription = new javax.swing.JTextArea();
			ivjJTextAreaDescription.setName("JTextAreaDescription");
			ivjJTextAreaDescription.setForeground(java.awt.Color.blue);
			ivjJTextAreaDescription.setRows(3);
			ivjJTextAreaDescription.setLineWrap(true);
			ivjJTextAreaDescription.setWrapStyleWord(true);
			ivjJTextAreaDescription.setEditable(false);
			ivjJTextAreaDescription.setEnabled(true);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjJTextAreaDescription;
}


/**
 * Return the MathOverridesPanel1 property value.
 * @return cbit.vcell.solver.ode.gui.MathOverridesPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private MathOverridesPanel getMathOverridesPanel1() {
	if (ivjMathOverridesPanel1 == null) {
		try {
			ivjMathOverridesPanel1 = new MathOverridesPanel();
			ivjMathOverridesPanel1.setName("MathOverridesPanel1");
			ivjMathOverridesPanel1.setEditable(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMathOverridesPanel1;
}


/**
 * Gets the simulation property (cbit.vcell.solver.Simulation) value.
 * @return The simulation property value.
 * @see #setSimulation
 */
public Simulation getSimulation() {
	return fieldSimulation;
}


/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}

private JPanel getSettingsPanel() {
	if (settingsPanel == null) {
		settingsPanel = new JPanel(new GridBagLayout());
		
		int gridy = 0;		
		java.awt.GridBagConstraints constraintsJLabel12 = new java.awt.GridBagConstraints();
		constraintsJLabel12.gridx = 0; 
		constraintsJLabel12.gridy = gridy;
		constraintsJLabel12.insets = new java.awt.Insets(4, 4, 4, 4);
		settingsPanel.add(getJLabel12(), constraintsJLabel12); // timestep
		
		java.awt.GridBagConstraints constraintsJLabel13 = new java.awt.GridBagConstraints();
		constraintsJLabel13.gridx = 1; 
		constraintsJLabel13.gridy = gridy;
		constraintsJLabel13.insets = new java.awt.Insets(4, 4, 4, 4);
		settingsPanel.add(new JLabel("output", javax.swing.SwingConstants.CENTER), constraintsJLabel13); // output
		
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 2; 
		gbc.gridy = gridy;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		settingsPanel.add(getJLabelRelTol(), gbc); // rel tol

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 3; 
		gbc.gridy = gridy;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		settingsPanel.add(getJLabelAbsTol(), gbc); // abs tol	
		
		java.awt.GridBagConstraints constraintsJLabel10 = new java.awt.GridBagConstraints();
		constraintsJLabel10.gridx = 4; 
		constraintsJLabel10.gridy = gridy;
		constraintsJLabel10.insets = new java.awt.Insets(4, 4, 4, 4);
		settingsPanel.add(getJLabel10(), constraintsJLabel10);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 5; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		settingsPanel.add(new JLabel(), gbc); // abs tol	
		
		gridy ++;		
		java.awt.GridBagConstraints constraintsJLabelTimestep = new java.awt.GridBagConstraints();
		constraintsJLabelTimestep.gridx = 0; 
		constraintsJLabelTimestep.gridy = gridy;
		constraintsJLabelTimestep.insets = new java.awt.Insets(0, 4, 4, 4);
		settingsPanel.add(getJLabelTimestep(), constraintsJLabelTimestep);

		java.awt.GridBagConstraints constraintsJLabelOutput = new java.awt.GridBagConstraints();
		constraintsJLabelOutput.gridx = 1; 
		constraintsJLabelOutput.gridy = gridy;
		constraintsJLabelOutput.insets = new java.awt.Insets(0, 4, 4, 4);
		settingsPanel.add(getJLabelOutput(), constraintsJLabelOutput);

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 2; 
		gbc.gridy = gridy;
		gbc.insets = new java.awt.Insets(0, 4, 4, 4);
		settingsPanel.add(getJLabelRelTolValue(), gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 3; 
		gbc.gridy = gridy;
		gbc.insets = new java.awt.Insets(0, 4, 4, 4);
		settingsPanel.add(getJLabelAbsTolValue(), gbc);
		
		java.awt.GridBagConstraints constraintsJLabelSensitivity = new java.awt.GridBagConstraints();
		constraintsJLabelSensitivity.gridx = 4; 
		constraintsJLabelSensitivity.gridy = gridy;
		constraintsJLabelSensitivity.insets = new java.awt.Insets(0, 4, 4, 4);
		settingsPanel.add(getJLabelSensitivity(), constraintsJLabelSensitivity);

	}
	return settingsPanel;
}

/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		setName("SimulationSummaryPanel");
		setLayout(new java.awt.GridBagLayout());
		
		int gridy = 0;
		java.awt.GridBagConstraints constraintsJLabel2 = new java.awt.GridBagConstraints();
		constraintsJLabel2.gridx = 0; 
		constraintsJLabel2.gridy = gridy;
		constraintsJLabel2.anchor = java.awt.GridBagConstraints.LINE_END;
		constraintsJLabel2.insets = new java.awt.Insets(4, 4, 4, 4);
		add(new JLabel("Annotation:"), constraintsJLabel2);

		java.awt.GridBagConstraints constraintsJScrollPane1 = new java.awt.GridBagConstraints();
		constraintsJScrollPane1.gridx = 1; 
		constraintsJScrollPane1.gridy = gridy;
		constraintsJScrollPane1.gridwidth = GridBagConstraints.REMAINDER;
		constraintsJScrollPane1.fill = java.awt.GridBagConstraints.BOTH;
		constraintsJScrollPane1.weightx = 1.0;
		constraintsJScrollPane1.insets = new java.awt.Insets(4, 4, 4, 4);
		add(new JScrollPane(getJTextAreaDescription()), constraintsJScrollPane1);
				
		gridy ++;
		java.awt.GridBagConstraints constraintsJLabel3 = new java.awt.GridBagConstraints();
		constraintsJLabel3.gridx = 0; 
		constraintsJLabel3.gridy = gridy;
		constraintsJLabel3.anchor = java.awt.GridBagConstraints.EAST;
		constraintsJLabel3.insets = new java.awt.Insets(4, 4, 4, 4);
		add(new JLabel("Settings:"), constraintsJLabel3); // Time:

		java.awt.GridBagConstraints  gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getSettingsPanel(), gbc); 	
		
		gridy ++;
		java.awt.GridBagConstraints constraintsJLabel11 = new java.awt.GridBagConstraints();
		constraintsJLabel11.gridx = 0; 
		constraintsJLabel11.gridy = gridy;
		constraintsJLabel11.anchor = java.awt.GridBagConstraints.EAST;
		constraintsJLabel11.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getJLabel11(), constraintsJLabel11); // Mesh:

		java.awt.GridBagConstraints constraintsJLabelMesh = new java.awt.GridBagConstraints();
		constraintsJLabelMesh.gridx = 1; 
		constraintsJLabelMesh.gridy = gridy;
		constraintsJLabelMesh.weightx = 1.0;
		constraintsJLabelMesh.gridwidth = 2;
		constraintsJLabelMesh.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsJLabelMesh.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getJLabelMesh(), constraintsJLabelMesh);

		java.awt.GridBagConstraints constraintsJLabel8 = new java.awt.GridBagConstraints();
		constraintsJLabel8.gridx = 3; 
		constraintsJLabel8.gridy = gridy;
		constraintsJLabel8.anchor = java.awt.GridBagConstraints.EAST;
		constraintsJLabel8.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getJLabel8(), constraintsJLabel8); // Geometry Size

		java.awt.GridBagConstraints constraintsJLabelGeometrySize = new java.awt.GridBagConstraints();
		constraintsJLabelGeometrySize.gridx = 4; 
		constraintsJLabelGeometrySize.gridy = gridy;
		constraintsJLabelGeometrySize.weightx = 1.0;
		constraintsJLabelGeometrySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsJLabelGeometrySize.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getJLabelGeometrySize(), constraintsJLabelGeometrySize);

		gridy ++;
		java.awt.GridBagConstraints constraintsMathOverridesPanel1 = new java.awt.GridBagConstraints();
		constraintsMathOverridesPanel1.gridx = 0; 
		constraintsMathOverridesPanel1.gridy = gridy;
		constraintsMathOverridesPanel1.gridwidth = GridBagConstraints.REMAINDER;
		constraintsMathOverridesPanel1.fill = java.awt.GridBagConstraints.BOTH;
		constraintsMathOverridesPanel1.weightx = 1.0;
		constraintsMathOverridesPanel1.weighty = 1.0;
		constraintsMathOverridesPanel1.insets = new java.awt.Insets(4, 4, 4, 4);
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Parameters with values changed from defaults");
		collapsiblePanel.getContentPanel().setLayout(new BorderLayout());
		collapsiblePanel.getContentPanel().add(getMathOverridesPanel1(), BorderLayout.CENTER);
		add(collapsiblePanel, constraintsMathOverridesPanel1);

		getJTextAreaDescription().addFocusListener(ivjEventHandler);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
}

/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		JFrame frame = new javax.swing.JFrame();
		SimulationSummaryPanel aSimulationSummaryPanel;
		aSimulationSummaryPanel = new SimulationSummaryPanel();
		frame.setContentPane(aSimulationSummaryPanel);
		frame.setSize(aSimulationSummaryPanel.getSize());
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
 * Comment
 */
private void refreshDisplay() {
	if (getSimulation() == null){
		getJTextAreaDescription().setBackground(getBackground());
		getJTextAreaDescription().setEditable(false);
//		getJLabelSimKey().setText("");
		getJTextAreaDescription().setText("");
		getJLabelTimestep().setText("");
		getJLabelOutput().setText("");
		getJLabelSensitivity().setText("");
		getJLabelGeometrySize().setText("");
		getJLabelMesh().setText("");
		getJLabelRelTolValue().setText("");
		getJLabelAbsTolValue().setText("");
		getMathOverridesPanel1().setMathOverrides(null);
	} else {
		displayAnnotation();
		displayTask();
		displayMesh();
		displayOverrides();
		displayOther();
	
		getJTextAreaDescription().setBackground(java.awt.Color.white);
		getJTextAreaDescription().setEditable(true);
//		String key = "";
//		if (fieldSimulation.getKey() != null) {
//			key = "(SimID=" + fieldSimulation.getKey();
//			if (fieldSimulation.getSimulationVersion() != null && fieldSimulation.getSimulationVersion().getParentSimulationReference() != null) {
//				key += ", parentSimRef="+fieldSimulation.getSimulationVersion().getParentSimulationReference();
//			}
//			key += ")";
//		}
//		getJLabelSimKey().setText(key);
	}
}


/**
 * Sets the simulation property (cbit.vcell.solver.Simulation) value.
 * @param newValue The new value for the property.
 * @see #getSimulation
 */
public void setSimulation(Simulation newValue) {
	Simulation oldValue = fieldSimulation;
	if (oldValue != null) {
		oldValue.removePropertyChangeListener(ivjEventHandler);
		oldValue.getSolverTaskDescription().removePropertyChangeListener(ivjEventHandler);
		MeshSpecification meshSpecification = oldValue.getMeshSpecification();
		if (meshSpecification != null) {
			meshSpecification.removePropertyChangeListener(ivjEventHandler);
		}
	}
	fieldSimulation = newValue;	
	if (newValue != null) {
		// also set up a listener that will refresh when simulation is edited in place
		newValue.addPropertyChangeListener(ivjEventHandler);
		newValue.getSolverTaskDescription().addPropertyChangeListener(ivjEventHandler);
		MeshSpecification meshSpecification = newValue.getMeshSpecification();
		if (meshSpecification != null) {
			meshSpecification.addPropertyChangeListener(ivjEventHandler);
		}
	}
	
	refreshDisplay();
}


/**
 * Comment
 */
private void updateAnnotation() {
	try {
		//int caretPosition = getJTextAreaDescription().getCaretPosition();
		String text = getJTextAreaDescription().getText();
		if(getSimulation() != null){
			getSimulation().setDescription(text);
		}
		//getJTextAreaDescription().setCaretPosition(caretPosition);
	}catch (PropertyVetoException e){
		e.printStackTrace(System.out);
		getJTextAreaDescription().setText(getSimulation().getDescription());
	}
}

@Override
protected void onSelectedObjectsChange(Object[] selectedObjects) {
	Simulation selectedSimulation = null;
	if (selectedObjects != null && selectedObjects.length == 1 && selectedObjects[0] instanceof Simulation) {
		selectedSimulation = (Simulation) selectedObjects[0];
	}
	setSimulation(selectedSimulation);	
}
}