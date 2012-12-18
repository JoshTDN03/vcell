/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.modelopt;
import java.util.Vector;

import org.vcell.util.BeanUtils;
import org.vcell.util.Issue;

import cbit.vcell.mapping.MappingException;
import cbit.vcell.mapping.MathMapping;
import cbit.vcell.mapping.MathSymbolMapping;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.math.AnnotatedFunction;
import cbit.vcell.math.Constant;
import cbit.vcell.math.InconsistentDomainException;
import cbit.vcell.math.MathDescription;
import cbit.vcell.math.MathException;
import cbit.vcell.math.ParameterVariable;
import cbit.vcell.math.Variable;
import cbit.vcell.matrix.MatrixException;
import cbit.vcell.model.ModelException;
import cbit.vcell.model.ReservedSymbol;
import cbit.vcell.opt.OdeObjectiveFunction;
import cbit.vcell.opt.OptimizationResultSet;
import cbit.vcell.opt.OptimizationSpec;
import cbit.vcell.opt.Parameter;
import cbit.vcell.opt.ReferenceData;
import cbit.vcell.opt.SimpleReferenceData;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionBindingException;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.ode.ODESolverResultSetColumnDescription;
import cbit.vcell.util.RowColumnResultSet;
/**
 * Insert the type's description here.
 * Creation date: (8/22/2005 9:26:10 AM)
 * @author: Jim Schaff
 */
public class ModelOptimizationMapping {
	private ModelOptimizationSpec modelOptimizationSpec = null;
	private OptimizationSpec optimizationSpec = null;
	private ParameterMapping[] parameterMappings = null;
	private MathSymbolMapping mathSymbolMapping = null;

/**
 * ModelOptimizationMapping constructor comment.
 */
public ModelOptimizationMapping(ModelOptimizationSpec argModelOptimizationSpec) {
	super();
	modelOptimizationSpec = argModelOptimizationSpec;
}


/**
 * Insert the method's description here.
 * Creation date: (8/30/2005 3:16:33 PM)
 * @return cbit.vcell.solver.ode.ODESolverResultSet
 * @param optResultSet cbit.vcell.opt.OptimizationResultSet
 */
public void applySolutionToMathOverrides(Simulation simulation, OptimizationResultSet optResultSet) throws ExpressionException {
	if (simulation==null){
		throw new RuntimeException("simulation is null");
	}
	//
	// load initial guesses for all parameters into MathOverrides
	// (Include those being optimized as well as those not being optimized)
	//
	if (mathSymbolMapping==null){
		try {
			computeOptimizationSpec();
		}catch (MappingException e){
			e.printStackTrace(System.out);
			throw new RuntimeException("couldn't generate math to map parameters to simulation");
		}catch (MathException e){
			e.printStackTrace(System.out);
			throw new RuntimeException("couldn't generate math to map parameters to simulation");
		}
	}
//	ParameterMappingSpec[] parameterMappingSpecs = getModelOptimizationSpec().getParameterMappingSpecs();
//	for (int i = 0; i < parameterMappingSpecs.length; i++){
//		Variable var = mathSymbolMapping.getVariable(parameterMappingSpecs[i].getModelParameter());
//		if (var instanceof Constant){
//			simulation.getMathOverrides().putConstant(new Constant(var.getName(),new Expression(parameterMappingSpecs[i].getCurrent())));
//		}
//	}

	//
	// if solution exists, override initial guesses with solution
	//
	if (optResultSet != null){
//		String[] solutionNames = optResultSet.getSolutionNames();
		if (optResultSet.getOptSolverResultSet() != null && optResultSet.getOptSolverResultSet().getParameterNames() != null){
			//
			// correct math overrides with parameter solution
			//
			for (int i = 0; i < optResultSet.getOptSolverResultSet().getParameterNames().length; i++){
				simulation.getMathOverrides().putConstant(
						new Constant(optResultSet.getOptSolverResultSet().getParameterNames()[i],new Expression(optResultSet.getOptSolverResultSet().getBestEstimates()[i])));
			}
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (8/22/2005 9:26:52 AM)
 * @return cbit.vcell.opt.OptimizationSpec
 * @param modelOptimizationSpec cbit.vcell.modelopt.ModelOptimizationSpec
 */
MathSymbolMapping computeOptimizationSpec() throws MathException, MappingException {

	if (getModelOptimizationSpec().getReferenceData()==null){
		System.out.println("no referenced data defined");
		return null;
	}
	OptimizationSpec optSpec = new OptimizationSpec();
	optSpec.setComputeProfileDistributions(modelOptimizationSpec.isComputeProfileDistributions());
	parameterMappings = null;

	//
	// get original MathDescription (later to be substituted for local/global parameters).
	//
	SimulationContext simContext = modelOptimizationSpec.getSimulationContext();
	MathMapping mathMapping = simContext.createNewMathMapping();
	MathDescription origMathDesc = null;
	mathSymbolMapping = null;
	try {
		origMathDesc = mathMapping.getMathDescription();
		mathSymbolMapping = mathMapping.getMathSymbolMapping();
	}catch (MatrixException e){
		e.printStackTrace(System.out);
		throw new MappingException(e.getMessage());
	}catch (ModelException e){
		e.printStackTrace(System.out);
		throw new MappingException(e.getMessage());
	}catch (ExpressionException e){
		e.printStackTrace(System.out);
		throw new MathException(e.getMessage());
	}

	//
	// create objective function (mathDesc and data)
	//
	StructureMapping structureMapping = simContext.getGeometryContext().getStructureMappings()[0];
	ReferenceData referenceData = getRemappedReferenceData(mathMapping,structureMapping);
	if (referenceData==null){
		throw new RuntimeException("no referenced data defined");
	}
	OdeObjectiveFunction odeObjectiveFunction = new OdeObjectiveFunction(origMathDesc,referenceData);
	optSpec.setObjectiveFunction(odeObjectiveFunction);

	//
	// get parameter mappings
	//
	ParameterMappingSpec[] parameterMappingSpecs = modelOptimizationSpec.getParameterMappingSpecs();
	Vector<ParameterMapping> parameterMappingList = new Vector<ParameterMapping>();
	Variable allVars[] = (Variable[])BeanUtils.getArray(origMathDesc.getVariables(),Variable.class);
	for (int i = 0; i < parameterMappingSpecs.length; i++){
		cbit.vcell.model.Parameter modelParameter = parameterMappingSpecs[i].getModelParameter();
		String mathSymbol = mathMapping.getMathSymbol(modelParameter,structureMapping.getGeometryClass());
		Variable mathVariable = origMathDesc.getVariable(mathSymbol);
		if (parameterMappingSpecs[i].isSelected()) {
			if (parameterMappingSpecs[i].getHigh() < parameterMappingSpecs[i].getLow()) {
				throw new MathException("The lower bound for Parameter '" + parameterMappingSpecs[i].getModelParameter().getName() + "' is greater than its upper bound.");
			}
			if (parameterMappingSpecs[i].getCurrent() < parameterMappingSpecs[i].getLow()){
				throw new MathException("The initial guess of '" + parameterMappingSpecs[i].getModelParameter().getName() + "' is smaller than its lower bound.");
			}
			if (parameterMappingSpecs[i].getCurrent() > parameterMappingSpecs[i].getHigh()){
				throw new MathException("The initial guess of '" + parameterMappingSpecs[i].getModelParameter().getName() + "' is greater than its upper bound.");
			}
			if (parameterMappingSpecs[i].getLow() < 0) {
				throw new MathException("The lower bound for Parameter '" + parameterMappingSpecs[i].getModelParameter().getName() + "' is negative. All lower bounds must not be negative.");
			}
			if (Double.isInfinite(parameterMappingSpecs[i].getLow())) {
				throw new MathException("The lower bound for Parameter '" + parameterMappingSpecs[i].getModelParameter().getName() + "' is infinity. Lower bounds must not be infinity.");
			}
			if (parameterMappingSpecs[i].getHigh() <= 0) {
				throw new MathException("The upper bound for Parameter '" + parameterMappingSpecs[i].getModelParameter().getName() + "' is negative. All upper bounds must be positive.");
			}
			if (Double.isInfinite(parameterMappingSpecs[i].getHigh())) {
				throw new MathException("The upper bound for Parameter '" + parameterMappingSpecs[i].getModelParameter().getName() + "' is infinity. Upper bounds must not be infinity.");
			}
		}
		Parameter optParameter = new Parameter(
								mathSymbol,
								parameterMappingSpecs[i].isSelected() && parameterMappingSpecs[i].getLow() == 0 ? 1e-8 : parameterMappingSpecs[i].getLow(),
								parameterMappingSpecs[i].getHigh(),
								Math.abs(parameterMappingSpecs[i].getCurrent()) < 1.0E-10 ? 1.0 : Math.abs(parameterMappingSpecs[i].getCurrent()),
								parameterMappingSpecs[i].getCurrent());
		ParameterMapping parameterMapping = new ParameterMapping(modelParameter,optParameter,mathVariable);
		//
		// replace constant values with "initial guess"
		//
		if (mathVariable instanceof Constant){
			Constant origConstant = (Constant)mathVariable;
			for (int j = 0; j < allVars.length; j++){
				if (allVars[j].equals(origConstant)){
					if (parameterMappingSpecs[i].isSelected()) {
						allVars[j] = new ParameterVariable(origConstant.getName());
					} else {
						allVars[j] = new Constant(origConstant.getName(),new Expression(optParameter.getInitialGuess()));
					}
					break;
				}
			}
		}
		//
		// add to list if "selected" for optimization.
		//
		if (parameterMappingSpecs[i].isSelected()){
			parameterMappingList.add(parameterMapping);
		}
	}
	parameterMappings = (ParameterMapping[])BeanUtils.getArray(parameterMappingList,ParameterMapping.class);
	try {
		origMathDesc.setAllVariables(allVars);
	}catch (ExpressionBindingException e){
		e.printStackTrace(System.out);
		throw new MathException(e.getMessage());
	}

	//
	// set optimization parameters
	//
	for (int i = 0; i < parameterMappings.length; i++){
		optSpec.addParameter(parameterMappings[i].getOptParameter());
	}

	Vector<Issue> issueList = new Vector<Issue>();
	optSpec.gatherIssues(issueList);
	for (int i = 0; i < issueList.size(); i++){
		Issue issue = issueList.elementAt(i);
		if (issue.getSeverity()==Issue.SEVERITY_ERROR){
			throw new RuntimeException(issue.getMessage());
		}
	}
	//
	//
	//
	optimizationSpec = optSpec;
	return mathSymbolMapping;
}


/**
 * Insert the method's description here.
 * Creation date: (8/25/2005 10:32:23 AM)
 * @return cbit.vcell.modelopt.ModelOptimizationSpec
 */
public ModelOptimizationSpec getModelOptimizationSpec() {
	return modelOptimizationSpec;
}


/**
 * Insert the method's description here.
 * Creation date: (8/22/2005 9:26:52 AM)
 * @return cbit.vcell.opt.OptimizationSpec
 * @param modelOptimizationSpec cbit.vcell.modelopt.ModelOptimizationSpec
 */
public OptimizationSpec getOptimizationSpec() {
	return optimizationSpec;
}


/**
 * Insert the method's description here.
 * Creation date: (8/25/2005 10:26:34 AM)
 * @return cbit.vcell.modelopt.ParameterMapping[]
 */
public ParameterMapping[] getParameterMappings() {
	return parameterMappings;
}


/**
 * Gets the constraintData property (cbit.vcell.opt.ConstraintData) value.
 * @return The constraintData property value.
 * @see #setConstraintData
 */
private ReferenceData getRemappedReferenceData(MathMapping mathMapping, StructureMapping structureMapping) throws MappingException {
	if (modelOptimizationSpec.getReferenceData()==null){
		return null;
	}
	//
	// make sure time is mapped
	//
	ReferenceData refData = modelOptimizationSpec.getReferenceData();
	ReferenceDataMappingSpec refDataMappingSpecs[] = modelOptimizationSpec.getReferenceDataMappingSpecs();
	RowColumnResultSet rowColResultSet = new RowColumnResultSet();
	Vector<SymbolTableEntry> modelObjectList = new Vector<SymbolTableEntry>();
	Vector<double[]> dataList = new Vector<double[]>();
	
	//
	// find bound columns, (time is always mapped to the first column)
	//
	int mappedColumnCount=0;
	for (int i = 0; i < refDataMappingSpecs.length; i++){
		SymbolTableEntry modelObject = refDataMappingSpecs[i].getModelObject();
		if (modelObject!=null){
			int mappedColumnIndex = mappedColumnCount;
			if (modelObject.equals(ReservedSymbol.TIME)){
				mappedColumnIndex = 0;
			}
			String origRefDataColumnName = refDataMappingSpecs[i].getReferenceDataColumnName();
			int origRefDataColumnIndex = refData.findColumn(origRefDataColumnName);
			if (origRefDataColumnIndex<0){
				throw new RuntimeException("reference data column named '"+origRefDataColumnName+"' not found");
			}
			double columnData[] = refData.getDataByColumn(origRefDataColumnIndex);
			if (modelObjectList.contains(modelObject)){
				throw new RuntimeException("multiple reference data columns mapped to same model object '"+modelObject.getName()+"'");
			}
			modelObjectList.insertElementAt(modelObject,mappedColumnIndex);
			dataList.insertElementAt(columnData,mappedColumnIndex);
			mappedColumnCount++;
		}
	}

	//
	// must have time and at least one other, else return null
	//
	if (modelObjectList.size()==0){
		throw new RuntimeException("reference data was not associated with model");
	}
	if (modelObjectList.size()==1){
		throw new RuntimeException("reference data was not associated with model, must map time and at least one other column");
	}
	if (!modelObjectList.contains(ReservedSymbol.TIME)){
		throw new RuntimeException("must map time column of reference data to model");
	}

	//
	// create data columns (time and rest)
	//
	for (int i = 0; i < modelObjectList.size(); i++){
		SymbolTableEntry modelObject = (SymbolTableEntry)modelObjectList.elementAt(i);
		String symbol = mathMapping.getMathSymbol(modelObject,structureMapping.getGeometryClass());
		rowColResultSet.addDataColumn(new ODESolverResultSetColumnDescription(symbol));
	}

	//
	// populate data columns (time and rest)
	//
	double[] weights = new double[rowColResultSet.getColumnDescriptionsCount()];
	weights[0] = 1.0;	
	
	int numRows = ((double[])dataList.elementAt(0)).length;
	int numColumns = modelObjectList.size();
	for (int j = 0; j < numRows; j++){
		double row[] = new double[numColumns];
		for (int i = 0; i < numColumns; i++){
			row[i] = ((double[])dataList.elementAt(i))[j];
			if (i > 0) {
				weights[i] += row[i] * row[i];
			}
		}
		rowColResultSet.addRow(row);
	}

	for (int i = 0; i < numColumns; i++){
		if (weights[i] == 0) {	
			weights[i] = 1;
		} else {
			weights[i] = 1/weights[i];
		}
	}
	
	SimpleReferenceData remappedRefData = new SimpleReferenceData(rowColResultSet,weights);
	return remappedRefData;
}


/**
 * Insert the method's description here.
 * Creation date: (5/2/2006 5:17:34 PM)
 */
public void refreshDependencies() {
}
}
