package org.vcell.model.rbm;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.vcell.model.rbm.RuleAnalysis.MolecularTypeEntry;
import org.vcell.util.Pair;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.mapping.NetworkTransformer;
import cbit.vcell.mapping.ParameterContext.LocalParameter;
import cbit.vcell.mapping.ReactionRuleSpec;
import cbit.vcell.mapping.RulebasedTransformer;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.SimulationContext.NetworkGenerationRequirements;
import cbit.vcell.mapping.SimulationContext.NetworkGenerationRequirements.RequestType;
import cbit.vcell.mapping.SpeciesContextSpec;
import cbit.vcell.model.MassActionKinetics;
import cbit.vcell.model.Model;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.model.Model.RbmModelContainer;
import cbit.vcell.model.Parameter;
import cbit.vcell.model.Product;
import cbit.vcell.model.RbmKineticLaw;
import cbit.vcell.model.RbmKineticLaw.RbmKineticLawParameterType;
import cbit.vcell.model.RbmObservable;
import cbit.vcell.model.Reactant;
import cbit.vcell.model.ReactionParticipant;
import cbit.vcell.model.ReactionRule;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.parser.Expression;
import cbit.vcell.solver.DefaultOutputTimeSpec;
import cbit.vcell.solver.SolverTaskDescription;
import cbit.vcell.solver.TimeBounds;
import cbit.vcell.solver.UniformOutputTimeSpec;

public class RbmNetworkGenerator {
	
	private static final String BEGIN_MODEL = "begin model";
	private static final String END_MODEL = "end model";
	private static final String BEGIN_REACTIONS = "begin reaction rules";
	private static final String END_REACTIONS = "end reaction rules";
	private static final String BEGIN_OBSERVABLES = "begin observables";
	private static final String END_OBSERVABLES = "end observables";
	private static final String BEGIN_FUNCTIONS = "begin functions";
	private static final String END_FUNCTIONS = "end functions";
	private static final String END_MOLECULE_TYPES = "end molecule types";
	private static final String BEGIN_MOLECULE_TYPES = "begin molecule types";
	private static final String BEGIN_SPECIES = "begin seed species";
	private static final String END_SPECIES = "end seed species";
	private static final String END_PARAMETERS = "end parameters";
	private static final String BEGIN_PARAMETERS = "begin parameters";

@Deprecated
	public static void writeBngl(BioModel bioModel, PrintWriter writer) {
		SimulationContext sc = bioModel.getSimulationContexts()[0];	// we assume one single simulation context which may not be the case
		writeBngl(sc, writer, false, false);	// don't apply the app filters
	}
	public static void writeBngl(SimulationContext simulationContext, PrintWriter writer, boolean ignoreFunctions, 
			boolean applyApplicationFilters) {
		Model model = simulationContext.getModel();
		RbmModelContainer rbmModelContainer = model.getRbmModelContainer();
		
		writer.println(BEGIN_MODEL);
		writer.println();
		
		RbmNetworkGenerator.writeParameters(writer, rbmModelContainer, ignoreFunctions);
		RbmNetworkGenerator.writeMolecularTypes(writer, rbmModelContainer);
		RbmNetworkGenerator.writeSpecies(writer, model, simulationContext);
		RbmNetworkGenerator.writeObservables(writer, rbmModelContainer);
		RbmNetworkGenerator.writeFunctions(writer, rbmModelContainer, ignoreFunctions);
		RbmNetworkGenerator.writeReactions(writer, rbmModelContainer, null, applyApplicationFilters);
		
		writer.println(END_MODEL);	
		writer.println();
		
		RbmNetworkGenerator.writeNetworkConstraints(writer, rbmModelContainer, simulationContext, NetworkGenerationRequirements.ComputeFullStandardTimeout);
		writer.println();
	}
	// modified bngl writer for special use restricted to network transform functionality
	public static void writeBngl_internal(SimulationContext simulationContext, PrintWriter writer,
			Map<FakeReactionRuleRateParameter, LocalParameter> kineticsParameterMap, 
			Map<FakeSeedSpeciesInitialConditionsParameter, Pair<SpeciesContext, Expression>> speciesEquivalenceMap, 
			NetworkGenerationRequirements networkGenerationRequirements) {
		
		String callerClassName = new Exception().getStackTrace()[1].getClassName();
		String networkTransformerClassName = NetworkTransformer.class.getName();
		String rulebasedTransformerClassName = RulebasedTransformer.class.getName();
		if(!callerClassName.equals(networkTransformerClassName) && !callerClassName.equals(rulebasedTransformerClassName)) {
			throw new UnsupportedOperationException("This method may only be called from within a " + networkTransformerClassName + " or " + rulebasedTransformerClassName + " instance.");
		}
		Model model = simulationContext.getModel();
		RbmModelContainer rbmModelContainer = model.getRbmModelContainer();
		
		// first we prepare the fake parameters we need to maintain the relationship between the species context and the seed species
		List<FakeSeedSpeciesInitialConditionsParameter> fakeParameterList = new ArrayList<FakeSeedSpeciesInitialConditionsParameter>();
		List<String> seedSpeciesList = new ArrayList<String>();
		SpeciesContext[] speciesContexts = model.getSpeciesContexts();
//		Long uuid = UUID.randomUUID().getMostSignificantBits();
//		if(uuid<0) uuid = -uuid;
//		String nameRoot = "p" + uuid;
		for(int i=0; i<speciesContexts.length; i++) {
			SpeciesContext sc = speciesContexts[i];
			if(!sc.hasSpeciesPattern()) { continue; }
			
			SpeciesContextSpec scs = simulationContext.getReactionContext().getSpeciesContextSpec(sc);
			Expression initialConcentration = scs.getParameter(SpeciesContextSpec.ROLE_InitialConcentration).getExpression();
			
			// fake initial values for the seed species, we need to present them to bngl as parameters
			FakeSeedSpeciesInitialConditionsParameter fakeSeedSpeciesParam = new FakeSeedSpeciesInitialConditionsParameter(sc.getName());
			Pair<SpeciesContext, Expression> p = new Pair<SpeciesContext, Expression>(sc, initialConcentration);
			speciesEquivalenceMap.put(fakeSeedSpeciesParam, p);
			
			String modified = RbmUtils.toBnglString(sc.getSpeciesPattern());
			modified += "\t\t" + fakeSeedSpeciesParam.fakeParameterName;
			seedSpeciesList.add(modified);				// we build the seed species list now, we write it later (in the BEGIN SPECIES block)
			fakeParameterList.add(fakeSeedSpeciesParam);
		}

		// second we produce the bngl file
		writer.println(BEGIN_MODEL);
		writer.println();
		
		for (ReactionRuleSpec rrs : simulationContext.getReactionContext().getReactionRuleSpecs()){
			if (!rrs.isExcluded()){
				ReactionRule reactionRule = rrs.getReactionRule();
				RbmKineticLaw kineticLaw = reactionRule.getKineticLaw();
				switch (kineticLaw.getRateLawType()){
				case MassAction:{
					FakeReactionRuleRateParameter fakeRateParameterForward = new FakeReactionRuleRateParameter(reactionRule,RbmKineticLawParameterType.MassActionForwardRate);
					LocalParameter origForwardRateParameter = kineticLaw.getLocalParameter(RbmKineticLawParameterType.MassActionForwardRate);
					kineticsParameterMap.put(fakeRateParameterForward, origForwardRateParameter);
					if (reactionRule.isReversible()){
						FakeReactionRuleRateParameter fakeRateParameterReverse = new FakeReactionRuleRateParameter(reactionRule,RbmKineticLawParameterType.MassActionReverseRate);
						LocalParameter origReverseRateParameter = kineticLaw.getLocalParameter(RbmKineticLawParameterType.MassActionReverseRate);
						kineticsParameterMap.put(fakeRateParameterReverse, origReverseRateParameter);
					}
					break;
				}
				default:{
					throw new RuntimeException("kinetic law type "+kineticLaw.getRateLawType().name()+" not yet implemented");
				}
				}
			}
		}
		
		writer.println(BEGIN_PARAMETERS);
		// the fake parameters used for reaction rule kinetics
		
		for (FakeReactionRuleRateParameter p : kineticsParameterMap.keySet()) {
			writer.println(p.fakeParameterName+"\t\t1");
		}
		// printing BNGL for internal use, use placeholders for all parameters
		// the fake parameters used at initial values for the seed species
		for (FakeSeedSpeciesInitialConditionsParameter s : fakeParameterList) {
			writer.println(s.fakeParameterName+"\t\t1");
		}
		writer.println(END_PARAMETERS);
		writer.println();
		
		RbmNetworkGenerator.writeMolecularTypes(writer, rbmModelContainer);

		// write modified version of seed species while maintaining the connection between the species context and the real seed species
		writer.println(BEGIN_SPECIES);
		for (String s : seedSpeciesList) {
			writer.println(s);
		}
		writer.println(END_SPECIES);
		writer.println();
		
		RbmNetworkGenerator.writeObservables(writer, rbmModelContainer);
		RbmNetworkGenerator.writeReactions_internal(writer, simulationContext);
		
		writer.println(END_MODEL);	
		writer.println();

		if(callerClassName.equals(networkTransformerClassName)) {
			RbmNetworkGenerator.writeNetworkConstraints(writer, rbmModelContainer, simulationContext, networkGenerationRequirements);
		} else if (callerClassName.equals(rulebasedTransformerClassName)) {
			writer.println();
			writer.println("writeXML()");
		}
		writer.println();
	}
	// modified bngl writer for special use restricted to network transform functionality
	public static void writeBngl_internal(RuleAnalysis.RuleEntry ruleEntry, PrintWriter writer) {
		
		writer.println(BEGIN_MODEL);
		writer.println();
		
		writer.println(BEGIN_MOLECULE_TYPES);
		ArrayList<String> uniqueMolecularTypeBNGLs = new ArrayList<String>();
		ArrayList<MolecularTypeEntry> allMolecularTypePatterns = new ArrayList<MolecularTypeEntry>();
		allMolecularTypePatterns.addAll(ruleEntry.getReactantMolecularTypeEntries());
		allMolecularTypePatterns.addAll(ruleEntry.getProductMolecularTypeEntries());
		for (MolecularTypeEntry mte : allMolecularTypePatterns){
			String bngl = mte.getMolecularTypeBNGL();
			if (!uniqueMolecularTypeBNGLs.contains(bngl)){
				uniqueMolecularTypeBNGLs.add(bngl);
			}
		}
		for (String bngl : uniqueMolecularTypeBNGLs) {
			writer.println(bngl);
		}
		writer.println(END_MOLECULE_TYPES);
		writer.println();

//		// write modified version of seed species while maintaining the connection between the species context and the real seed species
//		writer.println(BEGIN_SPECIES);
//		for (String s : seedSpeciesList) {
//			writer.println(s);
//		}
//		writer.println(END_SPECIES);
//		writer.println();
		
		writer.println(BEGIN_REACTIONS);
		writer.println(ruleEntry.getRuleName()+":"+"      "+ruleEntry.getReactionBNGLShort()+"    1.0");
		writer.println(END_REACTIONS);	
		writer.println();
		
		writer.println(END_MODEL);	
		writer.println();
		
		writer.println("writeXML()");
		writer.println();
	}
	private static void writeParameters(PrintWriter writer, RbmModelContainer rbmModelContainer, boolean ignoreFunctions) {
		writer.println(BEGIN_PARAMETERS);
		List<Parameter> paramList = rbmModelContainer.getParameterList();
		for (Parameter param : paramList) {
			writer.println(RbmUtils.toBnglString(param,false));
		}
		if(ignoreFunctions) {	// we cheat and transform all functions into constant parameters
			List<Parameter> functionList = rbmModelContainer.getFunctionList();
			for (Parameter function : functionList) {
				writer.println(RbmUtils.toBnglStringIgnoreExpression(function));
			}
		}
		writer.println(END_PARAMETERS);
		writer.println();
	}
	private static void writeMolecularTypes(PrintWriter writer, RbmModelContainer rbmModelContainer) {
		writer.println(BEGIN_MOLECULE_TYPES);
		List<MolecularType> molList = rbmModelContainer.getMolecularTypeList();
		for (MolecularType mt : molList) {
			writer.println(RbmUtils.toBnglString(mt));
		}
		writer.println(END_MOLECULE_TYPES);
		writer.println();
	}
	private static void writeSpecies(PrintWriter writer, Model model, SimulationContext simulationContext) {
		writer.println(BEGIN_SPECIES);
		SpeciesContext[] speciesContexts = model.getSpeciesContexts();
		for(SpeciesContext sc : speciesContexts) {
			if(!sc.hasSpeciesPattern()) { continue; }
			writer.println(RbmUtils.toBnglString(simulationContext, sc));
		}
		writer.println(END_SPECIES);
		writer.println();
	}
	private static void writeObservables(PrintWriter writer, RbmModelContainer rbmModelContainer) {
		writer.println(BEGIN_OBSERVABLES);
		List<RbmObservable> observablesList = rbmModelContainer.getObservableList();
		for (RbmObservable oo : observablesList) {
			writer.println(RbmUtils.toBnglString(oo));
		}
		writer.println(END_OBSERVABLES);
		writer.println();
	}
	private static void writeFunctions(PrintWriter writer, RbmModelContainer rbmModelContainer, boolean ignoreFunctions) {
		if(!ignoreFunctions) {
			writer.println(BEGIN_FUNCTIONS);
			List<Parameter> functionList = rbmModelContainer.getFunctionList();
			for (Parameter function : functionList) {
				writer.println(RbmUtils.toBnglString(function,true));
			}
			writer.println(END_FUNCTIONS);
			writer.println();
		}
	}
	private static void writeReactions(PrintWriter writer, RbmModelContainer rbmModelContainer, SimulationContext sc, boolean applyApplicationFilters) {
		writer.println(BEGIN_REACTIONS);
		List<ReactionRule> reactionList = rbmModelContainer.getReactionRuleList();
		for (ReactionRule rr : reactionList) {
			if(applyApplicationFilters && sc != null) {
				ReactionRuleSpec rrs = sc.getReactionContext().getReactionRuleSpec(rr);
				if(rrs != null && rrs.isExcluded()) {
				continue;		// we skip those rules which are disabled (excluded)
				}
			}
			writer.println(RbmUtils.toBnglStringLong(rr));
		}
		writer.println(END_REACTIONS);	
		writer.println();
	}
	
	private static void writeReactions_internal(PrintWriter writer, SimulationContext sc) {
		writer.println(BEGIN_REACTIONS);
		for (ReactionRuleSpec rrSpec : sc.getReactionContext().getReactionRuleSpecs()) {
			if (rrSpec.isExcluded()){
				continue;		// we skip those rules which are disabled (excluded)
			}
			writer.println(RbmUtils.toBnglStringLong_internal(rrSpec.getReactionRule()));
		}
		writer.println(END_REACTIONS);	
		writer.println();
	}
	private static void writeNetworkConstraints(PrintWriter writer, RbmModelContainer rbmModelContainer, SimulationContext sc, NetworkGenerationRequirements networkGenerationRequirements) {
		if(sc.getApplicationType().equals(SimulationContext.Application.NETWORK_DETERMINISTIC)) {
			generateNetwork(writer, rbmModelContainer, sc, networkGenerationRequirements);
		} else if(sc.getApplicationType().equals(SimulationContext.Application.NETWORK_STOCHASTIC)) {
			generateNetwork(writer, rbmModelContainer, sc, networkGenerationRequirements);
		} else if(sc.getApplicationType().equals(SimulationContext.Application.RULE_BASED_STOCHASTIC)) {
			runNFSim(writer, rbmModelContainer, sc, networkGenerationRequirements);
		}
	}
	public static void runNFSim(PrintWriter writer, RbmModelContainer rbmModelContainer, SimulationContext sc, NetworkGenerationRequirements networkGenerationRequirements) {
		// ex: simulate_nf({t_end=>100,n_steps=>50});
		writer.print("simulate_nf({");
		if(sc.getBioModel() == null || sc.getSimulations() == null || sc.getSimulations().length == 0) {
			writer.print("t_end=>100,n_steps=>50");
			writer.println("})");
			return;
		}
		// we just pick whatever the first simulation has, it'll get too complicated to offer the user a list of simulations and ask him to choose
		SolverTaskDescription solverTaskDescription = sc.getSimulations(0).getSolverTaskDescription();
		TimeBounds tb = solverTaskDescription.getTimeBounds();
		double dtime = tb.getEndingTime() - tb.getStartingTime();
		if(solverTaskDescription.getOutputTimeSpec() instanceof UniformOutputTimeSpec) {
			UniformOutputTimeSpec uots = (UniformOutputTimeSpec)solverTaskDescription.getOutputTimeSpec();
			double interval = uots.getOutputTimeStep();
			int steps = (int)Math.round(dtime/interval);
			writer.print("t_end=>" + dtime + ",n_steps=>" + steps);
		} else if(solverTaskDescription.getOutputTimeSpec() instanceof DefaultOutputTimeSpec) {		// currently unsupported, but some old simulations have it
			writer.print("t_end=>" + dtime + ",n_steps=>50");
		} else {
			writer.print("t_end=>100,n_steps=>50");
		}
		writer.println("})");
	}
	public static void generateNetwork(PrintWriter writer, RbmModelContainer rbmModelContainer, SimulationContext sc, NetworkGenerationRequirements networkGenerationRequirements) {
		List<MolecularType> molList = rbmModelContainer.getMolecularTypeList();
		NetworkConstraints constraints = sc.getNetworkConstraints();
		generateNetworkEx(constraints.getMaxIteration(), constraints.getMaxMoleculesPerSpecies(), writer, rbmModelContainer, sc, networkGenerationRequirements);
	}
	public static void generateNetworkEx(int maxIterations, int maxMoleculesPerSpecies, PrintWriter writer, RbmModelContainer rbmModelContainer, SimulationContext sc, NetworkGenerationRequirements networkGenerationRequirements) {
		List<MolecularType> molList = rbmModelContainer.getMolecularTypeList();
		NetworkConstraints constraints = sc.getNetworkConstraints();
		writer.print("generate_network({");
		if(networkGenerationRequirements.requestType == RequestType.AllowTruncatedNetwork) {
			// this is called when we create the first simulation in a new application
			// we don't really care about the network, we just want to do the minimal thing (the fastest)
			// hence we just do one single iteration
			writer.print("max_iter=>1");
		} else if (networkGenerationRequirements.requestType == RequestType.ComputeFullNetwork) {
			writer.print("max_iter=>" + maxIterations);
		} else {
			throw new RuntimeException("internal error: invocation of BioNetGen called unexpectly");
		}
		writer.print(",");
		writer.print("max_agg=>" + maxMoleculesPerSpecies);
		StringBuilder max_stoich = new StringBuilder(); 
		for (MolecularType mt : molList) {
			Integer stoich = constraints.getMaxStoichiometry(mt);
			if (stoich != null) {
				if (max_stoich.length() > 0) {
					max_stoich.append(",");
				}
				max_stoich.append(mt.getName() + "=>" + stoich);
			}
		}
		if (max_stoich.length() > 0) {
			writer.print(",max_stoich={" + max_stoich + "}");
		}
		writer.print(",overwrite=>1");
		writer.println("})");
	}

	private static class ReactionLine {
		String no;
		String reactants;
		String products;
		String ruleLabel;
		private ReactionLine(String no, String reactants, String products,
				String ruleLabel) {
			super();
			this.no = no;
			this.reactants = reactants;
			this.products = products;
			this.ruleLabel = ruleLabel;
		}
	}
	
	public static void generateModel(BioModel bioModel, String netfile) throws Exception {
		Model model = bioModel.getModel();
		Map<String, SpeciesContext> speciesMap = new HashMap<String, SpeciesContext>();
		Map<String, ReactionStep> reactionMap = new HashMap<String, ReactionStep>();
		List<ReactionLine> reactionLineList = new ArrayList<ReactionLine>();
		BufferedReader br = new BufferedReader(new StringReader(netfile));
		int reversibleCount = 0;
		int reactionCount = 0;
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			line = line.trim();
			if (line.equals(BEGIN_PARAMETERS)) {
				while (true) {
					String line2 = br.readLine();
					line2 = line2.trim();
					if (line2.length() == 0) {
						continue;
					}
					if (line2.equals(END_PARAMETERS)) {
						break;
					}					
					StringTokenizer st = new StringTokenizer(line2);
					String token1 = st.nextToken();
					String token2 = st.nextToken();
					String token3 = st.nextToken();
					ModelParameter mp = model.new ModelParameter(token2, new Expression(token3), Model.ROLE_UserDefined, bioModel.getModel().getUnitSystem().getInstance_TBD());
					model.addModelParameter(mp);
				}
			} else if (line.equals(BEGIN_SPECIES)) {
				while (true) {
					String line2 = br.readLine();
					line2 = line2.trim();
					if (line2.length() == 0) {
						continue;
					}
					if (line2.equals(END_SPECIES)) {
						break;
					}					
					
					StringTokenizer st = new StringTokenizer(line2);
					String token1 = st.nextToken();  // no
					String token2 = st.nextToken();  // pattern
					String token3 = st.nextToken();  // initial condition
					
					String newname = token2.replaceAll("\\." , "_");
					newname = newname.replaceAll("[\\(,][a-zA-Z]\\w*", "");
					newname = newname.replaceAll("~|!\\d*", "");
					newname = newname.replaceAll("\\(\\)", "");
					newname = newname.replaceAll("\\)", "");
					
					SpeciesContext sc = model.createSpeciesContext(model.getStructure(0));
					sc.setName(newname);
					bioModel.getVCMetaData().setFreeTextAnnotation(sc, token2);					
					bioModel.getVCMetaData().setFreeTextAnnotation(sc.getSpecies(), token2);					
					speciesMap.put(token1, sc);
				}				
			} else if (line.equals(BEGIN_REACTIONS)) {
				while (true) {
					String line2 = br.readLine();
					line2 = line2.trim();
					if (line2.length() == 0) {
						continue;
					}
					if (line2.equals(END_REACTIONS)) {
						break;
					}
					++ reactionCount;
					
					StringTokenizer st = new StringTokenizer(line2);
					String token1 = st.nextToken();
					String token2 = st.nextToken(); // reactants
					String token3 = st.nextToken(); // products
					String token4 = st.nextToken(); // rate
					String token5 = st.nextToken();
					
					boolean bFoundReversible = false;
					Expression rate = new Expression(token4);
					for (ReactionLine rl : reactionLineList) {
						if (token2.equals(rl.products) && token3.equals(rl.reactants) && token5.equals(rl.ruleLabel + "r")) {
							ReactionStep rs = reactionMap.get(rl.no);
							((MassActionKinetics)rs.getKinetics()).getReverseRateParameter().setExpression(rate);
							reactionLineList.remove(rl);
							bFoundReversible = true;
							break;
						}
					}
					if (bFoundReversible) {
						++ reversibleCount;
						continue;
					}	
					ReactionLine rl = new ReactionLine(token1, token2, token3, token5);
					reactionLineList.add(rl);
					SimpleReaction reaction = model.createSimpleReaction(model.getStructure(0));
					reactionMap.put(token1, reaction);
					
					reaction.setModel(model);
					bioModel.getVCMetaData().setFreeTextAnnotation(reaction, line2);
					MassActionKinetics kinetics = new MassActionKinetics(reaction);
					reaction.setKinetics(kinetics);
					
					st = new StringTokenizer(token2, ",");
					while (st.hasMoreTokens()) {
						String t = st.nextToken();
						SpeciesContext sc = speciesMap.get(t);
						if (sc != null) {
							boolean bExists = false;
							for (ReactionParticipant rp : reaction.getReactionParticipants()) {
								if (rp instanceof Reactant && rp.getSpeciesContext() == sc) {
									rp.setStoichiometry(rp.getStoichiometry() + 1);
									bExists = true;
									break;
								}
							}
							if (!bExists) {
								reaction.addReactant(sc, 1);
							}
						}
					}
					st = new StringTokenizer(token3, ",");
					while (st.hasMoreTokens()) {
						String t = st.nextToken();
						SpeciesContext sc = speciesMap.get(t);
						if (sc != null) {
							boolean bExists = false;
							for (ReactionParticipant rp : reaction.getReactionParticipants()) {
								if (rp instanceof Product && rp.getSpeciesContext() == sc) {
									rp.setStoichiometry(rp.getStoichiometry() + 1);
									bExists = true;
									break;
								}
							}
							if (!bExists) {
								reaction.addProduct(sc, 1);
							}
						}
					}					
					kinetics.getForwardRateParameter().setExpression(rate);
				}
			}
		}
		System.out.println(model.getNumSpecies() + " species added");
		System.out.println(model.getNumReactions() + " reactions added");
		System.out.println(reversibleCount + " reversible reactions found");
		if (reactionCount != model.getNumReactions() + reversibleCount) {
			throw new RuntimeException("Reactions are not imported correctly!");
		}
	}
}
