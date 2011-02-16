package cbit.vcell.mapping;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.interval.ia_math.RealInterval;

import org.vcell.util.BeanUtils;
import org.vcell.util.Compare;
import org.vcell.util.Issue;
import org.vcell.util.Matchable;

import cbit.vcell.geometry.GeometryClass;
import cbit.vcell.geometry.surface.GeometricRegion;
import cbit.vcell.model.BioNameScope;
import cbit.vcell.model.ExpressionContainer;
import cbit.vcell.model.Feature;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.ModelException;
import cbit.vcell.model.Parameter;
import cbit.vcell.model.ProxyParameter;
import cbit.vcell.model.ReservedBioSymbolEntries;
import cbit.vcell.model.ReservedSymbol;
import cbit.vcell.model.SimpleBoundsIssue;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.model.VCMODL;
import cbit.vcell.parser.AbstractNameScope;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionBindingException;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.NameScope;
import cbit.vcell.parser.ScopedSymbolTable;
import cbit.vcell.parser.SymbolTable;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.parser.SymbolTableFunctionEntry;
import cbit.vcell.units.VCUnitDefinition;

@SuppressWarnings("serial")
public class SpeciesContextSpec implements Matchable, ScopedSymbolTable, Serializable {

	public static final String PARAMETER_NAME_PROXY_PARAMETERS = "proxyParameters";
	private static final String PROPERTY_NAME_WELL_MIXED = "wellMixed";


	public class SpeciesContextSpecNameScope extends BioNameScope {
		private final NameScope children[] = new NameScope[0]; // always empty
		public SpeciesContextSpecNameScope(){
			super();
		}
		public NameScope[] getChildren() {
			//
			// no children to return
			//
			return children;
		}
		public String getName() {
			return SpeciesContextSpec.this.getSpeciesContext().getName()+"_scs";
		}
		public NameScope getParent() {
			if (SpeciesContextSpec.this.simulationContext != null){
				return SpeciesContextSpec.this.simulationContext.getNameScope();
			}else{
				return null;
			}
		}
		public ScopedSymbolTable getScopedSymbolTable() {
			return SpeciesContextSpec.this;
		}
		@Override
		public String getPathDescription() {
			if (simulationContext != null){
				return "App("+simulationContext.getName()+") / Species("+getSpeciesContext().getName()+")";
			}
			return null;
		}
	}
	
	public class SpeciesContextSpecParameter extends Parameter implements ExpressionContainer {
		private Expression fieldParameterExpression = null;
		private String fieldParameterName = null;
 		private int fieldParameterRole = -1;
 		private VCUnitDefinition fieldUnitDefinition = null;

		public SpeciesContextSpecParameter(String parmName, Expression argExpression, int argRole, VCUnitDefinition argUnitDefinition, String argDescription) {
			super();
			fieldParameterName = parmName;
			fieldParameterExpression = argExpression;
			if (argRole >= 0 && argRole < NUM_ROLES){
				this.fieldParameterRole = argRole;
				
			}else{
				throw new IllegalArgumentException("parameter 'role' = "+argRole+" is out of range");
			}
			fieldUnitDefinition = argUnitDefinition;
			setDescription(argDescription);
		}
		
		@Override	
		public String getNullExpressionDescription() {
			if (fieldParameterRole == ROLE_BoundaryValueXm
					|| fieldParameterRole == ROLE_BoundaryValueXp
					|| fieldParameterRole == ROLE_BoundaryValueYm
					|| fieldParameterRole == ROLE_BoundaryValueYp
					|| fieldParameterRole == ROLE_BoundaryValueZm
					|| fieldParameterRole == ROLE_BoundaryValueZp) {
				if (fieldUnitDefinition != null) {
					if (fieldUnitDefinition.compareEqual(getSpeciesContext().getUnitDefinition())) {
						return "<html><i>&lt;initial value&gt;</i></html>"; 
					} else if (fieldUnitDefinition.compareEqual(getSpeciesContext().getUnitDefinition().multiplyBy(VCUnitDefinition.UNIT_um_per_s))){
						return "<html><i>&lt;zero flux&gt;</i></html>";
					}
				}
			} else if (fieldParameterRole == ROLE_VelocityX
					|| fieldParameterRole == ROLE_VelocityY
					|| fieldParameterRole == ROLE_VelocityZ) {
				return "<html><i>&lt;0.0&gt;</i></html>";
			}
			return null;
		}
		public boolean compareEqual(Matchable obj) {
			if (!(obj instanceof SpeciesContextSpecParameter)){
				return false;
			}
			SpeciesContextSpecParameter smp = (SpeciesContextSpecParameter)obj;
			if (!super.compareEqual0(smp)){
				return false;
			}
			if (fieldParameterRole != smp.fieldParameterRole){
				return false;
			}
			
			return true;
		}

		public NameScope getNameScope(){
			return SpeciesContextSpec.this.getNameScope();
		}

		public boolean isExpressionEditable(){
			return true;
		}

		@Override
		public String getDescription() {
			return super.getDescription() + " for "+getSpeciesContext().getName();
		}
		
		public boolean isUnitEditable(){
			return false;
		}

		public boolean isNameEditable(){
			return false;
		}

		public VCUnitDefinition getUnitDefinition() {
			return fieldUnitDefinition;
		}
		
		public void setExpression(Expression expression) throws PropertyVetoException, ExpressionBindingException {
			if (expression!=null){
				expression = new Expression(expression);
				// Need to bind this expression, but changing the binding from SpeciesContextSpec.this to a symbolTable created on the fly.
				// This new symbolTable is this SpeciesContextSpec, but omits its parameters, so that the SpeciesContextSpecParameter
				// expression that is being bound cannot contain other speciesContextSpecParameters in its expression.
				expression.bindExpression(new SymbolTable() {
					public SymbolTableEntry getEntry(String identifierString) throws ExpressionBindingException {
						SymbolTableEntry ste = SpeciesContextSpec.this.getEntry(identifierString);
						if (ste instanceof SpeciesContextSpecParameter) {
							throw new ExpressionBindingException("\nCannot use a speciesContextSpec parameter (e.g., diff, initConc, Vel_X, etc.) in another speciesContextSpec parameter expression.");
						}
						return ste;
					}

					public void getEntries(Map<String, SymbolTableEntry> entryMap) {
						SpeciesContextSpec.this.getEntries(entryMap);
					}
				});
			}
			Expression oldValue = fieldParameterExpression;
			super.fireVetoableChange("expression", oldValue, expression);
			fieldParameterExpression = expression;
			try {
				SpeciesContextSpec.this.cleanupParameters();
			} catch (ModelException e) {
				e.printStackTrace();
			} catch (ExpressionException e) {
				e.printStackTrace();
			}
			super.firePropertyChange("expression", oldValue, expression);
		}
		
		public double getConstantValue() throws ExpressionException {
			return fieldParameterExpression.evaluateConstant();
		}
		
		public void setName(java.lang.String name) throws java.beans.PropertyVetoException {
			String oldValue = fieldParameterName;
			super.fireVetoableChange("name", oldValue, name);
			fieldParameterName = name;
			super.firePropertyChange("name", oldValue, name);
		}
		
		public void setUnitDefinition(VCUnitDefinition unitDefinition) throws java.beans.PropertyVetoException {
			VCUnitDefinition oldValue = fieldUnitDefinition;
			super.fireVetoableChange("unitDefinition", oldValue, unitDefinition);
			fieldUnitDefinition = unitDefinition;
			super.firePropertyChange("unitDefinition", oldValue, unitDefinition);
		}
		
		public String getName(){
			return fieldParameterName;
		}
		
		public Expression getExpression(){
			return fieldParameterExpression;
		}
		
		public int getIndex() {
			return -1;
		}

		public int getRole() {
			return fieldParameterRole;
		}
	}
	
	public class SpeciesContextSpecProxyParameter extends ProxyParameter {

		public SpeciesContextSpecProxyParameter(SymbolTableEntry target){
			super(target);
		}
		
		public NameScope getNameScope(){
			return SpeciesContextSpec.this.getNameScope();
		}

		public boolean compareEqual(Matchable obj) {
			if (!(obj instanceof SpeciesContextSpecProxyParameter)){
				return false;
			}
			SpeciesContextSpecProxyParameter other = (SpeciesContextSpecProxyParameter)obj;
			if (getTarget() instanceof Matchable && other.getTarget() instanceof Matchable &&
				Compare.isEqual((Matchable)getTarget(), (Matchable)other.getTarget())){
				return true;
			}else{
				return false;
			}
		}

		
		@Override
		public String getDescription() {
			if (getTarget() instanceof SpeciesContext) {
				return "Species Concentration";
			} else {
				return super.getDescription();
			}
		}

		@Override
		public void targetPropertyChange(PropertyChangeEvent evt) {
			super.targetPropertyChange(evt);
			if (evt.getPropertyName().equals("name")){
				String oldName = (String)evt.getOldValue();
				String newName = (String)evt.getNewValue();
				try {
					SpeciesContextSpecParameter newParameters[] = new SpeciesContextSpecParameter[fieldParameters.length];
					System.arraycopy(fieldParameters, 0, newParameters, 0, fieldParameters.length);
					//
					// go through all parameters' expressions and replace references to 'oldName' with 'newName'
					//
					for (int i = 0; i < newParameters.length; i++){ 
						Expression exp = getParameter(i).getExpression();
						if (exp != null) {
							Expression newExp = new Expression(exp);
							newExp.substituteInPlace(new Expression(oldName),new Expression(newName));
							newParameters[i] = new SpeciesContextSpecParameter(newParameters[i].getName(),newExp,newParameters[i].getRole(),newParameters[i].getUnitDefinition(), newParameters[i].getDescription());
						}
					}
					setParameters(newParameters);
	
					// 
					// rebind all expressions
					//
					for (int i = 0; i < newParameters.length; i++){
						if (newParameters[i].getExpression() != null) {
							newParameters[i].getExpression().bindExpression(SpeciesContextSpec.this);
						}
					}

					//
					// clean up dangling parameters (those not reachable from the 'required' parameters).
					//
					cleanupParameters();

				}catch (ModelException e1){
					e1.printStackTrace(System.out);
				}catch (ExpressionException e2){
					e2.printStackTrace(System.out);
				}catch (PropertyVetoException e3){
					e3.printStackTrace(System.out);
				}
			} 
		} 
		
	}

	
	private SpeciesContext speciesContext = null;
	private static final boolean DEFAULT_CONSTANT = false;
//	private static final boolean DEFAULT_ENABLE_DIFFUSING = true;
	private static final Boolean DEFAULT_WELL_MIXED = false;
	private boolean        bConstant = DEFAULT_CONSTANT;
//	private boolean        bEnableDiffusing = DEFAULT_ENABLE_DIFFUSING;
	private Boolean        bWellMixed = DEFAULT_WELL_MIXED;
	protected transient java.beans.VetoableChangeSupport vetoPropertyChange;
	private SpeciesContextSpecParameter[] fieldParameters = null;
	private SpeciesContextSpecProxyParameter[] fieldProxyParameters = new SpeciesContextSpecProxyParameter[0];
	protected transient java.beans.PropertyChangeSupport propertyChange;
	protected transient SimulationContext simulationContext = null;
	private SpeciesContextSpecNameScope nameScope = new SpeciesContextSpecNameScope();
	public static final int ROLE_InitialConcentration	= 0;
	public static final int ROLE_DiffusionRate			= 1;
	public static final int ROLE_BoundaryValueXm		= 2;
	public static final int ROLE_BoundaryValueXp		= 3;
	public static final int ROLE_BoundaryValueYm		= 4;
	public static final int ROLE_BoundaryValueYp		= 5;
	public static final int ROLE_BoundaryValueZm		= 6;
	public static final int ROLE_BoundaryValueZp		= 7;
	public static final int ROLE_InitialCount			= 8;
	public static final int ROLE_VelocityX				= 9;
	public static final int ROLE_VelocityY				= 10;
	public static final int ROLE_VelocityZ				= 11;
	
	public static final int NUM_ROLES		= 12;
	public static final String RoleTags[] = {
		VCMODL.InitialConcentration,
		VCMODL.DiffusionRate,
		VCMODL.BoundaryConditionXm,
		VCMODL.BoundaryConditionXp,
		VCMODL.BoundaryConditionYm,
		VCMODL.BoundaryConditionYp,
		VCMODL.BoundaryConditionZm,
		VCMODL.BoundaryConditionZp,
		VCMODL.InitialCount,
		VCMODL.VelocityX,
		VCMODL.VelocityY,
		VCMODL.VelocityZ
	};
	public static final String RoleNames[] = {
		"initConc",
		"diff",
		"BC_Xm",
		"BC_Xp",
		"BC_Ym",
		"BC_Yp",
		"BC_Zm",
		"BC_Zp",
		"initCount",
		"Vel_X",
		"Vel_Y",
		"Vel_Z"
	};
	public static final String RoleDescriptions[] = {
		"initial concentration", 
		"diffusion constant",
		"Boundary Condition X-",
		"Boundary Condition X+",
		"Boundary Condition Y-",
		"Boundary Condition Y+",
		"Boundary Condition Z-",
		"Boundary Condition Z+",
		"initial count",
		"Velocity X",
		"Velocity Y",
		"Velocity Z"
	};
	private static RealInterval[] parameterBounds = {
		new RealInterval(0.0, Double.POSITIVE_INFINITY), // init concentration
		new RealInterval(0.0, Double.POSITIVE_INFINITY), // diff rate
		null,	// BC X-
		null,	// BC X+
		null,	// BC Y-
		null,	// BC Y+
		null,	// BC Z-
		null,	// BC Z+
		new RealInterval(0.0, Double.POSITIVE_INFINITY), // init amount
		null, // Velocity X
		null, // Velocity Y
		null  // Velocity Z
	};

public SpeciesContextSpec(SpeciesContextSpec speciesContextSpec, SimulationContext argSimulationContext) {
	this.speciesContext = speciesContextSpec.speciesContext;
	this.bConstant = speciesContextSpec.bConstant;
//	this.bEnableDiffusing = speciesContextSpec.bEnableDiffusing;
	this.bWellMixed = speciesContextSpec.bWellMixed;
	fieldParameters = new SpeciesContextSpecParameter[speciesContextSpec.fieldParameters.length];
	for (int i = 0; i < speciesContextSpec.fieldParameters.length; i++){
		SpeciesContextSpecParameter otherParm = speciesContextSpec.fieldParameters[i];
		Expression otherParmExp = (otherParm.getExpression()==null)?(null):(new Expression(otherParm.getExpression()));
		fieldParameters[i] = new SpeciesContextSpecParameter(otherParm.getName(),otherParmExp,otherParm.getRole(),otherParm.getUnitDefinition(),otherParm.getDescription());
	}
	this.simulationContext = argSimulationContext;
	refreshDependencies();
}            


public SpeciesContextSpec(SpeciesContext speciesContext, SimulationContext argSimulationContext) {
	this.speciesContext = speciesContext;

	// avoid unit computation for case of micromolar
	VCUnitDefinition unit = speciesContext.getUnitDefinition();
	VCUnitDefinition fluxUnits = computeFluxUnit();
	fieldParameters = new SpeciesContextSpecParameter[NUM_ROLES];
	if(argSimulationContext == null)//called from XmlReader.getSpeciesContextSpec(Element)
	{
		fieldParameters[ROLE_InitialConcentration] = new SpeciesContextSpecParameter(RoleNames[ROLE_InitialConcentration], null,
															ROLE_InitialConcentration,unit,
															RoleDescriptions[ROLE_InitialConcentration]);
		
		fieldParameters[ROLE_InitialCount] = new SpeciesContextSpecParameter(RoleNames[ROLE_InitialCount], null,
															ROLE_InitialCount,VCUnitDefinition.UNIT_molecules,
															RoleDescriptions[ROLE_InitialCount]);
	}
	else //called from ReactionContext.refreshSpeciesContextSpecs()
	{
		if(argSimulationContext.isUsingConcentration())
		{
			fieldParameters[ROLE_InitialConcentration] = new SpeciesContextSpecParameter(RoleNames[ROLE_InitialConcentration], new Expression(0),
					ROLE_InitialConcentration,unit,
					RoleDescriptions[ROLE_InitialConcentration]);

			fieldParameters[ROLE_InitialCount] = new SpeciesContextSpecParameter(RoleNames[ROLE_InitialCount], null,
					ROLE_InitialCount,VCUnitDefinition.UNIT_molecules,
					RoleDescriptions[ROLE_InitialCount]);
		}
		else
		{
			fieldParameters[ROLE_InitialConcentration] = new SpeciesContextSpecParameter(RoleNames[ROLE_InitialConcentration], null,
					ROLE_InitialConcentration,unit,
					RoleDescriptions[ROLE_InitialConcentration]);

			fieldParameters[ROLE_InitialCount] = new SpeciesContextSpecParameter(RoleNames[ROLE_InitialCount], new Expression(0),
					ROLE_InitialCount,VCUnitDefinition.UNIT_molecules,
					RoleDescriptions[ROLE_InitialCount]);
		}
	}
	fieldParameters[ROLE_DiffusionRate] = new SpeciesContextSpecParameter(RoleNames[ROLE_DiffusionRate],new Expression(0.0),
														ROLE_DiffusionRate,VCUnitDefinition.UNIT_um2_per_s,
														RoleDescriptions[ROLE_DiffusionRate]);

	fieldParameters[ROLE_BoundaryValueXm] = new SpeciesContextSpecParameter(RoleNames[ROLE_BoundaryValueXm],null,
														ROLE_BoundaryValueXm,fluxUnits,
														RoleDescriptions[ROLE_BoundaryValueXm]);
	
	fieldParameters[ROLE_BoundaryValueXp] = new SpeciesContextSpecParameter(RoleNames[ROLE_BoundaryValueXp],null,
														ROLE_BoundaryValueXp,fluxUnits,
														RoleDescriptions[ROLE_BoundaryValueXp]);

	fieldParameters[ROLE_BoundaryValueYm] = new SpeciesContextSpecParameter(RoleNames[ROLE_BoundaryValueYm],null,
														ROLE_BoundaryValueYm,fluxUnits,
														RoleDescriptions[ROLE_BoundaryValueYm]);
	
	fieldParameters[ROLE_BoundaryValueYp] = new SpeciesContextSpecParameter(RoleNames[ROLE_BoundaryValueYp],null,
														ROLE_BoundaryValueYp,fluxUnits,
														RoleDescriptions[ROLE_BoundaryValueYp]);

	fieldParameters[ROLE_BoundaryValueZm] = new SpeciesContextSpecParameter(RoleNames[ROLE_BoundaryValueZm],null,
														ROLE_BoundaryValueZm,fluxUnits,
														RoleDescriptions[ROLE_BoundaryValueZm]);
	
	fieldParameters[ROLE_BoundaryValueZp] = new SpeciesContextSpecParameter(RoleNames[ROLE_BoundaryValueZp],null,
														ROLE_BoundaryValueZp,fluxUnits,
														RoleDescriptions[ROLE_BoundaryValueZp]);
	fieldParameters[ROLE_VelocityX] = new SpeciesContextSpecParameter(RoleNames[ROLE_VelocityX], null,
														ROLE_VelocityX, VCUnitDefinition.UNIT_um_per_s,	RoleDescriptions[ROLE_VelocityX]);
	
	fieldParameters[ROLE_VelocityY] = new SpeciesContextSpecParameter(RoleNames[ROLE_VelocityY], null,
														ROLE_VelocityY, VCUnitDefinition.UNIT_um_per_s,	RoleDescriptions[ROLE_VelocityY]);
	
	fieldParameters[ROLE_VelocityZ] = new SpeciesContextSpecParameter(RoleNames[ROLE_VelocityZ], null,
														ROLE_VelocityZ, VCUnitDefinition.UNIT_um_per_s,	RoleDescriptions[ROLE_VelocityZ]);

	this.simulationContext = argSimulationContext;
	resetDefaults();
	refreshDependencies();
}            


public VCUnitDefinition computeFluxUnit() {
	return speciesContext.getUnitDefinition().multiplyBy(VCUnitDefinition.UNIT_um_per_s);
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

public SpeciesContextSpecProxyParameter addProxyParameter(SymbolTableEntry symbolTableEntry) {
	if (getParameterFromName(symbolTableEntry.getName())!=null){
		throw new RuntimeException("local parameter '"+symbolTableEntry.getName()+"' already exists");
	}
	if (getProxyParameter(symbolTableEntry.getName())!=null){
		throw new RuntimeException("referenced external symbol '"+symbolTableEntry.getName()+"' already exists");
	}
	SpeciesContextSpecProxyParameter newProxyParameter = new SpeciesContextSpecProxyParameter(symbolTableEntry);
	SpeciesContextSpecProxyParameter newProxyParameters[] = (SpeciesContextSpecProxyParameter[])BeanUtils.addElement(fieldProxyParameters,newProxyParameter);
	setProxyParameters(newProxyParameters);
	return newProxyParameter;
}

/**
 * @return boolean
 * @param object java.lang.Object
 */
public boolean compareEqual(Matchable object) {

	SpeciesContextSpec scs = null;
	if (!(object instanceof SpeciesContextSpec)){
		return false;
	}
	scs = (SpeciesContextSpec)object;

	if (!Compare.isEqual(speciesContext,scs.speciesContext)){
		return false;
	}

	if (bConstant != scs.bConstant){
		return false;
	}
	
//	if (bEnableDiffusing != scs.bEnableDiffusing){
//		return false;
//	}

	if (bWellMixed!=null && scs.bWellMixed!=null){
		if (!bWellMixed.equals(scs.bWellMixed)){
			return false;
		}
	}else if (bWellMixed!=null || scs.bWellMixed!=null){
		return false; // one is specified and one isn't ... 
	}

	if (!Compare.isEqual(fieldParameters,scs.fieldParameters)){
		return false;
	}

	return true;
}


/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}


/**
 * The fireVetoableChange method was generated to support the vetoPropertyChange field.
 */
public void fireVetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
	getVetoPropertyChange().fireVetoableChange(evt);
}


/**
 * The fireVetoableChange method was generated to support the vetoPropertyChange field.
 */
public void fireVetoableChange(java.lang.String propertyName, int oldValue, int newValue) throws java.beans.PropertyVetoException {
	getVetoPropertyChange().fireVetoableChange(propertyName, oldValue, newValue);
}


/**
 * The fireVetoableChange method was generated to support the vetoPropertyChange field.
 */
public void fireVetoableChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) throws java.beans.PropertyVetoException {
	getVetoPropertyChange().fireVetoableChange(propertyName, oldValue, newValue);
}


/**
 * The fireVetoableChange method was generated to support the vetoPropertyChange field.
 */
public void fireVetoableChange(java.lang.String propertyName, boolean oldValue, boolean newValue) throws java.beans.PropertyVetoException {
	getVetoPropertyChange().fireVetoableChange(propertyName, oldValue, newValue);
}


/**
 * Insert the method's description here.
 * Creation date: (11/1/2005 10:03:46 AM)
 * @param issueVector java.util.Vector
 */
public void gatherIssues(Vector<Issue> issueVector) {
	//
	// add constraints (simpleBounds) for predefined parameters
	//
	for (int i = 0; i < fieldParameters.length; i++){
		RealInterval simpleBounds = parameterBounds[fieldParameters[i].getRole()];
		if (simpleBounds!=null){
			String parmName = fieldParameters[i].getNameScope().getName()+"."+fieldParameters[i].getName();
			issueVector.add(new SimpleBoundsIssue(fieldParameters[i], simpleBounds, "parameter "+parmName+": must be within "+simpleBounds.toString()));
		}
	}
}


/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getBoundaryXmParameter() {
	return getParameterFromRole(ROLE_BoundaryValueXm);		
}


/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getBoundaryXpParameter() {
	return getParameterFromRole(ROLE_BoundaryValueXp);		
}

/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getBoundaryYmParameter() {
	return getParameterFromRole(ROLE_BoundaryValueYm);		
}

/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getBoundaryYpParameter() {
	return getParameterFromRole(ROLE_BoundaryValueYp);		
}

/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getBoundaryZmParameter() {
	return getParameterFromRole(ROLE_BoundaryValueZm);		
}

/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getBoundaryZpParameter() {
	return getParameterFromRole(ROLE_BoundaryValueZp);		
}

/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getDiffusionParameter() {
	return getParameterFromRole(ROLE_DiffusionRate);		
}

public SpeciesContextSpec.SpeciesContextSpecParameter getVelocityXParameter() {
	return getParameterFromRole(ROLE_VelocityX);		
}

/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getVelocityYParameter() {
	return getParameterFromRole(ROLE_VelocityY);		
}

/**
 * @return cbit.vcell.parser.Expression
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getVelocityZParameter() {
	return getParameterFromRole(ROLE_VelocityZ);		
}

/**
 * getEntry method comment.
 */
public SymbolTableEntry getEntry(String identifierString) throws ExpressionBindingException {
	
	SymbolTableEntry ste = getLocalEntry(identifierString);
	if (ste != null){
		return ste;
	}
			
	ste = getNameScope().getExternalEntry(identifierString,this);

	if (ste instanceof SymbolTableFunctionEntry){
		return ste;
	}
	
	if (ste!=null){
		return addProxyParameter(ste);
	}
	
	//
	// if all else fails, try reserved symbols
	//
	SymbolTableEntry reservedSTE = ReservedBioSymbolEntries.getEntry(identifierString);
	if (reservedSTE instanceof SymbolTableFunctionEntry){
		return reservedSTE;
	}
	if (reservedSTE != null){
		return addProxyParameter(reservedSTE);
	}

	return null;
}

/**
 * @return java.lang.String
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getInitialConditionParameter()
{
	SpeciesContextSpec.SpeciesContextSpecParameter initParam = null;
	if(getParameterFromRole(ROLE_InitialConcentration).getExpression() != null)
	{
		initParam = getParameterFromRole(ROLE_InitialConcentration);
	}
	else if(getParameterFromRole(ROLE_InitialCount).getExpression() != null)
	{
		initParam = getParameterFromRole(ROLE_InitialCount);
	}
	
	return initParam;
}

public SpeciesContextSpec.SpeciesContextSpecParameter getInitialConcentrationParameter() {
	return getParameterFromRole(ROLE_InitialConcentration);		
}

public SpeciesContextSpec.SpeciesContextSpecParameter getInitialCountParameter() {
	return getParameterFromRole(ROLE_InitialCount);		
}

/**
 * Insert the method's description here.
 * Creation date: (12/8/2003 11:46:37 AM)
 * @return SymbolTableEntry
 * @param identifier java.lang.String
 */
public SymbolTableEntry getLocalEntry(java.lang.String identifier) throws ExpressionBindingException {
	SymbolTableEntry ste = null;

	ste = ReservedBioSymbolEntries.getEntry(identifier);
	if (ste!=null){
		return ste;
	}

	ste = getParameterFromName(identifier);
	if (ste!=null){
		return ste;
	}

	ste = getProxyParameter(identifier);
	if (ste!=null){
		return ste;
	}

	return null;
}


/**
 * Insert the method's description here.
 * Creation date: (12/8/2003 11:46:37 AM)
 * @return cbit.vcell.parser.NameScope
 */
public NameScope getNameScope() {
	return nameScope;
}


/**
 * Insert the method's description here.
 * Creation date: (4/3/2004 10:48:38 AM)
 * @return cbit.vcell.mapping.SpeciesContextSpec.SpeciesContextSpecParameter
 * @param role int
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getParameterFromName(String name) {
	for (int i = 0; i < fieldParameters.length; i++){
		if (fieldParameters[i].getName().equals(name)){
			return fieldParameters[i];
		}
	}
	return null;
}


/**
 * Insert the method's description here.
 * Creation date: (4/3/2004 10:48:38 AM)
 * @return cbit.vcell.mapping.SpeciesContextSpec.SpeciesContextSpecParameter
 * @param role int
 */
public SpeciesContextSpec.SpeciesContextSpecParameter getParameterFromRole(int role) {
	for (int i = 0; i < fieldParameters.length; i++){
		if (fieldParameters[i].getRole() == role){
			return fieldParameters[i];
		}
	}
	return null;
}


/**
 * Gets the parameters property (cbit.vcell.model.Parameter[]) value.
 * @return The parameters property value.
 * @see #setParameters
 */
public Parameter[] getParameters() {
	return fieldParameters;
}


/**
 * Gets the parameters index property (cbit.vcell.model.Parameter) value.
 * @return The parameters property value.
 * @param index The index value into the property array.
 * @see #setParameters
 */
public Parameter getParameter(int index) {
//	please reference getNumDisplayableParameters()(hardcoded number of parameters) to get more understanding.
	Parameter param = null;
	if(index == ROLE_InitialConcentration)//means take the initial conidtion parameter and concentration is being used
	{
		param = getInitialConditionParameter();
		
	}else if (index < getParameters().length){
		param = getParameters()[index]; //using iniAmount || other parameters now is one index greater than previous index(since iniAmount has added) 
	}else{
		param = getProxyParameters()[index-getParameters().length];
	}
	return param;
}

public SpeciesContextSpecProxyParameter getProxyParameter(String pName){
	if (fieldProxyParameters == null){
		return null;
	}
	for (int i=0;i<fieldProxyParameters.length;i++){
		SpeciesContextSpecProxyParameter parm = fieldProxyParameters[i];
		if (pName.equals(parm.getName())){
			return parm;
		}
	}
	return null;
}

public SpeciesContextSpecProxyParameter[] getProxyParameters() {
	return fieldProxyParameters;
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
 * Creation date: (4/3/2004 10:54:49 AM)
 * @return int
 * @param boundaryLocation cbit.vcell.mapping.BoundaryLocation
 */
private int getRole(BoundaryLocation boundaryLocation) {
	int role = -1;
	switch (boundaryLocation.getNum()){
		case BoundaryLocation.BOUNDARY_XM:{
			role = ROLE_BoundaryValueXm;
			break;
		}
		case BoundaryLocation.BOUNDARY_XP:{
			role = ROLE_BoundaryValueXp;
			break;
		}
		case BoundaryLocation.BOUNDARY_YM:{
			role = ROLE_BoundaryValueYm;
			break;
		}
		case BoundaryLocation.BOUNDARY_YP:{
			role = ROLE_BoundaryValueYp;
			break;
		}
		case BoundaryLocation.BOUNDARY_ZM:{
			role = ROLE_BoundaryValueZm;
			break;
		}
		case BoundaryLocation.BOUNDARY_ZP:{
			role = ROLE_BoundaryValueZp;
			break;
		}
	}
	return role;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.model.SpeciesContext
 */
public SpeciesContext getSpeciesContext() {
	return speciesContext;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getVCML() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\t"+VCMODL.SpeciesContextSpec+" "+getSpeciesContext().getName()+" {\n");
	buffer.append("\t\t"+VCMODL.ForceConstant+" "+isConstant()+"\n");
	buffer.append("\t\t"+VCMODL.EnableDiffusion+" "+isDiffusing()+"\n");
	Expression init = getInitialConditionParameter().getExpression();
	if (init!=null){
		buffer.append("\t\t"+VCMODL.InitialConcentration+" "+init.toString()+";\n");
	}
	Expression diffRate = getDiffusionParameter().getExpression();
	if (diffRate!=null){
		buffer.append("\t\t"+VCMODL.DiffusionRate+" "+diffRate.toString()+";\n");
	}
	//
	// write BoundaryConditions
	//
	for (int i=BoundaryLocation.FIRST;i<=BoundaryLocation.LAST;i++){
		BoundaryLocation bl = BoundaryLocation.fromDirection(i);
		Expression boundExp = getParameterFromRole(getRole(bl)).getExpression();
		if (boundExp!=null){
			buffer.append("\t\t"+VCMODL.BoundaryCondition+" "+bl.toString()+" "+boundExp.toString()+"\n");
		}
	}
	// write velocity x,y,z expressions
	Expression vel_x = getVelocityXParameter().getExpression();
	if (vel_x!=null){
		buffer.append("\t\t"+VCMODL.VelocityX+" "+vel_x.toString()+";\n");
	}
	Expression vel_y = getVelocityYParameter().getExpression();
	if (vel_y!=null){
		buffer.append("\t\t"+VCMODL.VelocityY+" "+vel_y.toString()+";\n");
	}
	Expression vel_z = getVelocityZParameter().getExpression();
	if (vel_z!=null){
		buffer.append("\t\t"+VCMODL.VelocityZ+" "+vel_z.toString()+";\n");
	}

	buffer.append("\t}\n");
	return buffer.toString();		
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
 * The hasListeners method was generated to support the vetoPropertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getVetoPropertyChange().hasListeners(propertyName);
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isConstant() {
	return bConstant;
}


/**
 * @return boolean
 */
public boolean isDiffusing() {
	if (bConstant){
		return false;
	}
	
	if (bWellMixed){
		return false;
	}
	
	try {
		double constantDiffRate = getDiffusionParameter().getExpression().evaluateConstant();
		return (constantDiffRate>0.0);
	}catch (ExpressionException e){
	}

	//
	// can't evaluate to constant, so must be non-zero
	//
	return true;
}

public boolean isAdvecting() {
	if (bWellMixed || bConstant==true){
		return false;
	}
	// If all 3 velocity (x,y,z) parameters are null, there is no advection; return <FALSE>, else return <TRUE>
	Expression eX = getVelocityXParameter().getExpression();
	Expression eY = getVelocityYParameter().getExpression();
	Expression eZ = getVelocityZParameter().getExpression();
	if (eX == null && eY == null && eZ == null) {
		return false;
	}
	return true;
}

///**
// * @return boolean
// * @deprecated (no longer used)
// */
//public final boolean isEnableDiffusing() {
//	return bEnableDiffusing;
//}

public final Boolean isWellMixed(){
	return bWellMixed;
}

/**
 * This method was created in VisualAge.
 * @return boolean
 * @param parm cbit.vcell.model.Parameter
 */
private boolean isReferenced(Parameter parm) throws ModelException, ExpressionException {

	if (fieldParameters==null){
		return false;
	}
	
	if (parm instanceof SpeciesContextSpec.SpeciesContextSpecParameter){
		SpeciesContextSpecParameter scsParm = (SpeciesContextSpecParameter)parm;
		for (int i = 0; i < fieldParameters.length; i++) {
			if (fieldParameters[i] == parm){
				return true;
			}
		}
		return false;
	} else if (parm instanceof SpeciesContextSpecProxyParameter){
		if (fieldParameters != null){
			for (int i=0;i<fieldParameters.length;i++){
				Expression exp = fieldParameters[i].getExpression();
				if (exp!=null){
					String[] symbols = exp.getSymbols();
					for (int j=0;symbols!=null && j<symbols.length;j++){
						if (AbstractNameScope.getStrippedIdentifier(symbols[j]).equals(parm.getName())){
							return true;
						}
					}
				}
			}
		}
	}
	return false;
}

/**
 * This method was created in VisualAge.
 */
private final void cleanupParameters() throws ModelException, ExpressionException, PropertyVetoException {
	//
	// for each parameter, see if it is used, if not delete it
	//
	if (fieldProxyParameters != null){
		for (int i=0;i<fieldProxyParameters.length;i++){
			if (!isReferenced(fieldProxyParameters[i])){
				removeProxyParameter(fieldProxyParameters[i]);
				i--;
			}
		}
	}
}

/**
 */
public void refreshDependencies() {
	for (int i = 0; i < fieldParameters.length; i++){
		try {
			if (fieldParameters[i].getExpression()!=null){
				fieldParameters[i].getExpression().bindExpression(this);
			}
		}catch (ExpressionException e){
			System.out.println("error binding expression '"+fieldParameters[i].getExpression().infix()+"', "+e.getMessage());
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (4/3/2004 11:13:04 AM)
 * @param parameter cbit.vcell.mapping.SpeciesContextSpec.SpeciesContextSpecParameter
 */
private void removeParameter(SpeciesContextSpec.SpeciesContextSpecParameter parameter) {}

protected void removeProxyParameter(SpeciesContextSpecProxyParameter parameter) {
	for (int i = 0; i < fieldProxyParameters.length; i++){
		if (fieldProxyParameters[i] == parameter){
			SpeciesContextSpecProxyParameter newProxyParameters[] = (SpeciesContextSpecProxyParameter[])BeanUtils.removeElement(fieldProxyParameters,parameter);
			setProxyParameters(newProxyParameters);
			return;
		}
	}
	throw new RuntimeException(parameter.getName()+"' not found");
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
 * The removeVetoableChangeListener method was generated to support the vetoPropertyChange field.
 */
public synchronized void removeVetoableChangeListener(java.lang.String propertyName, java.beans.VetoableChangeListener listener) {
	getVetoPropertyChange().removeVetoableChangeListener(propertyName, listener);
}


/**
 */
private void resetDefaults() {
	bConstant = DEFAULT_CONSTANT;
//	if (getSpeciesContext().getStructure() instanceof Feature || getSpeciesContext().getStructure() instanceof Membrane){
//		bEnableDiffusing = DEFAULT_ENABLE_DIFFUSING;
//	}else{
//		bEnableDiffusing = false;
//	}
	bWellMixed = DEFAULT_WELL_MIXED;
}


/**
 * @param isConstant boolean
 */
public void setConstant(boolean isConstant) {
	boolean oldDiffusing = isDiffusing();
	boolean oldConstant = bConstant;
	this.bConstant = isConstant;
	firePropertyChange("constant",new Boolean(oldConstant), new Boolean(isConstant));
	firePropertyChange("diffusing", new Boolean(oldDiffusing), new Boolean(isDiffusing()));
}

///**
// * @param isDiffusing boolean
// * @deprecated
// */
//public void setEnableDiffusing(boolean isEnableDiffusing) throws MappingException {
//	boolean oldEnableDiffusing = bEnableDiffusing;
//
//	this.bEnableDiffusing = isEnableDiffusing;
//
//	firePropertyChange("enableDiffusing",new Boolean(oldEnableDiffusing), new Boolean(isEnableDiffusing));
//}


public void setWellMixed(boolean bWellMixed) {
	Boolean oldValue = this.bWellMixed;
	this.bWellMixed = bWellMixed;
	firePropertyChange(PROPERTY_NAME_WELL_MIXED,oldValue, bWellMixed);
}


/**
 * Sets the parameters property (cbit.vcell.model.Parameter[]) value.
 * @param parameters The new value for the property.
 * @exception java.beans.PropertyVetoException The exception description.
 * @see #getParameters
 */
private void setParameters(SpeciesContextSpecParameter[] parameters) throws java.beans.PropertyVetoException {
	SpeciesContextSpecParameter[] oldValue = fieldParameters;
	fireVetoableChange("parameters", oldValue, parameters);
	fieldParameters = parameters;
	try {
		cleanupParameters();
	} catch (ModelException e) {
		e.printStackTrace();
	} catch (ExpressionException e) {
		e.printStackTrace();
	}
	firePropertyChange("parameters", oldValue, parameters);
}

private void setProxyParameters(SpeciesContextSpecProxyParameter[] proxyParameters) {
	SpeciesContextSpecProxyParameter[] oldValue = fieldProxyParameters;
	fieldProxyParameters = proxyParameters;
	firePropertyChange(PARAMETER_NAME_PROXY_PARAMETERS, oldValue, proxyParameters);
}


/**
 * Insert the method's description here.
 * Creation date: (4/23/2004 7:25:49 AM)
 * @param newSimulationContext cbit.vcell.mapping.SimulationContext
 */
public void setSimulationContext(SimulationContext newSimulationContext) {
	simulationContext = newSimulationContext;
}


/**
 * @param sc cbit.vcell.model.SpeciesContext
 */
void setSpeciesContextReference(SpeciesContext sc) throws MappingException {
	
	if (this.speciesContext == sc){
		return;
	}
	
	if (sc.compareEqual(this.speciesContext)){
		this.speciesContext = sc;
	}else{
		throw new MappingException("replacing speciesContext that is not the same");
	}
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String toString() {
	StringBuffer sb = new StringBuffer();
	
	sb.append(getClass().getName()+"\n");
   if (speciesContext != null) { sb.append("speciesContext = '"+speciesContext+"'\n"); }
 	
	return sb.toString();
}

private double computeStructureSize() throws ExpressionException, MappingException {
	Structure structure = getSpeciesContext().getStructure();
	StructureMapping sm = getSimulationContext().getGeometryContext().getStructureMapping(structure);
	double structSize = 0;
	if (getSimulationContext().getGeometry().getDimension() == 0) {
		Expression sizeParameterExpression = sm.getSizeParameter().getExpression();
		if (sizeParameterExpression == null || sizeParameterExpression.isZero()) {
			throw new RuntimeException("\nIn application '" + getSimulationContext().getName() + "', " +
				"size of structure '" + structure.getName() + "' is required to convert " +
				"concentration to number of particles.\n\nPlease go to 'Structure Mapping' tab to check the size.");
		}
		structSize = sizeParameterExpression.evaluateConstant();
	} else {
		StructureMapping structureMapping = getSimulationContext().getGeometryContext().getStructureMapping(structure);
		GeometryClass gc = structureMapping.getGeometryClass();
		if (gc == null) {
			throw new MappingException("model structure '" + structure.getName()+ "' not mapped to a geometry subdomain");
		}
		
		try {
			GeometricRegion[] geometricRegions = getSimulationContext().getGeometry().getGeometrySurfaceDescription().getGeometricRegions(gc);
			if (geometricRegions == null) {
				throw new MappingException("Geometry is not updated.");
			}
			
			for (GeometricRegion gr : geometricRegions) {
				structSize += gr.getSize();
			}
		} catch (Exception ex) {
			
		}
	}
	return structSize;
}

public Expression convertConcentrationToParticles(Expression iniConcentration) throws ExpressionException, MappingException
{
	Expression iniParticlesExpr = null; 
	Structure structure = getSpeciesContext().getStructure();
	double structSize = computeStructureSize();
	
	if (structure instanceof Membrane) {
		// convert concentration(particles/area) to number of particles
		// particles = iniConcentration(molecules/um2)*size(um2)
		try {
			iniParticlesExpr = new Expression((Math.round(iniConcentration.evaluateConstant()* structSize)));
		} catch (ExpressionException e) {
			iniParticlesExpr = Expression.mult(iniConcentration, new Expression(structSize)).flatten();
		}
	} else {
		// convert concentration(particles/volume) to number of particles
		// particles = [iniConcentration(uM)*size(um3)]/KMOLE
		try {
			iniParticlesExpr = new Expression((Math.round(iniConcentration.evaluateConstant() * structSize / ReservedSymbol.KMOLE.getExpression().evaluateConstant())));
		} catch (ExpressionException e) {
			Expression numeratorExpr = Expression.mult(iniConcentration, new Expression(structSize));
			Expression denominatorExpr = new Expression(ReservedSymbol.KMOLE.getExpression().evaluateConstant());
			iniParticlesExpr = Expression.div(numeratorExpr, denominatorExpr).flatten();
		}
	}
	
	return iniParticlesExpr;
}

public Expression convertParticlesToConcentration(Expression iniParticles) throws ExpressionException, MappingException
{
	Expression iniConcentrationExpr = null;
	Structure structure = getSpeciesContext().getStructure();
	double structSize = computeStructureSize();
	
	if (structure instanceof Membrane) {
		// convert number of particles to concentration(particles/area)
		// iniConcentration(molecules/um2) = particles/size(um2)
		try {
			iniConcentrationExpr = new Expression((iniParticles.evaluateConstant() * 1.0) / structSize); 
		} catch (ExpressionException e) {
			iniConcentrationExpr = Expression.div(iniParticles, new Expression(structSize)).flatten();
		}
	} else {
		// convert concentration(particles/volume) to number of particles
		// particles = [iniParticles(uM)/size(um3)]*KMOLE
		try {
			iniConcentrationExpr = new Expression((iniParticles.evaluateConstant()*ReservedSymbol.KMOLE.getExpression().evaluateConstant() / structSize));
		} catch (ExpressionException e) {
			Expression numeratorExpr = Expression.mult(iniParticles, new Expression(ReservedSymbol.KMOLE.getExpression().evaluateConstant()));
			Expression denominatorExpr = new Expression(structSize);
			iniConcentrationExpr = Expression.div(numeratorExpr, denominatorExpr).flatten();
		}
	}
	return iniConcentrationExpr;
}

public SimulationContext getSimulationContext() {
	return simulationContext;
}

public void getLocalEntries(Map<String, SymbolTableEntry> entryMap) {	
	for (SymbolTableEntry ste : fieldProxyParameters) {
		entryMap.put(ste.getName(), ste);
	}
	for (SymbolTableEntry ste : fieldParameters) {
		entryMap.put(ste.getName(), ste);
	}
	ReservedBioSymbolEntries.getAll(entryMap);
}

public void getEntries(Map<String, SymbolTableEntry> entryMap) {
	getNameScope().getExternalEntries(entryMap);		
}

public boolean hasTransport() {
	if (isConstant() || isWellMixed() || simulationContext == null 
			|| simulationContext.getGeometry() == null || simulationContext.getGeometry().getDimension() == 0) {
		return false;
	}
	int dimension = simulationContext.getGeometry().getDimension();
	SpeciesContext speciesContext = getSpeciesContext();
	if (speciesContext.getStructure() instanceof Membrane){
		if (dimension > 1 && !getDiffusionParameter().getExpression().isZero()) {
			return true;
		}			
	} else if (speciesContext.getStructure() instanceof Feature){
		if (!getDiffusionParameter().getExpression().isZero()) {
			return true;
		}
	
		if (getVelocityXParameter().getExpression() != null && !getVelocityXParameter().getExpression().isZero()) {
			return true;
		}
		if (dimension > 1) {
			if (getVelocityYParameter().getExpression() != null && !getVelocityYParameter().getExpression().isZero()) {
				return true;
			}						
		
			if (dimension > 2) {
				if (getVelocityZParameter().getExpression() != null && !getVelocityZParameter().getExpression().isZero()) {
					return true;
				}
			}
		}						
	}	
	return false;
}

public List<SpeciesContextSpecParameter> computeApplicableParameterList() {
	List<SpeciesContextSpecParameter> speciesContextSpecParameterList = new ArrayList<SpeciesContextSpecParameter>();
	speciesContextSpecParameterList.add(getInitialConditionParameter());
	int dimension = simulationContext.getGeometry().getDimension();
	if (!isConstant() && !isWellMixed() && dimension > 0) {
		// diffusion
		speciesContextSpecParameterList.add(getDiffusionParameter());
	}
	if (hasTransport()) {
		SpeciesContext speciesContext = getSpeciesContext();
		if (speciesContext.getStructure() instanceof Membrane) {
			
			// boundary condition
			speciesContextSpecParameterList.add(getBoundaryXmParameter());
			speciesContextSpecParameterList.add(getBoundaryXpParameter());
			speciesContextSpecParameterList.add(getBoundaryYmParameter());
			speciesContextSpecParameterList.add(getBoundaryYpParameter());
	
			if (dimension > 2) {
				speciesContextSpecParameterList.add(getBoundaryZmParameter());
				speciesContextSpecParameterList.add(getBoundaryZpParameter());
			}
			
		} else if (speciesContext.getStructure() instanceof Feature){						
			// boundary condition
			speciesContextSpecParameterList.add(getBoundaryXmParameter());
			speciesContextSpecParameterList.add(getBoundaryXpParameter());
						 
			if (dimension > 1) {
				speciesContextSpecParameterList.add(getBoundaryYmParameter());
				speciesContextSpecParameterList.add(getBoundaryYpParameter());
			
			
				if (dimension > 2) {
					speciesContextSpecParameterList.add(getBoundaryZmParameter());
					speciesContextSpecParameterList.add(getBoundaryZpParameter());
				}
			}
			
			// velocity
			speciesContextSpecParameterList.add(getVelocityXParameter());
			if (dimension > 1) {
				speciesContextSpecParameterList.add(getVelocityYParameter());
			
				if (dimension > 2) {
					speciesContextSpecParameterList.add(getVelocityZParameter());
				}
			}
		}
	}
	return speciesContextSpecParameterList;
}
}