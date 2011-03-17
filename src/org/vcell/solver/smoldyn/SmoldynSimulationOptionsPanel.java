package org.vcell.solver.smoldyn;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.vcell.util.gui.CollapsiblePanel;
import org.vcell.util.gui.DialogUtils;

import cbit.vcell.solver.SolverTaskDescription;


@SuppressWarnings("serial")
public class SmoldynSimulationOptionsPanel extends CollapsiblePanel {

	private JCheckBox randomSeedCheckBox;
	private JTextField randomSeedTextField;
	private JTextField accuracyTextField = null;
	private JTextField gaussianTableSizeTextField;
	private SolverTaskDescription solverTaskDescription = null;	
	private JButton randomSeedHelpButton = null;
	private JButton accuracyHelpButton = null;
	private JButton gaussianTableSizeHelpButton = null;
	
	private IvjEventHandler ivjEventHandler = new IvjEventHandler();

	private class IvjEventHandler implements java.awt.event.ActionListener, java.awt.event.FocusListener, PropertyChangeListener {

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == randomSeedHelpButton) {
				DialogUtils.showInfoDialog(SmoldynSimulationOptionsPanel.this, "Random Seed", "<html>rand_seed <i>int</i> " +
						"<br>Seed for random number generator. If this line is not entered, the current " +
						"time is used as a seed, producing different sequences for each run.</html>");
			} else if (source == accuracyHelpButton) {
				DialogUtils.showInfoDialog(SmoldynSimulationOptionsPanel.this, "Accuracy", "<html>accuracy <i>float</i> " +
						"<br>A parameter that determines the quantitative accuracy of the simulation, on a scale from 0 to 10. " +
						"Low values are less accurate but run faster.  Default value is 10, for maximum accuracy. " +
						"Bimolecular reactions are only checked for pairs of reactants that are both within the same " +
						"virtual box when accuracy is 0 to 2.99, reactants in nearest neighboring boxes " +
						"are considered as well when accuracy is 3 to 6.99, and reactants in all types of " +
						"neighboring boxes are checked when accuracy is 7 to 10.</html>");
//			} else if (source == molPerBoxHelpButton) {
//				DialogUtils.showInfoDialog(SmoldynSimulationOptionsPanel.this, "<html>molperbox <i>float</i> " +
//						"<br>Virtual boxes are set up initially so the average number of " +
//						"molecules per box is no more than this value. The default value is 5. " +
//						"boxsize is an alternate way of entering comparable information.</html>");
//			} else if (source == boxSizeHelpButton) {
//				DialogUtils.showInfoDialog(SmoldynSimulationOptionsPanel.this, "<html>boxsize <i>float</i> " +
//						"<br>Rather than using molperbox to specify the sizes of the virtual boxes, " +
//						"boxsize can be used to request the width of the boxes. The actual box volumes will " +
//						"be no larger than the volume calculated from the width given here.</html>");
			} else if (source == gaussianTableSizeHelpButton) {
				DialogUtils.showInfoDialog(SmoldynSimulationOptionsPanel.this, "Gaussian Table Size", "<html>gauss_table_size <i>int</i> " +
						"<br>This sets the size of a lookup table that is used to generate Gaussiandistributed random numbers. " +
						"It needs to be an integer power of 2. The default value is 4096, which should be appropriate for nearly all applications.</html>");
			} else if (source == randomSeedCheckBox) {
				randomSeedTextField.setEditable(randomSeedCheckBox.isSelected());
				if(!randomSeedCheckBox.isSelected())
				{
					setNewRandomSeed();
				}
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (e.isTemporary()) {
				return;
			}
			if (e.getSource() == accuracyTextField) {
				setNewAccuracy();
			}
			if (e.getSource() == gaussianTableSizeTextField) {
				setNewGaussianTableSize();
			}
			if (e.getSource() == randomSeedTextField) {
				setNewRandomSeed();
			}
		}

		public void propertyChange(PropertyChangeEvent evt) {
			
		}
		
	}
	public SmoldynSimulationOptionsPanel() {
		super("Advanced Solver Options", false);
		initialize();
	}
	private void initialize() {
		randomSeedCheckBox = new JCheckBox("random seed");
		randomSeedTextField = new JTextField();
		JLabel accuracyLabel = new JLabel("accuracy");
		accuracyTextField = new JTextField();		
	
		JLabel gaussianTableSizeLabel = new JLabel("gauss table size");
		gaussianTableSizeTextField = new JTextField();
		
		randomSeedHelpButton = new JButton(" ? ");
		accuracyHelpButton = new JButton(" ? ");
		gaussianTableSizeHelpButton = new JButton(" ? ");
		Font font = randomSeedHelpButton.getFont().deriveFont(Font.BOLD);
		Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		randomSeedHelpButton.setFont(font);
		randomSeedHelpButton.setBorder(border);
		accuracyHelpButton.setFont(font);
		accuracyHelpButton.setBorder(border);
		gaussianTableSizeHelpButton.setFont(font);
		gaussianTableSizeHelpButton.setBorder(border);
		
		getContentPanel().setLayout(new GridBagLayout());		
		int gridy = 0;
		GridBagConstraints  gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = new Insets(0, 0, 0, 0);
		getContentPanel().add(accuracyLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 0, 0, 4);
		getContentPanel().add(accuracyHelpButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		getContentPanel().add(accuracyTextField, gbc);		

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 10, 0, 0);
		getContentPanel().add(gaussianTableSizeLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 0, 0, 4);
		getContentPanel().add(gaussianTableSizeHelpButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 5;
		gbc.gridy = gridy;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 0, 0, 5);
		getContentPanel().add(gaussianTableSizeTextField, gbc);
		
		// 1
		gridy ++;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_END;
		getContentPanel().add(randomSeedCheckBox, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 0, 0, 4);
		getContentPanel().add(randomSeedHelpButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = gridy;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		getContentPanel().add(randomSeedTextField, gbc);
	}
	
	private void initConnections() {
		randomSeedCheckBox.addActionListener(ivjEventHandler);
		randomSeedTextField.addFocusListener(ivjEventHandler);
		accuracyTextField.addFocusListener(ivjEventHandler);		
		gaussianTableSizeTextField.addFocusListener(ivjEventHandler);
		
		randomSeedHelpButton.addActionListener(ivjEventHandler);
		accuracyHelpButton.addActionListener(ivjEventHandler);
		gaussianTableSizeHelpButton.addActionListener(ivjEventHandler);
	}
	
	public final void setSolverTaskDescription(SolverTaskDescription newValue) {
		SolverTaskDescription oldValue = solverTaskDescription;
		if (oldValue != null) {
			oldValue.removePropertyChangeListener(ivjEventHandler);
		}
		solverTaskDescription = newValue;

		if (newValue != null) {
			newValue.addPropertyChangeListener(ivjEventHandler);
		}		
		solverTaskDescription = newValue;
		
		refresh();
		initConnections();
	}

	private void refresh() {
		if (solverTaskDescription != null) {
			if (!solverTaskDescription.getSolverDescription().isSpatialStochasticSolver()) {
				setVisible(false);
				return;
			}
		}
			
		setVisible(true);
		SmoldynSimulationOptions smoldynSimulationOptions = solverTaskDescription.getSmoldynSimulationOptions();
		Integer randomSeed = smoldynSimulationOptions.getRandomSeed();
		if (randomSeed == null) {
			randomSeedTextField.setEditable(false);
			randomSeedCheckBox.setSelected(false);
		} else {			
			randomSeedTextField.setEditable(true);
			randomSeedCheckBox.setSelected(true);
			randomSeedTextField.setText(randomSeed.toString());
		}
		
		gaussianTableSizeTextField.setText("" + smoldynSimulationOptions.getGaussianTableSize());
		accuracyTextField.setText("" + smoldynSimulationOptions.getAccuracy());
	}
	
	private void setNewAccuracy(){
		if(!isVisible()){
			return;
		}
		
		try {
			double accuracy = Double.parseDouble(accuracyTextField.getText());
			solverTaskDescription.getSmoldynSimulationOptions().setAccuracy(accuracy);
		} catch (NumberFormatException ex) {
			DialogUtils.showErrorDialog(this, "Wrong number format for accuracy: " + ex.getMessage());
			return;
		}
	}
	
	private void setNewGaussianTableSize(){
		if(!isVisible()){
			return;
		}
		
		try {
			int gaussianTableSize = Integer.parseInt(gaussianTableSizeTextField.getText());
			solverTaskDescription.getSmoldynSimulationOptions().setGaussianTableSize(gaussianTableSize);
		} catch (NumberFormatException ex) {
			DialogUtils.showErrorDialog(this, "Wrong number format for gaussian table size: " + ex.getMessage());
		} catch (PropertyVetoException e) {
			DialogUtils.showErrorDialog(this, "Wrong number format for gaussian table size: " + e.getMessage());			
		}
	}
	
	private void setNewRandomSeed(){
		if(!isVisible()){
			return;
		}
		
		Integer randomSeed = null;
		if (randomSeedCheckBox.isSelected()) {
			try {
				randomSeed = new Integer(randomSeedTextField.getText());
			} catch (NumberFormatException ex) {
				DialogUtils.showErrorDialog(this, "Wrong number format for random seed: " + ex.getMessage());
				return;
			}
		}
		solverTaskDescription.getSmoldynSimulationOptions().setRandomSeed(randomSeed);		
	}

}
