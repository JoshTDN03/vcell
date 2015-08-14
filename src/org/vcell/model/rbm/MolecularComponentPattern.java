package org.vcell.model.rbm;

import java.util.List;

import org.vcell.model.rbm.SpeciesPattern.Bond;
import org.vcell.util.Compare;
import org.vcell.util.Displayable;
import org.vcell.util.Issue;
import org.vcell.util.Issue.IssueCategory;
import org.vcell.util.Issue.IssueSource;
import org.vcell.util.IssueContext;
import org.vcell.util.Matchable;


@SuppressWarnings("serial")
public class MolecularComponentPattern extends RbmElementAbstract implements Matchable, IssueSource,
	Displayable
{
	public static final String PROPERTY_NAME_COMPONENT_STATE = "componentStatePattern";
	public static final String PROPERTY_NAME_BOND_TYPE = "bondType";
	public static final String PROPERTY_NAME_BOND_ID = "bondId";
	public static final String PROPERTY_NAME_VISIBLE = "bondId";
	
	private MolecularComponent molecularComponent;
	private ComponentStatePattern componentStatePattern;
	private boolean bVisible = false;
	private Bond bond = null;
	private int bondId = -1;                // used in BNGL for mapping notation (e.g. 1, 2, 3)
	private BondType bondType = BondType.Possible;
	
	public enum BondType {
		Specified(""), // numbers
		Exists("+"),    // "+"
		Possible("?"),  // "?"
		None("-");  	   //
		
		public String symbol;
		BondType(String s) {
			this.symbol = s;
		}
		
		public static BondType fromSymbol(String symbol) throws NumberFormatException {
			for (BondType bondType : values()){
				if (bondType.symbol.equals(symbol)){
					return bondType;
				}
			}
			int bondInt = Integer.parseInt(symbol);		// not used, just for debugging
			return Specified;
		}
	}
	
	
//	public String getId() {
//		System.err.println("MolecularComponentPattern id generated badly");
//		return "MolecularComponentPattern_"+hashCode();
//	}

	public MolecularComponentPattern(MolecularComponent molecularComponent) {
		super();
		this.molecularComponent = molecularComponent;
		if(!molecularComponent.getComponentStateDefinitions().isEmpty()) {
			componentStatePattern = new ComponentStatePattern();
		}
	}
	
	public MolecularComponentPattern(MolecularComponentPattern mcp) {
		super();
		this.molecularComponent = mcp.getMolecularComponent();
		this.componentStatePattern = mcp.getComponentStatePattern();	// TODO: here we get lazy and don't create new state
	}

	public  boolean isFullyDefined(){
		return !componentStatePattern.isAny();
	}

	public final MolecularComponent getMolecularComponent() {
		return molecularComponent;
	}

	public final ComponentStatePattern getComponentStatePattern() {
		return componentStatePattern;
	}

	public final void setComponentStatePattern(ComponentStatePattern newValue) {
		if(componentStatePattern == newValue) {
			return;
		}
		ComponentStatePattern oldValue = componentStatePattern;
		this.componentStatePattern = newValue;
		firePropertyChange(PROPERTY_NAME_COMPONENT_STATE, oldValue, newValue);
		setVisible(!isImplied());
	}

	public final BondType getBondType() {
		return bondType;
	}

	public final int getBondId() {
		return bondId;
	}
	
	public final void setBondType(BondType newValue) {
		BondType oldValue = bondType;
		this.bondType = newValue;		
		firePropertyChange(PROPERTY_NAME_BOND_TYPE, oldValue, newValue);
		setVisible(!isImplied());
	}

	public final void setBondId(int newValue) {
		this.bondId = newValue;
		setBondType(BondType.Specified);
		setVisible(!isImplied());
	}
	
	public final Bond getBond() {
		return bond;
	}

	public final void setBond(Bond bond) {
		this.bond = bond;
	}

	@Override
	public String toString() {
		return RbmUtils.toBnglString(this);
	}
	
	public boolean isImplied() {
		return (componentStatePattern == null || componentStatePattern.isAny()) && bondType == BondType.Possible;
	}

	public final boolean isbVisible() {
		return bVisible;
	}

	public final void setVisible(boolean newValue) {
		boolean oldValue = bVisible;
		this.bVisible = newValue;
		firePropertyChange(PROPERTY_NAME_VISIBLE, oldValue, newValue);
	}
	
	@Override
	public boolean compareEqual(Matchable aThat) {
		if (this == aThat) {
			return true;
		}
		if (!(aThat instanceof MolecularComponentPattern)) {
			return false;
		}
		MolecularComponentPattern that = (MolecularComponentPattern)aThat;
		
		if (!Compare.isEqual(molecularComponent, that.molecularComponent)) {
			return false;
		}
		if (!Compare.isEqual(componentStatePattern, that.componentStatePattern)) {
			return false;
		}
		if(!(bVisible == that.bVisible)) {
			return false;
		}
		// we don't compare the bonds since they can be recomputed at any time from the molecular component pattern
//		if (!(Compare.isEqualOrNull(bond,that.bond))) {
//			return false;
//		}
		if (!Compare.isEqual(bondType, that.bondType)) {
			return false;
		}
		return true;
	}

	@Override
	public void gatherIssues(IssueContext issueContext, List<Issue> issueList) {
		if(molecularComponent == null) {
			issueList.add(new Issue(this, issueContext, IssueCategory.Identifiers, MolecularComponent.typeName + " of " + typeName + "  '" + toString() + "' is null", Issue.SEVERITY_INFO));
		} else {
			//molecularComponent.gatherIssues(issueContext, issueList);	// we call this somewhere else already
		}
		if(componentStatePattern == null) {
			issueList.add(new Issue(this, issueContext, IssueCategory.Identifiers, "State of " + typeName + "  '" + toString() + "' is null", Issue.SEVERITY_INFO));
		} else {
			//componentStatePattern.gatherIssues(issueContext, issueList);		// we call this somewhere else already
		}
	}

	public static final String typeName = "Site";		// normally would be site pattern but we try to simplify and get rid of the "pattern" part
	@Override
	public String getDisplayName() {
		String name = toString();
		if(name == null) {
			return "";
		} else {
			return name;
		}
	}
	@Override
	public String getDisplayType() {
		return typeName;
	}
	
}
