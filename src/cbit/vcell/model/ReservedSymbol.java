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
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionBindingException;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.NameScope;
import cbit.vcell.parser.ScopedSymbolTable;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.units.VCUnitDefinition;

public class ReservedSymbol implements EditableSymbolTableEntry, Serializable
{
   public final static ReservedSymbol TIME 	 = new ReservedSymbol("t","time",VCUnitDefinition.UNIT_s,null);
   public final static ReservedSymbol X    	 = new ReservedSymbol("x","x coord",VCUnitDefinition.UNIT_um,null);
   public final static ReservedSymbol Y    	 = new ReservedSymbol("y","y coord",VCUnitDefinition.UNIT_um,null);
   public final static ReservedSymbol Z    	 = new ReservedSymbol("z","z coord",VCUnitDefinition.UNIT_um,null);
   public final static ReservedSymbol TEMPERATURE = new ReservedSymbol("_T_","temperature",VCUnitDefinition.UNIT_K,null);
   public final static ReservedSymbol FARADAY_CONSTANT = new ReservedSymbol("_F_","Faraday const",VCUnitDefinition.UNIT_C_per_mol,new Expression(9.648e4));
   public final static ReservedSymbol FARADAY_CONSTANT_NMOLE = new ReservedSymbol("_F_nmol_","Faraday const",VCUnitDefinition.UNIT_C_per_nmol,new Expression(9.648e-5));
   public final static ReservedSymbol N_PMOLE = new ReservedSymbol("_N_pmol_","Avagadro Num (scaled)",VCUnitDefinition.UNIT_molecules_per_pmol,new Expression(6.02e11));
   public final static ReservedSymbol K_GHK = new ReservedSymbol("_K_GHK_","GHK unit scale",VCUnitDefinition.getInstance("1e9"),new Expression(1e-9));
   public final static ReservedSymbol GAS_CONSTANT = new ReservedSymbol("_R_","Gas Constant",VCUnitDefinition.UNIT_mV_C_per_K_per_mol,new Expression(8314.0));
   public final static ReservedSymbol KMILLIVOLTS = new ReservedSymbol("K_millivolts_per_volt","voltage scale",VCUnitDefinition.getInstance("1e-3"),new Expression(1000));

   public final static ReservedSymbol KMOLE;
   static {
	   ReservedSymbol temp = null;
	   try {
		   temp = new ReservedSymbol("KMOLE","Flux unit conversion",VCUnitDefinition.UNIT_uM_um3_per_molecules,Expression.div(new Expression(1.0), new Expression(602.0)));
	   } catch (Throwable e){
		   e.printStackTrace(System.out);
	   }
	   KMOLE = temp;
   }
   
   private String name = null;
   private Expression constantValue = null;
   private String description = null;
   private VCUnitDefinition unitDefinition = null;

   private final static ReservedSymbol[] otherReservedSymbols = {
	   TEMPERATURE,
	   GAS_CONSTANT,
	   FARADAY_CONSTANT,
	   FARADAY_CONSTANT_NMOLE,
	   KMOLE,
	   N_PMOLE,
	   KMILLIVOLTS,
	   K_GHK,
   };
   
   private static NameScope nameScope = new ReservedSymbol.ReservedSymbolNameScope(); 

   public static class ReservedSymbolTable implements ScopedSymbolTable {
		private boolean bIncludeTime = false;
		/**
		 * ReservedSymbolTable constructor comment.
		 */
		public ReservedSymbolTable(boolean bIncludeTime) {
			this.bIncludeTime = bIncludeTime;
		}
		/**
		 * getEntry method comment.
		 */
		public SymbolTableEntry getEntry(String identifierString) throws ExpressionBindingException {
			SymbolTableEntry ste;
			
			ste = ReservedSymbol.fromString(identifierString);
			if (ste != null){
				if (((ReservedSymbol)ste).isTIME() && !bIncludeTime){
					throw new ExpressionBindingException("expression must not be a function of time");
				}else{
					return ste;
				}
			}	
			
			throw new ExpressionBindingException("unresolved symbol <"+identifierString+">");
		}
		public void getEntries(Map<String, SymbolTableEntry> entryMap) {
			ReservedSymbol.getAll(entryMap, bIncludeTime, true);
			
		}
		public void getLocalEntries(Map<String, SymbolTableEntry> entryMap) {
			getEntries(entryMap);
			
		}
		public SymbolTableEntry getLocalEntry(String identifier) throws ExpressionBindingException {
			return getEntry(identifier);
		}
		public NameScope getNameScope() {
			return nameScope;
		}
   }
	public static class ReservedSymbolNameScope extends BioNameScope {
		private NameScope children[] = new NameScope[0];
		public ReservedSymbolNameScope(){
			super();
		}
		public NameScope[] getChildren() {
			return children;
		}
		public String getName() {
			return "ReservedSymbols";
		}
		public NameScope getParent() {
			//System.out.println("ModelNameScope.getParent() returning null ... no parent");
			return null;
		}
		public ScopedSymbolTable getScopedSymbolTable() {
			return new ReservedSymbolTable(true);
		}
		public boolean isPeer(NameScope nameScope){
			if (nameScope instanceof BioNameScope){
				return true;
			}else{
				return false;
			}
		}
	}

private ReservedSymbol(String argName, String argDescription, VCUnitDefinition argUnitDefinition, Expression argConstantValue){
	this.name = argName;
	this.unitDefinition = argUnitDefinition;
	this.constantValue = argConstantValue;
	this.description = argDescription;
}         


/**
 * Insert the method's description here.
 * Creation date: (2/19/2002 4:23:00 PM)
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean equals(Object obj) {
	if (!(obj instanceof ReservedSymbol)){
		return false;
	}
	ReservedSymbol rs = (ReservedSymbol)obj;
	if (!rs.name.equals(name)){
		return false;
	}
	return true;
}

public static void gatherLocalEntries(Set<SymbolTableEntry> symbolTableEntries){
	for (ReservedSymbol rs : otherReservedSymbols) {
		symbolTableEntries.add(rs);	
	}
	symbolTableEntries.add(TIME);
	symbolTableEntries.add(X);
	symbolTableEntries.add(Y);
	symbolTableEntries.add(Z);
}

public static void getAll(Map<String, SymbolTableEntry> entryMap, boolean bIncludeTime, boolean bIncludeSpace){
	for (ReservedSymbol rs : otherReservedSymbols) {
		entryMap.put(rs.getName(), rs);			
	}
	if (bIncludeTime) {
		entryMap.put(TIME.getName(), TIME);
	}
	if (bIncludeSpace) {
		entryMap.put(X.getName(), X);
		entryMap.put(Y.getName(), Y);
		entryMap.put(Z.getName(), Z);
	}
}

public static ReservedSymbol fromString(String symbolName) {
	if (symbolName==null){
		return null;
	}else if (symbolName.equals(TIME.getName())){
		return TIME;
	}else if (symbolName.equals(X.getName())){
		return X;
	}else if (symbolName.equals(Y.getName())){
		return Y;
	}else if (symbolName.equals(Z.getName())){
		return Z;
	}else if (symbolName.equals(TEMPERATURE.getName())){
		return TEMPERATURE;
	}else if (symbolName.equals(GAS_CONSTANT.getName())){
		return GAS_CONSTANT;
	}else if (symbolName.equals(FARADAY_CONSTANT.getName())){
		return FARADAY_CONSTANT;
	}else if (symbolName.equals(FARADAY_CONSTANT_NMOLE.getName())){
		return FARADAY_CONSTANT_NMOLE;
	}else if (symbolName.equals(KMOLE.getName())){
		return KMOLE;
	}else if (symbolName.equals(N_PMOLE.getName())){
		return N_PMOLE;
	}else if (symbolName.equals(KMILLIVOLTS.getName())){
		return KMILLIVOLTS;
	}else if (symbolName.equals(K_GHK.getName())){
		return K_GHK;
	}else{
		return null;
	}
}         


/**
 * This method was created in VisualAge.
 * @return double
 */
public double getConstantValue() throws ExpressionException {
//	if (constantValue==null){
		throw new ExpressionException(getName()+" is not constant");
//	}else{
//		return constantValue.doubleValue();
//	}
}


   public final String getDescription() 
   { 
	  return description; 
   }      


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 * @exception java.lang.Exception The exception description.
 */
public Expression getExpression() {
	return constantValue;
}


/**
 * This method was created in VisualAge.
 * @return int
 */
public int getIndex() {
	return -1;
}


   public final String getName() 
   { 
	  return name; 
   }      


/**
 * Insert the method's description here.
 * Creation date: (7/31/2003 10:29:33 AM)
 * @return cbit.vcell.parser.NameScope
 */
public NameScope getNameScope() {
	return nameScope;
}


/**
 * Insert the method's description here.
 * Creation date: (3/31/2004 2:11:57 PM)
 * @return cbit.vcell.units.VCUnitDefinition
 */
public VCUnitDefinition getUnitDefinition() {
	return unitDefinition;
}


/**
 * Insert the method's description here.
 * Creation date: (2/19/2002 4:24:46 PM)
 * @return int
 */
public int hashCode() {
	return name.hashCode();
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isConstant() {
	return false;  //constantValue!=null;
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isFARADAY_CONSTANT() {
	if (getName().equals(FARADAY_CONSTANT.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isFARADAY_CONSTANT_NMOLE() {
	if (getName().equals(FARADAY_CONSTANT_NMOLE.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isGAS_CONSTANT() {
	if (getName().equals(GAS_CONSTANT.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isK_GHK() {
	if (getName().equals(K_GHK.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isKMILLIVOLTS() {
	if (getName().equals(KMILLIVOLTS.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isKMOLE() {
	if (getName().equals(KMOLE.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isN_PMOLE() {
	if (getName().equals(N_PMOLE.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isTEMPERATURE() {
	if (getName().equals(TEMPERATURE.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isTIME() {
	if (getName().equals(TIME.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isX() {
	if (getName().equals(X.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isY() {
	if (getName().equals(Y.getName())){
		return true;
	}else{
		return false;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isZ() {
	if (getName().equals(Z.getName())){
		return true;
	}else{
		return false;
	}		
}


   public String toString()
   {
	   return getName();
   }


public boolean isDescriptionEditable() {
	return false;
}


public boolean isExpressionEditable() {
	return false;
}


public boolean isNameEditable() {
	return false;
}


public boolean isUnitEditable() {
	return false;
}


public void setDescription(String description) throws PropertyVetoException {
	throw new RuntimeException("cannot change description of a reserved symbol");
}


public void setExpression(Expression expression) throws PropertyVetoException, ExpressionBindingException {
	throw new RuntimeException("cannot change the value of a reserved symbol");
}


public void setName(String name) throws PropertyVetoException {
	throw new RuntimeException("cannot rename a reserved symbols");
}


public void setUnitDefinition(VCUnitDefinition unit) throws PropertyVetoException {
	throw new RuntimeException("cannot change unit of a reserved symbol");
}

}
