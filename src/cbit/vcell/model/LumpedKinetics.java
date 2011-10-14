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

import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;


/**
 * LumpedKinetics is the abstract superclass of all reaction kinetics that operate on pools of molecules 
 * and describe the rate of change of the total number of molecules or total current across a membrane.
 * 
 * For electrical transport, total current (rather than current density) is the "lumped" description of
 * charge transport.
 * 
 * For nonspatial descriptions, this can be a convenient form.  However, for spatial models either the
 * LumpedKinetics has to be translated to a corresponding DistributedKinetics (describing a distributed 
 * parameter system) or these will map to Region variables and Region equations that described lumped quantities.
 * 
 * A LumpedKinetics may be formed from a corresponding DistributedKinetics by integrating the local behavior 
 * over a given compartment of known size.  For nonspatial models, no assumptions are necessary, for spatial models
 * an assumption of uniform behavior over the compartment is required (e.g. no gradients or inhomogenieties).
 *
 * @see DistributedKinetics
 *
 */
public abstract class LumpedKinetics extends Kinetics {

	public LumpedKinetics(String name, ReactionStep reactionStep) {
		super(name, reactionStep);
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (8/6/2002 3:37:07 PM)
	 * @return cbit.vcell.model.KineticsParameter
	 */
	public KineticsParameter getLumpedReactionRateParameter() {
		return getKineticsParameterFromRole(ROLE_LumpedReactionRate);
	}

	public final KineticsParameter getAuthoritativeParameter(){
		if (getKineticsDescription().isElectrical()){
			return getLumpedCurrentParameter();
		}else{
			return getLumpedReactionRateParameter();
		}
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (8/6/2002 3:37:07 PM)
	 * @return cbit.vcell.model.KineticsParameter
	 */
	public KineticsParameter getLumpedCurrentParameter() {
		return getKineticsParameterFromRole(ROLE_LumpedCurrent);
	}

	public static LumpedKinetics toLumpedKinetics(DistributedKinetics distributedKinetics, double size){
		KineticsParameter[] distKineticsParms = distributedKinetics.getKineticsParameters();
		ReactionStep reactionStep = distributedKinetics.getReactionStep();
		try {
			Vector<KineticsParameter> parmsToAdd = new Vector<KineticsParameter>();
			
			LumpedKinetics lumpedKinetics = null;
			Expression sizeExpr = new Expression(size);
			if (distributedKinetics.getKineticsDescription().isElectrical()){
				lumpedKinetics = new GeneralCurrentLumpedKinetics(reactionStep);
				Expression lumpingFactor = sizeExpr; // from pA.um-2 to pA (current density to current)
				KineticsParameter distCurrentDensityParam = distributedKinetics.getCurrentDensityParameter();
				KineticsParameter lumpedCurrentParam = lumpedKinetics.getLumpedCurrentParameter();
				Expression newLumpedCurrentExp = Expression.mult(lumpingFactor,distCurrentDensityParam.getExpression()).flatten();
				parmsToAdd.add(lumpedKinetics.new KineticsParameter(lumpedCurrentParam.getName(),newLumpedCurrentExp,lumpedCurrentParam.getRole(),lumpedCurrentParam.getUnitDefinition()));
			}else{
				lumpedKinetics = new GeneralLumpedKinetics(reactionStep);
				Expression lumpingFactor = null;
				Expression kmole = distributedKinetics.getSymbolExpression(ReservedSymbol.KMOLE);
				if (reactionStep.getStructure() instanceof Membrane){
					if (reactionStep instanceof FluxReaction){
						// size/KMOLE  (from uM.um.s-1 to molecules.s-1)
						lumpingFactor = Expression.div(sizeExpr, kmole);
					}else if (reactionStep instanceof SimpleReaction){
						// size (from molecules.um-2.s-1 to molecules.s-1)
						lumpingFactor = sizeExpr;
					}else{
						throw new RuntimeException("unexpected ReactionStep type "+reactionStep.getClass().getName());
					}
				}else if (reactionStep.getStructure() instanceof Feature){
					// size/KMOLE (from uM.s-1 to molecules.s-1)
					lumpingFactor = Expression.div(sizeExpr, kmole);
				}else{
					throw new RuntimeException("unexpected structure type "+reactionStep.getStructure().getClass().getName());
				}
				KineticsParameter distReactionRateParam = distributedKinetics.getReactionRateParameter();
				KineticsParameter lumpedReactionRateParm = lumpedKinetics.getLumpedReactionRateParameter();
				Expression newLumpedRateExp = Expression.mult(lumpingFactor,distReactionRateParam.getExpression()).flatten();
				parmsToAdd.add(lumpedKinetics.new KineticsParameter(lumpedReactionRateParm.getName(),newLumpedRateExp,lumpedReactionRateParm.getRole(),lumpedReactionRateParm.getUnitDefinition()));
			}
			for (int i = 0; i < distKineticsParms.length; i++) {
				if (distKineticsParms[i].getRole()!=Kinetics.ROLE_ReactionRate &&
					distKineticsParms[i].getRole()!=Kinetics.ROLE_CurrentDensity){
					parmsToAdd.add(lumpedKinetics.new KineticsParameter(distKineticsParms[i].getName(),new Expression(distKineticsParms[i].getExpression()),Kinetics.ROLE_UserDefined,distKineticsParms[i].getUnitDefinition()));
				}
			}
			lumpedKinetics.addKineticsParameters(parmsToAdd.toArray(new KineticsParameter[parmsToAdd.size()]));
			return lumpedKinetics;
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw new RuntimeException("failed to create lumped Kinetics for reaction: \""+reactionStep.getName()+"\": "+e.getMessage());
		} catch (ExpressionException e) {
			e.printStackTrace();
			throw new RuntimeException("failed to create lumped Kinetics for reaction: \""+reactionStep.getName()+"\": "+e.getMessage());
		}
	}

}
