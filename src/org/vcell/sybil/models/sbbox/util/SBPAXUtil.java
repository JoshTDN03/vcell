/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.models.sbbox.util;

/*   SBPAXUtil  --- by Oliver Ruebenacker, UCHC --- October to December 2009
 *   Utilities for SBPAX, including some interfacing
 */

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.vcell.sybil.models.sbbox.SBBox;
import org.vcell.sybil.models.sbbox.SBBox.RDFType;
import org.vcell.sybil.rdf.schemas.SBPAX;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SBPAXUtil {
	
	public static List<RDFType> getSubstanceTypes(SBBox box) {
		List<RDFType> types = new Vector<RDFType>();
		for(Object typeNode : SBPAX.substanceClassList.asJavaList()) {
			if(typeNode instanceof Resource) {
				types.add(box.factories().typeFactory().create((Resource) typeNode));				
			}
		}
		return types;
	}
	
	public static Set<RDFType> getDefaultUSTs(SBBox box) {
		Set<RDFType> types = new HashSet<RDFType>();
		NodeIterator iterator = SBPAX.defaultUSTBag.iterator();
		while(iterator.hasNext()) {
			RDFNode typeNode = iterator.nextNode();
			if(typeNode instanceof Resource) {
				types.add(box.factories().typeFactory().create((Resource) typeNode));				
			}
		}
		return types;
	}
	
}
