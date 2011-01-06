package cbit.vcell.client.desktop.biomodel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.vcell.sybil.models.miriam.MIRIAMQualifier;
import org.vcell.util.DataAccessException;
import org.vcell.util.document.BioModelChildSummary;
import org.vcell.util.document.BioModelInfo;
import org.vcell.util.document.Version;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.JLabelLikeTextField;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.meta.MiriamManager.MiriamRefGroup;
import cbit.vcell.biomodel.meta.MiriamManager.MiriamResource;
import cbit.vcell.client.BioModelWindowManager;
import cbit.vcell.clientdb.DatabaseEvent;
import cbit.vcell.clientdb.DatabaseListener;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.xml.gui.MiriamTreeModel;
import cbit.vcell.xml.gui.MiriamTreeModel.LinkNode;
/**
 * Insert the type's description here.
 * Creation date: (2/3/2003 2:07:01 PM)
 * @author: Frank Morgan
 */
@SuppressWarnings("serial")
public class BioModelPropertiesPanel extends JPanel {
	
	private BioModel bioModel = null;
	private EventHandler eventHandler = new EventHandler();
	private JLabelLikeTextField nameLabel, ownerLabel, lastModifiedLabel, permissionLabel;
	private JButton changePermissionButton;
	private BioModelWindowManager bioModelWindowManager;
	private JPanel applicationsPanel = null;
	private JPanel webLinksPanel = null;
	private Icon geometryIcon = new ImageIcon(getClass().getResource("/images/geometry2_16x16.gif"));
	private Icon appTypeIcon = new ImageIcon(getClass().getResource("/images/type.gif"));

	private class EventHandler implements ActionListener, DatabaseListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == changePermissionButton) {
				changePermissions();
			}			
		}
		public void databaseDelete(DatabaseEvent event) {			
		}
		public void databaseInsert(DatabaseEvent event) {
		}
		public void databaseRefresh(DatabaseEvent event) {
		}
		public void databaseUpdate(DatabaseEvent event) {
			updateInterface();			
		}
	}

/**
 * EditSpeciesDialog constructor comment.
 */
public BioModelPropertiesPanel() {
	super();
	initialize();
}

public void changePermissions() {
	if (bioModel == null || bioModel.getVersion() == null) {
		return;
	}
	bioModelWindowManager.getRequestManager().accessPermissions(this, bioModel);	
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
		nameLabel = new JLabelLikeTextField();
		ownerLabel = new JLabelLikeTextField();
		lastModifiedLabel = new JLabelLikeTextField();
		permissionLabel = new JLabelLikeTextField();
		changePermissionButton = new JButton("Change Permissions...");
		changePermissionButton.setEnabled(false);
		applicationsPanel = new JPanel(new GridBagLayout());
		applicationsPanel.setBackground(Color.white);
		webLinksPanel = new JPanel();
		webLinksPanel.setBackground(Color.white);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBackground(Color.white);
		int gridy = 0;
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;		
		gbc.insets = new Insets(10, 4, 4, 4);
		JLabel label = new JLabel("Saved BioModel Info");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		mainPanel.add(label, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(10, 10, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		label = new JLabel("BioModel Name:");
		mainPanel.add(label, gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		mainPanel.add(nameLabel, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		label = new JLabel("Owner:");
		mainPanel.add(label, gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		mainPanel.add(ownerLabel, gbc);

		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		label = new JLabel("Last Modified:");
		mainPanel.add(label, gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		mainPanel.add(lastModifiedLabel, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;		
		label = new JLabel("Web Links:");
		mainPanel.add(label, gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		mainPanel.add(webLinksPanel, gbc);				
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		label = new JLabel("Permissions:");
		mainPanel.add(label, gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(permissionLabel, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		mainPanel.add(changePermissionButton, gbc);

		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;		
		label = new JLabel("Applications:");
		mainPanel.add(label, gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;	
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(4, 4, 20, 10);
		mainPanel.add(applicationsPanel, gbc);

		setLayout(new BorderLayout());
		add(new JScrollPane(mainPanel), BorderLayout.CENTER);
		changePermissionButton.addActionListener(eventHandler);
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
		javax.swing.JFrame frame = new javax.swing.JFrame();
		BioModelPropertiesPanel aEditSpeciesPanel = new BioModelPropertiesPanel();
		frame.add(aEditSpeciesPanel);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.pack();
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of cbit.gui.JInternalFrameEnhanced");
		exception.printStackTrace(System.out);
	}
}

/**
 * Sets the speciesContext property (cbit.vcell.model.SpeciesContext) value.
 * @param speciesContext The new value for the property.
 * @see #getSpeciesContext
 */
public void setBioModel(BioModel newValue) {
	if (newValue == bioModel) {
		return;
	}
	bioModel = newValue;
	updateInterface();
}

public void setBioModelWindowManager(BioModelWindowManager bioModelWindowManager) {
	this.bioModelWindowManager = bioModelWindowManager;
	bioModelWindowManager.getRequestManager().getDocumentManager().addDatabaseListener(eventHandler);
	updateInterface();
}
/**
 * Comment
 */
private void updateInterface() {
	if (bioModel == null || bioModelWindowManager == null) {
		return;
	}
	nameLabel.setText(bioModel.getName());
	
	Version version = bioModel.getVersion();
	if (version != null) {
		ownerLabel.setText(version.getOwner().getName());
		lastModifiedLabel.setText(version.getDate().toString());
		try {
			BioModelInfo bioModelInfo = bioModelWindowManager.getRequestManager().getDocumentManager().getBioModelInfo(version.getVersionKey());
			permissionLabel.setText(bioModelInfo.getVersion().getGroupAccess().getDescription());
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		changePermissionButton.setEnabled(true);
	}
	webLinksPanel.removeAll();
	webLinksPanel.setLayout(new GridLayout(0,1));
	Set<MiriamRefGroup> resources = new HashSet<MiriamRefGroup>();
	Set<MiriamRefGroup> isDescribedByAnnotation = bioModel.getVCMetaData().getMiriamManager().getMiriamRefGroups(bioModel, MIRIAMQualifier.MODEL_isDescribedBy);
	Set<MiriamRefGroup> isAnnotation = bioModel.getVCMetaData().getMiriamManager().getMiriamRefGroups(bioModel, MIRIAMQualifier.MODEL_is);
	resources.addAll(isDescribedByAnnotation);
	resources.addAll(isAnnotation);
	for (MiriamRefGroup refGroup : resources){
		for (MiriamResource miriamResources : refGroup.getMiriamRefs()){
			LinkNode linkNode = new MiriamTreeModel.LinkNode(MIRIAMQualifier.MODEL_isDescribedBy, miriamResources);
			final String link = linkNode.getLink();
			String labelText = miriamResources.getDataType() == null ? "" : miriamResources.getDataType().getDataTypeName();
			String toolTip = null;
			if (link != null) {
				toolTip = "double-click to open link " + link;
				labelText = "<html><b>"+ labelText + "</b>&nbsp;" + "<font color=blue><a href=" + link + ">" + link + "</a></font></html>";
			}
			JLabel label = new JLabel(labelText);
			label.addMouseListener(new MouseListener() {				
				public void mouseReleased(MouseEvent e) {					
				}				
				public void mousePressed(MouseEvent e) {					
				}				
				public void mouseExited(MouseEvent e) {
				}				
				public void mouseEntered(MouseEvent e) {
				}				
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						DialogUtils.browserLauncher(BioModelPropertiesPanel.this, link, "failed to open " + link, false);
					}
				}
			});
			label.setToolTipText(toolTip);
			webLinksPanel.add(label);
		}
	}	
	
	applicationsPanel.removeAll();
	int gridy = 0;
	SimulationContext[] simulationContexts = bioModel.getSimulationContexts();
	if (simulationContexts != null) {
		for (int i = 0; i < simulationContexts.length; i ++) {
			SimulationContext simContext = simulationContexts[i];
			JLabel label = new JLabel(simContext.getName());
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			
			GridBagConstraints gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; 
			gbc.gridy = gridy ++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			if (i > 0) {
				gbc.insets = new Insets(4, 0, 0, 0);
			}
			applicationsPanel.add(label, gbc);

			Geometry geometry = simContext.getGeometry();
			String geometryText = "Compartmental geometry";
			if (geometry != null) {
				Version geometryVersion = geometry.getVersion();
				int dimension = geometry.getDimension();
				if (dimension > 0){
					String description = geometry.getDimension() + "D " + (geometry.getGeometrySpec().hasImage() ? "image" : "analytic") + " geometry";
					geometryText = description;
					if (geometryVersion != null) {
						geometryText += " - " + geometryVersion.getName()/* + " ("+geometryVersion.getDate() + ")"*/;
					}
				}
			}
			JLabel geometryLabel = new JLabel(geometryText);
			geometryLabel.setIcon(geometryIcon);
			JLabel detStochLabel = new JLabel((simContext.isStoch() ? BioModelChildSummary.TYPE_STOCH_STR : BioModelChildSummary.TYPE_DETER_STR));
			detStochLabel.setIcon(appTypeIcon);
			
			gbc.insets = new Insets(2, 20, 2, 2);
			gbc.gridy = gridy ++;
			applicationsPanel.add(detStochLabel, gbc);
			gbc.gridy = gridy ++;
			if (i == simulationContexts.length - 1) {
				gbc.weighty = 1.0;
			}
			applicationsPanel.add(geometryLabel, gbc);
		}
	}
}

}
