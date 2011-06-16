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
import java.util.Vector;

import org.vcell.util.CommentStringTokenizer;
import org.vcell.util.Compare;
import org.vcell.util.Matchable;
import org.vcell.util.document.KeyValue;

import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;

public class FluxReaction extends ReactionStep {
	private Species fieldFluxCarrier = null;

public FluxReaction(Membrane membrane, KeyValue argKey, String name) throws PropertyVetoException {
    super(membrane, argKey, name);
	try {
		setKinetics(new GeneralKinetics(this));
	}catch (Exception e){
		e.printStackTrace(System.out);
	}
}


public FluxReaction(Membrane membrane, Species fluxCarrier, Model model, KeyValue key,String name) throws Exception {
	super(membrane,key,name);
	setFluxCarrier(fluxCarrier,model);
	try {
		setKinetics(new GeneralKinetics(this));
	}catch (Exception e){
		e.printStackTrace(System.out);
	}
}   


public FluxReaction(Membrane membrane, Species fluxCarrier, Model model,String name) throws Exception {
	this(membrane,fluxCarrier, model, null, name);
}   


/**
 * This method was created in VisualAge.
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(Matchable obj) {
	if (obj instanceof FluxReaction){
		FluxReaction fr = (FluxReaction)obj;
		if (!super.compareEqual0(fr)){
			return false;
		}
		if (!Compare.isEqualOrNull(getFluxCarrier(),fr.getFluxCarrier())){
			return false;
		}
		return true;
	}else{
		return false;
	}
}


/**
 * This method was created by a SmartGuide.
 * @param tokens java.util.StringTokenizer
 * @exception java.lang.Exception The exception description.
 */
public void fromTokens(CommentStringTokenizer tokens, Model model) throws Exception {
	String token = null;
	while (tokens.hasMoreTokens()){
		token = tokens.nextToken();
		if (token.equalsIgnoreCase(VCMODL.EndBlock)){
			break;
		}			
		if (token.equalsIgnoreCase(VCMODL.Valence)){
			getChargeCarrierValence().setExpression(new Expression((double)Integer.parseInt(tokens.nextToken())));
			continue;
		}
		if (token.equalsIgnoreCase(VCMODL.PhysicsOptions)){
			setPhysicsOptions(Integer.parseInt(tokens.nextToken()));
			continue;
		}
		if (token.equalsIgnoreCase(VCMODL.Catalyst)){
			Catalyst catalyst = new Catalyst(null,this);
			catalyst.fromTokens(tokens,model);
			addReactionParticipant(catalyst);
			continue;
		}
		if (token.equalsIgnoreCase(VCMODL.Kinetics)){
			String kineticsType = tokens.nextToken();	// read MassActionKinetics or GeneralKinetics
			if (kineticsType==null){
				throw new Exception("unexpected EOF in FluxReaction");
			}
			KineticsDescription kineticsDescription = KineticsDescription.fromVCMLKineticsName(kineticsType);
			if (kineticsDescription!=null){
				Kinetics kinetics = kineticsDescription.createKinetics(this);
				setKinetics(kinetics);
				getKinetics().fromTokens(tokens);
			}else{
				throw new Exception("unknown kinetic type '"+kineticsType+"' in FluxReaction");
			}						
			continue;
		}
		throw new Exception("FluxReaction.fromTokens(), unexpected identifier "+token);
	}
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.model.Flux
 * @param feature cbit.vcell.model.Feature
 */
public Flux getFlux(Feature feature) {
	ReactionParticipant rp_Array[] = getReactionParticipants();
	
	for (int i = 0; i < rp_Array.length; i++) { 
		if (rp_Array[i] instanceof Flux){
			Flux flux = (Flux)rp_Array[i];
			if (flux.getStructure().compareEqual(feature)){
				return flux;
			}
		}
	}
	return null;
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.model.Flux
 * @param feature cbit.vcell.model.Feature
 */
public Flux[] getFluxes() {
	ReactionParticipant rp_Array[] = getReactionParticipants();
	Vector<Flux> fluxList = new Vector<Flux>();
	for (int i = 0; i < rp_Array.length; i++) { 
		if (rp_Array[i] instanceof Flux){
			Flux flux = (Flux)rp_Array[i];
			fluxList.add(flux);
		}
	}
	return fluxList.toArray(new Flux[fluxList.size()]);
}


/**
 * Gets the fluxCarrier property (cbit.vcell.model.Species) value.
 * @return The fluxCarrier property value.
 * @see #setFluxCarrier
 */
public Species getFluxCarrier() {
	return fieldFluxCarrier;
}


/**
 * This method was created in VisualAge.
 * @return double
 * @param speciesContext cbit.vcell.model.SpeciesContext
 */
public int getStoichiometry(SpeciesContext speciesContext) {
	ReactionParticipant[] rps = getReactionParticipants();
	for (ReactionParticipant rp : rps) {
		if (rp.getSpeciesContext() == speciesContext) {
			if (rp instanceof Flux) {
				Membrane membrane = (Membrane)getStructure();
				Structure structure = speciesContext.getStructure();
				if (structure == membrane.getInsideFeature()){
					return rps[0].getStoichiometry();
				}else if (structure == membrane.getOutsideFeature()){
					return -rps[0].getStoichiometry();
				}
			}
		}
	}
	return 0;
}


/**
 * Insert the method's description here.
 * Creation date: (5/22/00 10:21:47 PM)
 * @return java.lang.String
 */
public String getTerm() {
	return "Flux Reaction";
}


/**
 * This method was created in VisualAge.
 */
private void removeAllFluxes() throws ModelException, PropertyVetoException {
	boolean fluxesLeft=true;
	while (fluxesLeft){
		fluxesLeft = false;
		ReactionParticipant rp_Array[] = getReactionParticipants();
		
		for (int i = 0; i < rp_Array.length; i++) {
			if (rp_Array[i] instanceof Flux){
				try {
					removeReactionParticipant(rp_Array[i]);
				}catch (ExpressionException e){
					throw new ModelException("Exception removing "+rp_Array[i]+": "+e.getMessage());
				}
				fluxesLeft=true;
			}
		}
	}
}


/**
 * Sets the fluxCarrier property (cbit.vcell.model.Species) value.
 * @param fluxCarrier The new value for the property.
 * @see #getFluxCarrier
 */
public void setFluxCarrier(Species fluxCarrier, Model model) throws PropertyVetoException, ModelException {
	Species oldValue = fieldFluxCarrier;
	fieldFluxCarrier = fluxCarrier;
	if (oldValue!=fieldFluxCarrier){
		if (model!=null){
			removeAllFluxes();

			Feature outside = ((Membrane)getStructure()).getOutsideFeature();
			Feature inside = ((Membrane)getStructure()).getInsideFeature();
			//
			// add inside flux
			//
			SpeciesContext insideSC = model.getSpeciesContext(fluxCarrier,inside);
			if (insideSC==null){
				throw new ModelException("interior speciesContext for "+fluxCarrier.getCommonName()+" within feature "+inside.getName()+" not defined");
			}
			addReactionParticipant(new Flux(null,this,insideSC));

			//
			// add outside flux
			//
			SpeciesContext outsideSC = model.getSpeciesContext(fluxCarrier,outside);
			if (outsideSC==null){
				throw new ModelException("exterior speciesContext for "+fluxCarrier.getCommonName()+" within feature "+outside.getName()+" not defined");
			}
			addReactionParticipant(new Flux(null,this,outsideSC));	
		}
		firePropertyChange("fluxCarrier", oldValue, fluxCarrier);
	}
}


/**
 * This method was created by a SmartGuide.
 * @param kinetics cbit.vcell.model.GeneralKinetics
 */
public void setKinetics(Kinetics kinetics) throws IllegalArgumentException {
	if (!(kinetics instanceof MassActionKinetics)){
		super.setKinetics(kinetics);
	}else{
		throw new IllegalArgumentException("kinetics must not be MassActionKinetics");
	}
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	return "FluxReaction@"+Integer.toHexString(hashCode())+"("+getKey()+", "+getName()+", "+getReactionParticipants().length+" reactParticipants, valence="+getChargeCarrierValence()+", physicsOption="+getPhysicsOptions()+")";

}

}
