package cbit.vcell.model;

import java.util.List;

import org.vcell.model.rbm.MolecularType;
import org.vcell.model.rbm.SpeciesPattern;
import org.vcell.util.Compare;
import org.vcell.util.Displayable;
import org.vcell.util.Matchable;

import cbit.vcell.units.VCUnitDefinition;

public abstract class ReactionRuleParticipant implements Displayable, ModelProcessParticipant {
	private final SpeciesPattern speciesPattern;
	private Structure structure;
	
	public ReactionRuleParticipant(SpeciesPattern speciesPattern, Structure structure){
		this.speciesPattern = speciesPattern;
		this.structure = structure;
		if (structure == null){
			throw new RuntimeException("Null structure for ReactionRule Participant");
		}
	}
	
	public SpeciesPattern getSpeciesPattern(){
		return this.speciesPattern;
	}
	
	public Structure getStructure(){
		return this.structure;
	}
	public void setStructure(Structure structure){
		if (structure == null){
			throw new RuntimeException("Null structure for ReactionRule Participant");
		}
		this.structure = structure;
	}
	
	public VCUnitDefinition getUnitDefinition(){
		Model model = structure.getModel();
		if (model!=null){
			return model.getUnitSystem().getConcentrationUnit(structure);
		}
		System.err.println("ReactionRuleParticipant.getUnitDefinition() returned null, structure.getModel() for "+structure.getName()+" was null");
		return null;
	}

	public String calculateSignature(List<MolecularType> molecularTypeList) {
		String signature = structure.getName() + ":";
		signature += speciesPattern.calculateSignature(molecularTypeList);
		return signature;
	}
	
	public String getDisplayNameShort() {
		return getSpeciesPattern().getNameShort();
	}
	
	@Override
	public boolean compareEqual(Matchable obj) {
		if (getClass().equals(obj.getClass())){
			ReactionRuleParticipant other = (ReactionRuleParticipant)obj;
			if (!Compare.isEqual(speciesPattern, other.speciesPattern)){
				return false;
			}
			return true;
		}
		return false;
	}
	
	
}
