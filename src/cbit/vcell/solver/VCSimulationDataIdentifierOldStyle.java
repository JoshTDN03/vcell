package cbit.vcell.solver;

import org.vcell.util.document.KeyValue;

import cbit.vcell.field.SimResampleInfoProvider;

/**
 * Temporary, to help SimulationData to deal with multiple simulation job datasets.
 * Should be removed after making SimulationData smarter in file handling for compatibility with old stuff.
 * @deprecated
 */
public class VCSimulationDataIdentifierOldStyle
	implements
		java.io.Serializable, org.vcell.util.VCDataIdentifier,
		SimResampleInfoProvider{


	public KeyValue getSimulationKey() {
		return vcSimID.getSimulationKey();
	}


	public boolean isParameterScanType() {
		return false;
	}


	private VCSimulationIdentifier vcSimID = null;

/**
 * VCSimulationIdentifier constructor comment.
 */
private VCSimulationDataIdentifierOldStyle(VCSimulationDataIdentifier vcSimDataID) {
	this.vcSimID = vcSimDataID.getVcSimID();
}


/**
 * Insert the method's description here.
 * Creation date: (10/18/2005 3:28:49 PM)
 * @return cbit.vcell.solver.VCSimulationDataIdentifierOldStyle
 * @param vcSimDataID cbit.vcell.solver.VCSimulationDataIdentifier
 * @deprecated
 */
public static VCSimulationDataIdentifierOldStyle createVCSimulationDataIdentifierOldStyle(VCSimulationDataIdentifier vcSimDataID) {
	return new VCSimulationDataIdentifierOldStyle(vcSimDataID);
}


/**
 * Insert the method's description here.
 * Creation date: (8/24/2004 10:56:16 AM)
 * @return boolean
 * @param object java.lang.Object
 * @deprecated
 */
public boolean equals(Object object) {
	if (object instanceof VCSimulationDataIdentifierOldStyle){
		if (((VCSimulationDataIdentifierOldStyle)object).getID().equals(getID())){
			return true;
		}
	}
	return false;
}


/**
 * Insert the method's description here.
 * Creation date: (8/24/2004 2:07:52 PM)
 * @return java.lang.String
 * @deprecated
 */
public java.lang.String getID() {
	return Simulation.createSimulationID(vcSimID.getSimulationKey());
}

public int getJobIndex() {
	return 0;
}


/**
 * Insert the method's description here.
 * Creation date: (8/24/2004 11:12:39 AM)
 * @return cbit.vcell.server.User
 * @deprecated
 */
public org.vcell.util.document.User getOwner() {
	return vcSimID.getOwner();
}


/**
 * Insert the method's description here.
 * Creation date: (1/25/01 12:28:06 PM)
 * @return int
 * @deprecated
 */
public int hashCode() {
	return toString().hashCode();
}


/**
 * Insert the method's description here.
 * Creation date: (8/24/2004 1:12:48 PM)
 * @return java.lang.String
 * @deprecated
 */
public String toString() {
	return "VCSimulationIdentifierOldStyle["+vcSimID.getSimulationKey()+","+getOwner()+"]";
}
}