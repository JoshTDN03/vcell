/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.desktop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.vcell.util.document.VCellSoftwareVersion;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.KeySequenceListener;

@SuppressWarnings("serial")
public class DocumentWindowAboutBox extends JPanel {

	private static final String COPASI_WEB_URL = "http://www.copasi.org";
	private static final String SMOLDYN_WEB_URL = "http://www.smoldyn.org";
	private static final String VCELL_WEB_URL = "http://www.vcell.org";
	private JLabel appName = null;
	private JLabel copyright = null;
	private JLabel iconLabel = null;
	private JLabel version = null;
	private static String VERSION_NO = "";
	private static String BUILD_NO = "";
	private static String EDITION = "";
	private JLabel buildNumber = null;

	public static void parseVCellVersion() {
		try {
			VCellSoftwareVersion vcellSoftwareVersion = VCellSoftwareVersion.fromSystemProperty();
			EDITION = vcellSoftwareVersion.getSite().name().toUpperCase();
			VERSION_NO = vcellSoftwareVersion.getVersionNumber();
			BUILD_NO = vcellSoftwareVersion.getBuildNumber();
		} catch (Exception exc) {
			System.out.println("Failed to parse vcell.softwareVersion: " + exc.getMessage());
			exc.printStackTrace(System.out);
		}
	}
	
	public static String getVERSION_NO() {
		return VERSION_NO;
	}

	public static String getBUILD_NO() {
		return BUILD_NO;
	}

	public static String getEDITION() {
		return EDITION;
	}
	
	public static class HierarchyPrinter extends KeySequenceListener {
		
		public String getSequence() { return "hierarchy"; }
		
		public void sequenceTyped() {
			for(Frame frame : Frame.getFrames()) {
				printHierarchy(frame, "");
			}
		}
		
		public void printHierarchy(Component component, String indentation) {
			System.out.println(indentation + component);
			if(component instanceof Container) {
				for(Component child : ((Container) component).getComponents()) {
					printHierarchy(child, indentation + "  ");
				}
				
			}
		}
		
	}
	
	public DocumentWindowAboutBox() {
		super();
		initialize();
		setFocusable(true);
		addKeyListener(new HierarchyPrinter());
	}

	private JLabel getAppName() {
		if (appName == null) {
			try {
				appName = new JLabel();
				appName.setName("AppName");
				appName.setText("<html><u>Virtual Cell</u></html>");
				appName.setForeground(Color.blue);
				appName.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						DialogUtils.browserLauncher(DocumentWindowAboutBox.this, VCELL_WEB_URL, "Failed to open VCell web page (" + VCELL_WEB_URL + ")", false);
					}
				});
			} catch (Throwable throwable) {
				handleException(throwable);
			}
		}
		return appName;
	}

	public JLabel getBuildNumber() {
		if (buildNumber == null) {
			try {
				buildNumber = new JLabel();
				buildNumber.setName("BuildNumber");
				buildNumber.setText("");
			} catch (Throwable throwable) {
				handleException(throwable);
			}
		}
		return buildNumber;
	}

	private JLabel getCopyright() {
		if (copyright == null) {
			try {
				copyright = new JLabel();
				copyright.setName("Copyright");
				copyright.setText("(c) Copyright 1998-2011 UCHC");
			} catch (Throwable throwable) {
				handleException(throwable);
			}
		}
		return copyright;
	}

	private JLabel getIconLabel() {
		if (iconLabel == null) {
			try {
				iconLabel = new JLabel();
				iconLabel.setName("IconLabel");
//				iconLabel.seth
				iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/ccam_sm_colorgr.gif")));
				iconLabel.setText("");
			} catch (Throwable throwable) {
				handleException(throwable);
			}
		}
		return iconLabel;
	}
	
	private JLabel getCOPASIAttribution() {
		JLabel copasiText = new JLabel();
			try {
				
				copasiText.setName("COPASI");
				copasiText.setText("<html>Featuring <font color=blue><u>COPASI</u></font> parameter estimation technology&nbsp;&nbsp;</html>");
				copasiText.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						DialogUtils.browserLauncher(DocumentWindowAboutBox.this, COPASI_WEB_URL, "Failed to open COPASI webpage ("+COPASI_WEB_URL+")", false);
					}
				});
			} catch (Throwable throwable) {
				handleException(throwable);
			}
		
		return copasiText;
	}
	
	private JLabel getSmoldynAttribution() {
		JLabel smoldynText = new JLabel();
			try {
				
				smoldynText.setName("SMOLDYN");
				smoldynText.setText("<html>Featuring spatial stochastic simulation powered by <font color=blue><u>SMOLDYN</u></font></html>");
				smoldynText.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						DialogUtils.browserLauncher(DocumentWindowAboutBox.this, SMOLDYN_WEB_URL, "Failed to open SMOLDYN webpage ("+SMOLDYN_WEB_URL+")", false);
					}
				});
			} catch (Throwable throwable) {
				handleException(throwable);
			}
		
		return smoldynText;
	}
	
	public JLabel getVersion() {
		if (version == null) {
			try {
				version = new JLabel();
				version.setName("Version");
				version.setText("Version 4.0");
			} catch (Throwable throwable) {
				handleException(throwable);
			}
		}
		return version;
	}

	private void handleException(Throwable exception) {
		System.out.println("--------- UNCAUGHT EXCEPTION ---------");
		exception.printStackTrace(System.out);
	}

	private void initialize() {
		try {
			setLayout(new GridBagLayout());
			
			int gridy = 0;
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.insets = new Insets(0,0,4,4);
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			add(getIconLabel(), gbc);
			
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.insets = new Insets(2,4,0,4);
			add(getAppName(), gbc);
	
			gridy ++;
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(0,4,0,4);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_START;
			add(getVersion(), gbc);

			gridy ++;
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(0,4,0,4);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_START;
			add(getBuildNumber(), gbc);
			
			gridy ++;
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(0,4,0,4);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_START;
			add(getCopyright(), gbc);

			gridy ++;
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(10,4,0,4);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_START;
			add(getCOPASIAttribution(), gbc);
			
			gridy ++;
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(10,4,0,4);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_START;
			add(getSmoldynAttribution(), gbc);
			
		} catch (Throwable throwable) {
			handleException(throwable);
		}
	}
}