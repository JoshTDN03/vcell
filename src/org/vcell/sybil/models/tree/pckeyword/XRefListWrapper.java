/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.models.tree.pckeyword;

/*   XRefListWrapper  --- by Oliver Ruebenacker, UCHC --- December 2009 to January 2010
 *   Wrapper for a tree node with a (Pathway Commons) XRef list
 */

import java.util.List;
import org.vcell.sybil.models.tree.NodeDataWrapper;
import org.vcell.sybil.util.http.pathwaycommons.search.PCKeywordResponse;
import org.vcell.sybil.util.http.pathwaycommons.search.XRef;
import org.vcell.sybil.util.text.NumberText;

public class XRefListWrapper extends NodeDataWrapper<List<XRef>> {

	protected XRefObsoleteListWrapper obsoletes;
	
	public XRefListWrapper(List<XRef> xRefs) {
		super(xRefs);
		for(XRef xRef : xRefs) { 
			if(obsolete(xRef)) {
				if(obsoletes == null) { obsoletes = new XRefObsoleteListWrapper(); }
				obsoletes.add(xRef);
			} else {
				append(new XRefWrapper(xRef)); 				
			}
		}
		if(obsoletes != null) { append(obsoletes); }
	}
	
	public boolean obsolete(XRef xRef) {
		if(xRef.db().equalsIgnoreCase("uniprot") &&
				PCKeywordResponse.uniProtBox().entryIsObsolete(xRef.id())) {
			return true;
		}
		return false;
	}

	public List<XRef> data() { return (List<XRef>) super.data(); }
	public List<XRef> xRefs() { return (List<XRef>) super.data(); }
	
	public String toString() {
		String string = NumberText.soMany(xRefs().size(), "cross reference");
		if(obsoletes != null) {
			string = string + " (" + NumberText.soMany(obsoletes.xRefs().size(), "obsolete one") + ")";
		}
		return string;
	}
	
}
