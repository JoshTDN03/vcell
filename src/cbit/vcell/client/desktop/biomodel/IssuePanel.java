package cbit.vcell.client.desktop.biomodel;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.vcell.util.Issue;
import org.vcell.util.gui.DefaultScrollTableCellRenderer;
import org.vcell.util.gui.VCellIcons;
import org.vcell.util.gui.sorttable.JSortTable;

import cbit.vcell.client.desktop.biomodel.DocumentEditorTreeModel.DocumentEditorTreeFolderClass;
import cbit.vcell.client.desktop.biomodel.SelectionManager.ActiveView;
import cbit.vcell.client.desktop.biomodel.SelectionManager.ActiveViewID;
import cbit.vcell.client.desktop.mathmodel.MathModelEditor;
import cbit.vcell.document.SimulationOwner;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.mapping.GeometryContext;
import cbit.vcell.mapping.GeometryContext.UnmappedGeometryClass;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.SimulationContext.SimulationContextNameScope;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.mapping.StructureMapping.StructureMappingNameScope;
import cbit.vcell.math.MathDescription;
import cbit.vcell.math.OutputFunctionContext.OutputFunctionIssueSource;
import cbit.vcell.mathmodel.MathModel;
import cbit.vcell.model.Parameter;

@SuppressWarnings("serial")
public class IssuePanel extends DocumentEditorSubPanel {

	private JSortTable issueTable = null;
	private IssueTableModel issueTableModel = null;
	private JButton refreshButton = null;
	private JCheckBox showWarningCheckBox;
	
	public IssuePanel() {
		super();
		initialize();
	}
	
	@Override
	public void setIssueManager(IssueManager issueManager) {
		super.setIssueManager(issueManager);
		issueTableModel.setIssueManager(issueManager);
	}
	
	@Override
	protected void onSelectedObjectsChange(Object[] selectedObjects) {	
	}	
	
	private void initialize() {
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (issueManager != null) {
					issueManager.updateIssues();
				}			
			}
		});
		showWarningCheckBox = new JCheckBox("Show Warnings");
		showWarningCheckBox.setSelected(true);
		showWarningCheckBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				issueTableModel.setShowWarning(showWarningCheckBox.isSelected());
				
			}
		});
		issueTable = new JSortTable();
		issueTableModel = new IssueTableModel(issueTable);
		issueTable.setModel(issueTableModel);
		issueTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = issueTable.getSelectedRow();
					if (row >= 0) {
						Issue issue = issueTableModel.getValueAt(row);
						Object object = issue.getSource();
						if (object instanceof Parameter) {
							setActiveView(new ActiveView(null, DocumentEditorTreeFolderClass.BIOMODEL_PARAMETERS_NODE, ActiveViewID.parameters_functions));
							setSelectedObjects(new Object[] {object});
						} else if (object instanceof StructureMapping) {
							StructureMapping structureMapping = (StructureMapping) object;
							StructureMappingNameScope structureMappingNameScope = (StructureMappingNameScope)structureMapping.getNameScope();
							SimulationContext simulationContext = ((SimulationContextNameScope)(structureMappingNameScope.getParent())).getSimulationContext();
							setActiveView(new ActiveView(simulationContext, DocumentEditorTreeFolderClass.GEOMETRY_NODE, ActiveViewID.structure_mapping));
							setSelectedObjects(new Object[] {object});
						} else if (object instanceof GeometryContext.UnmappedGeometryClass) {
							UnmappedGeometryClass unmappedGeometryClass = (UnmappedGeometryClass) object;
							SimulationContext simulationContext = unmappedGeometryClass.getSimulationContext();
							setActiveView(new ActiveView(simulationContext, DocumentEditorTreeFolderClass.GEOMETRY_NODE, ActiveViewID.structure_mapping));
							setSelectedObjects(new Object[] {object});
						} else if (object instanceof OutputFunctionIssueSource) {
							SimulationOwner simulationOwner = ((OutputFunctionIssueSource)object).getOutputFunctionContext().getSimulationOwner();
							if (simulationOwner instanceof SimulationContext) {
								SimulationContext simulationContext = (SimulationContext) simulationOwner;
								setActiveView(new ActiveView(simulationContext, DocumentEditorTreeFolderClass.SIMULATIONS_NODE, ActiveViewID.output_functions));								
							} else if (simulationOwner instanceof MathModel) {
								setActiveView(new ActiveView(null, DocumentEditorTreeFolderClass.MATH_OUTPUT_FUNCTIONS_NODE, ActiveViewID.math_output_functions));
							}
							setSelectedObjects(new Object[] {((OutputFunctionIssueSource)object).getAnnotatedFunction()});
						} 	else if (object instanceof GeometryContext) {
							setActiveView(new ActiveView(((GeometryContext)object).getSimulationContext(), DocumentEditorTreeFolderClass.GEOMETRY_NODE, ActiveViewID.geometry_definition));
						}else {
							boolean bInMathModelEditor = false;
							boolean bInBioModelEditor = false;
							for (Component c = IssuePanel.this; c != null; c = c.getParent()) {
								if (c instanceof MathModelEditor) {
									bInMathModelEditor = true;
									break;
								} else if (c instanceof BioModelEditor) {
									bInBioModelEditor = true;
									break;
								}
							}
							if (bInMathModelEditor) {
								if (object instanceof Geometry) {
									setActiveView(new ActiveView(null, DocumentEditorTreeFolderClass.MATH_GEOMETRY_NODE, ActiveViewID.math_geometry));								
								} else if (object instanceof OutputFunctionIssueSource) {
									setActiveView(new ActiveView(null, DocumentEditorTreeFolderClass.MATH_OUTPUT_FUNCTIONS_NODE, ActiveViewID.math_output_functions));
								} else {
									setActiveView(new ActiveView(null, DocumentEditorTreeFolderClass.MATH_VCML_NODE, ActiveViewID.math_vcml));
								}
							}
						}
					}
				}
			}			
		});
		
		setLayout(new GridBagLayout());
		int gridy = 0;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy;
		gbc.insets = new Insets(0,10,0,0);
		add(showWarningCheckBox, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = new Insets(0,0,0,10);
		add(refreshButton, gbc);
		
		gridy ++;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		add(issueTable.getEnclosingScrollPane(), gbc);
		
		DefaultTableCellRenderer tableRenderer = new DefaultScrollTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus,	row, column);
				setIcon(null);
				switch (column) {
				case IssueTableModel.COLUMN_DESCRIPTION: {
					Issue issue = (Issue)value;
					int severity = issue.getSeverity();
					Icon icon = null;
					switch (severity) {
					case Issue.SEVERITY_INFO:
						icon = VCellIcons.getInfoIcon();
						break;
					case Issue.SEVERITY_WARNING:
						icon = VCellIcons.getWarningIcon();
						break;					
					case Issue.SEVERITY_ERROR:
						icon = VCellIcons.getErrorIcon();
						break;
					}
					setIcon(icon);
					setText(issue.getMessage());
					break;
				}								
				}
				return this;
			}			
		};
		issueTable.getColumnModel().getColumn(IssueTableModel.COLUMN_DESCRIPTION).setCellRenderer(tableRenderer);
	}
}
