package org.vcell.pathway;

import java.util.ArrayList;

import org.vcell.pathway.persistence.BiopaxProxy;
import org.vcell.pathway.persistence.BiopaxProxy.RdfObjectProxy;

public class Complex extends PhysicalEntity {
	private ArrayList<PhysicalEntity> component = new ArrayList<PhysicalEntity>();
	private ArrayList<Stoichiometry> componentStoichiometry = new ArrayList<Stoichiometry>();
	
	
	public ArrayList<PhysicalEntity> getComponents() {
		return component;
	}
	public ArrayList<Stoichiometry> getComponentStoichiometry() {
		return componentStoichiometry;
	}


	public void setComponent(ArrayList<PhysicalEntity> component) {
		this.component = component;
	}
	public void setComponentStoichiometry(
			ArrayList<Stoichiometry> componentStoichiometry) {
		this.componentStoichiometry = componentStoichiometry;
	}
	
	@Override
	public void replace(RdfObjectProxy objectProxy, BioPaxObject concreteObject){
		super.replace(objectProxy, concreteObject);

		for (int i=0; i<component.size(); i++) {
			PhysicalEntity thing = component.get(i);
			if(thing == objectProxy) {
				component.set(i, (PhysicalEntity)concreteObject);
			}
		}
		for (int i=0; i<componentStoichiometry.size(); i++) {
			Stoichiometry thing = componentStoichiometry.get(i);
			if(thing == objectProxy) {
				componentStoichiometry.set(i, (Stoichiometry)concreteObject);
			}
		}
	}
		
	public void showChildren(StringBuffer sb, int level){
		super.showChildren(sb,level);
		printObjects(sb,"component",component,level);
		printObjects(sb,"componentStoichiometry",componentStoichiometry,level);
	}

}
