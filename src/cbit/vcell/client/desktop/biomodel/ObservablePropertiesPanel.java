/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.desktop.biomodel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.vcell.model.rbm.ComponentStateDefinition;
import org.vcell.model.rbm.ComponentStatePattern;
import org.vcell.model.rbm.MolecularComponent;
import org.vcell.model.rbm.MolecularComponentPattern;
import org.vcell.model.rbm.MolecularType;
import org.vcell.model.rbm.MolecularTypePattern;
import org.vcell.model.rbm.RbmElementAbstract;
import org.vcell.model.rbm.SpeciesPattern;
import org.vcell.model.rbm.MolecularComponentPattern.BondType;
import org.vcell.model.rbm.SpeciesPattern.Bond;

import cbit.vcell.model.RbmObservable;
import cbit.vcell.model.common.VCellErrorMessages;

import org.vcell.util.Compare;
import org.vcell.util.document.PropertyConstants;
import org.vcell.util.gui.GuiUtils;
import org.vcell.util.gui.VCellIcons;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.meta.VCMetaData;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.client.desktop.biomodel.RbmDefaultTreeModel.SpeciesPatternLocal;
import cbit.vcell.desktop.BioModelNode;
import cbit.vcell.graph.MolecularComponentLargeShape;
import cbit.vcell.graph.MolecularTypeSmallShape;
import cbit.vcell.graph.PointLocationInShapeContext;
import cbit.vcell.graph.SpeciesPatternLargeShape;
import cbit.vcell.graph.MolecularTypeLargeShape;
import cbit.vcell.graph.MolecularComponentLargeShape.ComponentStateLargeShape;


@SuppressWarnings("serial")
public class ObservablePropertiesPanel extends DocumentEditorSubPanel {
	
	private class InternalEventHandler implements PropertyChangeListener, ActionListener, MouseListener, TreeSelectionListener,
		TreeWillExpandListener, FocusListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == observable) {
				if (evt.getPropertyName().equals(PropertyConstants.PROPERTY_NAME_NAME)) {
					updateInterface();
				}
			}
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == getAddSpeciesPatternFromTreeMenuItem()) {
				addSpeciesPattern();
			} else if (source == getDeleteFromTreeMenuItem()) {
				deleteFromTree();
			} else if (source == getRenameFromTreeMenuItem()) {
				observableTree.startEditingAtPath(observableTree.getSelectionPath());
			} else if (source == getAddFromTreeMenu()) {
				addNewFromTree();
			} else if (source == getEditFromTreeMenuItem()) {
				observableTree.startEditingAtPath(observableTree.getSelectionPath());
			} else if (e.getSource() == getAddSpeciesPatternFromShapeMenuItem()) {
				addSpeciesPattern();
			} else if (source == getDeleteFromShapeMenuItem()) {
				deleteFromShape();
			} else if (source == getRenameFromShapeMenuItem()) {
				
//				observableTree.startEditingAtPath(observableTree.getSelectionPath());
			} else if (source == getAddFromShapeMenu()) {
				addNewFromShape();
			} else if (source == getEditFromShapeMenuItem()) {
				
//				observableTree.startEditingAtPath(observableTree.getSelectionPath());
			}

			
		}
		@Override
		public void mouseClicked(MouseEvent e) {
//			System.out.println("click! " + e.getSource());
		}
		@Override
		public void mousePressed(MouseEvent e) {
			if (!e.isConsumed() && e.getSource() == observableTree) {
				showPopupMenu(e);
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (!e.isConsumed() && e.getSource() == observableTree) {
				showPopupMenu(e);
			}			
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
//			super.mouseExited(e);
			if(e.getSource() == annotationTextArea){
				changeFreeTextAnnotation();
			}
		}
		@Override
		public void valueChanged(TreeSelectionEvent e) {
		}
		@Override
		public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
			boolean veto = false;
			if (veto) {
				throw new ExpandVetoException(e);
			}
		}
		@Override
		public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
			JTree tree = (JTree) e.getSource();
			TreePath path = e.getPath();
			boolean veto = false;
			if(path.getParentPath() == null) {
				veto = true;
			}
			if (veto) {
				throw new ExpandVetoException(e);
			}
		}
		@Override
		public void focusGained(FocusEvent e) {
		}
		@Override
		public void focusLost(FocusEvent e) {
			if (e.getSource() == annotationTextArea) {
				changeFreeTextAnnotation();
			}
		}
	}
	
	private JTree observableTree = null;
	private ObservableTreeModel observableTreeModel = null;
	private RbmObservable observable;
	private JLabel titleLabel = null;
	private JTextArea annotationTextArea;

	private InternalEventHandler eventHandler = new InternalEventHandler();
	
	JPanel shapePanel;
	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);	// between tree and right side
	private JSplitPane splitPaneHorizontal = new JSplitPane(JSplitPane.VERTICAL_SPLIT);	// between shape and annotation

	List<SpeciesPatternLargeShape> spsList = new ArrayList<SpeciesPatternLargeShape>();

	private JPopupMenu popupFromTreeMenu;
	private JMenu addFromTreeMenu;
	private JMenuItem deleteFromTreeMenuItem;	
	private JMenuItem renameFromTreeMenuItem;
	private JMenuItem editFromTreeMenuItem;
	private JMenuItem addSpeciesPatternFromTreeMenuItem;
	
	private JPopupMenu popupFromShapeMenu;
	private JMenu addFromShapeMenu;
	private JMenuItem deleteFromShapeMenuItem;	
	private JMenuItem renameFromShapeMenuItem;
	private JMenuItem editFromShapeMenuItem;
	private JMenuItem addSpeciesPatternFromShapeMenuItem;
//	private JCheckBox showDetailsCheckBox;
	
	private BioModel bioModel;
	public ObservablePropertiesPanel() {
		super();
		initialize();
	}

	
	public void addSpeciesPattern() {
		SpeciesPattern sp = new SpeciesPattern();
		observable.addSpeciesPattern(sp);
		final TreePath path = observableTreeModel.findObjectPath(null, observable);
		observableTree.setSelectionPath(path);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {				
				observableTree.scrollPathToVisible(path);
			}
		});
	}
	
	public void addNewFromShape() {
		
	}
	public void addNewFromTree() {
		Object obj = observableTree.getLastSelectedPathComponent();
		if (obj == null || !(obj instanceof BioModelNode)) {
			return;
		}
		BioModelNode selectedNode = (BioModelNode) obj;
		Object selectedUserObject = selectedNode.getUserObject();
		if (selectedUserObject == observable){
			for (MolecularType mt : bioModel.getModel().getRbmModelContainer().getMolecularTypeList()) {
				JMenuItem menuItem = new JMenuItem(mt.getName());
				menuItem.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						;
					}
				});
			}
		} else if (selectedUserObject instanceof MolecularComponent){
			;
		}	
	}

	public void deleteFromShape() {

	}
	public void deleteFromTree() {
		Object obj = observableTree.getLastSelectedPathComponent();
		if (obj == null || !(obj instanceof BioModelNode)) {
			return;
		}
		BioModelNode selectedNode = (BioModelNode) obj;
		TreeNode parent = selectedNode.getParent();
		if (!(parent instanceof BioModelNode)) {
			return;
		}
		BioModelNode parentNode = (BioModelNode) parent;
		Object selectedUserObject = selectedNode.getUserObject();
		
		if(selectedUserObject instanceof SpeciesPatternLocal) {
			System.out.println("deleting species pattern local");
			Object parentUserObject = parentNode.getUserObject();
			SpeciesPatternLocal spl = (SpeciesPatternLocal)selectedUserObject;
			RbmObservable o = (RbmObservable)parentUserObject;
			List<SpeciesPattern> speciesPatternList = o.getSpeciesPatternList();
			speciesPatternList.remove(spl.speciesPattern);
			final TreePath path = observableTreeModel.findObjectPath(null, observable);
			observableTree.setSelectionPath(path);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					observableTreeModel.populateTree();			// repaint tree
					observableTree.scrollPathToVisible(path);	// scroll back up to show the observable
				}
			});
		} else if (selectedUserObject instanceof MolecularTypePattern){
			System.out.println("deleting molecular type pattern");
			MolecularTypePattern mtp = (MolecularTypePattern) selectedUserObject;
			Object parentUserObject = parentNode.getUserObject();
			SpeciesPatternLocal spl = (SpeciesPatternLocal)parentUserObject;
			SpeciesPattern sp = spl.speciesPattern;
			sp.removeMolecularTypePattern(mtp);
			if(!sp.getMolecularTypePatterns().isEmpty()) {
				sp.resolveBonds();
			}
			final TreePath path = observableTreeModel.findObjectPath(null, spl);
			observableTree.setSelectionPath(path);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					observableTreeModel.populateTree();
					observableTree.scrollPathToVisible(path);	// this doesn't seem to work ?
				}
			});
		} else if(selectedUserObject instanceof MolecularComponentPattern) {
			MolecularComponentPattern mcp = (MolecularComponentPattern) selectedUserObject;
			Object parentUserObject = parentNode.getUserObject();
			MolecularTypePattern mtp = (MolecularTypePattern)parentUserObject;
			mtp.removeMolecularComponentPattern(mcp);
			System.out.println("deleting MolecularComponentPattern " + mcp.getMolecularComponent().getName());
			parent = parentNode.getParent();
			parentNode = (BioModelNode) parent;
			parentUserObject = parentNode.getUserObject();
			SpeciesPatternLocal spl = (SpeciesPatternLocal)parentUserObject;
			SpeciesPattern sp = spl.speciesPattern;
			if(!sp.getMolecularTypePatterns().isEmpty()) {
				sp.resolveBonds();
			}
			final TreePath path = observableTreeModel.findObjectPath(null, spl);
			observableTree.setSelectionPath(path);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					observableTreeModel.populateTree();
					observableTree.scrollPathToVisible(path);	// this doesn't seem to work ?
				}
			});
		} else {
			System.out.println("deleting " + selectedUserObject.toString());
		}
	}

	private void initialize() {
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setBackground(Color.white);	
		
		observableTree = new BioModelNodeEditableTree();
		observableTreeModel = new ObservableTreeModel(observableTree);
		observableTree.setModel(observableTreeModel);
		observableTreeModel.setShowDetails(true);
		
		RbmObservableTreeCellRenderer cro = new RbmObservableTreeCellRenderer(observableTree);
		observableTree.setCellRenderer(cro);
		DisabledTreeCellEditor dtce =  new DisabledTreeCellEditor(observableTree, (cro));
		observableTree.setCellEditor(dtce);
		observableTree.setEditable(false);
		
		int rowHeight = observableTree.getRowHeight();
		if (rowHeight < 10) { 
			rowHeight = 20; 
		}
		observableTree.setRowHeight(rowHeight + 5);
		observableTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		ToolTipManager.sharedInstance().registerComponent(observableTree);
		observableTree.addTreeSelectionListener(eventHandler);
		observableTree.addTreeWillExpandListener(eventHandler);
		observableTree.addMouseListener(eventHandler);
		observableTree.setLargeModel(true);
		observableTree.setRootVisible(true);
		
		setLayout(new GridBagLayout());
		
		int gridy = 0;
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(2,2,0,2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		titleLabel = new JLabel("Observable");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		leftPanel.add(titleLabel, gbc);
		
		gridy ++;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(2,2,2,2);
		gbc.fill = GridBagConstraints.BOTH;
		leftPanel.add(new JScrollPane(observableTree), gbc);
		
// --------------------------------------------------------------------------------------------------------	
		
		splitPaneHorizontal.setOneTouchExpandable(true);
		splitPaneHorizontal.setDividerLocation(120);
		splitPaneHorizontal.setResizeWeight(0.1);
		
		Border border = BorderFactory.createLineBorder(Color.gray);
		Border loweredEtchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border loweredBevelBorder = BorderFactory.createLoweredBevelBorder();

		TitledBorder annotationBorder = BorderFactory.createTitledBorder(loweredEtchedBorder, " Annotation ");
		annotationBorder.setTitleJustification(TitledBorder.LEFT);
		annotationBorder.setTitlePosition(TitledBorder.TOP);
		annotationBorder.setTitleFont(getFont().deriveFont(Font.BOLD));
		
//		JScrollPane p = new JScrollPane(shapePanel);
//		p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//		p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		shapePanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				for(SpeciesPatternLargeShape sps : spsList) {
					if(sps == null) {
						continue;
					}
					sps.paintSelf(g);
				}
			}
		};
		shapePanel.setBorder(border);
		shapePanel.setBackground(Color.white);
		shapePanel.setLayout(null);
		
		shapePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if(e.getButton() == 1) {					// left click selects the object (we highlight it)
					Point whereClicked = e.getPoint();
					PointLocationInShapeContext locationContext = new PointLocationInShapeContext(whereClicked);
					manageMouseActivity(locationContext);
				} else if(e.getButton() == 3) {				// right click invokes popup menu (only if the object is highlighted)
					Point whereClicked = e.getPoint();
					PointLocationInShapeContext locationContext = new PointLocationInShapeContext(whereClicked);
					manageMouseActivity(locationContext);
					if(locationContext.getDeepestShape() != null && !locationContext.getDeepestShape().isHighlighted()) {
						// TODO: (maybe) add code here to highlight the shape if it's not highlighted already but don't show the menu
						// return;
					}					
					showPopupMenu(e, locationContext);
				}
			}
			private void manageMouseActivity(PointLocationInShapeContext locationContext) {
				Graphics g = shapePanel.getGraphics();
				for (SpeciesPatternLargeShape sps : spsList) {
					sps.turnHighlightOffRecursive(g);
				}
				for (SpeciesPatternLargeShape sps : spsList) {
					if (sps.contains(locationContext)) {		//check if mouse is inside shape
						break;
					}
				}
				locationContext.highlightDeepestShape();
				locationContext.paintDeepestShape(g);
			}
		});
//		shapePanel.addMouseListener(eventHandler);		// alternately use this
		
		JPanel generalPanel = new JPanel();		// right bottom panel, contains just the annotation
		generalPanel.setBorder(annotationBorder);
		generalPanel.setLayout(new GridBagLayout());

		gridy = 0;
		annotationTextArea = new javax.swing.JTextArea("", 1, 30);
		annotationTextArea.setLineWrap(true);
		annotationTextArea.setWrapStyleWord(true);
		annotationTextArea.setFont(new Font("monospaced", Font.PLAIN, 11));
		annotationTextArea.setEditable(false);
		javax.swing.JScrollPane jsp = new javax.swing.JScrollPane(annotationTextArea);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 0.1;
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		generalPanel.add(jsp, gbc);

		JScrollPane scrollPane = new JScrollPane(shapePanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		splitPaneHorizontal.setTopComponent(scrollPane);
		splitPaneHorizontal.setBottomComponent(generalPanel);

// -------------------------------------------------------------------------------------------------		
		splitPane.setOneTouchExpandable(true);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(splitPaneHorizontal);
		
		splitPane.setResizeWeight(0.0d);
		splitPane.getLeftComponent().setMinimumSize(new Dimension());
		splitPane.getLeftComponent().setPreferredSize(new Dimension());
		splitPane.setDividerLocation(0.0d);

		setName("ObservablePropertiesPanel");
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		setBackground(Color.white);
		
		annotationTextArea.addFocusListener(eventHandler);
		annotationTextArea.addMouseListener(eventHandler);
	}
	
	private JMenu getAddFromTreeMenu() {
		if (addFromTreeMenu == null) {
			addFromTreeMenu = new JMenu("Add");
			addFromTreeMenu.addActionListener(eventHandler);
		}
		return addFromTreeMenu;
	}
	private JMenu getAddFromShapeMenu() {
		if (addFromShapeMenu == null) {
			addFromShapeMenu = new JMenu("Add");
			addFromShapeMenu.addActionListener(eventHandler);
		}
		return addFromShapeMenu;
	}
	
	private JMenuItem getRenameFromTreeMenuItem() {
		if (renameFromTreeMenuItem == null) {
			renameFromTreeMenuItem = new JMenuItem("Rename");
			renameFromTreeMenuItem.addActionListener(eventHandler);
		}
		return renameFromTreeMenuItem;
	}
	private JMenuItem getRenameFromShapeMenuItem() {
		if (renameFromShapeMenuItem == null) {
			renameFromShapeMenuItem = new JMenuItem("Rename");
			renameFromShapeMenuItem.addActionListener(eventHandler);
		}
		return renameFromShapeMenuItem;
	}
	
	private JMenuItem getDeleteFromTreeMenuItem() {
		if (deleteFromTreeMenuItem == null) {
			deleteFromTreeMenuItem = new JMenuItem("Delete");
			deleteFromTreeMenuItem.addActionListener(eventHandler);
		}
		return deleteFromTreeMenuItem;
	}
	private JMenuItem getDeleteFromShapeMenuItem() {
		if (deleteFromShapeMenuItem == null) {
			deleteFromShapeMenuItem = new JMenuItem("Delete");
			deleteFromShapeMenuItem.addActionListener(eventHandler);
		}
		return deleteFromShapeMenuItem;
	}
	
	private JMenuItem getEditFromTreeMenuItem() {
		if (editFromTreeMenuItem == null) {
			editFromTreeMenuItem = new JMenuItem("Edit");
			editFromTreeMenuItem.addActionListener(eventHandler);
		}
		return editFromTreeMenuItem;
	}
	private JMenuItem getEditFromShapeMenuItem() {
		if (editFromShapeMenuItem == null) {
			editFromShapeMenuItem = new JMenuItem("Edit");
			editFromShapeMenuItem.addActionListener(eventHandler);
		}
		return editFromShapeMenuItem;
	}
	
	private JMenuItem getAddSpeciesPatternFromTreeMenuItem() {
		if (addSpeciesPatternFromTreeMenuItem == null) {
			addSpeciesPatternFromTreeMenuItem = new JMenuItem("Add Species Pattern");
			addSpeciesPatternFromTreeMenuItem.addActionListener(eventHandler);
		}
		return addSpeciesPatternFromTreeMenuItem;
	}
	private JMenuItem getAddSpeciesPatternFromShapeMenuItem() {
		if (addSpeciesPatternFromShapeMenuItem == null) {
			addSpeciesPatternFromShapeMenuItem = new JMenuItem("Add Species Pattern");
			addSpeciesPatternFromShapeMenuItem.addActionListener(eventHandler);
		}
		return addSpeciesPatternFromShapeMenuItem;
	}
	
	@Override
	protected void onSelectedObjectsChange(Object[] selectedObjects) {
		RbmObservable observable = null;
		if (selectedObjects.length == 1 && selectedObjects[0] instanceof RbmObservable) {
			observable = (RbmObservable) selectedObjects[0];
		}
		setObservable(observable);	
	}
	
	private void setObservable(RbmObservable newValue) {
		if (observable == newValue) {
			return;
		}
		RbmObservable oldValue = observable;
		if (oldValue != null) {
			oldValue.removePropertyChangeListener(eventHandler);
		}
		if (newValue != null) {
			newValue.addPropertyChangeListener(eventHandler);
		}
		observable = newValue;
		observableTreeModel.setObservable(observable);
		if(observable != null) {
			observable.setHighlightedRecursively(false);
		}
		updateInterface();
	}
	
	private void updateInterface() {
		boolean bNonNullObservable = observable != null && bioModel != null;
		annotationTextArea.setEditable(bNonNullObservable);
		if (bNonNullObservable) {
			VCMetaData vcMetaData = bioModel.getModel().getVcMetaData();
			annotationTextArea.setText(vcMetaData.getFreeTextAnnotation(observable));
		} else {
			annotationTextArea.setText(null);
		}
		updateTitleLabel();
		updateShape();
	}
	private void updateTitleLabel() {
		if (observable != null) {
			titleLabel.setText("Properties for Observable : " + observable.getName());
		}
	}
//	public static final int ReservedSpaceForNameOnYAxis = 20;	// enough to write some text above the shape
	public static final int xOffsetInitial = 25;
	public static final int ReservedSpaceForNameOnYAxis = 2;	// just a little empty spacing above the shape
	private void updateShape() {
		spsList.clear();
		int maxXOffset = xOffsetInitial;
		int maxYOffset = 88 + 80;
		if(observable != null && observable.getSpeciesPatternList() != null && observable.getSpeciesPatternList().size() > 0) {
			Graphics gc = splitPane.getRightComponent().getGraphics();
			for(int i = 0; i<observable.getSpeciesPatternList().size(); i++) {
				SpeciesPattern sp = observable.getSpeciesPatternList().get(i);
				SpeciesPatternLargeShape sps = new SpeciesPatternLargeShape(xOffsetInitial, 8+(80+ReservedSpaceForNameOnYAxis)*i, 80, sp, gc, observable);
				spsList.add(sps);
				int xOffset = sps.getRightEnd();
				maxXOffset = Math.max(maxXOffset, xOffset);
			}
			maxYOffset = Math.max(maxYOffset,8+(80+ReservedSpaceForNameOnYAxis)*observable.getSpeciesPatternList().size() + 80);
		}
		Dimension preferredSize = new Dimension(maxXOffset+200, maxYOffset);
		shapePanel.setPreferredSize(preferredSize);

		splitPane.getRightComponent().repaint();
	}
	private void changeFreeTextAnnotation() {
		try{
			if (observable == null) {
				return;
			}
			// set text from annotationTextField in free text annotation for species in vcMetaData (from model)
			if(bioModel.getModel() != null && bioModel.getModel().getVcMetaData() != null){
				VCMetaData vcMetaData = bioModel.getModel().getVcMetaData();
				String textAreaStr = (annotationTextArea.getText() == null || annotationTextArea.getText().length()==0?null:annotationTextArea.getText());
				if(!Compare.isEqualOrNull(vcMetaData.getFreeTextAnnotation(observable),textAreaStr)){
					vcMetaData.setFreeTextAnnotation(observable, textAreaStr);	
				}
			}
		} catch(Exception e){
			e.printStackTrace(System.out);
			PopupGenerator.showErrorDialog(this,"Edit Observable Error\n"+e.getMessage(), e);
		}
	}
	
	private void showPopupMenu(MouseEvent e, PointLocationInShapeContext locationContext) {
		if (popupFromShapeMenu == null) {
			popupFromShapeMenu = new JPopupMenu();			
		}		
		if (popupFromShapeMenu.isShowing()) {
			return;
		}
		boolean bDelete = false;
		boolean bAdd = false;
		boolean bEdit = false;
		boolean bRename = false;
		popupFromShapeMenu.removeAll();
		Point mousePoint = e.getPoint();

		final Object deepestShape = locationContext.getDeepestShape();
		final RbmElementAbstract selectedObject;
		
		if(deepestShape == null) {
			selectedObject = null;
			System.out.println("outside");		// when cursor is outside any species pattern we offer to add a new one
			popupFromShapeMenu.add(getAddSpeciesPatternFromShapeMenuItem());
		} else if(deepestShape instanceof ComponentStateLargeShape) {
			System.out.println("inside state");
			if(((ComponentStateLargeShape)deepestShape).isHighlighted()) {
				selectedObject = ((ComponentStateLargeShape)deepestShape).getComponentStatePattern();
			} else {
				return;
			}
		} else if(deepestShape instanceof MolecularComponentLargeShape) {
			System.out.println("inside component");
			if(((MolecularComponentLargeShape)deepestShape).isHighlighted()) {
				selectedObject = ((MolecularComponentLargeShape)deepestShape).getMolecularComponentPattern();
			} else {
				return;
			}
		} else if(deepestShape instanceof MolecularTypeLargeShape) {
			System.out.println("inside molecule");
			if(((MolecularTypeLargeShape)deepestShape).isHighlighted()) {
				selectedObject = ((MolecularTypeLargeShape)deepestShape).getMolecularTypePattern();
			} else {
				return;
			}
		} else if(deepestShape instanceof SpeciesPatternLargeShape) {
			System.out.println("inside species pattern");
			if(((SpeciesPatternLargeShape)deepestShape).isHighlighted()) {
				selectedObject = ((SpeciesPatternLargeShape)deepestShape).getSpeciesPattern();
			} else {
				return;
			}
		} else {
			selectedObject = null;
			System.out.println("inside something else?");
			return;
		}

		if(selectedObject instanceof SpeciesPattern) {
			getAddFromShapeMenu().setText(VCellErrorMessages.AddMolecularTypes);
			getAddFromShapeMenu().removeAll();
			for (final MolecularType mt : bioModel.getModel().getRbmModelContainer().getMolecularTypeList()) {
				JMenuItem menuItem = new JMenuItem(mt.getName());
				Graphics gc = splitPane.getRightComponent().getGraphics();
				Icon icon = new MolecularTypeSmallShape(1, 4, mt, gc, mt);
				menuItem.setIcon(icon);
				getAddFromShapeMenu().add(menuItem);
				menuItem.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						MolecularTypePattern molecularTypePattern = new MolecularTypePattern(mt);
						((SpeciesPattern)selectedObject).addMolecularTypePattern(molecularTypePattern);
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {				

							}
						});
					}
				});
			}
			JMenuItem deleteMenuItem = new JMenuItem("Delete Species Pattern");
			deleteMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					observable.getSpeciesPatternList().remove((SpeciesPattern)selectedObject);
					
					final TreePath path = observableTreeModel.findObjectPath(null, observable);
					observableTree.setSelectionPath(path);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							observableTreeModel.populateTree();			// repaint tree
							observableTree.scrollPathToVisible(path);	// scroll back up to show the observable
						}
					});
				}
			});
			popupFromShapeMenu.add(deleteMenuItem);
			popupFromShapeMenu.add(new JSeparator());
			popupFromShapeMenu.add(getAddFromShapeMenu());
			
		} else if (selectedObject instanceof MolecularTypePattern) {
			MolecularTypePattern mtp = (MolecularTypePattern)selectedObject;
			
			String moveRightMenuText = "Move <b>" + "right" + "</b>";
			moveRightMenuText = "<html>" + moveRightMenuText + "</html>";
			JMenuItem moveRightMenuItem = new JMenuItem(moveRightMenuText);
			Icon icon = VCellIcons.moveRightIcon;
			moveRightMenuItem.setIcon(icon);
			moveRightMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MolecularTypePattern from = (MolecularTypePattern)selectedObject;
					SpeciesPattern sp = locationContext.sps.getSpeciesPattern();
					List<MolecularTypePattern> mtpList = sp.getMolecularTypePatterns();
					int fromIndex = mtpList.indexOf(from);
					if(mtpList.size() == fromIndex+1) {		// already the last element
						return;
					}
					int toIndex = fromIndex+1;
					MolecularTypePattern to = mtpList.remove(toIndex);
					mtpList.add(fromIndex, to);
					observableTreeModel.populateTree();
				}
			});
			popupFromShapeMenu.add(moveRightMenuItem);
			
			String moveLeftMenuText = "Move <b>" + "left" + "</b>";
			moveLeftMenuText = "<html>" + moveLeftMenuText + "</html>";
			JMenuItem moveLeftMenuItem = new JMenuItem(moveLeftMenuText);
			icon = VCellIcons.moveLeftIcon;
			moveLeftMenuItem.setIcon(icon);
			moveLeftMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MolecularTypePattern from = (MolecularTypePattern)selectedObject;
					SpeciesPattern sp = locationContext.sps.getSpeciesPattern();
					List<MolecularTypePattern> mtpList = sp.getMolecularTypePatterns();
					int fromIndex = mtpList.indexOf(from);
					if(fromIndex == 0) {			// already the first element
						return;
					}
					int toIndex = fromIndex-1;
					MolecularTypePattern to = mtpList.remove(toIndex);
					mtpList.add(fromIndex, to);
					observableTreeModel.populateTree();
				}
			});
			popupFromShapeMenu.add(moveLeftMenuItem);
			popupFromShapeMenu.add(new JSeparator());
			
			String deleteMenuText = "Delete <b>" + mtp.getMolecularType().getName() + "</b>";
			deleteMenuText = "<html>" + deleteMenuText + "</html>";
			JMenuItem deleteMenuItem = new JMenuItem(deleteMenuText);
			deleteMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MolecularTypePattern mtp = (MolecularTypePattern)selectedObject;
					SpeciesPattern sp = locationContext.sps.getSpeciesPattern();
					sp.removeMolecularTypePattern(mtp);
				}
			});
			popupFromShapeMenu.add(deleteMenuItem);
			
		} else if (selectedObject instanceof MolecularComponentPattern) {
			manageComponentPatternFromShape(selectedObject, locationContext, false);
			bDelete = false;
			
		} else if (selectedObject instanceof ComponentStatePattern) {
			MolecularComponentPattern mcp = ((ComponentStateLargeShape)deepestShape).getMolecularComponentPattern();
			manageComponentPatternFromShape(mcp, locationContext, true);
		}
		if (bRename) {
			popupFromShapeMenu.add(getRenameFromShapeMenuItem());
		}
		if (bDelete) {
			popupFromShapeMenu.add(getDeleteFromShapeMenuItem());
		}
		if (bEdit) {
			popupFromShapeMenu.add(getEditFromShapeMenuItem());
		}
		if (bAdd) {
			popupFromShapeMenu.add(new JSeparator());
			popupFromShapeMenu.add(getAddFromShapeMenu());
		}
		popupFromShapeMenu.show(e.getComponent(), mousePoint.x, mousePoint.y);
	}
	
	public void manageComponentPatternFromShape(final RbmElementAbstract selectedObject, PointLocationInShapeContext locationContext, boolean showStateOnly) {
		final MolecularComponentPattern mcp = (MolecularComponentPattern)selectedObject;
		final MolecularComponent mc = mcp.getMolecularComponent();
		popupFromShapeMenu.removeAll();
		// ------------------------------------------------------------------- State
		if(mc.getComponentStateDefinitions().size() != 0) {
			JMenu editStateMenu = new JMenu();
			editStateMenu.setText("Edit State");
			editStateMenu.removeAll();
			List<String> itemList = new ArrayList<String>();
			itemList.add(ComponentStatePattern.strAny);
			for (final ComponentStateDefinition csd : mc.getComponentStateDefinitions()) {
				String name = csd.getName();
				itemList.add(name);
			}
			for(String name : itemList) {
				JMenuItem menuItem = new JMenuItem(name);
				editStateMenu.add(menuItem);
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String name = e.getActionCommand();
						if(name.equals(ComponentStatePattern.strAny)) {
							ComponentStatePattern csp = new ComponentStatePattern();
							mcp.setComponentStatePattern(csp);
						} else {
							String csdName = e.getActionCommand();
							ComponentStateDefinition csd = mcp.getMolecularComponent().getComponentStateDefinition(csdName);
							if(csd == null) {
								throw new RuntimeException("Missing ComponentStateDefinition " + csdName + " for Component " + mcp.getMolecularComponent().getName());
							}
							ComponentStatePattern csp = new ComponentStatePattern(csd);
							mcp.setComponentStatePattern(csp);
						}
					}
				});
			}
			popupFromShapeMenu.add(editStateMenu);
		}
		if(showStateOnly) {
			return;
		}
		// ------------------------------------------------------------------------------------------- Bonds
		final MolecularTypePattern mtp = locationContext.getMolecularTypePattern();
		final SpeciesPattern sp = locationContext.getSpeciesPattern();
		
		JMenu editBondMenu = new JMenu();
		editBondMenu.setText("Edit Bond");
		editBondMenu.removeAll();
		final Map<String, Bond> itemMap = new LinkedHashMap<String, Bond>();
		
		final String noneString = "<html><b>" + BondType.None.symbol + "</b> " + BondType.None.name() + "</html>";
		final String existsString = "<html><b>" + BondType.Exists.symbol + "</b> " + BondType.Exists.name() + "</html>";
		final String possibleString = "<html><b>" + BondType.Possible.symbol + "</b> " + BondType.Possible.name() + "</html>";
		itemMap.put(noneString, null);
		itemMap.put(existsString, null);
		itemMap.put(possibleString, null);
		if(mtp != null && sp != null) {
			List<Bond> bondPartnerChoices = sp.getAllBondPartnerChoices(mtp, mc);
			for(Bond b : bondPartnerChoices) {
				if(b.equals(mcp.getBond())) {
					continue;	// if the mcp has a bond already we don't offer it
				}
				int index = 0;
				if(mcp.getBondType() == BondType.Specified) {
					index = mcp.getBondId();
				} else {
					index = sp.nextBondId();
				}
//				itemMap.put(b.toHtmlStringLong(sp, mtp, mc, index), b);
				itemMap.put(b.toHtmlStringLong(sp, index), b);
			}
		}
		for(String name : itemMap.keySet()) {
			JMenuItem menuItem = new JMenuItem(name);
			editBondMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = e.getActionCommand();
					BondType btBefore = mcp.getBondType();
					if(name.equals(noneString)) {
						if(btBefore == BondType.Specified) {	// specified -> not specified
							// change the partner to possible
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.None);
						mcp.setBond(null);
						SwingUtilities.invokeLater(new Runnable() { public void run() { observableTreeModel.populateTree(); } });
					} else if(name.equals(existsString)) {
						if(btBefore == BondType.Specified) {	// specified -> not specified
							// change the partner to possible
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.Exists);
						mcp.setBond(null);
						SwingUtilities.invokeLater(new Runnable() { public void run() { observableTreeModel.populateTree(); } });
					} else if(name.equals(possibleString)) {
						if(btBefore == BondType.Specified) {	// specified -> not specified
							// change the partner to possible
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.Possible);
						mcp.setBond(null);
						SwingUtilities.invokeLater(new Runnable() { public void run() { observableTreeModel.populateTree(); } });
					} else {
						if (btBefore != BondType.Specified) {
							// if we go from a non-specified to a specified we need to find the next available
							// bond id, so that we can choose the color for displaying the bond
							// a bad bond id, like -1, will crash badly when trying to choose the color
							int bondId = sp.nextBondId();
							mcp.setBondId(bondId);
						} else {
							// specified -> specified
							// change the old partner to possible, continue using the bond id
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.Specified);
						Bond b = itemMap.get(name);
						mcp.setBond(b);
						mcp.getBond().molecularComponentPattern.setBondId(mcp.getBondId());
						sp.resolveBonds();
						SwingUtilities.invokeLater(new Runnable() { public void run() { observableTreeModel.populateTree(); } });
					}
				}
			});
		}
		popupFromShapeMenu.add(editBondMenu);
	}
	
	private void showPopupMenu(MouseEvent e){ 
		if (!e.isPopupTrigger()) {
			return;
		}
		if (popupFromTreeMenu == null) {
			popupFromTreeMenu = new JPopupMenu();			
		}		
		if (popupFromTreeMenu.isShowing()) {
			return;
		}
		boolean bDelete = false;
		boolean bAdd = false;
		boolean bEdit = false;
		boolean bRename = false;
		popupFromTreeMenu.removeAll();
		Point mousePoint = e.getPoint();
		GuiUtils.selectClickTreePath(observableTree, e);		
		TreePath clickPath = observableTree.getPathForLocation(mousePoint.x, mousePoint.y);
	    if (clickPath == null) {
	    	popupFromTreeMenu.add(getAddSpeciesPatternFromTreeMenuItem());
	    	return;
	    }
		TreePath[] selectedPaths = observableTree.getSelectionPaths();
		if (selectedPaths == null) {
			return;
		}
		for (TreePath tp : selectedPaths) {
			Object obj = tp.getLastPathComponent();
			if (obj == null || !(obj instanceof BioModelNode)) {
				continue;
			}
			
			BioModelNode selectedNode = (BioModelNode) obj;
			final Object selectedObject = selectedNode.getUserObject();
			
			if (selectedObject instanceof RbmObservable) {
				popupFromTreeMenu.add(getAddSpeciesPatternFromTreeMenuItem());
			} else if(selectedObject instanceof SpeciesPatternLocal) {
				getAddFromTreeMenu().setText(VCellErrorMessages.AddMolecularTypes);
				getAddFromTreeMenu().removeAll();
				for (final MolecularType mt : bioModel.getModel().getRbmModelContainer().getMolecularTypeList()) {
					JMenuItem menuItem = new JMenuItem(mt.getName());
					Graphics gc = splitPane.getRightComponent().getGraphics();
					Icon icon = new MolecularTypeSmallShape(1, 4, mt, gc, mt);
					menuItem.setIcon(icon);
					getAddFromTreeMenu().add(menuItem);
					menuItem.addActionListener(new ActionListener() {
						
						public void actionPerformed(ActionEvent e) {
							MolecularTypePattern molecularTypePattern = new MolecularTypePattern(mt);
							((SpeciesPatternLocal)selectedObject).speciesPattern.addMolecularTypePattern(molecularTypePattern);
							final TreePath path = observableTreeModel.findObjectPath(null, molecularTypePattern);
							observableTree.setSelectionPath(path);
							SwingUtilities.invokeLater(new Runnable() {
								
								public void run() {				
									observableTree.scrollPathToVisible(path);
								}
							});
						}
					});
				}
				bAdd = true;
				bDelete = true;
			} else if (selectedObject instanceof MolecularTypePattern) {
				bDelete = true;
			} else if (selectedObject instanceof MolecularComponentPattern) {
				manageComponentPattern(observableTreeModel, observableTree, selectedNode, selectedObject);
				bDelete = false;
			}
		}
//		popupMenu.removeAll();
		// everything can be renamed
		if (bRename) {
			popupFromTreeMenu.add(getRenameFromTreeMenuItem());
		}
		if (bDelete) {
			popupFromTreeMenu.add(getDeleteFromTreeMenuItem());
		}
		if (bEdit) {
			popupFromTreeMenu.add(getEditFromTreeMenuItem());
		}
		if (bAdd) {
			popupFromTreeMenu.add(new JSeparator());
			popupFromTreeMenu.add(getAddFromTreeMenu());
		}
		popupFromTreeMenu.show(observableTree, mousePoint.x, mousePoint.y);
	}
	
	public void setBioModel(BioModel newValue) {
		bioModel = newValue;
		observableTreeModel.setBioModel(bioModel);
	}
	public void manageComponentPattern(final ObservableTreeModel treeModel, final JTree tree,
			BioModelNode selectedNode, final Object selectedObject) {
		popupFromTreeMenu.removeAll();
		final MolecularComponentPattern mcp = (MolecularComponentPattern)selectedObject;
		final MolecularComponent mc = mcp.getMolecularComponent();
		//
		// --- State
		//
		if(mc.getComponentStateDefinitions().size() != 0) {
			JMenu editStateMenu = new JMenu();
			editStateMenu.setText("Edit State");
			editStateMenu.removeAll();
			List<String> itemList = new ArrayList<String>();
			itemList.add(ComponentStatePattern.strAny);
			for (final ComponentStateDefinition csd : mc.getComponentStateDefinitions()) {
				String name = csd.getName();
				itemList.add(name);
			}
			for(String name : itemList) {
				JMenuItem menuItem = new JMenuItem(name);
				editStateMenu.add(menuItem);
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String name = e.getActionCommand();
						if(name.equals(ComponentStatePattern.strAny)) {
							ComponentStatePattern csp = new ComponentStatePattern();
							mcp.setComponentStatePattern(csp);
						} else {
							String csdName = e.getActionCommand();
							ComponentStateDefinition csd = mcp.getMolecularComponent().getComponentStateDefinition(csdName);
							if(csd == null) {
								throw new RuntimeException("Missing ComponentStateDefinition " + csdName + " for Component " + mcp.getMolecularComponent().getName());
							}
							ComponentStatePattern csp = new ComponentStatePattern(csd);
							mcp.setComponentStatePattern(csp);
						}
					}
				});
			}
			popupFromTreeMenu.add(editStateMenu);
		}
		//
		// --- Bonds
		//						
		final MolecularTypePattern mtp;
		final SpeciesPattern sp;
		BioModelNode parentNode = (BioModelNode) selectedNode.getParent();
		Object parentObject = parentNode == null ? null : parentNode.getUserObject();
		if(parentObject != null && parentObject instanceof MolecularTypePattern) {
			mtp = (MolecularTypePattern)parentObject;
			parentNode = (BioModelNode) parentNode.getParent();
			parentObject = parentNode == null ? null : parentNode.getUserObject();
			if(parentObject != null && parentObject instanceof SpeciesPatternLocal) {
				sp = ((SpeciesPatternLocal)parentObject).speciesPattern;
			} else {
				sp = null;
			}
		} else {
			mtp = null;
			sp = null;
		}
		
		JMenu editBondMenu = new JMenu();
		editBondMenu.setText("Edit Bond");
		editBondMenu.removeAll();
		final Map<String, Bond> itemMap = new LinkedHashMap<String, Bond>();
		
		final String noneString = "<html><b>" + BondType.None.symbol + "</b> " + BondType.None.name() + "</html>";
		final String existsString = "<html><b>" + BondType.Exists.symbol + "</b> " + BondType.Exists.name() + "</html>";
		final String possibleString = "<html><b>" + BondType.Possible.symbol + "</b> " + BondType.Possible.name() + "</html>";
		itemMap.put(noneString, null);
		itemMap.put(existsString, null);
		itemMap.put(possibleString, null);
		if(mtp != null && sp != null) {
			List<Bond> bondPartnerChoices = sp.getAllBondPartnerChoices(mtp, mc);
			for(Bond b : bondPartnerChoices) {
				if(b.equals(mcp.getBond())) {
					continue;	// if the mcp has a bond already we don't offer it
				}
				int index = 0;
				if(mcp.getBondType() == BondType.Specified) {
					index = mcp.getBondId();
				} else {
					index = sp.nextBondId();
				}
//				itemMap.put(b.toHtmlStringLong(sp, mtp, mc, index), b);
				itemMap.put(b.toHtmlStringLong(sp, index), b);
			}
		}
		for(String name : itemMap.keySet()) {
			JMenuItem menuItem = new JMenuItem(name);
			editBondMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = e.getActionCommand();
					BondType btBefore = mcp.getBondType();
					if(name.equals(noneString)) {
						if(btBefore == BondType.Specified) {	// specified -> not specified
							// change the partner to possible
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.None);
						mcp.setBond(null);
						treeModel.populateTree();
					} else if(name.equals(existsString)) {
						if(btBefore == BondType.Specified) {	// specified -> not specified
							// change the partner to possible
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.Exists);
						mcp.setBond(null);
						treeModel.populateTree();
					} else if(name.equals(possibleString)) {
						if(btBefore == BondType.Specified) {	// specified -> not specified
							// change the partner to possible
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.Possible);
						mcp.setBond(null);
						treeModel.populateTree();
					} else {
						if (btBefore != BondType.Specified) {
							// if we go from a non-specified to a specified we need to find the next available
							// bond id, so that we can choose the color for displaying the bond
							// a bad bond id, like -1, will crash badly when trying to choose the color
							int bondId = sp.nextBondId();
							mcp.setBondId(bondId);
						} else {
							// specified -> specified
							// change the old partner to possible, continue using the bond id
							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.Specified);
						Bond b = itemMap.get(name);
						mcp.setBond(b);
						mcp.getBond().molecularComponentPattern.setBondId(mcp.getBondId());
						sp.resolveBonds();

						final TreePath path = treeModel.findObjectPath(null, mcp);
						treeModel.populateTree();
						tree.setSelectionPath(path);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {				
								tree.scrollPathToVisible(path);
							}
						});
					}

				}
			});
		}
		popupFromTreeMenu.add(editBondMenu);
	}

}
