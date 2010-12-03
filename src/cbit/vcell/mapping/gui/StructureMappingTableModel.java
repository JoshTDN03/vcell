package cbit.vcell.mapping.gui;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyVetoException;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.vcell.sbml.vcell.StructureSizeSolver;

import cbit.vcell.client.PopupGenerator;
import cbit.vcell.document.GeometryOwner;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.GeometryClass;
import cbit.vcell.geometry.SubVolume;
import cbit.vcell.mapping.FeatureMapping;
import cbit.vcell.mapping.GeometryContext;
import cbit.vcell.mapping.IllegalMappingException;
import cbit.vcell.mapping.MappingException;
import cbit.vcell.mapping.MembraneMapping;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.math.BoundaryConditionType;
import cbit.vcell.model.Feature;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Parameter;
import cbit.vcell.model.Structure;
import cbit.vcell.parser.DivideByZeroException;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.units.VCUnitDefinition;
/**
 * Insert the type's description here.
 * Creation date: (2/23/01 10:52:36 PM)
 * @author: 
 */
@SuppressWarnings("serial")
public class StructureMappingTableModel extends javax.swing.table.AbstractTableModel implements java.beans.PropertyChangeListener {
	private static final String PROPERTY_GEOMETRY_CONTEXT = "geometryContext";
	
	public final static int SPATIAL_COLUMN_STRUCTURE = 0;
	public final static int SPATIAL_COLUMN_SUBDOMAIN = 1;
	public final static int SPATIAL_COLUMN_SIZERATIO = 2;
	public final static int SPATIAL_COLUMN_X_MINUS = 3;
	public final static int SPATIAL_COLUMN_X_PLUS = 4;
	public final static int SPATIAL_COLUMN_Y_MINUS = 5;
	public final static int SPATIAL_COLUMN_Y_PLUS = 6;
	public final static int SPATIAL_COLUMN_Z_MINUS = 7;
	public final static int SPATIAL_COLUMN_Z_PLUS = 8;
		
	public final static String SPATIAL_LABEL_STRUCTURE = "Structure";
	public final static String SPATIAL_LABEL_SUBDOMAIN = "Subdomain";
	public final static String SPATIAL_LABEL_SIZERATIO = "Size Ratio(Structure : Subdomain)";
	public final static String SPATIAL_LABEL_X_MINUS = "X-";
	public final static String SPATIAL_LABEL_X_PLUS = "X+";
	public final static String SPATIAL_LABEL_Y_MINUS = "Y-";
	public final static String SPATIAL_LABEL_Y_PLUS = "Y+";
	public final static String SPATIAL_LABEL_Z_MINUS = "Z-";
	public final static String SPATIAL_LABEL_Z_PLUS = "Z+";
	
	public final static int NONSPATIAL_COLUMN_STRUCTURE = 0;
	public final static int NONSPATIAL_COLUMN_SIZE = 1;
	public final static int NONSPATIAL_COLUMN_SURFVOL = 2;
	public final static int NONSPATIAL_COLUMN_VOLFRACT = 3;

	public final static String NONSPATIAL_LABEL_STRUCTURE = "Structure";
	public final static String NONSPATIAL_LABEL_SIZE = "Size";
	public final static String NONSPATIAL_LABEL_SURFVOL = "Surface : Volume";
	public final static String NONSPATIAL_LABEL_VOLFRACT = "Volume : Volume";
	
	public final static String[] SPATIAL_COLUMN_TOOLTIPS = {
		null,
		null,
		null,
		"<html>boundary condition type for X- boundary <ul><li>flux: specify flux at boundary</li><li>value: specify concentration at boundary</li></ul><html>", // 7
		"<html>boundary condition type for X+ boundary <ul><li>flux: specify flux at boundary</li><li>value: specify concentration at boundary</li></ul><html>", // 8
		"<html>boundary condition type for Y- boundary <ul><li>flux: specify flux at boundary</li><li>value: specify concentration at boundary</li></ul><html>", // 9
		"<html>boundary condition type for Y+ boundary <ul><li>flux: specify flux at boundary</li><li>value: specify concentration at boundary</li></ul><html>", // 10
		"<html>boundary condition type for Z- boundary <ul><li>flux: specify flux at boundary</li><li>value: specify concentration at boundary</li></ul><html>", // 11
		"<html>boundary condition type for Z+ boundary <ul><li>flux: specify flux at boundary</li><li>value: specify concentration at boundary</li></ul><html>" // 12
	};
	
	public final static String[] NONSPATIAL_COLUMN_TOOLTIPS = {
		null,
		"<html>The volume of a compartment which does <b>NOT</b> include the volumes of <br> any other compartments residing within that compartment OR the area of a membrane surface</html>",
		"ratio of membrane area to total enclosed volume",
		"ratio of total enclosed volume to parent's total enclosed volume",
	};
		
	private final String NONSPATIAL_LABELS[] = { NONSPATIAL_LABEL_STRUCTURE, NONSPATIAL_LABEL_SIZE, NONSPATIAL_LABEL_SURFVOL, NONSPATIAL_LABEL_VOLFRACT};
	private final String SPATIAL_LABELS[] = { SPATIAL_LABEL_STRUCTURE, SPATIAL_LABEL_SUBDOMAIN, SPATIAL_LABEL_SIZERATIO, 
			SPATIAL_LABEL_X_MINUS, SPATIAL_LABEL_X_PLUS, SPATIAL_LABEL_Y_MINUS, SPATIAL_LABEL_Y_PLUS, SPATIAL_LABEL_Z_MINUS, SPATIAL_LABEL_Z_PLUS };
	
	protected transient java.beans.PropertyChangeSupport propertyChange;
	private GeometryContext fieldGeometryContext = null;
	private JTable ownerTable = null;
	private boolean bNonSpatial = true;

/**
 * StructureMappingTableModel constructor comment.
 */
public StructureMappingTableModel(JTable table) {
	super();
	ownerTable = table;
	bNonSpatial = true;
	addPropertyChangeListener(this);
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


/**
 * Insert the method's description here.
 * Creation date: (2/24/01 12:24:35 AM)
 * @return java.lang.Class
 * @param column int
 */
public Class<?> getColumnClass(int column) {
	if (bNonSpatial) {
		switch (column){
			case NONSPATIAL_COLUMN_STRUCTURE:{
				return Structure.class;
			}
			case NONSPATIAL_COLUMN_SIZE:{
				return Double.class;
			}
			case NONSPATIAL_COLUMN_SURFVOL:
			case NONSPATIAL_COLUMN_VOLFRACT: {
				return Double.class;
			}
		}
	} else {
		switch (column){
			case SPATIAL_COLUMN_STRUCTURE:{
				return Structure.class;
			}
			case SPATIAL_COLUMN_SUBDOMAIN:{
				return GeometryClass.class;
			}
			case SPATIAL_COLUMN_SIZERATIO:{
				return Double.class;
			}
			case SPATIAL_COLUMN_X_MINUS:
			case SPATIAL_COLUMN_X_PLUS:
			case SPATIAL_COLUMN_Y_MINUS:
			case SPATIAL_COLUMN_Y_PLUS:
			case SPATIAL_COLUMN_Z_MINUS:
			case SPATIAL_COLUMN_Z_PLUS: {
				return BoundaryConditionType.class;
			}
		}
	}
	return Object.class;
}


/**
 * getColumnCount method comment.
 */
public int getColumnCount() {
	if (getGeometryContext() == null) {
		return 0;
	}
	if (bNonSpatial) {
		StructureMapping[] sms = getGeometryContext().getStructureMappings();
		boolean bHasOldSizeRatio = false;
		for (StructureMapping sm : sms) {
			Parameter volFrac = sm.getParameterFromRole(StructureMapping.ROLE_VolumeFraction);
			if (volFrac != null && volFrac.getExpression() != null) {
				bHasOldSizeRatio = true;
				break;
			}
			Parameter surfVolFrac = sm.getParameterFromRole(StructureMapping.ROLE_SurfaceToVolumeRatio);
			if (surfVolFrac != null && surfVolFrac.getExpression() != null) {
				bHasOldSizeRatio = true;
				break;
			}
		}		
		return bHasOldSizeRatio ? NONSPATIAL_LABELS.length : NONSPATIAL_LABELS.length - 2;
	} else {
		int dimension = getGeometryContext().getGeometry().getDimension();
		int count = SPATIAL_LABELS.length;
		if (dimension == 1) {
			return count - 4;
		} 
		if (dimension == 2) {
			return count - 2;
		}
		return count;
	}
}


public String getColumnName(int column) {
	if (column<0 || column>=getColumnCount()){
		throw new RuntimeException("StructureMappingTableModel.getColumnName(), column = "+column+" out of range ["+0+","+(getColumnCount()-1)+"]");
	}
	return bNonSpatial ? NONSPATIAL_LABELS[column] : SPATIAL_LABELS[column];
}


/**
 * Gets the geometryContext property (cbit.vcell.mapping.GeometryContext) value.
 * @return The geometryContext property value.
 * @see #setGeometryContext
 */
public GeometryContext getGeometryContext() {
	return fieldGeometryContext;
}


/**
 * Accessor for the propertyChange field.
 */
protected java.beans.PropertyChangeSupport getPropertyChange() {
	if (propertyChange == null) {
		propertyChange = new java.beans.PropertyChangeSupport(this);
	};
	return propertyChange;
}


/**
 * getRowCount method comment.
 */
public int getRowCount() {
	if (getGeometryContext()==null){
		return 0;
	}else{
		StructureMapping structureMappings[] = getGeometryContext().getStructureMappings();
		return structureMappings.length;
	}
}


/**
 * getValueAt method comment.
 */
public Object getValueAt(int row, int col) {
	if (row<0 || row>=getRowCount()){
		throw new RuntimeException("StructureMappingTableModel.getValueAt(), row = "+row+" out of range ["+0+","+(getRowCount()-1)+"]");
	}
	if (col<0 || col>=getColumnCount()){
		throw new RuntimeException("StructureMappingTableModel.getValueAt(), column = "+col+" out of range ["+0+","+(getColumnCount()-1)+"]");
	}

	if (getGeometryContext() == null){
		return null;
	}
	StructureMapping structureMapping = getGeometryContext().getStructureMapping(row);
	if (bNonSpatial) {
		switch (col){
			case NONSPATIAL_COLUMN_STRUCTURE:{
				if (structureMapping.getStructure()!=null){
					return structureMapping.getStructure();
				}else{
					return null;
				}
			}
			case NONSPATIAL_COLUMN_SIZE:{
				try{
					Expression sizeExpr = structureMapping.getSizeParameter().getExpression();
					if (sizeExpr != null) {
						return sizeExpr.evaluateConstant();
					}
				} catch(ExpressionException e){
					e.printStackTrace(System.out);
				}
				return null;
			}
			case NONSPATIAL_COLUMN_SURFVOL:{
				if (structureMapping instanceof MembraneMapping){
					MembraneMapping membraneMapping = (MembraneMapping)structureMapping;
					if(membraneMapping.getSurfaceToVolumeParameter().getExpression() != null)
					{
						try{
							return membraneMapping.getSurfaceToVolumeParameter().getExpression().evaluateConstant();
						}catch(ExpressionException e){
							e.printStackTrace(System.out);
						}
					}
				}
				return null;
			}
			case NONSPATIAL_COLUMN_VOLFRACT:{
				if (structureMapping instanceof FeatureMapping){
					FeatureMapping featureMapping = (FeatureMapping)structureMapping;
					if (featureMapping.getFeature()!=null && featureMapping.getFeature().getMembrane()!=null){
						Membrane membrane = featureMapping.getFeature().getMembrane();
						MembraneMapping membraneMapping = (MembraneMapping)getGeometryContext().getStructureMapping(membrane);
						if(membraneMapping.getVolumeFractionParameter().getExpression() != null)
						{
							try{
								return membraneMapping.getVolumeFractionParameter().getExpression().evaluateConstant();
							}catch(ExpressionException e){
								e.printStackTrace(System.out);
							}
						}						
					}
				}				
				return null;
			}			
		}
	} else {
		switch (col){
			case SPATIAL_COLUMN_STRUCTURE:{
				if (structureMapping.getStructure()!=null){
					return structureMapping.getStructure();
				}else{
					return null;
				}
			}
			case SPATIAL_COLUMN_SUBDOMAIN:{
				return structureMapping.getGeometryClass();
			}
			case SPATIAL_COLUMN_SIZERATIO:{
				try {
					if (structureMapping.getUnitSizeParameter()!=null && structureMapping.getUnitSizeParameter().getExpression()!=null){
						return structureMapping.getUnitSizeParameter().getExpression().evaluateConstant();
					}else{
						return null;
					}
				} catch (DivideByZeroException e) {
					e.printStackTrace();
					return -1;
				} catch (ExpressionException e) {
					e.printStackTrace();
					return -1;
				}
			}
			case SPATIAL_COLUMN_X_MINUS:{
				if(structureMapping.getBoundaryConditionTypeXm() != null){
					return structureMapping.getBoundaryConditionTypeXm();
				}else{
					return null;
				}
			}
			case SPATIAL_COLUMN_X_PLUS:{
				if(structureMapping.getBoundaryConditionTypeXp() != null){
					return structureMapping.getBoundaryConditionTypeXp();
				}else{
					return null;
				}
			}
			case SPATIAL_COLUMN_Y_MINUS:{
				if(structureMapping.getBoundaryConditionTypeYm() != null){
					return structureMapping.getBoundaryConditionTypeYm();
				}else{
					return null;
				}
			}
			case SPATIAL_COLUMN_Y_PLUS:{
				if(structureMapping.getBoundaryConditionTypeYp() != null){
					return structureMapping.getBoundaryConditionTypeYp();
				}else{
					return null;
				}
			}
			case SPATIAL_COLUMN_Z_MINUS:{
				if(structureMapping.getBoundaryConditionTypeZm() != null){
					return structureMapping.getBoundaryConditionTypeZm();
				}else{
					return null;
				}
			}
			case SPATIAL_COLUMN_Z_PLUS:{
				if(structureMapping.getBoundaryConditionTypeZp() != null){
					return structureMapping.getBoundaryConditionTypeZp();
				}else{
					return null;
				}
			}			
		} 
	}
	return null;
}


/**
 * The hasListeners method was generated to support the propertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getPropertyChange().hasListeners(propertyName);
}


/**
 * Insert the method's description here.
 * Creation date: (2/24/01 12:27:46 AM)
 * @return boolean
 * @param rowIndex int
 * @param columnIndex int
 */
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (getGeometryContext()==null){
		return false;
	}
	
	StructureMapping sm = getGeometryContext().getStructureMapping(rowIndex);
	if (bNonSpatial) {
		if (columnIndex == NONSPATIAL_COLUMN_SIZE){ // feature size are editable  
			return true;
		}
		return false;
	} else {
		//
		// see if feature is distributed and has a membrane (not top)
		//		
		if (columnIndex == SPATIAL_COLUMN_SUBDOMAIN) {
			return (sm instanceof FeatureMapping);
		}
		if (columnIndex == SPATIAL_COLUMN_SIZERATIO){
			GeometryClass gc = sm.getGeometryClass();
			StructureMapping[] structureMappings = getGeometryContext().getStructureMappings(gc);
			return structureMappings != null && structureMappings.length > 1;
		}
		// bounday conditions are editable
		if ((columnIndex >= SPATIAL_COLUMN_X_MINUS) && (columnIndex <= SPATIAL_COLUMN_Z_PLUS))
			return true;
	}
	return false;
}

private void updateSubdomainComboBox() {
	GeometryClass[] geometryClasses = getGeometryContext().getGeometry().getGeometryClasses();
	DefaultComboBoxModel aModel = new DefaultComboBoxModel();
	for (GeometryClass gc : geometryClasses) {
		aModel.addElement(gc);
	}
	JComboBox subdomainComboBoxCellEditor = new JComboBox();
	subdomainComboBoxCellEditor.setRenderer(new DefaultListCellRenderer() {
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setHorizontalTextPosition(SwingConstants.LEFT);
			if (value instanceof GeometryClass) {
				GeometryClass gc = (GeometryClass)value;
				setText(gc.getName());
//				setIcon(gc instanceof SubVolume ? StructureMappingTableRenderer.volumeIcon : StructureMappingTableRenderer.surfaceIcon);
			}
			return this;
		}
	});
	subdomainComboBoxCellEditor.setModel(aModel);
	ownerTable.getColumnModel().getColumn(SPATIAL_COLUMN_SUBDOMAIN).setCellEditor(new DefaultCellEditor(subdomainComboBoxCellEditor));
}

private void update() {
//	AsynchClientTask task1 = new AsynchClientTask("update geometry", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
//		
//		@Override
//		public void run(Hashtable<String, Object> hashTable) throws Exception {
//			getGeometryContext().getGeometry().precomputeAll();
//		}
//	};
//	AsynchClientTask task2 = new AsynchClientTask("update", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
//		
//		@Override
//		public void run(Hashtable<String, Object> hashTable) throws Exception {
			int dimension = getGeometryContext().getGeometry().getDimension();
			bNonSpatial = (dimension == 0);
			fireTableStructureChanged();
			
			if (!bNonSpatial) {
				class StructureMappingTableHeaderRenderer implements TableCellRenderer {
					TableCellRenderer defaultRenderer = null;
					public StructureMappingTableHeaderRenderer(TableCellRenderer dr) {
						defaultRenderer = dr;
					}
					public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
						c.setPreferredSize(new Dimension(20,45));
						JLabel label = null;
						if (c instanceof JLabel) {
							label = (JLabel)c;
						} else {
							label = new JLabel();
							Border border = null;
							if (hasFocus) {
								border = UIManager.getBorder("TableHeader.focusCellBorder");
							}
							if (border == null) {
								border = UIManager.getBorder("TableHeader.cellBorder");
							}							
							if (table != null) { 
								JTableHeader header = table.getTableHeader(); 
								if (header != null) { 
									Color fgColor = null;
									Color bgColor = null;
									if (hasFocus) {
										fgColor = UIManager.getColor("TableHeader.focusCellForeground");
										bgColor = UIManager.getColor("TableHeader.focusCellBackground");
									}
									if (fgColor == null) {
										fgColor = header.getForeground();
									}
									if (bgColor == null) {
										bgColor = header.getBackground();
									}
									label.setForeground(fgColor);
									label.setBackground(bgColor);
									label.setFont(header.getFont()); 
								} 
							} 
						}
						if (column == SPATIAL_COLUMN_SIZERATIO) {
							label.setHorizontalTextPosition(JLabel.CENTER);
							label.setVerticalTextPosition(JLabel.TOP);
							label.setText("Size Ratio");
							label.setIconTextGap(0);
							final JLabel finalLabel = label;
							label.setIcon(new StructureMappingTableRenderer.TextIcon("(Structure : Subdomain)"){

								@Override
								public int getIconWidth() {
									return finalLabel.getWidth();
								}
							});
						} else {
							label.setText(value.toString());
							label.setIcon(null);
						}
						return label;
					}
				}
				
				ownerTable.getTableHeader().setDefaultRenderer(new StructureMappingTableHeaderRenderer(ownerTable.getTableHeader().getDefaultRenderer()));				
				ownerTable.getColumnModel().getColumn(SPATIAL_COLUMN_SIZERATIO).setPreferredWidth(100);
				for (int col = SPATIAL_COLUMN_X_MINUS; col < getColumnCount(); col ++) {
					ownerTable.getColumnModel().getColumn(col).setPreferredWidth(8);
				}
				updateSubdomainComboBox();
			}		
			
//		}
//	};
//	ClientTaskDispatcher.dispatch(ownerTable, new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2});
}
/**
	 * This method gets called when a bound property is changed.
	 * @param evt A PropertyChangeEvent object describing the event source 
	 *   and the property that has changed.
	 */
public void propertyChange(java.beans.PropertyChangeEvent evt) {
	if (evt.getSource() == this && evt.getPropertyName().equals(PROPERTY_GEOMETRY_CONTEXT)) {		
		GeometryContext oldValue = (GeometryContext)evt.getOldValue();
		if (oldValue != null){
			oldValue.removePropertyChangeListener(this);
			StructureMapping oldStructureMappings[] = oldValue.getStructureMappings();
			for (int i=0;i<oldStructureMappings.length;i++){
				oldStructureMappings[i].removePropertyChangeListener(this);
			}
			SubVolume[] subvols = oldValue.getGeometry().getGeometrySpec().getSubVolumes();
			for (int i = 0; i < subvols.length; i++) {
				subvols[i].removePropertyChangeListener(this);
			}
		}
		GeometryContext newValue = (GeometryContext)evt.getNewValue();
		if (newValue!=null){
			newValue.addPropertyChangeListener(this);
			StructureMapping newStructureMappings[] = newValue.getStructureMappings();
			for (int i=0;i<newStructureMappings.length;i++){
				newStructureMappings[i].addPropertyChangeListener(this);
			}
			SubVolume[] subvols = newValue.getGeometry().getGeometrySpec().getSubVolumes();
			for (int i = 0; i < subvols.length; i++) {
				subvols[i].addPropertyChangeListener(this);
			}
		}
		update();
	}
	if (evt.getSource() == getGeometryContext() && evt.getPropertyName().equals(GeometryOwner.PROPERTY_NAME_GEOMETRY)) {
		SubVolume[] subvols = ((Geometry)evt.getOldValue()).getGeometrySpec().getSubVolumes();
		for (int i = 0; i < subvols.length; i++) {
			subvols[i].removePropertyChangeListener(this);
		}
		subvols = ((Geometry)evt.getNewValue()).getGeometrySpec().getSubVolumes();
		for (int i = 0; i < subvols.length; i++) {
			subvols[i].addPropertyChangeListener(this);
		}		
		update();
	}
	// subvolume name change
	if (evt.getSource() instanceof SubVolume) {
		update();
	}
	if (evt.getSource() instanceof GeometryContext
		&& evt.getPropertyName().equals(GeometryContext.PROPERTY_STRUCTURE_MAPPINGS)) {
		StructureMapping[] oldStructureMappings = (StructureMapping[])evt.getOldValue();
		StructureMapping[] newStructureMappings = (StructureMapping[])evt.getNewValue();
		for (int i=0;oldStructureMappings!=null && i<oldStructureMappings.length;i++){
			oldStructureMappings[i].removePropertyChangeListener(this);
		}
		for (int i=0;newStructureMappings!=null && i<newStructureMappings.length;i++){
			newStructureMappings[i].addPropertyChangeListener(this);
		}
		fireTableDataChanged();
	}
	if (evt.getSource() instanceof StructureMapping) {
		fireTableDataChanged();
	}
}


/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}


/**
 * Sets the geometryContext property (cbit.vcell.mapping.GeometryContext) value.
 * @param geometryContext The new value for the property.
 * @see #getGeometryContext
 */
public void setGeometryContext(GeometryContext geometryContext) {
	GeometryContext oldValue = fieldGeometryContext;
	fieldGeometryContext = geometryContext;
	firePropertyChange(PROPERTY_GEOMETRY_CONTEXT, oldValue, geometryContext);
}


public void setValueAt(Object aValue, int rowIndex, int columnIndex){
	if (rowIndex<0 || rowIndex>=getRowCount()){
		throw new RuntimeException("StructureMappingTableModel.setValueAt(), row = "+rowIndex+" out of range ["+0+","+(getRowCount()-1)+"]");
	}
	if (columnIndex<0 || columnIndex>=getColumnCount()){
		throw new RuntimeException("StructureMappingTableModel.setValueAt(), column = "+columnIndex+" out of range ["+0+","+(getColumnCount()-1)+"]");
	}	
	StructureMapping structureMapping = getGeometryContext().getStructureMapping(rowIndex);
	Structure structure = structureMapping.getStructure();
	if (bNonSpatial) {
		switch (columnIndex){
			case NONSPATIAL_COLUMN_SIZE:{
				try {
					Expression exp = null;
					if (aValue instanceof String){
						exp = new Expression((String)aValue);
					} else if (aValue instanceof Double){
						exp = new Expression(((Double)aValue).doubleValue());
					}
					//if the input volumn is null, leave it as it was.
					if(exp != null)
					{
						//for old ode model, once one size is input, solve the rest.                                                                                                          if it is unnamed compartment(the only one), we don't need to solve anything
						if(!getGeometryContext().getSimulationContext().isStoch() 
								&& getGeometryContext().isAllSizeSpecifiedNull() 
								&& getGeometryContext().isAllVolFracAndSurfVolSpecified() 
								&& getGeometryContext().getStructureMappings().length > 1) 
						{
							structureMapping.getSizeParameter().setExpression(exp);
							double size;
							try{
								size = exp.evaluateConstant();
								StructureSizeSolver.updateAbsoluteStructureSizes(getGeometryContext().getSimulationContext(), 
										structure, size, VCUnitDefinition.UNIT_um3);
								fireTableRowsUpdated(0,getRowCount());
							}catch(ExpressionException ex){
								ex.printStackTrace(System.out);
								PopupGenerator.showErrorDialog(ownerTable, "Size of Feature " + structure.getName() + " can not be solved as constant!");
							} catch (Exception ex) {
								ex.printStackTrace(System.out);
								PopupGenerator.showErrorDialog(ownerTable, ex.getMessage());
							}
						}
						else 
						{					
							structureMapping.getSizeParameter().setExpression(exp);
							//solve relative structure sizes(surface volume ratio, volume fraction) for non-stochastic applications
							//amended Sept. 27th, 2007
							//set fraction in stoch math description, because these might be used when copy from stoch app to ode app.
							if(getGeometryContext().isAllSizeSpecifiedPositive()/*&& !getGeometryContext().getSimulationContext().isStoch()*/) 
							{
								try {
									StructureSizeSolver.updateRelativeStructureSizes(getGeometryContext().getSimulationContext());
								} catch (Exception ex) {
									ex.printStackTrace(System.out);
									PopupGenerator.showErrorDialog(ownerTable, ex.getMessage());
								}
							}							
						}
					}
					fireTableDataChanged();
				}catch (ExpressionException e){
					e.printStackTrace(System.out);
					PopupGenerator.showErrorDialog(ownerTable, "expression error\n"+e.getMessage());
				}catch (java.beans.PropertyVetoException e){
					e.printStackTrace(System.out);
					PopupGenerator.showErrorDialog(ownerTable, "expression error\n"+e.getMessage());
				}
				break;
			}			
		}
	} else {
		switch (columnIndex){
			case SPATIAL_COLUMN_SUBDOMAIN: {
				GeometryClass geometryClass = null;
				if (aValue instanceof String) {
					String svname = (String)aValue;
					geometryClass = getGeometryContext().getGeometry().getGeometryClass(svname);
				} else if (aValue instanceof GeometryClass) {
					geometryClass = (GeometryClass)aValue;
				}
				if (geometryClass!=null && (structure instanceof Feature)){
					try {
						getGeometryContext().assignFeature((Feature)structure, geometryClass);
					} catch (PropertyVetoException e) {
						e.printStackTrace(System.out);
						PopupGenerator.showErrorDialog(ownerTable, e.getMessage());
					} catch (IllegalMappingException e) {
						e.printStackTrace(System.out);
						PopupGenerator.showErrorDialog(ownerTable, e.getMessage());
					} catch (MappingException e) {
						e.printStackTrace(System.out);
						PopupGenerator.showErrorDialog(ownerTable, e.getMessage());
					}
				}
				break;
			}
			case SPATIAL_COLUMN_SIZERATIO:	
				try {
					Expression exp = null;
					if (aValue instanceof String){
						exp = new Expression((String)aValue);
					} else if (aValue instanceof Double){
						exp = new Expression(((Double)aValue).doubleValue());
					}
					if(exp != null)
					{
						structureMapping.getUnitSizeParameter().setExpression(exp);
						StructureSizeSolver.updateUnitStructureSizes(getGeometryContext().getSimulationContext(), structureMapping.getGeometryClass());
					}
				}catch (ExpressionException e){
					e.printStackTrace(System.out);
					PopupGenerator.showErrorDialog(ownerTable, "expression error\n"+e.getMessage());
				}catch (java.beans.PropertyVetoException e){
					e.printStackTrace(System.out);
					PopupGenerator.showErrorDialog(ownerTable, "expression error\n"+e.getMessage());
				}				
				fireTableDataChanged();
				break;
			case SPATIAL_COLUMN_X_MINUS:{
				if (aValue != null) {
					structureMapping.setBoundaryConditionTypeXm(new BoundaryConditionType((String)aValue));
				}
				break;
			}
			case SPATIAL_COLUMN_X_PLUS:{
				if (aValue != null) {
					structureMapping.setBoundaryConditionTypeXp(new BoundaryConditionType((String)aValue));
				}
				break;
			}
			case SPATIAL_COLUMN_Y_MINUS:{
				if(aValue != null) {
					structureMapping.setBoundaryConditionTypeYm(new BoundaryConditionType((String)aValue));
				}
				break;
			}
			case SPATIAL_COLUMN_Y_PLUS:{
				if(aValue != null) {
					structureMapping.setBoundaryConditionTypeYp(new BoundaryConditionType((String)aValue));
				}
				break;
			}
			case SPATIAL_COLUMN_Z_MINUS:{
				if(aValue != null) {
					structureMapping.setBoundaryConditionTypeZm(new BoundaryConditionType((String)aValue));
				}
				break;
			}
			case SPATIAL_COLUMN_Z_PLUS:{
				if(aValue != null) {
					structureMapping.setBoundaryConditionTypeZp(new BoundaryConditionType((String)aValue));
				}
				break;
			}
		}
	}
}

public String getToolTip(int row, int column) {	
	if (!bNonSpatial) {
		StructureMapping structureMapping = getGeometryContext().getStructureMapping(row);
		if (column == SPATIAL_COLUMN_SIZERATIO) {
			if (structureMapping.getUnitSizeParameter()!=null && structureMapping.getUnitSizeParameter().getExpression()!=null){
				return structureMapping.getUnitSizeParameter().getDescription();
			}
		}
		return SPATIAL_COLUMN_TOOLTIPS[column];
	}
	return NONSPATIAL_COLUMN_TOOLTIPS[column];
	
}

public boolean isSubdomainColumn(int column) {
	return (!bNonSpatial && column == SPATIAL_COLUMN_SUBDOMAIN);
}

public boolean isNewSizeColumn(int column) {
	return (bNonSpatial && column == NONSPATIAL_COLUMN_SIZE || !bNonSpatial && column == SPATIAL_COLUMN_SIZERATIO);
}

public StructureMapping getStructureMapping(int row) {
	return getGeometryContext().getStructureMapping(row);
}

public boolean isNonSpatial() {
	return bNonSpatial;
}
}