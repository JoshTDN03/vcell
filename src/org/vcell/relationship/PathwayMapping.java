/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.relationship;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.vcell.pathway.BioPAXUtil;
import org.vcell.pathway.BioPAXUtil.Process;
import org.vcell.pathway.BioPaxObject;
import org.vcell.pathway.ComplexAssembly;
import org.vcell.pathway.Conversion;
import org.vcell.pathway.InteractionParticipant;
import org.vcell.pathway.PhysicalEntity;
import org.vcell.pathway.Transport;
import org.vcell.pathway.kinetics.SBPAXKineticsExtractor;
import org.vcell.pathway.sbpax.SBEntity;
import org.vcell.util.TokenMangler;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.client.desktop.biomodel.ConversionTableRow;
import cbit.vcell.model.Catalyst;
import cbit.vcell.model.FluxReaction;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Model;
import cbit.vcell.model.Product;
import cbit.vcell.model.Reactant;
import cbit.vcell.model.ReactionParticipant;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.Species;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.parser.ExpressionException;

import com.ibm.icu.util.StringTokenizer;

public class PathwayMapping {
	
	public void createBioModelEntitiesFromBioPaxObjects(BioModel bioModel, ArrayList<ConversionTableRow> conversionTableRows) throws Exception
	{
		for(ConversionTableRow ctr : conversionTableRows){
			if(ctr.getBioPaxObject() instanceof PhysicalEntity){
				createSpeciesContextFromTableRow(bioModel, (PhysicalEntity)ctr.getBioPaxObject(), ctr.stoich(), ctr.id(), ctr.location());
			}else if(ctr.getBioPaxObject() instanceof ComplexAssembly){ // Conversion : ComplexAssembly
				createReactionStepsFromTableRow(bioModel, (ComplexAssembly)ctr.getBioPaxObject(), ctr.stoich(), ctr.id(), ctr.location(), conversionTableRows);
			}else if(ctr.getBioPaxObject() instanceof Transport){ // Conversion : Transport
				createReactionStepsFromTableRow(bioModel, (Transport)ctr.getBioPaxObject(), ctr.stoich(), ctr.id(), ctr.location(), conversionTableRows);
//			}else if(ctr.getBioPaxObject() instanceof Degradation){ // Conversion : Degradation 
//				// to do
			}else if(ctr.getBioPaxObject() instanceof Conversion){ // Conversion : BiochemicalReaction
				createReactionStepsFromTableRow(bioModel, (Conversion)ctr.getBioPaxObject(), ctr.stoich(), ctr.id(), ctr.location(), conversionTableRows);
			}
		}
	}
	
	public void createBioModelEntitiesFromBioPaxObjects(BioModel bioModel, Object[] selectedObjects) throws Exception
	{
		for(int i = 0; i < selectedObjects.length; i++){
			if(selectedObjects[i] instanceof BioPaxObject){
				BioPaxObject bioPaxObject = (BioPaxObject)selectedObjects[i];
				if(bioPaxObject instanceof PhysicalEntity){
					createSpeciesContextFromBioPaxObject(bioModel, (PhysicalEntity)bioPaxObject);
				}else if(bioPaxObject instanceof Conversion){
					createReactionStepsFromBioPaxObject(bioModel, (Conversion)bioPaxObject);
				}
			}else if(selectedObjects[i] instanceof ConversionTableRow){
				ConversionTableRow ctr = (ConversionTableRow)selectedObjects[i];
				if(ctr.getBioPaxObject() instanceof PhysicalEntity){
					createSpeciesContextFromTableRow(bioModel, (PhysicalEntity)ctr.getBioPaxObject(), ctr.stoich(), ctr.id(), ctr.location());
				}else if(ctr.getBioPaxObject() instanceof Conversion){
					createReactionStepsFromTableRow(bioModel, (Conversion)ctr.getBioPaxObject(), ctr.stoich(), ctr.id(), ctr.location());
				}
			}
		}
	}
	
	private SpeciesContext createSpeciesContextFromBioPaxObject(BioModel bioModel, PhysicalEntity bioPaxObject) throws Exception
	{
		String name;
		if(bioPaxObject.getName().size() == 0){
			name = getSafetyName(bioPaxObject.getID());
		}else{
			name = getSafetyName(bioPaxObject.getName().get(0));
		}
		Model model = bioModel.getModel();
		SpeciesContext freeSpeciesContext = model.getSpeciesContext(name);
		if(freeSpeciesContext == null){
		// create the new speciesContex Object, and link it to the corresponding pathway object
			if(model.getSpecies(name) == null){
				freeSpeciesContext = model.createSpeciesContext(model.getStructures()[0]);
			}else{
				 freeSpeciesContext = new SpeciesContext(model.getSpecies(name), model.getStructures()[0]);
			}
			freeSpeciesContext.setName(name);
			RelationshipObject newRelationship = new RelationshipObject(freeSpeciesContext, bioPaxObject);
			bioModel.getRelationshipModel().addRelationshipObject(newRelationship);		
		}else{
			// if it is in the bioModel, then check whether it links to pathway object or not
			HashSet<RelationshipObject> linkedReObjects = 
				bioModel.getRelationshipModel().getRelationshipObjects(freeSpeciesContext);
			if(linkedReObjects != null){
				boolean flag = true;
				for(RelationshipObject reObject: linkedReObjects){
					if(reObject.getBioPaxObject() == bioPaxObject){
						flag = false;
						break;
					}
				}
				if(flag){
					RelationshipObject newSpeciesContext = new RelationshipObject(
							freeSpeciesContext, bioPaxObject);
					bioModel.getRelationshipModel().addRelationshipObject(newSpeciesContext);
				}
			}else{
				RelationshipObject newSpeciesContext = new RelationshipObject(
						freeSpeciesContext, bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newSpeciesContext);
			}
		}
		return freeSpeciesContext;
	}
	
	private SpeciesContext createSpeciesContextFromTableRow(BioModel bioModel, PhysicalEntity bioPaxObject, 
			double stoich, String id, String location) throws Exception
	{
		// use user defined id as the name of the speciesContext
		String safeId = getSafetyName(id);
		String name;
		if(bioPaxObject.getName().size() == 0){
			name = getSafetyName(bioPaxObject.getID());
		}else{
			name = getSafetyName(bioPaxObject.getName().get(0));
		}
		Model model = bioModel.getModel();
		SpeciesContext freeSpeciesContext = model.getSpeciesContext(safeId);
		if(freeSpeciesContext == null){
		// create the new speciesContex Object, and link it to the corresponding pathway object
			if(model.getSpecies(name) == null){
				freeSpeciesContext = model.createSpeciesContext(model.getStructure(location));
			}else {
				freeSpeciesContext = new SpeciesContext(model.getSpecies(name), model.getStructure(location));
			}
			freeSpeciesContext.setName(safeId);
			RelationshipObject newRelationship = new RelationshipObject(freeSpeciesContext, bioPaxObject);
			bioModel.getRelationshipModel().addRelationshipObject(newRelationship);		
		}else{
			// if it is in the bioModel, then check whether it links to pathway object or not
			HashSet<RelationshipObject> linkedReObjects = 
				bioModel.getRelationshipModel().getRelationshipObjects(freeSpeciesContext);
			if(linkedReObjects != null){
				boolean flag = true;
				for(RelationshipObject reObject: linkedReObjects){
					if(reObject.getBioPaxObject() == bioPaxObject){
						flag = false;
						break;
					}
				}
				if(flag){
					RelationshipObject newSpeciesContext = new RelationshipObject(
							freeSpeciesContext, bioPaxObject);
					bioModel.getRelationshipModel().addRelationshipObject(newSpeciesContext);
				}
			}else{
				RelationshipObject newSpeciesContext = new RelationshipObject(
						freeSpeciesContext, bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newSpeciesContext);
			}
		}
		return freeSpeciesContext;
	}
	
	private void createReactionStepsFromBioPaxObject(BioModel bioModel, Conversion conversion) throws Exception
	{
		for(Process process :BioPAXUtil.getAllProcesses(bioModel, conversion)) {
			String name = process.getName();
			if(bioModel.getModel().getReactionStep(name) == null){
				// create a new reactionStep object
				ReactionStep simpleReactionStep = bioModel.getModel().createSimpleReaction(bioModel.getModel().getStructures()[0]);
				simpleReactionStep.setName(name);
				RelationshipObject newRelationship = new RelationshipObject(simpleReactionStep, conversion);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStepFromPathway(bioModel, process, simpleReactionStep, newRelationship);
			}else{
				// add missing parts for the existing reactionStep
				RelationshipObject newRelationship = new RelationshipObject(bioModel.getModel().getReactionStep(name), conversion);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStepFromPathway(bioModel, process, bioModel.getModel().getReactionStep(name), newRelationship);
			}
		}
	}

	private void createReactionStepsFromTableRow(BioModel bioModel, Conversion bioPaxObject,
			double stoich, String id, String location, ArrayList<ConversionTableRow> conversionTableRows) throws Exception
			{
		// use user defined id as the name of the reaction name
		// get participants of this reaction from table rows
		for(Process process: BioPAXUtil.getAllProcesses(bioModel, bioPaxObject)) {
			ArrayList<ConversionTableRow> participants = new ArrayList<ConversionTableRow>();
			for(ConversionTableRow ctr : conversionTableRows){
				if(ctr.interactionId().equals(bioPaxObject.getID())){
					participants.add(ctr);
				}
			}
			// create reaction object
			String name = getSafetyName(process.getName() + "_" + location);
			ReactionStep reactionStep = bioModel.getModel().getReactionStep(name);
			if(reactionStep == null){
				// create a new reactionStep object
				ReactionStep simpleReactionStep = bioModel.getModel().createSimpleReaction(bioModel.getModel().getStructure(location));
				simpleReactionStep.setName(name);
				RelationshipObject newRelationship = new RelationshipObject(simpleReactionStep, bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStep(bioModel, process, simpleReactionStep, newRelationship, participants);
				addKinetics(simpleReactionStep, process);
			}else{
				//			bioModel.getModel().getReactionStep(safeId).setStructure(bioModel.getModel().getStructure(location));
				// add missing parts for the existing reactionStep
				RelationshipObject newRelationship = new RelationshipObject(reactionStep, bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStep(bioModel, process, reactionStep, newRelationship, participants);
				addKinetics(reactionStep, process);
			}
		}
	}
	
	private void createReactionStepsFromTableRow(BioModel bioModel, ComplexAssembly bioPaxObject,
			double stoich, String id, String location, ArrayList<ConversionTableRow> conversionTableRows) throws Exception
	{
		// use user defined id as the name of the reaction name
		// get participants from table rows
//		for(Process process: BioPAXUtil.getAllProcesses(bioModel.getPathwayModel(), bioPaxObject)) {
		for(Process process: BioPAXUtil.getAllProcesses(bioModel, bioPaxObject)) {
			ArrayList<ConversionTableRow> participants = new ArrayList<ConversionTableRow>();
			for(ConversionTableRow ctr : conversionTableRows){
				if(ctr.interactionId().equals(bioPaxObject.getID())){
					participants.add(ctr);
				}
			}
			// create reaction object
			String name = getSafetyName(process.getName() + "_" + location);
			ReactionStep reactionStep = bioModel.getModel().getReactionStep(name);
			if(reactionStep == null){
				// create a new reactionStep object
				ReactionStep simpleReactionStep = bioModel.getModel().createSimpleReaction(bioModel.getModel().getStructure(location));
				simpleReactionStep.setName(name);
				RelationshipObject newRelationship = new RelationshipObject(simpleReactionStep, bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStep(bioModel, process, simpleReactionStep, newRelationship, participants);
				addKinetics(simpleReactionStep, process);
			}else{
				//			bioModel.getModel().getReactionStep(safeId).setStructure(bioModel.getModel().getStructure(location));
				// add missing parts for the existing reactionStep
				RelationshipObject newRelationship = new RelationshipObject(reactionStep, bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStep(bioModel, process, reactionStep, newRelationship, participants);
				addKinetics(reactionStep, process);
			}
		}
	}
	
	private void createReactionStepsFromTableRow(BioModel bioModel, Transport bioPaxObject,
			double stoich, String id, String location, ArrayList<ConversionTableRow> conversionTableRows) throws Exception
			{
		for(Process process: BioPAXUtil.getAllProcesses(bioModel, bioPaxObject)) {
			// use user defined id as the name of the reaction name
			// get participants from table rows
			ArrayList<ConversionTableRow> participants = new ArrayList<ConversionTableRow>();
			for(ConversionTableRow ctr : conversionTableRows){
				if(ctr.interactionId().equals(bioPaxObject.getID())){
					participants.add(ctr);
				}
			}
			// create reaction object
			String name = getSafetyName(process.getName() + "_" + location);
			if(bioModel.getModel().getReactionStep(name) == null){
				// create a new reactionStep object
				FluxReaction fluxReactionStep = bioModel.getModel().createFluxReaction((Membrane)bioModel.getModel().getStructure(location));
				fluxReactionStep.setName(name);
				RelationshipObject newRelationship = new RelationshipObject(fluxReactionStep, bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStep(bioModel, process, fluxReactionStep, newRelationship, participants);
			}else{
				//			bioModel.getModel().getReactionStep(safeId).setStructure(bioModel.getModel().getStructure(location));
				// add missing parts for the existing reactionStep
				RelationshipObject newRelationship = new RelationshipObject(bioModel.getModel().getReactionStep(name), bioPaxObject);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStep(bioModel, process, bioModel.getModel().getReactionStep(name), newRelationship, participants);
			}
		}
	}
	
	/*
	 * for reaction:
	 * 1. annotate the selected vcell object using linked pathway conversion
	 * 2. add non-existing speciesContexts from linked pathway conversion
	 * 3. add links between relative vcell objects and pathway objects
	 * Questions:
	 * - how to deal with the case that the reaction is existing in the model?
	 * 		+ add it in no matter what? 
	 * 				(this is the version we have now: 
	 * 					add the duplicated reactions in without name changing, 
	 * 				 	all duplicated reactions share the same participant objects)
	 *      + just modify the existing one?
	 */
	private void createReactionStep(BioModel bioModel, Process process, ReactionStep reactionStep, 
			RelationshipObject relationshipObject, ArrayList<ConversionTableRow> participants) 
	throws Exception
	{
		if (reactionStep == null || bioModel == null || bioModel.getRelationshipModel() == null || participants.size() < 1) {
			return;
		}
		ArrayList<ReactionParticipant> rplist = new ArrayList<ReactionParticipant>();
		// create and add reaction participants to list 
		for(ConversionTableRow ctr : participants){
			if(ctr.getBioPaxObject() instanceof Conversion) continue;
			int stoich = ctr.stoich().intValue();
			String safeId = getSafetyName(ctr.id());

			// get speciesContext object based on its name
			// if the speciesContext is not existed, create a new one
			createSpeciesContextFromTableRow(bioModel, (PhysicalEntity)ctr.getBioPaxObject(), ctr.stoich(), ctr.id(), ctr.location());
			
			// add the existed speciesContext objects or new speciesContext objects to reaction participant list
			if(ctr.participantType().equals("Reactant")){
				if (reactionStep instanceof SimpleReaction || reactionStep instanceof FluxReaction) {
					rplist.add(new Reactant(null,reactionStep, bioModel.getModel().getSpeciesContext(safeId), stoich));
				}
			}else if(ctr.participantType().equals("Product")){
				if (reactionStep instanceof SimpleReaction || reactionStep instanceof FluxReaction) {
					rplist.add(new Product(null,reactionStep, bioModel.getModel().getSpeciesContext(safeId), stoich));
				}
			}		// we do not add catalysts
		}
		ReactionParticipant[] rpArray = rplist.toArray(new ReactionParticipant[0]);
		reactionStep.setReactionParticipants(rpArray);
		
		// add Controls to the reaction
		Set<PhysicalEntity> controllers = process.getControllers();
		for(ConversionTableRow ctr : participants){
			if(controllers.contains(ctr.getBioPaxObject())) {
				if(ctr.participantType().equals("Catalyst")){
					String safeId = getSafetyName(ctr.id());
					/* 
					 * using addCatalyst() to create catalyst in reaction: 
					 * this function cannot allow an object to be catalyst and (reactant/product) in the same reaction
					 */
					//reactionStep.addCatalyst(bioModel.getModel().getSpeciesContext(safeId));

					/* However, in pathway interaction object, an physicalEntity can be catalyst and (reactant/product) in the same reaction
					 * So we just call create catalyst for the reaction no matter what rolls the object is playing in the reaction
					 * Switch back to the addCatalyst() function when it is necessary, but exceptions make be reported for some reactions
					 */
					reactionStep.addReactionParticipant(new Catalyst(null,reactionStep, bioModel.getModel().getSpeciesContext(safeId)));
				}else if(ctr.participantType().equals("Control")){
					String safeId = getSafetyName(ctr.id());
					//reactionStep.addCatalyst(bioModel.getModel().getSpeciesContext(safeId));
					reactionStep.addReactionParticipant(new Catalyst(null,reactionStep, bioModel.getModel().getSpeciesContext(safeId)));
				}

			}
		}
	}
	
	private void createReactionStepsFromTableRow(BioModel bioModel, Conversion conversion,
			double stoich, String id, String location) throws Exception
	{
		// use user defined id as the name of the reaction name
		Set<Process> processes = BioPAXUtil.getAllProcesses(bioModel, conversion);
		for(Process process : processes) {
			String name = getSafetyName(process.getName() + "_" + location);
			if(bioModel.getModel().getReactionStep(name) == null){
				// create a new reactionStep object
				ReactionStep simpleReactionStep = bioModel.getModel().createSimpleReaction(bioModel.getModel().getStructure(location));
				simpleReactionStep.setName(name);
				RelationshipObject newRelationship = new RelationshipObject(simpleReactionStep, conversion);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStepFromPathway( bioModel, process, simpleReactionStep, newRelationship);
			}else{
				bioModel.getModel().getReactionStep(name).setStructure(bioModel.getModel().getStructure(location));
			// add missing parts for the existing reactionStep
				RelationshipObject newRelationship = new RelationshipObject(bioModel.getModel().getReactionStep(name), conversion);
				bioModel.getRelationshipModel().addRelationshipObject(newRelationship);
				createReactionStepFromPathway( bioModel, process, bioModel.getModel().getReactionStep(name), newRelationship);
			}			
		}
	}
		
	/* for SpeciesContext object: 
	 * 1. annotate the selected vcell object using linked pathway object
	 * 2. map all pathway neighbors to the selected vcell object -- not done yet!
	 * for duplicated SpeciesContext: we add them in without name changing.
	*/
	private void createSpeciesContextFromPathway(BioModel bioModel, SpeciesContext bioModelEntityObject, RelationshipObject relationshipObject) throws Exception
	{
		// annotate the selected vcell object using linked pathway object 
		if(((PhysicalEntity)relationshipObject.getBioPaxObject()).getName().size() == 0){
			(bioModelEntityObject).setName(
					getSafetyName(((PhysicalEntity)relationshipObject.getBioPaxObject()).getID()));
		}else{
			(bioModelEntityObject).setName(
					getSafetyName(((PhysicalEntity)relationshipObject.getBioPaxObject()).getName().get(0)));
		}
	}
	
	/*
	 * for reaction:
	 * 1. annotate the selected vcell object using linked pathway conversion
	 * 2. add non-existing speciesContexts from linked pathway conversion
	 * 3. add links between relative vcell objects and pathway objects
	 * Questions:
	 * - how to deal with the case that the reaction is existing in the model?
	 * 		+ add it in no matter what? 
	 * 				(this is the version we have now: 
	 * 					add the duplicated reactions in without name changing, 
	 * 				 	all duplicated reactions share the same participant objects)
	 *      + just modify the existing one?
	 */
	private void createReactionStepFromPathway(BioModel bioModel, Process process, 
			ReactionStep reactionStep, RelationshipObject relationshipObject) throws Exception
	{
		// annotate the selected vcell object using linked pathway object
		// add non-existing speciesContexts from linked pathway conversion
		ReactionParticipant[] rpArray = parseReaction(reactionStep, bioModel, relationshipObject);
		// create a hashtable for interaction Participants
		Hashtable<String, BioPaxObject> participantTable = new Hashtable<String, BioPaxObject>();
		for(BioPaxObject bpObject: ((Conversion)relationshipObject.getBioPaxObject()).getLeft()){
			if(((PhysicalEntity)bpObject).getName().size() == 0){
				participantTable.put(getSafetyName(((PhysicalEntity)bpObject).getID()), bpObject);
			}else{
				participantTable.put(getSafetyName(((PhysicalEntity)bpObject).getName().get(0)), bpObject);
			}
		}
		for(BioPaxObject bpObject: ((Conversion)relationshipObject.getBioPaxObject()).getRight()){
			if(((PhysicalEntity)bpObject).getName().size() == 0){
				participantTable.put(getSafetyName(((PhysicalEntity)bpObject).getID()), bpObject);
			}else{
				participantTable.put(getSafetyName(((PhysicalEntity)bpObject).getName().get(0)), bpObject);
			}
		}
		
		for (ReactionParticipant rp : rpArray) {
			SpeciesContext speciesContext = rp.getSpeciesContext();
			if (bioModel.getModel().getSpeciesContext(speciesContext.getName()) == null) {
			// if the speciesContext is not existed, then add it to the bioModel and link it to the corresponding pathway object 
				if(bioModel.getModel().getSpecies(speciesContext.getName()) == null){
					bioModel.getModel().addSpecies(speciesContext.getSpecies());
				}
				bioModel.getModel().addSpeciesContext(speciesContext);
				RelationshipObject newSpeciesContext = new RelationshipObject(speciesContext, participantTable.get(speciesContext.getName()));
				bioModel.getRelationshipModel().addRelationshipObject(newSpeciesContext);
			}else{
			// if it is in the bioModel, then check whether it links to pathway object or not
				HashSet<RelationshipObject> linkedReObjects = 
					bioModel.getRelationshipModel().getRelationshipObjects(bioModel.getModel().getSpeciesContext(speciesContext.getName()));
				if(linkedReObjects != null){
					boolean isLinked = false;
					for(RelationshipObject reObject: linkedReObjects){
						if(reObject.getBioPaxObject() == participantTable.get(speciesContext.getName())){
							isLinked = true;
							break;
						}
					}
					if(!isLinked){
						RelationshipObject newSpeciesContext = new RelationshipObject(speciesContext, participantTable.get(speciesContext.getName()));
						bioModel.getRelationshipModel().addRelationshipObject(newSpeciesContext);
					}
				}else{
					RelationshipObject newSpeciesContext = new RelationshipObject(speciesContext, participantTable.get(speciesContext.getName()));
					bioModel.getRelationshipModel().addRelationshipObject(newSpeciesContext);
				}
			}
		}
		(reactionStep).setReactionParticipants(rpArray);
		// add Control to the reaction
		if(process.getControl() != null) {
			for(InteractionParticipant pe : process.getControl().getParticipants()){
				SpeciesContext newSpeciescontext = createSpeciesContextFromBioPaxObject( bioModel, pe.getPhysicalEntity());
				(reactionStep).addReactionParticipant(new Catalyst(null,reactionStep, newSpeciescontext));
			}
			
		}
		addKinetics(reactionStep, process);
	}
	
	private ReactionParticipant[] parseReaction(ReactionStep reactionStep, BioModel bioModel, RelationshipObject relationshipObject  ) 
			throws ExpressionException, PropertyVetoException {
		if (reactionStep == null || bioModel == null || bioModel.getRelationshipModel() == null) {
			return null;
		}
		// create the reaction equation string
		String leftHand = getParticipantsString(((Conversion)relationshipObject.getBioPaxObject()).getLeft());
		String rightHand = getParticipantsString(((Conversion)relationshipObject.getBioPaxObject()).getRight());
		StringTokenizer st = new StringTokenizer(leftHand, "+");
		HashMap<String, SpeciesContext> speciesContextMap = new HashMap<String, SpeciesContext>();
		ArrayList<ReactionParticipant> rplist = new ArrayList<ReactionParticipant>();
		// create and add reaction participants to list for left-hand side of equation
		Model model = bioModel.getModel();
		Structure structure = reactionStep.getStructure();
		while (st.hasMoreElements()) {
			String nextToken = st.nextToken().trim();
			if (nextToken.length() == 0) {
				continue;
			}
			int stoichiIndex = 0;
			while (true) {
				if (Character.isDigit(nextToken.charAt(stoichiIndex))) {
					stoichiIndex ++;
				} else {
					break;
				}
			}
			int stoichi = 1;
			String tmp = nextToken.substring(0, stoichiIndex);
			if (tmp.length() > 0) {
				stoichi = Integer.parseInt(tmp);
			}
			String var = nextToken.substring(stoichiIndex).trim();
			// get speciesContext object based on its name
			// if the speciesContext is not existed, create a new one
			SpeciesContext sc = model.getSpeciesContext(var);
			if (sc == null) {
				sc = speciesContextMap.get(var);
				if (sc == null) {
					// get species object based on its name
					// if the species is not existed, create a new one
					Species species = model.getSpecies(var);
					if (species == null) {
						species = new Species(var, null);
					}
					sc = new SpeciesContext(species, structure);
					sc.setName(var);
					speciesContextMap.put(var, sc);
				}
			}
			// add the existed speciesContext objects or new speciesContext objects to reaction participant list
			if (reactionStep instanceof SimpleReaction || reactionStep instanceof FluxReaction) {
				rplist.add(new Reactant(null,(SimpleReaction) reactionStep, sc, stoichi));
			}
		}
		// create and add reaction participants to list for right-hand side of equation
		st = new StringTokenizer(rightHand, "+");
		while (st.hasMoreElements()) {
			String nextToken = st.nextToken().trim();
			if (nextToken.length() == 0) {
				continue;
			}
			int stoichiIndex = 0;
			while (true) {
				if (Character.isDigit(nextToken.charAt(stoichiIndex))) {
					stoichiIndex ++;
				} else {
					break;
				}
			}
			int stoichi = 1;
			String tmp = nextToken.substring(0, stoichiIndex);
			if (tmp.length() > 0) {
				stoichi = Integer.parseInt(tmp);
			}
			String var = nextToken.substring(stoichiIndex);
			SpeciesContext sc = model.getSpeciesContext(var);
			if (sc == null) {
				sc = speciesContextMap.get(var);
				if (sc == null) {
					Species species = model.getSpecies(var);
					if (species == null) {
						species = new Species(var, null);
					}
					sc = new SpeciesContext(species, structure);
					sc.setName(var);
					speciesContextMap.put(var, sc);
				}
			}
			if (reactionStep instanceof SimpleReaction || reactionStep instanceof FluxReaction) {
				rplist.add(new Product(null,(SimpleReaction) reactionStep, sc, stoichi));
			}
		}
		return rplist.toArray(new ReactionParticipant[0]);
	}

	// create the reaction equation based on the pathway conversion information 
	private static String getParticipantsString(List<PhysicalEntity> physicalEntities) {
		if (physicalEntities == null){
			return null;
		}
		String participantString = "";
		for(PhysicalEntity physicalEntity : physicalEntities){
			if(physicalEntity.getName().size() == 0){
				participantString += getSafetyName(physicalEntity.getID()) + "+";
			}else{
				participantString += getSafetyName(physicalEntity.getName().get(0)) + "+";
			}
		}
		// remove the last "+" from the string
		if(participantString.length()>0){
			participantString = participantString.substring(0, participantString.length()-1);
		}
		return participantString;
	}
	//convert the name of biopax object to safety vcell object name
	private static String getSafetyName(String oldValue){
		return TokenMangler.fixTokenStrict(oldValue, 60);
	} 
	
	private void addKinetics(ReactionStep reactionStep, Process process) {
		try {
			SBPAXKineticsExtractor.extractKineticsExactMatch(reactionStep, process);
		} catch (ExpressionException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}		
//		SBPAXKineticsExtractor.extractKineticsInferredMatch(reactionStep, Collections.<SBEntity>unmodifiableSet(process.getInteractions()));		
	}
	
}
