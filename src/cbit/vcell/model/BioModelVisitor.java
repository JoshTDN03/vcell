/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.model;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.vcell.util.document.BioModelInfo;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.MathModelInfo;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.GeometryInfo;
import cbit.vcell.mathmodel.MathModel;
import cbit.vcell.modeldb.VCDatabaseScanner;
import cbit.vcell.modeldb.VCDatabaseVisitor;

public class BioModelVisitor implements VCDatabaseVisitor {
	
	private Hashtable<KeyValue, BioModelInfo> bioModelInfoHash = new Hashtable<KeyValue, BioModelInfo>();
	private HashSet<KeyValue> unparsedBioModels = new HashSet<KeyValue>();
	
	public boolean filterBioModel(BioModelInfo bioModelInfo) {
		if (true){
			bioModelInfoHash.put(bioModelInfo.getVersion().getVersionKey(), bioModelInfo);
			unparsedBioModels.add(bioModelInfo.getVersion().getVersionKey());
			return true;
		}else{
			return false;
		}
	}

	public void visitBioModel(BioModel bioModel, PrintStream logFilePrintStream) {
		KeyValue currentKey = bioModel.getVersion().getVersionKey();
		logFilePrintStream.append("======SUCEEDED IN READING BIOMODEL with key = "+currentKey.toString()+"\n");
		unparsedBioModels.remove(currentKey);
		
		for (Iterator<KeyValue> iterator = unparsedBioModels.iterator(); iterator.hasNext();) {
			KeyValue key = iterator.next();
			logFilePrintStream.append("======FAILED TO READ BIOMODEL : " + bioModelInfoHash.get(key)+"\n");
			iterator.remove();
		}
		return;
	}

	public boolean filterGeometry(GeometryInfo geometryInfo) {
		return false;
	}

	public boolean filterMathModel(MathModelInfo mathModelInfo) {
		return false;
	}

	public void visitGeometry(Geometry geometry, PrintStream logFilePrintStream) {
	}

	public void visitMathModel(MathModel mathModel,	PrintStream logFilePrintStream) {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BioModelVisitor visitor = new BioModelVisitor();
		boolean bAbortOnDataAccessException = false;
		try{
			VCDatabaseScanner.scanBioModels(args, visitor, bAbortOnDataAccessException);
		}catch(Exception e){
			e.printStackTrace(System.err);
		}finally{
			System.err.println("out of "+visitor.bioModelInfoHash.size()+" bioModels, "+visitor.unparsedBioModels.size()+" could not be read");
			System.err.flush();
		}
	}

}
