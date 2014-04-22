package org.vcell.sedml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jlibsedml.Algorithm;
import org.jlibsedml.ChangeAttribute;
import org.jlibsedml.ComputeChange;
import org.jlibsedml.Curve;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Model;
import org.jlibsedml.Notes;
import org.jlibsedml.Output;
import org.jlibsedml.Parameter;
import org.jlibsedml.Plot2D;
import org.jlibsedml.Range;
import org.jlibsedml.RepeatedTask;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.SetValue;
import org.jlibsedml.Task;
import org.jlibsedml.UniformRange;
import org.jlibsedml.UniformTimeCourse;
import org.jlibsedml.VariableSymbol;
import org.jlibsedml.VectorRange;
import org.jlibsedml.XPathTarget;
import org.jlibsedml.modelsupport.SBMLSupport;
import org.jlibsedml.modelsupport.SBMLSupport.CompartmentAttribute;
import org.jlibsedml.modelsupport.SBMLSupport.ParameterAttribute;
import org.jlibsedml.modelsupport.SBMLSupport.SpeciesAttribute;
import org.jlibsedml.modelsupport.SUPPORTED_LANGUAGE;
import org.jmathml.ASTCi;
import org.jmathml.ASTNode;
import org.jmathml.MathMLReader;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.vcell.sbml.SimSpec;
import org.vcell.sbml.vcell.StructureSizeSolver;
import org.vcell.util.TokenMangler;

import com.sun.swing.internal.plaf.basic.resources.basic_zh_HK;

import cbit.util.xml.XmlUtil;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.mapping.MathMapping;
import cbit.vcell.mapping.MathSymbolMapping;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.SpeciesContextSpec;
import cbit.vcell.mapping.SpeciesContextSpec.SpeciesContextSpecParameter;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.mapping.StructureMapping.StructureMappingParameter;
import cbit.vcell.math.Function;
import cbit.vcell.model.Kinetics.KineticsParameter;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.model.Model.ReservedSymbol;
import cbit.vcell.model.Model.ReservedSymbolRole;
import cbit.vcell.model.ModelQuantity;
import cbit.vcell.model.ProxyParameter;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.model.Structure.StructureSize;
import cbit.vcell.parser.DivideByZeroException;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.parser.Token;
import cbit.vcell.resource.ResourceUtil;
import cbit.vcell.solver.AnnotatedFunction;
import cbit.vcell.solver.ConstantArraySpec;
import cbit.vcell.solver.MathOverrides;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SolverDescription;
import cbit.vcell.solver.SolverTaskDescription;
import cbit.vcell.solver.TimeBounds;
import cbit.vcell.xml.XMLSource;
import cbit.vcell.xml.XmlHelper;
import cbit.vcell.xml.XmlParseException;

public class SEDMLExporter {
	private int sedmlLevel = 1;
	private int sedmlVersion = 1;
	private  SedML sedmlModel = null;
	private cbit.vcell.biomodel.BioModel vcBioModel = null;

	private static String DATAGENERATOR_TIME_NAME = "time";
	private static String DATAGENERATOR_TIME_SYMBOL = "t";
	
	public SEDMLExporter(BioModel argBiomodel, int argLevel, int argVersion) {
		super();
		this.vcBioModel = argBiomodel;
		this.sedmlLevel = argLevel;
		this.sedmlVersion = argVersion;
	}
	
	public static void main(String[] args) {
//		if (args.length != 1) {
//			System.out.println("Usage:\n\t path_of_vcml_file\n" );
//	        System.exit(1);
//		} 
//		String pathName = args[0];
		String pathName = "c:\\dan\\SEDML\\SEDML2.vcml";
		try {
			String biomodelXmlStr = XmlUtil.getXMLString(pathName);
			BioModel bioModel = XmlHelper.XMLToBioModel(new XMLSource(biomodelXmlStr.toString()));
			bioModel.refreshDependencies();
			
			// invoke the SEDMLEXporter
			SEDMLExporter sedmlExporter = new SEDMLExporter(bioModel, 1, 1);
			String sedmlStr = sedmlExporter.getSEDMLFile();
			String absolutePath = "c:\\dan\\SEDML";
//			String absolutePath = ResourceUtil.getUserHomeDir().getAbsolutePath();
			String outputName = absolutePath+ "\\" + TokenMangler.mangleToSName(bioModel.getName()) + ".sedml";
			XmlUtil.writeXMLStringToFile(sedmlStr, outputName, true);
		} catch (IOException e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Unable to read VCML file '" + pathName + "' into string.");
		} catch (XmlParseException e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Unable to convert VCML file '" + pathName + "' to a biomodel.");
		}
	}
	
	public String getSEDMLFile() {

		// Create an SEDMLDocument and create the SEDMLModel from the document, so that other details can be added to it in translateBioModel()
		SEDMLDocument sedmlDocument = new SEDMLDocument();

		sedmlModel = sedmlDocument.getSedMLModel();

		translateBioModelToSedML();

		// write SEDML document into SEDML writer, so that the SEDML str can be retrieved
		return sedmlDocument.writeDocumentToString();
	}


	private void translateBioModelToSedML() {
		// models
		try {
			SimulationContext[] simContexts = vcBioModel.getSimulationContexts();
			cbit.vcell.model.Model vcModel = vcBioModel.getModel();
			String sbmlLanguageURN = SUPPORTED_LANGUAGE.SBML_GENERIC.getURN();	// "urn:sedml:language:sbml";
			String bioModelName = TokenMangler.mangleToSName(vcBioModel.getName());
			String usrHomeDirPath = ResourceUtil.getUserHomeDir().getAbsolutePath();
			SBMLSupport sbmlSupport = new SBMLSupport();		// to get Xpath string for variables.
			int simContextCnt = 0;	// for model count, task subcount
			int varCount = 0;		// for dtaGenerator count.
			boolean bSpeciesAddedAsDataGens = false;
			String sedmlNotesStr = "";

			for (SimulationContext simContext : simContexts) {
				String simContextName = simContext.getName();
				// export all applications that are not spatial stochastic
				if (!(simContext.getGeometry().getDimension() > 0 && simContext.isStoch())) {
					// check if structure sizes are set. If not, get a structure from the model, and set its size 
					// (thro' the structureMappings in the geometry of the simContext); invoke the structureSizeEvaluator 
					// to compute and set the sizes of the remaining structures.
					if (!simContext.getGeometryContext().isAllSizeSpecifiedPositive()) {
						Structure structure = simContext.getModel().getStructure(0);
						double structureSize = 1.0;
						StructureMapping structMapping = simContext.getGeometryContext().getStructureMapping(structure); 
						StructureSizeSolver.updateAbsoluteStructureSizes(simContext, structure, structureSize, structMapping.getSizeParameter().getUnitDefinition());
					}
					
					// Export the application itself to SBML, with default overrides
					String sbmlString = null;
					if (simContext.getGeometry().getDimension() > 0) {
						sbmlString = XmlHelper.exportSBML(vcBioModel, 2, 4, 0, true, simContext, null);
					} else {
						sbmlString = XmlHelper.exportSBML(vcBioModel, 2, 4, 0, false, simContext, null);
					}
					String sbmlFilePathStr = usrHomeDirPath+ "\\" + bioModelName + "_" + simContextName + ".xml";
					XmlUtil.writeXMLStringToFile(sbmlString, sbmlFilePathStr, true);
			        String simContextId = TokenMangler.mangleToSName(simContextName);
					sedmlModel.addModel(new Model(simContextId, simContextName, sbmlLanguageURN, sbmlFilePathStr));
		
					// required for mathOverrides, if any
					MathMapping mathMapping = simContext.createNewMathMapping();
					MathSymbolMapping mathSymbolMapping = mathMapping.getMathSymbolMapping();

					// create sedml simulation objects and tasks (mapping each sim with current simContext) 
			        int simCount = 0;
			        String taskRef = null;
					int overrideCount = 0;
			        for (Simulation vcSimulation : simContext.getSimulations()) {
						List<DataGenerator> dataGeneratorsOfSim = new ArrayList<DataGenerator> ();
						// if simContext is non-spatial stochastic, check if sim is histogram
			        	SolverTaskDescription simTaskDesc = vcSimulation.getSolverTaskDescription();
						if (simContext.getGeometry().getDimension() == 0 && simContext.isStoch()) {
							long numOfTrials = simTaskDesc.getStochOpt().getNumOfTrials();
							if (numOfTrials > 1) {
								String msg = "\n\t" + simContextName + " : non-spatial stochastic application with histogram option, ";
								sedmlNotesStr += msg;
								continue;
							}
						}
						// create Algorithm and sedmlSimulation (UniformtimeCourse)
						SolverDescription vcSolverDesc = simTaskDesc.getSolverDescription();
//						String kiSAOIdStr = getKiSAOIdFromSimulation(vcSolverDesc);
						String kiSAOIdStr = vcSolverDesc.getKisao();
						Algorithm sedmlAlgorithm = new Algorithm(kiSAOIdStr);
						TimeBounds vcSimTimeBounds = simTaskDesc.getTimeBounds();
						double startingTime = vcSimTimeBounds.getStartingTime();
						String simName = vcSimulation.getName();
						UniformTimeCourse utcSim = new UniformTimeCourse(TokenMangler.mangleToSName(simName), simName, startingTime, startingTime, 
								vcSimTimeBounds.getEndingTime(), (int) simTaskDesc.getExpectedNumTimePoints(), sedmlAlgorithm);
						
						// if solver is not CVODE, add a note to utcSim to indicate actual solver name
						if (!vcSolverDesc.equals(SolverDescription.CVODE)) {
							String simNotesStr = "Actual Solver Name : '" + vcSolverDesc.getDisplayLabel() + "'.";  
							utcSim.addNote(createNotesElement(simNotesStr));
						}
						sedmlModel.addSimulation(utcSim);
		
						// add SEDML tasks (map simulation to model:simContext)
						// repeated tasks
						MathOverrides mathOverrides = vcSimulation.getMathOverrides();
						if(mathOverrides != null && mathOverrides.hasOverrides()) {
							String[] overridenConstantNames = mathOverrides.getOverridenConstantNames();
							String[] scannedConstantsNames = mathOverrides.getScannedConstantNames();
							HashMap<String, String> scannedParamHash = new HashMap<String, String>();
							HashMap<String, String> unscannedParamHash = new HashMap<String, String>();
							for (String name : scannedConstantsNames) {
								scannedParamHash.put(name, name);
							}
							for (String name : overridenConstantNames) {
								if (!scannedParamHash.containsKey(name)) {
									unscannedParamHash.put(name, name);
								}
							}
							
							if (!unscannedParamHash.isEmpty() && scannedParamHash.isEmpty()) {
								// only parameters with simple overrides (numeric/expression) no scans
								// create new model with change for each parameter that has override; add simple task
								String overriddenSimContextId = simContextId + "_" + overrideCount;
								String overriddenSimContextName = simContextName + " modified";
								Model sedModel = new Model(overriddenSimContextId, overriddenSimContextName, sbmlLanguageURN, simContextId);
								overrideCount++;

								for (String unscannedParamName : unscannedParamHash.values()) {
									SymbolTableEntry ste = getSymbolTableEntryForModelEntity(mathSymbolMapping, unscannedParamName);
									Expression unscannedParamExpr = mathOverrides.getActualExpression(unscannedParamName, 0);
									if(unscannedParamExpr.isNumeric()) {
										// if expression is numeric, add ChangeAttribute to model created above
										XPathTarget targetXpath = getTargetAttributeXPath(ste);
										ChangeAttribute changeAttribute = new ChangeAttribute(targetXpath, unscannedParamExpr.infix());
										sedModel.addChange(changeAttribute);
									} else {
										// non-numeric expression : add 'computeChange' to modified model
										ASTNode math = Libsedml.parseFormulaString(unscannedParamExpr.infix());
										XPathTarget targetXpath = getTargetXPath(ste);
										ComputeChange computeChange = new ComputeChange(targetXpath, math);
										String[] exprSymbols = unscannedParamExpr.getSymbols();
										for (String symbol : exprSymbols) {
											String symbolName = TokenMangler.mangleToSName(symbol);
											SymbolTableEntry ste1 = vcModel.getEntry(symbol);
											if (ste != null) {
												if (ste1 instanceof SpeciesContext || ste1 instanceof Structure || ste1 instanceof ModelParameter) {
													XPathTarget ste1_XPath = getTargetXPath(ste1);
													org.jlibsedml.Variable sedmlVar = new org.jlibsedml.Variable(symbolName, symbolName, taskRef, ste1_XPath.getTargetAsString());
													computeChange.addVariable(sedmlVar);
												} else {
													double doubleValue = 0.0;
													if (ste1 instanceof ReservedSymbol) {
														doubleValue = getReservedSymbolValue(ste1); 
													}
													Parameter sedmlParameter = new Parameter(symbolName, symbolName, doubleValue);
													computeChange.addParameter(sedmlParameter);
												}
											} else {
												throw new RuntimeException("Sybol '" + symbol + "' used in expression for '" + unscannedParamName + "' not found in model.");
											}
										}
										sedModel.addChange(computeChange);
									}
								}
								sedmlModel.addModel(sedModel);

								String taskId = "task_" + simContextCnt + "_" + simCount;
								Task sedmlTask = new Task(taskId, taskId, sedModel.getId(), utcSim.getId());
								sedmlModel.addTask(sedmlTask);
								taskRef = taskId;		// to be used later to add dataGenerators : one set of DGs per model (simContext).
							} else if (!scannedParamHash.isEmpty() && unscannedParamHash.isEmpty()) {
								// only parameters with scans : only add 1 RepeatedTask
								String repeatedTaskId = "repeatedTask_" + simContextCnt + "_" + simCount;
								RepeatedTask t = new RepeatedTask(repeatedTaskId, repeatedTaskId, false, "blank");
								taskRef = repeatedTaskId;
								
								for (String scannedConstName : scannedConstantsNames) {
									ConstantArraySpec constantArraySpec = mathOverrides.getConstantArraySpec(scannedConstName);
									String rangeId = "range_" + simContextCnt + "_" + simCount + "_" + scannedConstName;
						
									// list of Ranges, if sim is parameter scan.
									if(constantArraySpec != null) {
										Range r = null;
										System.out.println("     " + constantArraySpec.toString());
										if(constantArraySpec.getType() == ConstantArraySpec.TYPE_INTERVAL) {
											// ------ Uniform Range
											r = new UniformRange(rangeId, constantArraySpec.getMinValue(), 
													constantArraySpec.getMaxValue(), constantArraySpec.getNumValues());
											t.addRange(r);
										} else {
											// ----- Vector Range
											cbit.vcell.math.Constant[] cs = constantArraySpec.getConstants();
											ArrayList<Double> values = new ArrayList<Double>();
											for (int i = 0; i < cs.length; i++){
												String value = cs[i].getExpression().infix();
												values.add(Double.parseDouble(value));
											}
											r = new VectorRange(rangeId, values);
											t.addRange(r);
										}
										
										SymbolTableEntry ste = getSymbolTableEntryForModelEntity(mathSymbolMapping, scannedConstName);
										XPathTarget target = getTargetXPath(ste);
										ASTNode math1 = new ASTCi(scannedConstName);
										SetValue setValue = new SetValue(target, r.getId(), simContextId);
										setValue.setMath(math1);
										t.addChange(setValue);
									} else {
										throw new RuntimeException("No scan ranges found for scanned parameter : '" + scannedConstName + "'.");
									}
								}
								sedmlModel.addTask(t);
							} else {

								// both scanned and simple parameters : create new model with change for each simple override; add RepeatedTask
								
								// create new model with change for each unscanned parameter that has override
								String overriddenSimContextId = simContextId + "_" + overrideCount;
								String overriddenSimContextName = simContextName + " modified";
								Model sedModel = new Model(overriddenSimContextId, overriddenSimContextName, sbmlLanguageURN, simContextId);
								overrideCount++;

								// scanned parameters
								String repeatedTaskId = "repeatedTask_" + simContextCnt + "_" + simCount;
								RepeatedTask t = new RepeatedTask(repeatedTaskId, repeatedTaskId, false, "blank");
								taskRef = repeatedTaskId;
								for (String scannedConstName : scannedConstantsNames) {
									ConstantArraySpec constantArraySpec = mathOverrides.getConstantArraySpec(scannedConstName);
									String rangeId = "range_" + simContextCnt + "_" + simCount + "_" + scannedConstName;
						
									// list of Ranges, if sim is parameter scan.
									if(constantArraySpec != null) {
										Range r = null;
										System.out.println("     " + constantArraySpec.toString());
										if(constantArraySpec.getType() == ConstantArraySpec.TYPE_INTERVAL) {
											// ------ Uniform Range
											r = new UniformRange(rangeId, constantArraySpec.getMinValue(), 
													constantArraySpec.getMaxValue(), constantArraySpec.getNumValues());
											t.addRange(r);
										} else {
											// ----- Vector Range
											cbit.vcell.math.Constant[] cs = constantArraySpec.getConstants();
											ArrayList<Double> values = new ArrayList<Double>();
											for (int i = 0; i < cs.length; i++){
												String value = cs[i].getExpression().infix() + ", ";
												values.add(Double.parseDouble(value));
											}
											r = new VectorRange(rangeId, values);
											t.addRange(r);
										}
										
										// use scannedParamHash to store rangeId for that param, since it might be needed if unscanned param has a scanned param in expr.
										if (scannedParamHash.get(scannedConstName).equals(scannedConstName)) {
											// the hash was originally populated as <scannedParamName, scannedParamName>. Replace 'value' with rangeId for scannedParam
											scannedParamHash.put(scannedConstName, r.getId());
										}
										
										// create setValue for scannedConstName
										SymbolTableEntry ste2 = getSymbolTableEntryForModelEntity(mathSymbolMapping, scannedConstName);
										XPathTarget target1 = getTargetXPath(ste2);
										ASTNode math1 = new ASTCi(scannedConstName);
										SetValue setValue1 = new SetValue(target1, r.getId(), sedModel.getId());
										setValue1.setMath(math1);
										t.addChange(setValue1);
									} else {
										throw new RuntimeException("No scan ranges found for scanned parameter : '" + scannedConstName + "'.");
									}
								}

								// for unscanned parameter overrides
/*
    TODO: WE SHOULDN'T HAVE A RANGE REFERENCE FIELD FOR SetValue AT ALL 
    <setValue target="/sbml/model/listOfParameters/parameter[@id='w']" range="current" modelReference="model1" >
    for CCC = AAA + BBB we should have:
    
    <functionalRange id="AAA_range" index="index">        // range for variable AAA with id=AAA_range
        <listOfVariables>
            <variable id="AAA" name="AAA" target="/sbml/model/listOfParameters/parameter[@id='AAA']" />
        </listOfVariables>
        <function>
            <math>
                <ci> AAA </ci>
            </math>
        </function>
    </functionalRange>
    
    <functionalRange id="BBB_range" index="index">        // // range for variable BBB with id=BBB_range
        .....
    </functionalRange>
    
    <setValue target="/sbml/model/listOfParameters/parameter[@id='CCC']" modelReference="model1" >
        <math>
            <apply>
              <plus />
              <ci> AAA_range </ci>        // <<<<<< use the names of the range for each variable !!!!
              <ci> BBB_range </ci>
            </apply>        
        </math>
    </setValue>
</listOfChanges>
*/
								for (String unscannedParamName : unscannedParamHash.values()) {
									SymbolTableEntry ste = getSymbolTableEntryForModelEntity(mathSymbolMapping, unscannedParamName);
									Expression unscannedParamExpr = mathOverrides.getActualExpression(unscannedParamName, 0);
									if(unscannedParamExpr.isNumeric()) {
										// if expression is numeric, add ChangeAttribute to model created above
										XPathTarget targetXpath = getTargetAttributeXPath(ste);
										ChangeAttribute changeAttribute = new ChangeAttribute(targetXpath, unscannedParamExpr.infix());
										sedModel.addChange(changeAttribute);
									} else {
										// check for any scanned parameter in unscanned parameter expression
										ASTNode math = Libsedml.parseFormulaString(unscannedParamExpr.infix());
										String[] exprSymbols = unscannedParamExpr.getSymbols();
										boolean bHasScannedParameter = false;
										String scannedParamNameInUnscannedParamExp = null;
										for (String symbol : exprSymbols) {
											if (scannedParamHash.get(symbol) != null) {
												bHasScannedParameter = true;
												scannedParamNameInUnscannedParamExp = new String(symbol);
												break;		// @TODO check for multiple scannedParameters in expression. 
											}
										}
										// (scanned parameter in expr) ? (add setValue for unscanned param in repeatedTask) : (add computeChange to modifiedModel)
										if (bHasScannedParameter && scannedParamNameInUnscannedParamExp != null) {
											// create setValue for unscannedParamName (which contains a scanned param in its expression)
											SymbolTableEntry entry = getSymbolTableEntryForModelEntity(mathSymbolMapping, unscannedParamName);
											XPathTarget target = getTargetXPath(entry);
											String rangeId = scannedParamHash.get(scannedParamNameInUnscannedParamExp);
											SetValue setValue = new SetValue(target, rangeId, sedModel.getId());	// @TODO: we have no range??
											setValue.setMath(math);
											t.addChange(setValue);
										} else {
											// non-numeric expression : add 'computeChange' to modified model
											XPathTarget targetXpath = getTargetXPath(ste);
											ComputeChange computeChange = new ComputeChange(targetXpath, math);
											for (String symbol : exprSymbols) {
												String symbolName = TokenMangler.mangleToSName(symbol);
												SymbolTableEntry ste1 = vcModel.getEntry(symbol);
												// ste1 could be a math parameter, hence the above could return null
												if (ste1 == null) {
													ste1 = simContext.getMathDescription().getEntry(symbol);
												}
												if (ste1 != null) {
													if (ste1 instanceof SpeciesContext || ste1 instanceof Structure || ste1 instanceof ModelParameter) {
														XPathTarget ste1_XPath = getTargetXPath(ste1);
														org.jlibsedml.Variable sedmlVar = new org.jlibsedml.Variable(symbolName, symbolName, taskRef, ste1_XPath.getTargetAsString());
														computeChange.addVariable(sedmlVar);
													} else {
														double doubleValue = 0.0;
														if (ste1 instanceof ReservedSymbol) {
															doubleValue = getReservedSymbolValue(ste1); 
														} else if (ste instanceof Function) {
															try {
																doubleValue = ste.getExpression().evaluateConstant();
															} catch (Exception e) {
																e.printStackTrace(System.out);
																throw new RuntimeException("Unable to evaluate function '" + ste.getName() + "' used in '" + unscannedParamName + "' expression : ", e);
															}
														} else {
															doubleValue = ste.getConstantValue();
														}
														// TODO: shouldn't be s1_init_uM which is a math symbol, should be s0 (so use the ste-something from above)
														// TODO: revert to Variable, not Parameter
														Parameter sedmlParameter = new Parameter(symbolName, symbolName, doubleValue);
														computeChange.addParameter(sedmlParameter);
													}
												} else {
													throw new RuntimeException("Symbol '" + symbol + "' used in expression for '" + unscannedParamName + "' not found in model.");
												}
											}
											sedModel.addChange(computeChange);
										}
									}
								}
								sedmlModel.addModel(sedModel);
								sedmlModel.addTask(t);
							}
						} else {						// no math overrides, add basic task.
							String taskId = "task_" + simContextCnt + "_" + simCount;
							Task sedmlTask = new Task(taskId, taskId, simContextId, utcSim.getId());
							sedmlModel.addTask(sedmlTask);
							taskRef = taskId;		// to be used later to add dataGenerators : one set of DGs per model (simContext).
						}

						// add one dataGenerator for 'time' for entire SEDML model.
						// (using the id of the first task in model for 'taskRef' field of var since 
						DataGenerator timeDataGen = sedmlModel.getDataGeneratorWithId(DATAGENERATOR_TIME_NAME);
		        		if (timeDataGen == null) {
							org.jlibsedml.Variable timeVar = new org.jlibsedml.Variable(DATAGENERATOR_TIME_SYMBOL, DATAGENERATOR_TIME_SYMBOL, sedmlModel.getTasks().get(0).getId(), VariableSymbol.TIME);
							ASTNode math = Libsedml.parseFormulaString(DATAGENERATOR_TIME_SYMBOL);
							timeDataGen = new DataGenerator(DATAGENERATOR_TIME_NAME, DATAGENERATOR_TIME_NAME, math);
							timeDataGen.addVariable(timeVar);
							sedmlModel.addDataGenerator(timeDataGen);
							dataGeneratorsOfSim.add(timeDataGen);
		        		}
						
						// add dataGenerators for species
						// get species list from SBML model.
						String dataGenIdPrefix = "dataGen_" + taskRef;
						String[] varNamesList = SimSpec.fromSBML(sbmlString).getVarsList();
						for (String varName : varNamesList) {
							org.jlibsedml.Variable sedmlVar = new org.jlibsedml.Variable(varName, varName, taskRef, sbmlSupport.getXPathForSpecies(varName));
							ASTNode varMath = Libsedml.parseFormulaString(varName);
							String dataGenId = dataGenIdPrefix + "_" + TokenMangler.mangleToSName(varName);			//"dataGen_" + varCount; - old code
							DataGenerator dataGen = new DataGenerator(dataGenId, dataGenId, varMath);
							dataGen.addVariable(sedmlVar);
							sedmlModel.addDataGenerator(dataGen);
							dataGeneratorsOfSim.add(dataGen);
							varCount++;
						}
						
						// add DataGenerators for output functions here
						ArrayList<AnnotatedFunction> outputFunctions = simContext.getOutputFunctionContext().getOutputFunctionsList();
						for (AnnotatedFunction annotatedFunction : outputFunctions) {
							Expression functionExpr = annotatedFunction.getExpression();
							ASTNode funcMath = Libsedml.parseFormulaString(functionExpr.infix());
							String dataGenId = dataGenIdPrefix + "_" + TokenMangler.mangleToSName(annotatedFunction.getName());		//"dataGen_" + varCount; - old code
							DataGenerator dataGen = new DataGenerator(dataGenId, dataGenId, funcMath);
							String[] functionSymbols = functionExpr.getSymbols();
							for (String symbol : functionSymbols) {
								String symbolName = TokenMangler.mangleToSName(symbol);
								// try to get symbol from model, if null, try simContext.mathDesc
								SymbolTableEntry ste = vcModel.getEntry(symbol);
								if (ste == null) {
									ste = simContext.getMathDescription().getEntry(symbol);
								}
								if (ste instanceof SpeciesContext || ste instanceof Structure || ste instanceof ModelParameter) {
									XPathTarget targetXPath = getTargetXPath(ste);
									org.jlibsedml.Variable sedmlVar = new org.jlibsedml.Variable(symbolName, symbolName, taskRef, targetXPath.getTargetAsString());
									dataGen.addVariable(sedmlVar);
								} else {
									double value = 0.0;
									if (ste instanceof Function) {
										try {
											value = ste.getExpression().evaluateConstant();
										} catch (Exception e) {
											e.printStackTrace(System.out);
											throw new RuntimeException("Unable to evaluate function '" + ste.getName() + "' for output function '" + annotatedFunction.getName() + "'.", e);
										}
									} else {
										value = ste.getConstantValue();
									}
									Parameter sedmlParameter = new Parameter(symbolName, symbolName, value);
									dataGen.addParameter(sedmlParameter);
								}
							}
							sedmlModel.addDataGenerator(dataGen);
							dataGeneratorsOfSim.add(dataGen);
							varCount++;
						}
						simCount++;
						// add output to sedml Model : 1 plot2d for each non-spatial simulation with all vars (species/output functions) vs time (1 curve per var)
						// ignoring output for spatial deterministic (spatial stochastic is not exported to SEDML) and non-spatial stochastic applications with histogram
						if (!(simContext.getGeometry().getDimension() > 0)) {
							// ignore Output (Plot2d)  for non-spatial stochastic simulation with histogram. 
							boolean bSimHasHistogram = false;
							if (simContext.isStoch()) {
								long numOfTrials = simTaskDesc.getStochOpt().getNumOfTrials();
								if (numOfTrials > 1) {	// not histogram {
									bSimHasHistogram = true;
								}
							}
							
							if (!bSimHasHistogram) {
								String plot2dId = "plot2d_" + TokenMangler.mangleToSName(vcSimulation.getName());
								Plot2D sedmlPlot2d = new Plot2D(plot2dId, simContext.getName() + "plots");
								sedmlPlot2d.addNote(createNotesElement("Plot of all variables and output functions from application '" + simContext.getName() + "' ; simulation '" + vcSimulation.getName() + "' in VCell model"));
								List<DataGenerator> dataGenerators = sedmlModel.getDataGenerators();
								String xDataRef = sedmlModel.getDataGeneratorWithId(DATAGENERATOR_TIME_NAME).getId();
								// add a curve for each dataGenerator in SEDML model
								int curveCnt = 0;
								for (DataGenerator dataGenerator : dataGeneratorsOfSim) {
									// no curve for time, since time is xDateReference
									if (dataGenerator.getId().equals(xDataRef)) {
										continue;
									}
									String curveId = "curve_" + curveCnt++; 
									Curve curve = new Curve(curveId, curveId, false, false, xDataRef, dataGenerator.getId());
									sedmlPlot2d.addCurve(curve);
								}
								sedmlModel.addOutput(sedmlPlot2d);
							}
						}
					} // end - for 'sims'
				} else {
					String msg = "\n\t" + simContextName + " : spatial stochastic application, ";
					sedmlNotesStr += msg;
				} // end : if-else simContext is not spatial stochastic
				simContextCnt++;
			}	// end - for 'simContexts'
			
        	
        	
        	// if sedmlNotesStr is not null, there were some applications that could not be exported to SEDML (eg., spatial stochastic). Create a notes element and add it to sedml Model.
        	if (sedmlNotesStr.length() > 0) {
	        	sedmlNotesStr = "\n\tThe following applications in the VCell model were not exported to VCell : " + sedmlNotesStr + "\n\nExport of spatial stochsatic (Smoldyn solver) applications to SEDML not supported at this time.";
	        	sedmlModel.addNote(createNotesElement(sedmlNotesStr));
        	}
			
			// error check : if there are no non-spatial deterministic applications (=> no models in SEDML document), complain.
			if (sedmlModel.getModels().isEmpty()) {
				throw new RuntimeException("No applications in biomodel to export to Sedml.");
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("Error adding model to SEDML document : " + e.getMessage());
		}
	}

	private double getReservedSymbolValue(SymbolTableEntry ste) {
		cbit.vcell.model.Model vcModel = vcBioModel.getModel();
		if (ste instanceof ReservedSymbol) {
			try {
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.TEMPERATURE) == 0) {
					return vcBioModel.getSimulationContext(0).getTemperatureKelvin();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.PI_CONSTANT) == 0) {
					return vcModel.getPI_CONSTANT().getExpression().evaluateConstant();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.FARADAY_CONSTANT) == 0) {
					return vcModel.getFARADAY_CONSTANT().getExpression().evaluateConstant();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.FARADAY_CONSTANT_NMOLE) == 0) {
					return vcModel.getFARADAY_CONSTANT_NMOLE().getExpression().evaluateConstant();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.N_PMOLE) == 0) {
					return vcModel.getN_PMOLE().getExpression().evaluateConstant();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.GAS_CONSTANT) == 0) {
					return vcModel.getGAS_CONSTANT().getExpression().evaluateConstant();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.K_GHK) == 0) {
					return vcModel.getK_GHK().getExpression().evaluateConstant();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.KMILLIVOLTS) == 0) {
					return vcModel.getKMILLIVOLTS().getExpression().evaluateConstant();
				}
				if (((ReservedSymbol) ste).getRole().compareTo(ReservedSymbolRole.KMOLE) == 0) {
					return vcModel.getKMOLE().getExpression().evaluateConstant();
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
				throw new RuntimeException("Unable to get the value of (reserved) VCell parameter : '" + ste.getName() + "' : ", e);
			} 
		} 
		return 0;
	}

	private Notes createNotesElement(String notesStr) {
		// create some xhtml. E.g.,
		Element para = new Element("p");
		para.setText(notesStr);
		// create a notes element
		Notes note = new Notes(para);
		return note;
	}

	private SymbolTableEntry getSymbolTableEntryForModelEntity(MathSymbolMapping mathSymbolMapping, String paramName) {
		cbit.vcell.math.Variable mathVar = mathSymbolMapping.findVariableByName(paramName);
		SymbolTableEntry[] stEntries = mathSymbolMapping.getBiologicalSymbol(mathVar);
		if (stEntries == null || stEntries.length == 0) {
			throw new NullPointerException("No matching biological symbol for : " + paramName);
		}
		
		// if the extra stes in the array are KineticsProxyParameters/ModelQuantities, remove them from array. Should be left with only one entry for overriddenConstantName
		if (stEntries.length > 1) {
			//
			// If there are more than one stEntries, usually, it is a regular ste (species, global parameter, structure, etc) together with 
			// kineticsProxyParameters (that have the regular ste as target) or Model quantities (structure size, membrane voltage). 
			// So filtering out the kinticProxyParametes should leave only the regular parameter, 
			// which is what we want. If there are more, then there is a problem.
			//
			ArrayList<SymbolTableEntry> steList = new ArrayList<SymbolTableEntry>(Arrays.asList(stEntries));
			for (int i = 0; i < stEntries.length; i++) {
				if (stEntries[i] instanceof ProxyParameter) {
					SymbolTableEntry ppTargetSte = ((ProxyParameter)stEntries[i]).getTarget();
					if (steList.contains(ppTargetSte) || ppTargetSte instanceof ModelQuantity) {
						steList.remove(stEntries[i]);
					}
				}
				if (stEntries[i] instanceof ModelQuantity) {
					if (steList.contains(stEntries[i])) {
						steList.remove(stEntries[i]);
					}
				}
			}
			// after removing proxy parameters, cannot have more than one ste in list
			if (steList.size() == 0) {
				throw new RuntimeException("No mapping entry for constant : '" + paramName + "'.");
			}
			if (steList.size() > 1) {
				throw new RuntimeException("Cannot have more than one mapping entry for constant : '" + paramName + "'.");
			}
			SymbolTableEntry[] stes = (SymbolTableEntry[])steList.toArray(new SymbolTableEntry[0]);
			return stes[0];
		} else {
			return stEntries[0];
		}
	}

	private XPathTarget getTargetXPath(SymbolTableEntry ste) {
		SBMLSupport sbmlSupport = new SBMLSupport();		// to get Xpath string for variables.
		XPathTarget targetXpath = null;
		if (ste instanceof SpeciesContext || ste instanceof SpeciesContextSpecParameter) {
			String name = ste.getName();
			if (ste instanceof SpeciesContextSpecParameter) {
				name = ((SpeciesContextSpecParameter)ste).getSpeciesContext().getName();
			}
			targetXpath = new XPathTarget(sbmlSupport.getXPathForSpecies(name));
		} else if (ste instanceof ModelParameter) {
			targetXpath = new XPathTarget(sbmlSupport.getXPathForGlobalParameter(ste.getName()));
		}  else if (ste instanceof Structure || ste instanceof Structure.StructureSize || (ste instanceof StructureMappingParameter && ((StructureMappingParameter)ste).getRole() == StructureMapping.ROLE_Size)) {
			String compartmentId = ste.getName();
			// can change compartment size or spatial dimension, but in vcell, we cannot change compartment dimension. 
			String compartmentAttr = "";
			if (ste instanceof Structure.StructureSize) { 
				compartmentId = ((StructureSize)ste).getStructure().getName();
				compartmentAttr = ((StructureSize)ste).getName();
			}
			if (ste instanceof StructureMappingParameter) {
				StructureMappingParameter smp = (StructureMappingParameter)ste;
				compartmentId = smp.getStructure().getName();
				if (smp.getRole() == StructureMapping.ROLE_Size) {
					compartmentAttr = smp.getName();
				}
			}
			if (compartmentAttr.length() < 1) {
				targetXpath = new XPathTarget(sbmlSupport.getXPathForCompartment(compartmentId));
			} else if (compartmentAttr.equalsIgnoreCase("size")) {
				targetXpath = new XPathTarget(sbmlSupport.getXPathForCompartment(compartmentId, CompartmentAttribute.size));
			} else {
				throw new RuntimeException("Unknown compartment attribute '" + compartmentAttr + "'; cannot get xpath target for compartment '" + compartmentId + "'.");
			}
		} else if (ste instanceof KineticsParameter) {
			KineticsParameter kp = (KineticsParameter)ste;
			String reactionID = kp.getKinetics().getReactionStep().getName();
			String parameterID = kp.getName();
			targetXpath = new XPathTarget(sbmlSupport.getXPathForKineticLawParameter(reactionID, parameterID));
		} else {
			System.err.println("Entity should be SpeciesContext, Structure, ModelParameter : " + ste.getClass());
			throw new RuntimeException("Unknown entity in SBML model");
		}
		return targetXpath;
	}
	
	private XPathTarget getTargetAttributeXPath(SymbolTableEntry ste) {
		SBMLSupport sbmlSupport = new SBMLSupport();		// to get Xpath string for variables.
		XPathTarget targetXpath = null;
		if (ste instanceof SpeciesContext || ste instanceof SpeciesContextSpecParameter) {
			String speciesId = ste.getName();
			// can change species initial concentration or amount 
			String speciesAttr = "";
			if (ste instanceof SpeciesContextSpecParameter) {
				SpeciesContextSpecParameter scsp = (SpeciesContextSpecParameter)ste;
				speciesId = (scsp).getSpeciesContext().getName();
				if (scsp.getRole() == SpeciesContextSpec.ROLE_InitialConcentration) {
					speciesAttr = scsp.getName(); 
				}
				if (scsp.getRole() == SpeciesContextSpec.ROLE_InitialCount) {
					speciesAttr = scsp.getName(); 
				}
			}
			if (speciesAttr.length() < 1) {
				targetXpath = new XPathTarget(sbmlSupport.getXPathForCompartment(speciesId));
			} else if (speciesAttr.equalsIgnoreCase("initialConcentration") || speciesAttr.equalsIgnoreCase("initConc")) {
				targetXpath = new XPathTarget(sbmlSupport.getXPathForSpecies(speciesId, SpeciesAttribute.initialConcentration));
			} else if (speciesAttr.equalsIgnoreCase("initialCount")) {
				targetXpath = new XPathTarget(sbmlSupport.getXPathForSpecies(speciesId, SpeciesAttribute.initialAmount));
			} else {
				throw new RuntimeException("Unknown species attribute '" + speciesAttr + "'; cannot get xpath target for species '" + speciesId + "'.");
			}

			targetXpath = new XPathTarget(sbmlSupport.getXPathForSpecies(speciesId));
		} else if (ste instanceof ModelParameter) {
			// can only change parameter value. 
			targetXpath = new XPathTarget(sbmlSupport.getXPathForGlobalParameter(ste.getName(), ParameterAttribute.value));
		}  else if (ste instanceof Structure || ste instanceof Structure.StructureSize || (ste instanceof StructureMappingParameter && ((StructureMappingParameter)ste).getRole() == StructureMapping.ROLE_Size)) {
			String compartmentId = ste.getName();
			// can change compartment size or spatial dimension, but in vcell, we cannot change compartment dimension. 
			String compartmentAttr = "";
			if (ste instanceof Structure.StructureSize) { 
				compartmentId = ((StructureSize)ste).getStructure().getName();
				compartmentAttr = ((StructureSize)ste).getName();
			}
			if (ste instanceof StructureMappingParameter) {
				StructureMappingParameter smp = (StructureMappingParameter)ste;
				compartmentId = smp.getStructure().getName();
				if (smp.getRole() == StructureMapping.ROLE_Size) {
					compartmentAttr = smp.getName();
				}
			}
			if (compartmentAttr.length() < 1) {
				targetXpath = new XPathTarget(sbmlSupport.getXPathForCompartment(compartmentId));
			} else if (compartmentAttr.equalsIgnoreCase("size")) {
				targetXpath = new XPathTarget(sbmlSupport.getXPathForCompartment(compartmentId, CompartmentAttribute.size));
			} else {
				throw new RuntimeException("Unknown compartment attribute '" + compartmentAttr + "'; cannot get xpath target for compartment '" + compartmentId + "'.");
			}
		} else if (ste instanceof KineticsParameter) {
			KineticsParameter kp = (KineticsParameter)ste;
			String reactionID = kp.getKinetics().getReactionStep().getName();
			String parameterID = kp.getName();
			targetXpath = new XPathTarget(sbmlSupport.getXPathForKineticLawParameter(reactionID, parameterID, ParameterAttribute.value));
		} else {
			System.err.println("Entity should be SpeciesContext, Structure, ModelParameter : " + ste.getClass());
			throw new RuntimeException("Unknown entity in SBML model");
		}
		return targetXpath;
	}
	
	private String getKiSAOIdFromSimulation(SolverDescription solverDesc) {
        String kiSAOId = null;
		try {
			// Create KiSAOQueryMaker instance, which uses last version of kisao.owl ontology
			// (URL: http://biomodels.net/kisao/KISAO).
			net.biomodels.kisao.IKiSAOQueryMaker kisaoQuery = new net.biomodels.kisao.impl.KiSAOQueryMaker();
			String solverName = solverDesc.getShortDisplayLabel();
			String cvodeSolverName = SolverDescription.CVODE.getShortDisplayLabel();
			if (!solverName.equalsIgnoreCase(cvodeSolverName)) {
				Set<IRI> iriSet = kisaoQuery.searchByName(solverName);
				while (kiSAOId == null && iriSet != null && iriSet.iterator().hasNext()) {
		        	IRI iri = iriSet.iterator().next();
					kiSAOId = kisaoQuery.getId(iri);
		        	String kiSAOName = kisaoQuery.getName(iri);
		        	// System.out.printf("solver name : " + kiSAOName + " (" + kiSAOId + ")\n");
				}
			}
			// At this point, either the solver is CVODE or kiSAO id could not be found for non-CVODE solver
			// for now : if a match for solver name is not found in KiSAO, set it to CVODE
			if (kiSAOId == null) {
		        IRI iri = kisaoQuery.searchByName(cvodeSolverName).iterator().next();
				kiSAOId = kisaoQuery.getId(iri);
	        	String kiSAOName = kisaoQuery.getName(iri);
				// System.out.printf("solver name : " + kiSAOName + " (" + kiSAOId + ")\n");
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace(System.err);
		}
		return kiSAOId;
	}

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
		MathMLReader mmlr = new MathMLReader();
		ASTNode mathNode = null;
		try {
			mathNode = mmlr.parseMathMLFromString(expMathMLStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
			throw new RuntimeException("Error converting MathML string to ASTNode:" + e.getMessage());
		}
		return mathNode;
//		return mathNode.deepCopy();
	}
	
	private void dummy() {
//		// ------ Functional Range
//		r = new FunctionalRange(rangeId, "-1");
//		ASTNode fRangeMath = SEDMLExporter.getFormulaFromExpression(expression);
//		if(fRangeMath != null) {
//			Function func = new Function(fRangeMath);
//			((FunctionalRange) r).setMath(func);
//			
//			String[] symbols = expression.getSymbols();
//			for(String symbolName : symbols) {
//				//String symbolName = TokenMangler.mangleToSName(symbol);
//				
//				mathVar = mathSymbolMapping.findVariableByName(symbolName);
//				stEntries = mathSymbolMapping.getBiologicalSymbol(mathVar);
//				if(stEntries.length > 1) {
//					System.err.println("SymbolTableEntry size: " + stEntries.length + ", " + stEntries.toString());
//				} else if(stEntries.length == 0) {
//					System.err.println("SymbolTableEntry size: " + stEntries.length + " for " + symbolName);
//				}
//				SymbolTableEntry ste1 = stEntries[0];
//				//SymbolTableEntry ste1 = vcModel.getEntry(symbol);
//				if (ste1 instanceof SpeciesContext || ste1 instanceof Structure || ste1 instanceof ReservedSymbol) {
//					Variable sedmlVar = new Variable(symbolName, symbolName, taskRef, sbmlSupport.getXPathForSpecies(symbolName));
//					((FunctionalRange) r).addVariable(sedmlVar);
//				} else if (ste1 instanceof ModelParameter) {
//					Parameter sedmlParameter = new Parameter(symbolName, symbolName, ((ModelParameter)ste1).getConstantValue());
//					((FunctionalRange) r).addVariable(sedmlParameter);
//				} else if (ste1 instanceof KineticsParameter) {
//					Parameter sedmlParameter = new Parameter(symbolName, symbolName, ((KineticsParameter)ste1).getConstantValue());
//					((FunctionalRange) r).addVariable(sedmlParameter);
//				} else {
//					System.err.println("Entity should be SpeciesContext, ReservedSymbol, Structure, ModelParameter or KineticParameter: " + ste1.getClass());
//				}
//			}
//		}
//		t.addRange(r);

	}
}


