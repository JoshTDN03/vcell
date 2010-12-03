package cbit.vcell.solver;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Set;

import org.vcell.util.CommentStringTokenizer;
import org.vcell.util.Compare;
import org.vcell.util.DataAccessException;
import org.vcell.util.Matchable;
import org.vcell.util.TokenMangler;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.SimulationVersion;
import org.vcell.util.document.Version;
import org.vcell.util.document.Versionable;

import cbit.vcell.client.GuiConstants;
import cbit.vcell.math.MathDescription;
import cbit.vcell.math.MathException;
import cbit.vcell.math.VCML;
import cbit.vcell.simdata.SimDataConstants;
import cbit.vcell.solver.SolverDescription.SolverFeature;
/**
 * Specifies the problem to be solved by a solver.
 * It is subclassed for each type of problem/solver.
 * Creation date: (8/16/2000 11:08:33 PM)
 * @author: John Wagner
 */
public class Simulation implements Versionable, Matchable, java.beans.VetoableChangeListener, java.io.Serializable,PropertyChangeListener {
	// size quotas enforced per simulation
	public static final int MAX_LIMIT_ODE_TIMEPOINTS = 100000;
	public static final int MAX_LIMIT_PDE_TIMEPOINTS = 100000;
	public static final int MAX_LIMIT_STOCH_TIMEPOINTS = 100000; //stoch 
	public static final int MAX_LIMIT_0DE_MEGABYTES = 20;
	public static final int MAX_LIMIT_PDE_MEGABYTES = 20000;
	public static final int MAX_LIMIT_STOCH_MEGABYTES = 200; //stoch
	public static final int WARNING_ODE_TIMEPOINTS = 5000;
	public static final int WARNING_PDE_TIMEPOINTS = 1000;
	public static final int WARNING_STOCH_TIMEPOINTS = 5000; //stoch 
	public static final int WARNING_0DE_MEGABYTES = 5;
	public static final int WARNING_PDE_MEGABYTES = 200;
	public static final int WARNING_STOCH_MEGABYTES = 100; //stoch
	
	public static final int WARNING_SCAN_JOBS = 20;
	public static final int MAX_LIMIT_SCAN_JOBS = 40;
	/**
	 * Database version of the Simulation.
	 */
	private SimulationVersion fieldSimulationVersion = null;
	/**
	 * Mathematical description of the physiological model.
	 */
	private MathDescription fieldMathDescription = null;
	/**
	 * An ASCII description of the run.
	 */
	private java.lang.String fieldDescription = new String();
	/**
	 * The name of the run, also used as a version name.
	 */
	private java.lang.String fieldName = new String("NoName");
	/**
	 * Settings that override those specified in the MathDescription.
	 */
	private DataProcessingInstructions dataProcessingInstructions = null;
	private MathOverrides fieldMathOverrides = null;
	protected transient java.beans.VetoableChangeSupport vetoPropertyChange;
	protected transient java.beans.PropertyChangeSupport propertyChange;
	private SolverTaskDescription fieldSolverTaskDescription = null;
	private java.lang.String fieldSimulationIdentifier = null;
	private MeshSpecification fieldMeshSpecification = null;
	private boolean fieldIsDirty = false;
	private java.lang.String fieldWarning = null;
	
/**
 * One of three ways to construct a Simulation.  This constructor
 * is used when creating a new Simulation.
 */
public Simulation(SimulationVersion argSimulationVersion, MathDescription mathDescription) {
	super();
	addVetoableChangeListener(this);
	this.fieldSimulationVersion = argSimulationVersion;
	if (fieldSimulationVersion != null) {
		if (fieldSimulationVersion.getParentSimulationReference()!=null){
			this.fieldSimulationIdentifier = null;
		}else{
			this.fieldSimulationIdentifier = createSimulationID(fieldSimulationVersion.getVersionKey());
		}
	}
	
	this.fieldName = argSimulationVersion.getName();
	this.fieldDescription = argSimulationVersion.getAnnot();

	try {
		setMathDescription(mathDescription);
	} catch (java.beans.PropertyVetoException e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	}
	//  Must set the MathDescription before constructing these...
	if (mathDescription.getGeometry().getDimension()>0){
		fieldMeshSpecification = new MeshSpecification(mathDescription.getGeometry());
	}
	fieldMathOverrides = new MathOverrides(this);
	fieldSolverTaskDescription = new SolverTaskDescription(this);
	refreshDependencies();
}


/**
 * One of three ways to construct a Simulation.  This constructor
 * is used when creating a Simulation from the database.
 */
public Simulation(SimulationVersion simulationVersion, MathDescription mathDescription, CommentStringTokenizer mathOverridesTokenizer, CommentStringTokenizer solverTaskDescriptionTokenizer) throws DataAccessException, PropertyVetoException {
	super();
	addVetoableChangeListener(this);

	fieldSimulationVersion = simulationVersion;
	if (simulationVersion!=null){
		fieldName = simulationVersion.getName();
		fieldDescription = simulationVersion.getAnnot();
		if (simulationVersion.getParentSimulationReference()!=null){
			fieldSimulationIdentifier = null;
		}else{
			fieldSimulationIdentifier = createSimulationID(simulationVersion.getVersionKey());
		}
	}	
	if (mathDescription != null){
		setMathDescription(mathDescription);
		if (mathDescription.getGeometry().getDimension()>0){
			fieldMeshSpecification = new MeshSpecification(mathDescription.getGeometry());
		}
	}
	//  Must set the MathDescription before constructing these...
	fieldMathOverrides = new MathOverrides(this, mathOverridesTokenizer);
	fieldSolverTaskDescription = new SolverTaskDescription(this, solverTaskDescriptionTokenizer);
	refreshDependencies();
}


/**
 * One of three ways to construct a Simulation.  This constructor
 * is used when creating a new Simulation.
 */
public Simulation(MathDescription mathDescription) {
	super();
	addVetoableChangeListener(this);

	try {
		setMathDescription(mathDescription);
	} catch (java.beans.PropertyVetoException e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	}
	fieldName = mathDescription.getName()+"_"+Math.random();
	//  Must set the MathDescription before constructing these...
	if (mathDescription.getGeometry().getDimension()>0){
		fieldMeshSpecification = new MeshSpecification(mathDescription.getGeometry());
	}
	fieldMathOverrides = new MathOverrides(this);
	fieldSolverTaskDescription = new SolverTaskDescription(this);

}


/**
 * One of three ways to construct a Simulation.  This constructor
 * is used when copying a Simulation from an existing one.
 */
public Simulation(Simulation simulation) {
	this(simulation,false);
}


/**
 * One of three ways to construct a Simulation.  This constructor
 * is used when copying a Simulation from an existing one.
 */
public Simulation(Simulation simulation, boolean bCloneMath) {
	super();
	addVetoableChangeListener(this);

	fieldSimulationVersion = null;
	fieldName = simulation.getName();
	fieldDescription = simulation.getDescription();
	fieldSimulationIdentifier = null;
	if (bCloneMath){
		fieldMathDescription = new MathDescription(simulation.getMathDescription());
	}else{
		fieldMathDescription = simulation.getMathDescription();
	}
	if (simulation.getMeshSpecification()!=null){
		fieldMeshSpecification = new MeshSpecification(simulation.getMeshSpecification());
	}else{
		fieldMeshSpecification = null;
	}
	//  Must set the MathDescription before constructing these...
	fieldMathOverrides = new MathOverrides (this, simulation.getMathOverrides());
	fieldSolverTaskDescription = new SolverTaskDescription(this, simulation.getSolverTaskDescription());
	dataProcessingInstructions = simulation.dataProcessingInstructions;
	refreshDependencies();
}


/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}

/**
 * The addVetoableChangeListener method was generated to support the vetoPropertyChange field.
 */
public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener listener) {
	getVetoPropertyChange().addVetoableChangeListener(listener);
}

/**
 * Insert the method's description here.
 * Creation date: (4/24/2003 3:33:12 PM)
 */
public void clearVersion() {
	fieldSimulationVersion = null;
}


/**
 * compareEqual method comment.
 */
public boolean compareEqual(Matchable object) {
	if (this == object) {
		return (true);
	}
	if (object != null && object instanceof Simulation) {
		Simulation simulation = (Simulation) object;
		//
		// check for content
		//
		if (!compareEqualMathematically(simulation)){
			return false; 
		}
		//
		// check for true equality
		//
		if (!Compare.isEqual(getName(),simulation.getName())){
			return false;
		}
		if (!Compare.isEqualOrNull(getDescription(),simulation.getDescription())){
			return false;
		}
		return true;
	}
	return false;

}


/**
 * compareEqual method comment.
 */
private boolean compareEqualMathematically(Simulation simulation) {
	if (this == simulation) {
		return true;
	}
	if (!getMathDescription().compareEqual(simulation.getMathDescription())) return (false);
	if (!getMathOverrides().compareEqual(simulation.getMathOverrides())) return (false);
	if (!getSolverTaskDescription().compareEqual(simulation.getSolverTaskDescription())) return (false);
	if (!Compare.isEqualOrNull(getMeshSpecification(),simulation.getMeshSpecification())) return (false);
	if (!Compare.isEqualOrNull(dataProcessingInstructions, simulation.dataProcessingInstructions)) return (false);

	return true;
}

/**
 * Insert the method's description here.
 * Creation date: (10/25/00 1:53:36 PM)
 * @return java.lang.String
 * @param version cbit.sql.Version
 */
public static String createSimulationID(KeyValue simKey) {
	return "SimID_"+simKey;
}


/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}


/**
 * The fireVetoableChange method was generated to support the vetoPropertyChange field.
 */
public void fireVetoableChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) throws java.beans.PropertyVetoException {
	getVetoPropertyChange().fireVetoableChange(propertyName, oldValue, newValue);
}


/**
 * Insert the method's description here.
 * Creation date: (3/18/2004 1:54:51 PM)
 * @param newVersion cbit.sql.Version
 */
public void forceNewVersionAnnotation(SimulationVersion newSimulationVersion) throws PropertyVetoException {
	if (getVersion().getVersionKey().equals(newSimulationVersion.getVersionKey())) {
		setVersion(newSimulationVersion);
	} else {
		throw new RuntimeException("Simulation.forceNewVersionAnnotation failed : version keys not equal");
	}
}


/**
 * Gets the description property (java.lang.String) value.
 * @return The description property value.
 * @see #setDescription
 */
public java.lang.String getDescription() {
	return fieldDescription;
}


/**
 * Gets the isDirty property (boolean) value.
 * @return The isDirty property value.
 * @see #setIsDirty
 */
public boolean getIsDirty() {
	return fieldIsDirty;
}


/**
 * Gets the isSpatial property (boolean) value.
 * @return The isSpatial property value.
 * @see #setIsSpatial
 */
public boolean isSpatial() {
	return getMathDescription().getGeometry().getDimension() > 0;
}


/**
 * Insert the method's description here.
 * Creation date: (5/25/01 11:25:24 AM)
 * @return boolean
 */
public boolean checkValid() {
	setWarning(null);

	//
	// Check if the math corresponding to this simulation has fast systems and if the solverTaskDescription contains a non-null sensitivity parameter.
	// If so, the simulation is invalid.
	//
	if (fieldMathDescription != null && getSolverTaskDescription() != null) {
		if (getMathDescription().hasFastSystems() && (getSolverTaskDescription().getSensitivityParameter() != null)) {
			setWarning("Sensitivity Analysis for a math with Fast Systems is not supported yet. Please disable sensitivity analysis for this simulation to run.");
			return false;
		}
	}
	if (fieldMathDescription==null || !fieldMathDescription.isValid()){
		setWarning(fieldMathDescription.getWarning());
		return false;
	}
	
	Set<SolverFeature> supportedFeatures = getSolverTaskDescription().getSolverDescription().getSupportedFeatures();
	Set<SolverFeature> missingFeatures = getRequiredFeatures();
	missingFeatures.removeAll(supportedFeatures);
	
	if (!missingFeatures.isEmpty()) {
		String text = "Selected solver '" + getSolverTaskDescription().getSolverDescription().getDisplayLabel() + "' does not support " +
		"the following required features: \n";
		for (SolverFeature sf : missingFeatures) {
			text += sf.getName() + "\n";
		}
		SolverDescription[] goodSolvers = SolverDescription.getSolverDescriptions(getRequiredFeatures().toArray(new SolverFeature[0]));
		if (goodSolvers != null && goodSolvers.length > 0) {
			text += "\nPlease choose one of the solvers : \n";
			for (SolverDescription sd : goodSolvers) {
				text += sd.getDisplayLabel() + "\n";
			}

		}
		setWarning(text);	
		return false;
	}
	return true;
	
}

public Set<SolverFeature> getRequiredFeatures() {
	Set<SolverFeature> requiredFeatures = new HashSet<SolverFeature>();
	if (isSpatial()) {
		requiredFeatures.add(SolverFeature.Feature_Spatial);
	} else {
		requiredFeatures.add(SolverFeature.Feature_NonSpatial);
	}
	if (getMathDescription().isNonSpatialStoch() || getMathDescription().isSpatialStoch()) {
		requiredFeatures.add(SolverFeature.Feature_Stochastic);
	} else {
		requiredFeatures.add(SolverFeature.Feature_Deterministic);
	}
	if (getMathDescription().hasFastSystems()) {
		requiredFeatures.add(SolverFeature.Feature_FastSystem);
	}
	if (getMathDescription().hasPeriodicBoundaryCondition()) {
		requiredFeatures.add(SolverFeature.Feature_PeriodicBoundaryCondition);
	}
	if (getMathDescription().hasEvents()) {
		requiredFeatures.add(SolverFeature.Feature_Events);
	}
	if (getMathDescription().hasRandomVariables()) {
		requiredFeatures.add(SolverFeature.Feature_RandomVariables);
	}
	if (getSolverTaskDescription().getStopAtSpatiallyUniformErrorTolerance() != null) {
		requiredFeatures.add(SolverFeature.Feature_StopAtSpatiallyUniform);
	}
	if (getDataProcessingInstructions() != null) {
		requiredFeatures.add(SolverFeature.Feature_DataProcessingInstructions);
	}
	if (getMathDescription().getVariable(SimDataConstants.PSF_FUNCTION_NAME) != null) {
		requiredFeatures.add(SolverFeature.Feature_PSF);
	}
	if (isSerialParameterScan()) {
		requiredFeatures.add(SolverFeature.Feature_SerialParameterScans);
	}
	if (getMathDescription().hasVolumeRegionEquations()) {
		requiredFeatures.add(SolverFeature.Feature_VolumeRegionEquations);
	}
	if (getMathDescription().hasRegionSizeFunctions()) {
		requiredFeatures.add(SolverFeature.Feature_RegionSizeFunctions);
	}
	return requiredFeatures;
}
/**
 * Insert the method's description here.
 * Creation date: (10/24/00 1:36:01 PM)
 * @return cbit.sql.KeyValue
 */
public KeyValue getKey() {
	return (getVersion()!=null)?(getVersion().getVersionKey()):(null);
}


/**
 * Gets the mathDescription property (cbit.vcell.math.MathDescription) value.
 * @return The mathDescription property value.
 */
public MathDescription getMathDescription() {
	return fieldMathDescription;
}


/**
 * Gets the mathOverrides property (cbit.vcell.solver.MathOverrides) value.
 * @return The mathOverrides property value.
 */
public MathOverrides getMathOverrides() {
	return fieldMathOverrides;
}


/**
 * Gets the meshSpecification property (cbit.vcell.mesh.MeshSpecification) value.
 * @return The meshSpecification property value.
 * @see #setMeshSpecification
 */
public MeshSpecification getMeshSpecification() {
	return fieldMeshSpecification;
}


/**
 * Gets the name property (java.lang.String) value.
 * @return The name property value.
 * @see #setName
 */
public java.lang.String getName() {
	return fieldName;
}


/**
 * Accessor for the propertyChange field.
 */
protected java.beans.PropertyChangeSupport getPropertyChange() {
	if (propertyChange == null) {
		propertyChange = new java.beans.PropertyChangeSupport(this);
	};
	return propertyChange;
}


/**
 * Insert the method's description here.
 * Creation date: (10/5/2005 1:02:08 PM)
 * @return int
 */
public int getScanCount() {
	return getMathOverrides().getScanCount();
}


/**
 * Gets the simulationIdentifier property (java.lang.String) value.
 * @return The simulationIdentifier property value.
 */
public java.lang.String getSimulationID() {
	return fieldSimulationIdentifier;
}


/**
 * Insert the method's description here.
 * Creation date: (10/30/00 11:48:21 AM)
 * @return cbit.vcell.solver.SimulationInfo
 */
public SimulationInfo getSimulationInfo() {
	if (getVersion() != null) {
		return new SimulationInfo(
			getMathDescription().getKey(),
			getSimulationVersion()); 
	} else {
		return null;
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/00 1:34:10 PM)
 * @return cbit.sql.Version
 */
public SimulationVersion getSimulationVersion() {
	return fieldSimulationVersion;
}


/**
 * Gets the solverTaskDescription property (cbit.vcell.solver.SolverTaskDescription) value.
 * @return The solverTaskDescription property value.
 * @see #setSolverTaskDescription
 */
public SolverTaskDescription getSolverTaskDescription() {
	return fieldSolverTaskDescription;
}


/**
 * Insert the method's description here.
 * Creation date: (10/30/00 11:56:03 AM)
 * @return java.lang.String
 */
public String getVCML() throws MathException {
	
	StringBuffer buffer = new StringBuffer();
	
	String name = (getVersion()!=null)?(getVersion().getName()):"unnamedSimulation";
	buffer.append(VCML.Simulation+" "+name+" {\n");

	//
	// write MathDescription
	//
	buffer.append(VCML.MathDescription+" "+getMathDescription().getVCML_database()+"\n");

	//
	// write SolverTaskDescription
	//
	buffer.append(getSolverTaskDescription().getVCML()+"\n");

	//
	// write SolverTaskDescription
	//
	buffer.append(getMathOverrides().getVCML()+"\n");

	//
	// write MeshSpecification
	//
	if (getMeshSpecification()!=null){
		buffer.append(getMeshSpecification().getVCML()+"\n");
	}

	buffer.append("}\n");
	return buffer.toString();		
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/00 1:34:10 PM)
 * @return cbit.sql.Version
 */
public Version getVersion() {
	return fieldSimulationVersion;
}


/**
 * Accessor for the vetoPropertyChange field.
 */
protected java.beans.VetoableChangeSupport getVetoPropertyChange() {
	if (vetoPropertyChange == null) {
		vetoPropertyChange = new java.beans.VetoableChangeSupport(this);
	};
	return vetoPropertyChange;
}


/**
 * Gets the warning property (java.lang.String) value.
 * @return The warning property value.
 */
public java.lang.String getWarning() {
	return fieldWarning;
}


/**
 * The hasListeners method was generated to support the propertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getPropertyChange().hasListeners(propertyName);
}

/**
 * Insert the method's description here.
 * Creation date: (5/11/01 4:00:35 PM)
 */
public void refreshDependencies() {
	removeVetoableChangeListener(this);
	addVetoableChangeListener(this);
	if (getMeshSpecification()!=null){
		getMeshSpecification().refreshDependencies();
	}
	getSolverTaskDescription().refreshDependencies();
	getMathOverrides().refreshDependencies();
	
	getMathDescription().removePropertyChangeListener(this);
	getMathDescription().addPropertyChangeListener(this);

}

/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}

/**
 * The removeVetoableChangeListener method was generated to support the vetoPropertyChange field.
 */
public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener listener) {
	getVetoPropertyChange().removeVetoableChangeListener(listener);
}

/**
 * Sets the description property (java.lang.String) value.
 * @param description The new value for the property.
 * @see #getDescription
 */
public void setDescription(java.lang.String description) throws java.beans.PropertyVetoException {
	java.lang.String oldValue = fieldDescription;
	fireVetoableChange("description", oldValue, description);
	fieldDescription = description;
	firePropertyChange("description", oldValue, description);
}


/**
 * Sets the isDirty property (boolean) value.
 * @param isDirty The new value for the property.
 * @see #getIsDirty
 */
public void setIsDirty(boolean isDirty) {
	boolean oldValue = fieldIsDirty;
	fieldIsDirty = isDirty;
	firePropertyChange("isDirty", new Boolean(oldValue), new Boolean(isDirty));
}


/**
 * Insert the method's description here.
 * Creation date: (10/24/00 1:17:37 PM)
 * @param mathDesc cbit.vcell.math.MathDescription
 */
public void setMathDescription(MathDescription mathDescription) throws java.beans.PropertyVetoException {
	MathDescription oldValue = fieldMathDescription;
	fireVetoableChange("mathDescription", oldValue, mathDescription);
	fieldMathDescription = mathDescription;

	if(oldValue != null){
		oldValue.removePropertyChangeListener(this);
	}
	if(fieldMathDescription != null){
		fieldMathDescription.removePropertyChangeListener(this);
		fieldMathDescription.addPropertyChangeListener(this);
	}
	refreshMeshSpec();

	//
	// refresh MathOverrides
	//
	if (mathDescription!=null && getMathOverrides()!=null){
		getMathOverrides().updateFromMathDescription();
	}

	//
	// refresh SolverTaskDescription (reset if oldMath is spatial and newMath is non-spatial .... or opposite).
	//
	if (oldValue==null || mathDescription==null || oldValue.isSpatial()!=mathDescription.isSpatial()){
		fieldSolverTaskDescription = new SolverTaskDescription(this);
	}

	firePropertyChange("mathDescription", oldValue, mathDescription);
}


/**
 * Insert the method's description here.
 * Creation date: (5/3/2001 7:13:50 PM)
 * @param newMathOverrides cbit.vcell.solver.MathOverrides
 * @exception java.beans.PropertyVetoException The exception description.
 */
public void setMathOverrides(MathOverrides mathOverrides) {
	MathOverrides oldValue = fieldMathOverrides;
	fieldMathOverrides = mathOverrides;
	// update overrides
	mathOverrides.setSimulation(this);
	mathOverrides.updateFromMathDescription();
	firePropertyChange("mathOverrides", oldValue, mathOverrides);
}


/**
 * Sets the meshSpecification property (cbit.vcell.mesh.MeshSpecification) value.
 * @param meshSpecification The new value for the property.
 * @exception java.beans.PropertyVetoException The exception description.
 * @see #getMeshSpecification
 */
public void setMeshSpecification(MeshSpecification meshSpecification) throws java.beans.PropertyVetoException {
	MeshSpecification oldValue = fieldMeshSpecification;
	fireVetoableChange("meshSpecification", oldValue, meshSpecification);
	fieldMeshSpecification = meshSpecification;
	firePropertyChange("meshSpecification", oldValue, meshSpecification);
}


/**
 * Sets the name property (java.lang.String) value.
 * @param name The new value for the property.
 * @see #setName
 */
public void setName(java.lang.String name) throws java.beans.PropertyVetoException {
	java.lang.String oldValue = fieldName;
	fireVetoableChange(GuiConstants.PROPERTY_NAME_NAME, oldValue, name);
	fieldName = name;
	firePropertyChange(GuiConstants.PROPERTY_NAME_NAME, oldValue, name);
}


/**
 * Sets the solverTaskDescription property (cbit.vcell.solver.SolverTaskDescription) value.
 * @param solverTaskDescription The new value for the property.
 * @exception java.beans.PropertyVetoException The exception description.
 * @see #getSolverTaskDescription()
 */
public void setSolverTaskDescription(SolverTaskDescription solverTaskDescription) throws java.beans.PropertyVetoException {
	SolverTaskDescription oldValue = fieldSolverTaskDescription;
	fireVetoableChange("solverTaskDescription", oldValue, solverTaskDescription);
	fieldSolverTaskDescription = solverTaskDescription;
	if (solverTaskDescription != null && solverTaskDescription.getSimulation() != this) {
		throw new IllegalArgumentException("SolverTaskDescription simulation field points to wrong simulation");
	}
	firePropertyChange("solverTaskDescription", oldValue, solverTaskDescription);
}


/**
 * Insert the method's description here.
 * Creation date: (11/14/00 3:49:12 PM)
 * @param version cbit.sql.Version
 */
private void setVersion(SimulationVersion simulationVersion) throws PropertyVetoException {
	this.fieldSimulationVersion = simulationVersion;
	if (simulationVersion != null){
		setName(simulationVersion.getName());
		setDescription(simulationVersion.getAnnot());
	}
}


/**
 * Sets the warning property (java.lang.String) value.
 * @param warning The new value for the property.
 * @see #getWarning
 */
private void setWarning(java.lang.String warning) {
	String oldValue = fieldWarning;
	fieldWarning = warning;
	firePropertyChange("warning", oldValue, warning);
}

/**
 * Insert the method's description here.
 * Creation date: (9/28/2004 5:50:22 PM)
 * @return java.lang.String
 * @param memoryMathDescription cbit.vcell.math.MathDescription
 * @param databaseMathDescription cbit.vcell.math.MathDescription
 */
public static boolean testEquivalency(Simulation memorySimulation, Simulation databaseSimulation, String mathEquivalency) {

	if (memorySimulation == databaseSimulation){
		return true;
	}
	
	if (mathEquivalency.equals(MathDescription.MATH_DIFFERENT)){
		return false;
	}else if (mathEquivalency.equals(MathDescription.MATH_SAME) || mathEquivalency.equals(MathDescription.MATH_EQUIVALENT)){
		if (!memorySimulation.getSolverTaskDescription().compareEqual(databaseSimulation.getSolverTaskDescription())){
			return false;
		}
		if (!Compare.isEqualOrNull(memorySimulation.getMeshSpecification(),databaseSimulation.getMeshSpecification())){
			return false;
		}
		//
		// math overrides are only influence the solution if they actually override something.
		//
		// if maths are equal/equivalent and overridden parameters (where actual value != default value) are same
		// then the MathDescriptions equality/equivalence is upheld.
		// otherwise, they are always different
		//
		// if (!memorySimulation.getMathOverrides().compareEqualIgnoreDefaults(databaseSimulation.getMathOverrides())){
		// now only non-defaults are stored in overrides...
		if (!memorySimulation.getMathOverrides().compareEqual(databaseSimulation.getMathOverrides())){
			return false;
		}
		return true;
	}else{
		throw new IllegalArgumentException("unknown equivalency choice '"+mathEquivalency+"'");
	}
}


public String toString() {
	String mathStr = (getMathDescription()!=null)?("Math@"+Integer.toHexString(getMathDescription().hashCode())+"("+getMathDescription().getName()+","+getMathDescription().getKey()+")"):"null";
	return "Simulation@"+Integer.toHexString(hashCode())+"("+getName()+"), "+mathStr;
}


	/**
	 * This method gets called when a constrained property is changed.
	 *
	 * @param     evt a <code>PropertyChangeEvent</code> object describing the
	 *   	      event source and the property that has changed.
	 * @exception PropertyVetoException if the recipient wishes the property
	 *              change to be rolled back.
	 */
public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
	TokenMangler.checkNameProperty(this, "simulation", evt);
}


	public DataProcessingInstructions getDataProcessingInstructions() {
		return dataProcessingInstructions;
	}


	public void setDataProcessingInstructions(DataProcessingInstructions dataProcessingInstructions) {
		DataProcessingInstructions oldValue = this.dataProcessingInstructions;
		this.dataProcessingInstructions = dataProcessingInstructions;
		firePropertyChange("dataProcessingInstructions", oldValue, dataProcessingInstructions);
}

public boolean isSerialParameterScan() {
	if (getSolverTaskDescription().isSerialParameterScan() && getScanCount() > 1) {
		return true;
	}
	return false;
}

	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		if(evt.getSource() == getMathDescription() && evt.getPropertyName().equals("geometry")){
			try{
				refreshMeshSpec();
			}catch(PropertyVetoException e){
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(),e);
			}
		}
		
	}

	private void refreshMeshSpec() throws PropertyVetoException{
		//
		// refresh MeshSpecification
		//
		if (getMathDescription().getGeometry().getDimension()>0){
			if (getMeshSpecification()!=null){
				getMeshSpecification().setGeometry(getMathDescription().getGeometry());
			}else{
				setMeshSpecification(new MeshSpecification(getMathDescription().getGeometry()));
			}
		}else{
			setMeshSpecification(null);
		}
	}
}