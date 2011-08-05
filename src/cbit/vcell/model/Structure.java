/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.model;

import cbit.vcell.biomodel.meta.Identifiable;
import cbit.vcell.field.FieldFunctionDefinition;
import cbit.vcell.parser.ExpressionBindingException;
import java.beans.*;
import java.io.Serializable;
import java.util.*;

import org.vcell.util.Cacheable;
import org.vcell.util.Matchable;
import org.vcell.util.TokenMangler;
import org.vcell.util.document.KeyValue;

import cbit.vcell.parser.NameScope;
import cbit.vcell.parser.ScopedSymbolTable;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.units.VCUnitDefinition;

@SuppressWarnings("serial")
public abstract class Structure implements Serializable, ScopedSymbolTable, Matchable, Cacheable, VetoableChangeListener,
		Identifiable
{
	public final static String TYPE_NAME_FEATURE = "Compartment";
	public final static String TYPE_NAME_MEMBRANE = "Membrane";
	
	private String fieldName = new String();
	protected transient java.beans.VetoableChangeSupport vetoPropertyChange;
	protected transient java.beans.PropertyChangeSupport propertyChange;
	private KeyValue fieldKey = null;
	private StructureNameScope fieldNameScope = new Structure.StructureNameScope();
	private transient Model fieldModel = null;
	private StructureSize fieldStructureSize = null;
	

	public class StructureNameScope extends BioNameScope {
		private NameScope children[] = new NameScope[0];
		
		public StructureNameScope(){
			super();
		}
		public NameScope[] getChildren() {
			//
			// no children to return
			//
			return children;
		}
		public String getName() {
			return TokenMangler.fixTokenStrict(Structure.this.getName());
		}
		public NameScope getParent() {
			if (Structure.this.fieldModel != null){
				return Structure.this.fieldModel.getNameScope();
			}else{
				return null;
			}
		}
		public ScopedSymbolTable getScopedSymbolTable() {
			return Structure.this;
		}
	}

	public class StructureSize extends ModelQuantity {

		@Override
		public String getDescription() {
			return "structure size";
		}

		public StructureSize(String name) {
			super(name);
		}

		public NameScope getNameScope() {
			return Structure.this.getNameScope();
		}
		
		public Structure getStructure(){
			return Structure.this;
		}

		public VCUnitDefinition getUnitDefinition() {
			switch (getDimension()){
				case 0: {
					return VCUnitDefinition.UNIT_DIMENSIONLESS;
				}
				case 1: {
					return VCUnitDefinition.UNIT_um;
				}
				case 2: {
					return VCUnitDefinition.UNIT_um2;
				}
				case 3: {
					return VCUnitDefinition.UNIT_um3;
				}
				default:{
					throw new RuntimeException("unexpected structure dimension: "+getDimension());
				}
			}
		}

		public boolean isUnitEditable() {
			return false;
		}

		public void setUnitDefinition(VCUnitDefinition unit) {
			throw new RuntimeException("cannot set units on structure sizes, structure '"+getStructure().getName()+"' is in units of "+getUnitDefinition().getSymbol());
		}
	}
	
protected Structure(KeyValue key){
	this.fieldKey = key;
	addVetoableChangeListener(this);
}      

public StructureSize getStructureSize(){
	if (fieldStructureSize == null){
		fieldStructureSize = new StructureSize(getDefaultStructureSizeName(fieldName));
	}
	return fieldStructureSize;
}

public static String getDefaultStructureSizeName(String structureName){
	return TokenMangler.fixTokenStrict(structureName);
}


/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}
/**
 * The addVetoableChangeListener method was generated to support the vetoPropertyChange field.
 */
public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener listener) {
	getVetoPropertyChange().addVetoableChangeListener(listener);
}
/**
 * This method was created in VisualAge.
 * @return boolean
 * @param structure cbit.vcell.model.Structure
 */
protected boolean compareEqual0(Structure s) {
	if (s == null){
		return false;
	}

	if (!getName().equals(s.getName())){
		return false;
	}
	return true;
}
/**
 * This method was created by a SmartGuide.
 * @return boolean
 * @param structure cbit.vcell.model.Structure
 */
public abstract boolean enclosedBy(Structure parentStructure);
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * The fireVetoableChange method was generated to support the vetoPropertyChange field.
 */
public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws java.beans.PropertyVetoException {
	getVetoPropertyChange().fireVetoableChange(propertyName, oldValue, newValue);
}
/**
 * getEntry method comment.
 */
public SymbolTableEntry getEntry(java.lang.String identifierString) throws ExpressionBindingException {
	
	SymbolTableEntry ste = getLocalEntry(identifierString);
	if (ste != null){
		return ste;
	}
	return getNameScope().getExternalEntry(identifierString,this);
}
/**
 * Gets the key property (cbit.sql.KeyValue) value.
 * @return The key property value.
 * @see #setKey
 */
public KeyValue getKey() {
	return fieldKey;
}
/**
 * Insert the method's description here.
 * Creation date: (4/6/2004 9:59:19 AM)
 * @return cbit.vcell.parser.SymbolTableEntry
 * @param identifier java.lang.String
 */
public SymbolTableEntry getLocalEntry(java.lang.String identifier) throws ExpressionBindingException {
	
	SymbolTableEntry ste = ReservedBioSymbolEntries.getEntry(identifier);
	if (ste != null){
		if (ste.equals(ReservedSymbol.X) || ste.equals(ReservedSymbol.Y) || ste.equals(ReservedSymbol.Z)){
			throw new ExpressionBindingException("can't use x, y, or z, Physiological Models must be spatially independent");
		}
		if (ste.equals(FieldFunctionDefinition.fieldFunctionDefinition)){
			throw new ExpressionBindingException("can't use field functions, Physiological Models must be spatially independent");
		}
		return ste;
	}	

	if (this instanceof Membrane){
		Membrane.MembraneVoltage membraneVoltage = ((Membrane)this).getMembraneVoltage();
		if (membraneVoltage.getName().equals(identifier)){
			return membraneVoltage;
		}
	}
	
	if (getStructureSize().getName().equals(identifier)){
		return getStructureSize();
	}

	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (4/6/2004 10:09:25 AM)
 * @return cbit.vcell.model.Model
 */
Model getModel() {
	return fieldModel;
}

public abstract int getDimension();

/**
 * Gets the name property (java.lang.String) value.
 * @return The name property value.
 * @see #setName
 */
public String getName() {
	return fieldName;
}
/**
 * Insert the method's description here.
 * Creation date: (4/6/2004 9:59:19 AM)
 * @return cbit.vcell.parser.NameScope
 */
public NameScope getNameScope() {
	return fieldNameScope;
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.model.Structure
 */
public abstract Structure getParentStructure();
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
 * Get sub features inside this structure.
 * If it is a feature, the sub features include all features inside it and itself.
 * If it is a membrance, the sub features include all features inside it.
 * Creation date: (12/11/2006 5:42:07 PM)
 * @return java.util.Enumeration
 */
public Enumeration<Feature> getSubFeatures() 
{
	Vector<Feature> subFeatures = new Vector<Feature>();
	Structure[] structures = this.getModel().getStructures();
	for (int i=0; i<structures.length; i++)
	{
		if((structures[i] instanceof Feature) && (structures[i].enclosedBy(this)))
		{
			subFeatures.addElement((Feature)structures[i]);
		}
	}
	return subFeatures.elements();
}
/**
 * Accessor for the vetoPropertyChange field.
 */
protected java.beans.VetoableChangeSupport getVetoPropertyChange() {
	if (vetoPropertyChange == null) {
		vetoPropertyChange = new java.beans.VetoableChangeSupport(this);
	};
	return vetoPropertyChange;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
protected void handleException(Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	System.out.println("--------- UNCAUGHT EXCEPTION --------- in Feature");
	exception.printStackTrace(System.out);
}
/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}
/**
 * The removeVetoableChangeListener method was generated to support the vetoPropertyChange field.
 */
public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener listener) {
	getVetoPropertyChange().removeVetoableChangeListener(listener);
}
/**
 * Insert the method's description here.
 * Creation date: (4/6/2004 10:09:25 AM)
 * @param newModel cbit.vcell.model.Model
 */
void setModel(Model newModel) {
	fieldModel = newModel;
}
/**
 * Sets the name property (java.lang.String) value.
 * @param name The new value for the property.
 * @exception java.beans.PropertyVetoException The exception description.
 * @see #getName
 */
public void setName(String name) throws java.beans.PropertyVetoException {
	String oldValue = fieldName;
	fireVetoableChange("name", oldValue, name);
	fieldName = name;
	firePropertyChange("name", oldValue, name);
}
/**
 * This method was created in VisualAge.
 * @param structure cbit.vcell.model.Structure
 */
public abstract void setParentStructure(Structure structure) throws ModelException;
/**
 * This method was created in VisualAge.
 * @param e java.beans.PropertyChangeEvent
 */
public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
	TokenMangler.checkNameProperty(this, "structure", e);
}

public void getLocalEntries(Map<String, SymbolTableEntry> entryMap) {	
	entryMap.put(getStructureSize().getName(), getStructureSize());
	if (this instanceof Membrane){
		Membrane.MembraneVoltage membraneVoltage = ((Membrane)this).getMembraneVoltage();
		entryMap.put(membraneVoltage.getName(), membraneVoltage);
	}
	ReservedBioSymbolEntries.getAll(entryMap);
	entryMap.remove(ReservedSymbol.X);
	entryMap.remove(ReservedSymbol.Y);
	entryMap.remove(ReservedSymbol.Z);
	entryMap.remove(FieldFunctionDefinition.fieldFunctionDefinition);
}

public void getEntries(Map<String, SymbolTableEntry> entryMap) {
	getNameScope().getExternalEntries(entryMap);		
}

public abstract String getTypeName();
public abstract String checkNewParent(Structure structure);
}
