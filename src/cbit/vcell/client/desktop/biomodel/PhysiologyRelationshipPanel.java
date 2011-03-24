package cbit.vcell.client.desktop.biomodel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.vcell.util.gui.sorttable.JSortTable;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.model.BioModelEntityObject;


@SuppressWarnings("serial")
public class PhysiologyRelationshipPanel extends DocumentEditorSubPanel {
	private BioModelEntityObject bioModelEntityObject = null;
	private EventHandler eventHandler = new EventHandler();
	private BioModel bioModel = null;
	private PhysiologyRelationshipTableModel tableModel = null; 
	private JSortTable table;
	private JCheckBox showLinkedEntityCheckBox = null;
	private JTextField textFieldSearch = null;
	
	private class EventHandler implements ActionListener, DocumentListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if(e.getSource() == showLinkedEntityCheckBox){
				tableModel.setShowLinkOnly(showLinkedEntityCheckBox.isSelected());
			}
		}
		public void insertUpdate(DocumentEvent e) {
			searchTable();
		}
		public void removeUpdate(DocumentEvent e) {
			searchTable();
		}
		public void changedUpdate(DocumentEvent e) {
			searchTable();
		}
	}
	
public PhysiologyRelationshipPanel() {
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

private void initialize() {
	try {
		setName("KineticsTypeTemplatePanel");
		setLayout(new GridBagLayout());
			
		table = new JSortTable();
		tableModel = new PhysiologyRelationshipTableModel(table);
		table.setModel(tableModel);
		table.disableUneditableForeground();
		
		int gridy = 0;
		GridBagConstraints gbc = new java.awt.GridBagConstraints();		
		gbc.gridx = 0;
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		table.setPreferredScrollableViewportSize(new Dimension(200,200));
		add(table.getEnclosingScrollPane(), gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 8;
		gbc.gridy = gridy;
		add(Box.createRigidArea(new Dimension(0, 75)), gbc);

		gridy ++;	
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = new Insets(4,4,4,4);
		add(new JLabel("Search "), gbc);

		textFieldSearch = new JTextField(30);
		textFieldSearch.addActionListener(eventHandler);
		textFieldSearch.getDocument().addDocumentListener(eventHandler);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 0, 4, 0);
		add(textFieldSearch, gbc);
		
		showLinkedEntityCheckBox = new JCheckBox("Show linked pathway entities only");
		showLinkedEntityCheckBox.setBackground(Color.white);
		showLinkedEntityCheckBox.addActionListener(eventHandler);
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 4; 
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 10, 4, 0);
		add(showLinkedEntityCheckBox, gbc);	
		
		setBackground(Color.white);		
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
}

private void searchTable() {
	String searchText = textFieldSearch.getText();
	tableModel.setSearchText(searchText);
}

// done
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		PhysiologyRelationshipPanel aKineticsTypeTemplatePanel;
		aKineticsTypeTemplatePanel = new PhysiologyRelationshipPanel();
		frame.setContentPane(aKineticsTypeTemplatePanel);
		frame.setSize(aKineticsTypeTemplatePanel.getSize());
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
 * Sets the kinetics property (cbit.vcell.model.Kinetics) value.
 * @param kinetics The new value for the property.
 * @see #getKinetics
 */
public void setBioModelEntityObject(BioModelEntityObject newValue) {
	if (bioModelEntityObject == newValue) {
		return;
	}
	bioModelEntityObject = newValue;
	tableModel.setBioModelEntityObject(newValue);
}

public void setBioModel(BioModel newValue) {
	if (bioModel == newValue) {
		return;
	}
	bioModel = newValue;
	tableModel.setBioModel(newValue);
}

@Override
protected void onSelectedObjectsChange(Object[] selectedObjects) {
	// TODO Auto-generated method stub
	
}

}

