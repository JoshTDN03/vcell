package org.vcell.chombo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cbit.vcell.client.constants.GuiConstants;
import cbit.vcell.solver.SolverTaskDescription;

@SuppressWarnings("serial")
public class ChomboOutputOptionsPanel extends JPanel {

	private class IvjEventHandler implements ActionListener, PropertyChangeListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			enableGuiElements();
			if (e.getSource() == chomboOutputCheckBox || e.getSource() == vcellOutputCheckBox) 
			{
				setChomboOutputOptions();
			} 
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == solverTaskDescription)
			{
				updateDisplay();
			}
		}
	}
	
	private IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private SolverTaskDescription solverTaskDescription;	
	private ChomboSolverSpec chomboSolverSpec;
	
	private JCheckBox vcellOutputCheckBox;
	private JCheckBox chomboOutputCheckBox;
//	private JTextField numProcessors = new JTextField(2);
	private JFormattedTextField numProcessors = new JFormattedTextField(new DecimalFormat("##"));
	
	private class FileOptions extends JPanel {
		FileOptions( ) {
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createTitledBorder(GuiConstants.TAB_PANEL_BORDER, "Save Output Files"));

			vcellOutputCheckBox = new JCheckBox("VCell (single processor only)");
			chomboOutputCheckBox = new JCheckBox("Chombo");
			int gridy = 0;
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);		
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.anchor = GridBagConstraints.WEST;
			add(vcellOutputCheckBox, gbc);

			++ gridy;
			gbc = new GridBagConstraints();
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.anchor = GridBagConstraints.WEST;
			add(chomboOutputCheckBox, gbc);

			vcellOutputCheckBox.addActionListener(ivjEventHandler);
			chomboOutputCheckBox.addActionListener(ivjEventHandler);
		}
	}
	
	private  class ProcessorOptions extends JPanel {
		public ProcessorOptions() {
			super(new FlowLayout(FlowLayout.LEFT));
			add(new JLabel("Num Processors:"));
			add(numProcessors);
			numProcessors.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
			numProcessors.setColumns(2);
			numProcessors.addPropertyChangeListener("value",new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					Object o = numProcessors.getValue();
					if (o != null) {
						Number n = (Number) o;
						solverTaskDescription.setNumProcessors(n.intValue());
					}
					setNumProcessorsField();
					enableGuiElements();
				}
			});
		}
	}
	
	/**
	 * set enabled state of VCell style output and num processors
	 * based on setting of other. Give priority to num processors (just to pick one)
	 */
	private void enableGuiElements( ) {
		vcellOutputCheckBox.setEnabled(!solverTaskDescription.isParallel());
		if (solverTaskDescription.isParallel()) {
			vcellOutputCheckBox.setSelected(false);
		}
		//numProcessors.setEnabled(!vcellOutputCheckBox.isSelected());
		numProcessors.setEnabled(false);
	}
	

	public ChomboOutputOptionsPanel() {
		super(new BorderLayout());
		add(new FileOptions(),BorderLayout.CENTER);
		add(new ProcessorOptions(),BorderLayout.EAST);
	}

	private void setChomboOutputOptions() {
		chomboSolverSpec.setSaveVCellOutput(vcellOutputCheckBox.isSelected());
		chomboSolverSpec.setSaveChomboOutput(chomboOutputCheckBox.isSelected());
	}
	
	/**
	 * set num processors field from current solverTaskDescription 
	 */
	private void setNumProcessorsField( ) {
		//numProcessors.setValue(new Long(solverTaskDescription.getNumProcessors()));
		numProcessors.setValue(1);
	}

	private void updateDisplay() {
		if (!solverTaskDescription.getSolverDescription().isChomboSolver()) {
			chomboSolverSpec = null;
			setVisible(false);
			return;
		}
		chomboSolverSpec = solverTaskDescription.getChomboSolverSpec();
		setVisible(true);
		vcellOutputCheckBox.setSelected(chomboSolverSpec.isSaveVCellOutput());
		chomboOutputCheckBox.setSelected(chomboSolverSpec.isSaveChomboOutput());
		setNumProcessorsField();
	}

	public final void setSolverTaskDescription(SolverTaskDescription newValue) {
		SolverTaskDescription oldValue = this.solverTaskDescription;
		if (oldValue == newValue)
		{
			return;
		}
		if (oldValue != null)
		{
			oldValue.removePropertyChangeListener(ivjEventHandler);
		}
		this.solverTaskDescription = newValue;
		if (solverTaskDescription != null)
		{
			solverTaskDescription.addPropertyChangeListener(ivjEventHandler);
		}
		updateDisplay();		
	}
}
