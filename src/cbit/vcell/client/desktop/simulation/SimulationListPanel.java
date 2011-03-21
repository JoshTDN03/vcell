package cbit.vcell.client.desktop.simulation;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;

import org.vcell.solver.smoldyn.SmoldynFileWriter;
import org.vcell.util.BeanUtils;
import org.vcell.util.Executable;
import org.vcell.util.ExecutableException;
import org.vcell.util.TokenMangler;
import org.vcell.util.gui.DefaultScrollTableCellRenderer;
import org.vcell.util.gui.DownArrowIcon;
import org.vcell.util.gui.MultiLineToolTip;
import org.vcell.util.gui.ScrollTable;
import org.vcell.util.gui.VCellIcons;

import cbit.vcell.client.ClientRequestManager;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.client.desktop.biomodel.DocumentEditorSubPanel;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.document.SimulationOwner;
import cbit.vcell.graph.ReactionCartoonEditorPanel;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.resource.ResourceUtil;
import cbit.vcell.simdata.SimDataConstants;
import cbit.vcell.solver.OutputTimeSpec;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationJob;
import cbit.vcell.solver.ode.gui.SimulationStatus;
/**
 * Insert the type's description here.
 * Creation date: (5/7/2004 3:41:07 PM)
 * @author: Ion Moraru
 */
@SuppressWarnings("serial")
public class SimulationListPanel extends DocumentEditorSubPanel {
	private OutputFunctionsPanel outputFunctionsPanel;
	private JToolBar toolBar = null;
	private JButton ivjEditButton = null;
	private JButton copyButton = null;
	private JButton ivjNewButton = null;
	private JButton ivjResultsButton = null;
	private JButton ivjRunButton = null;
	private JButton ivjDeleteButton = null;
	private JButton particleViewButton = null;
	private ScrollTable ivjScrollPaneTable = null;
	private IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private SimulationListTableModel ivjSimulationListTableModel1 = null;
	private SimulationWorkspace fieldSimulationWorkspace = null;
	private JButton moreActionsButton = null;
	private JButton stopButton;
	private JButton statusDetailsButton;
	
	private class IvjEventHandler implements java.awt.event.ActionListener, 
		java.beans.PropertyChangeListener, javax.swing.event.ListSelectionListener, MouseListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == getNewButton()) {
				newSimulation();
			} else if (e.getSource() == SimulationListPanel.this.getEditButton()) {
				editSimulation();
			} else if (e.getSource() == copyButton) {
				copySimulations();
			} else if (e.getSource() == getDeleteButton()) { 
				deleteSimulations();
			} else if (e.getSource() == getRunButton()) {
				runSimulations();
			} else if (e.getSource() == stopButton) {
				stopSimulations();
			} else if (e.getSource() == getResultsButton()) {
				showSimulationResults();
			} else if (e.getSource() == statusDetailsButton) {
				showSimulationStatusDetails();
			} else if (e.getSource() == particleViewButton) {
				particleView();
			}
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == fieldSimulationWorkspace && evt.getPropertyName().equals("status")) {
				refreshButtonsLax();
			}
		};
		public void valueChanged(javax.swing.event.ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			if (e.getSource() == getScrollPaneTable().getSelectionModel()) 
				tableSelectionChanged(e);
			
		}
		public void mouseClicked(MouseEvent e) {
		}
		public void mousePressed(MouseEvent e) {
		}
		public void mouseReleased(MouseEvent e) {
		}
		public void mouseEntered(MouseEvent e) {
		}
		public void mouseExited(MouseEvent e) {
		}
	};

public SimulationListPanel() {
	super();
	initialize();
}

/**
 * connEtoC9:  (selectionModel1.listSelection.valueChanged(javax.swing.event.ListSelectionEvent) --> SimulationListPanel.refreshButtons()V)
 * @param arg1 javax.swing.event.ListSelectionEvent
 */
private void tableSelectionChanged(javax.swing.event.ListSelectionEvent arg1) {
	try {		
		refreshButtonsLax();
		setSelectedObjectsFromTable(getScrollPaneTable(), getSimulationListTableModel1());
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
}

/**
 * Comment
 */
private void copySimulations() {
	int[] selections = getScrollPaneTable().getSelectedRows();
	Vector<Simulation> v = new Vector<Simulation>();
	for (int i = 0; i < selections.length; i++){
		v.add(getSimulationWorkspace().getSimulations()[selections[i]]);
	}
	Simulation[] toCopy = (Simulation[])BeanUtils.getArray(v, Simulation.class);
	int index = -1;
	try {
		index = getSimulationWorkspace().copySimulations(toCopy, this);
	} catch (Throwable exc) {
		exc.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(this, "Could not copy all simulations\n"+exc.getMessage(), exc);
	}
	// set selection to the last copied one
	getScrollPaneTable().getSelectionModel().setSelectionInterval(index, index);
	getScrollPaneTable().scrollRectToVisible(getScrollPaneTable().getCellRect(index, 0, true));
}


/**
 * Comment
 */
private void deleteSimulations() {
	int[] selections = getScrollPaneTable().getSelectedRows();
	Vector<Simulation> v = new Vector<Simulation>();
	for (int i = 0; i < selections.length; i++){
		SimulationStatus simStatus = getSimulationWorkspace().getSimulationStatus(getSimulationWorkspace().getSimulations()[selections[i]]);
		if (!simStatus.isRunning()){
			v.add(getSimulationWorkspace().getSimulations()[selections[i]]);
		}
	}
	Simulation[] toDelete = (Simulation[])BeanUtils.getArray(v, Simulation.class);
	try {
		getSimulationWorkspace().deleteSimulations(toDelete);
	} catch (Throwable exc) {
		exc.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(this, "Could not delete all simulations\n"+exc.getMessage(), exc);
	}
	// unset selection - may not be needed...
	getScrollPaneTable().clearSelection();
}


/**
 * Comment
 */
private void editSimulation() {
	// this should not be possible to call unless exactly one row is selected, but check anyway
	int[] selectedRows = getScrollPaneTable().getSelectedRows();
	if (selectedRows.length > 0) { // make sure something is selected...
		SimulationStatus simStatus = getSimulationWorkspace().getSimulationStatus(getSimulationWorkspace().getSimulations()[selectedRows[0]]);
		if (!simStatus.isRunning()){
			SimulationWorkspace.editSimulation(this, getSimulationWorkspace().getSimulationOwner(), getSimulationWorkspace().getSimulations()[selectedRows[0]]); // just the first one if more than one selected...
		}
	}
}

/**
 * Return the ButtonPanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JToolBar getToolBar() {
	if (toolBar == null) {
		try {
			toolBar = new javax.swing.JToolBar();
			toolBar.setFloatable(false);
			
			copyButton = new JButton("", VCellIcons.copySimIcon);
			copyButton.setToolTipText("Copy Simulation");
			copyButton.addActionListener(ivjEventHandler);			
			stopButton = new JButton("", VCellIcons.stopSimIcon);
			stopButton.setToolTipText("Stop Simulation");
			stopButton.setEnabled(false);
			stopButton.addActionListener(ivjEventHandler);
			statusDetailsButton = new JButton("", VCellIcons.statusDetailscon);
			statusDetailsButton.setToolTipText("Simulation Status Details...");
			statusDetailsButton.addActionListener(ivjEventHandler);
			particleViewButton = new JButton("", VCellIcons.particleRunSimIcon);
			particleViewButton.setToolTipText("Real-Time Particle View");
			particleViewButton.addActionListener(ivjEventHandler);
						
			toolBar.addSeparator();
			toolBar.add(getNewButton());
			toolBar.add(copyButton);
			toolBar.add(getEditButton());
			toolBar.add(getDeleteButton());
			toolBar.addSeparator();
			toolBar.add(getRunButton());
			toolBar.add(stopButton);
			toolBar.add(getResultsButton());
			toolBar.add(statusDetailsButton);
			toolBar.addSeparator();
			toolBar.add(particleViewButton);
			
			ReactionCartoonEditorPanel.setToolBarButtonSizes(getNewButton());
			ReactionCartoonEditorPanel.setToolBarButtonSizes(copyButton);
			ReactionCartoonEditorPanel.setToolBarButtonSizes(getEditButton());
			ReactionCartoonEditorPanel.setToolBarButtonSizes(getDeleteButton());
			ReactionCartoonEditorPanel.setToolBarButtonSizes(getRunButton());
			ReactionCartoonEditorPanel.setToolBarButtonSizes(stopButton);
			ReactionCartoonEditorPanel.setToolBarButtonSizes(getResultsButton());
			ReactionCartoonEditorPanel.setToolBarButtonSizes(statusDetailsButton);
			ReactionCartoonEditorPanel.setToolBarButtonSizes(particleViewButton);

		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return toolBar;
}

/**
 * Return the EditButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getEditButton() {
	if (ivjEditButton == null) {
		try {
			ivjEditButton = new javax.swing.JButton("", VCellIcons.editSimIcon);
			ivjEditButton.setName("EditButton");
			ivjEditButton.setToolTipText("Edit Simulation");
			ivjEditButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjEditButton;
}

/**
 * Return the NewButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getNewButton() {
	if (ivjNewButton == null) {
		try {
			ivjNewButton = new javax.swing.JButton("", VCellIcons.newSimIcon);
			ivjNewButton.setName("NewButton");
			ivjNewButton.setToolTipText("New Simulation");
			ivjNewButton.setEnabled(true);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNewButton;
}


/**
 * Return the ResultsButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getResultsButton() {
	if (ivjResultsButton == null) {
		try {
			ivjResultsButton = new javax.swing.JButton("", VCellIcons.resultsIcon);
			ivjResultsButton.setName("ResultsButton");
			ivjResultsButton.setToolTipText("Simulation Results");
			ivjResultsButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjResultsButton;
}


/**
 * Return the RunButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getRunButton() {
	if (ivjRunButton == null) {
		try {
			ivjRunButton = new javax.swing.JButton("", VCellIcons.runSimIcon);
			ivjRunButton.setName("RunButton");
			ivjRunButton.setToolTipText("Run Simulation");
			ivjRunButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjRunButton;
}

private javax.swing.JButton getDeleteButton() {
	if (ivjDeleteButton == null) {
		try {
			ivjDeleteButton = new javax.swing.JButton("", VCellIcons.delSimIcon);
			ivjDeleteButton.setName("DeleteButton");
			ivjDeleteButton.setToolTipText("Delete Simulation");
			ivjDeleteButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDeleteButton;
}

/**
 * Return the ScrollPaneTable property value.
 * @return cbit.gui.JTableFixed
 */
private ScrollTable getScrollPaneTable() {
	if (ivjScrollPaneTable == null) {
		try {
			ivjScrollPaneTable = new ScrollTable() {
				@Override
				public JToolTip createToolTip() {
					MultiLineToolTip tip = new MultiLineToolTip();
			        tip.setComponent(this);
			        return tip;
				}
			};
			ivjScrollPaneTable.setName("ScrollPaneTable");
			ivjScrollPaneTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
			ivjScrollPaneTable.setModel(getSimulationListTableModel1());
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjScrollPaneTable;
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2004 2:00:46 PM)
 * @return int[]
 */
public int[] getSelectedRows() {
	return getScrollPaneTable().getSelectedRows();
}

/**
 * Return the SimulationListTableModel1 property value.
 * @return cbit.vcell.client.desktop.biomodel.SimulationListTableModel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private SimulationListTableModel getSimulationListTableModel1() {
	if (ivjSimulationListTableModel1 == null) {
		try {
			ivjSimulationListTableModel1 = new SimulationListTableModel(ivjScrollPaneTable);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjSimulationListTableModel1;
}

private OutputFunctionsPanel getOutputFunctionsPanel() {
	if (outputFunctionsPanel == null) {
		try {
			outputFunctionsPanel = new OutputFunctionsPanel();
			outputFunctionsPanel.setName("ObservablesPanel");
			addPropertyChangeListener(ivjEventHandler);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return outputFunctionsPanel;
}

/**
 * Gets the simulationWorkspace property (cbit.vcell.client.desktop.simulation.SimulationWorkspace) value.
 * @return The simulationWorkspace property value.
 * @see #setSimulationWorkspace
 */
public SimulationWorkspace getSimulationWorkspace() {
	return fieldSimulationWorkspace;
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
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getNewButton().addActionListener(ivjEventHandler);
	getEditButton().addActionListener(ivjEventHandler);
	getDeleteButton().addActionListener(ivjEventHandler);
	getRunButton().addActionListener(ivjEventHandler);
	getResultsButton().addActionListener(ivjEventHandler);
	getMoreActionsButton().addActionListener(ivjEventHandler);
	getScrollPaneTable().addPropertyChangeListener(ivjEventHandler);
	
	getOutputFunctionsPanel().addPropertyChangeListener(ivjEventHandler);
	getScrollPaneTable().getSelectionModel().addListSelectionListener(ivjEventHandler);
	DefaultScrollTableCellRenderer renderer = new DefaultScrollTableCellRenderer(){

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus,	row, column);
			if (value instanceof OutputTimeSpec) {
				setText(((OutputTimeSpec) value).getDescription());
			} else if (value instanceof Double) {
				setText(value + "s");
			}
			return this;
		}
		
	};
	getScrollPaneTable().setDefaultRenderer(OutputTimeSpec.class, renderer);
	getScrollPaneTable().setDefaultRenderer(Double.class, renderer);
	getScrollPaneTable().setDefaultEditor(OutputTimeSpec.class, new DefaultCellEditor(new JTextField()));
}

/**
 * Initialize the class.
 */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("SimulationListPanel");
		setSize(750, 560);
						
		setLayout(new BorderLayout());
		add(getToolBar(), BorderLayout.NORTH);
		add(getScrollPaneTable().getEnclosingScrollPane(), BorderLayout.CENTER);
		
		initConnections();
		getScrollPaneTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		JFrame frame = new javax.swing.JFrame("SimulationListPanel");
		SimulationListPanel aSimulationListPanel;
		aSimulationListPanel = new SimulationListPanel();
		frame.setContentPane(aSimulationListPanel);
		frame.setSize(aSimulationListPanel.getSize());
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
 * Comment
 */
private void newSimulation() {
	AsynchClientTask task1 = new AsynchClientTask("new simulation", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
		
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			getSimulationWorkspace().getSimulationOwner().refreshMathDescription();
		}
	};
	AsynchClientTask task2 = new AsynchClientTask("new simulation", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
		
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			int newSimIndex = getSimulationWorkspace().newSimulation();
			getScrollPaneTable().getSelectionModel().setSelectionInterval(newSimIndex, newSimIndex);
			getScrollPaneTable().scrollRectToVisible(getScrollPaneTable().getCellRect(newSimIndex, 0, true));
		}
	};
	ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2});
}

/**
 * Comment
 */
private void refreshButtonsLax() {
	if (fieldSimulationWorkspace.getSimulationOwner().getMathDescription() != null) {
		particleViewButton.setVisible(fieldSimulationWorkspace.getSimulationOwner().getMathDescription().isSpatialStoch());
	}
	
	int[] selections = getScrollPaneTable().getSelectedRows();
	
	boolean bCopy = false;
	boolean bEditable = false;
	boolean bDeletable = false;
	boolean bRunnable = false;
	boolean bStoppable = false;
	boolean bHasData = false;
	boolean bStatusDetails = false;
	boolean bParticleView = false;
	
	if (selections != null && selections.length > 0) {
		bCopy = true;
		bStatusDetails = true;
		Simulation firstSelection = ivjSimulationListTableModel1.getValueAt(selections[0]);
		if (selections.length == 1){
			SimulationStatus simStatus = getSimulationWorkspace().getSimulationStatus(firstSelection);
			if (!simStatus.isRunning()){
				bEditable = true;
			}
			bParticleView = firstSelection.getScanCount() == 1;			
		}
		
		// we make'em true if at least one sim satisfies criterion (lax policy)
		for (int i = 0; i < selections.length; i++){
			Simulation sim = ivjSimulationListTableModel1.getValueAt(selections[i]);
			SimulationStatus simStatus = getSimulationWorkspace().getSimulationStatus(sim);
			bDeletable = bDeletable || !simStatus.isRunning();
			bRunnable = bRunnable || simStatus.isRunnable();
			bStoppable = bStoppable || simStatus.isStoppable();
			bHasData = bHasData || simStatus.getHasData();
		}
	}
	copyButton.setEnabled(bCopy);
	getEditButton().setEnabled(bEditable);
	getDeleteButton().setEnabled(bDeletable);	
	getRunButton().setEnabled(bRunnable);
	stopButton.setEnabled(bStoppable);
	getResultsButton().setEnabled(bHasData);
	statusDetailsButton.setEnabled(bStatusDetails);
	particleViewButton.setEnabled(bParticleView);
}


/**
 * Comment
 */
private void runSimulations() {
	final ArrayList<Simulation> simList = new ArrayList<Simulation>();
	int[] selections = getScrollPaneTable().getSelectedRows();
	for (int i = 0; i < selections.length; i++){
		Simulation sim = getSimulationWorkspace().getSimulations()[selections[i]];	
		simList.add(sim);
	}
	Simulation[] toRun = simList.toArray(new Simulation[0]);
	getSimulationWorkspace().runSimulations(toRun);
}


/**
 * Comment
 */
public void scrollPaneTable_FocusLost(java.awt.event.FocusEvent focusEvent) {
	int row = getScrollPaneTable().getSelectedRow();
	int col = getScrollPaneTable().getSelectedColumn();
	TableCellEditor ce = getScrollPaneTable().getCellEditor(row, col);
	if (ce != null) {
		ce.stopCellEditing();
	}
}

/**
 * Sets the simulationWorkspace property (cbit.vcell.client.desktop.simulation.SimulationWorkspace) value.
 * @param simulationWorkspace The new value for the property.
 * @see #getSimulationWorkspace
 */
public void setSimulationWorkspace(SimulationWorkspace newValue) {
	if (fieldSimulationWorkspace == newValue) {
		return;
	}
	SimulationWorkspace oldValue = fieldSimulationWorkspace;
	if (oldValue != null) {
		oldValue.removePropertyChangeListener(ivjEventHandler);
	}
	fieldSimulationWorkspace = newValue;
	if (fieldSimulationWorkspace != null) {
		fieldSimulationWorkspace.addPropertyChangeListener(ivjEventHandler);
	}
	getSimulationListTableModel1().setSimulationWorkspace(fieldSimulationWorkspace);	
	refreshButtonsLax();
}


/**
 * Comment
 */
private void showSimulationResults() {
	int[] selections = getScrollPaneTable().getSelectedRows();
	Vector<Simulation> v = new Vector<Simulation>();
	for (int i = 0; i < selections.length; i++){
		v.add(getSimulationWorkspace().getSimulations()[selections[i]]);
	}
	Simulation[] toShow = (Simulation[])BeanUtils.getArray(v, Simulation.class);
	getSimulationWorkspace().showSimulationResults(toShow);
}


/**
 * Comment
 */
private void showSimulationStatusDetails() {
	int[] selections = getScrollPaneTable().getSelectedRows();
	Vector<Simulation> v = new Vector<Simulation>();
	for (int i = 0; i < selections.length; i++){
		v.add(getSimulationWorkspace().getSimulations()[selections[i]]);
	}
	Simulation[] sims = (Simulation[])BeanUtils.getArray(v, Simulation.class);
	getSimulationWorkspace().showSimulationStatusDetails(sims);
}


/**
 * Comment
 */
private void stopSimulations() {
	int[] selections = getScrollPaneTable().getSelectedRows();
	Vector<Simulation> v = new Vector<Simulation>();
	for (int i = 0; i < selections.length; i++){
		v.add(getSimulationWorkspace().getSimulations()[selections[i]]);
	}
	Simulation[] toStop = (Simulation[])BeanUtils.getArray(v, Simulation.class);
	getSimulationWorkspace().stopSimulations(toStop);
}
	
	private javax.swing.JButton getMoreActionsButton() {
		if (moreActionsButton == null) {
			try {
				moreActionsButton = new JButton("More Actions", new DownArrowIcon());
				moreActionsButton.setHorizontalTextPosition(SwingConstants.LEFT);
				moreActionsButton.setName("MoreActionsButton");
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return moreActionsButton;
	}

	@Override
	protected void onSelectedObjectsChange(Object[] selectedObjects) {
		setTableSelections(selectedObjects, getScrollPaneTable(), getSimulationListTableModel1());
		
	}
	
	private void particleView() {
		int row = getScrollPaneTable().getSelectedRow();
		if (row < 0) {
			return;
		}
		AsynchClientTask[] tasks = null;
		SimulationOwner simulationOwner = getSimulationWorkspace().getSimulationOwner();
		final Simulation selectedSim = getSimulationListTableModel1().getValueAt(row);
		if (simulationOwner instanceof SimulationContext) {
			AsynchClientTask[] updateTask = ClientRequestManager.updateMath(this, ((SimulationContext)simulationOwner));
			tasks = new AsynchClientTask[updateTask.length + 1];
			System.arraycopy(updateTask, 0, tasks, 0, updateTask.length);
		} else {
			tasks = new AsynchClientTask[1];
		}
		
		tasks[tasks.length - 1] = new AsynchClientTask("start simulating", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				File smoldynExe = ResourceUtil.getSmoldynExecutable();
				SimulationJob simJob = new SimulationJob(selectedSim, 0, null);
				File inputFile = new File(smoldynExe.getParent(), simJob.getSimulationJobID() + SimDataConstants.SMOLDYN_INPUT_FILE_EXTENSION);
				inputFile.deleteOnExit();
				PrintWriter pw = new PrintWriter(inputFile);
				SmoldynFileWriter smf = new SmoldynFileWriter(pw, true, null, simJob, false);
				smf.write();
				pw.close();				
				String[] cmd = new String[] {smoldynExe.getAbsolutePath(), inputFile.getAbsolutePath()};
				StringBuilder commandLine = new StringBuilder();
				for (int i = 0; i < cmd.length; i ++) {
					if (i > 0) {
						commandLine.append(" ");
					}		
					commandLine.append(TokenMangler.getEscapedPathName(cmd[i]));		
				}
				System.out.println(commandLine);
				ProcessBuilder processBuilder = new ProcessBuilder(cmd);
				Process process = processBuilder.start();
				
//				long t = System.currentTimeMillis();
//				long timeoutMS = 0;
//				long pollingIntervalMS = 1000;
//				char charArrayOut[] = new char[10000];
//				char charArrayErr[] = new char[10000];
//				String outString = new String();
//				String errString = new String();
//				int numReadOut = 0; int numReadErr = 0; int exitValue = 0;
//				InputStream inputStreamOut = process.getInputStream();
//				InputStream inputStreamErr = process.getErrorStream();
//				InputStreamReader inputStreamReaderOut = new InputStreamReader(inputStreamOut);
//				InputStreamReader inputStreamReaderErr = new InputStreamReader(inputStreamErr);
//
//				boolean running = true;
//				while (running || (numReadOut > 0) || (numReadErr > 0)) {
//					if (timeoutMS > 0 && System.currentTimeMillis() - t > timeoutMS) {
//						throw new ExecutableException("Process timed out");
//					}
//					try {
//						exitValue = process.exitValue();
//						running = false;
//					} catch (IllegalThreadStateException e) {
//						// process didn't exit yet, do nothing
//					}
//					try {
//						if (pollingIntervalMS > 0) Thread.sleep(pollingIntervalMS);
//					} catch (InterruptedException e) {
//					}
//					try {
//						if (inputStreamOut.available() > 0) {
//							numReadOut = inputStreamReaderOut.read(charArrayOut, 0, charArrayOut.length);
//						} else {
//							numReadOut = 0;
//						}
//					} catch (IOException ioexc) {
//						System.out.println("EXCEPTION (process " + commandLine + ") - IOException while reading StdOut: " + ioexc.getMessage());
//						numReadOut = 0;
//					}
//					try {
//						if (inputStreamErr.available() > 0) {
//							numReadErr = inputStreamReaderErr.read(charArrayErr, 0, charArrayErr.length);
//						} else {
//							numReadErr = 0;
//						}
//					} catch (IOException ioexc) {
//						System.out.println("EXCEPTION (process " + commandLine + ") - IOException while reading StdErr: " + ioexc.getMessage());
//						numReadErr = 0;
//					}
//					if (numReadOut > 0) {
//						String newInput = new String(charArrayOut, 0, numReadOut);
//						outString += newInput;
//						if (outString.contains("[[[progress:0%]]]")) {
//							return;
//						}
//						if (numReadOut == charArrayOut.length) {
//							outString += "\n(standard output truncated...)";
//						}
//					}
//					if (numReadErr > 0) {
//						String newInput = new String(charArrayErr, 0, numReadErr);
//						errString += newInput;
//						if (numReadErr == charArrayErr.length) {
//							errString += "\n(standard output truncated...)";
//						}
//					}
//				}
			}
		};
		ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), tasks, false);
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
