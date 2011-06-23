package org.vcell.sybil.models.miriam;

/*   RefGroup  --- by Oliver Ruebenacker, UCHC --- March 2010
 *   A group of MIRIAM references (data type plus value, e.g. UniProt P00533)
 */

import java.util.Set;

import org.openrdf.model.Resource;

public interface RefGroup {
	public RefGroup add(MIRIAMRef ref);
	public RefGroup remove(MIRIAMRef ref);
	public boolean contains(MIRIAMRef ref);
	public Set<MIRIAMRef> refs();
	public RefGroup removeAll();
	public void delete();
	public Resource resource();
}