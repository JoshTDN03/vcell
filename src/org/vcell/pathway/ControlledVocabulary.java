/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.pathway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.vcell.pathway.persistence.BiopaxProxy.RdfObjectProxy;

public class ControlledVocabulary extends BioPaxObjectImpl implements UtilityClass {
	private ArrayList<String> term = new ArrayList<String>();
	private ArrayList<Xref> xRef = new ArrayList<Xref>();
	
	public ArrayList<Xref> getxRef() {
		return xRef;
	}
	public void setxRef(ArrayList<Xref> xRef) {
		this.xRef = xRef;
	}

	public ArrayList<String> getTerm() {
		return term;
	}

	public void setTerm(ArrayList<String> term) {
		this.term = term;
	}

	@Override
	public void replace(RdfObjectProxy objectProxy, BioPaxObject concreteObject){

		for (int i=0; i<xRef.size(); i++) {
			Xref thing = xRef.get(i);
			if(thing == objectProxy) {
				xRef.set(i, (Xref)concreteObject);
			}
		}
	}
	
	public void replace(HashMap<String, BioPaxObject> resourceMap, HashSet<BioPaxObject> replacedBPObjects){
		for (int i=0; i<xRef.size(); i++) {
			Xref thing = xRef.get(i);
			if(thing instanceof RdfObjectProxy) {
				RdfObjectProxy rdfObjectProxy = (RdfObjectProxy)thing;
				if (rdfObjectProxy.getResource() != null){
					BioPaxObject concreteObject = resourceMap.get(rdfObjectProxy.getResourceName());
					if (concreteObject != null){
						xRef.set(i, (Xref)concreteObject);
					}
				}
			}
		}
	}

	public void showChildren(StringBuffer sb, int level){
		super.showChildren(sb,level);
		printStrings(sb,"term",term,level);
		printObjects(sb,"xRef",xRef,level);
	}

}
