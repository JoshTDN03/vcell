package cbit.plot;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.NonEditableDefaultTableModel;
import org.vcell.util.gui.ScrollTable;

import cbit.vcell.desktop.VCellTransferable;
import cbit.vcell.model.ReservedSymbol;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.SymbolTableEntry;
/**
 * Insert the type's description here.
 * Creation date: (4/19/2001 12:33:58 PM)
 * @author: Ion Moraru
 */
public class Plot2DDataPanel extends JPanel {

class IvjEventHandler implements java.awt.event.ActionListener, java.awt.event.MouseListener, java.beans.PropertyChangeListener, javax.swing.event.ChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == Plot2DDataPanel.this.getJMenuItemCopy()) 
				connEtoC2(e);
			if (e.getSource() == Plot2DDataPanel.this.getJMenuItemCopyAll()) 
				connEtoC4(e);
		};
		public void mouseClicked(java.awt.event.MouseEvent e) {
			if (e.getSource() == Plot2DDataPanel.this.getScrollPaneTable()) 
				connEtoC1(e);
		};
		public void mouseEntered(java.awt.event.MouseEvent e) {};
		public void mouseExited(java.awt.event.MouseEvent e) {};
		public void mousePressed(java.awt.event.MouseEvent e) {};
		public void mouseReleased(java.awt.event.MouseEvent e) {};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == Plot2DDataPanel.this && (evt.getPropertyName().equals("plot2D"))) 
				connPtoP2SetTarget();
		};
		public void stateChanged(javax.swing.event.ChangeEvent e) {
			if (e.getSource() == Plot2DDataPanel.this.getplot2D1()) 
				connEtoM2(e);
		};
	}
	private Plot2D fieldPlot2D = new Plot2D(null,null, null);
	private boolean ivjConnPtoP2Aligning = false;
	private Plot2D ivjplot2D1 = null;
	private ScrollTable ivjScrollPaneTable = null;
	private NonEditableDefaultTableModel ivjNonEditableDefaultTableModel1 = null;
	private JMenuItem ivjJMenuItemCopy = null;
	private JPopupMenu ivjJPopupMenu1 = null;
	private JMenuItem ivjJMenuItemCopyAll = null;
	IvjEventHandler ivjEventHandler = new IvjEventHandler();

/**
 * Plot2DDataPanel constructor comment.
 */
public Plot2DDataPanel() {
	super();
	initialize();
}

/**
 * connEtoC1:  (ScrollPaneTable.mouse.mouseClicked(java.awt.event.MouseEvent) --> Plot2DDataPanel.scrollPaneTable_MouseClicked(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.showPopupMenu(arg1, getJPopupMenu1());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC2:  (JMenuItemCopy.action.actionPerformed(java.awt.event.ActionEvent) --> Plot2DDataPanel.copySelection()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.copyCells(getJMenuItemCopy().getActionCommand());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC3:  (Plot2DDataPanel.initialize() --> Plot2DDataPanel.controlKeys()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3() {
	try {
		// user code begin {1}
		// user code end
		this.controlKeys();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC4:  (JMenuItemCopyAll.action.actionPerformed(java.awt.event.ActionEvent) --> Plot2DDataPanel.copyCells(Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.copyCells(getJMenuItemCopyAll().getActionCommand());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoM1:  (plot2D1.this --> DefaultTableModel1.setDataVector([[Ljava.lang.Object;[Ljava.lang.Object;)V)
 * @param value cbit.plot.Plot2D
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM1(Plot2D value) {
	try {
		// user code begin {1}
		// user code end
		if (getplot2D1() != null) {
			getNonEditableDefaultTableModel1().setDataVector(getplot2D1().getVisiblePlotDataValuesByRow(), getplot2D1().getVisiblePlotColumnTitles());
		}else{
			getNonEditableDefaultTableModel1().setDataVector((Vector<Object>)null,(Vector<Object>)null);			
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoM2:  (plot2D1.change.stateChanged(javax.swing.event.ChangeEvent) --> NonEditableDefaultTableModel1.setDataVector([[Ljava.lang.Object;[Ljava.lang.Object;)V)
 * @param arg1 javax.swing.event.ChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM2(javax.swing.event.ChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		if (getplot2D1() != null) {
			getNonEditableDefaultTableModel1().setDataVector(getplot2D1().getVisiblePlotDataValuesByRow(), getplot2D1().getVisiblePlotColumnTitles());
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connPtoP1SetTarget:  (DefaultTableModel1.this <--> ScrollPaneTable.model)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP1SetTarget() {
	/* Set the target from the source */
	try {
		getScrollPaneTable().setModel(getNonEditableDefaultTableModel1());
		getScrollPaneTable().createDefaultColumnsFromModel();
		// user code begin {1}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connPtoP2SetSource:  (Plot2DDataPanel.plot2D <--> plot2D1.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP2SetSource() {
	/* Set the source from the target */
	try {
		if (ivjConnPtoP2Aligning == false) {
			// user code begin {1}
			// user code end
			ivjConnPtoP2Aligning = true;
			if ((getplot2D1() != null)) {
				this.setPlot2D(getplot2D1());
			}
			// user code begin {2}
			// user code end
			ivjConnPtoP2Aligning = false;
		}
	} catch (java.lang.Throwable ivjExc) {
		ivjConnPtoP2Aligning = false;
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connPtoP2SetTarget:  (Plot2DDataPanel.plot2D <--> plot2D1.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP2SetTarget() {
	/* Set the target from the source */
	try {
		if (ivjConnPtoP2Aligning == false) {
			// user code begin {1}
			// user code end
			ivjConnPtoP2Aligning = true;
			setplot2D1(this.getPlot2D());
			// user code begin {2}
			// user code end
			ivjConnPtoP2Aligning = false;
		}
	} catch (java.lang.Throwable ivjExc) {
		ivjConnPtoP2Aligning = false;
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * Comment
 */
private void controlKeys() {
	registerKeyboardAction(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			copyCells("Copy");
		}
	}, KeyStroke.getKeyStroke("ctrl C"), WHEN_IN_FOCUSED_WINDOW);
	registerKeyboardAction(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			copyCells("Copy All");
		}
	}, KeyStroke.getKeyStroke("ctrl K"), WHEN_IN_FOCUSED_WINDOW);
}


/**
 * Insert the method's description here.
 * Creation date: (4/20/2001 4:52:52 PM)
 * @param actionCommand java.lang.String
 * @return java.lang.String
 */
private synchronized void copyCells(String actionCommand) {
	try{
		int r = 0;
		int c = 0;
		int[] rows = new int[0];
		int[] columns = new int[0];
		if (actionCommand.equals("Copy")) {
			r = getScrollPaneTable().getSelectedRowCount();
			c = getScrollPaneTable().getSelectedColumnCount();
			rows = getScrollPaneTable().getSelectedRows();
			columns = getScrollPaneTable().getSelectedColumns();
		}
		if (actionCommand.equals("Copy All")) {
			r = getScrollPaneTable().getRowCount();
			c = getScrollPaneTable().getColumnCount();
			rows = new int[r];
			columns = new int[c];
			for (int i = 0; i < rows.length; i++){
				rows[i] = i;
			}
			for (int i = 0; i < columns.length; i++){
				columns[i] = i;
			}
		}
		StringBuffer buffer = new StringBuffer();
		//check if selected first column is time.
		boolean bHasTimeColumn = false;
		String selectedFirstColName = getScrollPaneTable().getColumnName(columns[0]);
		if(selectedFirstColName.equals(ReservedSymbol.TIME.getName()))
		{
			bHasTimeColumn = true;
		}
		//check if it is histogram (check name of the table first column name)
		boolean bHistogram = false;
		String firstColName = getScrollPaneTable().getColumnName(0);
		if(!firstColName.equals(ReservedSymbol.TIME.getName()))
		{
			bHistogram = true;
		}
		SymbolTableEntry[] symbolTableEntries = new SymbolTableEntry[c - (bHasTimeColumn?1:0)];
		Expression[] resolvedValues = new Expression[symbolTableEntries.length];
		//String[] dataNames = new String[symbolTableEntries.length];//don't include "t" for SimulationResultsSelection
		// if copying more than one cell, make a string that will paste like a table in spreadsheets
		// also include column headers in this case
		if (r + c > 2) {
			for (int i = 0; i < c; i++){
				//this if condition is dangerous, because it assumes that "t" appears only on column idx 0, other column numbers should be
				//greater than 0. However, histogram doesn't have "t" and there is sth. else in column 0 of the table.
				if(!bHistogram && (!bHasTimeColumn || i>0)){ 
					//dataNames[i-(bHasTimeColumn?1:0)] = getScrollPaneTable().getColumnName(columns[i]);
					symbolTableEntries[i-(bHasTimeColumn?1:0)] = null;
					if(getPlot2D().getSymbolTableEntries() != null){
						SymbolTableEntry ste =  getPlot2D().getPlotDataSymbolTableEntry(columns[i]);
						symbolTableEntries[i-(bHasTimeColumn?1:0)] = ste;
						buffer.append(
							( ste != null?"(Var="+(ste.getNameScope() != null?ste.getNameScope().getName()+"_":"")+ste.getName()+") ":"")+
							getScrollPaneTable().getColumnName(columns[i]) + (i==c-1?"":"\t"));
					}
				}else{
					buffer.append(getScrollPaneTable().getColumnName(columns[i]) + (i==c-1?"":"\t"));
				}
			}
			for (int i = 0; i < r; i++){
				buffer.append("\n");
				for (int j = 0; j < c; j++){
					Object cell = getScrollPaneTable().getValueAt(rows[i], columns[j]);
					cell = cell != null ? cell : ""; 
					buffer.append(cell.toString() + (j==c-1?"":"\t"));
					if(!cell.equals("") && (!bHasTimeColumn || j>0) ){
						resolvedValues[j-(bHasTimeColumn?1:0)] = new Expression(((Double)cell).doubleValue());
					}
				}
			}
		}
		// if copying a single cell, just get that value 
		if (r + c == 2) {
			Object cell = getScrollPaneTable().getValueAt(rows[0], columns[0]);
			cell = (cell != null ? cell : ""); 
			buffer.append(cell.toString());
			if(!bHasTimeColumn){
				//dataNames[0] = getScrollPaneTable().getColumnName(columns[0]);
				symbolTableEntries[0] = null;
				if(getPlot2D().getSymbolTableEntries() != null){					
					SymbolTableEntry ste =getPlot2D().getPlotDataSymbolTableEntry(columns[0]);					
					symbolTableEntries[0] = ste;
				}
				resolvedValues[0] = new Expression(((Double)cell).doubleValue());
			}
		}

		VCellTransferable.ResolvedValuesSelection rvs =
			new VCellTransferable.ResolvedValuesSelection(symbolTableEntries,null,resolvedValues,buffer.toString());
		VCellTransferable.sendToClipboard(rvs);
	}catch(Throwable e){
		e.printStackTrace();
		DialogUtils.showErrorDialog(Plot2DDataPanel.this, "Copy failed.  "+e.getMessage(), e);
	}
}


/**
 * Return the JMenuItemCopy property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getJMenuItemCopy() {
	if (ivjJMenuItemCopy == null) {
		try {
			ivjJMenuItemCopy = new javax.swing.JMenuItem();
			ivjJMenuItemCopy.setName("JMenuItemCopy");
			ivjJMenuItemCopy.setText("Copy");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJMenuItemCopy;
}


/**
 * Return the JMenuItemCopyAll property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getJMenuItemCopyAll() {
	if (ivjJMenuItemCopyAll == null) {
		try {
			ivjJMenuItemCopyAll = new javax.swing.JMenuItem();
			ivjJMenuItemCopyAll.setName("JMenuItemCopyAll");
			ivjJMenuItemCopyAll.setText("Copy All");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJMenuItemCopyAll;
}

/**
 * Return the JPopupMenu1 property value.
 * @return javax.swing.JPopupMenu
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPopupMenu getJPopupMenu1() {
	if (ivjJPopupMenu1 == null) {
		try {
			ivjJPopupMenu1 = new javax.swing.JPopupMenu();
			ivjJPopupMenu1.setName("JPopupMenu1");
			ivjJPopupMenu1.add(getJMenuItemCopy());
			ivjJPopupMenu1.add(getJMenuItemCopyAll());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPopupMenu1;
}

/**
 * Return the NonEditableDefaultTableModel1 property value.
 * @return cbit.gui.NonEditableDefaultTableModel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private org.vcell.util.gui.NonEditableDefaultTableModel getNonEditableDefaultTableModel1() {
	if (ivjNonEditableDefaultTableModel1 == null) {
		try {
			ivjNonEditableDefaultTableModel1 = new org.vcell.util.gui.NonEditableDefaultTableModel();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNonEditableDefaultTableModel1;
}


/**
 * Gets the plot2D property (cbit.plot.Plot2D) value.
 * @return The plot2D property value.
 * @see #setPlot2D
 */
public Plot2D getPlot2D() {
	return fieldPlot2D;
}


/**
 * Return the plot2D1 property value.
 * @return cbit.plot.Plot2D
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private Plot2D getplot2D1() {
	// user code begin {1}
	// user code end
	return ivjplot2D1;
}


/**
 * Return the ScrollPaneTable property value.
 * @return javax.swing.JTable
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ScrollTable getScrollPaneTable() {
	if (ivjScrollPaneTable == null) {
		try {
			ivjScrollPaneTable = new ScrollTable();
			ivjScrollPaneTable.setName("ScrollPaneTable");
			ivjScrollPaneTable.setCellSelectionEnabled(true);
			ivjScrollPaneTable.setBounds(0, 0, 200, 200);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjScrollPaneTable;
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
	this.addPropertyChangeListener(ivjEventHandler);
	getScrollPaneTable().addMouseListener(ivjEventHandler);
	getJMenuItemCopy().addActionListener(ivjEventHandler);
	getJMenuItemCopyAll().addActionListener(ivjEventHandler);
	connPtoP2SetTarget();
	connPtoP1SetTarget();
}

/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("Plot2DDataPanel");
		setLayout(new java.awt.BorderLayout());
		setSize(541, 348);
		add(getScrollPaneTable().getEnclosingScrollPane(), "Center");
		initConnections();
		connEtoC3();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
}

/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		JFrame frame = new javax.swing.JFrame();
		Plot2DDataPanel aPlot2DDataPanel;
		aPlot2DDataPanel = new Plot2DDataPanel();
		frame.setContentPane(aPlot2DDataPanel);
		frame.setSize(aPlot2DDataPanel.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);
		aPlot2DDataPanel.setPlot2D(Plot2DPanel.getSamplePlot2D());
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JPanel");
		exception.printStackTrace(System.out);
	}
}


/**
 * Sets the plot2D property (cbit.plot.Plot2D) value.
 * @param plot2D The new value for the property.
 * @see #getPlot2D
 */
public void setPlot2D(Plot2D plot2D) {
	Plot2D oldValue = fieldPlot2D;
	fieldPlot2D = plot2D;
	firePropertyChange("plot2D", oldValue, plot2D);
}


/**
 * Set the plot2D1 to a new value.
 * @param newValue cbit.plot.Plot2D
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setplot2D1(Plot2D newValue) {
	if (ivjplot2D1 != newValue) {
		try {
			cbit.plot.Plot2D oldValue = getplot2D1();
			/* Stop listening for events from the current object */
			if (ivjplot2D1 != null) {
				ivjplot2D1.removeChangeListener(ivjEventHandler);
			}
			ivjplot2D1 = newValue;

			/* Listen for events from the new object */
			if (ivjplot2D1 != null) {
				ivjplot2D1.addChangeListener(ivjEventHandler);
			}
			connPtoP2SetSource();
			connEtoM1(ivjplot2D1);
			firePropertyChange("plot2D", oldValue, newValue);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}

/**
 * Comment
 */
private void showPopupMenu(MouseEvent mouseEvent, javax.swing.JPopupMenu menu) {
	if (SwingUtilities.isRightMouseButton(mouseEvent)) {
		menu.show(getScrollPaneTable(), mouseEvent.getPoint().x, mouseEvent.getPoint().y);
	}
}
}