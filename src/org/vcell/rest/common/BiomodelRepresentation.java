package org.vcell.rest.common;

import java.util.ArrayList;

import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;

import cbit.vcell.modeldb.BioModelRep;

public class BiomodelRepresentation {
	
	public String bmKey;
	
	public String name;
	
	public int privacy;
	
	public String[] groupUsers;
	
	public Long savedDate;
	
	public String annot;
	
	public String branchID;
	
	public String modelKey;
	
	public String ownerName;
	
	public String ownerKey;
	
	public String[] simKeys;
	
	public String[] simContextKeys;

	public BiomodelRepresentation(){
		
	}
	
	
	
	public String getBmKey() {
		return bmKey;
	}



	public String getName() {
		return name;
	}



	public int getPrivacy() {
		return privacy;
	}



	public String[] getGroupUsers() {
		return groupUsers;
	}



	public Long getSavedDate() {
		return savedDate;
	}



	public String getAnnot() {
		return annot;
	}



	public String getBranchID() {
		return branchID;
	}



	public String getModelKey() {
		return modelKey;
	}



	public String getOwnerName() {
		return ownerName;
	}



	public String getOwnerKey() {
		return ownerKey;
	}



	public String[] getSimKeys() {
		return simKeys;
	}



	public String[] getSimContextKeys() {
		return simContextKeys;
	}



	public BiomodelRepresentation(BioModelRep bioModelRep){
		this.bmKey = bioModelRep.getBmKey().toString();
		this.name = bioModelRep.getName();
		this.privacy = bioModelRep.getPrivacy();
		
		ArrayList<String> groupList = new ArrayList<String>();
		for (User user : bioModelRep.getGroupUsers()) {
			groupList.add(user.getName());
		}
		this.groupUsers = groupList.toArray(new String[groupList.size()]);
		
		this.savedDate = bioModelRep.getDate().getTime();

		this.annot = bioModelRep.getAnnot();
		this.branchID = bioModelRep.getBranchID().toString();
		this.modelKey = bioModelRep.getModelRef().toString();
		this.ownerName = bioModelRep.getOwner().getName();
		this.ownerKey = bioModelRep.getOwner().getID().toString();

		ArrayList<String> simKeyList = new ArrayList<String>();
		for (KeyValue simKey : bioModelRep.getSimKeyList()){
			simKeyList.add(simKey.toString());
		}
		this.simKeys = simKeyList.toArray(new String[simKeyList.size()]);

		ArrayList<String> simContextKeyList = new ArrayList<String>();
		for (KeyValue simContextKey : bioModelRep.getSimContextKeyList()){
			simContextKeyList.add(simContextKey.toString());
		}
		this.simContextKeys = simContextKeyList.toArray(new String[simContextKeyList.size()]);
	}
}
