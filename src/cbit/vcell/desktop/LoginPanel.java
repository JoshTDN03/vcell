/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.desktop;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;

import javax.swing.*;

import org.vcell.util.document.User;
import org.vcell.util.document.UserLoginInfo;

import cbit.vcell.client.ChildWindowListener;
import cbit.vcell.client.ChildWindowManager;
import cbit.vcell.client.ChildWindowManager.ChildWindow;

/**
 * Insert the type's description here.
 * Creation date: (1/26/2001 1:59:33 AM)
 * @author: Ion Moraru
 */
public class LoginPanel extends JPanel {
	
	public static final String DIALOG_TITLE = "Virtual Cell Login";

	private JPanel ivjJPanel1 = null;
	private JButton lostPasswordJButton;
	private JButton ivjJButtonCancel = null;
	private JButton ivjJButtonOK = null;
	private JLabel ivjJLabelPassword = null;
	private JLabel ivjJLabelUser = null;
	private JPasswordField ivjJPasswordFieldPassword = null;
	private JTextField ivjJTextFieldUser = null;
	protected transient java.awt.event.ActionListener aActionListener = null;
	private java.lang.String fieldUser = new String();
	private java.lang.String fieldPassword = new String();
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private JButton ivjJButtonRegister = null;
	private LoginDelegate loginDelegate;


class IvjEventHandler implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == LoginPanel.this.getJTextFieldUser() || e.getSource() == LoginPanel.this.getJPasswordFieldPassword() || e.getSource() == LoginPanel.this.getJButtonOK()) {
				updateFields();
				loginDelegate.login(fieldUser, new UserLoginInfo.DigestedPassword(fieldPassword));
			}
			if (e.getSource() == LoginPanel.this.getJButtonRegister()) {
				loginDelegate.registerRequest();
			}
			if (e.getSource() == LoginPanel.this.getLostPasswordJButton()){
				updateFields();
				loginDelegate.lostPasswordRequest(fieldUser);
			}
			if (e.getSource() == LoginPanel.this.getJButtonCancel()){
				loginDelegate.userCancel();
			}
		};
	};

/**
 * LoginDialog constructor comment.
 */
public LoginPanel(LoginDelegate loginDelegate) {
	super();
	this.loginDelegate = loginDelegate;
    initialize();
}


/**
 * Return the JButtonCancel property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButtonCancel() {
	if (ivjJButtonCancel == null) {
			ivjJButtonCancel = new javax.swing.JButton();
			ivjJButtonCancel.setName("JButtonCancel");
			ivjJButtonCancel.setText("Cancel");
			// user code begin {1}
			// user code end
	}
	return ivjJButtonCancel;
}


/**
 * Return the JButtonOK property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButtonOK() {
	if (ivjJButtonOK == null) {
			ivjJButtonOK = new javax.swing.JButton();
			ivjJButtonOK.setName("JButtonOK");
			ivjJButtonOK.setText("Login");
			// user code begin {1}
			// user code end
	}
	return ivjJButtonOK;
}



/**
 * Return the JButtonRegister property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButtonRegister() {
	if (ivjJButtonRegister == null) {
			ivjJButtonRegister = new javax.swing.JButton();
			ivjJButtonRegister.setName("JButtonRegister");
			ivjJButtonRegister.setFont(new java.awt.Font("Arial", 1, 14));
			ivjJButtonRegister.setText("New User Registration...");
			// user code begin {1}
			// user code end
	}
	return ivjJButtonRegister;
}

/**
 * Return the JDialogContentPane property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setupLoginPanelContentPane() {
	
			this.setName("JDialogContentPane");
			final java.awt.GridBagLayout gridBagLayout = new java.awt.GridBagLayout();
			gridBagLayout.rowHeights = new int[] {0,0,0,7,0};
			this.setLayout(gridBagLayout);

			java.awt.GridBagConstraints constraintsJLabelUser = new java.awt.GridBagConstraints();
			constraintsJLabelUser.gridx = 0; constraintsJLabelUser.gridy = 0;
			constraintsJLabelUser.insets = new java.awt.Insets(4, 10, 4, 4);
			this.add(getJLabelUser(), constraintsJLabelUser);

			java.awt.GridBagConstraints constraintsJLabelPassword = new java.awt.GridBagConstraints();
			constraintsJLabelPassword.gridx = 0; constraintsJLabelPassword.gridy = 1;
			constraintsJLabelPassword.insets = new java.awt.Insets(4, 10, 4, 4);
			this.add(getJLabelPassword(), constraintsJLabelPassword);

			java.awt.GridBagConstraints constraintsJTextFieldUser = new java.awt.GridBagConstraints();
			constraintsJTextFieldUser.gridx = 1; constraintsJTextFieldUser.gridy = 0;
			constraintsJTextFieldUser.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsJTextFieldUser.weightx = 1.0;
			constraintsJTextFieldUser.insets = new java.awt.Insets(4, 4, 4, 10);
			this.add(getJTextFieldUser(), constraintsJTextFieldUser);

			java.awt.GridBagConstraints constraintsJPasswordFieldPassword = new java.awt.GridBagConstraints();
			constraintsJPasswordFieldPassword.gridx = 1; constraintsJPasswordFieldPassword.gridy = 1;
			constraintsJPasswordFieldPassword.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsJPasswordFieldPassword.weightx = 1.0;
			constraintsJPasswordFieldPassword.insets = new java.awt.Insets(4, 4, 4, 10);
			this.add(getJPasswordFieldPassword(), constraintsJPasswordFieldPassword);

			java.awt.GridBagConstraints constraintsJPanel1 = new java.awt.GridBagConstraints();
			constraintsJPanel1.fill = GridBagConstraints.HORIZONTAL;
			constraintsJPanel1.gridx = 0; constraintsJPanel1.gridy = 2;
			constraintsJPanel1.gridwidth = 2;
			constraintsJPanel1.weightx = 1;
			constraintsJPanel1.insets = new java.awt.Insets(4, 10, 4, 3);
			this.add(getJPanel1(), constraintsJPanel1);

			java.awt.GridBagConstraints constraintsJButtonRegister = new java.awt.GridBagConstraints();
			constraintsJButtonRegister.fill = GridBagConstraints.HORIZONTAL;
			constraintsJButtonRegister.gridx = 0; constraintsJButtonRegister.gridy = 4;
			constraintsJButtonRegister.gridwidth = 2;
			constraintsJButtonRegister.insets = new java.awt.Insets(2, 10, 2, 10);
			final GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(4, 10, 4, 10);
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.gridy = 3;
			gridBagConstraints.gridx = 0;
			this.add(getLostPasswordJButton(), gridBagConstraints);
			this.add(getJButtonRegister(), constraintsJButtonRegister);
			// user code begin {1}
			// user code end
}

/**
 * Return the JLabelPassword property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabelPassword() {
	if (ivjJLabelPassword == null) {
			ivjJLabelPassword = new javax.swing.JLabel();
			ivjJLabelPassword.setName("JLabelPassword");
			ivjJLabelPassword.setText("Password");
			// user code begin {1}
			// user code end
	}
	return ivjJLabelPassword;
}


/**
 * Return the JLabelUser property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabelUser() {
	if (ivjJLabelUser == null) {
			ivjJLabelUser = new javax.swing.JLabel();
			ivjJLabelUser.setName("JLabelUser");
			ivjJLabelUser.setText("User Name");
			// user code begin {1}
			// user code end
	}
	return ivjJLabelUser;
}

/**
 * Return the JPanel1 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel1() {
	if (ivjJPanel1 == null) {
			ivjJPanel1 = new javax.swing.JPanel();
			ivjJPanel1.setName("JPanel1");
			final java.awt.GridBagLayout gridBagLayout = new java.awt.GridBagLayout();
			gridBagLayout.columnWidths = new int[] {0,0,7,0};
			ivjJPanel1.setLayout(gridBagLayout);

			java.awt.GridBagConstraints constraintsJButtonOK = new java.awt.GridBagConstraints();
			constraintsJButtonOK.anchor = GridBagConstraints.WEST;
			constraintsJButtonOK.weightx = .5;
			constraintsJButtonOK.fill = GridBagConstraints.HORIZONTAL;
			constraintsJButtonOK.gridx = 0; constraintsJButtonOK.gridy = 0;
			constraintsJButtonOK.insets = new java.awt.Insets(4, 0, 4, 4);
			getJPanel1().add(getJButtonOK(), constraintsJButtonOK);

			java.awt.GridBagConstraints constraintsJButtonCancel = new java.awt.GridBagConstraints();
			constraintsJButtonCancel.fill = GridBagConstraints.HORIZONTAL;
			constraintsJButtonCancel.anchor = GridBagConstraints.EAST;
			constraintsJButtonCancel.weightx = .5;
			constraintsJButtonCancel.gridx = 1; constraintsJButtonCancel.gridy = 0;
			constraintsJButtonCancel.insets = new java.awt.Insets(4, 4, 4, 0);
			getJPanel1().add(getJButtonCancel(), constraintsJButtonCancel);
			// user code begin {1}
			// user code end
	}
	return ivjJPanel1;
}


/**
 * Return the JPasswordFieldPassword property value.
 * @return javax.swing.JPasswordField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPasswordField getJPasswordFieldPassword() {
	if (ivjJPasswordFieldPassword == null) {
			ivjJPasswordFieldPassword = new javax.swing.JPasswordField();
			ivjJPasswordFieldPassword.setName("JPasswordFieldPassword");
			// user code begin {1}
			// user code end
	}
	return ivjJPasswordFieldPassword;
}


/**
 * Return the JTextFieldUser property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getJTextFieldUser() {
	if (ivjJTextFieldUser == null) {
			ivjJTextFieldUser = new javax.swing.JTextField();
			ivjJTextFieldUser.setName("JTextFieldUser");
			// user code begin {1}
			// user code end
	}
	return ivjJTextFieldUser;
}


/**
 * Gets the password property (java.lang.String) value.
 * @return The password property value.
 */
public java.lang.String getPassword() {
	return fieldPassword;
}


/**
 * Gets the user property (java.lang.String) value.
 * @return The user property value.
 */
public java.lang.String getUser() {
	return fieldUser;
}


/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() {
	// user code begin {1}
	// user code end
	getJTextFieldUser().addActionListener(ivjEventHandler);
	getJPasswordFieldPassword().addActionListener(ivjEventHandler);
	//ChildWindowManager.findChildWindowManager(this).getChildWindowFromContentPane(this).addChildWindowListener(ivjEventHandler);
	getJButtonCancel().addActionListener(ivjEventHandler);
	getJButtonOK().addActionListener(ivjEventHandler);
	getJButtonRegister().addActionListener(ivjEventHandler);
	getLostPasswordJButton().addActionListener(ivjEventHandler);
}

/**
 * Initialize the class.
 * @param owner 
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void initialize() {
		// user code begin {1}
		// user code end
		setName("LoginPanel");
		//setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setSize(315, 226);
		//setModal(true);
		setupLoginPanelContentPane();
		initConnections();
		setLoggedInUser(null);
	// user code begin {2}
	// user code end
}

/**
 * Comment
 */
private void updateFields() {
	fieldUser = getJTextFieldUser().getText();
	fieldPassword = new String(getJPasswordFieldPassword().getPassword());
}
		
	public void setLoggedInUser(User loggedInUser){
		getJButtonRegister().setEnabled(loggedInUser == null);
	}
	/**
	 * @return
	 */
	protected JButton getLostPasswordJButton() {
		if (lostPasswordJButton == null) {
			lostPasswordJButton = new JButton();
			lostPasswordJButton.setText("Forgot Login Password...");
		}
		return lostPasswordJButton;
	}
	
}
