/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.models.bpimport.table.options;

/*   CellResourceOption  --- by Oliver Ruebenacker, UCHC --- July 2009 to November 2009
 *   An option to choose from in a cell, representing a Resource
 */

import org.vcell.sybil.models.sbbox.SBBox;

public class CellThingOption<T extends SBBox.NamedThing> extends CellSelectableOption implements CellOption {

	protected T thing;

	public CellThingOption(T thing) { this.thing = thing; }
	
	public T thing() { return thing; }
	public String label() { return thing.label(); }
	@Override
	public String toString() { return thing.label(); }
	@Override
	public boolean equals(Object o) {
		if(o instanceof CellThingOption<?>) {
			return thing.equals(((CellThingOption<?>) o).thing());
		}
		return false;
	}
	
	@Override
	public int hashCode() { return thing.hashCode(); }

}
