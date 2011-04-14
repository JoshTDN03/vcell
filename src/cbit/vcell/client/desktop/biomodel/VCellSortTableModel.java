package cbit.vcell.client.desktop.biomodel;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.vcell.util.Issue;
import org.vcell.util.gui.ScrollTable;
import org.vcell.util.gui.sorttable.SortPreference;
import org.vcell.util.gui.sorttable.SortTableModel;

import cbit.vcell.client.desktop.biomodel.IssueManager.IssueEvent;
import cbit.vcell.client.desktop.biomodel.IssueManager.IssueEventListener;
import cbit.vcell.math.OutputFunctionContext.OutputFunctionIssueSource;

@SuppressWarnings("serial")
public abstract class VCellSortTableModel<T> extends AbstractTableModel  implements SortTableModel, IssueEventListener {
	protected IssueManager issueManager;
	protected List<Issue> issueList = new ArrayList<Issue>();
	protected ScrollTable ownerTable = null;
	protected int currentPageIndex = 0;
	private static int MAX_ROWS_PER_PAGE = 200;
	private List<T> allRows = Collections.synchronizedList(new ArrayList<T>());

	private static final String PROPERTY_NAME_SORT_PREFERENCE = "sortPreference";
	private transient java.beans.PropertyChangeSupport propertyChange;
	private SortPreference fieldSortPreference = new SortPreference(true, -1);
	private List<T> visibleRows = Collections.synchronizedList(new ArrayList<T>());
	private String columns[] = null;		

	public VCellSortTableModel() {
		this(null, null);
	}
	public VCellSortTableModel(ScrollTable table) {
		this(table, null);
	}
	public VCellSortTableModel(String[] cols) {
		this(null, cols);
	}	
	public VCellSortTableModel(ScrollTable table, String[] cols) {
		ownerTable = table;
		columns = cols;
	}
	
	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public int getRowIndex(T rowObject) {
		for (int i = 0; i < visibleRows.size(); i ++) {
			if (visibleRows.get(i) == rowObject) {
				return i;
			}
		}
		return -1;
	}

	public T getValueAt(int row) {
		if (row >= 0 && row < visibleRows.size()) {
			return visibleRows.get(row);
		}
		return null;
	}
	
	public void removeValueAt(int row) {
		T object = visibleRows.remove(row);
		if (object != null) {
			for (T o : allRows) {
				if (o == object) {
					allRows.remove(o);
					break;
				}
			}
		}
		fireTableDataChanged();
	}
	
	public void setColumns(String[] newValue) {
		columns = newValue;
		fireTableStructureChanged();
	}

/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}

/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}

private java.beans.PropertyChangeSupport getPropertyChange() {
	if (propertyChange == null) {
		propertyChange = new java.beans.PropertyChangeSupport(this);
	};
	return propertyChange;
}

public SortPreference getSortPreference() {
	return fieldSortPreference;
}


/**
 * The hasListeners method was generated to support the propertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getPropertyChange().hasListeners(propertyName);
}


public boolean isSortable(int col) {
	return true;
}


/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}


protected void resortColumn() {
	if (getSortPreference() != null && getSortPreference().getSortedColumnIndex() != -1) {
		Collections.sort(allRows, getComparator(getSortPreference().getSortedColumnIndex(), getSortPreference().isSortedColumnAscending()));
	}	
}

/**
 * Sets the sortPreference property (cbit.vcell.messaging.admin.sorttable.SortPreference) value.
 * @param sortPreference The new value for the property.
 * @see #getSortPreference
 */
public void setSortPreference(SortPreference sortPreference) {
	if (fieldSortPreference == sortPreference || fieldSortPreference.equals(sortPreference)) {
		return;
	}
	SortPreference oldValue = fieldSortPreference;
	fieldSortPreference = sortPreference;
	resortColumn();
	currentPageIndex = 0;
	updateVisibleRows();
	firePropertyChange(PROPERTY_NAME_SORT_PREFERENCE, oldValue, sortPreference);
}

protected abstract Comparator<T> getComparator(final int col, final boolean ascending);
	
	public void gotoFirstPage() {
		currentPageIndex = 0;
		updateVisibleRows();
	}

	public void gotoPreviousPage() {
		if (currentPageIndex > 0) {
			currentPageIndex --;
		}
		updateVisibleRows();
	}
	
	public void gotoNextPage() {
		if (currentPageIndex < getNumPages() - 1) {
			currentPageIndex ++;
		}
		updateVisibleRows();
	}	
	public void gotoLastPage() {
		currentPageIndex = getNumPages() - 1;
		updateVisibleRows();
	}
	public boolean hasNextPage() {
		return currentPageIndex < getNumPages() - 1;
	}
	public boolean hasPreviousPage() {
		return currentPageIndex > 0;
	}
	
	public String getPageDescription() {
		int pageOffset = currentPageIndex * MAX_ROWS_PER_PAGE;
		return (pageOffset + 1) + " - " + (pageOffset + getDataRowCount()) + " of " + allRows.size();
	}
	
	public int getNumPages() {
		int size = allRows.size();
		int numPages = size / MAX_ROWS_PER_PAGE;
		if (size % MAX_ROWS_PER_PAGE != 0) {
			numPages ++;
		}
		return Math.max(1, numPages);
	}
	
	public int getDataRowCount() {
		return Math.min(allRows.size() - currentPageIndex * MAX_ROWS_PER_PAGE, MAX_ROWS_PER_PAGE);
	}
	
	public int getRowCount() {
		return getDataRowCount();
	}
	
	public void issueChange(IssueEvent issueEvent) {		
		//fireTableDataChanged();
		ownerTable.repaint();
	}
	
	public void setData(List<? extends T> list) {
		allRows.clear();
		if (list != null) {
			allRows.addAll(list);
			resortColumn();
		}		
		if (currentPageIndex >= getNumPages()) {
			currentPageIndex = getNumPages() - 1;
		}
		updateVisibleRows();
	}
	
	private void updateVisibleRows() {
		visibleRows.clear();	
		int pageOffset = currentPageIndex * MAX_ROWS_PER_PAGE;
		for (int i = 0; i < getDataRowCount(); i ++) {
			visibleRows.add(allRows.get(i + pageOffset));
		}
		fireTableDataChanged();	
	}

	public List<Issue> getIssues(int row, int col) {
		issueList.clear();
		Object rowAt = getValueAt(row);
		if (rowAt != null && issueManager != null) {
			List<Issue> allIssueList = issueManager.getIssueList();
			for (Issue issue: allIssueList) {
				Object source = issue.getSource();
				if (issue.getSeverity() == Issue.SEVERITY_ERROR) {
					if (rowAt == source || 
							source instanceof OutputFunctionIssueSource && ((OutputFunctionIssueSource)source).getAnnotatedFunction() == rowAt) {
						issueList.add(issue);
					}
				}
			}
		}
		return issueList;
	}
	
	public final void setIssueManager(IssueManager newValue) {
		if (newValue == issueManager) {
			return;
		}
		IssueManager oldValue = this.issueManager;
		if (oldValue != null) {
			oldValue.removeIssueEventListener(this);
		}
		this.issueManager = newValue;
		if (newValue != null) {
			newValue.addIssueEventListener(this);		
		}
		issueManagerChange(oldValue, newValue);
	}

	void issueManagerChange(IssueManager oldValue, IssueManager newValue) {}
	
	public void setTableSelections(Object[] selectedObjects, JTable table) {
		if (table != ownerTable || table.getModel() != this) {
			throw new RuntimeException("VCellSortTableModel.setTableSelections(), wrong table");
		}
		if (selectedObjects == null || selectedObjects.length == 0) {
			table.clearSelection();
			return;
		}
		int firstObjectRowIndex = -1;
		for (int i = 0; i < allRows.size(); i ++) {
			if (allRows.get(i) == selectedObjects[0]) {
				firstObjectRowIndex = i;
				break;
			}
		}
		if (firstObjectRowIndex < 0) {
			return;
		}
		currentPageIndex = firstObjectRowIndex / MAX_ROWS_PER_PAGE;
		updateVisibleRows();
		
		Set<Integer> oldSelectionSet = new HashSet<Integer>();
		for (int row : table.getSelectedRows()) {
			oldSelectionSet.add(row);
		}
		Set<Integer> newSelectionSet = new HashSet<Integer>();
		for (Object object : selectedObjects) {
			for (int i = 0; i < getRowCount(); i ++) {
				if (getValueAt(i) == object) {
					newSelectionSet.add(i);
					break;
				}
			}
		}
		
		Set<Integer> removeSet = new HashSet<Integer>(oldSelectionSet);
		removeSet.removeAll(newSelectionSet);
		Set<Integer> addSet = new HashSet<Integer>(newSelectionSet);
		addSet.removeAll(oldSelectionSet);
		for (int row : removeSet) {
			table.removeRowSelectionInterval(row, row);
		}
		for (int row : addSet) {
			table.addRowSelectionInterval(row, row);
		}
		if (removeSet.size() > 0 || addSet.size() > 0) {
			Rectangle r = table.getCellRect(table.getSelectedRow(), 0, true);
			table.scrollRectToVisible(r);
		}
	}
}
