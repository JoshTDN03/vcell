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
import org.vcell.util.Matchable;
import org.vcell.util.document.KeyValue;


public class Flux extends ReactionParticipant
{
	public static enum FluxDirection {
		Reactant, Product, Unknown;
	}
/**
 * This method was created in VisualAge.
 * @param reactionStep cbit.vcell.model.ReactionStep
 */
public Flux(KeyValue key, FluxReaction fluxReaction, SpeciesContext speciesContext) {
	super(key, fluxReaction, speciesContext, 1);
}


/**
 * This method was created in VisualAge.
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(Matchable obj) {
	if (obj instanceof Flux){
		Flux f = (Flux)obj;
		return compareEqual0(f);
	}else{
		return false;
	}
}

public FluxDirection getFluxDirection() {
	if (getStructure() == null) {
		return FluxDirection.Unknown;
	} 
	Membrane membrane = (Membrane)getReactionStep().getStructure();
	if (getStructure() == membrane.getInsideFeature()) {
		return FluxDirection.Product;
	} else 	if (getStructure() == membrane.getOutsideFeature()) {
		return FluxDirection.Reactant;
	}

	return FluxDirection.Unknown;
}
/**
 * This method was created by a SmartGuide.
 * @param tokens java.util.StringTokenizer
 * @exception java.lang.Exception The exception description.
 */
public void fromTokens(org.vcell.util.CommentStringTokenizer tokens, Model model) throws Exception {

	throw new Exception("not implemented");
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	String scName = (getSpeciesContext()!=null)?(getSpeciesContext().getName()):"null";
	return "Flux(id="+getKey()+", speciesContext="+scName+"')";
}


/**
 * This method was created by a SmartGuide.
 * @param ps java.io.PrintStream
 * @exception java.lang.Exception The exception description.
 */
public void writeTokens(java.io.PrintWriter pw) {
	System.out.println("not implemented");
}
}
