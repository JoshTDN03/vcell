package org.vcell.uome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.sbpax.schemas.UOMECore;
import org.vcell.sybil.rdf.pool.UnsupportedRDFTypeException;
import org.vcell.uome.core.UOMEUnit;

public class UOMEUnitLists {

	public static List<UOMEUnit> getUnitList(List<URI> uris, UOMEUnitPool pool) {
		List<UOMEUnit> list = new ArrayList<UOMEUnit>();
		for(URI uri : uris) {
			try {
				list.add(pool.getOrCreateObject(uri, UOMECore.UnitOfMeasurement));
			} catch (UnsupportedRDFTypeException e) {
				e.printStackTrace();
			}
		}
		return list;		
	}
	
	public static Set<UOMEUnit> getUnits(Collection<URI> uris, UOMEUnitPool pool) {
		Set<UOMEUnit> units = new HashSet<UOMEUnit>();
		for(URI uri : uris) {
			try {
				units.add(pool.getOrCreateObject(uri, UOMECore.UnitOfMeasurement));
			} catch (UnsupportedRDFTypeException e) {
				e.printStackTrace();
			}
		}
		return units;		
	}
	
}
