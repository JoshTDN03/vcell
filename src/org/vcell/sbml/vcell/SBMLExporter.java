/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sbml.vcell;
import java.util.ArrayList;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.Namespace;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Delay;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.OStringStream;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLError;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Trigger;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;
import org.vcell.sbml.SBMLUtils;
import org.vcell.sbml.SBMLUtils.SBMLUnitParameter;
import org.vcell.util.TokenMangler;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.mapping.BioEvent;
import cbit.vcell.mapping.MembraneMapping;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.SpeciesContextSpec;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.mapping.BioEvent.EventAssignment;
import cbit.vcell.model.DistributedKinetics;
import cbit.vcell.model.Feature;
import cbit.vcell.model.FluxReaction;
import cbit.vcell.model.Kinetics;
import cbit.vcell.model.LumpedKinetics;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Model;
import cbit.vcell.model.Model.ReservedSymbol;
import cbit.vcell.model.Parameter;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.resource.ResourceUtil;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationJob;
import cbit.vcell.units.VCUnitDefinition;
import cbit.vcell.xml.XMLTags;
import cbit.vcell.xml.XmlParseException;

/**
 * Insert the type's description here.
 * Creation date: (3/31/2006 1:02:43 PM)
 * @author: Anuradha Lakshminarayana
 */
public class SBMLExporter {
	private int sbmlLevel = 2;
	private int sbmlVersion = 3;
	private org.sbml.libsbml.Model sbmlModel = null;
	private cbit.vcell.biomodel.BioModel vcBioModel = null;

	private SimulationContext vcSelectedSimContext = null;
	private SimulationJob vcSelectedSimJob = null;
	
	// used for exporting vcell-related annotations.
	Namespace sbml_vcml_ns = Namespace.getNamespace(XMLTags.VCELL_NS_PREFIX, SBMLUtils.SBML_VCELL_NS);

	// SBMLAnnotationUtil to get the SBML-related annotations, notes, free-text annotations from a Biomodel VCMetaData
	private SBMLAnnotationUtil sbmlAnnotationUtil = null;

	private java.util.Hashtable<String, String> globalParamNamesHash = new java.util.Hashtable<String, String>();
	private SBMLExportSpec sbmlExportSpec = new SBMLExportSpec(VCUnitDefinition.UNIT_molecules, VCUnitDefinition.UNIT_um3, VCUnitDefinition.UNIT_um2, VCUnitDefinition.UNIT_um, VCUnitDefinition.UNIT_s);
	
	public static class SBMLExportSpec {
		private VCUnitDefinition substanceUnits = null;
		private VCUnitDefinition volUnits = null;
		private VCUnitDefinition areaUnits = null;
		private VCUnitDefinition lengthUnits = null;
		private VCUnitDefinition timeUnits = null;
		
		public SBMLExportSpec(VCUnitDefinition argSunits, VCUnitDefinition argVUnits, VCUnitDefinition argAUnits, VCUnitDefinition argLUnits, VCUnitDefinition argTUnits) {
			if (!argSunits.isCompatible(VCUnitDefinition.UNIT_molecules) && !argSunits.isCompatible(VCUnitDefinition.UNIT_mol)) {
				throw new RuntimeException("Substance units not compatible with molecules or moles");
			}
			if (!argVUnits.isCompatible(VCUnitDefinition.UNIT_um3)) {
				throw new RuntimeException("Volume units not compatible with m3 (eg., um3 or litre)");
			}
			if (!argAUnits.isCompatible(VCUnitDefinition.UNIT_um2)) {
				throw new RuntimeException("Area units not compatible with m2");
			}
			if (!argLUnits.isCompatible(VCUnitDefinition.UNIT_um)) {
				throw new RuntimeException("Length units not compatible with m");
			}
			if (!argTUnits.isCompatible(VCUnitDefinition.UNIT_s)) {
				throw new RuntimeException("Time units not compatible with sec");
			}
			this.substanceUnits = argSunits;
			this.volUnits = argVUnits;
			this.areaUnits = argAUnits;
			this.lengthUnits = argLUnits;
			this.timeUnits = argTUnits;
		}
		public VCUnitDefinition getSubstanceUnits() {
			return substanceUnits;			
		}
		public VCUnitDefinition getVolumeUnits() {
			return volUnits;		
		}
		public VCUnitDefinition getAreaUnits() {
			return areaUnits;			
		}
		public VCUnitDefinition getLengthUnits() {
			return lengthUnits;		
		}
		public VCUnitDefinition getTimeUnits() {
			return timeUnits;			
		}
		public VCUnitDefinition getConcentrationUnit(int dimension) {
			switch (dimension) {
			case 1 : 
				return getSubstanceUnits().divideBy(getLengthUnits());
			case 2 :
				return getSubstanceUnits().divideBy(getAreaUnits());
			case 3 : 
				return getSubstanceUnits().divideBy(getVolumeUnits());
			default :
				throw new RuntimeException("Unsupported dimension of compartment; unable to compute concentration units");
			}
		}
	}

	static
	{
		ResourceUtil.loadlibSbmlLibray();
	}

	/**
	 * SBMLExporter constructor comment.
	 */
	public SBMLExporter(BioModel argBioModel, int argSbmlLevel, int argSbmlVersion) {
		super();
		vcBioModel = argBioModel;
		sbmlLevel = argSbmlLevel;
		sbmlVersion = argSbmlVersion;
		if (vcBioModel != null) {
			sbmlAnnotationUtil = new SBMLAnnotationUtil(vcBioModel.getVCMetaData(), vcBioModel, SBMLUtils.getNamespaceFromLevelAndVersion(sbmlLevel, sbmlVersion));
		}
	}
	
	/**
	 * SBMLExporter constructor comment.
	 */
	public SBMLExporter(BioModel argBioModel) {
		super();
		vcBioModel = argBioModel;
		if (vcBioModel != null) {
			sbmlAnnotationUtil = new SBMLAnnotationUtil(vcBioModel.getVCMetaData(), vcBioModel, SBMLUtils.SBML_NS_2);
		}
	}

/**
 * addCompartments comment.
 */
protected void addCompartments() {
	cbit.vcell.model.Structure[] vcStructures = vcBioModel.getModel().getStructures();
	
	for (int i = 0; i < vcStructures.length; i++){
		org.sbml.libsbml.Compartment sbmlCompartment = sbmlModel.createCompartment();
		sbmlCompartment.setId(TokenMangler.mangleToSName(vcStructures[i].getName()));
		sbmlCompartment.setName(vcStructures[i].getName());
		VCUnitDefinition sbmlSizeUnit = null;
		if (vcStructures[i] instanceof Feature) {
			Feature vcFeature = (Feature)vcStructures[i];
			sbmlCompartment.setSpatialDimensions(3);
			sbmlCompartment.setConstant(true);
			String outside = null;
			if (vcFeature.getParentStructure() != null) {
				outside = TokenMangler.mangleToSName(vcFeature.getParentStructure().getName());
			}
			if (outside != null) {
				if (outside.length() > 0) {
					sbmlCompartment.setOutside(outside);
				}
			}
			sbmlSizeUnit = sbmlExportSpec.getVolumeUnits();
		} else if (vcStructures[i] instanceof Membrane) {
			Membrane vcMembrane = (Membrane)vcStructures[i];
			sbmlCompartment.setSpatialDimensions(2);
			sbmlCompartment.setOutside(TokenMangler.mangleToSName(vcMembrane.getOutsideFeature().getName()));
			sbmlSizeUnit = sbmlExportSpec.getAreaUnits();
			sbmlCompartment.setUnits(org.vcell.util.TokenMangler.mangleToSName(sbmlSizeUnit.getSymbol()));
		}

		StructureMapping vcStructMapping = getSelectedSimContext().getGeometryContext().getStructureMapping(vcStructures[i]);
		try {
			// The unit for 3D compartment size in VCell is um3, we are going to write it out in um3 in the SBML document.
			// Hence multiplying the size expression with the conversion factor between VC and SBML units for the compartment size. 
			
			Expression sizeExpr = null;
			VCUnitDefinition vcSizeUnit = vcStructMapping.getSizeParameter().getUnitDefinition();
			if (vcStructMapping.getSizeParameter().getExpression() == null) {
				throw new RuntimeException("Compartment size not set for compartment \"" + vcStructures[i].getName() + "\" ; Please set size and try exporting again.");
			}
			double factor = 1.0;
			factor = vcSizeUnit.convertTo(factor, sbmlSizeUnit);
			sizeExpr = Expression.mult(vcStructMapping.getSizeParameter().getExpression(), new Expression(factor));
			sbmlCompartment.setSize(sizeExpr.evaluateConstant());
		} catch (cbit.vcell.parser.ExpressionException e) {
			// If it is in the catch block, it means that the compartment size was probably not a double, but an assignment.
			// Check if the expression for the compartment size is not null and add it as an assignment rule.
			Expression sizeExpr = vcStructMapping.getSizeParameter().getExpression();
			if (sizeExpr != null) {
				ASTNode ruleFormulaNode = getFormulaFromExpression(sizeExpr);
				AssignmentRule assignRule = sbmlModel.createAssignmentRule();
				assignRule.setVariable(vcStructures[i].getName());
				assignRule.setMath(ruleFormulaNode);
				// If compartmentSize is specified by an assignment rule, the 'constant' field should be set to 'false' (default - true).
				sbmlCompartment.setConstant(false);
				sbmlModel.addRule(assignRule);
			}
		}
		
		// Get annotation (RDF and non-RDF) for reactionStep from SBMLAnnotationUtils
		sbmlAnnotationUtil.writeAnnotation(vcStructures[i], sbmlCompartment, null);
		
		// Now set notes,
		sbmlAnnotationUtil.writeNotes(vcStructures[i], sbmlCompartment);
	}
}


/**
 * addKineticParameterUnits:
 */
private void addKineticAndGlobalParameterUnits(ArrayList<String> unitsList) {

	//
	// Get all kinetic parameters from simple reactions and flux reactions from the Biomodel
	// And all Model (global) parameters from Model.
	// For each parameter,
	//		get its unit (VCunitDefinition)
	//		check if it is part of unitsList - if so, continue
	//		check if it is a base unit - if so, continue
	//		else, get the converted unit (VC -> SBML)
	//		add unit to sbmlModel unit definition
	//

	Vector<Parameter> paramsVector = new Vector<Parameter>();
	// Add globals
	ModelParameter[] globalParams = vcBioModel.getModel().getModelParameters();
	for (int i = 0; i < globalParams.length; i++) {
		paramsVector.addElement(globalParams[i]);
	}
	// Add reaction kinetic parameters
	ReactionStep[] vcReactions = vcBioModel.getModel().getReactionSteps();
	for (int i = 0; i < vcReactions.length; i++) {
		Kinetics rxnKinetics = vcReactions[i].getKinetics();
		Parameter[] kineticParams = rxnKinetics.getKineticsParameters();
		for (int j = 0; j < kineticParams.length; j++) {
			paramsVector.addElement(kineticParams[j]);
		}
	}

	for (int i = 0; i < paramsVector.size(); i++){
		Parameter param = (Parameter)paramsVector.elementAt(i);
		VCUnitDefinition paramUnitDefn = param.getUnitDefinition();
		if (paramUnitDefn == null || paramUnitDefn.isTBD()) {
			continue;
		}
		String unitSymbol = org.vcell.util.TokenMangler.mangleToSName(paramUnitDefn.getSymbol());
		// If this unit is present in the unitsList (already in the list of unitDefinitions for SBML model), continue.
		if (unitSymbol == null || unitsList.contains(unitSymbol)) {
			continue;
		}
		// If this unit is a base unit, continue.
		if (SBMLUnitTranslator.isSbmlBaseUnit(unitSymbol)) {
			continue;
		}
		if (!paramUnitDefn.isTBD()) {
			UnitDefinition newUnitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(paramUnitDefn, sbmlLevel, sbmlVersion);
			if (newUnitDefn != null) {
				unitsList.add(unitSymbol);
				sbmlModel.addUnitDefinition(newUnitDefn);
			}
		}
	}
}


/**
 * At present, the Virtual cell doesn't support global parameters
 */
protected void addParameters() throws ExpressionException {
	// first add an SBML parameter for VCell reserved symbol 'KMOLE'.
	org.sbml.libsbml.Parameter sbmlParam = sbmlModel.createParameter();
	String paramUnits = TokenMangler.mangleToSName(VCUnitDefinition.UNIT_uM_um3_per_molecules.getSymbol());
	sbmlParam.setId("KMOLE");
	sbmlParam.setValue(1.0/602.0);
	sbmlParam.setUnits(paramUnits);
	sbmlParam.setConstant(true);
	// Now add VCell global parameters to the SBML listofParameters
	Model vcModel = getSelectedSimContext().getModel();
	ReservedSymbol kMole = vcModel.getKMOLE();
	ModelParameter[] vcGlobalParams = vcModel.getModelParameters();  
	for (int i = 0; vcGlobalParams != null && i < vcGlobalParams.length; i++) {
		sbmlParam = sbmlModel.createParameter();
		sbmlParam.setId(vcGlobalParams[i].getName());
		Expression paramExpr = new Expression(vcGlobalParams[i].getExpression());
		if (paramExpr.isNumeric()) {
			// For a VCell global param, if it is numeric, it has a constant value and is not defined by a rule, hence set Constant = true.
			sbmlParam.setValue(paramExpr.evaluateConstant());
			sbmlParam.setConstant(true);
		} else {
			// non-numeric VCell global parameter will be defined by a (assignment) rule, hence mark Constant = false.
			sbmlParam.setConstant(false);

			// add assignment rule for param
			// First check if 'paramExpr' has any references to speciesContexts. If so, the conc units should be adjusted.
			// Get the VC (from SpeciesContextSpec) and SBML concentration units (from sbmlExportSpec) and get the conversion factor ('factor').
			// Replace the occurrence of species in the 'paramExpr' with the new expr : species*factor.
			String[] symbols = paramExpr.getSymbols();
			for (int j = 0; symbols != null && j < symbols.length; j++) {
				SpeciesContext vcSpeciesContext = vcModel.getSpeciesContext(symbols[j]); 
				if (vcSpeciesContext != null) {
					Species species = sbmlModel.getSpecies(vcSpeciesContext.getName());
					cbit.vcell.mapping.SpeciesContextSpec vcSpeciesContextsSpec = getSelectedSimContext().getReactionContext().getSpeciesContextSpec(vcSpeciesContext);
					VCUnitDefinition vcConcUnit = vcSpeciesContextsSpec.getInitialConditionParameter().getUnitDefinition();
					VCUnitDefinition sbmlConcUnits = sbmlExportSpec.getConcentrationUnit(vcSpeciesContext.getStructure().getDimension());
					SBMLUnitParameter sbmlUnitParam = SBMLUtils.getConcUnitFactor("spConcUnit", sbmlConcUnits, vcConcUnit, kMole);
					Expression modifiedSpExpr = Expression.mult(new Expression(species.getId()), sbmlUnitParam.getExpression()).flatten();
					paramExpr.substituteInPlace(new Expression(species.getId()), modifiedSpExpr);
				}
			}

			ASTNode paramFormulaNode = getFormulaFromExpression(paramExpr);
			AssignmentRule sbmlParamAssignmentRule = sbmlModel.createAssignmentRule();
			sbmlParamAssignmentRule.setVariable(vcGlobalParams[i].getName());
			sbmlParamAssignmentRule.setMath(paramFormulaNode);
		}
		VCUnitDefinition vcParamUnit = vcGlobalParams[i].getUnitDefinition();
		if (!vcParamUnit.compareEqual(VCUnitDefinition.UNIT_TBD)) {
			sbmlParam.setUnits(TokenMangler.mangleToSName(vcParamUnit.getSymbol()));
		}
	}
}


/**
 * addReactions comment.
 */
protected void addReactions() {

	// Check if any reaction has electrical mapping
	boolean bCalculatePotential = false;
	StructureMapping structureMappings[] = getSelectedSimContext().getGeometryContext().getStructureMappings();
	for (int i = 0; i < structureMappings.length; i++){
		if (structureMappings[i] instanceof MembraneMapping){
			if (((MembraneMapping)structureMappings[i]).getCalculateVoltage()){
				bCalculatePotential = true;
			}
		}
		
	}
	// If it does, VCell doesn't export it to SBML (no representation).
	if (bCalculatePotential) {
		throw new RuntimeException("This VCell model has Electrical mapping; cannot be exported to SBML at this time");
	}

	cbit.vcell.mapping.ReactionSpec[] vcReactionSpecs = getSelectedSimContext().getReactionContext().getReactionSpecs();
	for (int i = 0; i < vcReactionSpecs.length; i++){
		if (vcReactionSpecs[i].isExcluded()) {
			continue;
		}
		ReactionStep vcReactionStep = vcReactionSpecs[i].getReactionStep();
		//Create sbml reaction
		String rxnName = vcReactionStep.getName();
		org.sbml.libsbml.Reaction sbmlReaction = sbmlModel.createReaction();
		sbmlReaction.setId(org.vcell.util.TokenMangler.mangleToSName(rxnName));
		sbmlReaction.setName(rxnName);
			
		// If the reactionStep is a flux reaction, add the details to the annotation (structure, carrier valence, flux carrier, fluxOption, etc.)
		// If reactionStep is a simple reaction, add annotation to indicate the structure of reaction.
		// Useful when roundtripping ...
		Element sbmlImportRelatedElement = null;
		try {
			sbmlImportRelatedElement = getAnnotationElement(vcReactionStep);
		} catch (XmlParseException e1) {
			e1.printStackTrace(System.out);
//			throw new RuntimeException("Error ");
		}
		
		// Get annotation (RDF and non-RDF) for reactionStep from SBMLAnnotationUtils
		sbmlAnnotationUtil.writeAnnotation(vcReactionStep, sbmlReaction, sbmlImportRelatedElement);
		
		// Now set notes, 
		sbmlAnnotationUtil.writeNotes(vcReactionStep, sbmlReaction);
		
		// Get reaction kineticLaw
		Kinetics vcRxnKinetics = vcReactionStep.getKinetics();
		org.sbml.libsbml.KineticLaw sbmlKLaw = sbmlReaction.createKineticLaw();

		try {
			// Convert expression from kinetics rate parameter into MathML and use libSBMl utilities to convert it to formula
			// (instead of directly using rate parameter's expression infix) to maintain integrity of formula :
			// for example logical and inequalities are not handled gracefully by libSBMl if expression.infix is used.
			Expression correctedRateExpr = null; 
			if (vcRxnKinetics instanceof DistributedKinetics) {
				// If DistributedKinetics, multiply by compartmentSize & divide by KMOLE to convert to molecules/sec for feature reaction and membrane flux
				// for membrane reaction, we just need to multiply by compartmentSize to convert units to molecules/sec. 
				Compartment sbmlCompartment = sbmlModel.getCompartment(org.vcell.util.TokenMangler.mangleToSName(vcReactionStep.getStructure().getName()));
				correctedRateExpr = Expression.mult(((DistributedKinetics)vcRxnKinetics).getReactionRateParameter().getExpression(), new Expression(sbmlCompartment.getId()));
				if ( (vcReactionStep.getStructure() instanceof Feature) || ((vcReactionStep.getStructure() instanceof Membrane) && (vcReactionStep instanceof FluxReaction)) ) {
					correctedRateExpr = Expression.mult(correctedRateExpr, Expression.invert(new Expression("KMOLE")));
				}
			} else if (vcRxnKinetics instanceof LumpedKinetics) {
				// LumpedKinetics is already in molecules/sec, no need to do anything?
				correctedRateExpr = ((LumpedKinetics)vcRxnKinetics).getLumpedReactionRateParameter().getExpression();
			} else {
				throw new RuntimeException("Unknown kinetics type for reaction : " + vcReactionStep.getName());
			}
			
			// Adjust for species concentration - from SBML molecules/um3 -> VC uM
			correctedRateExpr = adjustSpeciesConcUnitsInRateExpr(correctedRateExpr, vcRxnKinetics);

			// Add parameters, if any, to the kineticLaw
			Kinetics.KineticsParameter[] vcKineticsParams = vcRxnKinetics.getKineticsParameters();

			// In the first pass thro' the kinetic params, store the non-numeric param names and expressions in arrays
			String[] kinParamNames = new String[vcKineticsParams.length];
			Expression[] kinParamExprs = new Expression[vcKineticsParams.length];
			for (int j = 0; j < vcKineticsParams.length; j++){
				if ( (vcKineticsParams[j].getRole() != Kinetics.ROLE_ReactionRate) && (vcKineticsParams[j].getRole() != Kinetics.ROLE_LumpedReactionRate) ) {
					// if expression of kinetic param does not evaluate to a double, the param value is defined by a rule. 
					// Since local reaction parameters cannot be defined by a rule, such parameters (with rules) are exported as global parameters.
					if ( (vcKineticsParams[j].getRole() == Kinetics.ROLE_CurrentDensity && (!vcKineticsParams[j].getExpression().isZero())) || 
							(vcKineticsParams[j].getRole() == Kinetics.ROLE_LumpedCurrent && (!vcKineticsParams[j].getExpression().isZero())) ) {
						throw new RuntimeException("Electric current not handled by SBML export; failed to export reaction \"" + vcReactionStep.getName() + "\" at this time");
					}
					if (!vcKineticsParams[j].getExpression().isNumeric()) {		// NON_NUMERIC KINETIC PARAM
						// Create new name for kinetic parameter and store it in kinParamNames, store corresponding exprs in kinParamExprs
						// Will be used later to add this param as global.
						String newParamName = TokenMangler.mangleToSName(vcKineticsParams[j].getName() + "_" + vcReactionStep.getName());
						kinParamNames[j] = newParamName;
						kinParamExprs[j] = new Expression(vcKineticsParams[j].getExpression());
					} 
				}
			} // end for (j) - first pass

			// Second pass - Check if any of the numeric parameters is present in any of the non-numeric param expressions
			// If so, these need to be added as global param (else the SBML doc will not be valid)
			for (int j = 0; j < vcKineticsParams.length; j++){
				if ( (vcKineticsParams[j].getRole() != Kinetics.ROLE_ReactionRate) && (vcKineticsParams[j].getRole() != Kinetics.ROLE_LumpedReactionRate) ) {
					// if expression of kinetic param evaluates to a double, the parameter value is set
					if ( (vcKineticsParams[j].getRole() == Kinetics.ROLE_CurrentDensity && (!vcKineticsParams[j].getExpression().isZero())) || 
							(vcKineticsParams[j].getRole() == Kinetics.ROLE_LumpedCurrent && (!vcKineticsParams[j].getExpression().isZero())) ) {
						throw new RuntimeException("Electric current not handled by SBML export; failed to export reaction \"" + vcReactionStep.getName() + "\" at this time");
					}
					if (vcKineticsParams[j].getExpression().isNumeric()) {		// NUMERIC KINETIC PARAM
						// check if it is used in other parameters that have expressions,
						boolean bAddedParam = false;
						String origParamName = vcKineticsParams[j].getName();
						String newParamName = TokenMangler.mangleToSName(origParamName + "_" + vcReactionStep.getName());
						for (int k = 0; k < vcKineticsParams.length; k++){
							if (kinParamExprs[k] != null) {
								// The param could be in the expression for any other param
								if (kinParamExprs[k].hasSymbol(origParamName)) {
									// if param is present in non-numeric param expression, this param has to be global
									// mangle its name to avoid conflict with other globals
									if (globalParamNamesHash.get(newParamName) == null) {
										globalParamNamesHash.put(newParamName, newParamName);
										org.sbml.libsbml.Parameter sbmlKinParam = sbmlModel.createParameter();
										sbmlKinParam.setId(newParamName);
										sbmlKinParam.setValue(vcKineticsParams[j].getConstantValue());
										// Set SBML units for sbmlParam using VC units from vcParam  
										if (!vcKineticsParams[j].getUnitDefinition().isTBD()) {
											sbmlKinParam.setUnits(TokenMangler.mangleToSName(vcKineticsParams[j].getUnitDefinition().getSymbol()));
										}
										bAddedParam = true;
									} else {
										// need to get another name for param and need to change all its refereces in the other kinParam euqations.
									}
									// update the expression to contain new name, since the globalparam has new name
									kinParamExprs[k].substituteInPlace(new Expression(origParamName), new Expression(newParamName));
								}
							} 
						}	// end for - k
						// If the param hasn't been added yet, it is definitely a local param. add it to kineticLaw now.
						if (!bAddedParam) {
							org.sbml.libsbml.Parameter sbmlKinParam = sbmlKLaw.createParameter();
							sbmlKinParam.setId(origParamName);
							sbmlKinParam.setValue(vcKineticsParams[j].getConstantValue());
							// Set SBML units for sbmlParam using VC units from vcParam  
							if (!vcKineticsParams[j].getUnitDefinition().isTBD()) {
								sbmlKinParam.setUnits(TokenMangler.mangleToSName(vcKineticsParams[j].getUnitDefinition().getSymbol()));
							}
						} else {
							// if parameter has been added to global param list, its name has been mangled, 
							// hence change its occurance in rate expression if it contains that param name
							if (correctedRateExpr.hasSymbol(origParamName)) {
								correctedRateExpr.substituteInPlace(new Expression(origParamName), new Expression(newParamName));
							}
						}
					}
				}
			} // end for (j) - second pass

			// In the third pass thro' the kinetic params, the expressions are changed to reflect the new names set above
			// (using the kinParamNames and kinParamExprs above) to ensure uniqueness in the global parameter names.
			for (int j = 0; j < vcKineticsParams.length; j++) {
				if ( ((vcKineticsParams[j].getRole() != Kinetics.ROLE_ReactionRate) && (vcKineticsParams[j].getRole() != Kinetics.ROLE_LumpedReactionRate)) && !(vcKineticsParams[j].getExpression().isNumeric())) {
					String oldName = vcKineticsParams[j].getName();
					String newName = kinParamNames[j];
					// change the name of this parameter in the rate expression 
					correctedRateExpr.substituteInPlace(new Expression(oldName), new Expression(newName));
					// Change the occurence of this param in other param expressions
					for (int k = 0; k < vcKineticsParams.length; k++){
						if ( ((vcKineticsParams[k].getRole() != Kinetics.ROLE_ReactionRate) && (vcKineticsParams[j].getRole() != Kinetics.ROLE_LumpedReactionRate)) && !(vcKineticsParams[k].getExpression().isNumeric())) {
							if (k != j && vcKineticsParams[k].getExpression().hasSymbol(oldName)) {
								// for all params except the current param represented by index j (whose name was changed)
								kinParamExprs[k].substituteInPlace(new Expression(oldName), new Expression(newName));
							}
							if (k == j && vcKineticsParams[k].getExpression().hasSymbol(oldName)) {
								throw new RuntimeException("A parameter cannot refer to itself in its expression");
							}
						}
					}	// end for - k
				} 
			}	// end for (j)  - third pass
			
			// In the fourth pass thro' the kinetic params, the non-numeric params are checked to see if they have any species in their
			// expressions - if so, they have to be adjusted for concentration units : from SBML molecules/um3 -> VC uM
			for (int j = 0; j < kinParamExprs.length; j++) {
				if (kinParamExprs[j] != null) {
					kinParamExprs[j] = adjustSpeciesConcUnitsInRateExpr(kinParamExprs[j], vcRxnKinetics);
				}
			}	// end for (j)  - fourth pass

					
			// In the fifth pass thro' the kinetic params, the non-numeric params are added to the global params of the model
			for (int j = 0; j < vcKineticsParams.length; j++){
				if (((vcKineticsParams[j].getRole() != Kinetics.ROLE_ReactionRate) && (vcKineticsParams[j].getRole() != Kinetics.ROLE_LumpedReactionRate)) && !(vcKineticsParams[j].getExpression().isNumeric())) {
					// Now, add this param to the globalParamNamesHash and add a global parameter to the sbmlModel
					String paramName = kinParamNames[j];
					if (globalParamNamesHash.get(paramName) == null) {
						globalParamNamesHash.put(paramName, paramName);
					} else {
						// need to get another name for param and need to change all its refereces in the other kinParam euqations.
					}
					ASTNode paramFormulaNode = getFormulaFromExpression(kinParamExprs[j]);
					AssignmentRule sbmlParamAssignmentRule = sbmlModel.createAssignmentRule();
					sbmlParamAssignmentRule.setVariable(paramName);
					sbmlParamAssignmentRule.setMath(paramFormulaNode);
					org.sbml.libsbml.Parameter sbmlKinParam = sbmlModel.createParameter();
					sbmlKinParam.setId(paramName);
					if (!vcKineticsParams[j].getUnitDefinition().isTBD()) {
						sbmlKinParam.setUnits(TokenMangler.mangleToSName(vcKineticsParams[j].getUnitDefinition().getSymbol()));
					}
					// Since the parameter is being specified by a Rule, its 'constant' field shoud be set to 'false' (default - true).
					sbmlKinParam.setConstant(false);
				}
			} // end for (j) - fifth pass

			// After making all necessary adjustments to the rate expression, now set the sbmlKLaw.
			ASTNode exprFormulaNode = getFormulaFromExpression(correctedRateExpr);
			sbmlKLaw.setMath(exprFormulaNode);
		} catch (cbit.vcell.parser.ExpressionException e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Error getting value of parameter : "+e.getMessage());
		}
		
		// Add kineticLaw to sbmlReaction - not needed now, since we use sbmlRxn.createKLaw() ??
		sbmlReaction.setKineticLaw(sbmlKLaw);
		
		// Add reactants, products, modifiers
		// Simple reactions have catalysts, fluxes have 'flux' 
		cbit.vcell.model.ReactionParticipant[] rxnParticipants = vcReactionStep.getReactionParticipants();
		for (int j = 0; j < rxnParticipants.length; j++){
			if (rxnParticipants[j] instanceof cbit.vcell.model.Reactant) {
				SpeciesReference reactantSpRef = sbmlReaction.createReactant();
				reactantSpRef.setSpecies(rxnParticipants[j].getSpeciesContext().getName());
				reactantSpRef.setStoichiometry(Double.parseDouble(Integer.toString(rxnParticipants[j].getStoichiometry())));
			} else if (rxnParticipants[j] instanceof cbit.vcell.model.Product) {
				SpeciesReference pdtSpRef = sbmlReaction.createProduct();
				pdtSpRef.setSpecies((rxnParticipants[j].getSpeciesContext().getName()));
				pdtSpRef.setStoichiometry(Double.parseDouble(Integer.toString(rxnParticipants[j].getStoichiometry())));
			} else if (rxnParticipants[j] instanceof cbit.vcell.model.Catalyst) {
				ModifierSpeciesReference modifierSpRef = sbmlReaction.createModifier();
				modifierSpRef.setSpecies(rxnParticipants[j].getSpeciesContext().getName());
			} else if (rxnParticipants[j] instanceof cbit.vcell.model.Flux) {
				// For a flux reaction, the reaction participants are 'fluxes'. The 'outside' participant is considered the reactant while
				// the 'inside' particpant is considered the product. Catalysts are modifiers.
				cbit.vcell.model.Flux flux = (cbit.vcell.model.Flux)rxnParticipants[j];
				if (vcReactionStep.getStructure() instanceof Membrane) {
					Membrane fluxMembrane = (Membrane)((cbit.vcell.model.FluxReaction)vcReactionStep).getStructure();
					if (fluxMembrane.getOutsideFeature().compareEqual(flux.getStructure())) {
						SpeciesReference reactantSpRef = sbmlReaction.createReactant();
						reactantSpRef.setSpecies(flux.getSpeciesContext().getName());
						reactantSpRef.setStoichiometry(Double.parseDouble(Integer.toString(flux.getStoichiometry())));
					} else if (fluxMembrane.getInsideFeature().compareEqual(flux.getStructure())) {
						SpeciesReference pdtSpRef = sbmlReaction.createProduct();
						pdtSpRef.setSpecies((rxnParticipants[j].getSpeciesContext().getName()));
						pdtSpRef.setStoichiometry(Double.parseDouble(Integer.toString(rxnParticipants[j].getStoichiometry())));
					}
				}
			}
		}

		if (vcReactionSpecs[i].isFast()) {
			sbmlReaction.setFast(true);
		}
		// delete used objects
		sbmlKLaw.delete();
		sbmlReaction.delete();
	}
}

/**
 * adjustSpeciesConcUnitsInRateExpr
 * @param origRateExpr
 * @return
 */
private Expression adjustSpeciesConcUnitsInRateExpr(Expression origRateExpr, Kinetics rxnKinetics) throws ExpressionException {
	// The expr for speciesConcentration from VCell is in terms of VCell units (uM); we have converted species units to SBML conc units
	// to export to SBML. We need to translate them back to VCell units within the expr; then we translate the VCell rate units into SBML units.

	Model vcModel = vcBioModel.getModel();
	ReservedSymbol kMole = vcModel.getKMOLE();
	String[] symbols = origRateExpr.getSymbols();
	if (symbols != null) {
	for (int i = 0; i < symbols.length; i++) {
		SpeciesContext vcSpeciesContext = vcModel.getSpeciesContext(symbols[i]);
		if (vcSpeciesContext != null) {
			// this change is applicable only to species in volumes, not on membranes, since the 
			// concentration units for species on membranes is already in molecules (/um2).
			org.sbml.libsbml.Species species = sbmlModel.getSpecies(vcSpeciesContext.getName());
			/* Check if species name is used as a kinetic parameter. If so, the parameter in the local namespace 
			   takes precedence. So ignore unit conversion for the species with the same name. */
			boolean bSpeciesNameFoundInLocalParamList = false;
			Kinetics.KineticsParameter[] kinParams = rxnKinetics.getKineticsParameters();
			for (int k = 0; k < kinParams.length; k++) {
				if ( (kinParams[k].getRole() != Kinetics.ROLE_ReactionRate) && (kinParams[k].getRole() != Kinetics.ROLE_LumpedReactionRate) ) {
					if (kinParams[k].getName().equals(species.getId())) {
						bSpeciesNameFoundInLocalParamList = true;
						break; 		// break out of kinParams loop
					}
				}
			}
			if (bSpeciesNameFoundInLocalParamList) {
				break;			// break out of speciesContexts loop
			}
			// Get the VC and SBML concentration units (from sbmlExportSpec) and get the conversion factor ('factor').
			// Replace the occurance of species in the rate expression with the new expr : species*factor.
			cbit.vcell.mapping.SpeciesContextSpec vcSpeciesContextsSpec = getSelectedSimContext().getReactionContext().getSpeciesContextSpec(vcSpeciesContext);
			VCUnitDefinition vcConcUnit = vcSpeciesContextsSpec.getInitialConditionParameter().getUnitDefinition();
			VCUnitDefinition sbmlConcUnits = sbmlExportSpec.getConcentrationUnit(vcSpeciesContext.getStructure().getDimension());
			SBMLUnitParameter sbmlUnitParam = SBMLUtils.getConcUnitFactor("spConcUnit", sbmlConcUnits, vcConcUnit, kMole);
			Expression modifiedSpExpr = Expression.mult(new Expression(species.getId()), sbmlUnitParam.getExpression()).flatten();
			origRateExpr.substituteInPlace(new Expression(species.getId()), modifiedSpExpr);
		}
	} 	// end for (i) - symbols
	}
	return origRateExpr;
}

/**
 * addSpecies comment.
 */
protected void addSpecies() {
	Model vcModel = vcBioModel.getModel();
	SpeciesContext[] vcSpeciesContexts = vcModel.getSpeciesContexts();
	ReservedSymbol kMole = vcModel.getKMOLE();
	for (int i = 0; i < vcSpeciesContexts.length; i++){
		org.sbml.libsbml.Species sbmlSpecies = sbmlModel.createSpecies();
		sbmlSpecies.setId(vcSpeciesContexts[i].getName());
		// Assuming that at this point, the compartment(s) for the model are already filled in.
		Compartment compartment = sbmlModel.getCompartment(TokenMangler.mangleToSName(vcSpeciesContexts[i].getStructure().getName()));
		if (compartment != null) {
			sbmlSpecies.setCompartment(compartment.getId());
		}

		// 'hasSubstanceOnly' field will be 'false', since VC deals only with initial concentrations and not initial amounts.
		sbmlSpecies.setHasOnlySubstanceUnits(false);

		// Get (and set) the initial concentration value

		if (getSelectedSimContext() == null) {
			throw new RuntimeException("No simcontext (application) specified; Cannot proceed.");
		}

		// Get the speciesContextSpec in the simContext corresponding to the 'speciesContext'; and extract its initial concentration value.
		SpeciesContextSpec vcSpeciesContextsSpec = getSelectedSimContext().getReactionContext().getSpeciesContextSpec(vcSpeciesContexts[i]);
		// since we are setting the substance units for species to 'molecule' or 'item', a unit that is originally in uM (or molecules/um2),
		// we need to convert concentration from uM -> molecules/um3; this can be achieved by dividing by KMOLE.
		VCUnitDefinition vcConcUnit = vcSpeciesContextsSpec.getInitialConditionParameter().getUnitDefinition();
		VCUnitDefinition sbmlConcUnits = sbmlExportSpec.getConcentrationUnit(vcSpeciesContexts[i].getStructure().getDimension());
		SBMLUnitParameter sbmlUnitParam = null;
		try {
			sbmlUnitParam = SBMLUtils.getConcUnitFactor("spConcUnit", vcConcUnit, sbmlConcUnits, kMole);
			Expression initConcExpr = Expression.mult(vcSpeciesContextsSpec.getInitialConditionParameter().getExpression(), sbmlUnitParam.getExpression()).flatten();
			sbmlSpecies.setInitialConcentration(initConcExpr.evaluateConstant());
		} catch (cbit.vcell.parser.ExpressionException e) {
			// If it is in the catch block, it means that the initial concentration of the species was not a double, but an expression, probably.
			// Check if the expression for the species is not null. If exporting to L2V1, and species is 'fixed', and if expr is not in terms of
			// x, y, z, or other species then create an assignment rule for species concentration; else throw exception.
			// If exporting to L2V3, if species concentration is not an expr with x, y, z or other species, add as InitialAssignment, else complain.
			if (vcSpeciesContextsSpec.getInitialConditionParameter().getExpression() != null) {
				try {
					sbmlUnitParam = SBMLUtils.getConcUnitFactor("spConcUnit", vcConcUnit, sbmlConcUnits, kMole);
					Expression initConcExpr = Expression.mult(vcSpeciesContextsSpec.getInitialConditionParameter().getExpression(), sbmlUnitParam.getExpression());
					if (sbmlLevel == 2 && sbmlVersion == 3) {
						// L2V3 - add expression as init assignment
						ASTNode initAssgnMathNode = getFormulaFromExpression(initConcExpr);
						InitialAssignment initAssignment = sbmlModel.createInitialAssignment();
						initAssignment.setSymbol(vcSpeciesContexts[i].getName());
						initAssignment.setMath(initAssgnMathNode);
					} else { 	// L2V1 (or L1V2 also??)
						// L2V1 (and L1V2?) and species is 'fixed' (constant), and not fn of x,y,z, other sp, add expr as assgn rule 
//						if (getBoundaryCondition(vcSpeciesContexts[i])) {
							ASTNode assgnRuleMathNode = getFormulaFromExpression(initConcExpr);
							AssignmentRule assgnRule = sbmlModel.createAssignmentRule();
							assgnRule.setVariable(vcSpeciesContexts[i].getName());
							assgnRule.setMath(assgnRuleMathNode);
//						} else {
//							throw new RuntimeException("Failed to export : Unable to add species " + vcSpeciesContexts[i].getName() + " to SBML model since its initial expression \'" + initConcExpr.infix() + "\'  is a function of x, y, z or contains another species in its expression.");
//						}
					}
				} catch (ExpressionException e1) {
					e.printStackTrace(System.out);
					throw new RuntimeException("Could not add concentration as Initial assignment for species : " + vcSpeciesContexts[i].getName());
				}
			}
		}

		// Get (and set) the boundary condition value
		boolean bBoundaryCondition = getBoundaryCondition(vcSpeciesContexts[i]);
		sbmlSpecies.setBoundaryCondition(bBoundaryCondition); 

		// set species substance units as 'molecules' - same as defined in the model; irrespective of it is in surface or volume.
		sbmlSpecies.setSubstanceUnits(sbmlExportSpec.getSubstanceUnits().getSymbol());

		// Add the common name of species to annotation, and add an annotation element to the species.
		// This is required later while trying to read in fluxes ...
		Element sbmlImportRelatedElement = new Element(XMLTags.VCellRelatedInfoTag, sbml_vcml_ns);
		Element speciesElement = new Element(XMLTags.SpeciesTag, sbml_vcml_ns);
		speciesElement.setAttribute(XMLTags.NameAttrTag, TokenMangler.mangleToSName(vcSpeciesContexts[i].getSpecies().getCommonName()));
		sbmlImportRelatedElement.addContent(speciesElement);
		
		// Get RDF annotation for species from SBMLAnnotationUtils
		sbmlAnnotationUtil.writeAnnotation(vcSpeciesContexts[i].getSpecies(), sbmlSpecies, sbmlImportRelatedElement);

		// Now set notes,
		sbmlAnnotationUtil.writeNotes(vcSpeciesContexts[i].getSpecies(), sbmlSpecies);
	}
}

/**
 * Add unit definitions to the model.
 */
protected void addUnitDefinitions() {
	// Define molecule - SUBSTANCE
	UnitDefinition unitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(sbmlExportSpec.getSubstanceUnits(), sbmlLevel, sbmlVersion);
	unitDefn.setId(SBMLUnitTranslator.SUBSTANCE);
	sbmlModel.addUnitDefinition(unitDefn);

	// Define um3 - VOLUME
	unitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(sbmlExportSpec.getVolumeUnits(), sbmlLevel, sbmlVersion);
	unitDefn.setId(SBMLUnitTranslator.VOLUME);
	sbmlModel.addUnitDefinition(unitDefn);

	// Define um2 - AREA
	unitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(sbmlExportSpec.getAreaUnits(), sbmlLevel, sbmlVersion);
	unitDefn.setId(SBMLUnitTranslator.AREA);
	sbmlModel.addUnitDefinition(unitDefn);

	// Redefine molecules as 'item' 
	unitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(VCUnitDefinition.UNIT_molecules, sbmlLevel, sbmlVersion);
	sbmlModel.addUnitDefinition(unitDefn);

	// Define umol.um3.L-1 - VCell (actual units of concentration, but with a multiplication factor.  Value = 1e-15 umol).
	unitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(VCUnitDefinition.UNIT_umol_um3_per_L, sbmlLevel, sbmlVersion);
	sbmlModel.addUnitDefinition(unitDefn);

	// Define um2 - VCell
	unitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(VCUnitDefinition.UNIT_um2, sbmlLevel, sbmlVersion);
	sbmlModel.addUnitDefinition(unitDefn);

	// Define KMOLE units : uM.um3/molecules - VCell (required in exported SBML for other tools).
	unitDefn = SBMLUnitTranslator.getSBMLUnitDefinition(VCUnitDefinition.UNIT_uM_um3_per_molecules, sbmlLevel, sbmlVersion);
	sbmlModel.addUnitDefinition(unitDefn);

	// Add units from paramater list in kinetics
	ArrayList<String> unitList = new ArrayList<String>();
	unitList.add(org.vcell.util.TokenMangler.mangleToSName(VCUnitDefinition.UNIT_molecules.getSymbol()));
	unitList.add(org.vcell.util.TokenMangler.mangleToSName(VCUnitDefinition.UNIT_umol_um3_per_L.getSymbol()));
	unitList.add(org.vcell.util.TokenMangler.mangleToSName(VCUnitDefinition.UNIT_um2.getSymbol()));
	addKineticAndGlobalParameterUnits(unitList);
}

protected void addEvents() throws ExpressionException {
	BioEvent[] vcBioevents = getSelectedSimContext().getBioEvents();
	
	if (vcBioevents != null) {
		Model vcModel = vcBioModel.getModel();
		ReservedSymbol kMole = vcModel.getKMOLE();
		for (int i = 0; i < vcBioevents.length; i++) {
			Event sbmlEvent = sbmlModel.createEvent();
			sbmlEvent.setId(vcBioevents[i].getName());
			// create trigger
			Trigger trigger = sbmlEvent.createTrigger();
			// if trigger expression has speciesContext in its list of symbols, need to multiply the unit conversion factor to SBML
			Expression triggerExpr = adjustSpeciesConcFactor(vcBioevents[i].getTriggerExpression());
			ASTNode math = getFormulaFromExpression(triggerExpr);
			trigger.setMath(math);
			
			// create delay
			if (vcBioevents[i].getDelay() != null) {
				Delay delay = sbmlEvent.createDelay();
				// if delay expression has speciesContext in its list of symbols, need to multiply the unit conversion factor to SBML
				Expression delayExpr = adjustSpeciesConcFactor(vcBioevents[i].getDelay().getDurationExpression());
				math = getFormulaFromExpression(delayExpr);
				delay.setMath(math);
			}
			
			// create eventAssignments
			ArrayList<EventAssignment> vcEventAssgns = vcBioevents[i].getEventAssignments();
			for (int j = 0; j < vcEventAssgns.size(); j++) {
				org.sbml.libsbml.EventAssignment sbmlEA = sbmlEvent.createEventAssignment();
				SymbolTableEntry target = vcEventAssgns.get(j).getTarget();
				sbmlEA.setVariable(target.getName());
				Expression eventAssgnExpr = new Expression(vcEventAssgns.get(j).getAssignmentExpression());
				
				// if event assignment expression has speciesContext in its list of symbols, need to multiply the unit conversion factor to SBML
				eventAssgnExpr = adjustSpeciesConcFactor(eventAssgnExpr);

				// if the event assignment variable is a speciesContext, need to convert the entire expression from VC units to SBML units
				if (target instanceof SpeciesContext) {
					SpeciesContext spContext = (SpeciesContext)target;
					SpeciesContextSpec vcSpeciesContextsSpec = getSelectedSimContext().getReactionContext().getSpeciesContextSpec(spContext);
					VCUnitDefinition vcConcUnit = vcSpeciesContextsSpec.getInitialConditionParameter().getUnitDefinition();
					VCUnitDefinition sbmlConcUnits = sbmlExportSpec.getConcentrationUnit(spContext.getStructure().getDimension());
					SBMLUnitParameter sbmlUnitParam = SBMLUtils.getConcUnitFactor("spConcUnit", vcConcUnit, sbmlConcUnits, kMole);
					eventAssgnExpr = Expression.mult(eventAssgnExpr, sbmlUnitParam.getExpression()).flatten();
				}
				math = getFormulaFromExpression(eventAssgnExpr);
				sbmlEA.setMath(math);
			}
		}
	}
}

private Expression adjustSpeciesConcFactor(Expression origExpr) throws ExpressionException {
	Expression expr = new Expression(origExpr);
	String[] symbols = expr.getSymbols();
	ReservedSymbol kMole = getSelectedSimContext().getModel().getKMOLE();
	for (int k = 0; symbols != null && k < symbols.length; k++) {
		SpeciesContext vcSpeciesContext = vcBioModel.getModel().getSpeciesContext(symbols[k]); 
		if (vcSpeciesContext != null) {
			Species species = sbmlModel.getSpecies(vcSpeciesContext.getName());
			SpeciesContextSpec vcSpeciesContextsSpec = getSelectedSimContext().getReactionContext().getSpeciesContextSpec(vcSpeciesContext);
			VCUnitDefinition vcConcUnit = vcSpeciesContextsSpec.getInitialConditionParameter().getUnitDefinition();
			VCUnitDefinition sbmlConcUnits = sbmlExportSpec.getConcentrationUnit(vcSpeciesContext.getStructure().getDimension());
			SBMLUnitParameter sbmlUnitParam = SBMLUtils.getConcUnitFactor("spConcUnit", sbmlConcUnits, vcConcUnit, kMole);
			Expression modifiedSpExpr = Expression.mult(new Expression(species.getId()), sbmlUnitParam.getExpression()).flatten();
			expr.substituteInPlace(new Expression(species.getId()), modifiedSpExpr);
		}
	}
	return expr;
}
/**
 * 	getAnnotationElement : 
 *	For a flux reaction, we need to add an annotation specifying the structure, flux carrier, carrier valence and fluxOption. 
 *  For a simple reaction, we need to add a annotation specifying the structure (useful for import)
 *  Using XML JDOM elements, so that it is convenient for libSBML setAnnotation (requires the annotation to be provided as an xml string).
 *
 **/
private Element getAnnotationElement(ReactionStep reactionStep) throws cbit.vcell.xml.XmlParseException {
	  		//"http://www.vcell.org/vcell";
//	Element annotationElement = new Element(XMLTags.SbmlAnnotationTag, "");

	Element sbmlImportRelatedElement = new Element(XMLTags.VCellRelatedInfoTag, sbml_vcml_ns);
	Element rxnElement = null;
	
	if (reactionStep instanceof FluxReaction) {
		FluxReaction fluxRxn = (FluxReaction)reactionStep;
		// Element for flux reaction. Write out the structure and flux carrier name.
		rxnElement = new Element(XMLTags.FluxStepTag, sbml_vcml_ns);
		rxnElement.setAttribute(XMLTags.StructureAttrTag, fluxRxn.getStructure().getName());
		if (fluxRxn.getFluxCarrier() != null) {
			rxnElement.setAttribute(XMLTags.FluxCarrierAttrTag, TokenMangler.mangleToSName(fluxRxn.getFluxCarrier().getCommonName()));
		}

		// Get the charge carrier valence (if its expression has constant value).
		Expression tempExp = null;
		int valence;
		try {
			tempExp = fluxRxn.getChargeCarrierValence().getExpression();
			double d = (int)tempExp.evaluateConstant();
			if ((int)d != d) {
				throw new XmlParseException("Invalid value for charge valence: " + d + " for reaction: " + fluxRxn.getName());
			}
			valence = (int)d;
		} catch (ExpressionException e) {
			e.printStackTrace();
			throw new XmlParseException("Invalid value for the charge valence: " + 
										(tempExp == null ? "null": tempExp.infix()) + " for reaction: " + fluxRxn.getName(), e);
		}
		rxnElement.setAttribute(XMLTags.FluxCarrierValenceAttrTag, String.valueOf(valence));

		// Get the physics option value.
		if (fluxRxn.getPhysicsOptions() == ReactionStep.PHYSICS_ELECTRICAL_ONLY){
			rxnElement.setAttribute(XMLTags.FluxOptionAttrTag, XMLTags.FluxOptionElectricalOnly);
		}else if (fluxRxn.getPhysicsOptions() == ReactionStep.PHYSICS_MOLECULAR_AND_ELECTRICAL){
			rxnElement.setAttribute(XMLTags.FluxOptionAttrTag, XMLTags.FluxOptionMolecularAndElectrical);
		}else if (fluxRxn.getPhysicsOptions() == ReactionStep.PHYSICS_MOLECULAR_ONLY){
			rxnElement.setAttribute(XMLTags.FluxOptionAttrTag, XMLTags.FluxOptionMolecularOnly);
		}

	} else if (reactionStep instanceof cbit.vcell.model.SimpleReaction) {
		// Element for a simple reaction - just store structure name - will be useful while importing.
		cbit.vcell.model.SimpleReaction simpleRxn = (cbit.vcell.model.SimpleReaction)reactionStep;
		rxnElement = new org.jdom.Element(cbit.vcell.xml.XMLTags.SimpleReactionTag, sbml_vcml_ns);
		rxnElement.setAttribute(cbit.vcell.xml.XMLTags.StructureAttrTag, simpleRxn.getStructure().getName());
	}

	// Add rate name as an element of annotation - this is especially useful when roundtripping VCell models, when the reaction
	// rate parameters have been renamed by user.
	Element rateElement = new Element(XMLTags.ReactionRateTag, sbml_vcml_ns);
	if (reactionStep.getKinetics() instanceof DistributedKinetics){
		rateElement.setAttribute(XMLTags.NameAttrTag, ((DistributedKinetics)reactionStep.getKinetics()).getReactionRateParameter().getName());
	}else if (reactionStep.getKinetics() instanceof LumpedKinetics){
		rateElement.setAttribute(XMLTags.NameAttrTag, ((LumpedKinetics)reactionStep.getKinetics()).getLumpedReactionRateParameter().getName());
	}else{
		throw new RuntimeException("unexpected kinetic type "+reactionStep.getKinetics().getClass().getName());
	}

	sbmlImportRelatedElement.addContent(rxnElement);
	sbmlImportRelatedElement.addContent(rateElement);
//	annotationElement.addContent(vcellInfoElement);
	
	return sbmlImportRelatedElement;
}


/**
 * 	getInitialConc : 
 */
private boolean getBoundaryCondition(SpeciesContext speciesContext) {

	// Get the simulationContext (application) that matches the 'vcPreferredSimContextName' field.
	cbit.vcell.mapping.SimulationContext simContext = getSelectedSimContext();
	if (simContext == null) {
		return false;
	}
	
	// Get the speciesContextSpec in the simContext corresponding to the 'speciesContext'; and extract its boundary condition value.
	cbit.vcell.mapping.SpeciesContextSpec[] vcSpeciesContextsSpecs = simContext.getReactionContext().getSpeciesContextSpecs();
	for (int i = 0; i < vcSpeciesContextsSpecs.length; i++){
		if (speciesContext.compareEqual(vcSpeciesContextsSpecs[i].getSpeciesContext())) {
			boolean bBoundaryCondition = vcSpeciesContextsSpecs[i].isConstant();
			return bBoundaryCondition;
		}
	}

	return false;
}


/**
 * 	getFormulaFromExpression : 
 *  Expression infix strings are not handled gracefully by libSBML, esp when ligical or inequality operators are used.
 *  This method 
 *		converts the expression into MathML using ExpressionMathMLPrinter;
 *		converts that into libSBMl-readable formula using libSBML utilties.
 *		returns the new formula string.
 *  
 */
public static ASTNode getFormulaFromExpression(Expression expression) { 
	// Convert expression into MathML string
	String expMathMLStr = null;

	try {
		expMathMLStr = cbit.vcell.parser.ExpressionMathMLPrinter.getMathML(expression, false);
	} catch (java.io.IOException e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Error converting expression to MathML string :" + e.getMessage());
	} catch (cbit.vcell.parser.ExpressionException e1) {
		e1.printStackTrace(System.out);
		throw new RuntimeException("Error converting expression to MathML string :" + e1.getMessage());
	}

	// Use libSBMl routines to convert MathML string to MathML document and a libSBML-readable formula string
	ASTNode mathNode = libsbml.readMathMLFromString(expMathMLStr);
	return mathNode.deepCopy();
}

//private SimulationContext getOverriddenSimContext() {
//	return vcOverridenSimContext;
//}

public SimulationContext getSelectedSimContext() {
	return vcSelectedSimContext;
}

private Simulation getSelectedSimulation() {
	if (vcSelectedSimJob == null) {
		return null;
	}
	return vcSelectedSimJob.getSimulation();
}


public String getSBMLFile() {

	// Create an SBMLDocument and create the sbmlModel from the document, so that other details can be added to it in translateBioModel()
	SBMLDocument sbmlDocument = new SBMLDocument(sbmlLevel,sbmlVersion);
	// If the chosen simulation is not null, the exported model's name should reflect it
	String modelName = vcBioModel.getName() + "_" + getSelectedSimContext().getName();  
	if (getSelectedSimulation() != null) {
		modelName += "_" + getSelectedSimulation().getName();
	}
	sbmlModel = sbmlDocument.createModel(TokenMangler.mangleToSName(modelName));
	sbmlModel.setName(modelName);

	translateBioModel();

	// include specific vcellInfo annotations
	Element sbmlImportRelatedElement = new Element(XMLTags.VCellRelatedInfoTag, sbml_vcml_ns);
	Element biomodelElement = new Element(XMLTags.BioModelTag, sbml_vcml_ns);
	biomodelElement.setAttribute(XMLTags.NameAttrTag, org.vcell.util.TokenMangler.mangleToSName(vcBioModel.getName())); 
	if (vcBioModel.getVersion() != null) {
		biomodelElement.setAttribute(XMLTags.KeyValueAttrTag, vcBioModel.getVersion().getVersionKey().toString());
	}
	sbmlImportRelatedElement.addContent(biomodelElement);
	Element simSpecElement = new Element(XMLTags.SimulationSpecTag, sbml_vcml_ns);
	simSpecElement.setAttribute(XMLTags.NameAttrTag, org.vcell.util.TokenMangler.mangleToSName(getSelectedSimContext().getName()));
	if (getSelectedSimContext().getVersion() != null) {
		simSpecElement.setAttribute(XMLTags.KeyValueAttrTag, getSelectedSimContext().getVersion().getVersionKey().toString());
	}
	sbmlImportRelatedElement.addContent(simSpecElement);
	if (getSelectedSimulation() != null) {
		Element simElement = new Element(XMLTags.SimulationTag, sbml_vcml_ns);
		simElement.setAttribute(XMLTags.NameAttrTag, org.vcell.util.TokenMangler.mangleToSName(getSelectedSimulation().getName()));
		if (getSelectedSimulation().getVersion() != null) {
			simElement.setAttribute(XMLTags.KeyValueAttrTag, getSelectedSimulation().getVersion().getVersionKey().toString());
		}
		sbmlImportRelatedElement.addContent(simElement);
	}

	// Get RDF annotation for species from SBMLAnnotationUtils
	sbmlAnnotationUtil.writeAnnotation(vcBioModel, sbmlModel, sbmlImportRelatedElement);
	
	// Now set notes, 
	sbmlAnnotationUtil.writeNotes(vcBioModel, sbmlModel);

	// write sbml document into sbml writer, so that the sbml str can be retrieved
	SBMLWriter sbmlWriter = new SBMLWriter();
	String sbmlStr = sbmlWriter.writeSBMLToString(sbmlDocument);
	
	// Error check - use libSBML's document.printError to print to outputstream
	System.out.println("\n\nSBML Export Error Report");
	OStringStream oStrStream = new OStringStream();
	sbmlDocument.printErrors(oStrStream);
	System.out.println(oStrStream.str());

	// cleanup
	sbmlModel.delete();
	sbmlDocument.delete();
	sbmlWriter.delete();	

	return sbmlStr;
}

//public void setOverriddenSimContext(SimulationContext newVcOverriddenSimContext) {
//	vcOverridenSimContext = newVcOverriddenSimContext;
//}

public void setSelectedSimContext(SimulationContext newVcSelectedSimContext) {
	vcSelectedSimContext = newVcSelectedSimContext;
}

public void setSelectedSimulationJob(SimulationJob newVcSelectedSimJob) {
	vcSelectedSimJob = newVcSelectedSimJob;
}

/**
 * VC_to_SB_Translator constructor comment.
 */
public void translateBioModel() {
	// 'Parse' the Virtual cell model into an SBML model
	addUnitDefinitions();
	// Add features/compartments
	addCompartments();
	// Add species/speciesContexts
	addSpecies(); 
	// Add Parameters
	try {
		addParameters();
	} catch (ExpressionException e) {
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
	// Add Reactions
	addReactions();
	// Add Events
	try {
		addEvents();
	} catch (ExpressionException e) {
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
}
}
