package org.vcell.util.document;

import java.io.Serializable;
import java.rmi.dgc.VMID;


public class VCDataJobID implements Serializable{

	private VMID jobID;
	private User jobOwner;
	private boolean isBackgroundTask;
	
	private VCDataJobID(VMID jobID, User jobOwner, boolean isBackgroundTask) {
		this.jobID = jobID;
		this.jobOwner = jobOwner;
		this.isBackgroundTask = isBackgroundTask;
	}

	public static VCDataJobID createVCDataJobID(User argJobOwner,boolean argIsBackgroundTask){
		return new VCDataJobID(new VMID(),argJobOwner,argIsBackgroundTask);
	}
	public boolean isBackgroundTask() {
		return isBackgroundTask;
	}

	public VMID getJobID() {
		return jobID;
	}

	public User getJobOwner() {
		return jobOwner;
	}

	@Override
	public String toString() {
		return "own="+jobOwner.toString()+":id="+jobID+":bg="+isBackgroundTask;
	}

	@Override
	public boolean equals(Object obj) {
		return
			(obj instanceof VCDataJobID)
			&&
			getJobID().equals(((VCDataJobID)obj).getJobID());
	}

	@Override
	public int hashCode() {
		return jobID.hashCode();
	}
}
