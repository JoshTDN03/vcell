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
import java.util.List;

import org.vcell.pathway.persistence.BiopaxProxy.RdfObjectProxy;
import org.vcell.util.Matchable;

public abstract class BioPaxObjectImpl implements BioPaxObject {
	public final static String spaces = "                                                                                                                                                                                                                                                                        ";
	private final static int MAX_DEPTH = 30;
	private String ID;
	private String about;
	private ArrayList<String> comments = new ArrayList<String>();
	private ArrayList<String> parserWarnings = new ArrayList<String>();
	
	public ArrayList<String> getComments() {
		return comments;
	}
	public void setComments(ArrayList<String> comments) {
		this.comments = comments;
	}

	public String getID() {
		return ID;
	}
	public void setID(String value) {
		this.ID = value;
	}
	public void setAbout(String about) {
		this.ID = about;
		this.about = about;
	}
	public String resourceFromID() {
		if (about != null){			// if we have an about we're sure to also have an ID
			return this.getID();	// ID and about are identical and represent an URL
		} else if(ID != null) {
			String resourceID = "#" + this.getID();		// just ID, "old" style
			return resourceID;
		}else{
			return ("#0");
//			throw new RuntimeException("BioPax Object does not have an ID"); 
		}
	}
	public boolean hasID() {
		if (ID != null){
			return true;
		}else{
			return false;
		}
	}

	public ArrayList<String> getParserWarnings() {
		return parserWarnings;
	}
	public void addParserWarning(String comment) {
		parserWarnings.add(comment);
	}

	public String getTypeLabel(){
		String typeName = getClass().getName();
		typeName = typeName.replace(getClass().getPackage().getName(),"");
		typeName = typeName.replace(".","");
		return typeName;
	}
	
	public void replace(RdfObjectProxy objectProxy, BioPaxObject concreteObject){
		// default implementation ... do nothing
	}
	public void replace(BioPaxObject keeperObject) {
		
	}
	public void replace(HashMap<String, BioPaxObject> resourceMap, HashSet<BioPaxObject> replacedBPObjects){
		
	}
	

	public String toString(){
		String suffix = "";
		if (this instanceof Entity){
			Entity entity = (Entity)this;
			if (entity.getName().size()>0){
				suffix = suffix + " : \""+entity.getName().get(0)+"\"";
			}
		}
		if (ID!=null){
			suffix = suffix + "  ID='"+ID+"'";
		}
		if (this instanceof RdfObjectProxy && ((RdfObjectProxy)this).getResource()!=null){
			suffix = suffix + "  resource='"+((RdfObjectProxy)this).getResource()+"'";
		}
		if (suffix.length()>0){
			suffix = suffix + " ";
		}
		return getTypeLabel()+suffix;
	}
	
	public final String getPad(int level){
		if (level==0){
			return "";
		}
		if (level>MAX_DEPTH){
			throw new RuntimeException("unchecked recursion in pathway.show()");
		}
		return spaces.substring(0, 2*level);
	}
	
	public final boolean fullCompare(HashSet<BioPaxObject> theirBiopaxObjects){
		for (BioPaxObject theirBpObject : theirBiopaxObjects){
			if(getID().equals(theirBpObject.getID())) {
				int level = 1;
				StringBuffer sbOurs = new StringBuffer();
				StringBuffer sbTheirs = new StringBuffer();
				
				showChildren(sbOurs, level);
				theirBpObject.showChildren(sbTheirs, level);
				if(sbOurs.toString().equals(sbTheirs.toString())) {
					return true;
				}
				return false;

			}
		}
		return false;
	}

	public boolean compareEqual(Matchable obj) {
		int level = 1;
		StringBuffer sbOurs = new StringBuffer();
		StringBuffer sbTheirs = new StringBuffer();
		
		showChildren(sbOurs, level);
		((BioPaxObject)obj).showChildren(sbTheirs, level);
		if(sbOurs.toString().equals(sbTheirs.toString())) {
			return true;
		}
		return false;
	}
	
	public final void show(StringBuffer sb){
		sb.append(getPad(0)+toString()+"\n");
//		System.out.print(getPad(0)+toString()+"\n");
		showChildren(sb,1);
	}

	public void showChildren(StringBuffer sb, int level) {
		for(String c : comments) {
			printString(sb,"comments",c,level);
		}
	}
	
	public void printString(StringBuffer sb, String name, String value, int level){
		if (name!=null && value!=null){
			sb.append(getPad(level)+name+" = "+value+"\n");
		}
	}

	public void printStrings(StringBuffer sb, String name, List<String> values, int level){
		if (name!=null && values!=null){
			for (String v : values){
				sb.append(getPad(level)+name+" = "+v+"\n");
			}
		}
	}

	public void printDoubles(StringBuffer sb, String name, List<Double> values, int level){
		if (name!=null && values!=null){
			for (Double v : values){
				sb.append(getPad(level)+name+" = "+v+"\n");
			}
		}
	}

	public void printObjects(StringBuffer sb,String name, List<? extends BioPaxObject> values, int level){
		if (name!=null && values!=null){
			for (BioPaxObject v : values){
				sb.append(getPad(level)+name+" = "+v.toString()+"\n");
				v.showChildren(sb,level+1);
			}
		}
	}

	public void printObject(StringBuffer sb, String name, BioPaxObject value, int level){
		if (name!=null && value!=null){
			sb.append(getPad(level)+name+" = "+value.toString()+"\n");
			value.showChildren(sb,level+1);
		}
	}

	public void printBoolean(StringBuffer sb, String name, Boolean value, int level){
		if (name!=null && value!=null){
			sb.append(getPad(level)+name+" = "+value.toString()+"\n");
		}
	}
	
	public void printDouble(StringBuffer sb, String name, Double value, int level){
		if (name!=null && value!=null){
			sb.append(getPad(level)+name+" = "+value.toString()+"\n");
		}
	}
	
	public void printInteger(StringBuffer sb, String name, Integer value, int level){
		if (name!=null && value!=null){
			sb.append(getPad(level)+name+" = "+value.toString()+"\n");
		}
	}
	
}
