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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.vcell.util.BeanUtils;
import org.vcell.util.ClientTaskStatusSupport;
import org.vcell.util.Coordinate;
import org.vcell.util.CoordinateIndex;
import org.vcell.util.DataAccessException;
import org.vcell.util.Executable;
import org.vcell.util.NumberUtils;
import org.vcell.util.PropertyLoader;
import org.vcell.util.Range;
import org.vcell.util.UserCancelException;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.LocalVCDataIdentifier;
import org.vcell.util.document.TSJobResultsNoStats;
import org.vcell.util.document.TSJobResultsSpaceStats;
import org.vcell.util.document.TimeSeriesJobSpec;
import org.vcell.util.document.VCDataIdentifier;
import org.vcell.util.document.VCDataJobID;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.DownArrowIcon;
import org.vcell.util.gui.FileFilters;
import org.vcell.util.gui.LineBorderBean;
import org.vcell.util.gui.ScrollTable;
import org.vcell.util.gui.TitledBorderBean;
import org.vcell.util.gui.VCFileChooser;

import cbit.image.gui.DisplayAdapterService;
import cbit.plot.Plot2D;
import cbit.plot.PlotData;
import cbit.plot.PlotPane;
import cbit.plot.SingleXPlot2D;
import cbit.rmi.event.DataJobEvent;
import cbit.rmi.event.DataJobListener;
import cbit.rmi.event.MessageEvent;
import cbit.vcell.client.ChildWindowListener;
import cbit.vcell.client.ChildWindowManager;
import cbit.vcell.client.ChildWindowManager.ChildWindow;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.client.desktop.DocumentWindowAboutBox;
import cbit.vcell.client.server.DataManager;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.export.ExportMonitorPanel;
import cbit.vcell.export.gloworm.atoms.UserDataEntry;
import cbit.vcell.export.gloworm.quicktime.MediaMethods;
import cbit.vcell.export.gloworm.quicktime.MediaMovie;
import cbit.vcell.export.gloworm.quicktime.MediaTrack;
import cbit.vcell.export.gloworm.quicktime.VideoMediaChunk;
import cbit.vcell.export.gloworm.quicktime.VideoMediaSample;
import cbit.vcell.export.server.ExportSpecs;
import cbit.vcell.export.server.FormatSpecificSpecs;
import cbit.vcell.geometry.Curve;
import cbit.vcell.geometry.SampledCurve;
import cbit.vcell.geometry.SinglePoint;
import cbit.vcell.geometry.gui.DataValueSurfaceViewer;
import cbit.vcell.geometry.gui.SurfaceCanvas;
import cbit.vcell.geometry.gui.SurfaceMovieSettingsPanel;
import cbit.vcell.geometry.surface.Surface;
import cbit.vcell.geometry.surface.SurfaceCollection;
import cbit.vcell.geometry.surface.TaubinSmoothing;
import cbit.vcell.geometry.surface.TaubinSmoothingSpecification;
import cbit.vcell.geometry.surface.TaubinSmoothingWrong;
import cbit.vcell.math.ReservedVariable;
import cbit.vcell.math.Variable.Domain;
import cbit.vcell.math.VariableType;
import cbit.vcell.math.VariableType.VariableDomain;
import cbit.vcell.math.VolVariable;
import cbit.vcell.parser.SimpleSymbolTable;
import cbit.vcell.parser.SymbolTable;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.render.Vect3d;
import cbit.vcell.resource.ResourceUtil;
import cbit.vcell.simdata.ClientPDEDataContext;
import cbit.vcell.simdata.DataIdentifier;
import cbit.vcell.simdata.PDEDataContext;
import cbit.vcell.simdata.SimulationData;
import cbit.vcell.simdata.gui.MeshDisplayAdapter;
import cbit.vcell.simdata.gui.PDEDataContextPanel;
import cbit.vcell.simdata.gui.PDEPlotControlPanel;
import cbit.vcell.simdata.gui.PdeTimePlotMultipleVariablesPanel;
import cbit.vcell.simdata.gui.SpatialSelection;
import cbit.vcell.simdata.gui.SpatialSelection.SSHelper;
import cbit.vcell.simdata.gui.SpatialSelectionMembrane;
import cbit.vcell.simdata.gui.SpatialSelectionVolume;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SolverDescription;
import cbit.vcell.solver.VCSimulationDataIdentifier;
import cbit.vcell.solver.VCSimulationDataIdentifierOldStyle;
import cbit.vcell.solvers.CartesianMesh;
import cbit.vcell.solvers.MembraneElement;
import cbit.vcell.visit.VisitConnectionInfo;
import cbit.vcell.visit.VisitControlPanel;
import cbit.vcell.visit.VisitSession;
/**
 * Insert the type's description here.
 * Creation date: (6/11/2004 6:03:07 AM)
 * @author: Ion Moraru
 */
@SuppressWarnings("serial")
public class PDEDataViewer extends DataViewer {
	public enum CurrentView {
		SLICE_VIEW("Slice View"),
		SURFACE_VIEW("Surface View");
		
		private String title;
		CurrentView(String t) {
			title = t;
		} 
	}
	private JPanel sliceViewPanel;
	
	private Vector<DataJobListener> dataJobListenerList = new Vector<DataJobListener>();
	 
	public static String StringKey_timeSeriesJobResults =  "timeSeriesJobResults";
	public static String StringKey_timeSeriesJobException =  "timeSeriesJobException";
	public static String StringKey_timeSeriesJobSpec =  "timeSeriesJobSpec";
	private String libDir;
	private String visitBinDir;
	private JTabbedPane viewDataTabbedPane;
//	public VisitProcess visitProcess;
	
	
	public static class TimeSeriesDataJobListener implements DataJobListener {
		private ClientTaskStatusSupport clientTaskStatusSupport = null;
		private Hashtable<String, Object> hashTable = null;
		private VCDataJobID vcDataJobID = null;
		private double oldProgress = 0;
		
		public TimeSeriesDataJobListener(VCDataJobID id, Hashtable<String, Object> hash, ClientTaskStatusSupport ctss) {
			vcDataJobID = id;
			clientTaskStatusSupport = ctss;
			hashTable = hash;
		}			
			
		public void dataJobMessage(final DataJobEvent dje) {
			if (!dje.getVcDataJobID().equals(vcDataJobID)) {
				return;
			}
			switch (dje.getEventTypeID()) {
			case MessageEvent.DATA_START:
				break;
			case MessageEvent.DATA_PROGRESS:
				Double progress = dje.getProgress();
				if (clientTaskStatusSupport != null && progress != null && progress.doubleValue() > oldProgress) {
					oldProgress = progress.doubleValue();								
					clientTaskStatusSupport.setProgress(progress.intValue());
				}
				break;
			case MessageEvent.DATA_COMPLETE:
				hashTable.put(StringKey_timeSeriesJobResults, dje.getTimeSeriesJobResults());
				break;
			case MessageEvent.DATA_FAILURE:
				hashTable.put(StringKey_timeSeriesJobException,dje.getFailedJobException());
				break;
			}
		}
	};
	
	public class TimeSeriesDataRetrievalTask extends AsynchClientTask {
		public TimeSeriesDataRetrievalTask(String title) {
			super(title, AsynchClientTask.TASKTYPE_NONSWING_BLOCKING);
		}
	
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			DataJobListener djl = null;
			try {
				TimeSeriesJobSpec timeSeriesJobSpec = (TimeSeriesJobSpec)hashTable.get(StringKey_timeSeriesJobSpec);
				djl =
					new TimeSeriesDataJobListener(timeSeriesJobSpec.getVcDataJobID(), hashTable, getClientTaskStatusSupport());
				PDEDataViewer.this.addDataJobListener(djl);
				PDEDataViewer.this.getPdeDataContext().getTimeSeriesValues(timeSeriesJobSpec);
				while (true) {
					Throwable timeSeriesJobFailed = (Throwable)hashTable.get(StringKey_timeSeriesJobException);
					if (timeSeriesJobFailed != null) {
						throw new Exception(timeSeriesJobFailed.getMessage());
					}
					if (hashTable.get(StringKey_timeSeriesJobResults) != null) {
						break;
					}
					if (getClientTaskStatusSupport() != null && getClientTaskStatusSupport().isInterrupted()) {
						break;
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						if (getClientTaskStatusSupport().isInterrupted()) {
							throw UserCancelException.CANCEL_GENERIC;
						} else {
							throw e;
						}
					}
				}
			} finally {
				if(djl != null){PDEDataViewer.this.removeDataJobListener(djl);}
			}
		}
	};

	//
	private DataValueSurfaceViewer fieldDataValueSurfaceViewer = null;
	private MeshDisplayAdapter.MeshRegionSurfaces meshRegionSurfaces = null;
//	private static final String   SHOW_MEMB_SURFACE_BUTTON_STRING = "Show Membrane Surfaces";
//	private static final String UPDATE_MEMB_SURFACE_BUTTON_STRING = "Update Membrane Surfaces";
	//
	private PDEDataContext fieldPdeDataContext = null;
	private PDEDataContextPanel ivjPDEDataContextPanel1 = null;
	private PDEPlotControlPanel ivjPDEPlotControlPanel1 = null;
	private IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private JPanel ivjExportData = null;
	private JPanel ivjJPanelButtons = null;
	private JTabbedPane ivjJTabbedPane1 = null;
	private JPanel ivjViewData = null;
	private NewPDEExportPanel ivjPDEExportPanel1 = null;
	private ExportMonitorPanel ivjExportMonitorPanel1 = null;
	private BitSet volumeSnapshotROI;
	private String volumeSnapshotROIDescription;
	private BitSet membraneSnapshotROI;
	private String membraneSnapshotROIDescription;
	private JButton plotButton = null;
	private JPopupMenu plotPopupMenu = null;
	private JMenuItem spatialPlotMenuItem;
	private JMenuItem timePlotMenuItem;
	private JMenuItem kymographMenuItem;
	
	private JButton roiButton = null;
	private JPopupMenu roiPopupMenu = null;
	private JMenuItem statisticsMenuItem;
	private JMenuItem snapShotMenuItem;
	
	private DataProcessingResultsPanel dataProcessingResultsPanel;

	private static final String EXPORT_DATA_TABNAME = "Export Data";
	private static final String POST_PROCESS_TABNAME = "Post Processing Data";
	
	private class IvjEventHandler implements java.awt.event.ActionListener, java.beans.PropertyChangeListener, ChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			try {
				if (e.getSource() == getPlotButton()) {	
					getPlotPopupMenu().show(getPlotButton(), 0, getPlotButton().getHeight());
				} else if (e.getSource() == getROIButton()) {
					getROIPopupMenu().show(getROIButton(), 0, getROIButton().getHeight());
				} else if (e.getSource() == spatialPlotMenuItem) { 
					connEtoC2(e);
				} else if (e.getSource() == timePlotMenuItem) { 
					showTimePlot();
				} else if (e.getSource() == kymographMenuItem) { 
					connEtoC4(e);
				} else if (e.getSource() == statisticsMenuItem) { 
					connEtoC9(e);
				} else if (e.getSource() == snapShotMenuItem) {
					snapshotROI();
				} else if (e.getSource() == getJButtonVisit()) {
					openInVisit();
				}  else if (e.getSource() == getJButtonLocalVisit()) {
					openInLocalVisit();
				}
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			try {
				if (evt.getSource() == PDEDataViewer.this &&
						(evt.getPropertyName().equals(DataViewer.PROP_SIM_MODEL_INFO) || evt.getPropertyName().equals("pdeDataContext"))) {
					if (getPdeDataContext() != null && getSimulationModelInfo() != null){
						getPDEDataContextPanel1().setDataInfoProvider(new PDEDataViewer.DataInfoProvider(getPdeDataContext(),getSimulationModelInfo()));
						getPDEExportPanel1().setDataInfoProvider(getPDEDataContextPanel1().getDataInfoProvider());
					} else {
						getPDEDataContextPanel1().setDataInfoProvider(null);
						getPDEExportPanel1().setDataInfoProvider(null);
					}
				}
				if (evt.getSource() == PDEDataViewer.this && (evt.getPropertyName().equals("pdeDataContext"))) { 
					getPDEDataContextPanel1().setPdeDataContext(getPdeDataContext());
					getPDEPlotControlPanel1().setPdeDataContext(getPdeDataContext());
					getPDEExportPanel1().setPdeDataContext(getPdeDataContext(),
							(getSimulation()==null?null:new ExportSpecs.SimNameSimDataID(getSimulation().getName(), getSimulation().getSimulationInfo().getAuthoritativeVCSimulationIdentifier(), null)));
					PDEDataContext oldValue = (PDEDataContext) evt.getOldValue();
					if (oldValue != null) {
						oldValue.removePropertyChangeListener(ivjEventHandler);
					}
					if (getPdeDataContext() != null) {
						getPdeDataContext().addPropertyChangeListener(ivjEventHandler);
					}					
					CartesianMesh cartesianMesh = getPdeDataContext().getCartesianMesh();
					if (cartesianMesh != null && cartesianMesh.getGeometryDimension() == 3
							&& cartesianMesh.getNumMembraneElements() > 0){
						if (viewDataTabbedPane.indexOfComponent(getDataValueSurfaceViewer()) < 0) {
							viewDataTabbedPane.addTab(CurrentView.SURFACE_VIEW.title, getDataValueSurfaceViewer());
						}
						getDataValueSurfaceViewer().setDisplayAdapterService(getPDEDataContextPanel1().getdisplayAdapterService1());
						getPDEDataContextPanel1().getdisplayAdapterService1().addPropertyChangeListener(this);
						if (getPdeDataContext().getDataIdentifier().getVariableType().getVariableDomain() != VariableDomain.VARIABLEDOMAIN_MEMBRANE) {
							viewDataTabbedPane.setEnabledAt(viewDataTabbedPane.indexOfComponent(getDataValueSurfaceViewer()), false);
						}
					}
//					dataProcessingResultsPanel.setPdeDataContext(getPdeDataContext());
				}
				if (evt.getSource() == PDEDataViewer.this && (evt.getPropertyName().equals("simulation"))) {
					//set Smoldyn flag for exports to create "particle" media
					boolean isSmoldyn = false;
					if(getSimulation() != null &&
						getSimulation().getSolverTaskDescription() != null &&
						getSimulation().getSolverTaskDescription().getSolverDescription() != null){
						SolverDescription solverDescription =
							getSimulation().getSolverTaskDescription().getSolverDescription();
						isSmoldyn = solverDescription.equals(SolverDescription.Smoldyn);
					}
					getPDEExportPanel1().setIsSmoldyn(isSmoldyn);
				}
//				if (evt.getSource() == PDEDataViewer.this.getPDEDataContextPanel1() && (evt.getPropertyName().equals("pdeDataContext"))) {
//					setPdeDataContext(getPDEDataContextPanel1().getPdeDataContext());
//				}
//				if (evt.getSource() == PDEDataViewer.this.getPDEPlotControlPanel1() && (evt.getPropertyName().equals("pdeDataContext"))) { 
//					setPdeDataContext(getPDEPlotControlPanel1().getPdeDataContext());
//				}
				if (evt.getSource() == PDEDataViewer.this.getPDEDataContextPanel1() && (evt.getPropertyName().equals("displayAdapterService1"))) {
					getPDEPlotControlPanel1().setDisplayAdapterService(getPDEDataContextPanel1().getdisplayAdapterService1());
					getPDEExportPanel1().setDisplayAdapterService(getPDEDataContextPanel1().getdisplayAdapterService1());
					if (fieldDataValueSurfaceViewer != null) {
						fieldDataValueSurfaceViewer.setDisplayAdapterService(getPDEDataContextPanel1().getdisplayAdapterService1());
					}
				}
				if (evt.getSource() == PDEDataViewer.this.getPDEDataContextPanel1()) { 
					updateDataSamplerContext(evt);
				}
				if (evt.getSource() == PDEDataViewer.this.getPDEDataContextPanel1() && (evt.getPropertyName().equals("slice"))) {
					getPDEExportPanel1().setSlice(getPDEDataContextPanel1().getSlice());
				}
				if (evt.getSource() == PDEDataViewer.this.getPDEExportPanel1() && (evt.getPropertyName().equals("slice"))) { 
					getPDEDataContextPanel1().setSlice(getPDEExportPanel1().getSlice());
				}
//				if (evt.getSource() == PDEDataViewer.this.getPDEDataContextPanel1() && (evt.getPropertyName().equals("pdeDataContext"))) {
//					getPDEExportPanel1().setPdeDataContext(getPDEDataContextPanel1().getPdeDataContext());
//				}
//				if (evt.getSource() == PDEDataViewer.this.getPDEExportPanel1() && (evt.getPropertyName().equals("pdeDataContext"))) {
//					getPDEDataContextPanel1().setPdeDataContext(getPDEExportPanel1().getPdeDataContext());
//				}
				if (evt.getSource() == PDEDataViewer.this.getPDEDataContextPanel1() && (evt.getPropertyName().equals("normalAxis"))) {
					getPDEExportPanel1().setNormalAxis(getPDEDataContextPanel1().getNormalAxis());
				}
				if (evt.getSource() == PDEDataViewer.this.getPDEExportPanel1() && (evt.getPropertyName().equals("normalAxis"))) { 
					getPDEDataContextPanel1().setNormalAxis(getPDEExportPanel1().getNormalAxis());
				}
				if (evt.getSource() == PDEDataViewer.this && (evt.getPropertyName().equals("dataViewerManager"))) {
					getPDEExportPanel1().setDataViewerManager(getDataViewerManager());
				}
				if (evt.getSource() == PDEDataViewer.this.getPdeDataContext() && 
						(evt.getPropertyName().equals(PDEDataContext.PROPERTY_NAME_VCDATA_IDENTIFIER) 
						|| evt.getPropertyName().equals(PDEDataContext.PROPERTY_NAME_VARIABLE) 
						|| evt.getPropertyName().equals(PDEDataContext.PROPERTY_NAME_TIME_POINT))) {
					getPDEDataContextPanel1().recodeDataForDomain();
					if (getPdeDataContext().getDataIdentifier().getVariableType().getVariableDomain() == VariableDomain.VARIABLEDOMAIN_MEMBRANE) {
						if (viewDataTabbedPane.indexOfComponent(getDataValueSurfaceViewer()) >= 0) {
							viewDataTabbedPane.setEnabledAt(viewDataTabbedPane.indexOfComponent(getDataValueSurfaceViewer()), true);
						}
						if (viewDataTabbedPane.getSelectedComponent() == getDataValueSurfaceViewer()) {
							updateDataValueSurfaceViewer();
						}
					} else {
						viewDataTabbedPane.setSelectedComponent(sliceViewPanel);
						if (viewDataTabbedPane.indexOfComponent(getDataValueSurfaceViewer()) >= 0) {
							viewDataTabbedPane.setEnabledAt(viewDataTabbedPane.indexOfComponent(getDataValueSurfaceViewer()), false);
						}
					}
				}
				if (evt.getSource() == getPDEDataContextPanel1().getdisplayAdapterService1()) {
					if (viewDataTabbedPane.getSelectedComponent() == getDataValueSurfaceViewer()) {
						updateDataValueSurfaceViewer();
					}
				}
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}				
		}
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == viewDataTabbedPane) {
				if (viewDataTabbedPane.getSelectedComponent() == getDataValueSurfaceViewer()) {
					updateDataValueSurfaceViewer();
				}
			}
		};
	};
	public static class VolumeDataInfo{
		public final int volumeIndex;
		public final String volumeNamePhysiology;
		public final String volumeNameGeometry;
		public final int subvolumeID;
		public final int volumeRegionID;
		public VolumeDataInfo(int volumeIndex,CartesianMesh cartesianMesh,SimulationModelInfo simulationModelInfo){
			this.volumeIndex = volumeIndex;
			volumeRegionID = cartesianMesh.getVolumeRegionIndex(volumeIndex);
			subvolumeID = cartesianMesh.getSubVolumeFromVolumeIndex(volumeIndex);
			volumeNamePhysiology = simulationModelInfo.getVolumeNamePhysiology(subvolumeID);
			volumeNameGeometry = simulationModelInfo.getVolumeNameGeometry(subvolumeID);
		}
	}

	public static class MembraneDataInfo{
		public final int membraneIndex;
		public final MembraneElement membraneElement;
		public final String membraneName;
		public final int membraneRegionID;
		public MembraneDataInfo(int membraneIndex,CartesianMesh cartesianMesh,SimulationModelInfo simulationModelInfo){
			this.membraneIndex = membraneIndex;
			membraneElement = cartesianMesh.getMembraneElements()[membraneIndex];
			membraneRegionID = cartesianMesh.getMembraneRegionIndex(membraneIndex);
			membraneName =
				simulationModelInfo.getMembraneName(
						cartesianMesh.getSubVolumeFromVolumeIndex(membraneElement.getInsideVolumeIndex()),
						cartesianMesh.getSubVolumeFromVolumeIndex(membraneElement.getOutsideVolumeIndex()), false);
		}
	}

	public static class DataInfoProvider{
		private PDEDataContext pdeDataContext;
		private SimulationModelInfo simulationModelInfo;
		public DataInfoProvider(PDEDataContext pdeDataContext, SimulationModelInfo simulationModelInfo){
			this.pdeDataContext = pdeDataContext;
			this.simulationModelInfo = simulationModelInfo;
		}
		public VolumeDataInfo getVolumeDataInfo(int volumeIndex){
			return new VolumeDataInfo(volumeIndex,pdeDataContext.getCartesianMesh(),simulationModelInfo);
		}
		public MembraneDataInfo getMembraneDataInfo(int membraneIndex){
			return new MembraneDataInfo(membraneIndex,pdeDataContext.getCartesianMesh(),simulationModelInfo);
		}
		public boolean isDefined(int dataIndex){
			return isDefined(pdeDataContext.getDataIdentifier(),dataIndex);
		}
		public boolean isDefined(DataIdentifier dataIdentifier,int dataIndex){
			try {
				Domain varDomain = dataIdentifier.getDomain();
				if (varDomain == null) {
					return true;
				}
				VariableType varType = dataIdentifier.getVariableType();
				if(varType.equals(VariableType.VOLUME) || varType.equals(VariableType.VOLUME_REGION)){
					int subvol = pdeDataContext.getCartesianMesh().getSubVolumeFromVolumeIndex(dataIndex);
					if (simulationModelInfo.getVolumeNameGeometry(subvol).equals(varDomain.getName())) {
						return true;
					}				
				}else if(varType.equals(VariableType.MEMBRANE) || varType.equals(VariableType.MEMBRANE_REGION)){
					String memSubdomainName = pdeDataContext.getCartesianMesh().getMembraneSubdomainNamefromMemIndex(dataIndex);
					if (varDomain.getName().equals(memSubdomainName)){
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		public final SimulationModelInfo getSimulationModelInfo() {
			return simulationModelInfo;
		}
		public PDEDataContext getPDEDataContext(){
			return pdeDataContext;
		}
	}

//	private JButton ivjJButtonStatistics = null;
	private Simulation fieldSimulation = null;

public PDEDataViewer() {
	super();
	initialize();
}

public void setDataIdentifierFilter(PDEPlotControlPanel.DataIdentifierFilter dataIdentifierFilter){
	getPDEPlotControlPanel1().setDataIdentifierFilter(dataIdentifierFilter);
}

/**
 * Comment
 */
private void calcStatistics(final ActionEvent actionEvent) {
	try {
		AsynchClientTask waitTask = new AsynchClientTask("Waiting for data to refresh...",AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				getPdeDataContext().waitWhileBusy();			
			}
		};
		AsynchClientTask roiActionTask = new AsynchClientTask("Statistics task...",AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				roiAction();				
			}
		};

		if(getPdeDataContext().isBusy()){
			//Show wait dialog
			ClientTaskDispatcher.dispatch(PDEDataViewer.this, new Hashtable<String, Object>(),
					new AsynchClientTask[] {waitTask,roiActionTask}, false, false, null, true);
		}else{
			//Not show wait dialog
			ClientTaskDispatcher.dispatch(PDEDataViewer.this, new Hashtable<String, Object>(), new AsynchClientTask[] {waitTask,roiActionTask});
		}

	}catch(Throwable e){
		PopupGenerator.showErrorDialog(this, "Error calculating statistics\n"+e.getMessage(), e);
	}
}


private BitSet getFillROI(SpatialSelectionVolume spatialSelectionVolume){
	if(spatialSelectionVolume.getCurveSelectionInfo().getCurve()instanceof SinglePoint){
		return null;
	}
	BitSet fillROI = null;
	SSHelper ssHelper = spatialSelectionVolume.getIndexSamples(0, 1);
	if(ssHelper != null && ssHelper.getSampledIndexes()[0] == ssHelper.getSampledIndexes()[ssHelper.getSampledIndexes().length-1]){
		Point projMin = null;
		Point projMax = null;
		Point[] projVolCI = new Point[ssHelper.getSampledIndexes().length];
		for (int i = 0; i < ssHelper.getSampledIndexes().length; i++) {
			CoordinateIndex vCI = getPdeDataContext().getCartesianMesh().getCoordinateIndexFromVolumeIndex(ssHelper.getSampledIndexes()[i]);
			int normalAxis = getPDEDataContextPanel1().getNormalAxis();
			projVolCI[i] = new Point((int)Coordinate.convertAxisFromStandardXYZToNormal(vCI.x, vCI.y,vCI.z, Coordinate.X_AXIS, normalAxis),
					(int)Coordinate.convertAxisFromStandardXYZToNormal(vCI.x, vCI.y,vCI.z, Coordinate.Y_AXIS, normalAxis));
			if(i==0){
				projMin = new Point(projVolCI[i]);
				projMax = new Point(projMin);
			}else{
				if(projVolCI[i].x < projMin.x){
					projMin.x = projVolCI[i].x;
				}
				if(projVolCI[i].y < projMin.y){
					projMin.y = projVolCI[i].y;
				}
				if(projVolCI[i].x > projMax.x){
					projMax.x = projVolCI[i].x;
				}
				if(projVolCI[i].y > projMax.y){
					projMax.y = projVolCI[i].y;
				}
			}
		}
//		System.out.println(projMin+" "+projMax);
		int UNMARKED = 0;
		int BOUNDARY_MARK = 1;
		//Create work area
		int[][] markers = new int[projMax.y-projMin.y+1][projMax.x-projMin.x+1];
		Vector<Vector<Point>> allSeedsV = new Vector<Vector<Point>>();
		allSeedsV.add(null);
		allSeedsV.add(null);
		//Mark boundary
		for (int i = 0; i < projVolCI.length; i++) {
			markers[projVolCI[i].y-projMin.y][projVolCI[i].x-projMin.x] = BOUNDARY_MARK;
		}
		//Create seeds around boundary
		for (int i = 0; i < projVolCI.length; i++) {
			if(projVolCI[i].x-1 >= projMin.x){
				int currentMark = markers[projVolCI[i].y-projMin.y][projVolCI[i].x-projMin.x-1];
				if(currentMark == UNMARKED){
					Vector<Point> newSeedV = new Vector<Point>();
					newSeedV.add(new Point(projVolCI[i].x-1,projVolCI[i].y));
					markers[projVolCI[i].y-projMin.y][projVolCI[i].x-projMin.x-1] = allSeedsV.size();
					allSeedsV.add(newSeedV);
				}
			}
			if(projVolCI[i].x+1 <= projMax.x){
				int currentMark = markers[projVolCI[i].y-projMin.y][projVolCI[i].x-projMin.x+1];
				if(currentMark == UNMARKED){
					Vector<Point> newSeedV = new Vector<Point>();
					newSeedV.add(new Point(projVolCI[i].x+1,projVolCI[i].y));
					markers[projVolCI[i].y-projMin.y][projVolCI[i].x-projMin.x+1] = allSeedsV.size();
					allSeedsV.add(newSeedV);
				}
			}
			if(projVolCI[i].y-1 >= projMin.y){
				int currentMark = markers[projVolCI[i].y-projMin.y-1][projVolCI[i].x-projMin.x];
				if(currentMark == UNMARKED){
					Vector<Point> newSeedV = new Vector<Point>();
					newSeedV.add(new Point(projVolCI[i].x,projVolCI[i].y-1));
					markers[projVolCI[i].y-projMin.y-1][projVolCI[i].x-projMin.x] = allSeedsV.size();
					allSeedsV.add(newSeedV);
				}
			}
			if(projVolCI[i].y+1 <= projMax.y){
				int currentMark = markers[projVolCI[i].y-projMin.y+1][projVolCI[i].x-projMin.x];
				if(currentMark == UNMARKED){
					Vector<Point> newSeedV = new Vector<Point>();
					newSeedV.add(new Point(projVolCI[i].x,projVolCI[i].y+1));
					markers[projVolCI[i].y-projMin.y+1][projVolCI[i].x-projMin.x] = allSeedsV.size();
					allSeedsV.add(newSeedV);
				}
			}
		}
		
//		System.out.println("Seeds");
//		for (int i = 0; i < markers.length; i++) {
//			for (int j = 0; j < markers[i].length; j++) {
//				System.out.print((markers[i][j] < 10?"0":"")+markers[i][j]+" ");
//			}
//			System.out.println();
//		}
		
		//Grow seeds
		for (int i = 2; i < allSeedsV.size(); i++) {
			while(allSeedsV.elementAt(i) != null && allSeedsV.elementAt(i).size()>0){
				Point currentPoint = allSeedsV.elementAt(i).remove(0);
				if(currentPoint.x-1 >= projMin.x){
					int currentMark = markers[currentPoint.y-projMin.y][currentPoint.x-projMin.x-1];
					if(currentMark == UNMARKED){
						allSeedsV.elementAt(i).add(new Point(currentPoint.x-1,currentPoint.y));
						markers[currentPoint.y-projMin.y][currentPoint.x-projMin.x-1] = i;
					}else if(currentMark != BOUNDARY_MARK && currentMark != i){
						for (int j = 0; j < allSeedsV.elementAt(currentMark).size(); j++) {
							if(!allSeedsV.elementAt(i).contains(allSeedsV.elementAt(currentMark).elementAt(j))){
								allSeedsV.elementAt(i).add(allSeedsV.elementAt(currentMark).elementAt(j));
								markers
								[allSeedsV.elementAt(currentMark).elementAt(j).y-projMin.y]
								[allSeedsV.elementAt(currentMark).elementAt(j).x-projMin.x] = i;
							}
						}
						allSeedsV.setElementAt(null, currentMark);
					}
				}
				if(currentPoint.x+1 <= projMax.x){
					int currentMark = markers[currentPoint.y-projMin.y][currentPoint.x-projMin.x+1];
					if(currentMark == UNMARKED){
						allSeedsV.elementAt(i).add(new Point(currentPoint.x+1,currentPoint.y));
						markers[currentPoint.y-projMin.y][currentPoint.x-projMin.x+1] = i;
					}else if(currentMark != BOUNDARY_MARK && currentMark != i){
						for (int j = 0; j < allSeedsV.elementAt(currentMark).size(); j++) {
							if(!allSeedsV.elementAt(i).contains(allSeedsV.elementAt(currentMark).elementAt(j))){
								allSeedsV.elementAt(i).add(allSeedsV.elementAt(currentMark).elementAt(j));
								markers
								[allSeedsV.elementAt(currentMark).elementAt(j).y-projMin.y]
								[allSeedsV.elementAt(currentMark).elementAt(j).x-projMin.x] = i;
							}
						}
						allSeedsV.setElementAt(null, currentMark);
					}
				}
				if(currentPoint.y-1 >= projMin.y){
					int currentMark = markers[currentPoint.y-projMin.y-1][currentPoint.x-projMin.x];
					if(currentMark == UNMARKED){
						allSeedsV.elementAt(i).add(new Point(currentPoint.x,currentPoint.y-1));
						markers[currentPoint.y-projMin.y-1][currentPoint.x-projMin.x] = i;
					}else if(currentMark != BOUNDARY_MARK && currentMark != i){
						for (int j = 0; j < allSeedsV.elementAt(currentMark).size(); j++) {
							if(!allSeedsV.elementAt(i).contains(allSeedsV.elementAt(currentMark).elementAt(j))){
								allSeedsV.elementAt(i).add(allSeedsV.elementAt(currentMark).elementAt(j));
								markers
								[allSeedsV.elementAt(currentMark).elementAt(j).y-projMin.y]
								[allSeedsV.elementAt(currentMark).elementAt(j).x-projMin.x] = i;
							}
						}
						allSeedsV.setElementAt(null, currentMark);
					}
				}
				if(currentPoint.y+1 <= projMax.y){
					int currentMark = markers[currentPoint.y-projMin.y+1][currentPoint.x-projMin.x];
					if(currentMark == UNMARKED){
						allSeedsV.elementAt(i).add(new Point(currentPoint.x,currentPoint.y+1));
						markers[currentPoint.y-projMin.y+1][currentPoint.x-projMin.x] = i;
					}else if(currentMark != BOUNDARY_MARK && currentMark != i){
						for (int j = 0; j < allSeedsV.elementAt(currentMark).size(); j++) {
							if(!allSeedsV.elementAt(i).contains(allSeedsV.elementAt(currentMark).elementAt(j))){
								allSeedsV.elementAt(i).add(allSeedsV.elementAt(currentMark).elementAt(j));
								markers
								[allSeedsV.elementAt(currentMark).elementAt(j).y-projMin.y]
								[allSeedsV.elementAt(currentMark).elementAt(j).x-projMin.x] = i;
							}
						}
						allSeedsV.setElementAt(null, currentMark);
					}
				}				
			}
			allSeedsV.setElementAt(null, i);
		}

		int[] encodeEdge = new int[allSeedsV.size()];
		for (int i = 0; i < encodeEdge.length; i++) {
			encodeEdge[i] = i;
		}
		int c= 0;
		while(true){
			if(c<markers.length){
				encodeEdge[markers[c][0]] = UNMARKED;
				encodeEdge[markers[c][markers[0].length-1]] = UNMARKED;
			}
			if(c<markers[0].length){
				encodeEdge[markers[0][c]] = UNMARKED;
				encodeEdge[markers[markers.length-1][c]] = UNMARKED;
			}
			c++;
			if(c>=markers.length && c>=markers[0].length){
				break;
			}
		}
		encodeEdge[1] = 1;//boundary
		
//		System.out.println("Distinct Areas");
//		for (int i = 0; i < markers.length; i++) {
//			for (int j = 0; j < markers[i].length; j++) {
//				System.out.print((encodeEdge[markers[i][j]] < 10?"0":"")+encodeEdge[markers[i][j]]+" ");
//			}
//			System.out.println();
//		}
		
		//Make BitSet
		fillROI = new BitSet(getPdeDataContext().getDataValues().length);
		CoordinateIndex coordinateIndex = new CoordinateIndex();
		for (int y = 0; y < markers.length; y++) {
			for (int x = 0; x < markers[y].length; x++) {
				if(encodeEdge[markers[y][x]] != UNMARKED){
					coordinateIndex.x = projMin.x+x;
					coordinateIndex.y = projMin.y+y;
					coordinateIndex.z = getPDEDataContextPanel1().getSlice();
					Coordinate.convertCoordinateIndexFromNormalToStandardXYZ(
						coordinateIndex, getPDEDataContextPanel1().getNormalAxis());
//					System.out.println(coordinateIndex);
					int volIndex = getPdeDataContext().getCartesianMesh().getVolumeIndex(coordinateIndex);
					fillROI.set(volIndex);
				}
			}
		}

	}
	return fillROI;
}

void plotSpaceStats (TSJobResultsSpaceStats tsjrss) {
	//Determine if Volume or Membrane
	DataIdentifier[] diArr = getPdeDataContext().getDataIdentifiers();
	boolean bVolume = true;
	for(int i=0;i<diArr.length;i+= 1){
		if(diArr[i].getName().equals(tsjrss.getVariableNames()[0])){
			if(diArr[i].getVariableType().equals(VariableType.MEMBRANE) || diArr[i].getVariableType().equals(VariableType.MEMBRANE_REGION)){
				bVolume = false;
				break;
			}
		}
	}

	SymbolTableEntry[] symbolTableEntries = null;
	if(tsjrss.getVariableNames().length == 1){
		symbolTableEntries = new SymbolTableEntry[3/*4*/];//max.mean.min,sum
		if(getSimulation() != null && getSimulation().getMathDescription() != null){
			symbolTableEntries[0] = getSimulation().getMathDescription().getEntry(tsjrss.getVariableNames()[0]);
		}else{
			symbolTableEntries[0] = new SimpleSymbolTable(tsjrss.getVariableNames()).getEntry(tsjrss.getVariableNames()[0]);
		}
		symbolTableEntries[1] = symbolTableEntries[0];
		symbolTableEntries[2] = symbolTableEntries[0];
	}
	SymbolTableEntry[] finalSymbolTableEntries = symbolTableEntries;
	boolean finalBVolume = bVolume;
	PlotPane plotPane = new cbit.plot.PlotPane();
	plotPane.setPlot2D(
		new SingleXPlot2D(finalSymbolTableEntries,"Time",
		new String[] {
				"Max",
				(tsjrss.getWeightedMean() != null?"WeightedMean":"UnweightedMean"),
				"Min"/*,
				(tsjrss.getWeightedSum() != null?"WeightedSum":"UnweightedSum")*/},
		new double[][] {
				tsjrss.getTimes(),
				tsjrss.getMaximums()[0],
				(tsjrss.getWeightedMean() != null?tsjrss.getWeightedMean()[0]:tsjrss.getUnweightedMean()[0]),
				tsjrss.getMinimums()[0]/*,
				(tsjrss.getWeightedSum() != null?tsjrss.getWeightedSum()[0]:tsjrss.getUnweightedSum()[0])*/},
		new String[] {
			"Statistics Plot for "+tsjrss.getVariableNames()[0]+(tsjrss.getTotalSpace() != null?" (ROI "+(finalBVolume?"volume":"area")+"="+tsjrss.getTotalSpace()[0]+")":""),
			ReservedVariable.TIME.getName(),
			"[" + tsjrss.getVariableNames()[0] + "]"}));


	String title = "Statistics: ("+tsjrss.getVariableNames()[0]+") ";
	if (getSimulationModelInfo() != null) {
		title += getSimulationModelInfo().getContextName()+" "+getSimulationModelInfo().getSimulationName();
	}
		
	ChildWindowManager childWindowManager = ChildWindowManager.findChildWindowManager(PDEDataViewer.this);
	ChildWindow childWindow = childWindowManager.addChildWindow(plotPane,plotPane,title);
	childWindow.setIsCenteredOnParent();
	childWindow.pack();
	childWindow.show();
}		


private void roiAction(){
	BeanUtils.setCursorThroughout(this, Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	try{
		final String[] ROI_COLUMN_NAMES = new String[] {"ROI source","ROI source name","ROI Description"};
		final Vector<Object> auxInfoV = new Vector<Object>();
		
		final DataIdentifier dataIdentifier = getPdeDataContext().getDataIdentifier();
		VariableType variableType = dataIdentifier.getVariableType();
		final boolean isVolume = variableType.equals(VariableType.VOLUME) || variableType.equals(VariableType.VOLUME_REGION);
		
		DefaultTableModel tableModel = new DefaultTableModel(){
		    public boolean isCellEditable(int row, int column) {
		        return false;
		    }
		};
		for (int i = 0; i < ROI_COLUMN_NAMES.length; i++) {
			tableModel.addColumn(ROI_COLUMN_NAMES[i]);
		}
		//Add Snapshot ROI
		if((isVolume?volumeSnapshotROI:membraneSnapshotROI) != null){
			tableModel.addRow(new Object[] {(isVolume?"Volume":"Membrane")+" Variables and Functions",
						"Snapshot", (isVolume ? volumeSnapshotROIDescription : membraneSnapshotROIDescription) + ", (values = 1.0)"});
			auxInfoV.add((isVolume?volumeSnapshotROI:membraneSnapshotROI));
		}
		//Add user ROIs
		SpatialSelection[] userROIArr = getPDEDataContextPanel1().fetchSpatialSelections(true,false);
		for (int i = 0; userROIArr != null && i < userROIArr.length; i += 1) {
			String descr = null;
			boolean bPoint = false;
			if (isVolume) {
				if (userROIArr[i] instanceof SpatialSelectionVolume) {
					Curve curve = ((SpatialSelectionVolume)userROIArr[i]).getCurveSelectionInfo().getCurve();
					descr = curve.getDescription();
					if (curve instanceof SinglePoint) {
						bPoint = true;
					}
				}
			} else {
				if (userROIArr[i] instanceof SpatialSelectionMembrane) {
					SampledCurve selectionSource = ((SpatialSelectionMembrane)userROIArr[i]).getSelectionSource();
					descr = selectionSource.getDescription();
					if (selectionSource instanceof SinglePoint) {
						bPoint = true;
					}
				}
			}
			
			//Add Area User ROI
			BitSet fillBitSet = null;
			if(userROIArr[i] instanceof SpatialSelectionVolume){
				fillBitSet = getFillROI((SpatialSelectionVolume)userROIArr[i]);
				if(fillBitSet != null){
					tableModel.addRow(new Object[] {"User Defined",	descr, "Area Enclosed Volume ROI"});
					auxInfoV.add(fillBitSet);
				}
			}
			//Add Point and Line User ROI
			if(fillBitSet == null){
				tableModel.addRow(new Object[] {"User Defined", descr, (bPoint?"Point":"Line") + (isVolume?" Volume":" Membrane") + " ROI "} );
				auxInfoV.add(userROIArr[i]);
			}
		}
		//Add sorted Geometry ROI
		final CartesianMesh cartesianMesh = getPdeDataContext().getCartesianMesh();
		HashMap<Integer,?> regionMapSubvolumesHashMap = (isVolume ? cartesianMesh.getVolumeRegionMapSubvolume()	: cartesianMesh.getMembraneRegionMapSubvolumesInOut());
		Set<?> regionMapSubvolumesEntrySet = regionMapSubvolumesHashMap.entrySet();
		Iterator<?> regionMapSubvolumesEntryIter = regionMapSubvolumesEntrySet.iterator();
		TreeSet<Object[]> sortedGeomROITreeSet = new TreeSet<Object[]>(
			new Comparator<Object[]>(){
				public int compare(Object[] o1, Object[] o2) {
					int result = ((String)((Object[])o1[0])[1]).compareToIgnoreCase((String)((Object[])o2[0])[1]);
					if (result == 0){
						result = (((Entry<Integer, ?>)o1[1]).getKey()).compareTo(((Entry<Integer, ?>)o2[1]).getKey());
					}
					return result;
				}
			}
		);
		while(regionMapSubvolumesEntryIter.hasNext()){
			Entry<Integer,?> regionMapSubvolumesEntry = (Entry<Integer, ?>) regionMapSubvolumesEntryIter.next();
			sortedGeomROITreeSet.add(new Object[] {
				new Object[] { "Geometry",
					(isVolume ? getSimulationModelInfo().getVolumeNamePhysiology(((Integer)regionMapSubvolumesEntry.getValue()))
						: getSimulationModelInfo().getMembraneName(((int[])regionMapSubvolumesEntry.getValue())[0], ((int[])regionMapSubvolumesEntry.getValue())[1], false)),
					(isVolume ? "(svID="+regionMapSubvolumesEntry.getValue()+ " " : "(") + "vrID="+regionMapSubvolumesEntry.getKey()+") Predefined "
					+ (isVolume ? "volume" : "membrane") + " region"}, 
				regionMapSubvolumesEntry}
			);
		}
		Iterator<Object[]> sortedGeomROIIter = sortedGeomROITreeSet.iterator();
		while(sortedGeomROIIter.hasNext()){
			Object[] sortedGeomROIObjArr = (Object[])sortedGeomROIIter.next();
			tableModel.addRow((Object[])sortedGeomROIObjArr[0]);
			auxInfoV.add(sortedGeomROIObjArr[1]);
		}
		
		final ScrollTable roiTable = new ScrollTable();
		roiTable.setModel(tableModel);
		roiTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		roiTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
	
		final JPanel mainJPanel = new JPanel();
		BoxLayout mainBL = new BoxLayout(mainJPanel,BoxLayout.Y_AXIS);
		mainJPanel.setLayout(mainBL);
		
		JPanel timeJPanel = new JPanel();
		timeJPanel.setLayout(new FlowLayout());
		double[] timePoints = getPdeDataContext().getTimePoints();
		Double[] timePointsD = new Double[timePoints.length];
		for(int i=0;i<timePoints.length;i+= 1){
			timePointsD[i] = new Double(timePoints[i]);
		}
		final JComboBox jcb_time_begin = new JComboBox(timePointsD);
		final JComboBox jcb_time_end = new JComboBox(timePointsD);
		jcb_time_end.setSelectedIndex(timePointsD.length-1);
		timeJPanel.add(new JLabel("Begin Time:"));
		timeJPanel.add(jcb_time_begin);
		timeJPanel.add(Box.createHorizontalStrut(30));
		timeJPanel.add(new JLabel("End Time:"));
		timeJPanel.add(jcb_time_end);
		
		JPanel okCancelJPanel = new JPanel();
		BoxLayout okCancelBL = new BoxLayout(okCancelJPanel,BoxLayout.X_AXIS);
		okCancelJPanel.setLayout(okCancelBL);
		final JButton okButton = new JButton("OK");
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(((Double)jcb_time_begin.getSelectedItem()).compareTo((Double)jcb_time_end.getSelectedItem()) > 0){
					PopupGenerator.showErrorDialog(PDEDataViewer.this, "Selected 'Begin Time' must be less than or equal to 'End Time'");
					return;
				}
				int[] selectedRows = roiTable.getSelectedRows();
				if(selectedRows != null){
					try {
						BitSet dataBitSet = new BitSet(getPdeDataContext().getDataValues().length);
						for (int i = 0; i < selectedRows.length; i++) {
							Object auxInfo = auxInfoV.elementAt(selectedRows[i]);
							if(auxInfo instanceof BitSet){
								dataBitSet.or((BitSet)auxInfo);
							}else if(auxInfo instanceof SpatialSelectionMembrane){
								int[] roiIndexes =
									((SpatialSelectionMembrane)auxInfo).getIndexSamples().getSampledIndexes();
								for (int j = 0; j < roiIndexes.length; j += 1) {
									dataBitSet.set(roiIndexes[j], true);
								}
							}else if(auxInfo instanceof SpatialSelectionVolume){
								int[] roiIndexes =
									((SpatialSelectionVolume)auxInfo).getIndexSamples(0,1).getSampledIndexes();
								for (int j = 0; j < roiIndexes.length; j += 1) {
									dataBitSet.set(roiIndexes[j], true);
								}
							}else if (auxInfo instanceof Entry){
								if(isVolume){
									int volumeRegionID = (Integer)((Entry<Integer, Integer>)auxInfo).getKey();
									dataBitSet.or(cartesianMesh.getVolumeROIFromVolumeRegionID(volumeRegionID));
								}else{
									int membraneRegionID = (Integer)((Entry<Integer, int[]>)auxInfo).getKey();
									dataBitSet.or(cartesianMesh.getMembraneROIFromMembraneRegionID(membraneRegionID));
								}
							}else if(auxInfo instanceof BitSet){
								dataBitSet.or((BitSet)auxInfo);
							}else{
								throw new Exception("ROI table, Unknown data type: "+auxInfo.getClass().getName());
							}
						}
						TimeSeriesJobSpec timeSeriesJobSpec = new TimeSeriesJobSpec(
								new String[] {dataIdentifier.getName()}, new BitSet[] {dataBitSet},
								((Double)jcb_time_begin.getSelectedItem()).doubleValue(), 1,
								((Double)jcb_time_end.getSelectedItem()).doubleValue(),
								true,false, VCDataJobID.createVCDataJobID(getDataViewerManager().getUser(),	true));
						
						Hashtable<String, Object> hash = new Hashtable<String, Object>();
						hash.put(StringKey_timeSeriesJobSpec, timeSeriesJobSpec);
	
						AsynchClientTask task1 = new TimeSeriesDataRetrievalTask("Retrieve data for '" + dataIdentifier + "'");
						AsynchClientTask task2 = new AsynchClientTask("Showing stat for '" + dataIdentifier + "'", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
	
							@Override
							public void run(Hashtable<String, Object> hashTable) throws Exception {
								TSJobResultsSpaceStats tsJobResultsSpaceStats = (TSJobResultsSpaceStats)hashTable.get(StringKey_timeSeriesJobResults);
								plotSpaceStats(tsJobResultsSpaceStats);
							}
						};	
						ClientTaskDispatcher.dispatch(PDEDataViewer.this, hash, new AsynchClientTask[] { task1, task2 }, true, true, null);						
					} catch (Exception e1) {
						e1.printStackTrace();
						PopupGenerator.showErrorDialog(PDEDataViewer.this, "ROI Error.\n"+e1.getMessage(), e1);
					}
				}
				BeanUtils.disposeParentWindow(mainJPanel);
			}
		});
		okCancelJPanel.add(okButton);
		roiTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				if(roiTable.getSelectedRows() != null && roiTable.getSelectedRows().length > 0){
					okButton.setEnabled(true);
				}else{
					okButton.setEnabled(false);
				}
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				BeanUtils.disposeParentWindow(mainJPanel);
			}
		});
		okCancelJPanel.add(cancelButton);
	
		mainJPanel.add(timeJPanel);
		mainJPanel.add(roiTable.getEnclosingScrollPane());
		mainJPanel.add(okCancelJPanel);
	
//		showComponentInFrame(mainJPanel,
//			"Calculate "+(isVolume?"volume":"membrane")+" statistics for '"+getPdeDataContext().getVariableName()+"'."+
//			"  Choose times and 1 or more ROI(s).");
		Frame dialogOwner = JOptionPane.getFrameForComponent(this);
		JOptionPane inputDialog = new JOptionPane(mainJPanel, JOptionPane.PLAIN_MESSAGE, 0, null, new Object[0]);
		final JDialog d = inputDialog.createDialog(dialogOwner, "Calculate "+(isVolume?"volume":"membrane")+" statistics for '"+getPdeDataContext().getVariableName()+"'."+
				"  Choose times and 1 or more ROI(s).");
		d.setResizable(true);
		try {
			DialogUtils.showModalJDialogOnTop(d,PDEDataViewer.this);
		}finally {
			d.dispose();
		}		
	
	} finally {
		BeanUtils.setCursorThroughout(this, Cursor.getDefaultCursor());
	}

}

/**
 * connEtoC2:  (JButtonSpatial.action.actionPerformed(java.awt.event.ActionEvent) --> PDEDataViewer.refireActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.showSpatialPlot();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC4:  (KymographJButton.action.actionPerformed(java.awt.event.ActionEvent) --> PDEDataViewer.showKymograph()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.showKymograph();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC9:  (JButtonStatistics.action.actionPerformed(java.awt.event.ActionEvent) --> PDEDataViewer.calcStatistics(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC9(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.calcStatistics(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


public void dataJobMessage(final DataJobEvent dje) {
	for (DataJobListener djl : dataJobListenerList) {
		djl.dataJobMessage(dje);
	}
}
	
/**
 * Insert the method's description here.
 * Creation date: (9/25/2005 1:53:00 PM)
 */
private DataValueSurfaceViewer getDataValueSurfaceViewer() {

	try{
	if(fieldDataValueSurfaceViewer == null){
		//Surfaces
		CartesianMesh cartesianMesh = getPdeDataContext().getCartesianMesh();
		meshRegionSurfaces = new MeshDisplayAdapter(cartesianMesh).generateMeshRegionSurfaces();
		SurfaceCollection surfaceCollection = meshRegionSurfaces.getSurfaceCollection();

		//SurfaceNames
		final String[] surfaceNames = new String[meshRegionSurfaces.getSurfaceCollection().getSurfaceCount()];
		for (int i = 0; i < meshRegionSurfaces.getSurfaceCollection().getSurfaceCount(); i++){
			MembraneElement me = //Get the first element, any will do, all have same inside/outside volumeIndex
				cartesianMesh.getMembraneElements()[meshRegionSurfaces.getMembraneIndexForPolygon(i,0)];
			if(getSimulationModelInfo() != null){
				surfaceNames[i] = getSimulationModelInfo().getMembraneName(
					cartesianMesh.getSubVolumeFromVolumeIndex(me.getInsideVolumeIndex()),
					cartesianMesh.getSubVolumeFromVolumeIndex(me.getOutsideVolumeIndex()), false
				);
			}else{
				surfaceNames[i] = i+"";
			}
		}

		//SurfaceAreas
		final Double[] surfaceAreas = new Double[meshRegionSurfaces.getSurfaceCollection().getSurfaceCount()];
		for (int i = 0; i < meshRegionSurfaces.getSurfaceCollection().getSurfaceCount(); i++){
			surfaceAreas[i] = new Double(cartesianMesh.getRegionMembraneSurfaceAreaFromMembraneIndex(meshRegionSurfaces.getMembraneIndexForPolygon(i,0)));
		}

		DataValueSurfaceViewer fieldDataValueSurfaceViewer0 = new DataValueSurfaceViewer();

		TaubinSmoothing taubinSmoothing = new TaubinSmoothingWrong();
		TaubinSmoothingSpecification taubinSpec = TaubinSmoothingSpecification.getInstance(.3);
		taubinSmoothing.smooth(surfaceCollection,taubinSpec);
		fieldDataValueSurfaceViewer0.init(
			meshRegionSurfaces.getSurfaceCollection(),
			cartesianMesh.getOrigin(),
			cartesianMesh.getExtent(),
			surfaceNames,
			surfaceAreas,
			cartesianMesh.getGeometryDimension()
		);

		fieldDataValueSurfaceViewer = fieldDataValueSurfaceViewer0;
	}
	}catch(Exception e){
		PopupGenerator.showErrorDialog(PDEDataViewer.this, e.getMessage(), e);
	}

	return fieldDataValueSurfaceViewer;
}


/**
 * Return the ExportData property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getExportData() {
	if (ivjExportData == null) {
		try {
			ivjExportData = new javax.swing.JPanel();
			ivjExportData.setName("ExportData");
			ivjExportData.setLayout(new java.awt.BorderLayout());
			getExportData().add(getPDEExportPanel1(), "Center");
			getExportData().add(getExportMonitorPanel1(), "South");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjExportData;
}

/**
 * Method generated to support the promotion of the exportMonitorPanel attribute.
 * @return cbit.vcell.export.ExportMonitorPanel
 */
public ExportMonitorPanel getExportMonitorPanel() {
	return getExportMonitorPanel1();
}


/**
 * Return the ExportMonitorPanel1 property value.
 * @return cbit.vcell.export.ExportMonitorPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ExportMonitorPanel getExportMonitorPanel1() {
	if (ivjExportMonitorPanel1 == null) {
		try {
			LineBorderBean ivjLocalBorder1;
			ivjLocalBorder1 = new LineBorderBean();
			ivjLocalBorder1.setLineColor(Color.blue);
			TitledBorderBean ivjLocalBorder;
			ivjLocalBorder = new TitledBorderBean();
			ivjLocalBorder.setBorder(ivjLocalBorder1);
			ivjLocalBorder.setTitle("Export jobs");
			ivjExportMonitorPanel1 = new ExportMonitorPanel();
			ivjExportMonitorPanel1.setName("ExportMonitorPanel1");
			ivjExportMonitorPanel1.setPreferredSize(new Dimension(453, 150));
			ivjExportMonitorPanel1.setBorder(ivjLocalBorder);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjExportMonitorPanel1;
}

/**
 * Return the JButtonSpatial property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
//private javax.swing.JButton getJButtonSpatial() {
//	if (ivjJButtonSpatial == null) {
//		try {
//			ivjJButtonSpatial = new JButton();
//			ivjJButtonSpatial.setName("JButtonSpatial");
//			ivjJButtonSpatial.setText("Show Spatial Plot");
//			// user code begin {1}
//			// user code end
//		} catch (Throwable ivjExc) {
//			// user code begin {2}
//			// user code end
//			handleException(ivjExc);
//		}
//	}
//	return ivjJButtonSpatial;
//}


/**
 * Return the JButtonStatistics property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
//private javax.swing.JButton getJButtonStatistics() {
//	if (ivjJButtonStatistics == null) {
//		try {
//			ivjJButtonStatistics = new javax.swing.JButton();
//			ivjJButtonStatistics.setName("JButtonStatistics");
//			ivjJButtonStatistics.setText("Statistics");
//			// user code begin {1}
//			// user code end
//		} catch (java.lang.Throwable ivjExc) {
//			// user code begin {2}
//			// user code end
//			handleException(ivjExc);
//		}
//	}
//	return ivjJButtonStatistics;
//}

private void openInVisit() {
	try {
		
//		if (!VCellVisitUtils.checkVisitResourcePrerequisites(this,"visit2_2_2"))   //This should be a property
//		{
//			System.out.println("VCellVisitUtils.checkVisitResourcePrerequisites returned false, meaning prerequisite tests failed");
//			return; //return without opening Visit or VisitControlPanel
//			
//		}
//		
//		System.out.println("Visit prerequisites tests passed");
		
		 /*
		 VCDataIdentifier vcDId = getPdeDataContext().getDataIdentifier();
		 if vcDId instanceOf VCSim
		 KeyValue fieldDataKey = getPdeDataContext().getDataIdentifier(
		 String simFileToOpen = SimulationData.createCanonicalSimLogFileName(fieldDataKey, jobIndex, isOldStyle);*/
		KeyValue fieldDataKey = null;
		int jobIndex = 0;
		boolean isOldStyle = false;
		VCDataIdentifier vcdid = getPdeDataContext().getVCDataIdentifier();
		if (vcdid instanceof VCSimulationDataIdentifier){
			fieldDataKey = ((VCSimulationDataIdentifier)vcdid).getSimulationKey();
			jobIndex = ((VCSimulationDataIdentifier)vcdid).getJobIndex();
		}else if (vcdid instanceof VCSimulationDataIdentifierOldStyle){
			isOldStyle = true;
			fieldDataKey = ((VCSimulationDataIdentifierOldStyle)vcdid).getSimulationKey();
			jobIndex = ((VCSimulationDataIdentifierOldStyle)vcdid).getJobIndex();
		}else{
			throw new RuntimeException("Unknown VCDataIdentifier type "+vcdid.getClass().getName());
		}

		String simlogname = SimulationData.createCanonicalSimLogFileName(fieldDataKey, jobIndex, isOldStyle);
		
		//Try to figure out where the Visit executable is. Check some educated guesses first, then ask the user as a last resort
		//
		String visitBinDirProp = null;
		if (ResourceUtil.bMac || ResourceUtil.bLinux) {
		
			//visitBinDirProp = new File(ResourceUtil.getVcellHome(),"visit2_2_2/bin").getCanonicalPath();    //TODO: Replace visit2_2_2 with property of latest VCell supported version
			visitBinDirProp = "/home/VCELL/visit/visit2_3_2.linux-x86_64/bin";
		}
		else {
				visitBinDirProp = PropertyLoader.getProperty("vcell.visit.installexe", null);
		}
		
		System.out.println("visitBinDirProp = " + visitBinDirProp);
		
		if (visitBinDirProp != null && (new File(visitBinDirProp,"visit.exe").exists()|| new File(visitBinDirProp,"visit").exists())) {
			visitBinDir=visitBinDirProp;
		}else {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Please locate the VisIt executable");
			chooser.showOpenDialog(this);
			if (!chooser.getSelectedFile().getName().toLowerCase().startsWith("visit")) {
				DialogUtils.showErrorDialog(this, "Expecting filename to begin with 'visit'");
				return;
			}
			visitBinDir=chooser.getSelectedFile().getParentFile().getAbsolutePath();
		}
		
		//final JDesktopPaneEnhanced desktopPane = (JDesktopPaneEnhanced) JOptionPane.getDesktopPaneForComponent(this);
		
		final VisitSession visitSession = getDataViewerManager().getRequestManager().createNewVisitSession(visitBinDir);
		visitSession.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(VisitSession.PROPERTY_NAME_VIEWER_WAS_CLOSED)) {
					ChildWindowManager childWindowManager = ChildWindowManager.findChildWindowManager(PDEDataViewer.this);
					ChildWindow childWindow = childWindowManager.getChildWindowFromContext(visitSession);
					if (childWindow!=null){
						childWindow.close();
					}
				}
			}
		});
		
		visitSession.openDatabase(getSimulation().getVersion().getOwner(), simlogname);
		
		String varName = getPdeDataContext().getVariableName();
		//String varName = "s0";
		visitSession.addAndDrawPseudocolorPlot(varName);

		VisitControlPanel visitControlPanel= new VisitControlPanel();
		visitControlPanel.setPdeDataContext(getPdeDataContext(), getPdeDataContext().getCartesianMesh().getOrigin(), getPdeDataContext().getCartesianMesh().getExtent());
		visitControlPanel.init(getPdeDataContext().getDataIdentifier(),visitSession);
		final boolean[] bClose = visitSession.pollViewerPoll();

		ChildWindowListener childWindowListener = new ChildWindowListener() {		
			public void closing(ChildWindow childWindow) {
				visitSession.close();
				bClose[0]=true;
			}		
			public void closed(ChildWindow childWindow) {
			}
		};

		ChildWindow childWindow = ChildWindowManager.findChildWindowManager(PDEDataViewer.this).addChildWindow(visitControlPanel, visitSession, "Visit Control Panel");
		childWindow.setIsCenteredOnParent();
		childWindow.pack();
		childWindow.addChildWindowListener(childWindowListener);
		childWindow.show();

	} catch(Exception e1) {
		e1.printStackTrace();
	}
}


private void openInLocalVisit() {
	try {
		
//		if (!VCellVisitUtils.checkVisitResourcePrerequisites(this,"visit2_2_2"))   //This should be a property
//		{
//			System.out.println("VCellVisitUtils.checkVisitResourcePrerequisites returned false, meaning prerequisite tests failed");
//			return; //return without opening Visit or VisitControlPanel
//			
//		}
//		
//		System.out.println("Visit prerequisites tests passed");
		
		 /*
		 VCDataIdentifier vcDId = getPdeDataContext().getDataIdentifier();
		 if vcDId instanceOf VCSim
		 KeyValue fieldDataKey = getPdeDataContext().getDataIdentifier(
		 String simFileToOpen = SimulationData.createCanonicalSimLogFileName(fieldDataKey, jobIndex, isOldStyle);*/
		KeyValue fieldDataKey = null;
		int jobIndex = 0;
		boolean isOldStyle = false;
		final boolean[] isLocalRun = new boolean[] {false};
		VCDataIdentifier vcdid = getPdeDataContext().getVCDataIdentifier();
		if (vcdid instanceof VCSimulationDataIdentifier){
			fieldDataKey = ((VCSimulationDataIdentifier)vcdid).getSimulationKey();
			jobIndex = ((VCSimulationDataIdentifier)vcdid).getJobIndex();
		}else if (vcdid instanceof VCSimulationDataIdentifierOldStyle){
			isOldStyle = true;
			fieldDataKey = ((VCSimulationDataIdentifierOldStyle)vcdid).getSimulationKey();
			jobIndex = ((VCSimulationDataIdentifierOldStyle)vcdid).getJobIndex();
		}else{
			throw new RuntimeException("Unknown VCDataIdentifier type "+vcdid.getClass().getName());
		}

		final String simlogname = SimulationData.createCanonicalSimLogFileName(fieldDataKey, jobIndex, isOldStyle);
		final File[] localDir = new File[1];
		if (vcdid instanceof LocalVCDataIdentifier){
			localDir[0] = ((LocalVCDataIdentifier)vcdid).getLocalDirectory();
			if (localDir[0] == null) {throw new RuntimeException("local directory not found");}
			isLocalRun[0] = true;
		}
		
		//Try to figure out where the Visit executable is. Check some educated guesses first, then ask the user as a last resort
		//
		String visitBinDirProp = null;
		if (ResourceUtil.bMac || ResourceUtil.bLinux) {
		
			//visitBinDirProp = new File(ResourceUtil.getVcellHome(),"visit2_2_2/bin").getCanonicalPath();    //TODO: Replace visit2_2_2 with property of latest VCell supported version
			//visitBinDirProp = "/home/VCELL/visit/visit2_3_2.linux-x86_64/bin";
		}
		else {
				visitBinDirProp = PropertyLoader.getProperty("vcell.visit.installexe", null);
		}
		
		System.out.println("visitBinDirProp = " + visitBinDirProp);
		final StringBuffer visitLocalBin = new StringBuffer();
		if (visitBinDirProp != null && (new File(visitBinDirProp,"visit.exe").exists()|| new File(visitBinDirProp,"visit").exists())) {
			visitBinDir=visitBinDirProp;
			visitLocalBin.append(visitBinDirProp+"/visit");
		}else {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Please locate the VisIt executable");
			chooser.showOpenDialog(this);
			if (chooser.getSelectedFile()==null) {return;}
			if (!chooser.getSelectedFile().getName().toLowerCase().startsWith("visit")) {
				DialogUtils.showErrorDialog(this, "Expecting filename to begin with 'visit'");
				return;
			}
			if (ResourceUtil.bMac || ResourceUtil.bLinux) {
				visitLocalBin.append(chooser.getSelectedFile().getParentFile().getCanonicalPath()+File.separator+"visit");
				System.out.println("Unix visitLocalBin= "+visitLocalBin);
			}
			else {
			
				visitLocalBin.append(chooser.getSelectedFile().getCanonicalPath());
			}
		}
		
//		JFileChooser simResultsChooser = new JFileChooser();visit2_3_2.linux-x86_64
//		simResultsChooser.setDialogTitle("Please locate .log file");
//		simResultsChooser.showOpenDialog(this);
		
		//String localSimResultLogFile = simResultsChooser.getSelectedFile().getCanonicalPath();
			new Thread(new Runnable() {
				
				public void run() {
					// TODO Auto-generated method stub
					try {
						VisitConnectionInfo localVisitConnInfo =  getDataViewerManager().getRequestManager().createNewLocalVisitSessionConnectionInfo(visitLocalBin.toString());
						String varName = getPdeDataContext().getVariableName();	
						
//						String visitMDServerHost = PropertyLoader.getProperty("vcell.visit.mdserverhost", null);
//						String serverResultsDataPath = PropertyLoader.getProperty("vcell.primarySimdatadir", null);
						
						String visitMDServerHost = localVisitConnInfo.getIPAddress();
						String serverResultsDataPath = localVisitConnInfo.getDataPath();
						
						if (visitMDServerHost == null || serverResultsDataPath == null) {
							System.out.println("Unable to get properties from the server");
							return;
						}

						
							final ArrayList<String> args = new ArrayList<String>();
							args.add(visitLocalBin.toString());
							args.add("-o");
							if (isLocalRun[0]) {
								args.add(new File(localDir[0],simlogname).getPath());
							}
							else
							{
							 
								args.add(visitMDServerHost+":"+serverResultsDataPath+"/"+getSimulation().getVersion().getOwner().getName()+"/"+simlogname);
							}
							System.out.println("args: "+args);
							
							final Executable executable = new Executable(args.toArray(new String[0]));

						executable.start();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						DialogUtils.showErrorDialog(PDEDataViewer.this, e.getMessage());
					}
				}
			}).start();
			
//			AsynchClientTask visitTask = new AsynchClientTask("Starting Local Visit Executable", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
//				
//				@Override
//				public void run(Hashtable<String, Object> hashTable) throws Exception {
//					Runtime.getRuntime().exec(args.toArray(new String[0]));
//					//executable.start();
//					
//				}
//			};
//			ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] {visitTask});
			
			

	} catch(Exception e1) {
		e1.printStackTrace();
	}
}

private JButton ivjButtonVisit;
private JButton getJButtonVisit(){
	if (ivjButtonVisit == null) {
		ivjButtonVisit = new javax.swing.JButton();
		ivjButtonVisit.setVisible(true);
		ivjButtonVisit.setName("JButtonVisit");
		ivjButtonVisit.setText("Open in VisIt");
		ivjButtonVisit.addActionListener(ivjEventHandler);
		ivjButtonVisit.setEnabled(true);

	}
	return ivjButtonVisit;
	
}

private JButton ivjButtonLocalVisit;
private JButton getJButtonLocalVisit(){
	if (ivjButtonLocalVisit == null) {
		ivjButtonLocalVisit = new javax.swing.JButton();
		ivjButtonLocalVisit.setVisible(true);
		ivjButtonLocalVisit.setName("JButtonLocalVisit");
		ivjButtonLocalVisit.setText("Open in Local VisIt");
		ivjButtonLocalVisit.addActionListener(ivjEventHandler);
		ivjButtonLocalVisit.setEnabled(true);

	}
	return ivjButtonLocalVisit;
	
}

private void snapshotROI() {
	final AsynchClientTask createSnapshotTask = new AsynchClientTask("Creating Snapshot...",AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			if(getClientTaskStatusSupport() != null){
				getClientTaskStatusSupport().setMessage("Waiting for data to refresh...");
			}
			getPdeDataContext().waitWhileBusy();
			if(getClientTaskStatusSupport() != null){
				getClientTaskStatusSupport().setMessage("Creating Snapshot...");
			}
			final double[] dataValues = getPdeDataContext().getDataValues();
			final VariableType variableType = getPdeDataContext().getDataIdentifier().getVariableType();
			final boolean isVolumeType = (variableType.equals(VariableType.VOLUME) ||	variableType.equals(VariableType.VOLUME_REGION));
			final BitSet snapshotROI = new BitSet(dataValues.length);
			for (int i = 0; i < dataValues.length; i++) {
				boolean bInDomain = (getPDEDataContextPanel1().getDataInfoProvider()==null?true:getPDEDataContextPanel1().getDataInfoProvider().isDefined(i));
				snapshotROI.set(i,bInDomain && (dataValues[i] == 1.0));
			}
			final String variableName = getPdeDataContext().getVariableName();
			final double timePoint = getPdeDataContext().getTimePoint();
			//Do the following so the 'progress' spinner will go away (if showing) when the message is displayed.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if(snapshotROI.cardinality() == 0){
						PopupGenerator.showWarningDialog(PDEDataViewer.this,(isVolumeType?"Volume":"Membrane")+" snapshot ROI cannot be updated.\n"+
								"No data values for variable '"+variableName+"'\n"+
								"at time '"+timePoint+"' have values equal to 1.0."+
								"  Add a function that evaluates to 1 at the points to be included in the ROI (user defined funtions can be added from the 'Simulation' panel), "+
								" then choose the new function name in the Simulation results viewer and press 'Snapshot ROI' again."
								,new String[] {"OK"},"OK");
						return;
					}else{
						if(isVolumeType){
							volumeSnapshotROI = snapshotROI;
							volumeSnapshotROIDescription = "Variable='"+variableName+"', Timepoint= "+timePoint;
						}else{
							membraneSnapshotROI = snapshotROI;
							membraneSnapshotROIDescription = "Variable='"+variableName+"', Timepoint= "+timePoint;
						}
						PopupGenerator.showWarningDialog(PDEDataViewer.this,(isVolumeType?"Volume":"Membrane")+" snapshot ROI updated using "+
								"Variable '"+variableName+"' at "+"Time '"+timePoint+"' (where values = 1.0).\n"+
								"ROI includes "+snapshotROI.cardinality()+" points.  (total size= "+dataValues.length+")\n"+
								"Snapshot ROI is available for use by choosing a variable/function name and pressing 'Statistics'."
								,new String[] {"OK"},"OK");						
					}
				}
			});
		}
	};
	if(getPdeDataContext().isBusy()){
		//Show wait dialog
		ClientTaskDispatcher.dispatch(PDEDataViewer.this, new Hashtable<String, Object>(),
				new AsynchClientTask[] {createSnapshotTask}, false, false, null, true);
	}else{
		//Not show wait dialog
		ClientTaskDispatcher.dispatch(PDEDataViewer.this, new Hashtable<String, Object>(),
				new AsynchClientTask[] {createSnapshotTask});
	}
}

//private javax.swing.JButton getJButtonSnapshotROI() {
//	if (ivjJButtonSnapshotROI == null) {
//		try {
//			ivjJButtonSnapshotROI = new javax.swing.JButton();
//			ivjJButtonSnapshotROI.setName("JButtonSnapshotROI");
//			ivjJButtonSnapshotROI.setText("Snapshot ROI");
//			ivjJButtonSnapshotROI.addActionListener(new ActionListener(){
//				public void actionPerformed(ActionEvent e) {
//					final AsynchClientTask createSnapshotTask = new AsynchClientTask("Creating Snapshot...",AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
//						@Override
//						public void run(Hashtable<String, Object> hashTable) throws Exception {
//							if(getClientTaskStatusSupport() != null){
//								getClientTaskStatusSupport().setMessage("Waiting for data to refresh...");
//							}
//							getPdeDataContext().waitWhileBusy();
//							if(getClientTaskStatusSupport() != null){
//								getClientTaskStatusSupport().setMessage("Creating Snapshot...");
//							}
//							final double[] dataValues = getPdeDataContext().getDataValues();
//							final VariableType variableType = getPdeDataContext().getDataIdentifier().getVariableType();
//							final boolean isVolumeType = (variableType.equals(VariableType.VOLUME) ||	variableType.equals(VariableType.VOLUME_REGION));
//							final BitSet snapshotROI = new BitSet(dataValues.length);
//							for (int i = 0; i < dataValues.length; i++) {
//								boolean bInDomain = (getPDEDataContextPanel1().getDataInfoProvider()==null?true:getPDEDataContextPanel1().getDataInfoProvider().isDefined(i));
//								snapshotROI.set(i,bInDomain && (dataValues[i] == 1.0));
//							}
//							final String variableName = getPdeDataContext().getVariableName();
//							final double timePoint = getPdeDataContext().getTimePoint();
//							//Do the following so the 'progress' spinner will go away (if showing) when the message is displayed.
//							SwingUtilities.invokeLater(new Runnable() {
//								public void run() {
//									if(snapshotROI.cardinality() == 0){
//										PopupGenerator.showWarningDialog(PDEDataViewer.this,(isVolumeType?"Volume":"Membrane")+" snapshot ROI cannot be updated.\n"+
//												"No data values for variable '"+variableName+"'\n"+
//												"at time '"+timePoint+"' have values equal to 1.0."+
//												"  Add a function that evaluates to 1 at the points to be included in the ROI (user defined funtions can be added from the 'Simulation' panel), "+
//												" then choose the new function name in the Simulation results viewer and press 'Snapshot ROI' again."
//												,new String[] {"OK"},"OK");
//										return;
//									}else{
//										if(isVolumeType){
//											volumeSnapshotROI = snapshotROI;
//											volumeSnapshotROIDescription = "Variable='"+variableName+"', Timepoint= "+timePoint;
//										}else{
//											membraneSnapshotROI = snapshotROI;
//											membraneSnapshotROIDescription = "Variable='"+variableName+"', Timepoint= "+timePoint;
//										}
//										PopupGenerator.showWarningDialog(PDEDataViewer.this,(isVolumeType?"Volume":"Membrane")+" snapshot ROI updated using "+
//												"Variable '"+variableName+"' at "+"Time '"+timePoint+"' (where values = 1.0).\n"+
//												"ROI includes "+snapshotROI.cardinality()+" points.  (total size= "+dataValues.length+")\n"+
//												"Snapshot ROI is available for use by choosing a variable/function name and pressing 'Statistics'."
//												,new String[] {"OK"},"OK");						
//									}
//								}
//							});
//						}
//					};
//					if(getPdeDataContext().isBusy()){
//						//Show wait dialog
//						ClientTaskDispatcher.dispatch(PDEDataViewer.this, new Hashtable<String, Object>(),
//								new AsynchClientTask[] {createSnapshotTask}, false, false, null, true);
//					}else{
//						//Not show wait dialog
//						ClientTaskDispatcher.dispatch(PDEDataViewer.this, new Hashtable<String, Object>(),
//								new AsynchClientTask[] {createSnapshotTask});
//					}
//				}});
//			// user code begin {1}
//			// user code end
//		} catch (java.lang.Throwable ivjExc) {
//			// user code begin {2}
//			// user code end
//			handleException(ivjExc);
//		}
//	}
//	return ivjJButtonSnapshotROI;
//}

/**
 * Return the JPanel1 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanelButtons() {
	if (ivjJPanelButtons == null) {
		try {
			ivjJPanelButtons = new javax.swing.JPanel();
			ivjJPanelButtons.setName("JPanelButtons");
			ivjJPanelButtons.add(getPlotButton());
			ivjJPanelButtons.add(getROIButton());
			if (DocumentWindowAboutBox.getVERSION_NO().compareTo("5.0") > 0) {
				ivjJPanelButtons.add(getJButtonVisit());
				ivjJPanelButtons.add(getJButtonLocalVisit());
			}
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanelButtons;
}

/**
 * Return the JTabbedPane1 property value.
 * @return javax.swing.JTabbedPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTabbedPane getJTabbedPane1() {
	if (ivjJTabbedPane1 == null) {
		try {
			ivjJTabbedPane1 = new javax.swing.JTabbedPane();
			ivjJTabbedPane1.setName("JTabbedPane1");
			ivjJTabbedPane1.insertTab("View Data", null, getViewData(), null, 0);
			ivjJTabbedPane1.insertTab(EXPORT_DATA_TABNAME, null, getExportData(), null, 1);
			dataProcessingResultsPanel = new DataProcessingResultsPanel();
			ivjJTabbedPane1.addTab(POST_PROCESS_TABNAME, dataProcessingResultsPanel);
			ivjJTabbedPane1.addChangeListener(
				new ChangeListener(){
					public void stateChanged(ChangeEvent e) {
						if(ivjJTabbedPane1.getSelectedIndex() == ivjJTabbedPane1.indexOfTab(EXPORT_DATA_TABNAME)){
							SpatialSelection[] spatialSelectionsVolume =
								getPDEDataContextPanel1().fetchSpatialSelectionsAll(VariableType.VOLUME);
							SpatialSelection[] spatialSelectionsMembrane =
								getPDEDataContextPanel1().fetchSpatialSelectionsAll(VariableType.MEMBRANE);
							getPDEExportPanel1().setSpatialSelections(spatialSelectionsVolume, spatialSelectionsMembrane,getPDEDataContextPanel1().getViewZoom());
						}else if(ivjJTabbedPane1.getSelectedIndex() == ivjJTabbedPane1.indexOfTab(POST_PROCESS_TABNAME)){
							dataProcessingResultsPanel.update(getPdeDataContext());
						}
					}
				}
			);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJTabbedPane1;
}

/**
 * Return the KymographJButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
//private javax.swing.JButton getKymographJButton() {
//	if (ivjKymographJButton == null) {
//		try {
//			ivjKymographJButton = new javax.swing.JButton();
//			ivjKymographJButton.setName("KymographJButton");
//			ivjKymographJButton.setText("Show Kymograph");
//			// user code begin {1}
//			// user code end
//		} catch (java.lang.Throwable ivjExc) {
//			// user code begin {2}
//			// user code end
//			handleException(ivjExc);
//		}
//	}
//	return ivjKymographJButton;
//}

private javax.swing.JButton getPlotButton() {
	if (plotButton == null) {
		try {
			plotButton = new javax.swing.JButton("Plot");
			plotButton.setHorizontalTextPosition(SwingConstants.LEFT);
			plotButton.setIcon(new DownArrowIcon());
			plotButton.addActionListener(ivjEventHandler);
			plotButton.setEnabled(false);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return plotButton;
}

private javax.swing.JButton getROIButton() {
	if (roiButton == null) {
		try {
			roiButton = new javax.swing.JButton("ROI");
			roiButton.setHorizontalTextPosition(SwingConstants.LEFT);
			roiButton.setIcon(new DownArrowIcon());
			roiButton.addActionListener(ivjEventHandler);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return roiButton;
}

private javax.swing.JPopupMenu getROIPopupMenu() {
	if (roiPopupMenu == null) {
		try {
			roiPopupMenu = new JPopupMenu();
			snapShotMenuItem = new JMenuItem("Snapshot ROI");
			snapShotMenuItem.addActionListener(ivjEventHandler);
			statisticsMenuItem = new JMenuItem("Statistics");
			statisticsMenuItem.addActionListener(ivjEventHandler);
			roiPopupMenu.add(snapShotMenuItem);
			roiPopupMenu.add(statisticsMenuItem);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return roiPopupMenu;
}

private JMenuItem getSpatialPlotMenuItem() {
	if (spatialPlotMenuItem == null) {
		spatialPlotMenuItem = new JMenuItem("Spatial");
		spatialPlotMenuItem.addActionListener(ivjEventHandler);
		spatialPlotMenuItem.setEnabled(false);
	}
	return spatialPlotMenuItem;
}
private JMenuItem getTimePlotMenuItem() {
	if (timePlotMenuItem == null) {
		timePlotMenuItem = new JMenuItem("Time");
		timePlotMenuItem.addActionListener(ivjEventHandler);
		timePlotMenuItem.setEnabled(false);
	}
	return timePlotMenuItem;
}
private JMenuItem getKymographMenuItem() {
	if (kymographMenuItem == null) {
		kymographMenuItem = new JMenuItem("Kymograph");		
		kymographMenuItem.addActionListener(ivjEventHandler);
		kymographMenuItem.setEnabled(false);
	}
	return kymographMenuItem;
}
private javax.swing.JPopupMenu getPlotPopupMenu() {
	if (plotPopupMenu == null) {
		try {
			plotPopupMenu = new JPopupMenu();
//			visitMenuItem = new JMenuItem("Open in VisIt");
//			visitMenuItem.addActionListener(ivjEventHandler);
			plotPopupMenu.add(getTimePlotMenuItem());
			plotPopupMenu.add(getSpatialPlotMenuItem());
			plotPopupMenu.add(getKymographMenuItem());
//			plotPopupMenu.add(visitMenuItem);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	
	return plotPopupMenu;
}

/**
 * Gets the pdeDataContext property (cbit.vcell.simdata.PDEDataContext) value.
 * @return The pdeDataContext property value.
 * @see #setPdeDataContext
 */
public PDEDataContext getPdeDataContext() {
	return fieldPdeDataContext;
}

/**
 * Return the PDEDataContextPanel1 property value.
 * @return cbit.vcell.simdata.gui.PDEDataContextPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private PDEDataContextPanel getPDEDataContextPanel1() {
	if (ivjPDEDataContextPanel1 == null) {
		try {
			ivjPDEDataContextPanel1 = new PDEDataContextPanel();
			ivjPDEDataContextPanel1.setName("PDEDataContextPanel1");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPDEDataContextPanel1;
}


/**
 * Return the PDEExportPanel1 property value.
 * @return cbit.vcell.client.data.NewPDEExportPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private NewPDEExportPanel getPDEExportPanel1() {
	if (ivjPDEExportPanel1 == null) {
		try {
			ivjPDEExportPanel1 = new NewPDEExportPanel();
			ivjPDEExportPanel1.setName("PDEExportPanel1");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPDEExportPanel1;
}


/**
 * Return the PDEPlotControlPanel1 property value.
 * @return cbit.vcell.simdata.gui.PDEPlotControlPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private PDEPlotControlPanel getPDEPlotControlPanel1() {
	if (ivjPDEPlotControlPanel1 == null) {
		try {
			ivjPDEPlotControlPanel1 = new PDEPlotControlPanel();
			ivjPDEPlotControlPanel1.setName("PDEPlotControlPanel1");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPDEPlotControlPanel1;
}


/**
 * Gets the simulation property (cbit.vcell.solver.Simulation) value.
 * @return The simulation property value.
 * @see #setSimulation
 */
private Simulation getSimulation() {
	return fieldSimulation;
}


/**
 * Return the ViewData property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getViewData() {
	if (ivjViewData == null) {
		try {
			sliceViewPanel = new JPanel(new BorderLayout());
			sliceViewPanel.add(getPDEDataContextPanel1(), BorderLayout.CENTER);
			sliceViewPanel.add(getJPanelButtons(), BorderLayout.SOUTH);
			
			ivjViewData = new javax.swing.JPanel();
			ivjViewData.setName("ViewData");
			ivjViewData.setLayout(new java.awt.BorderLayout());
			viewDataTabbedPane = new JTabbedPane();
			viewDataTabbedPane.addTab(CurrentView.SLICE_VIEW.title, sliceViewPanel);
			
			viewDataTabbedPane.addChangeListener(ivjEventHandler);
			ivjViewData.add(viewDataTabbedPane, BorderLayout.CENTER);
			ivjViewData.add(getPDEPlotControlPanel1(), BorderLayout.WEST);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjViewData;
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
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
private void initConnections() throws java.lang.Exception {
	this.addPropertyChangeListener(ivjEventHandler);
	getPDEDataContextPanel1().addPropertyChangeListener(ivjEventHandler);
	getPDEPlotControlPanel1().addPropertyChangeListener(ivjEventHandler);
	getPDEExportPanel1().addPropertyChangeListener(ivjEventHandler);
	getPDEPlotControlPanel1().setDisplayAdapterService(getPDEDataContextPanel1().getdisplayAdapterService1());
	getPDEExportPanel1().setSlice(getPDEDataContextPanel1().getSlice());
	getPDEExportPanel1().setNormalAxis(getPDEDataContextPanel1().getNormalAxis());
	getPDEExportPanel1().setDisplayAdapterService(getPDEDataContextPanel1().getdisplayAdapterService1());
	getPDEExportPanel1().setDataViewerManager(this.getDataViewerManager());
}

/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		setName("PDEDataViewer");
		setLayout(new java.awt.BorderLayout());
		setSize(725, 569);
		add(getJTabbedPane1(), "Center");
		initConnections();
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
		PDEDataViewer aPDEDataViewer;
		aPDEDataViewer = new PDEDataViewer();
		frame.setContentPane(aPDEDataViewer);
		frame.setSize(aPDEDataViewer.getSize());
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
 * Sets the pdeDataContext property (cbit.vcell.simdata.PDEDataContext) value.
 * @param pdeDataContext The new value for the property.
 * @see #getPdeDataContext
 */
public void setPdeDataContext(PDEDataContext pdeDataContext) {	
	meshRegionSurfaces = null;

	PDEDataContext oldValue = fieldPdeDataContext;
	fieldPdeDataContext = pdeDataContext;
	firePropertyChange("pdeDataContext", oldValue, pdeDataContext);
}

public void setSimNameSimDataID(ExportSpecs.SimNameSimDataID simNameSimDataID){
	getPDEExportPanel1().setPdeDataContext(getPdeDataContext(),simNameSimDataID);
}
/**
 * Sets the simulation property (cbit.vcell.solver.Simulation) value.
 * @param simulation The new value for the property.
 * @see #getSimulation
 */
public void setSimulation(Simulation simulation) {
	Simulation oldValue = fieldSimulation;
	fieldSimulation = simulation;
	firePropertyChange("simulation", oldValue, simulation);
}

/**
 * Comment
 */
private void showKymograph() {
	//Collect all sample curves created by user
	SpatialSelection[] spatialSelectionArr = getPDEDataContextPanel1().fetchSpatialSelections(false,true);
	final Vector<SpatialSelection> lineSSOnly = new Vector<SpatialSelection>();
	if (spatialSelectionArr != null && spatialSelectionArr.length > 0) {
		//
		for (int i = 0; i < spatialSelectionArr.length; i++){
			if(spatialSelectionArr[i].isPoint() ||
				(	spatialSelectionArr[i] instanceof SpatialSelectionMembrane && 
					((SpatialSelectionMembrane)spatialSelectionArr[i]).getSelectionSource() instanceof cbit.vcell.geometry.SinglePoint)){
			}else{
				lineSSOnly.add(spatialSelectionArr[i]);
			}
		}
	}
	//
	if(lineSSOnly.size() == 0){
		PopupGenerator.showErrorDialog(this, "No line samples match DataType="+getPdeDataContext().getDataIdentifier().getVariableType());
		return;
	}	
	
	try {
		VariableType varType = getPdeDataContext().getDataIdentifier().getVariableType();
		int[] indices = null;
		int[] crossingMembraneIndices = null;
		double[] accumDistances = null;
		for (int i = 0; i < lineSSOnly.size(); i++){
			if (varType.equals(VariableType.VOLUME) || varType.equals(VariableType.VOLUME_REGION)){
				SpatialSelectionVolume ssv = (SpatialSelectionVolume)lineSSOnly.get(i);
				SpatialSelection.SSHelper ssh = ssv.getIndexSamples(0.0,1.0);
				indices = ssh.getSampledIndexes();
				crossingMembraneIndices = ssh.getMembraneIndexesInOut();
				accumDistances = ssh.getWorldCoordinateLengths();
			}else if(varType.equals(VariableType.MEMBRANE) || varType.equals(VariableType.MEMBRANE_REGION)){
				SpatialSelectionMembrane ssm = (SpatialSelectionMembrane)lineSSOnly.get(i);
				SpatialSelection.SSHelper ssh = ssm.getIndexSamples();
				indices = ssh.getSampledIndexes();
				accumDistances = ssh.getWorldCoordinateLengths();
			}	
			
			String title = "Kymograph: ";
			if (getSimulationModelInfo()!=null){
				title += getSimulationModelInfo().getContextName()+" "+getSimulationModelInfo().getSimulationName();
			}
			KymographPanel  kymographPanel = new KymographPanel(this, title);
			
			SymbolTable symbolTable;
			if(getSimulation() != null && getSimulation().getMathDescription() != null){
				symbolTable = getSimulation().getMathDescription();
			}else{
				symbolTable = new SimpleSymbolTable(new String[] {getPdeDataContext().getDataIdentifier().getName()});
			}
			
			kymographPanel.initDataManager(getDataViewerManager().getUser(), ((ClientPDEDataContext)getPdeDataContext()).getDataManager(),
				getPdeDataContext().getDataIdentifier(), getPdeDataContext().getTimePoints()[0], 1,
				getPdeDataContext().getTimePoints()[getPdeDataContext().getTimePoints().length-1],
				indices,crossingMembraneIndices,accumDistances,true,getPdeDataContext().getTimePoint(),
				symbolTable);
		}
	} catch (Exception e) {
		PopupGenerator.showErrorDialog(PDEDataViewer.this, this.getClass().getName()+".showKymograph: "+e.getMessage(), e);
		e.printStackTrace(System.out);
	}

}

/**
 * Comment
 */
private void showSpatialPlot() {
	// check selections
	final SpatialSelection[] sl = getPDEDataContextPanel1().fetchSpatialSelections(false,true);
	if (sl == null) {
		PopupGenerator.showErrorDialog(this, "Nothing selected!");
		return;
	}
	for (int i = 0; i < sl.length; i++){
		if (sl[i].isPoint()) {
			PopupGenerator.showErrorDialog(this, "One or more selections are single points - no spatial plot will be produced for those selections");
			break;
		}
	}
	final String varName = getPdeDataContext().getVariableName();
	final double timePoint = getPdeDataContext().getTimePoint();
	final SymbolTableEntry[] symbolTableEntries = new SymbolTableEntry[1];
	if(getSimulation() != null && getSimulation().getMathDescription() != null){
		symbolTableEntries[0] = getSimulation().getMathDescription().getEntry(varName);
	}
	if(symbolTableEntries[0] == null){
		Domain domain = null; //TODO domain
		symbolTableEntries[0] = new VolVariable(varName,domain);
	}
	
	AsynchClientTask task1 = new AsynchClientTask("Retrieving spatial series for variable '" + varName, AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			// get plots, ignoring points
			PlotData[] plotDatas = new PlotData[sl.length];
			for (int i = 0; i < sl.length; i++){				
				PlotData plotData = getPdeDataContext().getLineScan(varName, timePoint, sl[i]);
				plotDatas[i] = plotData;								
			}
			hashTable.put("plotDatas", plotDatas);
		}
	};
				
	AsynchClientTask task2 = new AsynchClientTask("Showing spatial plot for variable" + varName, AsynchClientTask.TASKTYPE_SWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			PlotData[] plotDatas = (PlotData[])hashTable.get("plotDatas");
			for (PlotData plotData : plotDatas){
				if (plotData != null) {			
					PlotPane plotPane = new PlotPane();
					Plot2D plot2D = new Plot2D(symbolTableEntries, new String[] { varName },new PlotData[] { plotData },
								new String[] {"Values along curve", "Distance (\u00b5m)", "[" + varName + "]"});
					plotPane.setPlot2D(	plot2D);
					String title = null;
					if (getSimulation()!=null){
						title = getSimulation().getName() + " : Spatial Plot : " + varName;
					}else{
						title = "Spatial Plot : " + varName;
					}
					
					ChildWindowManager childWindowManager = ChildWindowManager.findChildWindowManager(PDEDataViewer.this);
					ChildWindow childWindow = childWindowManager.addChildWindow(plotPane,plotPane,title);
					childWindow.setIsCenteredOnParent();
					childWindow.pack();
					childWindow.show();
					System.out.println("Spatial plot requesting focus.  Result is: "+childWindow.requestFocusInWindow());
				}
			}
		}
	};
	ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2}, false);	
}


/**
 * Comment
 */
private void showTimePlot() {
	VariableType varType = getPdeDataContext().getDataIdentifier().getVariableType();

	//Collect all sample curves created by user	
	SpatialSelection[] spatialSelectionArr = getPDEDataContextPanel1().fetchSpatialSelections(true,true);
	SpatialSelection[] spatialSelectionArr2 = null;
	if (varType.getVariableDomain().equals(VariableDomain.VARIABLEDOMAIN_VOLUME)) {
		spatialSelectionArr2 = getPDEDataContextPanel1().fetchSpatialSelections(
				varType.equals(VariableType.VOLUME) ? VariableType.VOLUME_REGION : VariableType.VOLUME, true, true);
	} else {
		spatialSelectionArr2 = getPDEDataContextPanel1().fetchSpatialSelections(
				varType.equals(VariableType.MEMBRANE) ? VariableType.MEMBRANE_REGION : VariableType.MEMBRANE, true, true);
	}
	
	final Vector<SpatialSelection> singlePointSSOnly = new Vector<SpatialSelection>();
	final Vector<SpatialSelection> singlePointSSOnly2 = new Vector<SpatialSelection>();
	if (spatialSelectionArr != null && spatialSelectionArr.length > 0) {
		for (int i = 0; i < spatialSelectionArr.length; i++){
			if(spatialSelectionArr[i].isPoint() ||
				(spatialSelectionArr[i] instanceof SpatialSelectionMembrane &&
					((SpatialSelectionMembrane)spatialSelectionArr[i]).getSelectionSource() instanceof SinglePoint)){
				singlePointSSOnly.add(spatialSelectionArr[i]);
			}
			if(spatialSelectionArr2[i].isPoint() ||
				(spatialSelectionArr2[i] instanceof SpatialSelectionMembrane &&
					((SpatialSelectionMembrane)spatialSelectionArr2[i]).getSelectionSource() instanceof SinglePoint)){
				singlePointSSOnly2.add(spatialSelectionArr2[i]);
			}
		}
	}
	final String varName = getPdeDataContext().getVariableName();	
	if(singlePointSSOnly.size() == 0){
		PopupGenerator.showErrorDialog(this, "No Time sampling points match DataType="+varType);
		return;
	}
	
	try {		
		int[] indices = null;
		//
		indices = new int[singlePointSSOnly.size()];
		for (int i = 0; i < singlePointSSOnly.size(); i++){
			if (varType.equals(VariableType.VOLUME) || varType.equals(VariableType.VOLUME_REGION)){
				SpatialSelectionVolume ssv = (SpatialSelectionVolume)singlePointSSOnly.get(i);
				indices[i] = ssv.getIndex(0);
			}else if(varType.equals(VariableType.MEMBRANE) || varType.equals(VariableType.MEMBRANE_REGION)){
				SpatialSelectionMembrane ssm = (SpatialSelectionMembrane)singlePointSSOnly.get(i);
				indices[i] = ssm.getIndex(0);
			}
		}

		double[] timePoints = getPdeDataContext().getTimePoints();
		final TimeSeriesJobSpec tsjs = new TimeSeriesJobSpec(	new String[] {varName},
				new int[][] {indices}, null, timePoints[0], 1, timePoints[timePoints.length-1],
				VCDataJobID.createVCDataJobID(getDataViewerManager().getUser(), true));

		if (!tsjs.getVcDataJobID().isBackgroundTask()){
			throw new RuntimeException("Use getTimeSeries(...) if not a background job");
		}
		
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put(StringKey_timeSeriesJobSpec, tsjs);
		
		AsynchClientTask task1 = new TimeSeriesDataRetrievalTask("Retrieving Data for '"+varName+"'...");	
		AsynchClientTask task2 = new AsynchClientTask("showing time plot for '"+varName+"'", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {

			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
			
				TSJobResultsNoStats tsJobResultsNoStats = (TSJobResultsNoStats)hashTable.get(StringKey_timeSeriesJobResults);
				PdeTimePlotMultipleVariablesPanel pdeTimePlotPanel = new PdeTimePlotMultipleVariablesPanel(PDEDataViewer.this, 
						getPDEPlotControlPanel1().getVariableListCellRenderer(),
						getSimulation(), singlePointSSOnly, singlePointSSOnly2, tsJobResultsNoStats);
				
				ChildWindowManager childWindowManager = ChildWindowManager.findChildWindowManager(PDEDataViewer.this);
				ChildWindow childWindow = childWindowManager.addChildWindow(pdeTimePlotPanel, pdeTimePlotPanel, "Time Plot");
				childWindow.setSize(900, 550);
				childWindow.setIsCenteredOnParent();
				childWindow.show();
				
			}						
		};	

		ClientTaskDispatcher.dispatch(this, hash, new AsynchClientTask[] { task1, task2 }, true, true, null);
	
	} catch (Exception e) {
		e.printStackTrace(System.out);
	}
}

/**
 * Comment
 */
private void updateDataSamplerContext(java.beans.PropertyChangeEvent propertyChangeEvent) {
	//
	if(propertyChangeEvent.getPropertyName().equals("timeDataSamplers")){
		boolean shouldEnablePlot = ((Boolean)(propertyChangeEvent.getNewValue())).booleanValue();
		getTimePlotMenuItem().setEnabled(shouldEnablePlot);
	}else if(propertyChangeEvent.getPropertyName().equals("spatialDataSamplers")){
		boolean shouldEnablePlot = ((Boolean)(propertyChangeEvent.getNewValue())).booleanValue();
		getSpatialPlotMenuItem().setEnabled(shouldEnablePlot);
		getKymographMenuItem().setEnabled((getPdeDataContext().getTimePoints().length > 1) && shouldEnablePlot);
	}
	getPlotButton().setEnabled(getSpatialPlotMenuItem().isEnabled() || getTimePlotMenuItem().isEnabled() || getKymographMenuItem().isEnabled());
	
}


/**
 * Insert the method's description here.
 * Creation date: (9/25/2005 2:00:05 PM)
 */
private void updateDataValueSurfaceViewer() {

	if(getDataValueSurfaceViewer() == null){
		return;
	}
	System.out.println("***************PDEDataViewer.updateDataValueSurfaceViewer()");
	//SurfaceColors and DataValues
	SurfaceCollection surfaceCollection = getDataValueSurfaceViewer().getSurfaceCollectionDataInfo().getSurfaceCollection();
	DisplayAdapterService das = getPDEDataContextPanel1().getdisplayAdapterService1();
	final int[][] surfaceColors = new int[surfaceCollection.getSurfaceCount()][];
	final double[][] surfaceDataValues = new double[surfaceCollection.getSurfaceCount()][];
	boolean bMembraneVariable = getPdeDataContext().getDataIdentifier().getVariableType().equals(VariableType.MEMBRANE);
	for(int i=0;i<surfaceCollection.getSurfaceCount();i+= 1){
		Surface surface = surfaceCollection.getSurfaces(i);
		surfaceColors[i] = new int[surface.getPolygonCount()];
		surfaceDataValues[i] = new double[surface.getPolygonCount()];
		for(int j=0;j<surface.getPolygonCount();j+= 1){
			int membraneIndexForPolygon = meshRegionSurfaces.getMembraneIndexForPolygon(i,j);
			if (bMembraneVariable) {
				surfaceDataValues[i][j] = getPdeDataContext().getDataValues()[membraneIndexForPolygon];
			} else {
				// get membrane region index from membrane index
				surfaceDataValues[i][j] = getPdeDataContext().getDataValues()[getPdeDataContext().getCartesianMesh().getMembraneRegionIndex(membraneIndexForPolygon)];
			}
			surfaceColors[i][j] = das.getColorFromValue(surfaceDataValues[i][j]);
		}
	}
	

	DataValueSurfaceViewer.SurfaceCollectionDataInfoProvider svdp = new DataValueSurfaceViewer.SurfaceCollectionDataInfoProvider(){
		private DisplayAdapterService updatedDAS = new DisplayAdapterService(getPDEDataContextPanel1().getdisplayAdapterService1());
		private String updatedVariableName = getPdeDataContext().getVariableName();
		private double updatedTimePoint = getPdeDataContext().getTimePoint();
		private double[] updatedVariableValues = getPdeDataContext().getDataValues();
		private VCDataIdentifier updatedVCDataIdentifier = getPdeDataContext().getVCDataIdentifier();
		public void makeMovie(SurfaceCanvas surfaceCanvas){
			makeSurfaceMovie(surfaceCanvas, updatedVariableValues.length,updatedVariableName,updatedDAS,updatedVCDataIdentifier);
		}
		public double getValue(int surfaceIndex,int polygonIndex){
			return updatedVariableValues[meshRegionSurfaces.getMembraneIndexForPolygon(surfaceIndex,polygonIndex)];
		}
		public String getValueDescription(int surfaceIndex,int polygonIndex){
			return updatedVariableName;
		}
		public int[][] getSurfacePolygonColors(){
			return surfaceColors;
		}
		public Coordinate getCentroid(int surfaceIndex,int polygonIndex){
			return getPdeDataContext().getCartesianMesh().getMembraneElements()[meshRegionSurfaces.getMembraneIndexForPolygon(surfaceIndex,polygonIndex)].getCentroid();
		}
		public float getArea(int surfaceIndex,int polygonIndex){
			return getPdeDataContext().getCartesianMesh().getMembraneElements()[meshRegionSurfaces.getMembraneIndexForPolygon(surfaceIndex,polygonIndex)].getArea();
		}
		public Vect3d getNormal(int surfaceIndex,int polygonIndex){
			return getPdeDataContext().getCartesianMesh().getMembraneElements()[meshRegionSurfaces.getMembraneIndexForPolygon(surfaceIndex,polygonIndex)].getNormal();
		}
		public int getMembraneIndex(int surfaceIndex,int polygonIndex){
			return meshRegionSurfaces.getMembraneIndexForPolygon(surfaceIndex,polygonIndex);
		}
		public Color getROIHighlightColor(){
			return new Color(getPDEDataContextPanel1().getdisplayAdapterService1().getSpecialColors()[cbit.image.gui.DisplayAdapterService.FOREGROUND_HIGHLIGHT_COLOR_OFFSET]);
		}
//		public void showComponentInFrame(Component comp,String title){
//			PDEDataViewer.this.showComponentInFrame(comp,title);
//		}
		public void plotTimeSeriesData(
				int[][] indices,
				boolean bAllTimes,
				boolean bTimeStats,
				boolean bSpaceStats) throws DataAccessException{

				double[] timePoints = getPdeDataContext().getTimePoints();
				double beginTime = (bAllTimes?timePoints[0]:updatedTimePoint);
				double endTime = (bAllTimes?timePoints[timePoints.length-1]:beginTime);
				String[] varNames = new String[indices.length];
				for(int i=0;i<varNames.length;i+= 1){
					varNames[i] = updatedVariableName;
				}
				TimeSeriesJobSpec timeSeriesJobSpec =	new TimeSeriesJobSpec(varNames,indices,beginTime,1,endTime,bSpaceStats,bTimeStats,
							VCDataJobID.createVCDataJobID(getDataViewerManager().getUser(),	true));

				Hashtable<String, Object> hash = new Hashtable<String, Object>();
				hash.put(StringKey_timeSeriesJobSpec, timeSeriesJobSpec);

				AsynchClientTask task1 = new TimeSeriesDataRetrievalTask("Retrieve data");
				AsynchClientTask task2 = new AsynchClientTask("Showing surface", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {

					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {
						TSJobResultsSpaceStats tsJobResultsSpaceStats = (TSJobResultsSpaceStats)hashTable.get(StringKey_timeSeriesJobResults);
						plotSpaceStats(tsJobResultsSpaceStats);
					}
				};		
				ClientTaskDispatcher.dispatch(PDEDataViewer.this, hash, new AsynchClientTask[] { task1, task2 }, true, true, null);				
		}
	};

	getDataValueSurfaceViewer().setSurfaceCollectionDataInfoProvider(svdp);
}

private void makeSurfaceMovie(final SurfaceCanvas surfaceCanvas,
		final int varTotalNumIndices, final String movieDataVarName, final DisplayAdapterService movieDAS,
		final VCDataIdentifier movieVCDataIdentifier){

	final SurfaceMovieSettingsPanel smsp = new SurfaceMovieSettingsPanel();
	final double[] timePoints = getPdeDataContext().getTimePoints();
	final int surfaceWidth = surfaceCanvas.getWidth();
	final int surfaceHeight = surfaceCanvas.getHeight();
	smsp.init(surfaceWidth, surfaceHeight, timePoints);
	
	while (true){
		if(PopupGenerator.showComponentOKCancelDialog(this, smsp, "Movie Settings for var "+movieDataVarName) != JOptionPane.OK_OPTION){
			return;
		}
		long movieSize =(smsp.getTotalFrames()*surfaceWidth*surfaceHeight*3);
		long rawDataSize = (smsp.getTotalFrames()*varTotalNumIndices*8);//raw data size;
		if(movieSize+rawDataSize > 50000000){
			final String YES_RESULT = "Yes";
			String result = PopupGenerator.showWarningDialog(this,
				"Movie processing will require at least "+(movieSize+rawDataSize)/1000000+" mega-bytes of memory.\nMovie size will be "+(movieSize >= 1000000?movieSize/1000000+" mega-bytes.":movieSize/1000.0+" kilo-bytes.")+" Continue?", new String[] {YES_RESULT,"No"}, YES_RESULT);
			if(result != null && result.equals(YES_RESULT)){
				break;
			}
		}else{
			break;
		}
	}
	
	final int beginTimeIndex = smsp.getBeginTimeIndex();
	final int endTimeIndex = smsp.getEndTimeIndex();
	final int step = smsp.getSkipParameter()+1;
	final String[] varNames = new String[] {movieDataVarName};
	int[] allIndices = new int[varTotalNumIndices];
	for (int i = 0; i < allIndices.length; i++) {
		allIndices[i] = i;
	}
	final TimeSeriesJobSpec timeSeriesJobSpec =	new TimeSeriesJobSpec(varNames,	new int[][] {allIndices},null,
			timePoints[beginTimeIndex], step, timePoints[endTimeIndex],
			VCDataJobID.createVCDataJobID(getDataViewerManager().getUser(),	true));
	
	Hashtable<String, Object> hash = new Hashtable<String, Object>();
	hash.put(StringKey_timeSeriesJobSpec, timeSeriesJobSpec);

	AsynchClientTask task1 = new TimeSeriesDataRetrievalTask("Retrieving data for variable '" + movieDataVarName + "'");
	AsynchClientTask task2 = new AsynchClientTask("select a file", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			VCFileChooser fileChooser = new VCFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.addChoosableFileFilter(FileFilters.FILE_FILTER_MOV);
			// Set the default file filter...
			fileChooser.setFileFilter(FileFilters.FILE_FILTER_MOV);
			// remove all selector
			fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());		    
			
			fileChooser.setDialogTitle("Saving surface movie");
			
			File selectedFile = null;
			while(true){
				if(fileChooser.showSaveDialog(PDEDataViewer.this) != JFileChooser.APPROVE_OPTION){
					return;
				}
				selectedFile = fileChooser.getSelectedFile();
				if (!selectedFile.getName().endsWith(".mov")) {
					selectedFile = new File(selectedFile.getAbsolutePath() + ".mov");
				}
				if (selectedFile.exists()){
					final String YES_RESULT = "Yes";
					String result = PopupGenerator.showWarningDialog(PDEDataViewer.this, "Overwrite exisitng file:\n"+selectedFile.getAbsolutePath()+"?", new String[] {YES_RESULT,"No"}, YES_RESULT);
					if (result != null && result.equals(YES_RESULT)){						
						break;
					}
				} else {
					break;
				}
			}
			hashTable.put("selectedFile", selectedFile);
		}							
	};
		
	AsynchClientTask task3 = new AsynchClientTask("create movie", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			File selectedFile = (File)hashTable.get("selectedFile");
			if (selectedFile == null) {
				return;
			}
			
			TSJobResultsNoStats tsJobResultsNoStats = (TSJobResultsNoStats)hashTable.get(StringKey_timeSeriesJobResults);
			
			double[][] timeSeries = tsJobResultsNoStats.getTimesAndValuesForVariable(movieDataVarName);			
			int[] singleFrame = new int[surfaceWidth*surfaceHeight];
			BufferedImage bufferedImage = new BufferedImage(surfaceWidth, surfaceHeight,BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g2D = bufferedImage.createGraphics();
			VideoMediaChunk[] chunks = new VideoMediaChunk[tsJobResultsNoStats.getTimes().length];	
			VideoMediaSample sample;
			int sampleDuration = 0;
			int timeScale = smsp.getFramesPerSecond();
			int bitsPerPixel = 32;
			DisplayAdapterService das = new DisplayAdapterService(movieDAS);
			int[][] origSurfacesColors = surfaceCanvas.getSurfacesColors();
			DataInfoProvider dataInfoProvider = getPDEDataContextPanel1().getDataInfoProvider();
			try {
				for (int t = 0; t < tsJobResultsNoStats.getTimes().length; t++) {
					getClientTaskStatusSupport().setMessage("Creating Movie... Progress "+NumberUtils.formatNumber(100.0*((double)t/(double)tsJobResultsNoStats.getTimes().length),3)+"%");
					
					double min = Double.POSITIVE_INFINITY;
					double max = Double.NEGATIVE_INFINITY;
					for (int index = 1; index < timeSeries.length; index++) {
						double v = timeSeries[index][t];
						if((dataInfoProvider == null || dataInfoProvider.isDefined(index-1)) && !Double.isNaN(v) && !Double.isInfinite(v)){
							min = Math.min(min, v);
							max = Math.max(max, v);
						}
					}
					das.setValueDomain(new Range(min,max));
					if(das.getAutoScale()){
						das.setActiveScaleRange(new Range(min,max));
					}
					int[][] surfacesColors = new int[surfaceCanvas.getSurfaceCollection().getSurfaceCount()][];
					for (int i = 0; i < surfaceCanvas.getSurfaceCollection().getSurfaceCount(); i += 1) {
						Surface surface = surfaceCanvas.getSurfaceCollection().getSurfaces(i);
						surfacesColors[i] = new int[surface.getPolygonCount()];
						for (int j = 0; j < surface.getPolygonCount(); j += 1) {
							int membIndex = meshRegionSurfaces.getMembraneIndexForPolygon(i, j);
							surfacesColors[i][j] = das.getColorFromValue(timeSeries[membIndex+1][t]);
						}
					}
					surfaceCanvas.setSurfacesColors(surfacesColors);
					
					surfaceCanvas.paintImmediately(0, 0, surfaceWidth, surfaceHeight);
					surfaceCanvas.paint(g2D);
					bufferedImage.getRGB(0, 0, surfaceWidth, surfaceHeight, singleFrame, 0, surfaceWidth);
					sampleDuration = 1;
					sample =
						FormatSpecificSpecs.getVideoMediaSample(
							surfaceWidth, surfaceHeight * varNames.length, sampleDuration, false, FormatSpecificSpecs.CODEC_JPEG, 1.0f, singleFrame);
					chunks[t] = new VideoMediaChunk(sample);
				}
			}finally{
				surfaceCanvas.setSurfacesColors(origSurfacesColors);
				surfaceCanvas.paintImmediately(0, 0, surfaceWidth, surfaceHeight);
			}
			MediaTrack videoTrack = new MediaTrack(chunks);
			MediaMovie newMovie = new MediaMovie(videoTrack, videoTrack.getDuration(), timeScale);
			newMovie.addUserDataEntry(new UserDataEntry("cpy", "\u00A9" + (new GregorianCalendar()).get(Calendar.YEAR) + ", UCHC"));
			newMovie.addUserDataEntry(new UserDataEntry("des", "Dataset name: " + movieVCDataIdentifier.getID()));
			newMovie.addUserDataEntry(new UserDataEntry("cmt", "Time range: " + timePoints[beginTimeIndex] + " - " + timePoints[endTimeIndex]));
			for (int k = 0; k < varNames.length; k ++) {
				String entryType = "v" + (k < 10 ? "0" : "") + k; // pad with 0 if k < 10
				UserDataEntry entry = new UserDataEntry(entryType,	"Variable name: " + varNames[k] + " min: " + das.getValueDomain().getMin() 
						+ " max: " + das.getValueDomain().getMax());
				newMovie.addUserDataEntry(entry);
			}
			getClientTaskStatusSupport().setMessage("Writing Movie to disk...");
			FileOutputStream fos = new FileOutputStream(selectedFile);
			DataOutputStream movieOutput = new DataOutputStream(new BufferedOutputStream(fos));
			MediaMethods.writeMovie(movieOutput, newMovie);
			movieOutput.close();
			fos.close();
		}
	};
	ClientTaskDispatcher.dispatch(this, hash, new AsynchClientTask[] { task1, task2, task3 }, true, true, null);
}

public void setVisItButtonVisible(boolean bVisible)
{
	getJButtonVisit().setVisible(bVisible);
}

public void addDataJobListener(DataJobListener listener) {
	dataJobListenerList.add(listener);
	
}

public void removeDataJobListener(DataJobListener listener) {
	dataJobListenerList.remove(listener);
	
}

@Override
public void showTimePlotMultipleScans(DataManager dataManager) {
	// TODO Auto-generated method stub
	
}

}
