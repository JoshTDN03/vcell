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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import org.vcell.util.DataAccessException;
import org.vcell.util.Matchable;
import org.vcell.util.document.KeyValue;

import cbit.vcell.modeldb.ReactStepDbDriver.Flux;
import cbit.vcell.xml.XMLTags;
import cbit.vcell.xml.XmlParseException;

public class FluxReaction extends ReactionStep {
//	private Species fieldFluxCarrier = null;

public FluxReaction(Model model, Membrane membrane, KeyValue argKey, String name) throws PropertyVetoException {
    super(model, membrane, argKey, name);
	try {
		setKinetics(new GeneralKinetics(this));
	}catch (Exception e){
		e.printStackTrace(System.out);
	}
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
//		if (!Compare.isEqualOrNull(getFluxCarrier(),fr.getFluxCarrier())){
//			return false;
//		}
		return true;
	}else{
		return false;
	}
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

public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
	super.vetoableChange(e);
		
	if (e.getSource() == this && e.getPropertyName().equals("reactionParticipants")) {
		ArrayList<Reactant> reactants = new ArrayList<Reactant>();
		ArrayList<Product> products = new ArrayList<Product>();
		ReactionParticipant[] newReactionParticipants = (ReactionParticipant[])e.getNewValue();
		for (ReactionParticipant rp : newReactionParticipants){
			if (rp instanceof Reactant){
				reactants.add((Reactant)rp);
				}
			if (rp instanceof Product){
				products.add((Product)rp);
			}
		}
		// fluxreaction cannot have more than one reactant and product
		if (reactants.size() > 1 || products.size() > 1) {
			throw new RuntimeException("FluxReaction '" + getName() + "' can have only one reactant and one product.");
		}

		if (reactants.size() > 0 && products.size() > 0) {
			Reactant reactant = reactants.get(0);
			Product product = products.get(0);
			// if the speciesContexts for the reactant and product are the same, throw exception : cannot have same speciesContext as reactant and product for a flux reaction.
			if (reactant.getSpeciesContext() == product.getSpeciesContext()) {
				throw new PropertyVetoException("FluxReaction '" + getName() + "' cannot have the same species as reactant and product.", e);
			}
	
			// if the structure for the reactant and product are the same, throw exception. Cannot have reactant and product in the same structure.
			if (reactant.getStructure() == product.getStructure()) {
				throw new PropertyVetoException("FluxReaction '" + getName() + "' cannot have its reactant and product in the same structure.", e);
			}
	
			// reactant or product should not be in the same structure (membrane) as the flux reaction.
			if (reactant.getStructure() == this.getStructure() || product.getStructure() == this.getStructure()) {
				throw new PropertyVetoException("Reactant or product cannot be in the same membrane as the flux reaction '" + getName() + "'.", e);
			}
		}
	}
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	return "FluxReaction@"+Integer.toHexString(hashCode())+"("+getKey()+", "+getName()+", "+getReactionParticipants().length+" reactParticipants, physicsOption="+getPhysicsOptions()+")";

}

@Override
public void setReactionParticipantsFromDatabase(Model model, ReactionParticipant[] reactionParticipants) throws DataAccessException, PropertyVetoException {
	ArrayList<ReactionParticipant> participants = new ArrayList<ReactionParticipant>();
	Membrane membrane = (Membrane)getStructure();
	for (ReactionParticipant participant : reactionParticipants){
		if (participant instanceof Flux){
			// replace "Flux" objects with Reactants and Products.
			Flux flux = (Flux)participant;
			Structure structure = flux.getStructure();
			if (model.getStructureTopology()!=null){
				if (model.getStructureTopology().getInsideFeature(membrane) == structure){
					Product product = new Product(null, this);
					product.setSpeciesContext(flux.getSpeciesContext());
					participants.add(product);
				}else if (model.getStructureTopology().getOutsideFeature(membrane) == structure){
					Reactant reactant = new Reactant(null, this);
					reactant.setSpeciesContext(flux.getSpeciesContext());
					participants.add(reactant);
				}else{
					throw new DataAccessException("unable to translate Flux reaction \""+getName()+"\" saved prior to version 5.3, can't reconcile structure topology");
				}
			}else{
				throw new DataAccessException("unable to translate Flux reaction \""+getName()+"\" saved prior to version 5.3, has no structure topology");
			}
		}else{
			participants.add(participant);
		}
	}
	setReactionParticipants(participants.toArray( new ReactionParticipant[participants.size()] ));
}

}
