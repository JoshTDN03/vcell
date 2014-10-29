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
/*
 * Created on Feb 10, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author anu
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AdjacentDomains;
import org.sbml.libsbml.AdvectionCoefficient;
import org.sbml.libsbml.AlgebraicRule;
import org.sbml.libsbml.AnalyticGeometry;
import org.sbml.libsbml.AnalyticVolume;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.BoundaryCondition;
import org.sbml.libsbml.CSGeometry;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentMapping;
import org.sbml.libsbml.CoordinateComponent;
import org.sbml.libsbml.DiffusionCoefficient;
import org.sbml.libsbml.Domain;
import org.sbml.libsbml.DomainType;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ImageData;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.InteriorPoint;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfAnalyticVolumes;
import org.sbml.libsbml.ListOfCSGObjects;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfCoordinateComponents;
import org.sbml.libsbml.ListOfDomainTypes;
import org.sbml.libsbml.ListOfDomains;
import org.sbml.libsbml.ListOfEvents;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSampledVolumes;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.OStringStream;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.ParametricGeometry;
import org.sbml.libsbml.RateRule;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.RequiredElementsSBasePlugin;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.SampledField;
import org.sbml.libsbml.SampledFieldGeometry;
import org.sbml.libsbml.SampledVolume;
import org.sbml.libsbml.SpatialCompartmentPlugin;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.SpatialSpeciesRxnPlugin;
import org.sbml.libsbml.SpatialSymbolReference;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.XMLNamespaces;
import org.sbml.libsbml.libsbml;
import org.vcell.sbml.SBMLUtils;
import org.vcell.util.BeanUtils;
import org.vcell.util.Coordinate;
import org.vcell.util.Extent;
import org.vcell.util.ISize;
import org.vcell.util.Issue;
import org.vcell.util.Issue.IssueCategory;
import org.vcell.util.Issue.IssueSource;
import org.vcell.util.IssueContext;
import org.vcell.util.Origin;
import org.vcell.util.TokenMangler;
import org.vcell.util.document.BioModelChildSummary;

import cbit.image.VCImage;
import cbit.image.VCImageCompressed;
import cbit.image.VCImageUncompressed;
import cbit.image.VCPixelClass;
import cbit.util.xml.VCLogger;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.meta.VCMetaData;
import cbit.vcell.geometry.AnalyticSubVolume;
import cbit.vcell.geometry.CSGObject;
import cbit.vcell.geometry.CSGPrimitive.PrimitiveType;
import cbit.vcell.geometry.CSGSetOperator.OperatorType;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.GeometryClass;
import cbit.vcell.geometry.GeometrySpec;
import cbit.vcell.geometry.GeometryThumbnailImageFactoryAWT;
import cbit.vcell.geometry.ImageSubVolume;
import cbit.vcell.geometry.RegionImage.RegionInfo;
import cbit.vcell.geometry.SubVolume;
import cbit.vcell.geometry.SurfaceClass;
import cbit.vcell.geometry.surface.GeometricRegion;
import cbit.vcell.geometry.surface.GeometrySurfaceDescription;
import cbit.vcell.geometry.surface.SurfaceGeometricRegion;
import cbit.vcell.geometry.surface.VolumeGeometricRegion;
import cbit.vcell.mapping.BioEvent;
import cbit.vcell.mapping.BioEvent.Delay;
import cbit.vcell.mapping.BioEvent.EventAssignment;
import cbit.vcell.mapping.FeatureMapping;
import cbit.vcell.mapping.MembraneMapping;
import cbit.vcell.mapping.ReactionSpec;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.SpeciesContextSpec;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.math.BoundaryConditionType;
import cbit.vcell.model.Feature;
import cbit.vcell.model.FluxReaction;
import cbit.vcell.model.GeneralKinetics;
import cbit.vcell.model.GeneralLumpedKinetics;
import cbit.vcell.model.Kinetics;
import cbit.vcell.model.Kinetics.KineticsParameter;
import cbit.vcell.model.Kinetics.KineticsProxyParameter;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Model;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.model.Model.ReservedSymbol;
import cbit.vcell.model.ModelUnitSystem;
import cbit.vcell.model.Product;
import cbit.vcell.model.Reactant;
import cbit.vcell.model.ReactionParticipant;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.Species;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.model.StructureSorter;
import cbit.vcell.parser.AbstractNameScope;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionBindingException;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.ExpressionMathMLParser;
import cbit.vcell.parser.LambdaFunction;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.render.Vect3d;
import cbit.vcell.resource.NativeLib;
import cbit.vcell.units.VCUnitDefinition;
import cbit.vcell.units.VCUnitSystem;
import cbit.vcell.xml.XMLTags;

public class SBMLImporter {
	
	public static class SBMLIssueSource implements IssueSource {
		private final SBase issueSource;
		public SBMLIssueSource(SBase issueSource){
			this.issueSource = issueSource;
		}
		public SBase getSBase(){
			return issueSource;
		}
	}

	private long level = 2;
	//private long version = 3;
	
	private String sbmlFileName = null;
	private org.sbml.libsbml.Model sbmlModel = null;
	private SimulationContext simContext = null;
	private LambdaFunction[] lambdaFunctions = null;
	private BioModel vcBioModel = null;
	private HashMap<String, Expression> assignmentRulesHash = new HashMap<String, Expression>();
	private HashMap<String, Expression> rateRulesHash = new HashMap<String, Expression>();
	private HashMap<String, VCUnitDefinition> sbmlUnitIdentifierHash = new HashMap<String, VCUnitDefinition>();
//	private Hashtable<String, SBVCConcentrationUnits> speciesUnitsHash = new Hashtable<String, SBVCConcentrationUnits>();
	
	// issue list for medium-level warnings while importing 
	private Vector<Issue> localIssueList = new Vector<Issue>();
	private final IssueContext issueContext = new IssueContext();
	
	// is model spatial?
	private boolean bSpatial = false;

	private VCLogger logger = null;
	// for VCell specific annotation 
	private static String RATE_NAME = XMLTags.ReactionRateTag;
	private static String SPECIES_NAME = XMLTags.SpeciesTag;
	private static String REACTION = XMLTags.ReactionTag;
	private static String OUTSIDE_COMP_NAME = XMLTags.OutsideCompartmentTag;
	
	// For SBML geometry (definition) type
	public static int GEOM_OTHER = 0;
	public static int GEOM_ANALYTIC = 1;
	public static int GEOM_IMAGEBASED = 2;
	public static int GEOM_CSG = 3;
	
	// SBMLAnnotationUtil to get the SBML-related annotations, notes, free-text annotations from a Biomodel VCMetaData
	private SBMLAnnotationUtil sbmlAnnotationUtil = null;


	/* A lightweight inner class to contain the SBML and VC concentration units for species. Needed when running the Semantics test suite.
	 * When SBML model is imported into VCell and a simulation is run, the units of the generated results are different from
	 * units of results in SBML (usually a factor of 1e-6 for species concentration). In order to compare the two results, 
	 * we need to know the units in SBML (this is after importing to VCEll and running simulations, at which point we only know the 
	 * VCell units). Hence using this lookup that is stored in a hashTable, which is retrieved later to make the appropriate unit
	 * conversions before comparing the 2 results.  
	*/
	public static class SBVCConcentrationUnits {
		private VCUnitDefinition SBunits = null;
		private VCUnitDefinition VCunits = null;
		
		public SBVCConcentrationUnits(VCUnitDefinition argSBunits, VCUnitDefinition argVCunits) {
			this.SBunits = argSBunits;
			this.VCunits = argVCunits;
		}
		public VCUnitDefinition getSBConcentrationUnits() {
			return SBunits;			
		}
		public VCUnitDefinition getVCConcentrationUnits() {
			return VCunits;		
		}
	}
	static
	{
		NativeLib.SBML.load( );
	}
	
	/**
	 * helper class for {@link SBMLImporter#addGeometry()}
	 * sort by ordinal number of sort data, descending
	 */
	private static class CSGObjectSorter implements Comparable<CSGObjectSorter>{
		final CSGObject cSGObject;
		final Integer ordinal;
		public CSGObjectSorter(CSGObject cSGObject, int ordinal) {
			this.cSGObject = cSGObject;
			this.ordinal = ordinal;
		}
		
		/**
		 * reverse semantics of sort by multiplying ordinal compare to by -1
		 */
		@Override
		public int compareTo(CSGObjectSorter o) {
			return -1 * ordinal.compareTo(o.ordinal);
		}
	}
	
	private static Logger lg = Logger.getLogger(SBMLImporter.class);

	public SBMLImporter(String argSbmlFileName, VCLogger argVCLogger, boolean isSpatial) {
		super();
		this.sbmlFileName = argSbmlFileName;
		this.logger = argVCLogger;
		this.vcBioModel = new BioModel(null);
		this.bSpatial = isSpatial;
	}


	
protected void addCompartments(VCMetaData metaData) {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listofCompartments = sbmlModel.getListOfCompartments();
	if (listofCompartments == null) {
		throw new RuntimeException("Cannot have 0 compartments in model"); 
	}
	// Using a vector here - since there can be SBML models with only features and no membranes.
	// Hence keeping the datastructure flexible.
	Vector<Structure> structVector = new Vector<Structure>();
	java.util.HashMap<String, Structure> structureNameMap = new java.util.HashMap<String, Structure>();

	
	try {
		int structIndx = 0;
		// First pass - create the structures
		for (int i = 0; i < sbmlModel.getNumCompartments(); i++) {
			org.sbml.libsbml.Compartment compartment = (org.sbml.libsbml.Compartment)listofCompartments.get(i);
			String compartmentName = compartment.getId();
			if (compartment.getSpatialDimensions() == 3) {
				Feature feature = new Feature(compartmentName);
				structVector.insertElementAt(feature, structIndx);
				structureNameMap.put(compartmentName, feature);
			} else if (compartment.getSpatialDimensions() == 2) {
				Membrane membrane = new Membrane(compartmentName);
				structVector.insertElementAt(membrane, structIndx);
				structureNameMap.put(compartmentName, membrane);
			} else {
				logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.COMPARTMENT_ERROR, "Cannot deal with spatial dimension : " + compartment.getSpatialDimensions() + " for compartments at this time.");
				throw new RuntimeException("Cannot deal with spatial dimension : " + compartment.getSpatialDimensions() + " for compartments at this time");
			}
			structIndx++;
			sbmlAnnotationUtil.readAnnotation(structVector.get(i), compartment);
			sbmlAnnotationUtil.readNotes(structVector.get(i), compartment);
		}

		// Second pass - connect the structures
		Model model = simContext.getModel();
		for (int i = 0; i < sbmlModel.getNumCompartments(); i++) {
			org.sbml.libsbml.Compartment sbmlCompartment = (org.sbml.libsbml.Compartment)listofCompartments.get(i);
			String outsideCompartmentId = null;
			if (sbmlCompartment.getOutside() != null && sbmlCompartment.getOutside().length() > 0) {
				//compartment.getOutside returns the Sid of the 'outside' compartment, so get the compartment from model.
				outsideCompartmentId = sbmlCompartment.getOutside();
			} else {
				Element sbmlImportRelatedElement = sbmlAnnotationUtil.readVCellSpecificAnnotation(sbmlCompartment);
				if (sbmlImportRelatedElement != null) {
					Element embeddedVCellElement = sbmlImportRelatedElement.getChild(OUTSIDE_COMP_NAME, Namespace.getNamespace(SBMLUtils.SBML_VCELL_NS));
					if (embeddedVCellElement != null) {
						outsideCompartmentId = embeddedVCellElement.getAttributeValue(XMLTags.NameTag);
					}
				}
			}
			if (outsideCompartmentId != null) {
				Compartment outsideCompartment = sbmlModel.getCompartment(outsideCompartmentId);
				Structure outsideStructure = (Structure)structureNameMap.get(outsideCompartment.getId());
				Structure struct = (Structure)structureNameMap.get(sbmlCompartment.getId());
				struct.setSbmlParentStructure(outsideStructure);
			}
		}

		// set the structures in vc simContext
		Structure[] structures = (Structure[])BeanUtils.getArray(structVector, Structure.class);
		model.setStructures(structures);
		
		// Third pass thro' the list of compartments : set the sizes on the structureMappings - can be done only after setting 
		// the structures on simContext.
		boolean allSizesSet = true;
		for (int i = 0; i < sbmlModel.getNumCompartments(); i++) {
			org.sbml.libsbml.Compartment compartment = (org.sbml.libsbml.Compartment)listofCompartments.get(i);
			String compartmentName = compartment.getId();

			if (!compartment.isSetSize()) {
				// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, TranslationMessage.COMPARTMENT_ERROR, "compartment "+compartmentName+" size is not set in SBML document.");
				allSizesSet = false;
			} else {
				double size = compartment.getSize();
				// Check if size is specified by a rule
				Expression sizeExpr = getValueFromAssignmentRule(compartmentName);
				if (sizeExpr != null && !sizeExpr.isNumeric()) {
					// We are NOT handling compartment sizes with assignment rules/initial Assignments that are NON-numeric at this time ...
					logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.COMPARTMENT_ERROR, "compartment "+compartmentName+" size has an assignment rule which is not a numeric value, cannot handle it at this time.");
				}
				// check if sizeExpr is null - no assignment rule for size - check if it is specified by initial assignment
				if (sizeExpr == null) {
					InitialAssignment compInitAssgnment = sbmlModel.getInitialAssignment(compartmentName);
					if (compInitAssgnment != null) {
						sizeExpr = getExpressionFromFormula(compInitAssgnment.getMath());
					}
				}
				if (sizeExpr != null && !sizeExpr.isNumeric()) {
					// We are NOT handling compartment sizes with assignment rules/initial Assignments that are NON-numeric at this time ...
					logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.COMPARTMENT_ERROR, "compartment "+compartmentName+" size has an initial assignment which is not a numeric value, cannot handle it at this time.");
				}
				
				// no init assignment or assignment rule; create expression from 'size' attribute,  
				if (sizeExpr == null) {
					sizeExpr = new Expression(size);
				}
				
				// Now set the size of the compartment.
				Structure struct = model.getStructure(compartmentName);
				StructureMapping.StructureMappingParameter mappingParam = simContext.getGeometryContext().getStructureMapping(struct).getSizeParameter();
				mappingParam.setExpression(sizeExpr);
			}
		}

		// Handle the absolute size to surface_vol/volFraction conversion if size is set
		if (allSizesSet) {
			StructureSizeSolver.updateRelativeStructureSizes(simContext);
		}
	} catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Error adding Feature to vcModel " + e.getMessage());
	}
}

protected void addEvents() {
	if (sbmlModel.getNumEvents() > 0) {
		// VCell does not support events in spatial model 
		if (bSpatial) {
			throw new RuntimeException("Events are not supported in a spatial VCell model.");
		}
		
		ListOfEvents listofEvents = sbmlModel.getListOfEvents();

		Model vcModel = simContext.getModel();
		for (int i = 0; i < sbmlModel.getNumEvents(); i++){
			try {
				Event event = listofEvents.get(i);
				
				// trigger - adjust for species context and time conversion factors if necessary
				Expression triggerExpr = null;
				if (event.isSetTrigger()) {
					triggerExpr = getExpressionFromFormula(event.getTrigger().getMath());
					triggerExpr = adjustExpression(triggerExpr, vcModel);
				}
				
				// create bioevent
				String eventName = event.getId();
				if (eventName == null || eventName.length() == 0) {
					eventName = TokenMangler.mangleToSName(event.getName());
					// if event name is still null, get free event name from simContext.
					if  (eventName == null  || eventName.length() == 0) {
						eventName = simContext.getFreeEventName();
					}
				}
				
				//delay 
				BioEvent vcEvent = new BioEvent(eventName, triggerExpr, null, null, simContext);
				if (event.isSetDelay()) {
					Expression durationExpr = null;
					durationExpr = getExpressionFromFormula(event.getDelay().getMath());
					durationExpr = adjustExpression(durationExpr, vcModel);
					boolean bUseValsFromTriggerTime = true;
					/* TODO : This code is used when libSBML-4.1.0-b2 is used. On 3-29-10, version was switched back to 
					 * libSBML-4.0.1-b2, which does not support boolean 'bUseValueFromTriggerTime'. This code shall be reinstated
					 * when libSBML version is upgraded.
					 */ 
					if (event.isSetUseValuesFromTriggerTime()) {
						bUseValsFromTriggerTime = event.isSetUseValuesFromTriggerTime();
					} else {
						if (durationExpr != null && !durationExpr.isZero()) {
							bUseValsFromTriggerTime = false;
						}
					}
  					
					if (durationExpr != null && !durationExpr.isZero()) {
						bUseValsFromTriggerTime = false;
					}
					Delay vcDelay = vcEvent.new Delay(bUseValsFromTriggerTime, durationExpr);
					vcEvent.setDelay(vcDelay);
				}
				
				// event assignments
				ArrayList<EventAssignment> vcEvntAssgnList = new ArrayList<EventAssignment>();
				for (int j = 0; j < event.getNumEventAssignments(); j++) {
					org.sbml.libsbml.EventAssignment sbmlEvntAssgn = event.getEventAssignment(j);
					String varName = sbmlEvntAssgn.getVariable();
					SymbolTableEntry varSTE = simContext.getEntry(varName);
					if (varSTE != null) {
						Expression evntAssgnExpr = getExpressionFromFormula(sbmlEvntAssgn.getMath());
						evntAssgnExpr = adjustExpression(evntAssgnExpr, vcModel);
						EventAssignment vcEvntAssgn = vcEvent.new EventAssignment(varSTE, evntAssgnExpr);
						vcEvntAssgnList.add(vcEvntAssgn);
					} else {
						logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNSUPPORED_ELEMENTS_OR_ATTS, "No symbolTableEntry for '"+varName + "'; Cannot add event assignment.");
					}
				}
				
				vcEvent.setEventAssignmentsList(vcEvntAssgnList);
				vcEvent.bind();
				simContext.addBioEvent(vcEvent);
			} catch (Exception e) {
				e.printStackTrace(System.out);
				throw new RuntimeException(e.getMessage());
			}	// end - try/catch
		}	// end - for(sbmlEvents)
	}	// end - if numEvents > 0)
}

protected void addCompartmentTypes() {
	if (sbmlModel.getNumCompartmentTypes() > 0) {
		throw new RuntimeException("VCell doesn't support CompartmentTypes at this time");
	}
}

protected void addSpeciesTypes() {
	if (sbmlModel.getNumSpeciesTypes() > 0) {
		throw new RuntimeException("VCell doesn't support SpeciesTypes at this time");
	}
}

protected void addConstraints() {
	if (sbmlModel.getNumConstraints() > 0) {
		throw new RuntimeException("VCell doesn't support Constraints at this time");
	}
}

protected void addInitialAssignments() {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listofInitialAssgns = sbmlModel.getListOfInitialAssignments();
	if (listofInitialAssgns == null) {
		System.out.println("No Initial Assignments specified");
		return;
	}
	Model vcModel = simContext.getModel();

	for (int i = 0; i < sbmlModel.getNumInitialAssignments(); i++){
		try {
			InitialAssignment initAssgn = (InitialAssignment)listofInitialAssgns.get(i);
			String initAssgnSymbol = initAssgn.getSymbol();
			Expression initAssignMathExpr = getExpressionFromFormula(initAssgn.getMath());
			// if initial assignment is for a compartment, VCell doesn't support compartmentSize expressions, warn and bail out.
			if (sbmlModel.getCompartment(initAssgnSymbol) != null) {
				if (!initAssignMathExpr.isNumeric()) {
					logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.COMPARTMENT_ERROR, "compartment '"+initAssgnSymbol+"' size has an initial assignment, cannot handle it at this time.");
				} 
				// if init assgn for compartment is numeric, the numeric value for size is set in addCompartments().
			}
			// Check if init assgn expr for a species is in terms of x,y,z or other species. Not allowed for species.
			if ( !bSpatial && sbmlModel.getSpecies(initAssgnSymbol) != null) {
				if (initAssignMathExpr.hasSymbol(vcModel.getX().getName()) || 
					initAssignMathExpr.hasSymbol(vcModel.getY().getName()) ||
					initAssignMathExpr.hasSymbol(vcModel.getZ().getName()) ) {
					logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.SPECIES_ERROR, "species '"+initAssgnSymbol+"' initial assignment expression cannot contain 'x', 'y', 'z'.");
				}
			}
			
			initAssignMathExpr = adjustExpression(initAssignMathExpr, vcModel);
			// set the init assgn expr on VCell species init condn or global parameter expression
			SpeciesContextSpec scs = simContext.getReactionContext().getSpeciesContextSpec(simContext.getModel().getSpeciesContext(initAssgnSymbol));
			ModelParameter mp = simContext.getModel().getModelParameter(initAssgnSymbol);
			if (scs != null) {
				scs.getInitialConditionParameter().setExpression(initAssignMathExpr);
			} else if (mp != null) {
				mp.setExpression(initAssignMathExpr);
			} else {
				localIssueList.add(new Issue(new SBMLIssueSource(initAssgn), issueContext, IssueCategory.SBMLImport_UnsupportedAttributeOrElement , "Symbol '"+initAssgnSymbol+"' not a species or global parameter in VCell; initial assignment ignored.", Issue.SEVERITY_WARNING));
				// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNSUPPORED_ELEMENTS_OR_ATTS, "Symbol '"+initAssgnSymbol+"' not a species or global parameter in VCell; initial assignment ignored..");
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Error reading InitialAssignment : " + e.getMessage()); 
		}
	}
}

protected void addFunctionDefinitions() {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listofFunctionDefinitions = sbmlModel.getListOfFunctionDefinitions();
	if (listofFunctionDefinitions == null) {
		System.out.println("No Function Definitions");
		return;
	}
	// The function definitions contain lambda function definition.
	// Each lambda function has a name, (list of) argument(s), function body which is represented as a math element.
	lambdaFunctions = new LambdaFunction[(int)sbmlModel.getNumFunctionDefinitions()];
	try {
		for (int i = 0; i < sbmlModel.getNumFunctionDefinitions(); i++) {
			FunctionDefinition fnDefn = (FunctionDefinition)listofFunctionDefinitions.get(i);
			String functionName = new String(fnDefn.getId());
			ASTNode math = null;
			Vector<String> argsVector = new Vector<String>();
			String[] functionArgs = null;
			
			if (fnDefn.isSetMath()) {
				math = fnDefn.getMath();
				// Function body. 
				if (math.getNumChildren() == 0) {
					System.out.println("(no function body defined)");
					continue;
				}
				// Add function arguments into vector, print args 
				// Note that lambda function always should have at least 2 children
				for (long j = 0; j < math.getNumChildren() - 1; ++j) {
					argsVector.addElement(new String(math.getChild(j).getName()));
				}
			
				functionArgs = argsVector.toArray(new String[0]);
				
				math = math.getChild(math.getNumChildren() - 1);
				// formula = libsbml.formulaToString(math);
				
				Expression fnExpr = getExpressionFromFormula(math);
				lambdaFunctions[i] = new LambdaFunction(functionName, fnExpr, functionArgs);
			}
		}
	} catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Error adding Lambda function" + e.getMessage());
	}
}

/**
 * addParameters : 
 * 		Adds global parameters from SBML model to VCell model. 
 * 		If expression for global parameter contains species, creates a conc_factor parameter (conversion from SBML - VCell conc units)
 * 		and adds this factor to VC global params list, and replaces occurances of 'sp' with 'sp*concFactor' in original param expression.
 * 
 * @throws PropertyVetoException
 */

protected void addParameters() throws Exception {
	ListOf listofGlobalParams = sbmlModel.getListOfParameters();
	if (listofGlobalParams == null) {
		System.out.println("No Global Parameters");
		return;
	}
	Model vcModel = simContext.getModel();
	ArrayList<ModelParameter> vcModelParamsList = new ArrayList<Model.ModelParameter>();
	
	// create a hash of reserved symbols so that if there is any reserved symbol occurring as a global parameter in the SBML model,
	// the hash can be used to check for reserved symbols, so that it will not be added as a global parameter in VCell, 
	// since reserved symbols cannot be used as other variables (species, structureSize, parameters, reactions, etc.).
	HashSet<String> reservedSymbolHash = new HashSet<String>();
	for (ReservedSymbol rs : vcModel.getReservedSymbols()) {
		reservedSymbolHash.add(rs.getName());
	}
	
	ModelUnitSystem modelUnitSystem = vcModel.getUnitSystem();
	
	// needed to ascertain the boundary condition type (later, when processing the SBML parameters for boundary condition) 
	CoordinateComponent ccX = null; 
	CoordinateComponent ccY = null;
	CoordinateComponent ccZ = null;
	if (bSpatial) {
		SpatialModelPlugin mplugin = (SpatialModelPlugin)sbmlModel.getPlugin(SBMLUtils.SBML_SPATIAL_NS_PREFIX);
		if (mplugin != null) {
			org.sbml.libsbml.Geometry sbmlGeometry = mplugin.getGeometry();
			ccX = sbmlGeometry.getCoordinateComponent(vcModel.getX().getName());
			ccY = sbmlGeometry.getCoordinateComponent(vcModel.getY().getName());
			ccZ = sbmlGeometry.getCoordinateComponent(vcModel.getZ().getName());
		}
	}
	
	for (int i = 0; i < sbmlModel.getNumParameters(); i++){
		Parameter sbmlGlobalParam = (Parameter)listofGlobalParams.get(i);
		String paramName = sbmlGlobalParam.getId();

		SpatialParameterPlugin spplugin = null;
		if (bSpatial) {
			// check if parameter id is x/y/z : if so, check if its 'spatialSymbolRef' child's spatial id and type are non-empty.
			// If so, the parameter represents a spatial element.
			// If not, throw an exception, since a parameter that does not represent a spatial element cannot have an id of x/y/z
			
			// (a) the requiredElements attributes should be 'spatial'
			boolean bSpatialParam = false;
			RequiredElementsSBasePlugin reqPlugin = (RequiredElementsSBasePlugin)sbmlGlobalParam.getPlugin(SBMLUtils.SBML_REQUIREDELEMENTS_NS_PREFIX);
			if (reqPlugin != null) {
				if (reqPlugin.getMathOverridden().equals(SBMLUtils.SBML_SPATIAL_NS_PREFIX)) {
					bSpatialParam = true;
				}
				spplugin = (SpatialParameterPlugin)sbmlGlobalParam.getPlugin(SBMLUtils.SBML_SPATIAL_NS_PREFIX);
				if (paramName.equals("x") || paramName.equals("y") || paramName.equals("z")) {
					// (b) and the spatialSymbolRef (splatialParamPlugin) attributes should be non-empty
					SpatialSymbolReference spSymRef = spplugin.getSpatialSymbolReference();
					if (spSymRef.isSetSpatialId() && spSymRef.isSetType()) {
						bSpatialParam = bSpatialParam && true;
					}
					
					// if (a) and (b) are not true, for param with id x/y/z, throw exception - not allowed.
					// if (a) and (b) are true, continue with the next parameter
					if (!bSpatialParam) {
						throw new RuntimeException("Parameter '" + paramName +
							"' is not a spatial parameter : Cannot have a variable in VCell named '" + 
							paramName + "' unless it is a spatial variable.");
					} else {
						// go to the next parameter - do not add the spatial parameter to the list of vcell parameters.
						continue;
					}
				}
			}
		}
		
		//
		// Get param value if set or get its expression from rule
		//
		
		// Check if param is defined by an assignment rule or initial assignment. If so, that value overrides the value existing in the param element.
		// assignment rule, first
		Expression valueExpr = getValueFromAssignmentRule(paramName);
		if (valueExpr == null) {
			if (sbmlGlobalParam.isSetValue()) {
				double value = sbmlGlobalParam.getValue();
				valueExpr = new Expression(value);
			} else {
				// if value for global param is not set and param has a rate rule, need to set an init value for param (else, there will be a problem in reaction which uses this parameter).
				// use a 'default' initial value of '0'
				valueExpr = new Expression(0.0);
				// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.LOW_PRIORITY, "Parameter did not have an initial value, but has a rate rule specified. Using a default value of 0.0.");
			}
		}

		if (valueExpr != null) {
			// valueExpr will be changed
			valueExpr = adjustExpression(valueExpr, vcModel);
		}

		// if SBML model is spatial, check if param represents species diffusion/advection/boundary condition parameters for 'spatial' extension
		if (bSpatial) {
			try {
				SpeciesContext paramSpContext = null;
				SpeciesContextSpec vcSpContextsSpec = null;
				// Check for diffusion coefficient(s)
				DiffusionCoefficient diffCoeff = spplugin.getDiffusionCoefficient();
				if (diffCoeff.isSetVariable()) {
					// get the var of diffCoeff; find appropriate spContext in vcell; set its diff param to param value. 
					paramSpContext = vcModel.getSpeciesContext(diffCoeff.getVariable());
					if (paramSpContext != null) {
						vcSpContextsSpec = simContext.getReactionContext().getSpeciesContextSpec(paramSpContext);
						vcSpContextsSpec.getDiffusionParameter().setExpression(valueExpr);
					}
					// go to the next parameter - do not add the diffusion coeff parameter to the list of vcell parameters.
					continue;
				}
				
				// Check for advection coefficient(s)
				AdvectionCoefficient advCoeff = spplugin.getAdvectionCoefficient();
				if (advCoeff.isSetVariable()) {
					// get the var of advCoeff; find appropriate spContext in vcell; set its adv param to param value. 
					paramSpContext = vcModel.getSpeciesContext(advCoeff.getVariable());
					if (paramSpContext != null) {
						vcSpContextsSpec = simContext.getReactionContext().getSpeciesContextSpec(paramSpContext);
						long coord = advCoeff.getCoordinateIndex();
						if (coord == 0) {
							vcSpContextsSpec.getVelocityXParameter().setExpression(valueExpr);
						} else if (coord == 1) {
							vcSpContextsSpec.getVelocityYParameter().setExpression(valueExpr);
						} else if (coord == 2) {
							vcSpContextsSpec.getVelocityZParameter().setExpression(valueExpr);
						}
					}
					// go to the next parameter - do not add the advection coeff parameter to the list of vcell parameters.
					continue;
				} 
	
				// Check for Boundary condition(s)
				BoundaryCondition bCondn = spplugin.getBoundaryCondition();
				if (bCondn.isSetVariable()) {
					// get the var of boundaryCondn; find appropriate spContext in vcell; 
					// set the BC param of its speciesContextSpec to param value. 
					paramSpContext = vcModel.getSpeciesContext(bCondn.getVariable());
					if (paramSpContext != null) {
						StructureMapping sm = simContext.getGeometryContext().getStructureMapping(paramSpContext.getStructure());
						vcSpContextsSpec = simContext.getReactionContext().getSpeciesContextSpec(paramSpContext);
						if (bCondn.getCoordinateBoundary().equals(ccX.getBoundaryMin().getSpatialId())) {
							// if type from SBML parameter Boundary Condn is not the same as the boundary type of the 
							// structureMapping of structure of paramSpContext, set the boundary condn type of the structureMapping
							// to the value of 'type' from SBML parameter Boundary Condn. 
							String bcXmType = bCondn.getType();
							if (!sm.getBoundaryConditionTypeXm().boundaryTypeStringValue().equals(bcXmType)) {
								sm.setBoundaryConditionTypeXm(new BoundaryConditionType(bcXmType));
							}
							// set expression for boundary condition in speciesContextSpec
							vcSpContextsSpec.getBoundaryXmParameter().setExpression(valueExpr);
						} else if (bCondn.getCoordinateBoundary().equals(ccX.getBoundaryMax().getSpatialId())) {
							String bcXpType = bCondn.getType();
							if (!sm.getBoundaryConditionTypeXp().boundaryTypeStringValue().equals(bcXpType)) {
								sm.setBoundaryConditionTypeXp(new BoundaryConditionType(bcXpType));
							}
							vcSpContextsSpec.getBoundaryXpParameter().setExpression(valueExpr);
						} else if (bCondn.getCoordinateBoundary().equals(ccY.getBoundaryMin().getSpatialId())) {
							String bcYmType = bCondn.getType();
							if (!sm.getBoundaryConditionTypeYm().boundaryTypeStringValue().equals(bcYmType)) {
								sm.setBoundaryConditionTypeYm(new BoundaryConditionType(bcYmType));
							}
							vcSpContextsSpec.getBoundaryYmParameter().setExpression(valueExpr);
						} else if (bCondn.getCoordinateBoundary().equals(ccY.getBoundaryMax().getSpatialId())) {
							String bcYpType = bCondn.getType();
							if (!sm.getBoundaryConditionTypeYp().boundaryTypeStringValue().equals(bcYpType)) {
								sm.setBoundaryConditionTypeYp(new BoundaryConditionType(bcYpType));
							}
							vcSpContextsSpec.getBoundaryYpParameter().setExpression(valueExpr);
						} else if (bCondn.getCoordinateBoundary().equals(ccZ.getBoundaryMin().getSpatialId())) {
							String bcZmType = bCondn.getType();
							if (!sm.getBoundaryConditionTypeZm().boundaryTypeStringValue().equals(bcZmType)) {
								sm.setBoundaryConditionTypeZm(new BoundaryConditionType(bcZmType));
							}
							vcSpContextsSpec.getBoundaryZmParameter().setExpression(valueExpr);
						} else if (bCondn.getCoordinateBoundary().equals(ccZ.getBoundaryMax().getSpatialId())) {
							String bcZpType = bCondn.getType();
							if (!sm.getBoundaryConditionTypeZp().boundaryTypeStringValue().equals(bcZpType)) {
								sm.setBoundaryConditionTypeZp(new BoundaryConditionType(bcZpType));
							}
							vcSpContextsSpec.getBoundaryZpParameter().setExpression(valueExpr);
						}
					}
					// go to the next parameter - do not add the advection coeff parameter to the list of vcell parameters.
					continue;
				}
			} catch (ExpressionBindingException e) {
				e.printStackTrace(System.out);
			}
		}

		
		// Finally, create and add model parameter to VC model if it already doesn't exist.
		if (vcModel.getModelParameter(paramName) == null) {
			VCUnitDefinition glParamUnitDefn = sbmlUnitIdentifierHash.get(sbmlGlobalParam.getUnits());
			// if units for param were not defined, don't let it be null; set it to TBD or check if it was dimensionless.
			if (glParamUnitDefn == null) {
				glParamUnitDefn = modelUnitSystem.getInstance_TBD();
			}
			// Also check if the SBML global param is a reserved symbol in VCell : cannot add reserved symbol to model params.
			if (!reservedSymbolHash.contains(paramName)) {
				ModelParameter vcGlobalParam = vcModel.new ModelParameter(paramName, valueExpr, Model.ROLE_UserDefined, glParamUnitDefn);
				if (paramName.length() > 64) {
					// record global parameter name in annotation if it is longer than 64 characeters
					vcGlobalParam.setDescription("Parameter Name : " + paramName);
				}
				vcModelParamsList.add(vcGlobalParam);
			}
		}
	}	// end for - sbmlModel.parameters
	vcModel.setModelParameters(vcModelParamsList.toArray(new ModelParameter[0]));
}

/**
 * @param spConcFactorInModelParamsList
 * @param valueExpr
 * @param vcModel
 * @param vcSpContexts
 * @throws PropertyVetoException
 */
private Expression adjustExpression(Expression valueExpr, Model model) throws PropertyVetoException {
	Expression adjustedExpr = new Expression(valueExpr);
	// ************* TIME CONV_FACTOR if 'time' is present in global parameter expression
	// If time 't' is present in the global expression, it is in VC units (secs), convert it back to SBML units
	// hence, we take the inverse of the time factor (getSBMLTimeUnitsFactor() converts from SBML to VC units)

	/** ---- FOR NOW, IGNORE TIME UNIT CONVERSION ----  */
	// adjustedExpr = adjustTimeConvFactor(model, adjustedExpr);
	
	return adjustedExpr;
}


/**
 *  addReactionParticipant :
 *		Adds reactants and products and modifiers to a reaction.
 *		Input args are the sbml reaction, vc reaction
 *		This method was created mainly to handle reactions where there are reactants and/or products that appear multiple times in a reaction.
 *		Virtual Cell now allows the import of such reactions.
 *		
**/
private void addReactionParticipants(org.sbml.libsbml.Reaction sbmlRxn, ReactionStep vcRxn) throws Exception {
	Model vcModel = simContext.getModel();

	if (!(vcRxn instanceof FluxReaction)) {
		// reactants in sbmlRxn
		HashMap<String, Integer> sbmlReactantsHash = new HashMap<String, Integer>();
		for (int j = 0; j < (int)sbmlRxn.getNumReactants(); j++){
			SpeciesReference spRef = sbmlRxn.getReactant(j);
			String sbmlReactantSpId = spRef.getSpecies();
			if (sbmlModel.getSpecies(sbmlReactantSpId) != null) {		// check if spRef is in sbml model
				// If stoichiometry of speciesRef is not an integer, it is not handled in the VCell at this time; no point going further
				double stoichiometry = 0.0;
				if (level < 3) {	// for SBML models < L3, default stoichiometry is 1, if field is not set.
					stoichiometry = 1.0; 		// default value of stoichiometry, if not set.
					if (spRef.isSetStoichiometry()) {
						stoichiometry = spRef.getStoichiometry();
						if ( ((int)stoichiometry != stoichiometry) || spRef.isSetStoichiometryMath()) {
							throw new RuntimeException("Non-integer stoichiometry ('" + stoichiometry + "' for reactant '" + sbmlReactantSpId + "' in reaction '" + sbmlRxn.getId() + "') or stoichiometryMath not handled in VCell at this time.");
							// logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.REACTION_ERROR, "Non-integer stoichiometry or stoichiometryMath not handled in VCell at this time.");							
						}
					} 
				} else {
					if (spRef.isSetStoichiometry()) {
						stoichiometry = spRef.getStoichiometry();
						if ( ((int)stoichiometry != stoichiometry) || spRef.isSetStoichiometryMath()) {
							throw new RuntimeException("Non-integer stoichiometry ('" + stoichiometry + "' for reactant '" + sbmlReactantSpId + "' in reaction '" + sbmlRxn.getId() + "') or stoichiometryMath not handled in VCell at this time.");
							// logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.REACTION_ERROR, "Non-integer stoichiometry or stoichiometryMath not handled in VCell at this time.");							
						}
					} else {
						throw new RuntimeException("This is a SBML level 3 model, stoichiometry is not set for the reactant '" + sbmlReactantSpId + "' and no default value can be assumed.");
						// logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.REACTION_ERROR, "This is a SBML level 3 model, stoichiometry is not set for the reactant '" + spRef.getSpecies() + "' and no default value can be assumed.");						
					}
				}
				
				if (sbmlReactantsHash.get(sbmlReactantSpId) == null) {
					// if sbmlReactantSpId is NOT in sbmlReactantsHash, add it with its stoichiometry
					sbmlReactantsHash.put(sbmlReactantSpId, Integer.valueOf((int)stoichiometry));
				} else {
					// if sbmlReactantSpId IS in sbmlReactantsHash, update its stoichiometry value to (existing-from-hash + stoichiometry) and put it back in hash  
					int intStoich = sbmlReactantsHash.get(sbmlReactantSpId).intValue();
					intStoich += (int)stoichiometry;
					sbmlReactantsHash.put(sbmlReactantSpId, Integer.valueOf(intStoich));
				}
			} else {
				// spRef is not in model, throw exception
				throw new RuntimeException("Reactant '" + sbmlReactantSpId + "' in reaction '" + sbmlRxn.getId() + "' not found as species in SBML model.");
			}	// end - if (spRef is species in model)
		}	// end - for reactants
		
		// now add the reactants for the sbml reaction from sbmlReactionParticipantsHash as reactants to vcRxn
		Iterator<String> sbmlReactantsIter = sbmlReactantsHash.keySet().iterator();
		while (sbmlReactantsIter.hasNext()) {
			String sbmlReactantStr = sbmlReactantsIter.next();
			SpeciesContext speciesContext = vcModel.getSpeciesContext(sbmlReactantStr);
			int stoich = sbmlReactantsHash.get(sbmlReactantStr).intValue();
			((SimpleReaction)vcRxn).addReactant(speciesContext, stoich);
		}
	
		// products in sbmlRxn
		HashMap<String, Integer> sbmlProductsHash = new HashMap<String, Integer>();
		for (int j = 0; j < (int)sbmlRxn.getNumProducts(); j++){
			SpeciesReference spRef = sbmlRxn.getProduct(j);
			String sbmlProductSpId = spRef.getSpecies();
			if (sbmlModel.getSpecies(sbmlProductSpId) != null) {		// check if spRef is in sbml model
				// If stoichiometry of speciesRef is not an integer, it is not handled in the VCell at this time; no point going further
				double stoichiometry = 0.0;
				if (level < 3) {	// for sBML models < L3, default stoichiometry is 1, if field is not set.
					stoichiometry = 1.0; 		// default value of stoichiometry, if not set.
					if (spRef.isSetStoichiometry()) {
						stoichiometry = spRef.getStoichiometry();
						if ( ((int)stoichiometry != stoichiometry) || spRef.isSetStoichiometryMath()) {
							throw new RuntimeException("Non-integer stoichiometry ('" + stoichiometry + "' for product '" + sbmlProductSpId + "' in reaction '" + sbmlRxn.getId() + "') or stoichiometryMath not handled in VCell at this time.");
							// logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.REACTION_ERROR, "Non-integer stoichiometry or stoichiometryMath not handled in VCell at this time.");							
						}
					} 
				} else {
					if (spRef.isSetStoichiometry()) {
						stoichiometry = spRef.getStoichiometry();
						if ( ((int)stoichiometry != stoichiometry) || spRef.isSetStoichiometryMath()) {
							throw new RuntimeException("Non-integer stoichiometry ('" + stoichiometry + "' for product '" + sbmlProductSpId + "' in reaction '" + sbmlRxn.getId() + "') or stoichiometryMath not handled in VCell at this time.");
							// logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.REACTION_ERROR, "Non-integer stoichiometry or stoichiometryMath not handled in VCell at this time.");							
						}
					} else {
						throw new RuntimeException("This is a SBML level 3 model, stoichiometry is not set for the product '" + sbmlProductSpId + "' and no default value can be assumed.");
						// logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.REACTION_ERROR, "This is a SBML level 3 model, stoichiometry is not set for the product '" + spRef.getSpecies() + "' and no default value can be assumed.");						
					}
				}

				if (sbmlProductsHash.get(sbmlProductSpId) == null) {
					// if sbmlProductSpId is NOT in sbmlProductsHash, add it with its stoichiometry
					sbmlProductsHash.put(sbmlProductSpId, Integer.valueOf((int)stoichiometry));
				} else {
					// if sbmlProductSpId IS in sbmlProductsHash, update its stoichiometry value to (existing-value-from-hash + stoichiometry) and put it back in hash  
					int intStoich = sbmlProductsHash.get(sbmlProductSpId).intValue();
					intStoich += (int)stoichiometry;
					sbmlProductsHash.put(sbmlProductSpId, Integer.valueOf(intStoich));
				}
			} else {
				// spRef is not in model, throw exception
				throw new RuntimeException("Product '" + sbmlProductSpId + "' in reaction '" + sbmlRxn.getId() + "' not found as species in SBML model.");
			}	// end - if (spRef is species in model)
		}	// end - for products

		// now add the products for the sbml reaction from sbmlProductsHash as products to vcRxn
		Iterator<String> sbmlProductsIter = sbmlProductsHash.keySet().iterator();
		while (sbmlProductsIter.hasNext()) {
			String sbmlProductStr = sbmlProductsIter.next();
			SpeciesContext speciesContext = vcModel.getSpeciesContext(sbmlProductStr);
			int stoich = sbmlProductsHash.get(sbmlProductStr).intValue();
			((SimpleReaction)vcRxn).addProduct(speciesContext, stoich);
		}
	}	// end - if (vcRxn NOT FluxRxn)

	// modifiers
	for (int j = 0; j < (int)sbmlRxn.getNumModifiers(); j++){
		ModifierSpeciesReference spRef = sbmlRxn.getModifier(j);
		String sbmlSpId = spRef.getSpecies();
		if (sbmlModel.getSpecies(sbmlSpId) != null) {
			// check if this modifier species is preesent in vcRxn (could have been added as reactamt/product/catalyst). 
			// If alreay a catalyst in vcRxn, do nothing
			ArrayList<ReactionParticipant> vcRxnParticipants = getVCReactionParticipantsFromSymbol(vcRxn, sbmlSpId);
			SpeciesContext speciesContext = vcModel.getSpeciesContext(sbmlSpId);
			if (vcRxnParticipants == null || vcRxnParticipants.size() == 0) {
				// If not in reactionParticipantList of vcRxn, add as catalyst.
				vcRxn.addCatalyst(speciesContext);
			} else {
				for (ReactionParticipant rp : vcRxnParticipants) {
					if (rp instanceof Reactant || rp instanceof Product) {
						// If already a reactant or product in vcRxn, add warning to localIssuesList, don't do anything
						localIssueList.add(new Issue(speciesContext, issueContext, IssueCategory.SBMLImport_Reaction, "Species " + speciesContext.getName() + " was already added as a reactant and/or product to " + vcRxn.getName() + "; Cannot add it as a catalyst also.", Issue.SEVERITY_INFO));
						break;
					}
				}
			}
		} else {
			// spRef is not in model, throw exception
			throw new RuntimeException("Modifier '" + sbmlSpId + "' in reaction '" + sbmlRxn.getId() + "' not found as species in SBML model.");
		}	// end - if (spRef is species in model)
	}	// end - for modifiers
}


/**
 *  addAssignmentRules :
 *		Adds Assignment Rules from the SBML document
 *		Assignment rules are allowed (initial concentration of species; parameter definitions, etc.
 *		
**/
protected void addAssignmentRules() throws Exception {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listofRules = sbmlModel.getListOfRules();
	if (listofRules == null) {
		System.out.println("No Rules specified");
		return;
	}
	for (int i = 0; i < sbmlModel.getNumRules(); i++){
		Rule rule = (org.sbml.libsbml.Rule)listofRules.get(i);
		if (rule instanceof AssignmentRule) {
			// Get the assignment rule and store it in the hashMap.
			AssignmentRule assignmentRule = (AssignmentRule)rule;
			Expression assignmentRuleMathExpr = getExpressionFromFormula(assignmentRule.getMath());
			String assgnRuleVar = assignmentRule.getVariable();
			// check if assignment rule is for species. If so, check if expression has x/y/z term. This is not allowed for non-spatial models in vcell.
			org.sbml.libsbml.Species ruleSpecies = sbmlModel.getSpecies(assgnRuleVar);
			if (ruleSpecies != null) {
				if (assignmentRuleMathExpr != null) {
					Model vcModel = simContext.getModel();
					if (!bSpatial) {
						if (assignmentRuleMathExpr.hasSymbol(vcModel.getX().getName()) || 
							assignmentRuleMathExpr.hasSymbol(vcModel.getY().getName()) || 
							assignmentRuleMathExpr.hasSymbol(vcModel.getZ().getName())) {
							logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.SPECIES_ERROR, "An assignment rule for species " + ruleSpecies.getId() + " contains reserved spatial variable(s) (x,y,z), this is not allowed for a non-spatial model in VCell");
						}
					}
				}
			}
			assignmentRulesHash.put(assignmentRule.getVariable(), assignmentRuleMathExpr);
		} 
	}	// end - for i : rules
}

/**
 *  addRateRules :
 *		Adds Rate Rules from the SBML document
 *		Rate rules are allowed (initial concentration of species; parameter definitions, etc.
 *		
**/
protected void addRateRules() throws ExpressionException {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listofRules = sbmlModel.getListOfRules();
	if (listofRules == null) {
		System.out.println("No Rules specified");
		return;
	}

	for (int i = 0; i < sbmlModel.getNumRules(); i++){
		Rule rule = (org.sbml.libsbml.Rule)listofRules.get(i);
		if (rule instanceof RateRule) {
			// Get the rate rule and store it in the hashMap, and create VCell rateRule.
			RateRule sbmlRateRule = (RateRule)rule;
			// rate rule name
			String rateruleName = sbmlRateRule.getId();
			if (rateruleName == null || rateruleName.length() == 0) {
				rateruleName = TokenMangler.mangleToSName(sbmlRateRule.getName());
				// if rate rule name is still null, get free rate rule name from simContext.
				if  (rateruleName == null  || rateruleName.length() == 0) {
					rateruleName = simContext.getFreeRateRuleName();
				}
			}
			// rate rule variable
			String varName = sbmlRateRule.getVariable();
			SymbolTableEntry rateRuleVar = simContext.getEntry(varName);
			if (rateRuleVar instanceof Structure) {
				throw new RuntimeException("Compartment '" + rateRuleVar.getName() + "' has a rate rule : not allowed in VCell at this time.");
			}
			try {
				if (rateRuleVar != null) {
					Expression vcRateRuleExpr = getExpressionFromFormula(sbmlRateRule.getMath());
					cbit.vcell.mapping.RateRule vcRateRule = new cbit.vcell.mapping.RateRule(rateruleName, rateRuleVar, vcRateRuleExpr, simContext);
					vcRateRule.bind();
					rateRulesHash.put(rateRuleVar.getName(), vcRateRuleExpr);
					simContext.addRateRule(vcRateRule);
				}
			} catch (PropertyVetoException e) {
				e.printStackTrace(System.out);
				throw new RuntimeException("Unable to create and add rate rule to VC model : " + e.getMessage());
			}
		}	// end if - RateRule 
	}	// end - for i : rules
}

protected void addSpecies(VCMetaData metaData) {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listOfSpecies = sbmlModel.getListOfSpecies();
	if (listOfSpecies == null) {
		System.out.println("No Spcecies");
		return;
	}
	HashMap<String, Species> vcSpeciesHash = new HashMap<String, Species>();
	HashMap<Species, org.sbml.libsbml.Species> vc_sbmlSpeciesHash = new HashMap<Species, org.sbml.libsbml.Species>();
	SpeciesContext[] vcSpeciesContexts = new SpeciesContext[(int)sbmlModel.getNumSpecies()];	
	// Get species from SBMLmodel;  Add/get speciesContext
	try {
		// First pass - add the speciesContexts
		for (int i = 0; i < sbmlModel.getNumSpecies(); i++) {
			org.sbml.libsbml.Species sbmlSpecies = (org.sbml.libsbml.Species)listOfSpecies.get(i);
			// Sometimes, the species name can be null or a blank string; in that case, use species id as the name.
			String speciesName = sbmlSpecies.getId();
			Species vcSpecies = null;
			// create a species with speciesName as commonName. If it is different in the annotation, can change it later
			Element sbmlImportRelatedElement = sbmlAnnotationUtil.readVCellSpecificAnnotation(sbmlSpecies);
			if (sbmlImportRelatedElement != null) {
				Element embeddedElement = getEmbeddedElementInAnnotation(sbmlImportRelatedElement, SPECIES_NAME);
				if (embeddedElement != null) {
					// Get the species name from annotation and create the species.
					if (embeddedElement.getName().equals(XMLTags.SpeciesTag)) {
						String vcSpeciesName = embeddedElement.getAttributeValue(XMLTags.NameAttrTag);
						vcSpecies = vcSpeciesHash.get(vcSpeciesName);
						if (vcSpecies == null) {
							vcSpecies = new Species(vcSpeciesName, vcSpeciesName);
							vcSpeciesHash.put(vcSpeciesName, vcSpecies);
						}
					}
					// if embedded element is not speciesTag, do I have to do something?
				} else {
					// Annotation element is present, but doesn't contain the species element.
					vcSpecies = new Species(speciesName, speciesName);
					vcSpeciesHash.put(speciesName, vcSpecies);
				}
			} else {
				vcSpecies = new Species(speciesName, speciesName);
				vcSpeciesHash.put(speciesName, vcSpecies);
			}

			// store vc & sbml species in hash to read in annotation later
			vc_sbmlSpeciesHash.put(vcSpecies, sbmlSpecies);
			
			// Get matching compartment name (of sbmlSpecies[i]) from feature list
			String compartmentId = sbmlSpecies.getCompartment();
			Structure spStructure = simContext.getModel().getStructure(compartmentId);
			vcSpeciesContexts[i] = new SpeciesContext(vcSpecies, spStructure);
			vcSpeciesContexts[i].setName(speciesName);

			// Adjust units of species, convert to VC units.
			// Units in SBML, compute this using some of the attributes of sbmlSpecies
			int dimension = (int)sbmlModel.getCompartment(sbmlSpecies.getCompartment()).getSpatialDimensions();
			if (dimension == 0 || dimension == 1){
				logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNIT_ERROR, dimension+" dimensional compartment "+compartmentId+" not supported");
			}
		} // end - for sbmlSpecies
		
		// set the species & speciesContexts on model
		Model vcModel = simContext.getModel();
		vcModel.setSpecies(vcSpeciesHash.values().toArray(new Species[0]));
		vcModel.setSpeciesContexts(vcSpeciesContexts);

		// Set annotations and notes from SBML to VCMetadata
		Species[] vcSpeciesArray = vc_sbmlSpeciesHash.keySet().toArray(new Species[0]);
		for (Species vcSpecies : vcSpeciesArray) {
			org.sbml.libsbml.Species sbmlSpecies = vc_sbmlSpeciesHash.get(vcSpecies);
			sbmlAnnotationUtil.readAnnotation(vcSpecies, sbmlSpecies);
			sbmlAnnotationUtil.readNotes(vcSpecies, sbmlSpecies);
		}
	} catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Error adding species context; "+ e.getMessage());
	}
}

/**
 * setSpeciesInitialConditions : called after speciesContexts and global parameters have been set.  Checks for init conditions set on species
 * in the Sbml model, and if it is set using an assignment rule, obtain the corresponding expression. Obtain the sbml -> vc unit conversion 
 * factor for species concentrations to adjust the species initial condition units/factor.
 *  
 */
private void setSpeciesInitialConditions() {
	try {
		// fill in SpeciesContextSpec for each speciesContext
		Model vcModel = simContext.getModel();
		SpeciesContext[] vcSpeciesContexts = vcModel.getSpeciesContexts();
		for (int i = 0; i < vcSpeciesContexts.length; i++) {
			org.sbml.libsbml.Species sbmlSpecies = (org.sbml.libsbml.Species)sbmlModel.getSpecies(vcSpeciesContexts[i].getName());
			// Sometimes, the species name can be null or a blank string; in that case, use species id as the name.
			String speciesName = sbmlSpecies.getId();
			Compartment compartment = (Compartment)sbmlModel.getCompartment(sbmlSpecies.getCompartment());
	
			Expression initExpr = null;
			if (sbmlSpecies.isSetInitialConcentration()) { 		// If initial Concentration is set
				Expression initConcentration = new Expression(sbmlSpecies.getInitialConcentration());
				// check if initConc is set by a (assignment) rule. That takes precedence over initConc value set on species.
				initExpr = getValueFromAssignmentRule(speciesName);
				if (initExpr == null) {
					initExpr = new Expression(initConcentration);
				}
			} else if (sbmlSpecies.isSetInitialAmount()) {		// If initial amount is set
				double initAmount = sbmlSpecies.getInitialAmount();
				// initConcentration := initAmount / compartmentSize.
				// If compartmentSize is set and non-zero, compute initConcentration. Else, throw exception.
				if (compartment.isSetSize()) {
					double compartmentSize = compartment.getSize();
					Expression initConcentration = new Expression(0.0);
					if (compartmentSize != 0.0) {
						initConcentration = new Expression(initAmount / compartmentSize);
					} else {
						logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNIT_ERROR, "compartment '"+compartment.getId()+"' has zero size, unable to determine initial concentration for species "+speciesName);
					}
					// check if initConc is set by a (assignment) rule. That takes precedence over initConc/initAmt value set on species.
					initExpr = getValueFromAssignmentRule(speciesName);
					if (initExpr == null) {
						initExpr = new Expression(initConcentration);
					}
				} else {
					logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.SPECIES_ERROR, " Compartment '" + compartment.getId() + "' size not set or is defined by a rule; cannot calculate initConc.");
				}
			} else {
				// initConc/initAmt not set; check if species has a (assignment) rule.
				initExpr = getValueFromAssignmentRule(speciesName);
				if (initExpr == null) {
					// no assignment rule (and there was no initConc or initAmt); if it doesn't have initialAssignment, throw warning and set it to 0.0
					if (sbmlModel.getInitialAssignment(speciesName) == null) {
						localIssueList.add(new Issue(new SBMLIssueSource(sbmlModel.getSpecies(speciesName)), issueContext, IssueCategory.SBMLImport_MissingSpeciesInitCondition , "no initial condition for species "+speciesName+", assuming 0.0", Issue.SEVERITY_WARNING));
						// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNIT_ERROR, "no initial condition for species "+speciesName+", assuming 0.0");
					}
					initExpr = new Expression(0.0);
				}
	
			}

			// if initExpr is an expression with model species, we need a conversion factor for the species units (SBML - VC units),
			// similar to the conversion that is done in reactions.
			if (initExpr != null) {
				// initExpr will be changed
				initExpr = adjustExpression(initExpr, vcModel);
			}
			

			// If any of the symbols in the expression for speciesConc is a rule, expand it.
			substituteGlobalParamRulesInPlace(initExpr, false);
	
			SpeciesContextSpec speciesContextSpec = simContext.getReactionContext().getSpeciesContextSpec(vcSpeciesContexts[i]);
			speciesContextSpec.getInitialConditionParameter().setExpression(initExpr);
			speciesContextSpec.setConstant(sbmlSpecies.getBoundaryCondition() || sbmlSpecies.getConstant());
		}
	} catch (Throwable e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Error setting initial condition for species context; "+ e.getMessage()); 
	}
}

/**
 *  checkCompartmentScaleFactorInRxnRateExpr :
 *		Used to check if reaction rate expression has a compartment scale factor. Need to remove this factor from the rate expression.
 *		Differentiate the rate expression wrt the compartmentSizeParamName. If the differentiated expression contains the compartmentSizeParamName,
 *		VCell doesn't support non-linear functions of compartmentSizeParam.
 *		Substitute 1.0 for the compartmentSizeParam in the rateExpr and check its equivalency with the differentiated expr above.
 *		If they are not equal, the rate expression is a non-linear function of compartmentSizeParam - not acceptable.
 *		Substitute 0.0 for compartmentSizeParam in rateExpr. If the value doesn't evaluate to 0.0, it is not valid for the same reason above.
 **/

/* pending delete? gcw 4/2014
private Expression removeCompartmentScaleFactorInRxnRateExpr(Expression rateExpr, String compartmentSizeParamName, String rxnName) throws Exception {
	Expression diffExpr = rateExpr.differentiate(compartmentSizeParamName).flatten();
	if (diffExpr.hasSymbol(compartmentSizeParamName)) {
		logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNIT_ERROR, "Unable to interpret Kinetic rate for reaction : " + rxnName + " Cannot interpret non-linear function of compartment size");
	}

	Expression expr1 = rateExpr.getSubstitutedExpression(new Expression(compartmentSizeParamName), new Expression(1.0)).flatten();
	if (!expr1.compareEqual(diffExpr) && !(ExpressionUtils.functionallyEquivalent(expr1, diffExpr))) {
		logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNIT_ERROR, "Unable to interpret Kinetic rate for reaction : " + rxnName + " Cannot interpret non-linear function of compartment size");
	}

	Expression expr0 = rateExpr.getSubstitutedExpression(new Expression(compartmentSizeParamName), new Expression(0.0)).flatten();
	if (!expr0.isZero()) {
		logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNIT_ERROR, "Unable to interpret Kinetic rate for reaction : " + rxnName + " Cannot interpret non-linear function of compartment size");
	}

	return expr1;
}
*/


/**
 * resolveRxnParameterNameConflicts :
 * 		Check if the reaction rate name matches with any global or local parameter, in which case, we have to change the rate name 
 * 		(to oldName_rxnName); since the global or local parameter value will override the rate equation/value. Also, when we import 
 * 		a VCell model that has been exported to SBML, if the user has changed the rate name in a reaction, it is stored in the 
 * 		reaction annotation. This has to be retrieved and set as reaction rate name.
 * 		
 * @param sbmlRxn
 * @param newKinetics
 * @throws ExpressionException
 */
private void resolveRxnParameterNameConflicts(Reaction sbmlRxn, Kinetics vcKinetics, Element sbmlImportElement) throws PropertyVetoException {
	// If the name of the rate parameter has been changed by user, it is stored in rxnAnnotation. 
	// Retrieve this to re-set rate param name.
	if (sbmlImportElement != null) {
		Element embeddedRxnElement = getEmbeddedElementInAnnotation(sbmlImportElement, RATE_NAME);
		String vcRateParamName = null;
		if (embeddedRxnElement != null) {
			if (embeddedRxnElement.getName().equals(XMLTags.RateTag)) {
				vcRateParamName = embeddedRxnElement.getAttributeValue(XMLTags.NameAttrTag);
				vcKinetics.getAuthoritativeParameter().setName(vcRateParamName);
			}
		} 
	}

	/* Get the rate name from the kinetics : if it is from GeneralKinetics, it is the reactionRateParamter name;
	 * if it is from LumpedKinetics, it is the LumpedReactionRateParameter name.
	 */
	String origRateParamName = vcKinetics.getAuthoritativeParameter().getName();
	
	/* Check if any parameters (global/local) have the same name as kinetics rate param name;
	 * This will replace any rate expression with the global/local param value; which is unacceptable.
	 * If there is a match, replace it with a new name for rate param - say, origName_reactionName.
	 */
	ListOf listofGlobalParams = sbmlModel.getListOfParameters();
	for (int j = 0; j < sbmlModel.getNumParameters(); j++) {
		org.sbml.libsbml.Parameter param = (org.sbml.libsbml.Parameter)listofGlobalParams.get(j);
		String paramName = param.getId();
		// Check if reaction rate param clashes with an existing (pre-defined) kinetic parameter - eg., reaction rate param 'J'
		// If so, change the name of the kinetic param (say, by adding reaction name to it).
		if (paramName.equals(origRateParamName)) {
			vcKinetics.getAuthoritativeParameter().setName(origRateParamName+"_"+TokenMangler.mangleToSName(sbmlRxn.getId()));
		}
	}

	KineticLaw kLaw = sbmlRxn.getKineticLaw();
	if (kLaw != null) {
		ListOf listofLocalParams = kLaw.getListOfParameters();
		for (int j = 0; j < kLaw.getNumParameters(); j++) {
			org.sbml.libsbml.Parameter param = (org.sbml.libsbml.Parameter)listofLocalParams.get(j);
			String paramName = param.getId();
			// Check if reaction rate param clashes with an existing (pre-defined) kinetic parameter - eg., reaction rate param 'J'
			// If so, change the name of the kinetic param (say, by adding reaction name to it).
			if (paramName.equals(origRateParamName)) {
				vcKinetics.getAuthoritativeParameter().setName(origRateParamName+"_"+TokenMangler.mangleToSName(sbmlRxn.getId()));
			}
		}
	} 
}

/**
 *  getReferencedSpecies(Reaction , HashSet<String> ) :
 *  	Get the species referenced in sbmlRxn (reactants and products); store their names in hashSet (refereceNamesHash)
 *		Also, get the species referenced in the reaction kineticLaw expression from getReferencedSpeciesInExpr.
 * @param sbmlRxn
 * @param refSpeciesNameHash
 * @throws ExpressionException
 */
private void getReferencedSpecies(Reaction sbmlRxn, HashSet<String> refSpeciesNameHash) throws ExpressionException {
	// get all species referenced in listOfReactants
	for (int i = 0; i < (int)sbmlRxn.getNumReactants(); i++){
		SpeciesReference reactRef = sbmlRxn.getReactant(i);
		refSpeciesNameHash.add(reactRef.getSpecies());
	}
	// get all species referenced in listOfProducts
	for (int i = 0; i < (int)sbmlRxn.getNumProducts(); i++){
		SpeciesReference pdtRef = sbmlRxn.getProduct(i);
		refSpeciesNameHash.add(pdtRef.getSpecies());
	}
	// get all species referenced in reaction rate law
	if (sbmlRxn.getKineticLaw() != null) {
		Expression rateExpression = getExpressionFromFormula(sbmlRxn.getKineticLaw().getMath());
		getReferencedSpeciesInExpr(rateExpression, refSpeciesNameHash);
	} 
}

/**
 * getReferencedSpeciesInExpr(Expression , HashSet<String> ) : 
 * 		Recursive method to get species referenced in expression 'sbmlExpr' - takes care of cases where expressions
 * 		have symbols that are themselves expression and might contain other species.
 * @param sbmlExpr
 * @param refSpNamesHash
 * @throws ExpressionException
 */
private void getReferencedSpeciesInExpr(Expression sbmlExpr, HashSet<String> refSpNamesHash) throws ExpressionException {
	String[] symbols = sbmlExpr.getSymbols();
	for (int i = 0; symbols != null && i < symbols.length; i++) {
		Parameter sbmlParam = sbmlModel.getParameter(symbols[i]);
		if (sbmlParam != null){
			Expression paramExpression = getValueFromAssignmentRule(sbmlParam.getId());
			if (paramExpression != null) {
				getReferencedSpeciesInExpr(paramExpression, refSpNamesHash);
			}
		}else{
			org.sbml.libsbml.Species sbmlSpecies = sbmlModel.getSpecies(symbols[i]);
			if (sbmlSpecies!=null){
				refSpNamesHash.add(sbmlSpecies.getId());
			}
		}
	}
}

/**
 * substituteGlobalParamRulesInPlace:
 * @param sbmlExpr
 * @param expandedExpr
 * @throws ExpressionException
 */
private void substituteGlobalParamRulesInPlace(Expression sbmlExpr, boolean bReplaceValues) throws ExpressionException {
	boolean bParamChanged = true;
	while (bParamChanged) {
		bParamChanged = false;
		String[] symbols = sbmlExpr.getSymbols();
		for (int i = 0; symbols != null && i < symbols.length; i++) {
			Parameter sbmlParam = sbmlModel.getParameter(symbols[i]);
			if (sbmlParam != null){
				Expression paramExpression = getValueFromAssignmentRule(sbmlParam.getId());
				if (paramExpression != null) {
					sbmlExpr.substituteInPlace(new Expression(sbmlParam.getId()), paramExpression);
					bParamChanged = true;
				} else if (bReplaceValues) {
					sbmlExpr.substituteInPlace(new Expression(sbmlParam.getId()), new Expression(sbmlParam.getValue()));
				}
			}
		}
	}
}

///**
// * 	@ TODO: This method doesn't take care of adjusting species in nested parameter rules with the species_concetration_factor.
// * @param kinetics
// * @param paramExpr
// * @throws ExpressionException
// */
//private void substituteOtherGlobalParams(Kinetics kinetics, Expression paramExpr) throws ExpressionException, PropertyVetoException {
//	String[] exprSymbols = paramExpr.getSymbols();
//	if (exprSymbols == null || exprSymbols.length == 0) {
//		return;
//	}
//	Model vcModel = simContext.getModel();
//	for (int kk = 0; kk < exprSymbols.length; kk++) {
//		ModelParameter mp = vcModel.getModelParameter(exprSymbols[kk]);
//		if (mp != null) {
//			Expression expr = mp.getExpression();
//			if (expr != null) {
//				Expression newExpr = new Expression(expr);
//				substituteGlobalParamRulesInPlace(newExpr, false);
//				// param has constant value, add it as a kinetic parameter if it is not already in the kinetics
//				kinetics.setParameterValue(exprSymbols[kk], newExpr.infix());
//				kinetics.getKineticsParameter(exprSymbols[kk]).setUnitDefinition(getSBMLUnit(sbmlModel.getParameter(exprSymbols[kk]).getUnits(), null));
//				if (newExpr.getSymbols() != null) {
//					substituteOtherGlobalParams(kinetics, newExpr);
//				}
//			}
//		}
//	}
//}

/**
 * parse SBML file into biomodel 
 * logs errors to log4j if present in source document
 * @return new Biomodel
 */
public BioModel getBioModel() {
	// Read SBML model into libSBML SBMLDocument and create an SBML model
	SBMLReader reader = new SBMLReader();
	SBMLDocument document = reader.readSBML(sbmlFileName);

	long numProblems = document.getNumErrors();
	System.out.println("\n\nSBML Import Error Report");
	OStringStream oStrStream = new OStringStream();
	document.printErrors(oStrStream);
	if (numProblems > 0 && lg.isEnabledFor(Level.WARN)) {
		lg.warn("Num problems in original SBML document : " + numProblems);
		lg.warn(oStrStream.str());
	}

	try {
		sbmlModel = document.getModel();

		if (sbmlModel == null) {
			throw new RuntimeException("Unable to read SBML file : \n" + oStrStream.str());
		}

		// Convert SBML Model to VCell model
		// An SBML model will correspond to a simcontext - which needs a Model and a Geometry
		// SBML handles only nonspatial geometries at this time, hence creating a non-spatial default geometry
		String modelName = sbmlModel.getId();
		if (modelName == null || modelName.trim().equals("")) {
			modelName = sbmlModel.getName();
		} 
		// if sbml 'model' didn't have either id or name set, use a default name, say 'newModel'
		if (modelName == null || modelName.trim().equals("")) {
			modelName = "newModel";
		} 

		// get namespace based on SBML model level and version to use in SBMLAnnotationUtil
		this.level = sbmlModel.getLevel();
		//this.version = sbmlModel.getVersion();
		XMLNamespaces nss = document.getNamespaces();
		String namespaceStr = nss.getURI();

		// SBML annotation
		sbmlAnnotationUtil = new SBMLAnnotationUtil(vcBioModel.getVCMetaData(), vcBioModel, namespaceStr);

		try {
			// create SBML unit system to pass into VCModel
			Model vcModel;
			try {
				vcModel = new Model(modelName, createSBMLUnitSystemForVCModel());
			} catch (Exception e) {
				e.printStackTrace(System.out);
				throw new RuntimeException("Inconsistent unit system. Cannot import SBML model into VCell");
			}
			Geometry geometry = new Geometry(BioModelChildSummary.COMPARTMENTAL_GEO_STR, 0);
			simContext = new SimulationContext(vcModel, geometry);
			simContext.setName(simContext.getModel().getName());
			//		simContext.setName(simContext.getModel().getName()+"_"+simContext.getGeometry().getName());
		} catch (PropertyVetoException e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Could not create simulation context corresponding to the input SBML model");
		} 
		translateSBMLModel();

		try {
			// **** TEMPORARY BLOCK - to name the biomodel with proper name, rather than model id
			String biomodelName = sbmlModel.getName();
			// if name is not set, use id
			if ((biomodelName == null) || biomodelName.trim().equals("")) {
				biomodelName = sbmlModel.getId();
			}
			// if id is not set, use a default, say, 'newModel'
			if ((biomodelName == null) || biomodelName.trim().equals("")) {
				biomodelName = "newBioModel";
			}
			vcBioModel.setName(biomodelName);
			// **** end - TEMPORARY BLOCK

			// bioModel.setName(modelName);
			vcBioModel.setModel(simContext.getModel());
			vcBioModel.setSimulationContexts(new SimulationContext[] {simContext});			
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Could not create Biomodel");
		}

		sbmlAnnotationUtil.readAnnotation(vcBioModel, sbmlModel);
		sbmlAnnotationUtil.readNotes(vcBioModel, sbmlModel);

		vcBioModel.refreshDependencies();

		Issue warningIssues[] = (Issue[])BeanUtils.getArray(localIssueList,Issue.class);
		if (warningIssues!=null && warningIssues.length>0){
			StringBuffer messageBuffer = new StringBuffer("Issues encountered during SBML Import:\n");
			int issueCount=0;
			for (int i = 0; i < warningIssues.length; i++){
				if (warningIssues[i].getSeverity()==Issue.SEVERITY_WARNING || warningIssues[i].getSeverity()==Issue.SEVERITY_INFO){
					messageBuffer.append(warningIssues[i].getCategory()+" "+warningIssues[i].getSeverityName()+" : "+warningIssues[i].getMessage()+"\n");
					issueCount++;
				}
			}
			if (issueCount>0){
				try {
					logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.OVERALL_WARNINGS, messageBuffer.toString());
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
				// PopupGenerator.showWarningDialog(requester,messageBuffer.toString(),new String[] { "OK" }, "OK");
			}
		}
	} catch (Exception e) {
		throw new RuntimeException("Unable to read SBML file : \n" + oStrStream.str(),e);
	}

	return vcBioModel;
}

private ModelUnitSystem createSBMLUnitSystemForVCModel() throws Exception {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listofUnitDefns = sbmlModel.getListOfUnitDefinitions();
	if (listofUnitDefns == null) {
		System.out.println("No Unit Definitions");
		// if < level 3, use SBML default units to create unit system; else, return a default VC modelUnitSystem.
		// @TODO: deal with SBML level < 3.
		return ModelUnitSystem.createDefaultVCModelUnitSystem();
	}
	
	@SuppressWarnings("serial")
	VCUnitSystem tempVCUnitSystem = new VCUnitSystem() {};
	HashMap<String, VCUnitDefinition> sbmlUnitIdentifierHash = new HashMap<String, VCUnitDefinition>();
	// add base SI unit identifiers (as defined in SBML spec) to hash 
	sbmlUnitIdentifierHash.put("ampere", tempVCUnitSystem.getInstance("A"));
	sbmlUnitIdentifierHash.put("avogadro", tempVCUnitSystem.getInstance("6.02e23"));
//	sbmlUnitIdentifierHash.put("becquerel", tempVCUnitSystem.getInstance("becquerel"));
//	sbmlUnitIdentifierHash.put("candela", tempVCUnitSystem.getInstance("candela"));
	sbmlUnitIdentifierHash.put("coulomb", tempVCUnitSystem.getInstance("C"));
	sbmlUnitIdentifierHash.put("dimensionless", tempVCUnitSystem.getInstance("1"));
	sbmlUnitIdentifierHash.put("farad", tempVCUnitSystem.getInstance("F"));
	sbmlUnitIdentifierHash.put("gram", tempVCUnitSystem.getInstance("g"));
//	sbmlUnitIdentifierHash.put("gray", tempVCUnitSystem.getInstance("gray"));
	sbmlUnitIdentifierHash.put("henry", tempVCUnitSystem.getInstance("H"));
	sbmlUnitIdentifierHash.put("hertz", tempVCUnitSystem.getInstance("Hz"));
	sbmlUnitIdentifierHash.put("item", tempVCUnitSystem.getInstance("molecules"));
	sbmlUnitIdentifierHash.put("joule", tempVCUnitSystem.getInstance("J"));
//	sbmlUnitIdentifierHash.put("katal", tempVCUnitSystem.getInstance("katal"));
	sbmlUnitIdentifierHash.put("kelvin", tempVCUnitSystem.getInstance("K"));
	sbmlUnitIdentifierHash.put("kilogram", tempVCUnitSystem.getInstance("kg"));
	sbmlUnitIdentifierHash.put("litre", tempVCUnitSystem.getInstance("litre"));
//	sbmlUnitIdentifierHash.put("lumen", tempVCUnitSystem.getInstance("lumen"));
//	sbmlUnitIdentifierHash.put("lux", tempVCUnitSystem.getInstance("lux"));
	sbmlUnitIdentifierHash.put("metre", tempVCUnitSystem.getInstance("m"));
	sbmlUnitIdentifierHash.put("mole", tempVCUnitSystem.getInstance("mol"));
	sbmlUnitIdentifierHash.put("newton", tempVCUnitSystem.getInstance("N"));
//	sbmlUnitIdentifierHash.put("ohm", tempVCUnitSystem.getInstance("ohm"));
//	sbmlUnitIdentifierHash.put("pascal", tempVCUnitSystem.getInstance("pascal"));
//	sbmlUnitIdentifierHash.put("radian", tempVCUnitSystem.getInstance("radian"));
	sbmlUnitIdentifierHash.put("second", tempVCUnitSystem.getInstance("s"));
	sbmlUnitIdentifierHash.put("siemens", tempVCUnitSystem.getInstance("S"));
//	sbmlUnitIdentifierHash.put("sievert", tempVCUnitSystem.getInstance("sievert"));
//	sbmlUnitIdentifierHash.put("steradian", tempVCUnitSystem.getInstance("steradian"));
//	sbmlUnitIdentifierHash.put("tesla", tempVCUnitSystem.getInstance("tesla"));
	sbmlUnitIdentifierHash.put("volt", tempVCUnitSystem.getInstance("V"));
	sbmlUnitIdentifierHash.put("watt", tempVCUnitSystem.getInstance("W"));
	sbmlUnitIdentifierHash.put("weber", tempVCUnitSystem.getInstance("Wb"));	

	long sbmlLevel = sbmlModel.getLevel();
	if (sbmlLevel < 3) {
		// SBML predefined unit identifiers
		sbmlUnitIdentifierHash.put("substance", tempVCUnitSystem.getInstance("mole"));
		sbmlUnitIdentifierHash.put("volume", tempVCUnitSystem.getInstance("litre"));
		sbmlUnitIdentifierHash.put("area", tempVCUnitSystem.getInstance("m2"));
		sbmlUnitIdentifierHash.put("length", tempVCUnitSystem.getInstance("m"));
		sbmlUnitIdentifierHash.put("time", tempVCUnitSystem.getInstance("s"));
	}

	// read unit definition (identifiers) declared in SBML model
	for (int i = 0; i < sbmlModel.getNumUnitDefinitions(); i++) {
		UnitDefinition ud = (org.sbml.libsbml.UnitDefinition)listofUnitDefns.get(i);
		String unitName = ud.getId();
		VCUnitDefinition vcUnitDef = SBMLUnitTranslator.getVCUnitDefinition(ud, tempVCUnitSystem);
		sbmlUnitIdentifierHash.put(unitName, vcUnitDef);
	}
	
	// For SBML level 2
		// default units
		VCUnitDefinition defaultSubstanceUnit = sbmlUnitIdentifierHash.get("substance");
		VCUnitDefinition defaultVolumeUnit = sbmlUnitIdentifierHash.get("volume");
		VCUnitDefinition defaultAreaUnit = sbmlUnitIdentifierHash.get("area");
		VCUnitDefinition defaultLengthUnit = sbmlUnitIdentifierHash.get("length");
		VCUnitDefinition defaultTimeUnit = sbmlUnitIdentifierHash.get("time");

			
		
		VCUnitDefinition modelSubstanceUnit = null;
		VCUnitDefinition modelVolumeUnit = null;
		VCUnitDefinition modelAreaUnit = null;
		VCUnitDefinition modelLengthUnit = null;
		VCUnitDefinition modelTimeUnit = null;
		
		// units in SBML model
		
		// compartments
		ListOfCompartments listOfCompartments = sbmlModel.getListOfCompartments();
		for (int i = 0; i < listOfCompartments.size(); i++) {
			Compartment sbmlComp = listOfCompartments.get(i);
			long dim = sbmlComp.getSpatialDimensions();
			String unitStr = sbmlComp.getUnits();
			VCUnitDefinition sbmlUnitDefinition = null;
			if (unitStr != null && unitStr.length() > 0) {
				sbmlUnitDefinition = sbmlUnitIdentifierHash.get(unitStr);
			} else {
				// applying default unit if not defined for this compartment
				if (dim == 3){
					sbmlUnitDefinition = defaultVolumeUnit;
				}else if (dim == 2){
					sbmlUnitDefinition = defaultAreaUnit;
				}else if (dim == 1){
					sbmlUnitDefinition = defaultLengthUnit;
				}
			}
			if (dim == 3) {
				if (sbmlUnitDefinition == null) {
					sbmlUnitDefinition = defaultVolumeUnit;
				}
				if (modelVolumeUnit == null) {
					modelVolumeUnit = sbmlUnitDefinition;
				} else if (!sbmlUnitDefinition.isEquivalent(modelVolumeUnit)) {
					localIssueList.add(new Issue(new SBMLIssueSource(sbmlComp), issueContext, IssueCategory.Units, "unit for compartment '" + sbmlComp.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current vol unit (" + modelVolumeUnit.getSymbol() + ")", Issue.SEVERITY_WARNING));
					// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNIT_ERROR, "unit for compartment '" + sbmlComp.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current vol unit (" + modelVolumeUnit.getSymbol() + ")");
				}
			} else if (dim == 2) {
				if (modelAreaUnit == null) {
					modelAreaUnit = sbmlUnitDefinition;
				} else if (!sbmlUnitDefinition.isEquivalent(modelAreaUnit)) {
					localIssueList.add(new Issue(new SBMLIssueSource(sbmlComp), issueContext, IssueCategory.Units, "unit for compartment '" + sbmlComp.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current area unit (" + modelAreaUnit.getSymbol() + ")", Issue.SEVERITY_WARNING));
					// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNIT_ERROR, "unit for compartment '" + sbmlComp.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current area unit (" + modelAreaUnit.getSymbol() + ")");
				}
			}
		}
		
		// species
		ListOfSpecies listOfSpecies = sbmlModel.getListOfSpecies();
		for (int i = 0; i < listOfSpecies.size(); i++) {
			org.sbml.libsbml.Species sbmlSpecies = listOfSpecies.get(i);
			String unitStr = sbmlSpecies.getSubstanceUnits();
			VCUnitDefinition sbmlUnitDefinition = null;
			if (unitStr != null && unitStr.length() > 0) {
				sbmlUnitDefinition = sbmlUnitIdentifierHash.get(unitStr);
			} else {
				// apply default substance unit
				sbmlUnitDefinition = defaultSubstanceUnit;
			}
			if (modelSubstanceUnit==null){
				modelSubstanceUnit = sbmlUnitDefinition;
			}else if (!sbmlUnitDefinition.isEquivalent(modelSubstanceUnit)) {
				localIssueList.add(new Issue(new SBMLIssueSource(sbmlSpecies), issueContext, IssueCategory.Units, "unit for species '" + sbmlSpecies.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current substance unit (" + modelSubstanceUnit.getSymbol() + ")", Issue.SEVERITY_WARNING));
				// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNIT_ERROR, "unit for species '" + sbmlSpecies.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current substance unit (" + modelSubstanceUnit.getSymbol() + ")");
			}
		}

		// reactions for SBML level 2 version < 3
		long sbmlVersion = sbmlModel.getVersion();
		if (sbmlVersion < 3) {
			ListOfReactions listOfReactions = sbmlModel.getListOfReactions();
			for (int i = 0; i < listOfReactions.size(); i++) {
				Reaction sbmlReaction = listOfReactions.get(i);
				KineticLaw kineticLaw = sbmlReaction.getKineticLaw();
				if (kineticLaw != null) {
					// first check substance unit
					String unitStr = kineticLaw.getSubstanceUnits();
					VCUnitDefinition sbmlUnitDefinition = null;
					if (unitStr != null && unitStr.length() > 0) {
						sbmlUnitDefinition = sbmlUnitIdentifierHash.get(unitStr);
					} else {
						// apply default substance unit
						sbmlUnitDefinition = defaultSubstanceUnit;
					}
					if (modelSubstanceUnit == null) {
						modelSubstanceUnit = sbmlUnitDefinition;
					} else if (!sbmlUnitDefinition.isEquivalent(modelSubstanceUnit)) {
						localIssueList.add(new Issue(new SBMLIssueSource(sbmlReaction), issueContext, IssueCategory.Units, "substance unit for reaction '" + sbmlReaction.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current substance unit (" + modelSubstanceUnit.getSymbol() + ")", Issue.SEVERITY_WARNING));
						// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNIT_ERROR, "substance unit for reaction '" + sbmlReaction.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current substance unit (" + modelSubstanceUnit.getSymbol() + ")");
					}
					// check time unit
					unitStr = kineticLaw.getTimeUnits();
					if (unitStr != null && unitStr.length() > 0) {
						sbmlUnitDefinition = sbmlUnitIdentifierHash.get(unitStr);
					} else {
						// apply default time unit
						sbmlUnitDefinition = defaultTimeUnit;
					}
					if (modelTimeUnit == null) {
						modelTimeUnit = sbmlUnitDefinition;
					} else if (!sbmlUnitDefinition.isEquivalent(modelTimeUnit)) {
						localIssueList.add(new Issue(new SBMLIssueSource(sbmlReaction), issueContext, IssueCategory.Units, "time unit for reaction '" + sbmlReaction.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current time unit (" + modelTimeUnit.getSymbol() + ")", Issue.SEVERITY_WARNING));
						// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNIT_ERROR, "time unit for reaction '" + sbmlReaction.getId() + "' (" + unitStr + ") : (" + sbmlUnitDefinition.getSymbol() + ") not compatible with current time unit (" + modelTimeUnit.getSymbol() + ")");
					}
				}
			}
		}
		
		if (modelSubstanceUnit == null) {
			modelSubstanceUnit = defaultSubstanceUnit;
		}
		if (modelVolumeUnit == null) {
			modelVolumeUnit = defaultVolumeUnit;
		}
		if (modelAreaUnit == null) {
			modelAreaUnit = defaultAreaUnit;
		}
		if (modelLengthUnit == null) {
			modelLengthUnit = defaultLengthUnit;
		}
		if (modelTimeUnit == null) {
			modelTimeUnit = defaultTimeUnit;
		}
		
		if (modelSubstanceUnit == null && modelVolumeUnit == null && modelAreaUnit == null && modelLengthUnit == null && modelTimeUnit == null) {
			// no default units specified in SBML Level 3, so just return a default (VC)modelUnitSystem
			return ModelUnitSystem.createDefaultVCModelUnitSystem();
		} else {
			return ModelUnitSystem.createSBMLUnitSystem(modelSubstanceUnit, modelVolumeUnit, modelAreaUnit, modelLengthUnit, modelTimeUnit);
		}
	
}


/**
 *  getEmbeddedElementInRxnAnnotation :
 *  Takes the reaction annotation as an argument and returns the embedded element  (fluxstep or simple reaction), if present.
 */
private Element getEmbeddedElementInAnnotation(Element sbmlImportRelatedElement, String tag) {
	// Get the XML element corresponding to the annotation xmlString.
	String elementName = null;
	if (tag.equals(RATE_NAME)) {
		elementName = XMLTags.ReactionRateTag;
	} else if (tag.equals(SPECIES_NAME)) {
		elementName = XMLTags.SpeciesTag;
	} else if (tag.equals(REACTION)) {
		if (sbmlImportRelatedElement.getChild(XMLTags.FluxStepTag, sbmlImportRelatedElement.getNamespace()) != null) {
			elementName = XMLTags.FluxStepTag;
		} else if (sbmlImportRelatedElement.getChild(XMLTags.SimpleReactionTag, sbmlImportRelatedElement.getNamespace()) != null) {
			elementName = XMLTags.SimpleReactionTag;
		}
	} else if (tag.equals(OUTSIDE_COMP_NAME)) {
		elementName = XMLTags.OutsideCompartmentTag;
	}
	// If there is an annotation element for the reaction or species, retrieve and return.
	if (sbmlImportRelatedElement != null) {
		for (int j = 0; j < sbmlImportRelatedElement.getChildren().size(); j++) {
			Element infoChild = sbmlImportRelatedElement.getChild(elementName, sbmlImportRelatedElement.getNamespace());
			if (infoChild != null){
				return infoChild;
			}
		}
	}	
	return null;
}


/**
 *  getExpressionFromFormula : 
 *	Convert the math formula string in a kineticLaw, rule or lambda function definition into MathML
 *	and use ExpressionMathMLParser to convert the MathML into an expression to be brought into the VCell.
 *	NOTE : ExpressionMathMLParser will handle only the <apply> elements of the MathML string,
 *	hence the ExpressionMathMLParser is given a substring of the MathML containing the <apply> elements. 
 */
private Expression getExpressionFromFormula(ASTNode math) throws ExpressionException {
	String mathMLStr = libsbml.writeMathMLToString(math);
	ExpressionMathMLParser exprMathMLParser = new ExpressionMathMLParser(lambdaFunctions);
	Expression expr =  exprMathMLParser.fromMathML(mathMLStr);
	return expr;
}

/**
 * getReactionStructure :
 */
private Structure getReactionStructure(org.sbml.libsbml.Reaction sbmlRxn, SpeciesContext[] speciesContexts, Element sbmlImportElement) throws Exception {
    Structure struct = null;
    String structName = null;
    Model vcModel = simContext.getModel();
    
    // if sbml model is spatial, see if reaction has compartment atribute, return structure from vcmodel, if present.
    if (bSpatial) {
    	structName = sbmlRxn.getCompartment();
    	if (structName != null && structName.length() > 0) {
    		struct = vcModel.getStructure(structName);
    		if (struct != null) {
    			return struct;
    		}
    	}
    }
    
    // Check annotation for reaction - if we are importing an exported VCell model, it will contain annotation for reaction.
    // If annotation has structure name, return the corresponding structure.
	if (sbmlImportElement != null) {
        // Get the embedded element in the annotation str (fluxStep or simpleReaction), and the structure attribute from the element.
        Element embeddedElement = getEmbeddedElementInAnnotation(sbmlImportElement, REACTION);
        if (embeddedElement != null) {
            structName = embeddedElement.getAttributeValue(XMLTags.StructureAttrTag);
	        // Using the structName, get the structure from the structures (compartments) list.
	        struct = vcModel.getStructure(structName);
	        return struct;
        }
    }
	
	if (sbmlRxn.isSetKineticLaw()){
		//String rxnName = sbmlRxn.getId();
		KineticLaw kLaw = sbmlRxn.getKineticLaw();
		Expression kRateExp = getExpressionFromFormula(kLaw.getMath());
		String[] symbols = kRateExp.getSymbols();
		if (symbols != null) {
			for (String symbol : symbols){
				Compartment sbmlCompartment = sbmlModel.getCompartment(symbol);
				if (sbmlCompartment!=null){
					return simContext.getModel().getStructure(sbmlCompartment.getId());
				}
			}
		}
	}

    HashSet<String> refSpeciesNameHash = new HashSet<String>(); 
    getReferencedSpecies(sbmlRxn, refSpeciesNameHash);
    
    java.util.Iterator<String> refSpIterator = refSpeciesNameHash.iterator();
    HashSet<String> compartmentNamesHash = new HashSet<String>();
    while (refSpIterator.hasNext()) {
    	String spName = refSpIterator.next();
    	String rxnCompartmentName = sbmlModel.getSpecies(spName).getCompartment();
    	compartmentNamesHash.add(rxnCompartmentName);
    }
    
    if (compartmentNamesHash.size() == 1) {
    	struct = vcModel.getStructure(compartmentNamesHash.iterator().next());
    	return struct;
    } else if (compartmentNamesHash.size() == 0){
    	struct = vcModel.getStructures()[0];
    	return struct;
    } else {
    	// more than one structure in reaction participants, try to figure out which one to choose
    	HashMap<String, Integer> structureFrequencyHash = new HashMap<String, Integer>();
    	for (String structureName : compartmentNamesHash){
    		if (structureFrequencyHash.containsKey(structureName)){
    			structureFrequencyHash.put(structureName, structureFrequencyHash.get(structName)+1);
    		}else{
    			structureFrequencyHash.put(structureName, 1);
    		}
    	}
    	Iterator<Entry<String, Integer>> iterator = structureFrequencyHash.entrySet().iterator();
		Entry<String, Integer> mostUsedStructureEntry = iterator.next();
		while (iterator.hasNext()){
			Entry<String, Integer> currentStructureEntry = iterator.next();
			if (currentStructureEntry.getValue()>mostUsedStructureEntry.getValue()){
				mostUsedStructureEntry = currentStructureEntry;
			}
		}
		String mostUsedStructureName = mostUsedStructureEntry.getKey();
    	struct = vcModel.getStructure(mostUsedStructureName);
    	return struct;
    }
}


/**
 *  getSpatialDimentionBuiltInName : 
 */
/* pending delete? gcw 4/2014
private String getSpatialDimensionBuiltInName(int dimension) {
	String name = null;
	switch (dimension) {
		case 0 : {
			name = SBMLUnitTranslator.DIMENSIONLESS;
			break;
		}
		case 1 : {
			name = SBMLUnitTranslator.LENGTH;
			break;
		}
		case 2 : {
			name = SBMLUnitTranslator.AREA;
			break;
		}
		case 3 : {
			name = SBMLUnitTranslator.VOLUME;
			break;
		}						
	}
	return name;
}
*/

/**
 *  getValueFromRuleOrFunctionDefinition : 
 *	If the value of a kinetic law parameter or species initial concentration/amount (or compartment volume)
 *	is 0.0, check if it is given by a rule or functionDefinition, and return the string (of the rule or
 *	functionDefinition expression).
 */
private Expression getValueFromAssignmentRule(String paramName)  {
	Expression valueExpr = null;
	// Check if param name has an assignment rule associated with it
	int numAssgnRules = assignmentRulesHash.size();
	for (int i = 0; i < numAssgnRules; i++) {
		valueExpr = (Expression)assignmentRulesHash.get(paramName);
		if (valueExpr != null) {
			return new Expression(valueExpr);
		}
	}
	return null;
}

/* pending delete? gcw 4/2014
private boolean varHasRateRule(String paramName)  {
	// Check if param name has an assignment rule associated with it
	int numRateRules = rateRulesHash.size();
	for (int i = 0; i < numRateRules; i++) {
		Expression valueExpr = (Expression)rateRulesHash.get(paramName);
		if (valueExpr != null) {
			return true;
		}
	}
	return false;
}
*/
/**
 * checkForUnsupportedVCellFeatures:
 * 
 * Check if SBML model has algebraic, rate rules, events, other functionality that VCell does not support, 
 * such as: 'hasOnlySubstanceUnits'; compartments with dimension 0; species that have assignment rules that contain other species, etc.
 * If so, stop the import process, since there is no point proceeding with the import any further.
 * 
 */
private void checkForUnsupportedVCellFeatures() throws Exception {
	
	// Check if rules, if present, are algrbraic rules
	if (sbmlModel.getNumRules() > 0) {
		for (int i = 0; i < sbmlModel.getNumRules(); i++){
			Rule rule = (org.sbml.libsbml.Rule)sbmlModel.getRule((long)i);
			if (rule instanceof AlgebraicRule) {
				logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNSUPPORED_ELEMENTS_OR_ATTS, "Algebraic rules are not handled in the Virtual Cell at this time");
			}
		}
	}

	// Check if any of the compartments have spatial dimension 0
	for (int i = 0; i < (int)sbmlModel.getNumCompartments(); i++) {
		Compartment comp = (Compartment)sbmlModel.getCompartment(i);

		if (level > 2) {
			// level 3+ does not have default value for spatialDimension. So cannot assume a value.
			if (!comp.isSetSpatialDimensions()) {
				logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.COMPARTMENT_ERROR, "Compartment '" + comp.getId() + "' spatial dimension is not set; default value cannot be assumed in an SBML Level 3 model.");
			}
		} 
		if (comp.getSpatialDimensions() == 0 || comp.getSpatialDimensions() == 1) {
			logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.COMPARTMENT_ERROR, "Compartment " + comp.getId() + " has spatial dimension 0; this is not supported in VCell");
		}
	}
	
	// if SBML model is spatial and has events, it cannot be imported, since events are not supported in a spatial VCell model.
	if (bSpatial) {
		if (sbmlModel.getNumEvents() > 0) {
			logger.sendMessage(VCLogger.HIGH_PRIORITY, VCLogger.UNSUPPORED_ELEMENTS_OR_ATTS, "Events are not supported in a spatial Virtual Cell model at this time, they are only supported in a non-spatial model.");
		}
	}
	
}

/**
 * translateSBMLModel:
 *
 */
public void translateSBMLModel() {
	// Add Function Definitions (Lambda functions).
	addFunctionDefinitions();
	// Check for SBML features not supported in VCell; stop import process if present.
	try {
		checkForUnsupportedVCellFeatures();
	} catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
	
	// Create Virtual Cell Model with species, compartment, etc. and read in the 'values' from the SBML model

	// Add compartmentTypes (not handled in VCell)
	addCompartmentTypes();
	// Add spciesTypes (not handled in VCell)
	addSpeciesTypes();
	// Add Assignment Rules : adding these first, since compartment/species/parameter init condns could be defined by assignment rules
	try {
		addAssignmentRules();
	} catch (Exception ee) {
		ee.printStackTrace(System.out);
		throw new RuntimeException(ee.getMessage());
	}
	// Add features/compartments
	VCMetaData vcMetaData = vcBioModel.getVCMetaData();
	addCompartments(vcMetaData);
	// Add species/speciesContexts
	addSpecies(vcMetaData); 
	// Add Parameters
	try {
		addParameters();
	} catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
	// Set initial conditions on species
	setSpeciesInitialConditions();
	// Add InitialAssignments 
	addInitialAssignments();
	// Add Rules Rules : adding these later (after assignment rules, since compartment/species/parameter need to be defined before rate rules for those vars can be read in).
	try {
		addRateRules();
	} catch (ExpressionException ee) {
		ee.printStackTrace(System.out);
		throw new RuntimeException(ee.getMessage());
	}
	// Add constraints (not handled in VCell)
	addConstraints();
	// Add Reactions
	addReactions(vcMetaData);
	// Sort VCell-model Structures in structure array according to reaction adjacency and parentCompartment.
	Structure[] sortedStructures = StructureSorter.sortStructures(simContext.getModel());
	try {
		simContext.getModel().setStructures(sortedStructures);
	} catch (PropertyVetoException e1) {
		e1.printStackTrace(System.out);
		throw new RuntimeException("Error while sorting compartments: "+e1.getMessage());
	}
	
	// Add Events
	addEvents();
	// Check if names of species, structures, reactions, parameters are long (say, > 64), if so give warning.
	try {
		checkIdentifiersNameLength();
	} catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
	
	// Add geometry, if sbml model is spatial
	if (bSpatial) {
		addGeometry();
	}
}

private void checkIdentifiersNameLength() throws Exception {
	// Check compartment name lengths
	ListOf listofIds = sbmlModel.getListOfCompartments();
	boolean bLongCompartmentName = false;
	SBase issueSource = null;
	for (int i = 0; i < sbmlModel.getNumCompartments(); i++) {
		Compartment compartment = (Compartment)listofIds.get(i);
		String compartmentName = compartment.getId();
		if (compartmentName.length() > 64) {
			bLongCompartmentName = true;
			issueSource = compartment;
		}
	}
	// Check species name lengths
	listofIds = sbmlModel.getListOfSpecies();
	boolean bLongSpeciesName = false;
	for (int i = 0; i < sbmlModel.getNumSpecies(); i++) {
		org.sbml.libsbml.Species species = (org.sbml.libsbml.Species)listofIds.get(i);
		String speciesName = species.getId();
		if (speciesName.length() > 64) {
			bLongSpeciesName = true;
			issueSource = species;
		}
	}
	// Check parameter name lengths
	listofIds = sbmlModel.getListOfParameters();
	boolean bLongParameterName = false;
	for (int i = 0; i < sbmlModel.getNumParameters(); i++) {
		Parameter param = (Parameter)listofIds.get(i);
		String paramName = param.getId();
		if (paramName.length() > 64) {
			bLongParameterName = true;
			issueSource = param;
		}
	}
	// Check reaction name lengths
	listofIds = sbmlModel.getListOfReactions();
	boolean bLongReactionName = false;
	for (int i = 0; i < sbmlModel.getNumReactions(); i++) {
		Reaction rxn = (Reaction)listofIds.get(i);
		String rxnName = rxn.getId();
		if (rxnName.length() > 64) {
			bLongReactionName = true;
			issueSource = rxn;
		}
	}

	if (bLongCompartmentName || bLongSpeciesName || bLongParameterName || bLongReactionName) {
		String warningMsg = "WARNING: The imported model has one or more ";
		if (bLongCompartmentName) {
			warningMsg = warningMsg + "compartments, ";
		} 
		if (bLongSpeciesName) {
			warningMsg = warningMsg + "species, ";
		}
		if (bLongParameterName) {
			warningMsg = warningMsg + "global parameters, ";
		}
		if (bLongReactionName) {
			warningMsg = warningMsg + "reactions ";
		}
		warningMsg = warningMsg + "that have ids/names that are longer than 64 characters. \n\nUser is STRONGLY recommeded to shorten " +
						"the names to avoid problems with the length of expressions these names might be used in.";
		
		localIssueList.add(new Issue(new SBMLIssueSource(issueSource), issueContext, IssueCategory.SBMLImport_UnsupportedAttributeOrElement, warningMsg, Issue.SEVERITY_WARNING));
		// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.UNSUPPORED_ELEMENTS_OR_ATTS, warningMsg);
	}
}


private ArrayList<ReactionParticipant> getVCReactionParticipantsFromSymbol(ReactionStep reactionStep, String reactParticipantName) {

	ReactionParticipant rp_Array[] = reactionStep.getReactionParticipants();
	ArrayList<ReactionParticipant> matchingRxnParticipants = new ArrayList<ReactionParticipant>(); 
	for (int i = 0; i < rp_Array.length; i++) {
		if (AbstractNameScope.getStrippedIdentifier(reactParticipantName).equals(rp_Array[i].getSpeciesContext().getName())){
			matchingRxnParticipants.add(rp_Array[i]);
		}
	}
	return matchingRxnParticipants;
}   

/**
 * addReactions:
 *
 */
protected void addReactions(VCMetaData metaData) {
	if (sbmlModel == null) {
		throw new RuntimeException("SBML model is NULL");
	}
	ListOf listofReactions = sbmlModel.getListOfReactions();
	if (listofReactions == null) {
		System.out.println("No Reactions");
		return;
	}
	int numReactions = (int)sbmlModel.getNumReactions();
	ReactionStep[] vcReactions = new ReactionStep[numReactions];
	boolean[] fast = new boolean[numReactions];
	Model vcModel = simContext.getModel();
	ModelUnitSystem vcModelUnitSystem = vcModel.getUnitSystem();
	SpeciesContext[] vcSpeciesContexts = vcModel.getSpeciesContexts();
	try {
		for (int i = 0; i < sbmlModel.getNumReactions(); i++) {
			org.sbml.libsbml.Reaction sbmlRxn = (org.sbml.libsbml.Reaction)listofReactions.get(i);
			String rxnName = sbmlRxn.getId();
			// Check of reaction annotation is present; if so, does it have an embedded element (flux or simpleRxn).
			// Create a fluxReaction or simpleReaction accordingly.
			Element sbmlImportRelatedElement = sbmlAnnotationUtil.readVCellSpecificAnnotation(sbmlRxn);
			Structure reactionStructure = getReactionStructure(sbmlRxn, vcSpeciesContexts, sbmlImportRelatedElement);
			if (sbmlImportRelatedElement != null) {
				Element embeddedRxnElement = getEmbeddedElementInAnnotation(sbmlImportRelatedElement, REACTION);
				if (embeddedRxnElement != null) {
					if (embeddedRxnElement.getName().equals(XMLTags.FluxStepTag)) {
						// If embedded element is a flux reaction, set flux reaction's strucure, flux carrier, physicsOption from the element attributes.
						String structName = embeddedRxnElement.getAttributeValue(XMLTags.StructureAttrTag);
						Structure struct = vcModel.getStructure(structName);
						if (!(struct instanceof Membrane)) {
							throw new RuntimeException("Appears that the flux reaction is not occuring on a membrane.");
						}
						vcReactions[i] = new FluxReaction(vcModel, (Membrane)struct, null, rxnName);
						vcReactions[i].setModel(vcModel);
						// Set the fluxOption on the flux reaction based on whether it is molecular, molecular & electrical, electrical.
						String fluxOptionStr = embeddedRxnElement.getAttributeValue(XMLTags.FluxOptionAttrTag);
						if (fluxOptionStr.equals(XMLTags.FluxOptionMolecularOnly)) {
							((FluxReaction)vcReactions[i]).setPhysicsOptions(ReactionStep.PHYSICS_MOLECULAR_ONLY);
						} else if (fluxOptionStr.equals(XMLTags.FluxOptionMolecularAndElectrical)) {
							((FluxReaction)vcReactions[i]).setPhysicsOptions(ReactionStep.PHYSICS_MOLECULAR_AND_ELECTRICAL);
						} else if (fluxOptionStr.equals(XMLTags.FluxOptionElectricalOnly)) {
							((FluxReaction)vcReactions[i]).setPhysicsOptions(ReactionStep.PHYSICS_ELECTRICAL_ONLY);
						} else {
							localIssueList.add(new Issue(vcReactions[i], issueContext, IssueCategory.SBMLImport_Reaction, "Unknown FluxOption : " + fluxOptionStr + " for SBML reaction : " + rxnName, Issue.SEVERITY_WARNING));
							// logger.sendMessage(VCLogger.MEDIUM_PRIORITY, VCLogger.REACTION_ERROR, "Unknown FluxOption : " + fluxOptionStr + " for SBML reaction : " + rxnName);
						}
					} else if (embeddedRxnElement.getName().equals(XMLTags.SimpleReactionTag)) {
						// if embedded element is a simple reaction, set simple reaction's structure from element attributes
						vcReactions[i] = new SimpleReaction(vcModel, reactionStructure, rxnName);
					}
				} else {
					vcReactions[i] = new SimpleReaction(vcModel, reactionStructure, rxnName);
				}
			} else {
				vcReactions[i] = new SimpleReaction(vcModel, reactionStructure, rxnName);
			}
			
			// set annotations and notes on vcReactions[i]
			sbmlAnnotationUtil.readAnnotation(vcReactions[i], sbmlRxn);
			sbmlAnnotationUtil.readNotes(vcReactions[i], sbmlRxn);
			// record reaction name in annotation if it is greater than 64 characters. Choosing 64, since that is (as of 12/2/08) 
			// the limit on the reactionName length.
			if (rxnName.length() > 64) {
				String freeTextAnnotation = metaData.getFreeTextAnnotation(vcReactions[i]);
				if (freeTextAnnotation == null) {
					freeTextAnnotation = "";
				}
				StringBuffer oldRxnAnnotation = new StringBuffer(freeTextAnnotation);
				oldRxnAnnotation.append("\n\n" + rxnName);
				metaData.setFreeTextAnnotation(vcReactions[i], oldRxnAnnotation.toString());
			}
			

			// Now add the reactants, products, modifiers as specified by the sbmlRxn
			addReactionParticipants(sbmlRxn, vcReactions[i]);
			
			KineticLaw kLaw = sbmlRxn.getKineticLaw();
			Kinetics kinetics = null;
			if (kLaw != null) {
				// Convert the formula from kineticLaw into MathML and then to an expression (infix) to be used in VCell kinetics
				ASTNode sbmlRateMath = kLaw.getMath();
				Expression kLawRateExpr = getExpressionFromFormula(sbmlRateMath);
				Expression vcRateExpression = new Expression(kLawRateExpr);
	
				// Check the kinetic rate equation for occurances of any species in the model that is not a reaction participant.
				// If there exists any such species, it should be added as a modifier (catalyst) to the reaction.
				for (int k = 0; k < vcSpeciesContexts.length; k++){
					if (vcRateExpression.hasSymbol(vcSpeciesContexts[k].getName())) {
						if ((sbmlRxn.getReactant(vcSpeciesContexts[k].getName()) == null) && 
							(sbmlRxn.getProduct(vcSpeciesContexts[k].getName()) == null) && 
							(sbmlRxn.getModifier(vcSpeciesContexts[k].getName()) == null)) {
							// This means that the speciesContext is not a reactant, product or modifier : it has to be added to the VC Rxn as a catalyst
							vcReactions[i].addCatalyst(vcSpeciesContexts[k]);
						}
					}
				}
	
				// set kinetics on VCell reaction 
				if (bSpatial) {
					// if spatial SBML ('isSpatial' attribute set), create DistributedKinetics)
					SpatialSpeciesRxnPlugin ssrplugin = null;
					// (a) the requiredElements attributes should be 'spatial'
					ssrplugin = (SpatialSpeciesRxnPlugin)sbmlRxn.getPlugin(SBMLUtils.SBML_SPATIAL_NS_PREFIX);
					if (ssrplugin != null && ssrplugin.getIsLocal()) {
						kinetics = new GeneralKinetics(vcReactions[i]);
					} else {
						kinetics = new GeneralLumpedKinetics(vcReactions[i]);
					}
						
				} else {
					kinetics = new GeneralLumpedKinetics(vcReactions[i]);
				}
				
				// set kinetics on vcReaction
				vcReactions[i].setKinetics(kinetics);
	
				// If the name of the rate parameter has been changed by user, or matches with global/local param, 
				// it has to be changed.
				resolveRxnParameterNameConflicts(sbmlRxn, kinetics, sbmlImportRelatedElement);
				
				/**
				 * Now, based on the kinetic law expression, see if the rate is expressed in concentration/time or substance/time :
				 * If the compartment_id of the compartment corresponding to the structure in which the reaction takes place 
				 * occurs in the rate law expression, it is in concentration/time; divide it by the compartment size and bring in 
				 * the rate law as 'Distributed' kinetics. If not, the rate law is in substance/time; bring it in (as is) as 
				 * 'Lumped' kinetics. 
				 */ 
				
				ListOf listofLocalParams = kLaw.getListOfParameters();
				kinetics.setParameterValue(kinetics.getAuthoritativeParameter(), vcRateExpression);
				
				// If there are any global parameters used in the kinetics, and if they have species,
				// check if the species are already reactionParticipants in the reaction. If not, add them as catalysts.
				KineticsProxyParameter[] kpps = kinetics.getProxyParameters();
				for (int j = 0; j < kpps.length; j++) {
					if (kpps[j].getTarget() instanceof ModelParameter) {
						ModelParameter mp = (ModelParameter)kpps[j].getTarget();
					    HashSet<String> refSpeciesNameHash = new HashSet<String>(); 
					    getReferencedSpeciesInExpr(mp.getExpression(), refSpeciesNameHash);
					    java.util.Iterator<String> refSpIterator = refSpeciesNameHash.iterator();
						while (refSpIterator.hasNext()) {
					    	String spName = refSpIterator.next();
					    	org.sbml.libsbml.Species sp = sbmlModel.getSpecies(spName);
					    	ArrayList<ReactionParticipant> rpArray = getVCReactionParticipantsFromSymbol(vcReactions[i], sp.getId());
							if (rpArray == null || rpArray.size() == 0) {
								// This means that the speciesContext is not a reactant, product or modifier : it has to be added as a catalyst
								vcReactions[i].addCatalyst(vcModel.getSpeciesContext(sp.getId()));
							}
						}
					}
				}
				
				// Introduce all remaining local parameters from the SBML model - local params cannot be defined by rules.
				for (int j = 0; j < kLaw.getNumParameters(); j++) {
					org.sbml.libsbml.Parameter param = (org.sbml.libsbml.Parameter)listofLocalParams.get(j);
					String paramName = param.getId();
					// check if sbml local param is in kinetic params list; if so, add its value. 
					KineticsParameter kineticsParameter = kinetics.getKineticsParameter(paramName);
					if (kineticsParameter != null) {
						kinetics.setParameterValue(kineticsParameter, new Expression(param.getValue()));
						VCUnitDefinition paramUnit = sbmlUnitIdentifierHash.get(param.getUnits());
						if (paramUnit == null) {
							paramUnit = vcModelUnitSystem.getInstance_TBD();
						}
						kineticsParameter.setUnitDefinition(paramUnit);
					} else {
						// check if it is a proxy parameter (specifically, speciesContext or model parameter (structureSize too)).
						KineticsProxyParameter kpp = kinetics.getProxyParameter(paramName);
						// if there is a proxy param with same name as sbml kinetic local param, if proxy param
						// is a model global parameter, change proxy param to local, set its value 
						// and units to local param values
						if (kpp != null && kpp.getTarget() instanceof ModelParameter) {
							kinetics.convertParameterType(kpp, false);
							kineticsParameter = kinetics.getKineticsParameter(paramName);
							kinetics.setParameterValue(kineticsParameter, new Expression(param.getValue()));
							VCUnitDefinition paramUnit = sbmlUnitIdentifierHash.get(param.getUnits());
							kineticsParameter.setUnitDefinition(paramUnit);
						}
					}
				}
			} else {
				// sbmlKLaw was null, so creating a GeneralKinetics with 0.0 as rate.
				kinetics = new GeneralKinetics(vcReactions[i]); 
			} // end - if-else  KLaw != null

			// set the reaction kinetics, and add reaction to the vcell model.
			kinetics.resolveUndefinedUnits();
			// System.out.println("ADDED SBML REACTION : \"" + rxnName + "\" to VCModel");
			if (sbmlRxn.isSetFast() && sbmlRxn.getFast()) {
				fast[i] = true;
			} else {
				fast[i] = false;
			}
		}	// end - for vcReactions
		
		vcModel.setReactionSteps(vcReactions);

		// set 'fast' on reactionSpec in simulationContext
		for (int j = 0; j < fast.length; j++) {
			if (fast[j]) {
				simContext.getReactionContext().getReactionSpec(vcReactions[j]).setReactionMapping(ReactionSpec.FAST);
			}
		}

	} catch (Exception e1) {
		e1.printStackTrace(System.out);
		throw new RuntimeException(e1.getMessage());
	}
	
}

public static cbit.vcell.geometry.CSGNode getVCellCSGNode(org.sbml.libsbml.CSGNode sbmlCSGNode){
	String csgNodeName = sbmlCSGNode.getSpatialId();
	if (sbmlCSGNode.isCSGPrimitive()){
		String primitiveType = ((org.sbml.libsbml.CSGPrimitive)sbmlCSGNode).getPrimitiveType();
		if (primitiveType.equals(SBMLSpatialConstants.SOLID_SPHERE)){
			cbit.vcell.geometry.CSGPrimitive vcellPrimitive = new cbit.vcell.geometry.CSGPrimitive(csgNodeName, PrimitiveType.SPHERE);
			return vcellPrimitive;
		}
		if (primitiveType.equals(SBMLSpatialConstants.SOLID_CONE)){
			cbit.vcell.geometry.CSGPrimitive vcellPrimitive = new cbit.vcell.geometry.CSGPrimitive(csgNodeName, PrimitiveType.CONE);
			return vcellPrimitive;
		}
		if (primitiveType.equals(SBMLSpatialConstants.SOLID_CUBE)){
			cbit.vcell.geometry.CSGPrimitive vcellPrimitive = new cbit.vcell.geometry.CSGPrimitive(csgNodeName, PrimitiveType.CUBE);
			return vcellPrimitive;
		}
		if (primitiveType.equals(SBMLSpatialConstants.SOLID_CYLINDER)){
			cbit.vcell.geometry.CSGPrimitive vcellPrimitive = new cbit.vcell.geometry.CSGPrimitive(csgNodeName, PrimitiveType.CYLINDER);
			return vcellPrimitive;
		}
		throw new RuntimeException("PrimitiveType '" + primitiveType + "' not recognized");
	}else if (sbmlCSGNode.isCSGPseudoPrimitive()) {
		throw new RuntimeException("Pseudo primitives not yet supported in CSGeometry.");
	}else if (sbmlCSGNode.isCSGSetOperator()) {
		org.sbml.libsbml.CSGSetOperator sbmlSetOperator = (org.sbml.libsbml.CSGSetOperator)sbmlCSGNode;
		String operatorType = sbmlSetOperator.getOperationType();
		OperatorType opType = null;
		if (operatorType.equals(SBMLSpatialConstants.UNION)){
			opType = OperatorType.UNION;
		}else if (operatorType.equals(SBMLSpatialConstants.DIFFERENCE)){
			opType = OperatorType.DIFFERENCE;
		}else if (operatorType.equals(SBMLSpatialConstants.INTERSECTION)){
			opType = OperatorType.INTERSECTION;
		}else{
			throw new RuntimeException("unsupported operator type '"+operatorType+"'");
		}
		cbit.vcell.geometry.CSGSetOperator vcellSetOperator = new cbit.vcell.geometry.CSGSetOperator(csgNodeName, opType);
		for (int c = 0; c < sbmlSetOperator.getNumCSGNodeChildren(); c++){
			vcellSetOperator.addChild(getVCellCSGNode(sbmlSetOperator.getCSGNodeChild(c)));
		}
		return vcellSetOperator;
	}else if (sbmlCSGNode.isCSGTransformation()) {
		org.sbml.libsbml.CSGTransformation sbmlTransformation = (org.sbml.libsbml.CSGTransformation)sbmlCSGNode;  
		cbit.vcell.geometry.CSGNode vcellCSGChild = getVCellCSGNode(sbmlTransformation.getChild());
		if (sbmlTransformation.isCSGTranslation()) {
			org.sbml.libsbml.CSGTranslation sbmlTranslation = (org.sbml.libsbml.CSGTranslation) sbmlTransformation;
			Vect3d translation = new Vect3d(sbmlTranslation.getTranslateX(),sbmlTranslation.getTranslateY(),sbmlTranslation.getTranslateZ());
			cbit.vcell.geometry.CSGTranslation vcellTranslation = new cbit.vcell.geometry.CSGTranslation(csgNodeName, translation);
			vcellTranslation.setChild(vcellCSGChild);
			return vcellTranslation;
		}else if (sbmlTransformation.isCSGRotation()) {
			org.sbml.libsbml.CSGRotation sbmlRotation = (org.sbml.libsbml.CSGRotation) sbmlTransformation;
			Vect3d axis = new Vect3d(sbmlRotation.getRotationAxisX(),sbmlRotation.getRotationAxisY(),sbmlRotation.getRotationAxisZ());
			double rotationAngleRadians = sbmlRotation.getRotationAngleInRadians();
			cbit.vcell.geometry.CSGRotation vcellRotation = new cbit.vcell.geometry.CSGRotation(csgNodeName, axis, rotationAngleRadians);
			vcellRotation.setChild(vcellCSGChild);
			return vcellRotation;
		}else if (sbmlTransformation.isCSGScale()) {
			org.sbml.libsbml.CSGScale sbmlScale = (org.sbml.libsbml.CSGScale) sbmlTransformation;
			Vect3d scale = new Vect3d(sbmlScale.getScaleX(),sbmlScale.getScaleY(),sbmlScale.getScaleZ());
			cbit.vcell.geometry.CSGScale vcellScale = new cbit.vcell.geometry.CSGScale(csgNodeName, scale);
			vcellScale.setChild(vcellCSGChild);
			return vcellScale;
		}else if (sbmlTransformation.isCSGHomogeneousTransformation()) {
			throw new RuntimeException("homogeneous transformations not supported yet.");
		}else{
			throw new RuntimeException("unsupported type of CSGTransformation");
		}
	}else{
		throw new RuntimeException("unsupported type of CSGNode");
	}
}

protected void addGeometry() {
	// Get a SpatialModelPlugin object plugged in the model object.
	//
	// The type of the returned value of SBase::getPlugin() function is 
	// SBasePlugin*, and thus the value needs to be cast for the 
	// corresponding derived class.
	//
	SpatialModelPlugin mplugin = (SpatialModelPlugin)sbmlModel.getPlugin(SBMLUtils.SBML_SPATIAL_NS_PREFIX);

	// get a Geometry object via SpatialModelPlugin object.
	org.sbml.libsbml.Geometry sbmlGeometry = mplugin.getGeometry();

	// (2/15/2013) For now, allow model to be imported even without geometry defined. Issue a warning.
	if (sbmlGeometry.getNumGeometryDefinitions() < 1) {
		localIssueList.add(new Issue(new SBMLIssueSource(sbmlModel), issueContext, IssueCategory.SBMLImport_UnsupportedAttributeOrElement, "Geometry not deifned in spatial model.", Issue.SEVERITY_WARNING));
		return;
		// throw new RuntimeException("SBML model does not have any geometryDefinition. Cannot proceed with import.");
	}
	
	// get a CoordComponent object via the Geometry object.	
	ListOfCoordinateComponents listOfCoordComps = sbmlGeometry.getListOfCoordinateComponents();
	if (listOfCoordComps == null) {
		throw new RuntimeException("Cannot have 0 coordinate compartments in geometry"); 
	}
	// coord component
	double origX = 0.0; double origY = 0.0; double origZ = 0.0;
	double extentX = 1.0; double extentY = 1.0; double extentZ = 1.0;
	int dimension = 0;
	long dim = sbmlGeometry.getNumCoordinateComponents();
	for (int i = 0; i < dim; i++) {
		CoordinateComponent coordComponent = listOfCoordComps.get(i);
		if (coordComponent.getComponentType().equals("cartesianX") && (coordComponent.getIndex() == 0)) {
			origX = coordComponent.getBoundaryMin().getValue();
			extentX = coordComponent.getBoundaryMax().getValue() - origX;
		} else if (coordComponent.getComponentType().equals("cartesianY") && (coordComponent.getIndex() == 1)) {
			origY = coordComponent.getBoundaryMin().getValue();
			extentY = coordComponent.getBoundaryMax().getValue() - origY;
		} else if (coordComponent.getComponentType().equals("cartesianZ") && (coordComponent.getIndex() == 2)) {
			origZ = coordComponent.getBoundaryMin().getValue();
			extentZ = coordComponent.getBoundaryMax().getValue() - origZ;
		} else {
			throw new RuntimeException("unknown componentType '" + coordComponent.getComponentType() + "' : not supported in VCell");
		}
		dimension++;
	}
	
	Origin vcOrigin = new Origin(origX, origY, origZ);
	Extent vcExtent = new Extent(extentX, extentY, extentZ);
	
	// from geometry definition, find out which type of geometry : image or analytic or CSG
	
	AnalyticGeometry analyticGeometryDefinition = null;
	CSGeometry csGeometry = null;
	SampledFieldGeometry segmentedSampledFieldGeometry = null;
	SampledFieldGeometry distanceMapSampledFieldGeometry = null;
	ParametricGeometry parametricGeometry = null;
	
	for (int i = 0; i < sbmlGeometry.getNumGeometryDefinitions(); i++) {
		GeometryDefinition gd_temp = sbmlGeometry.getGeometryDefinition(i);
		if (gd_temp.isAnalyticGeometry()) {
			analyticGeometryDefinition = (AnalyticGeometry)gd_temp;
		} else if (gd_temp.isSampledFieldGeometry()){
			SampledFieldGeometry temp_sampledFieldGeometry = (SampledFieldGeometry)gd_temp;
			if (temp_sampledFieldGeometry.getSampledField().getInterpolationType().equals("linear")){
				distanceMapSampledFieldGeometry = temp_sampledFieldGeometry;
			}else if (temp_sampledFieldGeometry.getSampledField().getInterpolationType().equals("constant")){
				segmentedSampledFieldGeometry = temp_sampledFieldGeometry;
			}
		} else if (gd_temp.isCSGeometry()) {
			csGeometry = (CSGeometry)gd_temp;
		} else if (gd_temp.isParametricGeometry()) {
			parametricGeometry = (ParametricGeometry)gd_temp;
		}
	}
	
	if (analyticGeometryDefinition==null && segmentedSampledFieldGeometry==null && distanceMapSampledFieldGeometry==null && csGeometry==null) {
		throw new RuntimeException("VCell supports only Analytic, Image based (segmentd or distance map) or Constructed Solid Geometry at this time.");
	}
	GeometryDefinition selectedGeometryDefinition = null;
	if (csGeometry!=null){
		selectedGeometryDefinition = csGeometry;
	}else if (analyticGeometryDefinition!=null){
		selectedGeometryDefinition = analyticGeometryDefinition;
	}else if (segmentedSampledFieldGeometry!=null){
		selectedGeometryDefinition = segmentedSampledFieldGeometry;
	}else if (distanceMapSampledFieldGeometry!=null){
		selectedGeometryDefinition = distanceMapSampledFieldGeometry;
	}else if (parametricGeometry!=null){
		selectedGeometryDefinition = parametricGeometry;
	}else{
		throw new RuntimeException("no geometry definition found");
	}
	Geometry vcGeometry = null;
	if (selectedGeometryDefinition==analyticGeometryDefinition || selectedGeometryDefinition==csGeometry){
		vcGeometry = new Geometry("spatialGeom", dimension);
	} else if (selectedGeometryDefinition==distanceMapSampledFieldGeometry || selectedGeometryDefinition==segmentedSampledFieldGeometry){
		SampledFieldGeometry sfg = (SampledFieldGeometry)selectedGeometryDefinition;
		// get image from sampledFieldGeometry
		// get a sampledVol object via the listOfSampledVol (from SampledGeometry) object.
		
		SampledField sf = sfg.getSampledField();
		int numX = sf.getNumSamples1();
		int numY = sf.getNumSamples2();
		int numZ = sf.getNumSamples3();
		ImageData id = sf.getImageData();
		int[] samples = new int[(int) id.getSamplesLength()];
		id.getSamples(samples);
		byte[] imageInBytes = new byte[samples.length];
		if (selectedGeometryDefinition == distanceMapSampledFieldGeometry){
			//
			// single distance-map ... negative values are 1, zero and positive are 2 (for now assume that there are only two DomainTypes which are volume)
			// alternatively, one could use the marching cube algorithm to create polygons which can be super sampled.
			//
			// could resample to higher resolution segmented images (via linear interpolation).
			//
			for (int i = 0; i < imageInBytes.length; i++) {
//				if (interpolation(samples[i])<0){
				if (samples[i]<0){
					imageInBytes[i] = -1;
				}else{
					imageInBytes[i] = 1;
				}
			}
		}else{
			for (int i = 0; i < imageInBytes.length; i++) {
				imageInBytes[i] = (byte)samples[i];
			}
		}
		try {
			VCImage vcImage = null;
			if (id.getDataType().equals("compressed")) {
				vcImage = new VCImageCompressed(null, imageInBytes, vcExtent, numX, numY, numZ);
			}else if (id.getDataType().equals("uint8") || id.getDataType().equals("int16")){
				vcImage = new VCImageUncompressed(null, imageInBytes, vcExtent, numX, numY, numZ);
			}else{			
				throw new RuntimeException("Unknown dataType for imageData : datatType should be 'compressed' to be able to be imported into the Virtual Cell.");
			}
			vcImage.setName(sf.getSpatialId());
			ListOfSampledVolumes listOfSampledVols = sfg.getListOfSampledVolumes();
			if (listOfSampledVols == null) {
				throw new RuntimeException("Cannot have 0 sampled volumes in sampledField (image_based) geometry"); 
			}
			int numSampledVols = (int)sfg.getNumSampledVolumes();
			VCPixelClass[] vcpixelClasses = new VCPixelClass[numSampledVols]; 
			// get pixel classes for geometry
			for (int i = 0; i < numSampledVols; i++) {
				SampledVolume sVol = listOfSampledVols.get(i);
				// from subVolume, get pixelClass?
				vcpixelClasses[i] = new VCPixelClass(null, sVol.getSpatialId(), (int)sVol.getSampledValue());
			}
			vcImage.setPixelClasses(vcpixelClasses);
			// now create image geometry
			vcGeometry = new Geometry("spatialGeom", vcImage);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Unable to create image from SampledFieldGeometry : " + e.getMessage());
		} 
	}
	GeometrySpec vcGeometrySpec = vcGeometry.getGeometrySpec();
	vcGeometrySpec.setOrigin(vcOrigin);
	try {
		vcGeometrySpec.setExtent(vcExtent);
	} catch (PropertyVetoException e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Unable to set extent on VC geometry : " + e.getMessage());
	}

	// get  listOfDomainTypes via the Geometry object.	
	ListOfDomainTypes listOfDomainTypes = sbmlGeometry.getListOfDomainTypes();
	if (listOfDomainTypes == null || listOfDomainTypes.size() < 1) {
		throw new RuntimeException("Cannot have 0 domainTypes in geometry"); 
	}
	// get a listOfDomains via the Geometry object.	
	ListOfDomains listOfDomains = sbmlGeometry.getListOfDomains();
	if (listOfDomains == null || listOfDomains.size() < 1) {
		throw new RuntimeException("Cannot have 0 domains in geometry"); 
	}
	
//	ListOfGeometryDefinitions listOfGeomDefns = sbmlGeometry.getListOfGeometryDefinitions();
//	if ((listOfGeomDefns == null) || (sbmlGeometry.getNumGeometryDefinitions() > 1)) {
//		throw new RuntimeException("Can have only 1 geometry definition in geometry");
//	}
	// use the boolean bAnalytic to create the right kind of subvolume. First match the somVol=domainTypes for spDim=3. Deal witl spDim=2 afterwards.
	GeometrySurfaceDescription vcGsd = vcGeometry.getGeometrySurfaceDescription();
	Vector<DomainType> surfaceClassDomainTypesVector = new Vector<DomainType>();
	try {
		for (int i = 0; i < sbmlGeometry.getNumDomainTypes(); i++) {
			DomainType dt = listOfDomainTypes.get(i);
			if (dt.getSpatialDimensions() == 3) {
				// subvolume
				if (selectedGeometryDefinition == analyticGeometryDefinition) {
					// will set expression later - when reading in Analytic Volumes in GeometryDefinition
					vcGeometrySpec.addSubVolume(new AnalyticSubVolume(dt.getSpatialId(), new Expression(1.0)));
				} else {
					// add SubVolumes later for CSG and Image-based
				}
			} else if (dt.getSpatialDimensions() == 2) {
				surfaceClassDomainTypesVector.add(dt);
			}
		}

		// get an AnalyticGeometry object via the Geometry object. The analytic vol is needed to get the expression for subVols
		if (selectedGeometryDefinition == analyticGeometryDefinition) {
			// get an analyticVol object via the listOfAnalyticVol (from AnalyticGeometry) object.	
			ListOfAnalyticVolumes listOfAnalyticVols = analyticGeometryDefinition.getListOfAnalyticVolumes();
			if (listOfAnalyticVols == null || listOfAnalyticVols.size() < 1) {
				throw new RuntimeException("Cannot have 0 Analytic volumes in analytic geometry"); 
			}
			for (int i = 0; i < analyticGeometryDefinition.getNumAnalyticVolumes(); i++) {
				// get subVol from VC geometry using analyticVol spatialId; set its expr using analyticVol's math.
				AnalyticVolume analyticVol = listOfAnalyticVols.get(i);
				SubVolume vcSubvolume = vcGeometrySpec.getSubVolume(analyticVol.getDomainType());
				if (vcSubvolume == null) {
					throw new RuntimeException("analytic volume '" + analyticVol.getSpatialId() + "' does not map to any VC subvolume.");
				}
				try {
					Expression subVolExpr = getExpressionFromFormula(analyticVol.getMath());
					((AnalyticSubVolume)vcSubvolume).setExpression(subVolExpr);
				} catch (ExpressionException e) {
					e.printStackTrace(System.out);
					throw new RuntimeException("Unable to set expression on subVolume '" + vcSubvolume.getName() + "'. " + e.getMessage());
				}
			}
		}
		if (selectedGeometryDefinition instanceof SampledFieldGeometry) {
			SampledFieldGeometry sfg = (SampledFieldGeometry)selectedGeometryDefinition;
			ListOfSampledVolumes listOfSampledVols = sfg.getListOfSampledVolumes();
			if (listOfSampledVols == null) {
				throw new RuntimeException("Cannot have 0 sampled volumes in sampledField (image_based) geometry"); 
			}
			int numSampledVols = (int)sfg.getNumSampledVolumes();
			VCPixelClass[] vcpixelClasses = new VCPixelClass[numSampledVols]; 
			// get pixel classes for geometry
			for (int i = 0; i < numSampledVols; i++) {
				SampledVolume sVol = listOfSampledVols.get(i);
				// from subVolume, get pixelClass?
				vcpixelClasses[i] = new VCPixelClass(null, sVol.getSpatialId(), (int)sVol.getSampledValue());
			}

			ImageSubVolume[] vcImageSubVols = new ImageSubVolume[numSampledVols]; 
			for (int i = 0; i < numSampledVols; i++) {
				SampledVolume sVol = listOfSampledVols.get(i);
				// find the pixel class corresponding to pixel value from sVol.sampledValue.
				VCPixelClass pixelClass = null;
				for (int j = 0; j < vcpixelClasses.length; j++){
					if (vcpixelClasses[j].getPixel() == (int)sVol.getSampledValue()){
						pixelClass = vcpixelClasses[j];
					}
				}
				//Create the new Image SubVolume - use index of this for loop as 'handle' for ImageSubVol?
				vcImageSubVols[i] = new ImageSubVolume(null, pixelClass, i);
				vcImageSubVols[i].setName(sVol.getSpatialId());
			}
			vcGeometry.getGeometrySpec().setSubVolumes(vcImageSubVols);
		}
		if (selectedGeometryDefinition == csGeometry) {
			ListOfCSGObjects listOfcsgObjs = csGeometry.getListOfCSGObjects();
			int numCSGObjects = (int)csGeometry.getNumCSGObjects();
			//ArrayList<CSGObject> vcCSGSubVolumes = new ArrayList<CSGObject>(); 
			PriorityQueue<CSGObjectSorter> csgQueue = new PriorityQueue<CSGObjectSorter>(); 
			for (int kk = 0; kk < numCSGObjects;kk++) {
				org.sbml.libsbml.CSGObject sbmlCSGObject = listOfcsgObjs.get(kk);
				int index = numCSGObjects - ((int)sbmlCSGObject.getOrdinal()+1);
				// indx = n - (ordinal+1) : we want the CSGObj with highest ordinal to be the first element in the CSG subvols array. 
				// insert vcCSGObj at position 'indx' in arraylist 
				CSGObject vcellCSGObject = new CSGObject(null, sbmlCSGObject.getDomainType(), kk);
				vcellCSGObject.setRoot(getVCellCSGNode(sbmlCSGObject.getCSGNodeRoot()));
				//vcCSGSubVolumes.add(index, vcellCSGObject);
				csgQueue.add(new CSGObjectSorter(vcellCSGObject, index));
			}
			assert csgQueue.size() == numCSGObjects;
			CSGObject vcCSGSubVolumes []= new CSGObject[numCSGObjects];
			for (int i = 0; i < numCSGObjects; i++) {
				vcCSGSubVolumes[i] = csgQueue.poll().cSGObject; 
			}
			assert csgQueue.isEmpty(); //no leftovers!
			vcGeometry.getGeometrySpec().setSubVolumes(vcCSGSubVolumes);
		}
		
		// Call geom.geomSurfDesc.updateAll() to automatically generate surface classes.
//		vcGsd.updateAll();
		vcGeometry.precomputeAll(new GeometryThumbnailImageFactoryAWT(), true, true);
	}   catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Unable to create VC subVolumes from SBML domainTypes : " + e.getMessage());
	}
	
	
	// should now map each SBML domain to right VC geometric region.
	GeometricRegion[] vcGeomRegions = vcGsd.getGeometricRegions(); 
	ISize sampleSize = vcGsd.getVolumeSampleSize();
	RegionInfo[] regionInfos = vcGsd.getRegionImage().getRegionInfos();
	int numX = sampleSize.getX();
	int numY = sampleSize.getY();
	int numZ = sampleSize.getZ();
	double ox = vcOrigin.getX();
	double oy = vcOrigin.getY();
	double oz = vcOrigin.getZ();

	for (int i = 0; i < sbmlGeometry.getNumDomains(); i++) {
		Domain domain = listOfDomains.get(i);
		String domainType = domain.getDomainType();
		InteriorPoint interiorPt = domain.getInteriorPoint(0);
		if (interiorPt == null && sbmlGeometry.getDomainType(domainType).getSpatialDimensions() == 2) {
			continue;
		}
		Coordinate sbmlInteriorPtCoord = new Coordinate(interiorPt.getCoord1(), interiorPt.getCoord2(), interiorPt.getCoord3());
		for (int j = 0; j < vcGeomRegions.length; j++) {
			if (vcGeomRegions[j] instanceof VolumeGeometricRegion) {
				int regionID = ((VolumeGeometricRegion)vcGeomRegions[j]).getRegionID();
				for (int k = 0; k < regionInfos.length; k++) {
					// get the regionInfo corresponding to the vcGeomRegion (using gemoRegion regionID).
					if (regionInfos[k].getRegionIndex() == regionID) {
						int volIndx = 0;
						Coordinate nearestPtCoord = null;
						double minDistance = Double.MAX_VALUE;
						// for each point in the region, find it if is close to 'sbmlInteriorPt'. If it is, this is the region represented by SBML 'domain[i]'. 
						for (int z = 0; z < numZ; z++) {
							for (int y = 0; y < numY; y++) {
								for (int x = 0; x < numX; x++) {
									if (regionInfos[k].isIndexInRegion(volIndx)) {
										double unit_z = (numZ>1)?((double)z)/(numZ-1):0.5;
										double coordZ = oz + vcExtent.getZ() * unit_z;
										double unit_y = (numY>1)?((double)y)/(numY-1):0.5;
										double coordY = oy + vcExtent.getY() * unit_y;
										double unit_x = (numX>1)?((double)x)/(numX-1):0.5;
										double coordX = ox + vcExtent.getX() * unit_x;
										// for now, find the shortest dist coord. Can refine algo later.
										Coordinate vcCoord = new Coordinate(coordX, coordY, coordZ);
										double distance = sbmlInteriorPtCoord.distanceTo(vcCoord);
										if (distance < minDistance) {
											minDistance = distance;
											nearestPtCoord = vcCoord;
										}
									}
									volIndx++;
								}	// end - for x
							}	// end - for y
						}	// end - for z
						// verify that domainType of domain and geomClass of geomRegion are the same; if so, name vcGeomReg[j] with domain name
						if (nearestPtCoord != null) {
							GeometryClass geomClassSBML = vcGeometry.getGeometryClass(domainType);
							// we know vcGeometryReg[j] is a VolGeomRegion
							GeometryClass geomClassVC = ((VolumeGeometricRegion)vcGeomRegions[j]).getSubVolume();
							if (geomClassSBML.compareEqual(geomClassVC)) {
								vcGeomRegions[j].setName(domain.getSpatialId());
							}
						}
					}	// end if (regInfoIndx = regId)
				}	// end - for regInfo 
			} 
		}	// end for - vcGeomRegions
	}	// end - for domains
	
	// now that we have the subVolumes:spDim3-domainTypes mapped, we need to deal with surfaceClass:spDim2-domainTypes
	for (int i = 0; i < surfaceClassDomainTypesVector.size(); i++) {
		DomainType surfaceClassDomainType = surfaceClassDomainTypesVector.elementAt(i);
		// get the domain that has the same 'domainType' field as 'surfaceClassDomainType'
		for (int j = 0; j < sbmlGeometry.getNumDomains(); j++) {
			Domain d = listOfDomains.get(j);
			if (d.getDomainType().equals(surfaceClassDomainType.getSpatialId())) {
				// get the adjacent domains of this 'surface' domain (surface domain + its 2 adj vol domains)
				Set<Domain> adjacentDomainsSet = getAssociatedAdjacentDomains(sbmlGeometry, d);
				// get the domain types of the adjacent domains in SBML and store the corresponding subVol counterparts from VC for adj vol domains 
				Vector<SubVolume> adjacentSubVolumesVector = new Vector<SubVolume>();
				Vector<VolumeGeometricRegion> adjVolGeomRegionsVector = new Vector<VolumeGeometricRegion>();
				Iterator<Domain> iterator = adjacentDomainsSet.iterator();
				while (iterator.hasNext()) {
					Domain dom = iterator.next();
					DomainType dt = sbmlGeometry.getDomainType(dom.getDomainType());
					if (dt.getSpatialDimensions() == 3) {
						// for domain type with sp. dim = 3, get correspoinding subVol from VC geometry.
						GeometryClass gc = vcGeometry.getGeometryClass(dt.getSpatialId());
						adjacentSubVolumesVector.add((SubVolume)gc);
						// store volGeomRegions corresponding to this (vol) geomClass in adjVolGeomRegionsVector : this should return ONLY 1 region for subVol.
						GeometricRegion[] geomRegion = vcGsd.getGeometricRegions(gc);	
						adjVolGeomRegionsVector.add((VolumeGeometricRegion)geomRegion[0]);
					}
				}
				// there should be only 2 subVols in this vector
				if (adjacentSubVolumesVector.size() != 2) {
					throw new RuntimeException("Cannot have more or less than 2 subvolumes that are adjacent to surface (membrane) '" + d.getSpatialId() + "'"); 
				}
				// get the surface class with these 2 adj subVols. Set its name to that of 'surfaceClassDomainType'
				SurfaceClass surfacClass = vcGsd.getSurfaceClass(adjacentSubVolumesVector.get(0), adjacentSubVolumesVector.get(1));
				surfacClass.setName(surfaceClassDomainType.getSpatialId());
				// get surfaceGeometricRegion that has adjVolGeomRegions as its adjacent vol geom regions and set its name from domain 'd'
				SurfaceGeometricRegion surfaceGeomRegion = getAssociatedSurfaceGeometricRegion(vcGsd, adjVolGeomRegionsVector);
				if (surfaceGeomRegion != null) {
					surfaceGeomRegion.setName(d.getSpatialId());
				}
			}	// end if - domain.domainType == surfaceClassDomainType
		}	// end for - numDomains
	}	// end surfaceClassDomainTypesVector
		
	// structureMappings in VC from compartmentMappings in SBML
	try {
		// set geometry first and then set structureMappings?
		simContext.setGeometry(vcGeometry);
		// update simContextName ...
		simContext.setName(simContext.getName() + "_" + vcGeometry.getName());
		Model vcModel = simContext.getModel();
		ModelUnitSystem vcModelUnitSystem = vcModel.getUnitSystem();
		
		Vector<StructureMapping> structMappingsVector = new Vector<StructureMapping>();
		Compartment c;
		SpatialCompartmentPlugin cplugin = null;
		for (int i = 0; i < sbmlModel.getNumCompartments(); i++) {
			c = sbmlModel.getCompartment(i);
			cplugin = (SpatialCompartmentPlugin)c.getPlugin("spatial");
			CompartmentMapping compMapping = cplugin.getCompartmentMapping();
			Structure struct = vcModel.getStructure(compMapping.getCompartment());
			String domainType = compMapping.getDomainType();
			GeometryClass geometryClass = vcGeometry.getGeometryClass(domainType);
			double unitSize = compMapping.getUnitSize();
			if (struct instanceof Feature) {
				FeatureMapping featureMapping = new FeatureMapping((Feature)struct, simContext, vcModelUnitSystem);
				featureMapping.setGeometryClass(geometryClass);
				if (geometryClass instanceof SubVolume) {
					featureMapping.getVolumePerUnitVolumeParameter().setExpression(new Expression(unitSize));
				} else if (geometryClass instanceof SurfaceClass) {
					featureMapping.getVolumePerUnitAreaParameter().setExpression(new Expression(unitSize));
				}
				structMappingsVector.add(featureMapping);
			} else if (struct instanceof Membrane) {
				MembraneMapping membraneMapping = new MembraneMapping((Membrane)struct, simContext, vcModelUnitSystem);
				membraneMapping.setGeometryClass(geometryClass);
				if (geometryClass instanceof SubVolume) {
					membraneMapping.getAreaPerUnitVolumeParameter().setExpression(new Expression(unitSize));
				} else if (geometryClass instanceof SurfaceClass) {
					membraneMapping.getAreaPerUnitAreaParameter().setExpression(new Expression(unitSize));
				}
				structMappingsVector.add(membraneMapping);
			}
		}
		StructureMapping[] structMappings = structMappingsVector.toArray(new StructureMapping[0]);
		simContext.getGeometryContext().setStructureMappings(structMappings);

		// if type from SBML parameter Boundary Condn is not the same as the boundary type of the 
		// structureMapping of structure of paramSpContext, set the boundary condn type of the structureMapping
		// to the value of 'type' from SBML parameter Boundary Condn. 
		ListOfParameters listOfGlobalParams = sbmlModel.getListOfParameters();
		CoordinateComponent ccX = sbmlGeometry.getCoordinateComponent(vcModel.getX().getName());
		CoordinateComponent ccY = sbmlGeometry.getCoordinateComponent(vcModel.getY().getName());
		CoordinateComponent ccZ = sbmlGeometry.getCoordinateComponent(vcModel.getZ().getName());

		for (int i = 0; i < sbmlModel.getNumParameters(); i++){
			Parameter sbmlGlobalParam = (Parameter)listOfGlobalParams.get(i);
			SpatialParameterPlugin spplugin = (SpatialParameterPlugin)sbmlGlobalParam.getPlugin(SBMLUtils.SBML_SPATIAL_NS_PREFIX);
			BoundaryCondition bCondn = spplugin.getBoundaryCondition();
			if (bCondn.isSetVariable()) {
				// get the var of boundaryCondn; find appropriate spContext in vcell; 
				SpeciesContext paramSpContext = simContext.getModel().getSpeciesContext(bCondn.getVariable());
				if (paramSpContext != null) {
					StructureMapping sm = simContext.getGeometryContext().getStructureMapping(paramSpContext.getStructure());
					if (bCondn.getCoordinateBoundary().equals(ccX.getBoundaryMin().getSpatialId())) {
						String bcXmType = bCondn.getType();
						if (!sm.getBoundaryConditionTypeXm().boundaryTypeStringValue().equals(bcXmType)) {
							sm.setBoundaryConditionTypeXm(new BoundaryConditionType(bcXmType));
						}
					} else if (bCondn.getCoordinateBoundary().equals(ccX.getBoundaryMax().getSpatialId())) {
						String bcXpType = bCondn.getType();
						if (!sm.getBoundaryConditionTypeXp().boundaryTypeStringValue().equals(bcXpType)) {
							sm.setBoundaryConditionTypeXp(new BoundaryConditionType(bcXpType));
						}
					} else if (bCondn.getCoordinateBoundary().equals(ccY.getBoundaryMin().getSpatialId())) {
						String bcYmType = bCondn.getType();
						if (!sm.getBoundaryConditionTypeYm().boundaryTypeStringValue().equals(bcYmType)) {
							sm.setBoundaryConditionTypeYm(new BoundaryConditionType(bcYmType));
						}
					} else if (bCondn.getCoordinateBoundary().equals(ccY.getBoundaryMax().getSpatialId())) {
						String bcYpType = bCondn.getType();
						if (!sm.getBoundaryConditionTypeYp().boundaryTypeStringValue().equals(bcYpType)) {
							sm.setBoundaryConditionTypeYp(new BoundaryConditionType(bcYpType));
						}
					} else if (bCondn.getCoordinateBoundary().equals(ccZ.getBoundaryMin().getSpatialId())) {
						String bcZmType = bCondn.getType();
						if (!sm.getBoundaryConditionTypeZm().boundaryTypeStringValue().equals(bcZmType)) {
							sm.setBoundaryConditionTypeZm(new BoundaryConditionType(bcZmType));
						}
					} else if (bCondn.getCoordinateBoundary().equals(ccZ.getBoundaryMax().getSpatialId())) {
						String bcZpType = bCondn.getType();
						if (!sm.getBoundaryConditionTypeZp().boundaryTypeStringValue().equals(bcZpType)) {
							sm.setBoundaryConditionTypeZp(new BoundaryConditionType(bcZpType));
						}
					}
				}	// end if (paramSpContext != null)
			}	// end if (bCondn.isSetVar()) 
		}	// end for (sbmlModel.numParams)

		simContext.getGeometryContext().refreshStructureMappings();
	}  catch (Exception e) {
		e.printStackTrace(System.out);
		throw new RuntimeException("Unable to create VC structureMappings from SBML compartment mappings : " + e.getMessage());
	}
}

private SurfaceGeometricRegion getAssociatedSurfaceGeometricRegion(GeometrySurfaceDescription vcGsd, Vector<VolumeGeometricRegion> volGeomRegionsVector) {
	GeometricRegion[] geomeRegions = vcGsd.getGeometricRegions();
	// adjVolGeomRegionsVector should have only 2 elements - the 2 adj volGeomRegions for any surfaceRegion.
	VolumeGeometricRegion[] volGeomRegionsArray = volGeomRegionsVector.toArray(new VolumeGeometricRegion[0]);
	for (int i = 0; i < geomeRegions.length; i++) {
		if (geomeRegions[i] instanceof SurfaceGeometricRegion) {
			SurfaceGeometricRegion surfaceRegion = (SurfaceGeometricRegion)geomeRegions[i];
			GeometricRegion[] adjVolGeomRegs = surfaceRegion.getAdjacentGeometricRegions();
			// adjVolGeomRegs array should also have 2 elements : the 2 adj volGeomRegions for surfaceRegion.
			// if the 2 arrays do not have 2 elements each, throw exception
			if (volGeomRegionsArray.length != 2 && adjVolGeomRegs.length != 2) {
				throw new RuntimeException("There should be 2 adjacent geometric regions for surfaceRegion '" + surfaceRegion.getName() + "'");
			}
			// if the vol geomtric regions in both arrays match, we have a winner! - return surfaceRegion
			if ((adjVolGeomRegs[0].compareEqual(volGeomRegionsArray[0]) && adjVolGeomRegs[1].compareEqual(volGeomRegionsArray[1])) || 
				(adjVolGeomRegs[0].compareEqual(volGeomRegionsArray[1]) && adjVolGeomRegs[1].compareEqual(volGeomRegionsArray[0]))) {
				return surfaceRegion;
			}
		}
	}
	return null;
}


// collect the domains that are adjacent to each other. Typically, 'd' is a 'surface' domain. We scan through adjacent domains to get the 
// the surface domain and its adjacent volume domains. The set returned should have 3 domains.
private Set<Domain> getAssociatedAdjacentDomains(org.sbml.libsbml.Geometry sbmlGeom, Domain d) {
	Set<Domain> adjacentDomainsSet = new HashSet<Domain>();
	for (int i = 0; i < sbmlGeom.getNumAdjacentDomains(); i++) {
		AdjacentDomains adjDomains = sbmlGeom.getAdjacentDomains(i);
		if (adjDomains.getDomain1().equals(d.getSpatialId()) || adjDomains.getDomain2().equals(d.getSpatialId())) {
			if (!adjacentDomainsSet.contains(sbmlGeom.getDomain(adjDomains.getDomain1()))) {
				adjacentDomainsSet.add(sbmlGeom.getDomain(adjDomains.getDomain1()));
			}
			if (!adjacentDomainsSet.contains(sbmlGeom.getDomain(adjDomains.getDomain2()))) {
				adjacentDomainsSet.add(sbmlGeom.getDomain(adjDomains.getDomain2()));
			}
		}
	}
	return adjacentDomainsSet;
}


}
