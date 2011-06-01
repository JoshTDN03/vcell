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
import java.util.Vector;

import org.vcell.sybil.models.tree.NodeDataWrapper;
import org.vcell.sybil.util.http.pathwaycommons.search.XRef;
import org.vcell.sybil.util.text.NumberText;

public class XRefObsoleteListWrapper extends NodeDataWrapper<List<XRef>> {

	public XRefObsoleteListWrapper() {
		super(new Vector<XRef>());
	}

	public void add(XRef xRef) {
		data().add(xRef);
		append(new XRefWrapper(xRef));
	}
	
	public List<XRef> data() { return (List<XRef>) super.data(); }
	public List<XRef> xRefs() { return (List<XRef>) super.data(); }
	
	public String toString() {
		return NumberText.soMany(xRefs().size(), "obsolete cross reference");
	}
	
}
