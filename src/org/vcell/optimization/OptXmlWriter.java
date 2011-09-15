/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.optimization;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.jdom.CDATA;
import org.jdom.Element;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationMethod;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationParameter;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationParameterType;
import org.vcell.sbml.vcell.MathModel_SBMLExporter;
import org.vcell.util.BeanUtils;
import org.vcell.util.ISize;
import org.vcell.util.document.KeyValue;

import cbit.util.xml.XmlUtil;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.surface.GeometrySurfaceDescription;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.math.Function;
import cbit.vcell.math.MathUtilities;
import cbit.vcell.math.Variable;
import cbit.vcell.mathmodel.MathModel;
import cbit.vcell.model.ReservedSymbol;
import cbit.vcell.modelopt.ParameterEstimationTask;
import cbit.vcell.modelopt.ReferenceDataMappingSpec;
import cbit.vcell.opt.Constraint;
import cbit.vcell.opt.ConstraintType;
import cbit.vcell.opt.ElementWeights;
import cbit.vcell.opt.ExplicitFitObjectiveFunction;
import cbit.vcell.opt.ExplicitObjectiveFunction;
import cbit.vcell.opt.ObjectiveFunction;
import cbit.vcell.opt.OdeObjectiveFunction;
import cbit.vcell.opt.OptimizationException;
import cbit.vcell.opt.OptimizationSpec;
import cbit.vcell.opt.Parameter;
import cbit.vcell.opt.PdeObjectiveFunction;
import cbit.vcell.opt.ReferenceData;
import cbit.vcell.opt.SimpleReferenceData;
import cbit.vcell.opt.SpatialReferenceData;
import cbit.vcell.opt.TimeWeights;
import cbit.vcell.opt.VariableWeights;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.resource.ResourceUtil;
import cbit.vcell.solver.ExplicitOutputTimeSpec;
import cbit.vcell.solver.OutputTimeSpec;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationJob;
import cbit.vcell.solver.SimulationSymbolTable;
import cbit.vcell.solver.SolverDescription;
import cbit.vcell.solver.SolverException;
import cbit.vcell.solver.TimeBounds;
import cbit.vcell.solver.TimeStep;
import cbit.vcell.solver.ode.CVodeFileWriter;
import cbit.vcell.solver.ode.IDAFileWriter;
import cbit.vcell.solvers.FiniteVolumeFileWriter;
import cbit.vcell.xml.XmlParseException;

public class OptXmlWriter {
	
	public static Element getCoapsiOptProblemDescriptionXML(ParameterEstimationTask parameterEstimationTask) throws IOException, XmlParseException, ExpressionException{
		OptimizationSpec optimizationSpec = parameterEstimationTask.getModelOptimizationMapping().getOptimizationSpec();			

		Element optProblemDescriptionElement = new Element(OptXmlTags.OptProblemDescription_Tag);
		
		File modelSbmlFile = File.createTempFile("mathModel", ".xml", ResourceUtil.getVcellHome());
		SimulationContext simulationContext = parameterEstimationTask.getSimulationContext();
		simulationContext.refreshMathDescription();
        MathModel vcellMathModel = new MathModel(null);
        vcellMathModel.setMathDescription(simulationContext.getMathDescription());
        //get math model string
        String sbmlString = MathModel_SBMLExporter.getSBMLString(vcellMathModel, 2, 4);

//		String modelSbml = XmlHelper.exportSBML(simulationContext.getBioModel(), 3, 1, 0, false, simulationContext, null);
        
        XmlUtil.writeXMLStringToFile(sbmlString, modelSbmlFile.getAbsolutePath(), true);
        Element element = new Element(OptXmlTags.MathModelSbmlFile_Tag);
        element.addContent(modelSbmlFile.getAbsolutePath());
        optProblemDescriptionElement.addContent(element);

		if (optimizationSpec.isComputeProfileDistributions()) {
			optProblemDescriptionElement.setAttribute(OptXmlTags.ComputeProfileDistributions_Attr, optimizationSpec.isComputeProfileDistributions() + "");
		}
		optProblemDescriptionElement.addContent(getVCellOptionsXML(parameterEstimationTask.getOptimizationSolverSpec().getCopasiOptimizationMethod()));
		optProblemDescriptionElement.addContent(getParameterDescriptionXML(optimizationSpec));
		Element dataElement = getCopasiDataXML(parameterEstimationTask);
		optProblemDescriptionElement.addContent(dataElement);
		
		element = getCopasiOptimizationMethodXML(parameterEstimationTask.getOptimizationSolverSpec().getCopasiOptimizationMethod());
		optProblemDescriptionElement.addContent(element);		
		return optProblemDescriptionElement;
	}

	public static Element getVCellOptionsXML(CopasiOptimizationMethod copasiOptimizationMethod)
	{
		Element vcOptionElement = new Element(OptXmlTags.VCellOptions_Tag);
		Element numRunsElement = new Element(OptXmlTags.NumOptimizationRuns_Tag);
		
		numRunsElement.addContent(copasiOptimizationMethod.getNumOfRuns()+"");
		vcOptionElement.addContent(numRunsElement);
		return vcOptionElement;
	}
	
	public static Element getCopasiOptimizationMethodXML(CopasiOptimizationMethod copasiOptimizationMethod) {
		Element element = new Element(OptXmlTags.CopasiOptimizationMethod);
		element.setAttribute(OptXmlTags.Name_Attr, copasiOptimizationMethod.getType().getName());
		for (CopasiOptimizationParameter cop : copasiOptimizationMethod.getParameters()) {
			//do not write "Num of runs" parameter within copasi parameters, it's a vcell option.
			//it is written under "VCellOptions" --> "NumberOfOptimizationRuns" tags.
			if(cop.getType() != CopasiOptimizationParameterType.Num_of_Runs) 
			{
				Element e = new Element(OptXmlTags.CopasiOptimizationParameter);
				e.setAttribute(OptXmlTags.Name_Attr, cop.getType().getDisplayName());
				e.setAttribute(OptXmlTags.Value_Attr, "" + cop.getValue());
				e.setAttribute(OptXmlTags.DataType_Attr, "" + cop.getType().getDataType());
				element.addContent(e);
			}
		}
		return element;
	}
	
	public static Element getOptProblemDescriptionXML(OptimizationSpec optimizationSpec) {
		Element optProblemDescriptionElement = new Element(OptXmlTags.OptProblemDescription_Tag);
		if (optimizationSpec.isComputeProfileDistributions()) {
			optProblemDescriptionElement.setAttribute(OptXmlTags.ComputeProfileDistributions_Attr, optimizationSpec.isComputeProfileDistributions() + "");
		}
		optProblemDescriptionElement.addContent(getParameterDescriptionXML(optimizationSpec));
		optProblemDescriptionElement.addContent(getConstraintDescriptionXML(optimizationSpec));
		optProblemDescriptionElement.addContent(getObjectiveFunctionXML(optimizationSpec));
		return optProblemDescriptionElement;
	}

	public static Element getParameterDescriptionXML(OptimizationSpec optimizationSpec){
		Element parameterDescriptionElement = new Element(OptXmlTags.ParameterDescription_Tag);
		Parameter[] parameters = optimizationSpec.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Element parameterElement = new Element(OptXmlTags.Parameter_Tag);
			parameterElement.setAttribute(OptXmlTags.ParameterName_Attr, parameters[i].getName());
			parameterElement.setAttribute(OptXmlTags.ParameterLow_Attr, Double.toString(parameters[i].getLowerBound()));
			parameterElement.setAttribute(OptXmlTags.ParameterHigh_Attr, Double.toString(parameters[i].getUpperBound()));
			parameterElement.setAttribute(OptXmlTags.ParameterInit_Attr, Double.toString(parameters[i].getInitialGuess()));
			parameterElement.setAttribute(OptXmlTags.ParameterScale_Attr, Double.toString(parameters[i].getScale()));
			parameterDescriptionElement.addContent(parameterElement);
		}
		return parameterDescriptionElement;
	}


	public static Element getConstraintDescriptionXML(OptimizationSpec optimizationSpec){
		Element constraintDescriptionElement = new Element(OptXmlTags.ConstraintDescription_Tag);
		Constraint[] constraints = optimizationSpec.getConstraints();
		for (int i = 0; i < constraints.length; i++) {
			Element constraintElement = new Element(OptXmlTags.Constraint_Tag);
			if (constraints[i].getConstraintType().equals(ConstraintType.LinearEquality)){
				constraintElement.setAttribute(OptXmlTags.ConstraintType_Attr, OptXmlTags.ConstraintType_Attr_LinearEquality);
			}else if (constraints[i].getConstraintType().equals(ConstraintType.LinearInequality)){
				constraintElement.setAttribute(OptXmlTags.ConstraintType_Attr, OptXmlTags.ConstraintType_Attr_LinearInequality);
			}else if (constraints[i].getConstraintType().equals(ConstraintType.NonlinearEquality)){
				constraintElement.setAttribute(OptXmlTags.ConstraintType_Attr, OptXmlTags.ConstraintType_Attr_NonlinearEquality);
			}else if (constraints[i].getConstraintType().equals(ConstraintType.NonlinearInequality)){
				constraintElement.setAttribute(OptXmlTags.ConstraintType_Attr, OptXmlTags.ConstraintType_Attr_NonlinearInequality);
			}else {
				throw new RuntimeException("unsupported constraint type "+constraints[i].getConstraintType());
			}
			constraintElement.addContent(constraints[i].getExpression().infix());
			constraintDescriptionElement.addContent(constraintElement);
		}
		return constraintDescriptionElement;
	}

	public static Element getObjectiveFunctionXML(OptimizationSpec optimizationSpec) {
		Element objectiveFunctionElement = new Element(OptXmlTags.ObjectiveFunction_Tag);
		ObjectiveFunction objectiveFunction = optimizationSpec.getObjectiveFunction();
		
		if (objectiveFunction instanceof ExplicitObjectiveFunction){
			
			ExplicitObjectiveFunction explicitObjectiveFunction = (ExplicitObjectiveFunction)objectiveFunction;
			objectiveFunctionElement.setAttribute(OptXmlTags.ObjectiveFunctionType_Attr,OptXmlTags.ObjectiveFunctionType_Attr_Explicit);
			Element expressionElement = new Element(OptXmlTags.Expression_Tag);
			expressionElement.addContent(explicitObjectiveFunction.getExpression().infix());		
			objectiveFunctionElement.addContent(expressionElement);
			
		}else if (objectiveFunction instanceof ExplicitFitObjectiveFunction){
			
			ExplicitFitObjectiveFunction explicitFitObjectiveFunction = (ExplicitFitObjectiveFunction)objectiveFunction;
			objectiveFunctionElement.setAttribute(OptXmlTags.ObjectiveFunctionType_Attr,OptXmlTags.ObjectiveFunctionType_Attr_ExplicitFit);
			//add expression list
			Element expressionListElement = new Element(OptXmlTags.ExpressionList_Tag);
			objectiveFunctionElement.addContent(expressionListElement);
			ExplicitFitObjectiveFunction.ExpressionDataPair[] expDataPairs = explicitFitObjectiveFunction.getExpressionDataPairs();
			for(int i=0; i<expDataPairs.length; i++)
			{
				Element expressionElement = new Element(OptXmlTags.Expression_Tag);
				expressionElement.setAttribute(OptXmlTags.ExpRefDataColumnIndex_Attr, expDataPairs[i].getReferenceDataIndex()+"");
				expressionElement.addContent(expDataPairs[i].getFitFunction().infix());
				expressionListElement.addContent(expressionElement);
			}
			if(explicitFitObjectiveFunction.getReferenceData() instanceof SimpleReferenceData)
			{
				Element dataElement = getDataXML(explicitFitObjectiveFunction.getReferenceData());
				objectiveFunctionElement.addContent(dataElement);
			}
			else
			{
				throw new OptimizationException("Optimization XML Writer exports SimpleReferenceData only.");
			}

		}else if (objectiveFunction instanceof OdeObjectiveFunction){
			
			OdeObjectiveFunction odeObjectiveFunction = (OdeObjectiveFunction)objectiveFunction;
			objectiveFunctionElement.setAttribute(OptXmlTags.ObjectiveFunctionType_Attr,OptXmlTags.ObjectiveFunctionType_Attr_ODE);
			ReferenceData refData = odeObjectiveFunction.getReferenceData();
			if(refData instanceof SimpleReferenceData)
			{
				Element dataElement = getDataXML((SimpleReferenceData)refData);
				objectiveFunctionElement.addContent(dataElement);
			}
			else
			{
				throw new OptimizationException("Optimization XML Writer exports SimpleReferenceData only.");
			}
			Element modelElement = getModelXML(odeObjectiveFunction, optimizationSpec.getParameterNames());
			objectiveFunctionElement.addContent(modelElement);
			
			Simulation tempSim = new Simulation(odeObjectiveFunction.getMathDescription());
			SimulationSymbolTable simSymbolTable = new SimulationSymbolTable(tempSim, 0);
			//
			// write data/model variable mappings
			//
			for (int i = 1; i < refData.getNumDataColumns(); i ++) {
				Element modelMappingElement = new Element(OptXmlTags.ModelMapping_Tag);
				modelMappingElement.setAttribute(OptXmlTags.ModelMappingDataColumn_Attr,refData.getColumnNames()[i]);
				modelMappingElement.setAttribute(OptXmlTags.ModelMappingWeight_Attr,String.valueOf(refData.getColumnWeights()[i]));

				Expression mappingExpression = null;
				try {
					cbit.vcell.math.Variable var = odeObjectiveFunction.getMathDescription().getVariable(refData.getColumnNames()[i]);
					if (var instanceof cbit.vcell.math.Function) {
						Expression exp = new Expression(var.getExpression());
						exp.bindExpression(simSymbolTable);
						exp = MathUtilities.substituteFunctions(exp, simSymbolTable);
						mappingExpression = exp.flatten();
					} else {
						mappingExpression = new Expression(var.getName());
					}
				} catch (ExpressionException e) {
					e.printStackTrace();
					throw new OptimizationException(e.getMessage());
				}
				modelMappingElement.addContent(mappingExpression.infix());
				objectiveFunctionElement.addContent(modelMappingElement);
			}	
		} else if (objectiveFunction instanceof PdeObjectiveFunction){
			
			PdeObjectiveFunction pdeObjectiveFunction = (PdeObjectiveFunction)objectiveFunction;
			objectiveFunctionElement.setAttribute(OptXmlTags.ObjectiveFunctionType_Attr,OptXmlTags.ObjectiveFunctionType_Attr_PDE);
			SpatialReferenceData refData = pdeObjectiveFunction.getReferenceData();

			Element dataElement = getDataXML(refData);
			objectiveFunctionElement.addContent(dataElement);

			Element modelElement = getModelXML(pdeObjectiveFunction, optimizationSpec.getParameterNames());
			objectiveFunctionElement.addContent(modelElement);
			
			Simulation tempSim = new Simulation(pdeObjectiveFunction.getMathDescription());
			SimulationSymbolTable simSymbolTable = new SimulationSymbolTable(tempSim, 0); 
			//
			// write data/model variable mappings
			//
			for (int i = 1; i < refData.getNumVariables(); i ++) {
				Element modelMappingElement = new Element(OptXmlTags.ModelMapping_Tag);
				modelMappingElement.setAttribute(OptXmlTags.ModelMappingDataColumn_Attr,refData.getVariableNames()[i]);
				modelMappingElement.setAttribute(OptXmlTags.ModelMappingWeight_Attr,String.valueOf(refData.getVariableWeights()[i]));

				Expression mappingExpression = null;
				try {
					Variable var = pdeObjectiveFunction.getMathDescription().getVariable(refData.getVariableNames()[i]);
					if (var instanceof Function) {
						Expression exp = new Expression(var.getExpression());
						exp.bindExpression(simSymbolTable);
						exp = MathUtilities.substituteFunctions(exp, simSymbolTable);
						mappingExpression = exp.flatten();
					} else {
						mappingExpression = new Expression(var.getName());
					}
				} catch (ExpressionException e) {
					e.printStackTrace();
					throw new OptimizationException(e.getMessage());
				}
				modelMappingElement.addContent(mappingExpression.infix());
				objectiveFunctionElement.addContent(modelMappingElement);
			}
		}
		return objectiveFunctionElement;
	}

	public static Element getDataXML(ReferenceData refData){
		Element refDataElement = null; 
		if(refData instanceof SimpleReferenceData)
		{
			refDataElement = new Element(OptXmlTags.SimpleReferenceData_Tag);
		}
		else if(refData instanceof SpatialReferenceData)
		{
			refDataElement = new Element(OptXmlTags.SpatialReferenceData_Tag);
		}
		
		if(refDataElement != null)
		{
			// write variable declarations
			// independent variable is t and dimension is 1, these are fixed.
			Element timeVarElement = new Element(OptXmlTags.Variable_Tag);
			timeVarElement.setAttribute(OptXmlTags.VariableType_Attr,OptXmlTags.VariableType_Attr_Independent);
			timeVarElement.setAttribute(OptXmlTags.VariableName_Attr,ReservedSymbol.TIME.getName());
			timeVarElement.setAttribute(OptXmlTags.VariableDimension_Attr,"1");
			refDataElement.addContent(timeVarElement);
			// check if t is at the first column
			int timeIndex = refData.findColumn(ReservedSymbol.TIME.getName());
			if (timeIndex != 0) {
				throw new RuntimeException("t must be the first column");
			}
			// add all other dependent variables, recall that the dependent variables start from 2nd column onward in reference data
			for (int i = 1; i < refData.getNumDataColumns(); i++) {
				Element variableElement = new Element(OptXmlTags.Variable_Tag);
				variableElement.setAttribute(OptXmlTags.VariableType_Attr,OptXmlTags.VariableType_Attr_Dependent);
				variableElement.setAttribute(OptXmlTags.VariableName_Attr,refData.getColumnNames()[i]);
				variableElement.setAttribute(OptXmlTags.VariableDimension_Attr,refData.getDataSize() + "");
				refDataElement.addContent(variableElement);
			}
			// write data
			Element dataRowElement = new Element(OptXmlTags.Datarow_Tag);
			for (int i = 0; i < refData.getNumDataRows(); i ++) {
				double[] data = refData.getDataByRow(i);
				Element rowElement = new Element(OptXmlTags.Row_Tag);
				StringBuffer rowText = new StringBuffer();
				for (int j = 0; j < data.length; j++) {
					rowText.append(data[j]+" ");
				}
				rowElement.addContent(rowText.toString());
				dataRowElement.addContent(rowElement);
			}
			refDataElement.addContent(dataRowElement);
			// write weights 
			Element weightRowElement = new Element(OptXmlTags.WeightDatarow_Tag);
			// add weight type
			if(refData.getWeights() instanceof ElementWeights)//print weights in multiple rows, each row has one-to-more elements
			{
				weightRowElement.setAttribute(OptXmlTags.WeightType_Attr, OptXmlTags.WeightType_Attr_Element);
				double[][] weightData = ((ElementWeights)refData.getWeights()).getWeightData();
				//elementWeights: first dimensin is number of rows, second dimension is number of vars
				for (int i = 0; i < weightData.length; i ++) {
					double[] rowData = weightData[i];
					Element rowElement = new Element(OptXmlTags.Row_Tag);
					StringBuffer rowText = new StringBuffer();
					for (int j = 0; j < rowData.length; j++) {
						rowText.append(rowData[j]+" ");
					}
					rowElement.addContent(rowText.toString());
					weightRowElement.addContent(rowElement);
				}
				
			}
			else if(refData.getWeights() instanceof VariableWeights) //print weights in one row, the row has one-to-more elements
			{
				weightRowElement.setAttribute(OptXmlTags.WeightType_Attr, OptXmlTags.WeightType_Attr_Variable);
				double[] weightData = ((VariableWeights)refData.getWeights()).getWeightData();
				Element rowElement = new Element(OptXmlTags.Row_Tag);
				StringBuffer rowText = new StringBuffer();
				for (int j = 0; j < weightData.length; j++) {
					rowText.append(weightData[j]+" ");
				}
				rowElement.addContent(rowText.toString());
				weightRowElement.addContent(rowElement);
			}
			else//time weights. Print weights in multiple rows, each row has one element
			{
				weightRowElement.setAttribute(OptXmlTags.WeightType_Attr, OptXmlTags.WeightType_Attr_Time);
				double[] weightData = ((TimeWeights)refData.getWeights()).getWeightData();
				for (int j = 0; j < weightData.length; j++) {
					Element rowElement = new Element(OptXmlTags.Row_Tag);
					StringBuffer rowText = new StringBuffer();
					rowText.append(weightData[j]+" ");
					rowElement.addContent(rowText.toString());
					weightRowElement.addContent(rowElement);
				}
			}
			 
			refDataElement.addContent(weightRowElement);
		}
		return refDataElement;
	}

	public static Element getModelXML(OdeObjectiveFunction odeObjectiveFunction, String[] parameterNames){
		Element modelElement = new Element(OptXmlTags.Model_Tag);

		ReferenceData refData = odeObjectiveFunction.getReferenceData();
		double refDataEndTime = refData.getDataByColumn(0)[refData.getNumDataRows()-1];

		//
		// post the problem either as an IDA or CVODE model
		//
		org.vcell.util.document.SimulationVersion simVersion = new org.vcell.util.document.SimulationVersion(
				new KeyValue("12345"),
				"name",
				new org.vcell.util.document.User("user",new KeyValue("123")),
				new org.vcell.util.document.GroupAccessNone(),
				null, // versionBranchPointRef
				new java.math.BigDecimal(1.0), // branchID
				new java.util.Date(),
				org.vcell.util.document.VersionFlag.Archived,
				"",
				null);
		Simulation simulation = new Simulation(simVersion,odeObjectiveFunction.getMathDescription());
		int timeIndex = refData.findColumn("t");
		if (timeIndex>=0){
			double[] refTimeData = refData.getDataByColumn(timeIndex);
			OutputTimeSpec outputTimeSpec = new ExplicitOutputTimeSpec(refTimeData);
			try {
				simulation.getSolverTaskDescription().setOutputTimeSpec(outputTimeSpec);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
				throw new OptimizationException("failed to set output times for simulation: "+e.getMessage());
			}
		}

		if (odeObjectiveFunction.getMathDescription().hasFastSystems()){
			//
			// has fast systems, must use IDA
			//
			try {
				simulation.getSolverTaskDescription().setTimeBounds(new TimeBounds(0.0, refDataEndTime));
				simulation.getSolverTaskDescription().setSolverDescription(SolverDescription.IDA);
				StringWriter simulationInputStringWriter = new StringWriter();
				IDAFileWriter idaFileWriter = new IDAFileWriter(new PrintWriter(simulationInputStringWriter,true), new SimulationJob(simulation, 0, null));
				idaFileWriter.write(parameterNames);
				simulationInputStringWriter.close();
				modelElement.setAttribute(OptXmlTags.ModelType_Attr,OptXmlTags.ModelType_Attr_IDA);
				CDATA simulationInputText = new CDATA(simulationInputStringWriter.getBuffer().toString());
				modelElement.addContent(simulationInputText);
			}catch (Exception e){
				e.printStackTrace(System.out);
				throw new OptimizationException("failed to create IDA input file: "+e.getMessage());
			}
		}else{
			//
			// no fast systems, use CVODE
			//
			try {
				simulation.getSolverTaskDescription().setTimeBounds(new TimeBounds(0.0, refDataEndTime));
				simulation.getSolverTaskDescription().setSolverDescription(SolverDescription.CVODE);
				StringWriter simulationInputStringWriter = new StringWriter();
				CVodeFileWriter cvodeFileWriter = new CVodeFileWriter(new PrintWriter(simulationInputStringWriter,true), new SimulationJob(simulation, 0, null));
				cvodeFileWriter.write(parameterNames);
				simulationInputStringWriter.close();
				modelElement.setAttribute(OptXmlTags.ModelType_Attr,OptXmlTags.ModelType_Attr_CVODE);
				CDATA simulationInputText = new CDATA(simulationInputStringWriter.getBuffer().toString());
				modelElement.addContent(simulationInputText);
			}catch (Exception e){
				e.printStackTrace(System.out);
				throw new OptimizationException("failed to create CVODE input file: "+e.getMessage());
			}
		}
		return modelElement;
	}

	public static Element getModelXML(PdeObjectiveFunction pdeObjectiveFunction, String[] parameterNames){
		Element modelElement = new Element(OptXmlTags.Model_Tag);
		try {
			SpatialReferenceData refData = pdeObjectiveFunction.getReferenceData();
			double refDataEndTime = refData.getDataByRow(refData.getNumDataRows()-1)[0];

			//
			// post the problem either as an IDA or CVODE model
			//
			org.vcell.util.document.SimulationVersion simVersion = new org.vcell.util.document.SimulationVersion(
					new KeyValue("12345"),
					"name",
					new org.vcell.util.document.User("user",new KeyValue("123")),
					new org.vcell.util.document.GroupAccessNone(),
					null, // versionBranchPointRef
					new java.math.BigDecimal(1.0), // branchID
					new java.util.Date(),
					org.vcell.util.document.VersionFlag.Archived,
					"",
					null);
			Simulation simulation = new Simulation(simVersion,pdeObjectiveFunction.getMathDescription());
			simulation.getMeshSpecification().setSamplingSize(refData.getDataISize());	

			double[] times = refData.getDataByColumn(0);
			double minDt = Double.POSITIVE_INFINITY;
			for (int i = 1; i < times.length; i ++) {
				minDt = Math.min(minDt, times[i] - times[i-1]);
			}
			simulation.getSolverTaskDescription().setTimeBounds(new cbit.vcell.solver.TimeBounds(0.0, refDataEndTime));
			simulation.getSolverTaskDescription().setSolverDescription(SolverDescription.FiniteVolume);
			simulation.getSolverTaskDescription().setTimeStep(new TimeStep(minDt/5, minDt/5, minDt/5));
			
			// clone and resample geometry
			Geometry resampledGeometry = null;
			try {
				resampledGeometry = (Geometry) BeanUtils.cloneSerializable(simulation.getMathDescription().getGeometry());
				GeometrySurfaceDescription geoSurfaceDesc = resampledGeometry.getGeometrySurfaceDescription();
				ISize newSize = simulation.getMeshSpecification().getSamplingSize();
				geoSurfaceDesc.setVolumeSampleSize(newSize);
				geoSurfaceDesc.updateAll();		
			} catch (Exception e) {
				e.printStackTrace();
				throw new SolverException(e.getMessage());
			}	
			SimulationJob simJob = new SimulationJob(simulation, 0, pdeObjectiveFunction.getFieldDataIDSs());
			
			StringWriter simulationInputStringWriter = new StringWriter();
			FiniteVolumeFileWriter fvFileWriter = new FiniteVolumeFileWriter(new PrintWriter(simulationInputStringWriter,true), simJob, resampledGeometry, pdeObjectiveFunction.getWorkingDirectory());		
			fvFileWriter.write(parameterNames);
			simulationInputStringWriter.close();
			modelElement.setAttribute(OptXmlTags.ModelType_Attr,OptXmlTags.ModelType_Attr_FVSOLVER);
			CDATA simulationInputText = new CDATA(simulationInputStringWriter.getBuffer().toString());
			modelElement.addContent(simulationInputText);
		}catch (Exception e){
			e.printStackTrace(System.out);
			throw new OptimizationException("failed to create fv input file: "+e.getMessage());
		}
		return modelElement;
	}
	
	public static Element getCopasiDataXML(ParameterEstimationTask parameterEstimationTask) throws IOException{
		ReferenceData refData = parameterEstimationTask.getModelOptimizationSpec().getReferenceData();
		Element refDataElement = null; 
		if(refData instanceof SimpleReferenceData)
		{
			refDataElement = new Element(OptXmlTags.SimpleReferenceData_Tag);
		}
		else if(refData instanceof SpatialReferenceData)
		{
			refDataElement = new Element(OptXmlTags.SpatialReferenceData_Tag);
		}
		
		if(refDataElement != null)
		{
			// write variable declarations
			// independent variable is t and dimension is 1, these are fixed.
			Element timeVarElement = new Element(OptXmlTags.Variable_Tag);
			timeVarElement.setAttribute(OptXmlTags.VariableType_Attr,OptXmlTags.VariableType_Attr_Independent);
			timeVarElement.setAttribute(OptXmlTags.VariableName_Attr,ReservedSymbol.TIME.getName());
			refDataElement.addContent(timeVarElement);
			// check if t is at the first column
			int timeIndex = refData.findColumn(ReservedSymbol.TIME.getName());
			if (timeIndex != 0) {
				throw new RuntimeException("t must be the first column");
			}
			// add all other dependent variables, recall that the dependent variables start from 2nd column onward in reference data
			File expDataFile = File.createTempFile("expData", ".txt", ResourceUtil.getVcellHome());
			PrintWriter	pw = new PrintWriter(expDataFile);	
			pw.print("# Time\t");
			for (int i = 1; i < refData.getNumDataColumns(); i++) {
				ReferenceDataMappingSpec rdms = parameterEstimationTask.getModelOptimizationSpec().getReferenceDataMappingSpec(refData.getColumnNames()[i]);
				
				Element variableElement = new Element(OptXmlTags.Variable_Tag);
				variableElement.setAttribute(OptXmlTags.VariableType_Attr,OptXmlTags.VariableType_Attr_Dependent);
				variableElement.setAttribute(OptXmlTags.VariableName_Attr,rdms.getModelObject().getName());
				refDataElement.addContent(variableElement);
				
				pw.print(refData.getColumnNames()[i] + "\t");
			}
			pw.println();
			
			// write data
			for (int i = 0; i < refData.getNumDataRows(); i ++) {
				double[] data = refData.getDataByRow(i);
				for (int j = 0; j < data.length; j++) {
					pw.print(data[j] + "\t");
				}
				pw.println();
			}
			
			pw.close();
			Element dataFileElement = new Element(OptXmlTags.ExperimentalDataFile_Tag);
			dataFileElement.setAttribute(OptXmlTags.ExperimentalDataFile_Attr_LastRow, (refData.getNumDataRows() + 1) + "");
			dataFileElement.addContent(expDataFile.getAbsolutePath());
			refDataElement.addContent(dataFileElement);
		}
		return refDataElement;
	}
}