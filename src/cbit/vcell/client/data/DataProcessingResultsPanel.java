/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.data;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vcell.util.BeanUtils;
import org.vcell.util.DataAccessException;
import org.vcell.util.Extent;
import org.vcell.util.ISize;
import org.vcell.util.Origin;
import org.vcell.util.Range;
import org.vcell.util.UserCancelException;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.FileFilters;

import cbit.image.DisplayAdapterService;
import cbit.image.gui.ImagePaneModel;
import cbit.image.gui.ImagePlaneManagerPanel;
import cbit.image.gui.SourceDataInfo;
import cbit.plot.Plot2D;
import cbit.plot.PlotPane;
import cbit.plot.SingleXPlot2D;
import cbit.vcell.client.ClientMDIManager;
import cbit.vcell.client.DatabaseWindowManager;
import cbit.vcell.client.RequestManager;
import cbit.vcell.client.desktop.DocumentWindow;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.field.gui.FieldDataGUIPanel;
import cbit.vcell.field.io.FieldDataFileOperationSpec;
import cbit.vcell.math.PostProcessingBlock;
import cbit.vcell.math.ReservedVariable;
import cbit.vcell.math.VariableType;
import cbit.vcell.simdata.DataOperation;
import cbit.vcell.simdata.DataOperationResults;
import cbit.vcell.simdata.PDEDataContext;
import cbit.vcell.simdata.DataOperationResults.DataProcessingOutputDataValues;
import cbit.vcell.simdata.DataOperationResults.DataProcessingOutputInfo;

@SuppressWarnings("serial")
public class DataProcessingResultsPanel extends JPanel/* implements PropertyChangeListener*/ {

//	private PDEDataContext pdeDataContext;
//	private NetcdfFile ncfile;
	private JList varJList;
	private PlotPane plotPane = null;
	private int[] lastSelectedIdxArray = null;
	private JScrollPane graphScrollPane;
	private JPanel cardLayoutPanel;
	private DataProcessingOutputInfo dataProcessingOutputInfo;
	
	public DataProcessingResultsPanel() {
		super();
		initialize();
	}
	private void initialize() {		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0};
		gridBagLayout.columnWeights = new double[]{0, 0};
		setLayout(gridBagLayout);
		varJList = new JList();
		varJList.setVisibleRowCount(5);
		varJList.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				if(varJList.getSelectedIndex() != -1){
					((CardLayout)cardLayoutPanel.getLayout()).show(cardLayoutPanel, "plotPane1");
				}
				onVariablesChange();
			}
		});
		GridBagConstraints gbc_graphScrollPane = new GridBagConstraints();
		gbc_graphScrollPane.weighty = 0.3;
		gbc_graphScrollPane.gridx = 0;
		gbc_graphScrollPane.gridy = 0;
		gbc_graphScrollPane.insets = new Insets(4, 4, 4, 4);
		gbc_graphScrollPane.fill = GridBagConstraints.BOTH;
		graphScrollPane = new JScrollPane(varJList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(graphScrollPane, gbc_graphScrollPane);
		
		cardLayoutPanel = new JPanel();
		GridBagConstraints gbc_cardLayoutPanel = new GridBagConstraints();
		gbc_cardLayoutPanel.weightx = 1.0;
		gbc_cardLayoutPanel.weighty = 1.0;
		gbc_cardLayoutPanel.insets = new Insets(4, 4, 4, 4);
		gbc_cardLayoutPanel.fill = GridBagConstraints.BOTH;
		gbc_cardLayoutPanel.gridx = 1;
		gbc_cardLayoutPanel.gridy = 0;
		add(cardLayoutPanel, gbc_cardLayoutPanel);
		cardLayoutPanel.setLayout(new CardLayout(0, 0));
		
		plotPane = new PlotPane();
		cardLayoutPanel.add(plotPane,"plotPane1");
	}

	private PDEDataContext pdeDataContext;

	private void read(PDEDataContext pdeDataContext0) throws Exception {
		this.pdeDataContext = pdeDataContext0;
		dataProcessingOutputInfo = null;
		try {
			dataProcessingOutputInfo = (DataProcessingOutputInfo)pdeDataContext.doDataOperation(new DataOperation.DataProcessingOutputInfoOP(pdeDataContext.getVCDataIdentifier(),true,((NewClientPDEDataContext)pdeDataContext0).getDataManager().getOutputContext()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Data Processing Output Error - '"+e.getMessage()+"'  (Note: Data Processing Output is generated automatically when running VCell 5.2 or later simulations)");
		}
	}
	
	public void update(final PDEDataContext pdeDataContext) {
		AsynchClientTask task1 = new AsynchClientTask("retrieving data", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {		
				read(pdeDataContext);
			}
		};
		
		AsynchClientTask task2 = new AsynchClientTask("showing data", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {		
				varJList.removeAll();
				if (/*ncfile*/ dataProcessingOutputInfo == null) {
					plotPane.setPlot2D(null);
				} else {
					DefaultListModel dlm = new DefaultListModel();
					for (int i = 0; i < dataProcessingOutputInfo.getVariableNames().length; i++) {
						String variableName = dataProcessingOutputInfo.getVariableNames()[i];
						if(dataProcessingOutputInfo.getPostProcessDataType(dataProcessingOutputInfo.getVariableNames()[i]).equals(DataOperationResults.DataProcessingOutputInfo.PostProcessDataType.statistic)){
							String variableUnits = dataProcessingOutputInfo.getVariableUnits(variableName);
							if(variableUnits != null){
								dlm.addElement(variableName + "_(" + variableUnits + ")");
							}else{
								dlm.addElement(variableName);
							}
						}
					}
					int[] lastSelectedIdxArray0 = varJList.getSelectedIndices(); 
					varJList.setModel(dlm);
					lastSelectedIdxArray = lastSelectedIdxArray0;
					if(lastSelectedIdxArray == null || lastSelectedIdxArray.length == 0){
						varJList.setSelectedIndex(0);
					}
					else{
						varJList.setSelectedIndices(lastSelectedIdxArray);
					}
				}
			}
		};
		
		ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2});
	}
	
	private void onVariablesChange() {
		try {
			//keep old indices
			lastSelectedIdxArray = varJList.getSelectedIndices();
			Object[] selectedObjects = varJList.getSelectedValues();
			int numSelectedVars = selectedObjects.length;
			int totalColumns = 1;
			for (int v = 0; v < numSelectedVars; v ++) {
				totalColumns++;
//				String varName = (String)selectedObjects[v];
//				ucar.nc2.Variable volVar = ncfile.findVariable(varName);
//				int[] shape = volVar.getShape();
//				int numColumns = shape[1];
//				
//				totalColumns += numColumns;
			}
			int numTimes = dataProcessingOutputInfo.getVariableTimePoints().length;
			double[][] plotDatas = new double[totalColumns][numTimes];
			plotDatas[0] = dataProcessingOutputInfo.getVariableTimePoints();//assumes all times same
			String[] plotNames = new String[totalColumns - 1];
			int columnCount = 0;
			for (int v = 0; v < numSelectedVars; v ++) {
				String varName = ((String)selectedObjects[v]);
				//remove the unit from name if exist
				if(varName.indexOf("_(") > 0) //"_(" doesn't suppose to be the first char
				{
					varName = varName.substring(0, varName.indexOf("_("));
				}

//				ucar.nc2.Variable volVar = ncfile.findVariable(varName);
//				int[] shape = volVar.getShape();
//				int numColumns = shape[1];
//				int[] origin = new int[2];
//				ArrayDouble.D2 data = null;
//				try {
//					data = (ArrayDouble.D2) volVar.read(origin, shape);
//				} catch (Exception e) {
//					e.printStackTrace(System.err);
//					throw new IOException("Can not read volVar data.");
//				}
				for (int i = 0; i < /*numColumns*/ 1; i++) {
					String plotName = varName ;
					if (i > 0) {
						plotName += ": region " + (i-1);
					}
					plotNames[columnCount] = plotName;
					for (int j = 0; j < numTimes; j++) {
						plotDatas[columnCount + 1][j] = dataProcessingOutputInfo.getVariableStatValues().get(varName)[j];//data.get(j, i);
					}
					columnCount ++;
				}
			}
			Plot2D plot2D = new SingleXPlot2D(null, ReservedVariable.TIME.getName(), plotNames, plotDatas, 
					new String[] {"Time Plot", ReservedVariable.TIME.getName(), ""});				
			plotPane.setPlot2D(plot2D);
		} catch (Exception e1) {
			DialogUtils.showErrorDialog(this, e1.getMessage(), e1);
			e1.printStackTrace();
		}
	}
//	public void propertyChange(PropertyChangeEvent evt) {
//		if (evt.getSource() == pdeDataContext && evt.getPropertyName().equals(PDEDataContext.PROPERTY_NAME_TIME_POINTS)) {
//			update();
//		}
//		if (evt.getSource() == pdeDataContext && evt.getPropertyName().equals(PDEDataContext.PROPERTY_NAME_VCDATA_IDENTIFIER)) {
//			update();
//		}
//	}
	
}
