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
import cbit.vcell.mapping.SimulationContext;
/**
 * Insert the type's description here.
 * Creation date: (8/22/2005 9:52:46 AM)
 * @author: Jim Schaff
 */
public class ModelOptimizationSpecTest {
/**
 * Insert the method's description here.
 * Creation date: (8/22/2005 9:53:12 AM)
 * @return cbit.vcell.modelopt.ModelOptimizationSpec
 */
public static ModelOptimizationSpec getExample() {

	try {
		SimulationContext simContext = cbit.vcell.mapping.SimulationContextTest.getExample(0);
		ModelOptimizationSpec modelOptSpec = new ModelOptimizationSpec(simContext);
		ParameterMappingSpec[] parameterMappingSpecs = modelOptSpec.getParameterMappingSpecs();

		parameterMappingSpecs[0].setSelected(true);
		parameterMappingSpecs[1].setSelected(true);

		String dataString = "SimpleReferenceData { 3 2 t Ca_er 1 1 0 1 1 2 2 3 }";
		modelOptSpec.setReferenceData(cbit.vcell.opt.SimpleReferenceData.fromVCML(new org.vcell.util.CommentStringTokenizer(dataString)));
		
		return modelOptSpec;
	}catch (Throwable e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
}
}
