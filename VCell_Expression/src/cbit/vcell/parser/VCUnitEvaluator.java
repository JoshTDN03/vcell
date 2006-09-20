package cbit.vcell.parser;
import ucar.units.RationalNumber;
import cbit.vcell.units.VCUnitDefinition;
import cbit.vcell.units.VCUnitException;

import java.util.ArrayList;
/**
 * This class may not exist in the future, and its functionality may be spread on one or more classes.
 
 * Creation date: (3/5/2004 4:37:34 PM)
 * @author: Rashad Badrawi
 */
public class VCUnitEvaluator {

	public static VCUnitDefinition defaultLengthUnit = VCUnitDefinition.UNIT_um;

	public static class UnitsHashMap {
		boolean bDirty = false;
		java.util.HashMap hashMap = new java.util.HashMap();
		public UnitsHashMap() {
		}
		public VCUnitDefinition put(SymbolTableEntry keySTE, VCUnitDefinition unitDefinition){
			if (unitDefinition==null){
				throw new RuntimeException(keySTE.getName()+" has a null unit definition");
			}
			VCUnitDefinition oldUnit = (VCUnitDefinition)hashMap.put(keySTE,unitDefinition);
			if (oldUnit==null){
				bDirty = true;
			}else if (!oldUnit.isTBD() && !unitDefinition.compareEqual(oldUnit)){
				throw new RuntimeException("unit for '"+keySTE.getName()+"' set to '"+unitDefinition.getSymbol()+"', was '"+oldUnit.getSymbol());
			}
			return oldUnit;
		}
		public VCUnitDefinition get(SymbolTableEntry keySTE){
			return (VCUnitDefinition)hashMap.get(keySTE);
		}
		public void clearAll() {
			clearDirty();
			hashMap.clear();
		}
		public void clearDirty() {
			bDirty = false;
		}
		public boolean getDirty() {
			return bDirty;
		}
	}


	private static void assignAndVerify(VCUnitDefinition nodeUnit, SimpleNode node, UnitsHashMap unitsHashMap) throws ExpressionException {
		
		if (node == null) {
			throw new RuntimeException("VCUnitEvaluator.assignAndVerify(): node is null");
		}
		if (nodeUnit == null) {
			throw new RuntimeException("VCUnitEvaluator.assignAndVerify(): nodeUnit is null");
		}
		
		//
		// children should have same units as parent
		//
		if (node instanceof ASTAndNode || node instanceof ASTOrNode || node instanceof ASTNotNode) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				assignAndVerify(VCUnitDefinition.UNIT_DIMENSIONLESS,(SimpleNode)node.jjtGetChild(i),unitsHashMap);
			}
		}else if (node instanceof ASTRelationalNode){
			//
			// if either child has known unit, then other must also be known
			//
			SimpleNode child0 = (SimpleNode)node.jjtGetChild(0);
			SimpleNode child1 = (SimpleNode)node.jjtGetChild(1);
			VCUnitDefinition unit0 = getUnitDefinition(child0,unitsHashMap);
			VCUnitDefinition unit1 = getUnitDefinition(child1,unitsHashMap);
			if (unit0.isTBD() && unit1.isTBD()){
				assignAndVerify(VCUnitDefinition.UNIT_TBD,child0,unitsHashMap);
				assignAndVerify(VCUnitDefinition.UNIT_TBD,child1,unitsHashMap);				
				return;
			} else if (unit0.isTBD()){
				assignAndVerify(unit1,child0,unitsHashMap);
				return;
			} else if (unit1.isTBD()){
				assignAndVerify(unit0,child1,unitsHashMap);
				return;
			} else if (!unit0.compareEqual(unit1)){
				throw new RuntimeException("operands of a relational operator are not same ["+unit0.getSymbol()+"] and ["+unit1.getSymbol()+"]");
			} else {
				assignAndVerify(unit0,child0,unitsHashMap);
				assignAndVerify(unit0,child1,unitsHashMap);
			}
		}else if (node instanceof ASTAddNode || node instanceof ASTMinusTermNode) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				assignAndVerify(nodeUnit,(SimpleNode)node.jjtGetChild(i),unitsHashMap);
			}
		} else if (node instanceof ASTMultNode) {
			if (node.jjtGetNumChildren() == 1) {
				assignAndVerify(nodeUnit,(SimpleNode)node.jjtGetChild(0),unitsHashMap);
			}else{
				//
				// product of children should be that of parent (tough one)
				// ... can only resolve if only one unknown
				//
				SimpleNode unknownChildNode = null;
				int unknownChildCount = 0;
				int constantChildCount = 0;
				VCUnitDefinition accumUnit = nodeUnit;	
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {             
					SimpleNode child = (SimpleNode)node.jjtGetChild(i);
					VCUnitDefinition childUnit = getUnitDefinition(child,unitsHashMap);
					assignAndVerify(childUnit,child,unitsHashMap);
					if (childUnit.isTBD()){
						//
						// ignore those that evaluate to a constant
						//
						boolean bConstant = false;
						try {
							child.evaluateConstant();
							bConstant = true;
						}catch (ExpressionException e){
						}
						if (!bConstant){
							unknownChildNode = child;
							unknownChildCount++;
						}else{
							constantChildCount++;
						}
					} else {
						if (!accumUnit.isTBD()){
							accumUnit = accumUnit.divideBy(childUnit);
						}
					}
				}
				if (unknownChildCount==0 && constantChildCount==0){
					// should cancel to dimensionless, but if constant children (numbers...) are used in a product, then can assume appropriate units.
					if (!accumUnit.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS)){
						//accumUnit.show();
						//accumUnit.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS);
						throw new RuntimeException("expression '"+node.infixString(SimpleNode.LANGUAGE_DEFAULT,SimpleNode.NAMESCOPE_DEFAULT)+"' missing factor of '"+accumUnit.getSymbol()+"'");
					}
				}else if (unknownChildCount==1){
					//
					// can resolve child
					//
					if (!accumUnit.isTBD()){
						assignAndVerify(accumUnit,unknownChildNode,unitsHashMap);
					}
				}
			}
		} else if (node instanceof ASTInvertTermNode) {
			SimpleNode child = (SimpleNode)node.jjtGetChild(0);
			if (nodeUnit.isTBD()){
				assignAndVerify(nodeUnit,child,unitsHashMap);
			}else{
				assignAndVerify(nodeUnit.getInverse(),child,unitsHashMap);
			}
		//} else if (node instanceof DerivativeNode) {
			//unit = getUnitDefinition((SimpleNode)node.jjtGetChild(1),unitsHashMap);
			//return getUnitDefinition((SimpleNode)node.jjtGetChild(0),unitsHashMap).divideBy(unit);
		//} else if (node instanceof ASTLaplacianNode) {
			//unit = ReservedSymbol.X.getUnitDefinition(); 
			//return getUnitDefinition((SimpleNode)node.jjtGetChild(0),unitsHashMap).divideBy(unit).divideBy(unit);
	    } else if (node instanceof ASTExpression) {
			assignAndVerify(nodeUnit,(SimpleNode)node.jjtGetChild(0),unitsHashMap);
		} else if (node instanceof ASTFloatNode) {          //return TBD instead of dimensionless.
			// do nothing
		} else if (node instanceof ASTFuncNode) {   
			String functionName = ((ASTFuncNode)node).getName();
			if (functionName.equalsIgnoreCase("sqrt")) {         //?              
				assignAndVerify(nodeUnit.raiseTo(new RationalNumber(2)),(SimpleNode)node.jjtGetChild(0),unitsHashMap);
			}else if (functionName.equalsIgnoreCase("exp")) {         //?              
				assignAndVerify(VCUnitDefinition.UNIT_DIMENSIONLESS,(SimpleNode)node.jjtGetChild(0),unitsHashMap);
			}else if (functionName.equalsIgnoreCase("pow")) {              							 // later....
				assignAndVerify(VCUnitDefinition.UNIT_DIMENSIONLESS,(SimpleNode)node.jjtGetChild(1),unitsHashMap);  // exponent should always be dimensionless
				//
				// for base, impose the 1/Nth root of nodeUnit if exponent is constant
				//
				try {
					double exponentValue = ((SimpleNode)node.jjtGetChild(1)).evaluateConstant();
					if (!nodeUnit.isTBD()){
						RationalNumber rn = RationalNumber.getApproximateFraction(exponentValue);
						assignAndVerify(nodeUnit.raiseTo(rn.inverse()),(SimpleNode)node.jjtGetChild(0),unitsHashMap);  // exponent should always be dimensionless
					}
				}catch (ExpressionException e){
					//
					// a^b where b not constant, a must be non-dimensional
					//
					assignAndVerify(VCUnitDefinition.UNIT_DIMENSIONLESS,(SimpleNode)node.jjtGetChild(0),unitsHashMap);  // exponent should always be dimensionless
				}
			} else if (functionName.equalsIgnoreCase("abs") || functionName.equalsIgnoreCase("min") ||
					   functionName.equalsIgnoreCase("max")) {
				//
				// children should be same as parent
				//
				for (int i = 0; i < node.jjtGetNumChildren(); i++){
					assignAndVerify(nodeUnit,(SimpleNode)node.jjtGetChild(i),unitsHashMap);
				}
			} else {
				if (!nodeUnit.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS)){
					throw new RuntimeException("function '"+functionName+"' should be dimensionless, assignAndVerify trying to impose '"+nodeUnit.getSymbol());
				}
				//
				// child should be dimensionless
				//
				assignAndVerify(VCUnitDefinition.UNIT_DIMENSIONLESS,(SimpleNode)node.jjtGetChild(0),unitsHashMap);
			}
		} else if (node instanceof ASTPowerNode) {
				assignAndVerify(VCUnitDefinition.UNIT_DIMENSIONLESS,(SimpleNode)node.jjtGetChild(1),unitsHashMap);  // exponent should always be dimensionless
				//
				// for base, impose the 1/Nth root of nodeUnit if exponent is constant
				//
				try {
					double exponentValue = ((SimpleNode)node.jjtGetChild(1)).evaluateConstant();
					RationalNumber rn = RationalNumber.getApproximateFraction(exponentValue);
					assignAndVerify(nodeUnit.raiseTo(rn.inverse()),(SimpleNode)node.jjtGetChild(0),unitsHashMap);  // exponent should always be dimensionless
				}catch (ExpressionException e){
					//
					// a^b where b not constant, a must be non-dimensional
					//
					assignAndVerify(VCUnitDefinition.UNIT_DIMENSIONLESS,(SimpleNode)node.jjtGetChild(0),unitsHashMap);  // exponent should always be dimensionless
				}
		} else if (node instanceof ASTIdNode) {
			if (!nodeUnit.isTBD()){
				unitsHashMap.put(((ASTIdNode)node).symbolTableEntry,nodeUnit);
			}
		} else {
			throw new ExpressionException("node type "+node.getClass().toString()+" not supported yet");
		}
	}


	public static VCUnitDefinition computeUnit(VCUnitDefinition units []) throws VCUnitException {

		return computeUnit(units, false);		
	}


	public static VCUnitDefinition computeUnit(VCUnitDefinition units [], boolean assignTBDs) throws VCUnitException {
	
		if (units == null || units.length == 0)
			return null;
		VCUnitDefinition unit = VCUnitDefinition.UNIT_TBD;
		boolean first = true;
		for (int i = 0; i < units.length; i++) {
			if (units[i].isTBD()) {
				continue;
			} else {
				if (first) {
					unit = units[i];
					first = false;
					continue;
				}
			}
			if (!unit.compareEqual(units[i])){
				throw new VCUnitException("Incompatible units: [" + unit.getSymbol() + "] and [" + units[i].getSymbol() + "]");
			}
		}

		if (assignTBDs && unit != null) {
			for (int i = 0; i < units.length; i++) {
				if (units[i].isTBD()) {
					units[i] = unit;
				}	
			}
		}
		 
		return unit;
	}


	public static VCUnitDefinition getUnitDefinition(Expression exp) throws ExpressionException, VCUnitException {
		UnitsHashMap unitsHashMap = new UnitsHashMap();
		String symbols[] = exp.getSymbols();
		for (int i = 0;symbols!=null && i < symbols.length; i++){
			SymbolTableEntry ste = exp.getSymbolBinding(symbols[i]);
			unitsHashMap.put(ste,ste.getUnitDefinition());
		}
		return getUnitDefinition(exp.getRootNode(),unitsHashMap);
	}


	private static VCUnitDefinition getUnitDefinition(SimpleNode node, UnitsHashMap unitsHashMap) throws ExpressionException, VCUnitException {
		
		VCUnitDefinition unit = null;                                //temp variable

		if (node == null) {
			return null;
		}
		if (node instanceof ASTAndNode || node instanceof ASTOrNode || node instanceof ASTNotNode) {
			ArrayList units = new ArrayList();
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				SimpleNode child = (SimpleNode)node.jjtGetChild(i);
				VCUnitDefinition childUnit = getUnitDefinition(child,unitsHashMap);
				if (!childUnit.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS)){
					throw new VCUnitException("argument to boolean expression '"+child.infixString(SimpleNode.LANGUAGE_DEFAULT,SimpleNode.NAMESCOPE_DEFAULT)+"' must be dimensionless");
				}
			}
			return VCUnitDefinition.UNIT_DIMENSIONLESS;
		} else if (node instanceof ASTRelationalNode) {
			ArrayList units = new ArrayList();
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				units.add(getUnitDefinition((SimpleNode)node.jjtGetChild(i),unitsHashMap));
			}
			computeUnit((VCUnitDefinition [])units.toArray(new VCUnitDefinition[units.size()]), true); // looking for imcompatabilities
			return VCUnitDefinition.UNIT_DIMENSIONLESS;
		} else if (node instanceof ASTAddNode || node instanceof ASTMinusTermNode) {
			ArrayList units = new ArrayList();
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				units.add(getUnitDefinition((SimpleNode)node.jjtGetChild(i),unitsHashMap));
			}
			return computeUnit((VCUnitDefinition [])units.toArray(new VCUnitDefinition[units.size()]), true);
		} else if (node instanceof ASTMultNode) {
			if (node.jjtGetNumChildren() == 1) {
				return getUnitDefinition((SimpleNode)node.jjtGetChild(0),unitsHashMap);
			}
			VCUnitDefinition accumUnit = null;	
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {             
				SimpleNode child = (SimpleNode)node.jjtGetChild(i);
				unit = getUnitDefinition(child,unitsHashMap);
				if (unit.isTBD()){
					return unit;
				}
				if (accumUnit == null){
					accumUnit = unit;
				}else {
					accumUnit = accumUnit.multiplyBy(unit);
				}
			}
			return accumUnit;
		} else if (node instanceof ASTInvertTermNode) {
			SimpleNode child = (SimpleNode)node.jjtGetChild(0);
			unit = getUnitDefinition(child,unitsHashMap);
			if (unit.isTBD()){
				return unit;
			}else{
				return unit.getInverse();
			}
		} else if (node instanceof DerivativeNode) {
			unit = getUnitDefinition((SimpleNode)node.jjtGetChild(1),unitsHashMap);
			return getUnitDefinition((SimpleNode)node.jjtGetChild(0),unitsHashMap).divideBy(unit);
		} else if (node instanceof ASTLaplacianNode) {
			unit = defaultLengthUnit;
			return getUnitDefinition((SimpleNode)node.jjtGetChild(0),unitsHashMap).divideBy(unit).divideBy(unit);
	    } else if (node instanceof ASTExpression) {
			return getUnitDefinition((SimpleNode)node.jjtGetChild(0),unitsHashMap);
		} else if (node instanceof ASTFloatNode) {          //return TBD instead of dimensionless. 
			return VCUnitDefinition.UNIT_TBD;
		} else if (node instanceof ASTFuncNode) {   
			String functionName = ((ASTFuncNode)node).getName();
			if (functionName.equalsIgnoreCase("pow")) {       
				SimpleNode child0 =  (SimpleNode)node.jjtGetChild(0);
				SimpleNode child1 =  (SimpleNode)node.jjtGetChild(1);
				VCUnitDefinition unit0 = getUnitDefinition(child0,unitsHashMap);
				VCUnitDefinition unit1 = getUnitDefinition(child1,unitsHashMap);
				if (!unit1.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS) && !unit1.compareEqual(VCUnitDefinition.UNIT_TBD)){
					throw new VCUnitException("exponent of '"+node.infixString(SimpleNode.LANGUAGE_DEFAULT,SimpleNode.NAMESCOPE_DEFAULT)+"' has units of "+unit0);
				}
				if (unit0.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS) || unit0.isTBD()){
					return unit0;
				}
				try {
					double d = ((SimpleNode)node.jjtGetChild(1)).evaluateConstant();
					RationalNumber rn = RationalNumber.getApproximateFraction(d);
					return unit0.raiseTo(rn);
				}catch(ExpressionException e){
					return VCUnitDefinition.UNIT_TBD;  // ????? don't know the unit now
				}
			}else if (functionName.equalsIgnoreCase("exp")) {       
				SimpleNode child0 =  (SimpleNode)node.jjtGetChild(0);
				VCUnitDefinition unit0 = getUnitDefinition(child0,unitsHashMap);
				if (!unit0.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS) && !unit0.isTBD()){
					throw new VCUnitException("exponent of exp() '"+node.infixString(SimpleNode.LANGUAGE_DEFAULT,SimpleNode.NAMESCOPE_DEFAULT)+"' has units of "+unit0);
				}
				return VCUnitDefinition.UNIT_DIMENSIONLESS;
			}else if (functionName.equalsIgnoreCase("sqrt")) {       
				SimpleNode child0 =  (SimpleNode)node.jjtGetChild(0);
				VCUnitDefinition unit0 = getUnitDefinition(child0,unitsHashMap);
				if (unit0.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS) || unit0.isTBD()){
					return unit0;
				}
				RationalNumber rn = new RationalNumber(1,2);
				return unit0.raiseTo(rn);
			} else if (functionName.equalsIgnoreCase("abs") || functionName.equalsIgnoreCase("min") ||
					   functionName.equalsIgnoreCase("max")) {
				return getUnitDefinition((SimpleNode)node.jjtGetChild(0),unitsHashMap);
			} else {                                          //if (functionName.equalsIgnoreCase("exp")) {
				return VCUnitDefinition.UNIT_DIMENSIONLESS;
			}
		} else if (node instanceof ASTPowerNode) {
			SimpleNode child0 =  (SimpleNode)node.jjtGetChild(0);
			SimpleNode child1 =  (SimpleNode)node.jjtGetChild(1);
			VCUnitDefinition unit0 = getUnitDefinition(child0,unitsHashMap);
			VCUnitDefinition unit1 = getUnitDefinition(child1,unitsHashMap);
			if (!unit1.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS) && !unit1.compareEqual(VCUnitDefinition.UNIT_TBD)){
				throw new VCUnitException("exponent of '"+node.infixString(SimpleNode.LANGUAGE_DEFAULT,SimpleNode.NAMESCOPE_DEFAULT)+"' has units of "+unit0);
			}
			if (unit0.compareEqual(VCUnitDefinition.UNIT_DIMENSIONLESS)){
				return VCUnitDefinition.UNIT_DIMENSIONLESS;
			}
			boolean bConstantExponent = false;
			double exponentValue = 1;
			try {
				exponentValue = child1.evaluateConstant();
				bConstantExponent = true;
			}catch(ExpressionException e){
				bConstantExponent = false;
			}
			if (bConstantExponent){ //
				if (unit0.isTBD()){
					return VCUnitDefinition.UNIT_TBD;
				}else{
					RationalNumber rn = RationalNumber.getApproximateFraction(exponentValue);
					return unit0.raiseTo(rn);
				}
			}else{
				return VCUnitDefinition.UNIT_TBD;
			}
		} else if (node instanceof ASTIdNode) {
			SymbolTableEntry ste = ((ASTIdNode)node).symbolTableEntry;
			unit = unitsHashMap.get(ste);
			if (unit == null) {
				unit = ste.getUnitDefinition();
				if (unit==null){
					throw new ExpressionException("No unit found for expression node: " + node.toString());
				}else{
					unitsHashMap.put(ste,unit);
				}
			}
			return unit;		
		} else {
			throw new ExpressionException("node type "+node.getClass().toString()+" not supported yet");
		}
	}


public static VCUnitDefinition[] suggestUnitDefinitions(SymbolTableEntry symbolTableEntries[]) throws ExpressionException, VCUnitException {
	
	//
	// initialize to already known units
	//
	UnitsHashMap unitsHashMap = new UnitsHashMap();
	for (int i = 0; i < symbolTableEntries.length; i++){
		unitsHashMap.put(symbolTableEntries[i],symbolTableEntries[i].getUnitDefinition());
	}
	
	//
	// propagate units
	//
	while (unitsHashMap.getDirty()){
		unitsHashMap.clearDirty();
		for (int i = 0; i < symbolTableEntries.length; i++){
			Expression exp = symbolTableEntries[i].getExpression();
			if (exp != null){
				VCUnitDefinition vcUnitDefinition = (VCUnitDefinition)unitsHashMap.get(symbolTableEntries[i]);
				if (vcUnitDefinition==null){
					vcUnitDefinition = getUnitDefinition(exp.getRootNode(),unitsHashMap);
					unitsHashMap.put(symbolTableEntries[i],vcUnitDefinition);
				}
				assignAndVerify(vcUnitDefinition,exp.flatten().getRootNode(),unitsHashMap);
			}
		}
	}

	//
	// return array
	//
	VCUnitDefinition unitDefinitions[] = new VCUnitDefinition[symbolTableEntries.length];
	for (int i = 0; i < symbolTableEntries.length; i++){
		unitDefinitions[i] = unitsHashMap.get(symbolTableEntries[i]);
	}
	return unitDefinitions;
}
}