package cbit.vcell.client.desktop.biomodel;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.vcell.util.gui.DialogUtils;

import cbit.gui.TextFieldAutoCompletion;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.mapping.SpeciesContextSpec;
import cbit.vcell.model.Kinetics.KineticsParameter;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.model.Kinetics;
import cbit.vcell.model.Parameter;
import cbit.vcell.parser.Expression;
import cbit.vcell.units.VCUnitDefinition;
/**
 * Insert the type's description here.
 * Creation date: (2/3/2003 2:07:01 PM)
 * @author: Frank Morgan
 */
@SuppressWarnings("serial")
public class ParameterPropertiesPanel extends DocumentEditorSubPanel {
	private Parameter parameter = null;
	private EventHandler eventHandler = new EventHandler();
	private JTextArea annotationTextArea;
	private JTextField nameTextField = null;
	private JTextField unitTextField = null;
	private TextFieldAutoCompletion expressionTextField = null;
	private JTextField descriptionTextField = null;
	private JLabel pathLabel = null;

	private class EventHandler implements ActionListener, FocusListener, PropertyChangeListener {
		public void focusGained(FocusEvent e) {
		}
		public void focusLost(FocusEvent e) {
			if (e.getSource() == annotationTextArea) {
				changeAnnotation();
			} else if (e.getSource() == nameTextField) {
				changeName();
			} else if (e.getSource() == unitTextField) {
				changeUnit();
			} else if (e.getSource() == expressionTextField) {
				if (expressionTextField.isPopupVisible()) {
					return;
				}
				changeExpression();
			}
		}
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == parameter) {
				updateInterface();
			}
		}
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == nameTextField) {
				changeName();
			} else if (e.getSource() == unitTextField) {
				changeUnit();
			} else if (e.getSource() == expressionTextField) {
				changeExpression();
			}
		}
	}

/**
 * EditSpeciesDialog constructor comment.
 */
public ParameterPropertiesPanel() {
	super();
	initialize();
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
 * Initialize the class.
 */
private void initialize() {
	try {
		nameTextField = new JTextField();
		nameTextField.setEditable(false);
		unitTextField = new JTextField();
		unitTextField.setEditable(false);
		expressionTextField = new TextFieldAutoCompletion();
		expressionTextField.setEditable(false);
		descriptionTextField = new JTextField();
		descriptionTextField.setEditable(false);
		annotationTextArea = new javax.swing.JTextArea("", 1, 30);
		annotationTextArea.setLineWrap(true);
		annotationTextArea.setWrapStyleWord(true);
		annotationTextArea.setEditable(false);
		annotationTextArea.setBackground(UIManager.getColor("TextField.inactiveBackground"));

		nameTextField.addActionListener(eventHandler);
		unitTextField.addActionListener(eventHandler);
		expressionTextField.addActionListener(eventHandler);
		
		annotationTextArea.addFocusListener(eventHandler);
		nameTextField.addFocusListener(eventHandler);
		expressionTextField.addFocusListener(eventHandler);
		unitTextField.addFocusListener(eventHandler);
		
		setBackground(Color.white);
		setLayout(new GridBagLayout());
		int gridy = 0;
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.gridwidth = 2;
		gbc.insets = new java.awt.Insets(0, 4, 0, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pathLabel= new JLabel("");
		pathLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pathLabel.setFont(pathLabel.getFont().deriveFont(Font.BOLD));
		add(pathLabel, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		JLabel label = new JLabel("Parameter Name");
		add(label, gbc);
		
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		add(nameTextField, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		label = new JLabel("Description");
		add(label, gbc);
		
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		add(descriptionTextField, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		label = new JLabel("Unit");
		add(label, gbc);
		
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		add(unitTextField, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		label = new JLabel("Expression");
		add(label, gbc);
		
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		add(expressionTextField, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		add(new JLabel("Annotation"), gbc);

		javax.swing.JScrollPane jsp = new javax.swing.JScrollPane(annotationTextArea);
		gbc = new java.awt.GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1;
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		add(jsp, gbc);		
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
}

/**
 * Comment
 */
private void changeAnnotation() {
	try{
		if (parameter == null) {
			return;
		}
		String text = annotationTextArea.getText();
		if (parameter instanceof ModelParameter) {
			((ModelParameter)parameter).setModelParameterAnnotation(text);
		}
	} catch(Exception e){
		e.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(this, e.getMessage(), e);
	}
}

private void changeUnit() {
	try{
		if (parameter == null) {
			return;
		}
		String text = unitTextField.getText();
		if (text.length() == 0) {
			parameter.setUnitDefinition(VCUnitDefinition.UNIT_TBD);
		} else if (parameter.getUnitDefinition() == null ||  !parameter.getUnitDefinition().getSymbol().equals(text)){
			parameter.setUnitDefinition(VCUnitDefinition.getInstance(text));
		}
	} catch(Exception e){
		e.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(this, e.getMessage(), e);
	}
}

private void changeExpression() {
	try{
		if (parameter == null) {
			return;
		}
		String text = expressionTextField.getText();
		if(text.equals(parameter.getExpression().infix())) {
			return;
		}
		if (parameter instanceof SpeciesContextSpec.SpeciesContextSpecParameter){
			SpeciesContextSpec.SpeciesContextSpecParameter scsParm = (SpeciesContextSpec.SpeciesContextSpecParameter)parameter;
			Expression newExp = null;
			if (text == null || text.trim().length() == 0) {
				if (scsParm.getRole() == SpeciesContextSpec.ROLE_InitialConcentration
						|| scsParm.getRole() == SpeciesContextSpec.ROLE_DiffusionRate
						|| scsParm.getRole() == SpeciesContextSpec.ROLE_InitialCount) {
					newExp = new Expression(0.0);
				}
			} else {
				newExp = new Expression(text);
			}
			scsParm.setExpression(newExp);	
		} else if (parameter instanceof KineticsParameter){
			Expression exp1 = new Expression(text);
			Kinetics kinetics = ((KineticsParameter) parameter).getKinetics();
			kinetics.setParameterValue((Kinetics.KineticsParameter)parameter, exp1);
		} else {
			Expression exp1 = new Expression(text);
			exp1.bindExpression(parameter.getNameScope().getScopedSymbolTable());
			parameter.setExpression(exp1);
		}
	} catch(Exception e){
		e.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(this, e.getMessage(), e);
	}
}

/**
 * Sets the speciesContext property (cbit.vcell.model.SpeciesContext) value.
 * @param speciesContext The new value for the property.
 * @see #getSpeciesContext
 */
void setParameter(Parameter newValue) {
	if (newValue == parameter) {
		return;
	}
	Parameter oldValue = parameter;
	if (oldValue != null) {
		oldValue.removePropertyChangeListener(eventHandler);
	}
	parameter = newValue;
	if (newValue != null) {
		newValue.addPropertyChangeListener(eventHandler);
	}
	updateInterface();
}

/**
 * Comment
 */
private void updateInterface() {
	boolean bNonNullParameter = parameter != null;
	nameTextField.setEditable(bNonNullParameter && (parameter instanceof ModelParameter || parameter.isNameEditable()));
	descriptionTextField.setEditable(bNonNullParameter && parameter.isDescriptionEditable());		
	if (bNonNullParameter) {
		nameTextField.setEditable(parameter.isNameEditable());
		expressionTextField.setEditable(parameter.isExpressionEditable());
		unitTextField.setEditable(parameter.isUnitEditable());

		pathLabel.setText("Defined In: " + parameter.getNameScope().getPathDescription());
		expressionTextField.setSymbolTable(parameter.getNameScope().getScopedSymbolTable());
		nameTextField.setText(parameter.getName());
		descriptionTextField.setText(parameter.getDescription());
		if (parameter.getExpression() != null) {
			expressionTextField.setText(parameter.getExpression().infix());
		}
		unitTextField.setText(parameter.getUnitDefinition().getSymbol());
		
		boolean bAnnotationEditable = false;
		if (parameter instanceof ModelParameter) {
			annotationTextArea.setText(((ModelParameter)parameter).getModelParameterAnnotation());
			bAnnotationEditable = true;
		}
		annotationTextArea.setEditable(bAnnotationEditable);
		annotationTextArea.setBackground(bAnnotationEditable ? UIManager.getColor("TextField.background") : UIManager.getColor("TextField.inactiveBackground"));
	} else {
		pathLabel.setText(null);
		annotationTextArea.setText(null);
		nameTextField.setText(null);
		descriptionTextField.setText(null);
		expressionTextField.setText(null);
		unitTextField.setText(null);
		
		annotationTextArea.setEditable(false);
		nameTextField.setEditable(false);
		expressionTextField.setEditable(false);
		unitTextField.setEditable(false);
		annotationTextArea.setEditable(false);
	}
}

private void changeName() {
	if (parameter == null) {
		return;
	}
	String newName = nameTextField.getText();
	if (newName == null || newName.length() == 0) {
		nameTextField.setText(parameter.getName());
		return;
	}
	if (newName.equals(parameter.getName())) {
		return;
	}
	try {
		parameter.setName(newName);
	} catch (PropertyVetoException e1) {
		e1.printStackTrace();
		DialogUtils.showErrorDialog(ParameterPropertiesPanel.this, e1.getMessage());
	}
}

@Override
protected void onSelectedObjectsChange(Object[] selectedObjects) {
	if (selectedObjects == null || selectedObjects.length != 1) {
		return;
	}
	if (selectedObjects[0] instanceof Parameter) {
		setParameter((Parameter) selectedObjects[0]);
	} else {
		setParameter(null);
	}	
}
}
