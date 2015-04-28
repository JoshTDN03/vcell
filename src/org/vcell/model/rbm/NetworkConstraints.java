package org.vcell.model.rbm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcell.util.Compare;
import org.vcell.util.Issue;
import org.vcell.util.IssueContext;
import org.vcell.util.Matchable;

@SuppressWarnings("serial")
public class NetworkConstraints extends RbmElementAbstract implements Matchable {
	public static final String PROPERTY_NAME_MAX_STOICHIOMETRY = "maxStoichiometry";
	public static final String PROPERTY_NAME_MAX_ITERATION = "maxIteration";
	public static final String PROPERTY_NAME_MOLECULES_PER_SPECIES = "maxMoleculesPerSpecies";
	
	private int maxIteration = 3;
	private int maxMoleculesPerSpecies = 10;
	private Map<MolecularType, Integer> maxStoichiometryMap = new HashMap<MolecularType, Integer>();
		
	public NetworkConstraints() {		
	}

	public final int getMaxIteration() {
		return maxIteration;
	}

	public final void setMaxIteration(int newValue) {
		int oldValue = maxIteration;
		this.maxIteration = newValue;
		firePropertyChange(PROPERTY_NAME_MAX_ITERATION, oldValue, newValue);
	}

	public final int getMaxMoleculesPerSpecies() {
		return maxMoleculesPerSpecies;
	}

	public final void setMaxMoleculesPerSpecies(int newValue) {
		int oldValue = maxMoleculesPerSpecies;
		this.maxMoleculesPerSpecies = newValue;
		firePropertyChange(PROPERTY_NAME_MOLECULES_PER_SPECIES, oldValue, newValue);
	}
	
	public void setMaxStoichiometry(MolecularType molecularType, Integer newValue) {
		Integer oldValue;
		if (newValue == null) {
			oldValue = maxStoichiometryMap.remove(molecularType);
		} else {
			oldValue = maxStoichiometryMap.get(molecularType);
			maxStoichiometryMap.put(molecularType, newValue);
		}
		firePropertyChange(PROPERTY_NAME_MAX_STOICHIOMETRY, oldValue, newValue);
	}
	
	public Map<MolecularType, Integer> getMaxStoichiometry() {
		return maxStoichiometryMap;
	}
	public Integer getMaxStoichiometry(MolecularType molecularType) {
		return maxStoichiometryMap.get(molecularType);
	}
	
	public void clear() {
		maxStoichiometryMap.clear();
		maxIteration = 3;
		maxMoleculesPerSpecies = 10;
	}
	
	@Override
	public boolean compareEqual(Matchable aThat) {
		if (this == aThat) {
			return true;
		}
		if (!(aThat instanceof NetworkConstraints)) {
			return false;
		}
		NetworkConstraints that = (NetworkConstraints)aThat;

		if (!Compare.isEqual(maxIteration, that.maxIteration)) {
			return false;
		}
		if (!Compare.isEqual(maxMoleculesPerSpecies, that.maxMoleculesPerSpecies)) {
			return false;
		}
		Map<MolecularType, Integer> thatMaxStoichiometryMap = new HashMap<MolecularType, Integer>(maxStoichiometryMap);
		for(Map.Entry<MolecularType, Integer> var1 : maxStoichiometryMap.entrySet()) {
			boolean found = false;
			for(Map.Entry<MolecularType, Integer> var2 : thatMaxStoichiometryMap.entrySet()) {
				if(Compare.isEqual(var1.getKey(),var2.getKey()) && Compare.isEqual(var1.getValue(),var2.getValue())) {
					found = true;
					thatMaxStoichiometryMap.remove(var2);
					break;
				}
			}
			if(found == false) {
				return false;
			}
		}
		if(!thatMaxStoichiometryMap.isEmpty()) {
			return false;
		}
		return true;
	}
	
	@Override
	public void gatherIssues(IssueContext issueContext, List<Issue> issueList) {
		// TODO Auto-generated method stub
	}

}
