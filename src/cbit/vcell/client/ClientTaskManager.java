/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JComponent;

import org.vcell.util.gui.DialogUtils;

import cbit.image.ImageException;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.GeometryClass;
import cbit.vcell.geometry.GeometryException;
import cbit.vcell.geometry.gui.GeometryThumbnailImageFactoryAWT;
import cbit.vcell.geometry.surface.GeometricRegion;
import cbit.vcell.mapping.MappingException;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.solver.AnnotatedFunction;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SolverTaskDescription;
import cbit.vcell.solver.TimeBounds;
import cbit.vcell.solver.UniformOutputTimeSpec;

public class ClientTaskManager {
	public static AsynchClientTask[] newApplication(final BioModel bioModel, final boolean isStoch) {		
		
		AsynchClientTask task0 = new AsynchClientTask("create application", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
			
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				String newApplicationName = bioModel.getFreeSimulationContextName();
				SimulationContext newSimulationContext = bioModel.addNewSimulationContext(newApplicationName, isStoch);
				hashTable.put("newSimulationContext", newSimulationContext);
			}
		};
		AsynchClientTask task1 = new AsynchClientTask("process geometry", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				SimulationContext newSimulationContext = (SimulationContext)hashTable.get("newSimulationContext");
				newSimulationContext.getGeometry().precomputeAll(new GeometryThumbnailImageFactoryAWT());
			}
		};
		return new AsynchClientTask[] {task0, task1};
	}

	public static AsynchClientTask[] copyApplication(final JComponent requester, final BioModel bioModel, final SimulationContext simulationContext, final boolean bSpatial, final boolean bStochastic) {	
		//get valid application name
		String newApplicationName = null;
		String baseName = "Copy of " + simulationContext.getName();
		int count = 0;
		while (true) {
			if (count == 0) {
				newApplicationName = baseName;
			} else {
				newApplicationName = baseName + " " + count;
			}
			if (bioModel.getSimulationContext(newApplicationName) == null) {
				break;
			}
			count ++;
		}
		
		final String newName = newApplicationName;
		AsynchClientTask task1 = new AsynchClientTask("preparing to copy", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				SimulationContext newSimulationContext = ClientTaskManager.copySimulationContext(simulationContext, newName, bSpatial, bStochastic);
				newSimulationContext.getGeometry().precomputeAll(new GeometryThumbnailImageFactoryAWT());
				if (newSimulationContext.isSameTypeAs(simulationContext)) { 
					newSimulationContext.refreshMathDescription();
				}
				hashTable.put("newSimulationContext", newSimulationContext);
			}
		};
		AsynchClientTask task2 = new AsynchClientTask("copying application and simulations", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {			
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				SimulationContext newSimulationContext = (SimulationContext)hashTable.get("newSimulationContext");
				bioModel.addSimulationContext(newSimulationContext);
				if (newSimulationContext.isSameTypeAs(simulationContext)) {
					// copy simulations to new simContext
					for (Simulation sim : simulationContext.getSimulations()) {
						Simulation clonedSimulation = new Simulation(sim, false);
						clonedSimulation.setMathDescription(newSimulationContext.getMathDescription());
						clonedSimulation.setName(simulationContext.getBioModel().getFreeSimulationName());
						newSimulationContext.addSimulation(clonedSimulation);
					}
					// copy output functions to new simContext
					ArrayList<AnnotatedFunction> outputFunctions = simulationContext.getOutputFunctionContext().getOutputFunctionsList(); 
					ArrayList<AnnotatedFunction> newOutputFunctions = new ArrayList<AnnotatedFunction>();
					for (AnnotatedFunction afn : outputFunctions) {
						newOutputFunctions.add(new AnnotatedFunction(afn));
					}
					newSimulationContext.getOutputFunctionContext().setOutputFunctions(newOutputFunctions);
				} else {
					if (simulationContext.getSimulations().length > 0) {
						DialogUtils.showWarningDialog(requester, "Simulations are not copied because new application is of different type.");
					}
				}
			}
		};
		return new AsynchClientTask[] { task1, task2};			
	}

	public static SimulationContext copySimulationContext(SimulationContext srcSimContext, String newSimulationContextName, boolean bSpatial, boolean bStoch) throws java.beans.PropertyVetoException, ExpressionException, MappingException, GeometryException, ImageException {
		Geometry newClonedGeometry = new Geometry(srcSimContext.getGeometry());
		newClonedGeometry.precomputeAll(new GeometryThumbnailImageFactoryAWT());
		//if stoch copy to ode, we need to check is stoch is using particles. If yes, should convert particles to concentraton.
		//the other 3 cases are fine. ode->ode, ode->stoch, stoch-> stoch 
		SimulationContext destSimContext = new SimulationContext(srcSimContext,newClonedGeometry, bStoch);
		if(srcSimContext.isStoch() && !srcSimContext.isUsingConcentration() && !bStoch)
		{
			try {
				destSimContext.convertSpeciesIniCondition(true);
			} catch (MappingException e) {
				e.printStackTrace();
				throw new java.beans.PropertyVetoException(e.getMessage(), null);
			}
		}
		if (srcSimContext.getGeometry().getDimension() > 0 && !bSpatial) { // copy the size over
			destSimContext.setGeometry(new Geometry("nonspatial", 0));
			StructureMapping srcStructureMappings[] = srcSimContext.getGeometryContext().getStructureMappings();
			StructureMapping destStructureMappings[] = destSimContext.getGeometryContext().getStructureMappings();
			for (StructureMapping destStructureMapping : destStructureMappings) {
				for (StructureMapping srcStructureMapping : srcStructureMappings) {
					if (destStructureMapping.getStructure() == srcStructureMapping.getStructure()) {
						if (srcStructureMapping.getUnitSizeParameter() != null) {
							Expression sizeRatio = srcStructureMapping.getUnitSizeParameter().getExpression();
							GeometryClass srcGeometryClass = srcStructureMapping.getGeometryClass();
							GeometricRegion[] srcGeometricRegions = srcSimContext.getGeometry().getGeometrySurfaceDescription().getGeometricRegions(srcGeometryClass);
							if (srcGeometricRegions != null) {
								double size = 0;
								for (GeometricRegion srcGeometricRegion : srcGeometricRegions) {
									size += srcGeometricRegion.getSize();
								}
								destStructureMapping.getSizeParameter().setExpression(Expression.mult(sizeRatio, new Expression(size)));
							}
						}
						break;
					}
				}
			}
		}
		destSimContext.setName(newSimulationContextName);	
		return destSimContext;
	}
	
	public static void changeEndTime(JComponent requester, SolverTaskDescription solverTaskDescription, double newEndTime) throws PropertyVetoException {
		TimeBounds oldTimeBounds = solverTaskDescription.getTimeBounds();
		TimeBounds timeBounds = new TimeBounds(oldTimeBounds.getStartingTime(), newEndTime);
		solverTaskDescription.setTimeBounds(timeBounds);
		
		if (solverTaskDescription.getOutputTimeSpec() instanceof UniformOutputTimeSpec) {
			UniformOutputTimeSpec uniformOutputTimeSpec = (UniformOutputTimeSpec)solverTaskDescription.getOutputTimeSpec();
			if (timeBounds.getEndingTime() < uniformOutputTimeSpec.getOutputTimeStep()) {
				double outputTime = solverTaskDescription.getTimeBounds().getEndingTime()/20.0;
				String ret = PopupGenerator.showWarningDialog(requester, "Output Interval", 
						"Output interval(" + uniformOutputTimeSpec.getOutputTimeStep() + "s) is greater than end time(" + timeBounds.getEndingTime() + "s) which will not output any results. Do you want to change " +
						"output interval to every " + outputTime + "s (20 time points)?\n\nIf not, output interval will change to " + timeBounds.getEndingTime() + "s(the end time).",
						new String[]{ UserMessage.OPTION_YES, UserMessage.OPTION_NO}, UserMessage.OPTION_YES);
				if (ret.equals(UserMessage.OPTION_YES)) {
					solverTaskDescription.setOutputTimeSpec(new UniformOutputTimeSpec(outputTime));
				} else {
					solverTaskDescription.setOutputTimeSpec(new UniformOutputTimeSpec(timeBounds.getEndingTime()));
				}
			}
		}
	}
}
