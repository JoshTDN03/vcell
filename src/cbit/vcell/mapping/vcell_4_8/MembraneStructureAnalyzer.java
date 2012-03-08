/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.mapping.vcell_4_8;

import java.util.Vector;

import cbit.vcell.geometry.SubVolume;
import cbit.vcell.mapping.FeatureMapping;
import cbit.vcell.mapping.GeometryContext;
import cbit.vcell.mapping.ReactionSpec;
import cbit.vcell.mapping.SpeciesContextSpec;
import cbit.vcell.model.DistributedKinetics;
import cbit.vcell.model.Feature;
import cbit.vcell.model.FluxReaction;
import cbit.vcell.model.LumpedKinetics;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Product;
import cbit.vcell.model.Reactant;
import cbit.vcell.model.ReactionParticipant;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.parser.Expression;
/**
 * This type was created in VisualAge.
 */
class MembraneStructureAnalyzer extends StructureAnalyzer {
	private SubVolume innerSubVolume = null;
	private SubVolume outerSubVolume = null;
	private Membrane membrane = null;
	private ResolvedFlux resolvedFluxes[] = null;

	public static boolean bResolvedFluxCorrectionBug = false;
	public static boolean bResolvedFluxCorrectionBugExercised = false;
	public static boolean bNoFluxIfFixed = false;
	public static boolean bNoFluxIfFixedExercised = false;
/**
 * MembraneStructureAnalyzer constructor comment.
 * @param mathMapping_4_8 cbit.vcell.mapping.MathMapping
 * @param subVolume cbit.vcell.geometry.SubVolume
 */
MembraneStructureAnalyzer(MathMapping_4_8 mathMapping_4_8, Membrane membrane, SubVolume innerSubVolume, SubVolume outerSubVolume) {
	super(mathMapping_4_8);
	this.innerSubVolume = innerSubVolume;
	this.outerSubVolume = outerSubVolume;
	this.membrane = membrane;
	//refresh();
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.geometry.SubVolume
 */
SubVolume getInnerSubVolume() {
	return innerSubVolume;
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.model.Membrane
 */
Membrane getMembrane() {
	return membrane;
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.geometry.SubVolume
 */
SubVolume getOuterSubVolume() {
	return outerSubVolume;
}
/**
 * This method was created in VisualAge.
 * @return UndefinedObject[]
 */
ResolvedFlux[] getResolvedFluxes() {
	return resolvedFluxes;
}
/**
 * This method was created in VisualAge.
 */
void refresh() {
	super.refresh();
	try {
		refreshResolvedFluxes();
	}catch (Exception e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
}
/**
 * This method was created in VisualAge.
 */
void refreshResolvedFluxes() throws Exception {

//System.out.println("MembraneStructureAnalyzer.refreshResolvedFluxes()");

	GeometryContext geoContext = mathMapping_4_8.getSimulationContext().getGeometryContext();
	Vector<ResolvedFlux> resolvedFluxList = new Vector<ResolvedFlux>();

	//
	// for each reaction, get all fluxReactions associated with this membrane
	//
	Vector<ReactionStep> fluxList = new Vector<ReactionStep>();
	ReactionSpec reactionSpecs[] = mathMapping_4_8.getSimulationContext().getReactionContext().getReactionSpecs();
	for (int j=0;j<reactionSpecs.length;j++){
		if (reactionSpecs[j].isExcluded()){
			continue;
		}
		ReactionStep rs = reactionSpecs[j].getReactionStep();
		if (rs.getStructure()==getMembrane()){
			if (rs instanceof FluxReaction){
				fluxList.addElement(rs);
			}
		}
	}

	//
	// for each FluxReaction, add fluxes to influx and subtract from outflux
	//
	for (int i=0;i<fluxList.size();i++){
		FluxReaction fr = (FluxReaction)fluxList.elementAt(i);
		if (fr.getFluxCarrier() == null) {
			continue;
		}
		ResolvedFlux rf = null;
		for (int j=0;j<resolvedFluxList.size();j++){
			ResolvedFlux rf_tmp = (ResolvedFlux)resolvedFluxList.elementAt(j);
			if (rf_tmp.getSpecies() == fr.getFluxCarrier()){
				rf = rf_tmp;
			}
		}
		//
		// if "inside" speciesContext is not "fixed", add flux to ResolvedFlux
		//
		SpeciesContext insideSpeciesContext = mathMapping_4_8.getSimulationContext().getModel().getSpeciesContext(fr.getFluxCarrier(),getMembrane().getInsideFeature());
		SpeciesContextSpec insideSpeciesContextSpec = mathMapping_4_8.getSimulationContext().getReactionContext().getSpeciesContextSpec(insideSpeciesContext);
		//
		// introduce bug compatability mode for NoFluxIfFixed bug
		//
		//if (!insideSpeciesContextSpec.isConstant()){
		if (bNoFluxIfFixed || !insideSpeciesContextSpec.isConstant()){
			if (bNoFluxIfFixed && insideSpeciesContextSpec.isConstant()){
				bNoFluxIfFixedExercised = true;
			}
			if (rf == null){
				rf = new ResolvedFlux(fr.getFluxCarrier());
				resolvedFluxList.addElement(rf);
			}
			FeatureMapping insideFeatureMapping = (FeatureMapping)geoContext.getStructureMapping(((Membrane)fr.getStructure()).getInsideFeature());
			
			Expression residualVolumeFraction = mathMapping_4_8.getResidualVolumeFraction(insideFeatureMapping).renameBoundSymbols(mathMapping_4_8.getNameScope());
			Expression insideFluxCorrection = Expression.invert(residualVolumeFraction);
			//
			// introduce bug compatability mode for resolved flux correction
			//
			if (bResolvedFluxCorrectionBug && !residualVolumeFraction.compareEqual(new Expression(1.0))){
				bResolvedFluxCorrectionBugExercised = true;
				System.out.println("MembraneStructureAnalyzer.refreshResolvedFluxes() ... 'ResolvedFluxCorrection' bug compatability mode");
				insideFluxCorrection = new Expression(1.0);
			}
			//
			// add flux term to ResolvedFlux.inFlux
			//
			if (fr.getKinetics() instanceof DistributedKinetics){
				Expression reactionRateParameter = new Expression(((DistributedKinetics)fr.getKinetics()).getReactionRateParameter(), mathMapping_4_8.getNameScope());
				if (rf.inFlux.isZero()){
					rf.inFlux = Expression.mult(reactionRateParameter,insideFluxCorrection).flatten();
				}else{
					rf.inFlux = Expression.add(rf.inFlux,Expression.mult(reactionRateParameter,insideFluxCorrection).flatten());
				}
			}else if (fr.getKinetics() instanceof LumpedKinetics){
				throw new RuntimeException("Lumped Kinetics for fluxes not yet supported");
			}else{
				throw new RuntimeException("unexpected Kinetic type in MembraneStructureAnalyzer.refreshResolvedFluxes()");
			}
//			rf.inFlux.bindExpression(mathMapping);
		}
		SpeciesContext outsideSpeciesContext = mathMapping_4_8.getSimulationContext().getModel().getSpeciesContext(fr.getFluxCarrier(),getMembrane().getOutsideFeature());
		SpeciesContextSpec outsideSpeciesContextSpec = mathMapping_4_8.getSimulationContext().getReactionContext().getSpeciesContextSpec(outsideSpeciesContext);
		//
		// introduce bug compatability mode for NoFluxIfFixed bug
		//
		//if (!outsideSpeciesContextSpec.isConstant()){
		if (bNoFluxIfFixed || !outsideSpeciesContextSpec.isConstant()){
			if (bNoFluxIfFixed && outsideSpeciesContextSpec.isConstant()){
				bNoFluxIfFixedExercised = true;
			}
			if (rf == null){
				rf = new ResolvedFlux(fr.getFluxCarrier());
				resolvedFluxList.addElement(rf);
			}
			FeatureMapping outsideFeatureMapping = (FeatureMapping)geoContext.getStructureMapping(((Membrane)fr.getStructure()).getOutsideFeature());
			Expression residualVolumeFraction = mathMapping_4_8.getResidualVolumeFraction(outsideFeatureMapping).renameBoundSymbols(mathMapping_4_8.getNameScope());
			Expression outsideFluxCorrection = Expression.invert(residualVolumeFraction);
			//
			// introduce bug compatability mode for resolved flux correction
			//
			if (bResolvedFluxCorrectionBug && !residualVolumeFraction.compareEqual(new Expression(1.0))){
				bResolvedFluxCorrectionBugExercised = true;
				System.out.println("MembraneStructureAnalyzer.refreshResolvedFluxes() ... 'ResolvedFluxCorrection' bug compatability mode");
				outsideFluxCorrection = new Expression(1.0);
			}
			//
			// sub flux term to resolvedFlux.outFlux
			//
			if (fr.getKinetics() instanceof DistributedKinetics){
				Expression reactionRateParameter = new Expression(((DistributedKinetics)fr.getKinetics()).getReactionRateParameter(), mathMapping_4_8.getNameScope());
				if (rf.outFlux.isZero()){
					rf.outFlux = Expression.mult(Expression.negate(reactionRateParameter),outsideFluxCorrection).flatten();
				}else{
					rf.outFlux = Expression.add(rf.outFlux,Expression.mult(Expression.negate(reactionRateParameter),outsideFluxCorrection).flatten());
				}
			}else if (fr.getKinetics() instanceof LumpedKinetics){
				throw new RuntimeException("Lumped Kinetics not yet supported for Flux Reaction: "+fr.getName());
			}else{
				throw new RuntimeException("unexpected Kinetics type for Flux Reaction "+fr.getName());
			}
//			rf.outFlux.bindExpression(mathMapping);
		}
	}
	
	//
	// for each reaction, incorporate all reactionSteps involving binding with volumetric species
	//
	for (int i=0;i<reactionSpecs.length;i++){
		if (reactionSpecs[i].isExcluded()){
			continue;
		}
		ReactionStep rs = reactionSpecs[i].getReactionStep();
		if (rs.getStructure()==getMembrane()){
			if (rs instanceof SimpleReaction){
				SimpleReaction sr = (SimpleReaction)rs;
				ReactionParticipant rp_Array[] = sr.getReactionParticipants();
				for (int k = 0; k < rp_Array.length; k++) {
					if (rp_Array[k] instanceof Reactant || rp_Array[k] instanceof Product){
						SpeciesContextSpec scs = mathMapping_4_8.getSimulationContext().getReactionContext().getSpeciesContextSpec(rp_Array[k].getSpeciesContext());
						//
						// for volume species that are not "fixed", add fluxes to "ResolvedFlux"
						//

						//
						// introduce bug compatability mode for NoFluxIfFixed bug
						//
						// if (rp_Array[k].getStructure() instanceof Feature && !scs.isConstant()){
						if (rp_Array[k].getStructure() instanceof Feature && (bNoFluxIfFixed || !scs.isConstant())){
							if (bNoFluxIfFixed && scs.isConstant()){
								bNoFluxIfFixedExercised = true;
							}
							//
							// for each Reactant or Product binding to this membrane...
							//

							//
							// get ResolvedFlux for this species
							//
							ResolvedFlux rf = null;
							for (int j=0;j<resolvedFluxList.size();j++){
								ResolvedFlux rf_tmp = (ResolvedFlux)resolvedFluxList.elementAt(j);
								if (rf_tmp.getSpecies() == rp_Array[k].getSpecies()){
									rf = rf_tmp;
								}
							}
							if (rf == null){
								rf = new ResolvedFlux(rp_Array[k].getSpecies());
								resolvedFluxList.addElement(rf);
							}
							
							Expression reactionRateExpression = sr.getReactionRateExpression(rp_Array[k]).renameBoundSymbols(mathMapping_4_8.getNameScope());
							cbit.vcell.model.Model.ReservedSymbol kMole = mathMapping_4_8.getSimulationContext().getModel().getKMOLE();
							if (rp_Array[k].getStructure() == getMembrane().getInsideFeature()){
								//
								// for binding on inside, add to ResolvedFlux.inFlux
								//
								FeatureMapping insideFeatureMapping = (FeatureMapping)geoContext.getStructureMapping(getMembrane().getInsideFeature());
								Expression residualVolumeFraction = mathMapping_4_8.getResidualVolumeFraction(insideFeatureMapping).renameBoundSymbols(mathMapping_4_8.getNameScope());
								Expression insideFluxCorrection = Expression.div(new Expression(kMole, mathMapping_4_8.getNameScope()), residualVolumeFraction).flatten();
								//
								// introduce bug compatability mode for resolved flux correction
								//
								if (bResolvedFluxCorrectionBug && !residualVolumeFraction.compareEqual(new Expression(1.0))){
									bResolvedFluxCorrectionBugExercised = true;
									System.out.println("MembraneStructureAnalyzer.refreshResolvedFluxes() ... 'ResolvedFluxCorrection' bug compatability mode");
									insideFluxCorrection = new Expression(kMole, mathMapping_4_8.getNameScope());
								}
								if (rf.inFlux.isZero()){
									rf.inFlux = Expression.mult(insideFluxCorrection, reactionRateExpression);
								}else{
									rf.inFlux = Expression.add(rf.inFlux,Expression.mult(insideFluxCorrection, reactionRateExpression));
								}
//								rf.inFlux.bindExpression(mathMapping);
							}else if (rp_Array[k].getStructure() == getMembrane().getOutsideFeature()){
								//
								// for binding on outside, add to ResolvedFlux.outFlux
								//
								FeatureMapping outsideFeatureMapping = (FeatureMapping)geoContext.getStructureMapping(getMembrane().getOutsideFeature());
								Expression residualVolumeFraction = mathMapping_4_8.getResidualVolumeFraction(outsideFeatureMapping).renameBoundSymbols(mathMapping_4_8.getNameScope());
								Expression outsideFluxCorrection = Expression.div(new Expression(kMole, mathMapping_4_8.getNameScope()), residualVolumeFraction).flatten();
								//
								// introduce bug compatability mode for resolved flux correction
								//
								if (bResolvedFluxCorrectionBug && !residualVolumeFraction.compareEqual(new Expression(1.0))){
									bResolvedFluxCorrectionBugExercised = true;
									System.out.println("MembraneStructureAnalyzer.refreshResolvedFluxes() ... 'ResolvedFluxCorrection' bug compatability mode");
									outsideFluxCorrection = new Expression(kMole, mathMapping_4_8.getNameScope());
								}
								if (rf.outFlux.isZero()){
									rf.outFlux = Expression.mult(outsideFluxCorrection, reactionRateExpression);
								}else{
									rf.outFlux = Expression.add(rf.outFlux,Expression.mult(outsideFluxCorrection, reactionRateExpression));
								}
//								rf.outFlux.bindExpression(mathMapping);
							}else{
								throw new Exception("SpeciesContext "+rp_Array[k].getSpeciesContext().getName()+" doesn't border membrane "+getMembrane().getName()+" but reacts there");
							}
						}
					}
				}					
			}
		}
	}

	
	//
	// copy Vector into resolvedFluxes[] array
	//
	if (resolvedFluxList.size()>0){
		resolvedFluxes = new ResolvedFlux[resolvedFluxList.size()];
		resolvedFluxList.copyInto(resolvedFluxes);
	}else{
		resolvedFluxes=null;
	}
}
/**
 * Build list of structures (just one membrane) that are mapped to this volume subdomain
 */
protected void refreshStructures() {
	structures = new Structure[1];
	structures[0] = membrane;
}
}
