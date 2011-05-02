package cbit.vcell.mapping;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.Vector;

import org.vcell.util.TokenMangler;

import cbit.vcell.geometry.GeometryClass;
import cbit.vcell.geometry.SubVolume;
import cbit.vcell.math.Action;
import cbit.vcell.math.CompartmentSubDomain;
import cbit.vcell.math.Constant;
import cbit.vcell.math.Function;
import cbit.vcell.math.JumpProcess;
import cbit.vcell.math.MathDescription;
import cbit.vcell.math.MathException;
import cbit.vcell.math.StochVolVariable;
import cbit.vcell.math.SubDomain;
import cbit.vcell.math.VarIniCondition;
import cbit.vcell.math.VarIniCount;
import cbit.vcell.math.VarIniPoissonExpectedCount;
import cbit.vcell.math.Variable.Domain;
import cbit.vcell.matrix.MatrixException;
import cbit.vcell.matrix.RationalExp;
import cbit.vcell.model.Feature;
import cbit.vcell.model.FluxReaction;
import cbit.vcell.model.Kinetics;
import cbit.vcell.model.KineticsDescription;
import cbit.vcell.model.LumpedKinetics;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.ModelException;
import cbit.vcell.model.Parameter;
import cbit.vcell.model.Product;
import cbit.vcell.model.ProxyParameter;
import cbit.vcell.model.Reactant;
import cbit.vcell.model.ReactionParticipant;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.ReservedSymbol;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.model.Kinetics.KineticsParameter;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.RationalExpUtils;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.solver.stoch.MassActionSolver;
import cbit.vcell.units.VCUnitDefinition;
/**
 * The StochMathMapping class performs the Biological to Mathematical transformation once upon calling getMathDescription()
 * for stochastic simulation. To get math description for deterministic simulation please reference @MathMapping.
 * ApplicationEditor.updatMath() decides to use either StochMathMapping or MathMapping.
 * Created Sept. 18, 2006
 * @version 1.0 Beta
 * @author Tracy LI
 */
public class StochMathMapping extends MathMapping {

	private static final String PARAMETER_PROBABILITY_RATE_REVERSE_SUFFIX = "_reverse";
	private static final String PARAMETER_PROBABILITYRATE_PREFIX = "P_";


	/**
	 * The constructor, which pass the simulationContext pointer.
	 * @param model cbit.vcell.model.Model
	 * @param geometry cbit.vcell.geometry.Geometry
	 */
	protected StochMathMapping(SimulationContext simContext) {
		super(simContext);
	}

/**
 * getExpressionConcToAmt : converts the concentration expression ('concExpr') to an expression of the number of particles. 
 * 		If argument 'speciesContext' is on a membrane, particlesExpr = concExpr * size_of_Mem. If 'speciesContext' is in 
 * 		feature, particlesExpr = (concExpr * size_of_Feature)/KMOLE.
 * @param concExpr
 * @param speciesContext
 * @return
 * @throws MappingException
 * @throws ExpressionException
 */
private Expression getExpressionConcToExpectedCount(Expression concExpr, SpeciesContext speciesContext) throws MappingException, ExpressionException
{
	Expression particlesExpr = null;	//to create an expression for number of particles 

	if (speciesContext.getStructure() instanceof Membrane)
	{
		// convert concentration(particles/area) to number of particles
		particlesExpr = Expression.mult(concExpr, new Expression(speciesContext.getStructure().getStructureSize(), getNameScope())); // particles = concentration(molecues/um2) * size(um2)
	}
	else
	{
		// convert number of particles to concentration(particles/volume)
		// particles = [iniConcentration(uM)*size(um3)]/KMOLE
		Expression numeratorExpr = Expression.mult(concExpr, new Expression(speciesContext.getStructure().getStructureSize(), getNameScope()));
		Expression denominatorExpr = new Expression(ReservedSymbol.KMOLE, getNameScope());
		particlesExpr = Expression.div(numeratorExpr, denominatorExpr);
	}
	
	return particlesExpr;
}

/**
 * getExpressionAmtToConc : converts the particles expression ('particlesExpr') to an expression for concentration. 
 * 		If argument 'speciesContext' is on a membrane, concExpr = particlesExpr/size_of_Mem. If 'speciesContext' is in 
 * 		feature, concExpr = (particlesExpr/size_of_Feature)*KMOLE.
 * @param particlesExpr
 * @param speciesContext
 * @return
 * @throws MappingException
 * @throws ExpressionException
 */
private Expression getExpressionAmtToConc(Expression particlesExpr, SpeciesContext speciesContext) throws MappingException, ExpressionException
{
	Expression concentrationExpr = null;	//to create an expression for concentration 

	if (speciesContext.getStructure() instanceof Membrane)
	{
		// convert number of particles to concentration(particles/area) 
		concentrationExpr = Expression.div(particlesExpr, new Expression(speciesContext.getStructure().getStructureSize(), getNameScope())); // particles/size(um2) = concentration(molecues/um2)
	}
	else
	{
		// convert number of particles to concentration(particles/volume) 
		// concentration(uM) = [particles/size(um3)]*KMOLE)
		Expression numeratorExpr = Expression.mult(particlesExpr, new Expression(ReservedSymbol.KMOLE, getNameScope()));
		Expression denominatorExpr = new Expression(speciesContext.getStructure().getStructureSize(), getNameScope());
		concentrationExpr = Expression.div(numeratorExpr, denominatorExpr);
	}
	
	return concentrationExpr;
}


	/**
	 * This method returns the mathDeac if it is existing, otherwise it creates a mathDescription and returns it.
	 * @return cbit.vcell.math.MathDescription
	 */
	public MathDescription getMathDescription() throws MappingException, MathException, MatrixException, ExpressionException, ModelException {
		if (mathDesc==null){
			refresh();
		}
		return mathDesc;
	}


/**
 * Get probability expression for the specific elementary reaction.
 * Input: ReactionStep, the reaction. isForwardDirection, if the elementary reaction is forward from the reactionstep.
 * Output: Expression. the probability expression.
 * Creation date: (9/14/2006 3:22:58 PM)
 */
public Expression getProbabilityRate(ReactionStep rs, Expression rateConstant, boolean isForwardDirection) throws MappingException
{
	ReactionStep reactionStep = rs;
	Expression probExp = null;
	//get kinetics of the reaction step
	Kinetics kinetics = reactionStep.getKinetics();
	Expression rateConstantExpr = null; 	//to compose the rate constant expression e.g. Kf, Kr
	Expression rxnProbabilityExpr = null; 	//to compose the stochastic variable(species) expression, e.g. s*(s-1)*(s-2)* speciesFactor.
	Expression factorExpr = null; //to compose the factor that the probability expression multiplies with, which convert the rate expression under stochastic context
	//the structure where reaction happens
	StructureMapping sm = getSimulationContext().getGeometryContext().getStructureMapping(rs.getStructure());
	try 
	{
		if(isForwardDirection) // forward reaction
		{
			//get the reaction rate constant and convert it to rate of Number of particles
			rateConstantExpr = rateConstant;
		    // get convert factor for rate constant( membrane:rateConstant*membrane_Size (factor is membrane_size), feature : rateConstant*(feature_size/KMole)(factor is feature_size/KMOLE)) )
		    if(sm.getStructure() instanceof Membrane) {
		    	factorExpr = new Expression(sm.getStructure().getStructureSize(), getNameScope());
		    } else {
		    	factorExpr = new Expression(sm.getStructure().getStructureSize(), getNameScope());
		    	Expression kmoleExpr = new Expression(ReservedSymbol.KMOLE, getNameScope());
		    	factorExpr = Expression.div(factorExpr, kmoleExpr);
			}
			
			//complete the probability expression by the reactants' stoichiometries 
			ReactionParticipant[] reacPart = reactionStep.getReactionParticipants();
			for (int i=0; i<reacPart.length; i++)
			{
				int stoichiometry = 0;
				if(reacPart[i] instanceof Reactant) 
				{ 
					stoichiometry = ((Reactant)reacPart[i]).getStoichiometry();
					//******the following part is to form the s*(s-1)(s-2)..(s-stoi+1).portion of the probability rate.
					StructureMapping reactSM = getSimulationContext().getGeometryContext().getStructureMapping(reacPart[i].getStructure());
					Expression speciesFactor = null;//factor expression for species
					//convert speceis' unit from moles/liter to molecules.
					if(reactSM.getStructure() instanceof Membrane) {
						speciesFactor = Expression.invert(new Expression(reactSM.getStructure().getStructureSize(), getNameScope()));
					} else {
						Expression numExpr = new Expression(ReservedSymbol.KMOLE, getNameScope());
						Expression denomExpr = new Expression(reactSM.getStructure().getStructureSize(), getNameScope());
						speciesFactor =  Expression.div(numExpr, denomExpr);
					}
					//s*(s-1)(s-2)..(s-stoi+1)
					SpeciesCountParameter spCountParam = getSpeciesCountParameter(reacPart[i].getSpeciesContext());
					Expression spCount_exp = new Expression(spCountParam, getNameScope());
					Expression tempExpr = new Expression(spCount_exp);//species from uM to No. of Particles, form s*(s-1)*(s-2)
					for(int j = 1; j < stoichiometry; j++) {
						tempExpr = Expression.mult(tempExpr, Expression.add(spCount_exp, new Expression(-j)));
					}
					//update total factor with speceies factor
					if(stoichiometry == 1) {
						factorExpr = Expression.mult(factorExpr, speciesFactor);
					} else if (stoichiometry > 1) {
						// rxnProbExpr * (structSize^stoichiometry)
						Expression powerExpr = Expression.power(speciesFactor, new Expression(stoichiometry));
						factorExpr = Expression.mult(factorExpr, powerExpr);
					}
					if (rxnProbabilityExpr == null) {
						rxnProbabilityExpr = new Expression(tempExpr);
					} else {//for more than one reactant
						rxnProbabilityExpr = Expression.mult(rxnProbabilityExpr, tempExpr);
					}
				}
			}
		} 
		else // reverse reaction
		{
			rateConstantExpr = rateConstant;
		    // get convert factor for rate constant( membrane:rateConstant*membrane_Size (factor is membrane_size), feature : rateConstant*(feature_size/KMole)(factor is feature_size/KMOLE)) ) 
		    if(sm.getStructure() instanceof Membrane) {
		    	factorExpr = new Expression(sm.getStructure().getStructureSize(), getNameScope());
		    } else {
		    	factorExpr = new Expression(sm.getStructure().getStructureSize(), getNameScope());
		    	Expression denominatorExpr = new Expression(ReservedSymbol.KMOLE, getNameScope());
		    	factorExpr = Expression.div(factorExpr, denominatorExpr);
			}
		    
			//complete the remaining part of the probability expression by the products' stoichiometries.
			ReactionParticipant[] reacPart = reactionStep.getReactionParticipants();
			
			for (int i=0; i<reacPart.length; i++)
			{
				int stoichiometry = 0;
				if(reacPart[i] instanceof Product) 
				{ 
					stoichiometry = ((Product)reacPart[i]).getStoichiometry();
					//******the following part is to form the s*(s-1)*(s-2)...(s-stoi+1).portion of the probability rate.
					StructureMapping reactSM = getSimulationContext().getGeometryContext().getStructureMapping(reacPart[i].getStructure());
					Expression speciesFactor = null;//factor expression for species
					//convert speceis' unit from moles/liter to molecules. 
					if(reactSM.getStructure() instanceof Membrane) {
						speciesFactor = Expression.invert(new Expression(reactSM.getStructure().getStructureSize(), getNameScope()));
					} else {
						Expression numExpr = new Expression(ReservedSymbol.KMOLE, getNameScope());
						Expression denomExpr = new Expression(reactSM.getStructure().getStructureSize(), getNameScope());
						speciesFactor =  Expression.div(numExpr, denomExpr);
					}
					//s*(s-1)*(s-2)...(s-stoi+1)
					SpeciesCountParameter spCountParam = getSpeciesCountParameter(reacPart[i].getSpeciesContext());
					Expression spCount_exp = new Expression(spCountParam, getNameScope());
					Expression tempExpr = new Expression(spCount_exp);//species from uM to No. of Particles, form s*(s-1)*(s-2)
					for(int j = 1; j < stoichiometry; j++) {
						tempExpr = Expression.mult(tempExpr, Expression.add(spCount_exp, new Expression(-j)));
					}
					//update total factor with speceies factor
					if(stoichiometry == 1) {
						factorExpr = Expression.mult(factorExpr, speciesFactor);
					} else if (stoichiometry > 1) {
						// rxnProbExpr * (structSize^stoichiometry)
						Expression powerExpr = Expression.power(speciesFactor, new Expression(stoichiometry));
						factorExpr = Expression.mult(factorExpr, powerExpr);
					}
					if (rxnProbabilityExpr == null) {
						rxnProbabilityExpr = new Expression(tempExpr);
					} else {
						rxnProbabilityExpr = Expression.mult(rxnProbabilityExpr, tempExpr);
					}
				}
			}
		}

		// Now construct the probability expression.
		if(rateConstantExpr == null) {
			throw new MappingException("Can not find reaction rate constant in reaction: "+ reactionStep.getName());
		} else if(rxnProbabilityExpr == null) {
		 	probExp = new Expression(rateConstantExpr);   
		} else if((rateConstantExpr != null) && (rxnProbabilityExpr != null)) {
			probExp = Expression.mult(rateConstantExpr, rxnProbabilityExpr);
	    }
		//simplify the factor
		RationalExp factorRatExp = RationalExpUtils.getRationalExp(factorExpr);
		factorExpr = new Expression(factorRatExp.infixString());
		factorExpr.bindExpression(this);
		//get probability rate with converting factor
		probExp = Expression.mult(probExp, factorExpr);
		probExp = probExp.flatten();

	}catch (ExpressionException e) {
		e.printStackTrace();
	}

	return probExp;
}


/**
 * Basically the function clears the error list and calls to get a new mathdescription.
 */
protected void refresh() throws MappingException, ExpressionException, MatrixException, MathException, ModelException{
	localIssueList.clear();
	//refreshKFluxParameters();
	
	refreshSpeciesContextMappings();
	//refreshStructureAnalyzers();
	refreshVariables();
	
	refreshLocalNameCount();
	refreshMathDescription();
}


	/**
	 * set up a math description based on current simulationContext.
	 */
	@Override
	protected void refreshMathDescription() throws MappingException, MatrixException, MathException, ExpressionException, ModelException
	{
		GeometryClass geometryClass = getSimulationContext().getGeometry().getGeometrySpec().getSubVolumes()[0];
		Domain domain = new Domain(geometryClass);
		
		//use local variable instead of using getter all the time.
		SimulationContext simContext = getSimulationContext();
		//local structure mapping list
		StructureMapping structureMappings[] = simContext.getGeometryContext().getStructureMappings();
		//We have to check if all the reactions are able to tranform to stochastic jump processes before generating the math.
		String stochChkMsg =simContext.getModel().isValidForStochApp();
		if(!(stochChkMsg.equals("")))
		{
			throw new ModelException("Problem updating math description: "+ simContext.getName()+"\n"+stochChkMsg);
		}
		//All sizes must be set for new ODE models and ratios must be set for old ones.
		simContext.checkValidity();
		

		//
		// verify that all structures are mapped to subvolumes and all subvolumes are mapped to a structure
		//
		Structure structures[] = simContext.getGeometryContext().getModel().getStructures();
		for (int i = 0; i < structures.length; i++){
			StructureMapping sm = simContext.getGeometryContext().getStructureMapping(structures[i]);
			if (sm==null || (sm instanceof FeatureMapping && ((FeatureMapping)sm).getGeometryClass() == null)){
				throw new MappingException("model structure '"+structures[i].getName()+"' not mapped to a geometry subVolume");
			}
			if (sm!=null && (sm instanceof MembraneMapping) && ((MembraneMapping)sm).getVolumeFractionParameter()!=null){
				Expression volFractExp = ((MembraneMapping)sm).getVolumeFractionParameter().getExpression();
				try {
					if(volFractExp != null)
					{
						double volFract = volFractExp.evaluateConstant();
						if (volFract>=1.0){
							throw new MappingException("model structure '"+((MembraneMapping)sm).getMembrane().getInsideFeature().getName()+"' has volume fraction >= 1.0");
						}
					}
				}catch (ExpressionException e){
					e.printStackTrace(System.out);
				}
			}
		}
		SubVolume subVolumes[] = simContext.getGeometryContext().getGeometry().getGeometrySpec().getSubVolumes();
		for (int i = 0; i < subVolumes.length; i++){
			Structure[] mappedStructures = simContext.getGeometryContext().getStructuresFromGeometryClass(subVolumes[i]);
			if (mappedStructures==null || mappedStructures.length==0){
				throw new MappingException("geometry subVolume '"+subVolumes[i].getName()+"' not mapped from a model structure");
			}
		}
		
		//
		// gather only those reactionSteps that are not "excluded"
		//
		ReactionSpec reactionSpecs[] = simContext.getReactionContext().getReactionSpecs();
		Vector<ReactionStep> rsList = new Vector<ReactionStep>();
		for (int i = 0; i < reactionSpecs.length; i++){
			if (reactionSpecs[i].isExcluded()==false){
				rsList.add(reactionSpecs[i].getReactionStep());
			}
		}
		ReactionStep reactionSteps[] = new ReactionStep[rsList.size()];
		rsList.copyInto(reactionSteps);	
		
		//
		// fail if any unresolved parameters
		//
		for (int i = 0; i < reactionSteps.length; i++){
			Kinetics.UnresolvedParameter unresolvedParameters[] = reactionSteps[i].getKinetics().getUnresolvedParameters();
			if (unresolvedParameters!=null && unresolvedParameters.length>0){
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < unresolvedParameters.length; j++){
					if (j>0){
						buffer.append(", ");
					}
					buffer.append(unresolvedParameters[j].getName());
				}
				throw new MappingException(reactionSteps[i].getTerm()+" '"+reactionSteps[i].getName()+"' contains unresolved identifier(s): "+buffer);
			}
		}
			
		//
		// create new MathDescription (based on simContext's previous MathDescription if possible)
		//
		MathDescription oldMathDesc = simContext.getMathDescription();
		mathDesc = null;
		if (oldMathDesc != null){
			if (oldMathDesc.getVersion() != null){
				mathDesc = new MathDescription(oldMathDesc.getVersion());
			}else{
				mathDesc = new MathDescription(oldMathDesc.getName());
			}
		}else{
			mathDesc = new MathDescription(simContext.getName()+"_generated");
		}

		//
		// temporarily place all variables in a hashtable (before binding) and discarding duplicates
		//
		VariableHash varHash = new VariableHash();
		
		//
		// conversion factors
		//
		varHash.addVariable(new Constant(ReservedSymbol.KMOLE.getName(),getIdentifierSubstitutions(ReservedSymbol.KMOLE.getExpression(),ReservedSymbol.KMOLE.getUnitDefinition(),null)));
		varHash.addVariable(new Constant(ReservedSymbol.N_PMOLE.getName(),getIdentifierSubstitutions(ReservedSymbol.N_PMOLE.getExpression(),ReservedSymbol.N_PMOLE.getUnitDefinition(),null)));
		varHash.addVariable(new Constant(getMathSymbol(ReservedSymbol.FARADAY_CONSTANT,null),getIdentifierSubstitutions(ReservedSymbol.FARADAY_CONSTANT.getExpression(),ReservedSymbol.FARADAY_CONSTANT.getUnitDefinition(),null)));
		varHash.addVariable(new Constant(getMathSymbol(ReservedSymbol.FARADAY_CONSTANT_NMOLE,null),getIdentifierSubstitutions(ReservedSymbol.FARADAY_CONSTANT_NMOLE.getExpression(),ReservedSymbol.FARADAY_CONSTANT_NMOLE.getUnitDefinition(),null)));
		varHash.addVariable(new Constant(getMathSymbol(ReservedSymbol.GAS_CONSTANT,null),getIdentifierSubstitutions(ReservedSymbol.GAS_CONSTANT.getExpression(),ReservedSymbol.GAS_CONSTANT.getUnitDefinition(),null)));
		varHash.addVariable(new Constant(getMathSymbol(ReservedSymbol.TEMPERATURE,null),getIdentifierSubstitutions(new Expression(simContext.getTemperatureKelvin()),VCUnitDefinition.UNIT_K,null)));
		//varHash.addVariable(new Constant(getMathSymbol(ReservedSymbol.PI,null),getIdentifierSubstitutions(ReservedSymbol.PI.getExpression(),ReservedSymbol.PI.getUnitDefinition(),null)));
		
		Enumeration<SpeciesContextMapping> enum1 = getSpeciesContextMappings();
		while (enum1.hasMoreElements()){
			SpeciesContextMapping scm = enum1.nextElement();
			if (scm.getVariable() instanceof StochVolVariable){
				varHash.addVariable(scm.getVariable());
			}
		}

		// deals with model parameters
		ModelParameter[] modelParameters = simContext.getModel().getModelParameters();
		for (int j=0;j<modelParameters.length;j++){
			Expression expr = getSubstitutedExpr(modelParameters[j].getExpression(), true, false);
			expr = getIdentifierSubstitutions(expr,modelParameters[j].getUnitDefinition(), geometryClass);
			varHash.addVariable(newFunctionOrConstant(getMathSymbol(modelParameters[j],geometryClass), expr,geometryClass));
		}
		
		//added July 2009, ElectricalStimulusParameter electric mapping tab
		ElectricalStimulus[] elecStimulus = simContext.getElectricalStimuli();
		if (elecStimulus.length > 0) {
			throw new MappingException("Modles with electrophysiology are not supported for stochastic applications.");			
		}
		
		//
		// add constant mem voltage
		//
		
		for (int j = 0; j < structureMappings.length; j++){
			if (structureMappings[j] instanceof MembraneMapping){
				MembraneMapping memMapping = (MembraneMapping)structureMappings[j];
				Parameter initialVoltageParm = memMapping.getInitialVoltageParameter();
				try{
					Expression exp = initialVoltageParm.getExpression();
					exp.evaluateConstant();
					varHash.addVariable(newFunctionOrConstant(getMathSymbol(memMapping.getMembrane().getMembraneVoltage(),memMapping.getGeometryClass()),
							getIdentifierSubstitutions(memMapping.getInitialVoltageParameter().getExpression(),memMapping.getInitialVoltageParameter().getUnitDefinition(),memMapping.getGeometryClass()),memMapping.getGeometryClass()));
				}catch(ExpressionException e){
					e.printStackTrace(System.out);
					throw new MappingException("Membrane initial voltage: "+initialVoltageParm.getName()+" cannot be evaluated as constant.");
				}
			}
		}
		
		
		//
		// kinetic parameters (functions or constants)
		//
		for (int j=0;j<reactionSteps.length;j++){
			ReactionStep rs = reactionSteps[j];
			if (simContext.getReactionContext().getReactionSpec(rs).isExcluded()){
				continue;
			}
			if (rs.getKinetics() instanceof LumpedKinetics){
				throw new RuntimeException("Lumped Kinetics not yet supported for Stochastic Math Generation");
			}
			Kinetics.KineticsParameter parameters[] = rs.getKinetics().getKineticsParameters();
			StructureMapping sm = simContext.getGeometryContext().getStructureMapping(rs.getStructure());
			if (parameters != null){
				for (int i=0;i<parameters.length;i++){
					if ((parameters[i].getRole() == Kinetics.ROLE_CurrentDensity) && (parameters[i].getExpression()==null || parameters[i].getExpression().isZero())){
						continue;
					}
					//don't add rate, we'll do it later when creating the jump processes
					if (parameters[i].getRole() != Kinetics.ROLE_ReactionRate) {
						Expression expr = getSubstitutedExpr(parameters[i].getExpression(), true, false);
						varHash.addVariable(newFunctionOrConstant(getMathSymbol(parameters[i],sm.getGeometryClass()), getIdentifierSubstitutions(expr,parameters[i].getUnitDefinition(),sm.getGeometryClass()),sm.getGeometryClass()));
					}
				}
			}
		}
		

		//geometic mapping
		//the parameter "Size" is already put into mathsymbolmapping in refreshSpeciesContextMapping()
		for (int i=0;i<structureMappings.length;i++){
			StructureMapping sm = structureMappings[i];
			StructureMapping.StructureMappingParameter parm = sm.getParameterFromRole(StructureMapping.ROLE_Size);
			if(parm.getExpression() != null)
			{
				try {
					double value = parm.getExpression().evaluateConstant();
					varHash.addVariable(new Constant(getMathSymbol(parm,sm.getGeometryClass()),new Expression(value)));
				}catch (ExpressionException e){
					//varHash.addVariable(new Function(getMathSymbol0(parm,sm),getIdentifierSubstitutions(parm.getExpression(),parm.getUnitDefinition(),sm)));
					e.printStackTrace(System.out);
					throw new MappingException("Size of structure:"+sm.getNameScope().getName()+" cannot be evaluated as constant.");
				}
			}
		}

		//
		// species initial values (either function or constant)
		//
		SpeciesContextSpec speciesContextSpecs[] = simContext.getReactionContext().getSpeciesContextSpecs();
		for (int i = 0; i < speciesContextSpecs.length; i++){
			SpeciesContextSpec.SpeciesContextSpecParameter initParam = null;//can be concentration or amount
			Expression iniExp = null;
			StructureMapping sm = simContext.getGeometryContext().getStructureMapping(speciesContextSpecs[i].getSpeciesContext().getStructure());
			if(speciesContextSpecs[i].getInitialConcentrationParameter() != null && speciesContextSpecs[i].getInitialConcentrationParameter().getExpression() != null)
			{//use concentration, need to set up amount functions
				initParam = speciesContextSpecs[i].getInitialConcentrationParameter();
				iniExp = initParam.getExpression();
				iniExp = getSubstitutedExpr(iniExp, true, !speciesContextSpecs[i].isConstant());
				// now create the appropriate function or Constant for the speciesContextSpec.
				varHash.addVariable(newFunctionOrConstant(getMathSymbol(initParam,sm.getGeometryClass()),getIdentifierSubstitutions(iniExp,initParam.getUnitDefinition(),sm.getGeometryClass()),sm.getGeometryClass()));

				//add function for initial amount
				SpeciesContextSpec.SpeciesContextSpecParameter initAmountParam = speciesContextSpecs[i].getInitialCountParameter();
				Expression 	iniAmountExp = getExpressionConcToExpectedCount(new Expression(initParam, getNameScope()),speciesContextSpecs[i].getSpeciesContext());
				// this is just going to add a var in math with iniCountSymbol, it is not actually write the expression to IniCountParameter.
				varHash.addVariable(new Function(getMathSymbol(initAmountParam, sm.getGeometryClass()),getIdentifierSubstitutions(iniAmountExp,initAmountParam.getUnitDefinition(),sm.getGeometryClass()),domain));
			}
			else if(speciesContextSpecs[i].getInitialCountParameter() != null && speciesContextSpecs[i].getInitialCountParameter().getExpression() != null)
			{// use amount
				initParam = speciesContextSpecs[i].getInitialCountParameter();
				iniExp = initParam.getExpression();
				iniExp = getSubstitutedExpr(iniExp, false, !speciesContextSpecs[i].isConstant());
				// now create the appropriate function or Constant for the speciesContextSpec.
				varHash.addVariable(newFunctionOrConstant(getMathSymbol(initParam,sm.getGeometryClass()),getIdentifierSubstitutions(iniExp,initParam.getUnitDefinition(),sm.getGeometryClass()),sm.getGeometryClass()));
			}

			//add spConcentration (concentration of species) to varHash as function or constant
			SpeciesConcentrationParameter spConcParam = getSpeciesConcentrationParameter(speciesContextSpecs[i].getSpeciesContext());
			varHash.addVariable(newFunctionOrConstant(getMathSymbol(spConcParam,sm.getGeometryClass()),getIdentifierSubstitutions(spConcParam.getExpression(), spConcParam.getUnitDefinition(), sm.getGeometryClass()),sm.getGeometryClass()));

		}
		
		//
		// constant species (either function or constant)
		//
		enum1 = getSpeciesContextMappings();
		while (enum1.hasMoreElements()){
			SpeciesContextMapping scm = (SpeciesContextMapping)enum1.nextElement();
			if (scm.getVariable() instanceof Constant){
				varHash.addVariable(scm.getVariable());
			}
		}

					
		//
		// geometry
		//
		if (simContext.getGeometryContext().getGeometry() != null){
			try {
				mathDesc.setGeometry(simContext.getGeometryContext().getGeometry());
			}catch (java.beans.PropertyVetoException e){
				e.printStackTrace(System.out);
				throw new MappingException("failure setting geometry "+e.getMessage());
			}
		}else{
			throw new MappingException("geometry must be defined");
		}


		//
		// functions: species which is not a variable, but has dependency expression
		//
		enum1 = getSpeciesContextMappings();
		while (enum1.hasMoreElements()){
			SpeciesContextMapping scm = (SpeciesContextMapping)enum1.nextElement();
			if (scm.getVariable()==null && scm.getDependencyExpression()!=null){
				StructureMapping sm = simContext.getGeometryContext().getStructureMapping(scm.getSpeciesContext().getStructure());
				Expression exp = scm.getDependencyExpression();
				exp.bindExpression(this);
				SpeciesCountParameter spCountParam = getSpeciesCountParameter(scm.getSpeciesContext());
				varHash.addVariable(new Function(getMathSymbol(spCountParam,sm.getGeometryClass()),getIdentifierSubstitutions(exp, VCUnitDefinition.UNIT_molecules, sm.getGeometryClass()),domain));
			}
		}

		//
		// create subDomains
		//
		SubDomain subDomain = null;
		subVolumes = simContext.getGeometryContext().getGeometry().getGeometrySpec().getSubVolumes();
		for (int j=0;j<subVolumes.length;j++){
			SubVolume subVolume = (SubVolume)subVolumes[j];
			//
			// get priority of subDomain
			//
			int priority;
			if (simContext.getGeometryContext().getGeometry().getDimension()==0){
				priority = CompartmentSubDomain.NON_SPATIAL_PRIORITY;
			}else{
				priority = j; // now does not have to match spatial feature, *BUT* needs to be unique
			}
			//
			// create subDomain
			//
			subDomain = new CompartmentSubDomain(subVolume.getName(),priority);
			mathDesc.addSubDomain(subDomain);
		}
	
		// set up jump processes
		// get all the reactions from simulation context
		// ReactionSpec[] reactionSpecs = simContext.getReactionContext().getReactionSpecs();---need to take a look here!
		for (int i = 0; i < reactionSpecs.length; i++)
		{
			if (reactionSpecs[i].isExcluded()) {
				continue;
			}
						
			// get the reaction
			ReactionStep reactionStep = reactionSpecs[i].getReactionStep();
			Kinetics kinetics = reactionStep.getKinetics();
			// the structure where reaction happens
			StructureMapping sm = simContext.getGeometryContext().getStructureMapping(reactionStep.getStructure());
	
			// Different ways to deal with simple reactions and flux reactions
			if(reactionStep instanceof SimpleReaction) // simple reactions
			{
				// check the reaction rate law to see if we need to decompose a reaction(reversible) into two jump processes.
				// rate constants are important in calculating the probability rate.
				// for Mass Action, we use KForward and KReverse, 
				// for General Kinetics we parse reaction rate J to see if it is in Mass Action form.
				Expression forwardRate = null;
				Expression reverseRate = null;
				if (kinetics.getKineticsDescription().equals(KineticsDescription.MassAction) ||
					kinetics.getKineticsDescription().equals(KineticsDescription.General))
				{
					Expression rateExp = kinetics.getKineticsParameterFromRole(Kinetics.ROLE_ReactionRate).getExpression();
					MassActionSolver.MassActionFunction maFunc = MassActionSolver.solveMassAction(rateExp, reactionStep);
					if(maFunc.getForwardRate() == null && maFunc.getReverseRate() == null)
					{
						throw new MappingException("Cannot generate stochastic math mapping for the reaction:" + reactionStep.getName() + "\nLooking for the rate function according to the form of k1*Reactant1^Stoir1*Reactant2^Stoir2...-k2*Product1^Stoip1*Product2^Stoip2.");
					}
					else
					{
						if(maFunc.getForwardRate() != null)
						{
							forwardRate = maFunc.getForwardRate();
						}
						if(maFunc.getReverseRate() != null)
						{
							reverseRate = maFunc.getReverseRate();
						}
					}
				}
			    boolean isForwardRatePresent = false;
			    boolean isReverseRatePresent = false;
		       	if(forwardRate != null)
		    	{
		       		isForwardRatePresent = true;
		    	}
		    	
		    	if(reverseRate != null)
		    	{
		    		isReverseRatePresent = true;
		    	}
			    
				// if the reaction has forward rate (Mass action,HMMs), or don't have either forward or reverse rate (some other rate laws--like general)
				// we process it as forward reaction
				if ((isForwardRatePresent) /*|| ((forwardRate == null) && (reverseRate == null))*/)
				{
					// get jump process name
					String jpName = TokenMangler.mangleToSName(reactionStep.getName());
					// get probability
					Expression exp = null;
									
					// reactions are of mass action form
					exp = getProbabilityRate(reactionStep, forwardRate, true);
					
					MathMapping.ProbabilityParameter probParm = null;
					try{
						probParm = addProbabilityParameter(PARAMETER_PROBABILITYRATE_PREFIX+jpName, exp,MathMapping.PARAMETER_ROLE_P,VCUnitDefinition.UNIT_molecules_per_s,reactionSpecs[i]);
					}catch(PropertyVetoException pve){
						pve.printStackTrace();
						throw new MappingException(pve.getMessage());
					}
					
					//add probability to function or constant
					varHash.addVariable(newFunctionOrConstant(getMathSymbol(probParm,sm.getGeometryClass()),getIdentifierSubstitutions(exp, VCUnitDefinition.UNIT_molecules_per_s, sm.getGeometryClass()),sm.getGeometryClass()));
										
					JumpProcess jp = new JumpProcess(jpName,new Expression(getMathSymbol(probParm,sm.getGeometryClass())));
					// actions
					ReactionParticipant[] reacPart = reactionStep.getReactionParticipants();
					for(int j=0; j<reacPart.length; j++)
					{
						Action action = null;
						SpeciesCountParameter spCountParam = getSpeciesCountParameter(reacPart[j].getSpeciesContext());
						if(reacPart[j] instanceof Reactant)
						{ 
							// check if the reactant is a constant. If the species is a constant, there will be no action taken on this species
							if(!simContext.getReactionContext().getSpeciesContextSpec(reacPart[j].getSpeciesContext()).isConstant()) // not a constant
							{
								int stoi = ((Reactant)reacPart[j]).getStoichiometry();
								action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(-stoi));
								jp.addAction(action);
							}
						}
						else if(reacPart[j] instanceof Product)
						{
							// check if the product is a constant. If the product is a constant, there will be no action taken on this species
							if(!simContext.getReactionContext().getSpeciesContextSpec(reacPart[j].getSpeciesContext()).isConstant()) // not a constant
							{
								int stoi = ((Product)reacPart[j]).getStoichiometry();
								action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(stoi));
								jp.addAction(action);
							}
						}
					}
					// add jump process to compartment subDomain
					subDomain.addJumpProcess(jp);
				}
				if (isReverseRatePresent) // one more jump process for a reversible reaction
				{
					// get jump process name
					String jpName = TokenMangler.mangleToSName(reactionStep.getName())+PARAMETER_PROBABILITY_RATE_REVERSE_SUFFIX;
					Expression exp = null;
					
					// reactions are mass actions
					exp = getProbabilityRate(reactionStep, reverseRate, false);
					
					MathMapping.ProbabilityParameter probRevParm = null;
					try{
						probRevParm = addProbabilityParameter(PARAMETER_PROBABILITYRATE_PREFIX+jpName,exp,MathMapping.PARAMETER_ROLE_P_reverse,VCUnitDefinition.UNIT_molecules_per_s,reactionSpecs[i]);
					}catch(PropertyVetoException pve){
						pve.printStackTrace();
						throw new MappingException(pve.getMessage());
					}
					//add probability to function or constant
					varHash.addVariable(newFunctionOrConstant(getMathSymbol(probRevParm,sm.getGeometryClass()),getIdentifierSubstitutions(exp, VCUnitDefinition.UNIT_molecules_per_s, sm.getGeometryClass()),sm.getGeometryClass()));
									
					JumpProcess jp = new JumpProcess(jpName,new Expression(getMathSymbol(probRevParm,sm.getGeometryClass())));
					// actions
					ReactionParticipant[] reacPart = reactionStep.getReactionParticipants();
					for(int j=0; j<reacPart.length; j++)
					{
						Action action = null;
						SpeciesCountParameter spCountParam = getSpeciesCountParameter(reacPart[j].getSpeciesContext());
						if(reacPart[j] instanceof Reactant)
						{ 
							// check if the reactant is a constant. If the species is a constant, there will be no action taken on this species
							if(!simContext.getReactionContext().getSpeciesContextSpec(reacPart[j].getSpeciesContext()).isConstant()) // not a constant
							{
								int stoi = ((Reactant)reacPart[j]).getStoichiometry();
								action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(stoi));
								jp.addAction(action);
							}
						}
						else if(reacPart[j] instanceof Product)
						{
							// check if the product is a constant. If the product is a constant, there will be no action taken on this species
							if(!simContext.getReactionContext().getSpeciesContextSpec(reacPart[j].getSpeciesContext()).isConstant()) // not a constant
							{
								int stoi = ((Product)reacPart[j]).getStoichiometry();
								action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(-stoi));
								jp.addAction(action);
							}
						}
					}
					// add jump process to compartment subDomain
					subDomain.addJumpProcess(jp);	
				} // end of if(isForwardRateNonZero), if(isReverseRateNonRate)
			}
			else if(reactionStep instanceof FluxReaction)// flux reactions
			{
				//we could set jump processes for general flux rate in forms of p1*Sout + p2*Sin
				if(kinetics.getKineticsDescription().equals(KineticsDescription.General) || kinetics.getKineticsDescription().equals(KineticsDescription.GeneralPermeability) )
				{
					Expression fluxRate = kinetics.getKineticsParameterFromRole(Kinetics.ROLE_ReactionRate).getExpression();
					//we have to pass the math description para to flux solver, coz somehow math description in simulation context is not updated.
					MassActionSolver.MassActionFunction fluxFunc = MassActionSolver.solveMassAction(fluxRate, (FluxReaction)reactionStep);
					//create jump process for forward flux if it exists.
					if(fluxFunc.getForwardRate() != null && !fluxFunc.getForwardRate().isZero()) 
					{
						//jump process name
						String jpName = TokenMangler.mangleToSName(reactionStep.getName());//+"_reverse";
											
						Expression rate = fluxFunc.getForwardRate();
						//get species expression (depend on structure, if mem: Species/mem_Size, if vol: species*KMOLE/vol_size)
						if(fluxFunc.getReactants().size() != 1)
						{
							throw new MappingException("Flux " + reactionStep.getName() + " should have only one reactant." );
						}
						SpeciesContext scOut = fluxFunc.getReactants().get(0).getSpeciesContext();
						Expression speciesFactor = null;
						if(scOut.getStructure() instanceof Feature) {
							Expression numExpr = new Expression(ReservedSymbol.KMOLE.getName());
							Expression denomExpr = new Expression(scOut.getStructure().getStructureSize().getName());
							speciesFactor =  Expression.div(numExpr, denomExpr);
						} else {
							speciesFactor = Expression.invert(new Expression(scOut.getStructure().getStructureSize().getName()));
						}
						Expression speciesExp = Expression.mult(speciesFactor, new Expression(scOut.getName()));	
						//get probability expression by adding factor to rate (rate: rate*size_mem/KMOLE)
						Expression expr1 = Expression.mult(rate, speciesExp);
						Expression numeratorExpr = Expression.mult(expr1, new Expression(sm.getStructure().getStructureSize().getName()));
						Expression denominatorExpr = new Expression(ReservedSymbol.KMOLE.getName());
						Expression probExp = Expression.div(numeratorExpr, denominatorExpr);
						probExp.bindExpression(reactionStep);//bind symbol table before substitute identifiers in the reaction step

						MathMapping.ProbabilityParameter probParm = null;
						try{
							probParm = addProbabilityParameter(PARAMETER_PROBABILITYRATE_PREFIX+jpName,probExp,MathMapping.PARAMETER_ROLE_P,VCUnitDefinition.UNIT_molecules_per_s,reactionSpecs[i]);
						}catch(PropertyVetoException pve){
							pve.printStackTrace();
							throw new MappingException(pve.getMessage());
						}
						//add probability to function or constant
						varHash.addVariable(newFunctionOrConstant(getMathSymbol(probParm,sm.getGeometryClass()),getIdentifierSubstitutions(probExp, VCUnitDefinition.UNIT_molecules_per_s, sm.getGeometryClass()),sm.getGeometryClass()));
										
						JumpProcess jp = new JumpProcess(jpName,new Expression(getMathSymbol(probParm,sm.getGeometryClass())));
						// actions
						Action action = null;
						SpeciesContext sc = fluxFunc.getReactants().get(0).getSpeciesContext();
						
						if (!simContext.getReactionContext().getSpeciesContextSpec(sc).isConstant()) {
							SpeciesCountParameter spCountParam = getSpeciesCountParameter(sc);
							action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(-1));
							jp.addAction(action);
						}	
						
						sc = fluxFunc.getProducts().get(0).getSpeciesContext();
						if (!simContext.getReactionContext().getSpeciesContextSpec(sc).isConstant()) {
							SpeciesCountParameter spCountParam = getSpeciesCountParameter(sc);
							action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(1));
							jp.addAction(action);
						}
							
						subDomain.addJumpProcess(jp);
					}
					//create jump process for reverse flux if it exists.
					if(fluxFunc.getReverseRate() != null && !fluxFunc.getReverseRate().isZero()) 
					{
						//jump process name
						String jpName = TokenMangler.mangleToSName(reactionStep.getName())+PARAMETER_PROBABILITY_RATE_REVERSE_SUFFIX;
											
						Expression rate = fluxFunc.getReverseRate();
						//get species expression (depend on structure, if mem: Species/mem_Size, if vol: species*KMOLE/vol_size)
						if(fluxFunc.getProducts().size() != 1)
						{
							throw new MappingException("Flux " + reactionStep.getName() + " should have only one product." );
						}
						SpeciesContext scIn = fluxFunc.getProducts().get(0).getSpeciesContext();
						Expression speciesFactor = null;
						if(scIn.getStructure() instanceof Membrane) {
							speciesFactor = Expression.invert(new Expression(scIn.getStructure().getStructureSize().getName()));
						} else {
							Expression numExpr = new Expression(ReservedSymbol.KMOLE.getName());
							Expression denomExpr = new Expression(scIn.getStructure().getStructureSize().getName());
							speciesFactor =  Expression.div(numExpr, denomExpr);
						}
						Expression speciesExp = Expression.mult(speciesFactor, new Expression(scIn.getName()));	
						//get probability expression by adding factor to rate (rate: rate*size_mem/KMOLE)
						Expression expr1 = Expression.mult(rate, speciesExp);
						Expression numeratorExpr = Expression.mult(expr1, new Expression(sm.getStructure().getStructureSize().getName()));
						Expression denominatorExpr = new Expression(ReservedSymbol.KMOLE.getName());
						Expression probRevExp = Expression.div(numeratorExpr, denominatorExpr);
						probRevExp.bindExpression(reactionStep);//bind symbol table before substitute identifiers in the reaction step
						
						MathMapping.ProbabilityParameter probRevParm = null;
						try{
							probRevParm = addProbabilityParameter(PARAMETER_PROBABILITYRATE_PREFIX+jpName,probRevExp,MathMapping.PARAMETER_ROLE_P_reverse,VCUnitDefinition.UNIT_molecules_per_s,reactionSpecs[i]);
						}catch(PropertyVetoException pve){
							pve.printStackTrace();
							throw new MappingException(pve.getMessage());
						}
						//add probability to function or constant
						varHash.addVariable(newFunctionOrConstant(getMathSymbol(probRevParm,sm.getGeometryClass()),getIdentifierSubstitutions(probRevExp, VCUnitDefinition.UNIT_molecules_per_s, sm.getGeometryClass()),sm.getGeometryClass()));
										
						JumpProcess jp = new JumpProcess(jpName,new Expression(getMathSymbol(probRevParm,sm.getGeometryClass())));
						// actions
						Action action = null;
						SpeciesContext sc = fluxFunc.getReactants().get(0).getSpeciesContext();
						if (!simContext.getReactionContext().getSpeciesContextSpec(sc).isConstant()) {
							SpeciesCountParameter spCountParam = getSpeciesCountParameter(sc);
							action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(1));
							jp.addAction(action);
						}
							
						sc = fluxFunc.getProducts().get(0).getSpeciesContext();
						if (!simContext.getReactionContext().getSpeciesContextSpec(sc).isConstant()) {
							SpeciesCountParameter spCountParam = getSpeciesCountParameter(sc);
							action = Action.createIncrementAction(varHash.getVariable(getMathSymbol(spCountParam, sm.getGeometryClass())),new Expression(-1));
							jp.addAction(action);
						}
						
						subDomain.addJumpProcess(jp);
					}
				}
			}//end of if (simplereaction)...else if(fluxreaction)
		} // end of reaction step loop
			
		//
		// set Variables to MathDescription all at once with the order resolved by "VariableHash"
		//
		mathDesc.setAllVariables(varHash.getAlphabeticallyOrderedVariables());
		
		// set up variable initial conditions in subDomain
		SpeciesContextSpec scSpecs[] = simContext.getReactionContext().getSpeciesContextSpecs();
		for (int i = 0; i < speciesContextSpecs.length; i++){
			//get stochastic variable by name
			SpeciesCountParameter spCountParam = getSpeciesCountParameter(speciesContextSpecs[i].getSpeciesContext());
			StructureMapping sm = simContext.getGeometryContext().getStructureMapping(speciesContextSpecs[i].getSpeciesContext().getStructure());
			String varName = getMathSymbol(spCountParam, sm.getGeometryClass()); 

			StochVolVariable var = (StochVolVariable)mathDesc.getVariable(varName);
			SpeciesContextSpec.SpeciesContextSpecParameter initParm = scSpecs[i].getInitialCountParameter();//stochastic use initial number of particles
			//stochastic variables initial expression.
			if (initParm!=null)
			{
				VarIniCondition varIni = null;
				if(getSimulationContext().isUsingConcentration() && !scSpecs[i].isConstant())
				{
					varIni = new VarIniPoissonExpectedCount(var,new Expression(getMathSymbol(initParm, sm.getGeometryClass())));
				}
				else 
				{
					varIni = new VarIniCount(var,new Expression(getMathSymbol(initParm, sm.getGeometryClass())));
				}
				
				subDomain.addVarIniCondition(varIni);
			}
		}

		if (!mathDesc.isValid()){
			throw new MappingException("generated an invalid mathDescription: "+mathDesc.getWarning());
		}
	}

/**
 * 
 * @param expr
 * @param bConcentration
 * @return
 * @throws ExpressionException
 */	
private Expression getSubstitutedExpr(Expression expr, boolean bConcentration, boolean bIsInitialCondn) throws ExpressionException {
	expr = new Expression(expr);
	String[] symbols = expr.getSymbols();
	// Check if 'expr' has other speciesContexts in its expression, need to replace it with 'spContext_init'
	for (int j = 0; symbols != null && j < symbols.length; j++) {
		// if symbol is a speciesContext, replacing it with a reference to initial condition for that speciesContext.
		SpeciesContext spC = null;
		SymbolTableEntry ste = expr.getSymbolBinding(symbols[j]);
		if (ste instanceof ProxyParameter) {
			// if expression is for speciesContextSpec or Kinetics, ste will be a ProxyParameter instance.
			ProxyParameter spspp = (ProxyParameter)ste;
			if (spspp.getTarget() instanceof SpeciesContext) {
				spC = (SpeciesContext)spspp.getTarget();
			}
		} else if (ste instanceof SpeciesContext) {
			// if expression is for a global parameter, ste will be a SpeciesContext instance. 
			spC = (SpeciesContext)ste;
		}
		if (spC != null) {
			SpeciesContextSpec spcspec = getSimulationContext().getReactionContext().getSpeciesContextSpec(spC);
			Parameter spCParm = null;
			if (bConcentration && bIsInitialCondn) {
				// speciesContext has initConcentration set, so need to replace 'spContext' in 'expr' 'spContext_init'
				spCParm = spcspec.getParameterFromRole(SpeciesContextSpec.ROLE_InitialConcentration);
			} else if (!bConcentration && bIsInitialCondn) {
				// speciesContext has initCount set, so need to replace 'spContext' in 'expr' 'spContext_initCount'
				spCParm = spcspec.getParameterFromRole(SpeciesContextSpec.ROLE_InitialCount);
			} else if (bConcentration && !bIsInitialCondn) {
				// need to replace 'spContext' in 'expr' 'spContext_Conc'
				spCParm = getSpeciesConcentrationParameter(spC);
			} else if (!bConcentration && !bIsInitialCondn) {
				// need to replace 'spContext' in 'expr' 'spContext_Count'
				spCParm = getSpeciesCountParameter(spC);
			}
			// need to get init condn expression, but can't get it from getMathSymbol() (mapping between bio and math), hence get it as below.
			Expression scsInitExpr = new Expression(spCParm, getNameScope());
//			scsInitExpr.bindExpression(this);
			expr.substituteInPlace(new Expression(spC.getName()), scsInitExpr);
		}
	}
	return expr;
}
	
	
/**
 * Insert the method's description here.
 * Creation date: (10/26/2006 11:47:26 AM)
 * @exception cbit.vcell.parser.ExpressionException The exception description.
 * @exception cbit.vcell.mapping.MappingException The exception description.
 * @exception cbit.vcell.math.MathException The exception description.
 */
@Override
protected void refreshSpeciesContextMappings() throws ExpressionException, MappingException, MathException 
{
	//
	// create a SpeciesContextMapping for each speciesContextSpec.
	//
	// set initialExpression from SpeciesContextSpec.
	// set diffusing5
	// set variable (only if "Constant" or "Function", else leave it as null)-----why commented?
	//

	//
	// have to put geometric paras into mathsymbolmapping, since species initial condition needs the volume size symbol.
	// and the parameters later on were added into contants or functions in refreshMathDescription()
	//
	StructureMapping structureMappings[] = getSimulationContext().getGeometryContext().getStructureMappings();
	for (int i=0;i<structureMappings.length;i++){
		StructureMapping sm = structureMappings[i];
		StructureMapping.StructureMappingParameter parm = sm.getParameterFromRole(StructureMapping.ROLE_Size);
		getMathSymbol(parm,sm.getGeometryClass());
	}

	
	getSpeciesContextMappingList().removeAllElements();
	
	SpeciesContextSpec speciesContextSpecs[] = getSimulationContext().getReactionContext().getSpeciesContextSpecs();
	for (int i=0;i<speciesContextSpecs.length;i++){
		SpeciesContextSpec scs = speciesContextSpecs[i];

		SpeciesContextMapping scm = new SpeciesContextMapping(scs.getSpeciesContext());
		scm.setPDERequired(false);
		scm.setHasEventAssignment(false);
		// We still want the stochastic constant species context to be a fixed function, but still stochvolumnVar.
		// we don't eliminate variables for stochastic
		scm.setDependencyExpression(null);
		// We don't participant in fast reaction step for stochastic
		scm.setFastParticipant(false);
		
		getSpeciesContextMappingList().addElement(scm);
	}
}


/**
 * Map speciesContext to variable, used for structural analysis (slow reactions and fast reactions)
 * Creation date: (10/25/2006 8:59:43 AM)
 * @exception cbit.vcell.mapping.MappingException The exception description.
 */
@Override
protected void refreshVariables() throws MappingException {
	Enumeration<SpeciesContextMapping> enum1 = getSpeciesContextMappings();

	//
	// non-constant independant variables require either a membrane or volume variable
	//
	enum1 = getSpeciesContextMappings();
	while (enum1.hasMoreElements()){
		SpeciesContextMapping scm = (SpeciesContextMapping)enum1.nextElement();
		SpeciesContextSpec scs = getSimulationContext().getReactionContext().getSpeciesContextSpec(scm.getSpeciesContext());
		//stochastic variable is always a function of size.
		MathMapping.SpeciesCountParameter spCountParm = null;
		try{
			String countName = scs.getSpeciesContext().getName() + BIO_PARAM_SUFFIX_SPECIES_COUNT;
			Expression countExp = new Expression(0.0);
			spCountParm = addSpeciesCountParameter(countName, countExp, MathMapping.PARAMETER_ROLE_COUNT, VCUnitDefinition.UNIT_molecules, scs);
		}catch(PropertyVetoException pve){
			pve.printStackTrace();
			throw new MappingException(pve.getMessage());
		}
		
		//add concentration of species as MathMappingParameter - this will map to species concentration function
		try{
			String concName = scs.getSpeciesContext().getName() + BIO_PARAM_SUFFIX_SPECIES_CONCENTRATION;
			Expression concExp = getExpressionAmtToConc(new Expression(spCountParm.getName()), scs.getSpeciesContext());
			concExp.bindExpression(this);
			addSpeciesConcentrationParameter(concName, concExp, MathMapping.PARAMETER_ROLE_CONCENRATION, scs.getSpeciesContext().getUnitDefinition(), scs);
		}catch(Exception e){
			e.printStackTrace();
			throw new MappingException(e.getMessage());
		}
		//we always add variables, all species are independent variables, no matter they are constant or not.
		scm.setVariable(new StochVolVariable(getMathSymbol(spCountParm, getSimulationContext().getGeometryContext().getStructureMapping(scs.getSpeciesContext().getStructure()).getGeometryClass())));
		mathSymbolMapping.put(scm.getSpeciesContext(),scm.getVariable().getName());
		
	}
}

}