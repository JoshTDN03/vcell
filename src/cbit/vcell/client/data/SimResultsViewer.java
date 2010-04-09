package cbit.vcell.client.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.vcell.util.BeanUtils;
import org.vcell.util.DataAccessException;
import org.vcell.util.document.VCDataIdentifier;

import cbit.rmi.event.DataJobEvent;
import cbit.vcell.client.server.DataManager;
import cbit.vcell.client.server.ODEDataManager;
import cbit.vcell.client.server.PDEDataManager;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.export.ExportMonitorPanel;
import cbit.vcell.math.Constant;
import cbit.vcell.simdata.ClientPDEDataContext;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.VCSimulationDataIdentifier;
/**
 * Insert the type's description here.
 * Creation date: (10/17/2005 11:22:58 PM)
 * @author: Ion Moraru
 */
public class SimResultsViewer extends DataViewer {
	private Simulation simulation = null;
	private DataViewer mainViewer = null;
	private JPanel paramChoicesPanel = null;
	private ODEDataViewer odeDataViewer = null;
	private PDEDataViewer pdeDataViewer = null;
	private boolean isODEData;
	private Hashtable<String, JTable> choicesHash = new Hashtable<String, JTable>();
	private DataManager dataManager = null;

/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:30:45 PM)
 * @param simulation cbit.vcell.solver.Simulation
 * @param vcDataManager cbit.vcell.client.server.VCDataManager
 */
public SimResultsViewer(Simulation simulation, DataManager arg_dataManager) throws DataAccessException {
	super();
	setSimulation(simulation);
	this.isODEData = !simulation.isSpatial();
	this.dataManager = arg_dataManager;
	initialize();
}


/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 2:33:44 PM)
 * @return javax.swing.JPanel
 * @throws DataAccessException 
 */
private DataViewer createODEDataViewer() throws DataAccessException {
	odeDataViewer = new ODEDataViewer();
	odeDataViewer.setSimulation(getSimulation());
	odeDataViewer.setOdeSolverResultSet(((ODEDataManager)dataManager).getODESolverResultSet());
	odeDataViewer.setVcDataIdentifier(dataManager.getVCDataIdentifier());
	return odeDataViewer;
}


/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 2:33:44 PM)
 * @return javax.swing.JPanel
 */
private DataViewer createPDEDataViewer() throws DataAccessException {
	pdeDataViewer = new PDEDataViewer();
	pdeDataViewer.setSimulation(getSimulation());
	pdeDataViewer.setPdeDataContext(((PDEDataManager)dataManager).getPDEDataContext());
	return pdeDataViewer;
}

public void dataJobMessage(DataJobEvent dje) {
	getMainViewer().dataJobMessage(dje);
}

/**
 * Method generated to support the promotion of the exportMonitorPanel attribute.
 * @return cbit.vcell.export.ExportMonitorPanel
 */
public ExportMonitorPanel getExportMonitorPanel() {
	return getMainViewer().getExportMonitorPanel();
}


/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:36:17 PM)
 * @return cbit.vcell.client.data.DataViewer
 */
private DataViewer getMainViewer() {
	return mainViewer;
}


/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:36:17 PM)
 * @return javax.swing.JPanel
 */
private javax.swing.JPanel getParamChoicesPanel() {
	return paramChoicesPanel;
}


/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:36:17 PM)
 * @return cbit.vcell.solver.Simulation
 */
private Simulation getSimulation() {
	return simulation;
}

/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:37:52 PM)
 * @exception org.vcell.util.DataAccessException The exception description.
 */
private void initialize() throws DataAccessException {
	
	// create main viewer for jobIndex 0 and wire it up
	if (isODEData) {
		setMainViewer(createODEDataViewer());
	} else {
		setMainViewer(createPDEDataViewer());
	}
	java.beans.PropertyChangeListener pcl = new java.beans.PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == SimResultsViewer.this && (evt.getPropertyName().equals("dataViewerManager"))) {
				try {
					getMainViewer().setDataViewerManager(getDataViewerManager());
				} catch (java.beans.PropertyVetoException exc) {
					exc.printStackTrace();
				}
			}
			if (evt.getSource() == SimResultsViewer.this && (evt.getPropertyName().equals("simulationModelInfo"))) {
				getMainViewer().setSimulationModelInfo(getSimulationModelInfo());
			}
		}
	};
	addPropertyChangeListener(pcl);
		
	
	// if necessarry, create parameter choices panel and wire it up
	if (getSimulation().getScanCount() > 1) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEtchedBorder());
		
		JLabel label = new JLabel("<html><u><b>Choose Parameter Values</b></u></html>");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		panel.add(label, BorderLayout.NORTH);
		
		String[] scanParams = getSimulation().getMathOverrides().getScannedConstantNames();
		Arrays.sort(scanParams);
		javax.swing.event.ListSelectionListener lsl = new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateScanParamChoices();
				}
			}
		};
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.X_AXIS));
		for (int i = 0; i < scanParams.length; i++){
			Constant[] scanConstants = getSimulation().getMathOverrides().getConstantArraySpec(scanParams[i]).getConstants();
			String[][] values = new String[scanConstants.length][1]; 
			for (int j = 0; j < scanConstants.length; j++){
				values[j][0] = scanConstants[j].getExpression().infix();
			}
			class ScanChoicesTableModel extends javax.swing.table.AbstractTableModel {
				String[] columnNames;
				Object[][] rowData;
				ScanChoicesTableModel(Object[][] argData, String[] argNames) {
					columnNames = argNames;
					rowData = argData;
				}
				public String getColumnName(int column) { return columnNames[column].toString(); }
				public int getRowCount() { return rowData.length; }
				public int getColumnCount() { return columnNames.length; }
				public Object getValueAt(int row, int col) { return rowData[row][col]; }
				public boolean isCellEditable(int row, int column) { return false; }
				public void setValueAt(Object value, int row, int col) {
					rowData[row][col] = value;
					fireTableCellUpdated(row, col);
				}
			};
			ScanChoicesTableModel tm = new ScanChoicesTableModel(values, new String[] {scanParams[i]});
			JTable table = new JTable(tm);
			choicesHash.put(scanParams[i], table);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().setSelectionInterval(0,0);
			table.getSelectionModel().addListSelectionListener(lsl);
			JScrollPane scr = new JScrollPane(table);
			JPanel p = new JPanel();
			scr.setPreferredSize(new java.awt.Dimension (100, Math.min(150, table.getPreferredSize().height + table.getTableHeader().getPreferredSize().height + 5)));
			p.setLayout(new java.awt.BorderLayout());
			p.add(scr, java.awt.BorderLayout.CENTER);
			p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			tablePanel.add(p);
		}
		panel.add(tablePanel, BorderLayout.CENTER);
				
		if (isODEData) {
			JPanel buttonPanel = new JPanel(new FlowLayout());
			JButton button = new JButton("Time Plot with Multiple Parameter Value Sets");
			buttonPanel.add(button);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			
			button.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					mainViewer.showTimePlotMultipleScans(dataManager);
				}
			});
		}
		
		setParamChoicesPanel(panel);
	}

	// put things together
	setLayout(new java.awt.BorderLayout());
	add(getMainViewer(), java.awt.BorderLayout.CENTER);
	if (getSimulation().getScanCount() > 1) {
		add(getParamChoicesPanel(), java.awt.BorderLayout.SOUTH);
	}
}

/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 2:43:49 PM)
 * @exception org.vcell.util.DataAccessException The exception description.
 */
public void refreshData() throws DataAccessException {
	if (isODEData) {
		updateScanParamChoices(); // this takes care of all logic to get the fresh data
	} else {
		pdeDataViewer.getPdeDataContext().refreshTimes();
	}
}

public void refreshFunctions() throws DataAccessException {
	if (isODEData) {
		updateScanParamChoices();
	} else {
		// no other reliable way until the PDE context/viewer/manager/dataset furball will be cleaned up... 
		updateScanParamChoices();
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:36:17 PM)
 * @param newMainViewer cbit.vcell.client.data.DataViewer
 */
private void setMainViewer(DataViewer newMainViewer) {
	mainViewer = newMainViewer;
}


/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:36:17 PM)
 * @param newParamChoicesPanel javax.swing.JPanel
 */
private void setParamChoicesPanel(javax.swing.JPanel newParamChoicesPanel) {
	paramChoicesPanel = newParamChoicesPanel;
}


/**
 * Insert the method's description here.
 * Creation date: (10/17/2005 11:36:17 PM)
 * @param newSimulation cbit.vcell.solver.Simulation
 */
private void setSimulation(Simulation newSimulation) {
	simulation = newSimulation;
}


/**
 * Insert the method's description here.
 * Creation date: (10/18/2005 12:44:06 AM)
 */
private void updateScanParamChoices(){
	
	// figure out what job data we are looking for
	String[] scanConstantNames = getSimulation().getMathOverrides().getScannedConstantNames();
	java.util.Arrays.sort(scanConstantNames);
	int[] indices = new int[scanConstantNames.length];
	int[] bounds = new int[scanConstantNames.length];
	for (int i = 0; i < indices.length; i++){
		indices[i] = choicesHash.get(scanConstantNames[i]).getSelectedRow();
		bounds[i] = getSimulation().getMathOverrides().getConstantArraySpec(scanConstantNames[i]).getNumValues() - 1;
	}
	int jobIndex = -1;
	try {
		jobIndex = BeanUtils.coordinateToIndex(indices, bounds);
	} catch (RuntimeException exc) {}
	
	// update viewer
	if (jobIndex == -1) {
		if (isODEData) {
			odeDataViewer.setOdeSolverResultSet(null);
		} else {
			pdeDataViewer.setPdeDataContext(null);
		}
		return;
	}
	
	final VCDataIdentifier vcdid = new VCSimulationDataIdentifier(getSimulation().getSimulationInfo().getAuthoritativeVCSimulationIdentifier(), jobIndex);
	if (isODEData) {
		AsynchClientTask task1 = new AsynchClientTask("get ode results", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				ODEDataManager odeDatamanager = ((ODEDataManager)dataManager).createNewODEDataManager(vcdid);
				hashTable.put("odeDatamanager", odeDatamanager);
			}
		};
		AsynchClientTask task2 = new AsynchClientTask("show results", AsynchClientTask.TASKTYPE_SWING_BLOCKING, false, false) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				Exception ex = (Exception)hashTable.get(ClientTaskDispatcher.TASK_ABORTED_BY_ERROR);
				if (ex == null) {
					ODEDataManager odeDatamanager = (ODEDataManager)hashTable.get("odeDatamanager");
					odeDataViewer.setOdeSolverResultSet(odeDatamanager.getODESolverResultSet());
					odeDataViewer.setVcDataIdentifier(vcdid);
				} else {
					odeDataViewer.setOdeSolverResultSet(null);
				}
			}
		};
		ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2});
	} else {
		AsynchClientTask task1 = new AsynchClientTask("get pde results", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				ClientPDEDataContext currentContext = (ClientPDEDataContext)pdeDataViewer.getPdeDataContext();
				if (currentContext == null || currentContext.getDataIdentifier() == null) {
					PDEDataManager pdeDatamanager = ((PDEDataManager)dataManager).createNewPDEDataManager(vcdid, null);
					pdeDataViewer.setPdeDataContext(pdeDatamanager.getPDEDataContext());
				} else {
					PDEDataManager pdeDatamanager = ((PDEDataManager)dataManager).createNewPDEDataManager(vcdid, (NewClientPDEDataContext)currentContext);					
					currentContext.setDataManager(pdeDatamanager);
				}
			}
		};
		ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1});
	}
}

}