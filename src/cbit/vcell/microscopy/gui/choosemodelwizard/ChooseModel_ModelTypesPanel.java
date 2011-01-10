package cbit.vcell.microscopy.gui.choosemodelwizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EtchedBorder;

import cbit.vcell.microscopy.FRAPModel;

@SuppressWarnings("serial")
public class ChooseModel_ModelTypesPanel extends JPanel
{
	private JCheckBox diffOneCheckBox = null;
	private JCheckBox diffTwoCheckBox = null;
	private JCheckBox koffCheckBox = null;
	private JCheckBox effectiveDiffCheckBox = null;
//	private JCheckBox diffBindingCheckBox = null;
	
	public ChooseModel_ModelTypesPanel() {
		super();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0,0,0,0,7,7,7,7,0,0,0,0,0,7,0,0,0,0,0,0,7};
		setLayout(gridBagLayout);

		final JLabel choosePossibleModelLabel = new JLabel();
		choosePossibleModelLabel.setFont(new Font("", Font.BOLD | Font.ITALIC, 16));
		choosePossibleModelLabel.setForeground(new Color(0, 0, 128));
		choosePossibleModelLabel.setText("Choose Possible Models");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		add(choosePossibleModelLabel, gridBagConstraints);

		final JLabel chooseOneOrLabel = new JLabel();
		chooseOneOrLabel.setText("Choose one or more possible models that may fit your data. A comparison of the ");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.anchor = GridBagConstraints.WEST;
		gridBagConstraints_1.gridy = 7;
		gridBagConstraints_1.gridx = 0;
		add(chooseOneOrLabel, gridBagConstraints_1);

		final JLabel selectedModelsWillLabel = new JLabel();
		selectedModelsWillLabel.setText("selected models will be given after  'Parameter Estimation'.  The possible models are");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.anchor = GridBagConstraints.WEST;
		gridBagConstraints_2.gridy = 8;
		gridBagConstraints_2.gridx = 0;
		add(selectedModelsWillLabel, gridBagConstraints_2);

		final JLabel diffusionWithOneLabel = new JLabel();
		diffusionWithOneLabel.setText("listed below :");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.anchor = GridBagConstraints.WEST;
		gridBagConstraints_3.gridy = 9;
		gridBagConstraints_3.gridx = 0;
		add(diffusionWithOneLabel, gridBagConstraints_3);

		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(450, 3));
		separator.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.RAISED),new EtchedBorder(EtchedBorder.LOWERED)));
		GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.anchor = GridBagConstraints.WEST;
		gridBagConstraints_8.gridy = 12;
		gridBagConstraints_8.gridx = 0;
		add(separator, gridBagConstraints_8);
		
		JLabel diffustionOnlyLabel = new JLabel("Diffusion Dominant Models :");
		diffustionOnlyLabel.setFont(new Font("", Font.BOLD, 12));
		diffustionOnlyLabel.setForeground(new Color(0, 0, 128));
		GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		gridBagConstraints_7.gridy = 18;
		gridBagConstraints_7.gridx = 0;
		add(diffustionOnlyLabel, gridBagConstraints_7);
		
		diffOneCheckBox = new JCheckBox();
		diffOneCheckBox.setSelected(true);
		diffOneCheckBox.setText(FRAPModel.MODEL_TYPE_ARRAY[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT]);
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.anchor = GridBagConstraints.WEST;
		gridBagConstraints_5.gridy = 19;
		gridBagConstraints_5.gridx = 0;
		add(diffOneCheckBox, gridBagConstraints_5);

		diffTwoCheckBox = new JCheckBox();
		diffTwoCheckBox.setText(FRAPModel.MODEL_TYPE_ARRAY[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS]);
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.anchor = GridBagConstraints.WEST;
		gridBagConstraints_6.gridy = 20;
		gridBagConstraints_6.gridx = 0;
		add(diffTwoCheckBox, gridBagConstraints_6);
		
		JLabel reactionOnlyLabel = new JLabel("Reaction Dominant Models :");
		reactionOnlyLabel.setFont(new Font("", Font.BOLD, 12));
		reactionOnlyLabel.setForeground(new Color(0, 0, 128));
		GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.anchor = GridBagConstraints.WEST;
		gridBagConstraints_9.gridy = 25;
		gridBagConstraints_9.gridx = 0;
		add(reactionOnlyLabel, gridBagConstraints_9);
		
		koffCheckBox = new JCheckBox();
		koffCheckBox.setText(FRAPModel.MODEL_TYPE_ARRAY[FRAPModel.IDX_MODEL_REACTION_OFF_RATE]);
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.anchor = GridBagConstraints.WEST;
		gridBagConstraints_10.gridy = 26;
		gridBagConstraints_10.gridx = 0;
		add(koffCheckBox, gridBagConstraints_10);
		
		/*JLabel effectiveDiffLabel = new JLabel("Effective diffusion model :");
		reactionOnlyLabel.setFont(new Font("", Font.BOLD, 12));
		reactionOnlyLabel.setForeground(new Color(0, 0, 128));
		GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.anchor = GridBagConstraints.WEST;
		gridBagConstraints_11.gridy = 21;
		gridBagConstraints_11.gridx = 0;
		add(effectiveDiffLabel, gridBagConstraints_11);
		
		effectiveDiffCheckBox = new JCheckBox();
		effectiveDiffCheckBox.setText(FRAPModel.MODEL_TYPE_ARRAY[FRAPModel.IDX_MODEL_EFFECTIVE_DIFFUSION]);
		final GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.anchor = GridBagConstraints.WEST;
		gridBagConstraints_12.gridy = 22;
		gridBagConstraints_12.gridx = 0;
		add(effectiveDiffCheckBox, gridBagConstraints_12);*/
		/*diffBindingCheckBox = new JCheckBox();
		diffBindingCheckBox.setText("Diffusion plus Binding");
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		gridBagConstraints_7.gridy = 15;
		gridBagConstraints_7.gridx = 0;
		add(diffBindingCheckBox, gridBagConstraints_7);*/
	}
	
	public boolean[] getModelTypes()
	{
		boolean[] result = new boolean[FRAPModel.NUM_MODEL_TYPES];
		if(diffOneCheckBox.isSelected())
		{
			result[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT] = true;
		}
		if(diffTwoCheckBox.isSelected())
		{
			result[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS] = true;
		}
		if(koffCheckBox.isSelected())
		{
			result[FRAPModel.IDX_MODEL_REACTION_OFF_RATE] = true;
		}
		return result;
	}
	
	public int getNumUserSelectedModelTypes()
	{
		int numTypes = 0;
		for(boolean modelSelected:getModelTypes())
		{
			if(modelSelected)
			{
				numTypes++;
			}
		}
		return numTypes;
	}
	
	public void clearAllSelected()
	{
		diffOneCheckBox.setSelected(false);
		diffTwoCheckBox.setSelected(false);
		koffCheckBox.setSelected(false);
//		diffBindingCheckBox.setSelected(false);
	}
	
	public void setDiffOneSelected(boolean bSelected)
	{
		diffOneCheckBox.setSelected(bSelected);
	}
	
	public void setDiffTwoSelected(boolean bSelected)
	{
		diffTwoCheckBox.setSelected(bSelected);
	}
	
	public void setReactionOffRateSelected(boolean bSelected)
	{
		koffCheckBox.setSelected(bSelected);
	}
//	public void setDiffBindingSelected(boolean bSelected)
//	{
//		diffBindingCheckBox.setSelected(bSelected);
//	}
}
