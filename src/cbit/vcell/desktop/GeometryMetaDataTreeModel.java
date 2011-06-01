/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.desktop;
import cbit.image.VCImageInfo;
import cbit.vcell.geometry.*;
import cbit.vcell.mathmodel.*;
import java.util.Vector;
import cbit.vcell.solver.SolverResultSetInfo;
import cbit.vcell.solver.SimulationInfo;
import java.util.Enumeration;
import cbit.vcell.clientdb.DatabaseListener;
import javax.swing.tree.DefaultTreeModel;

import org.vcell.util.DataAccessException;
import org.vcell.util.document.BioModelInfo;
import org.vcell.util.document.User;

import cbit.vcell.clientdb.DocumentManager;
import cbit.vcell.biomodel.BioModelMetaData;
/**
 * Insert the type's description here.
 * Creation date: (2/14/01 3:33:23 PM)
 * @author: Jim Schaff
 */
public class GeometryMetaDataTreeModel extends javax.swing.tree.DefaultTreeModel {
	private cbit.vcell.geometry.GeometryInfo fieldGeometryInfo = null;
	private cbit.vcell.clientdb.DocumentManager fieldDocumentManager = null;

/**
 * BioModelDbTreeModel constructor comment.
 * @param root javax.swing.tree.TreeNode
 */
public GeometryMetaDataTreeModel() {
	super(new BioModelNode("empty",false),true);
}


/**
 * Insert the method's description here.
 * Creation date: (11/28/00 2:41:43 PM)
 * @param bioModelNode cbit.vcell.desktop.BioModelNode
 * @param bioModelInfo cbit.vcell.biomodel.BioModelInfo
 */
private BioModelNode createVersionSubTree(GeometryInfo geoInfo) throws DataAccessException {
	
	BioModelNode versionNode = new BioModelNode(geoInfo,true);
	
	versionNode.add(new BioModelNode(geoInfo,false));
	if (geoInfo.getVersion().getAnnot()!=null && geoInfo.getVersion().getAnnot().length()>0){
		versionNode.add(new BioModelNode(new Annotation(geoInfo.getVersion().getAnnot()),false));
	}
	if (geoInfo.getImageRef()!=null){
		VCImageInfo imgInfos[] = getDocumentManager().getImageInfos();
		for (int i = 0; i < imgInfos.length; i++){
			if (imgInfos[i].getVersion().getVersionKey().equals(geoInfo.getImageRef())){
				versionNode.add(new BioModelNode(imgInfos[i],false));
			}
		}
	}	
	return versionNode;
}


/**
 * Gets the documentManager property (cbit.vcell.clientdb.DocumentManager) value.
 * @return The documentManager property value.
 * @see #setDocumentManager
 */
public cbit.vcell.clientdb.DocumentManager getDocumentManager() {
	return fieldDocumentManager;
}


/**
 * Gets the geometryInfo property (cbit.vcell.geometry.GeometryInfo) value.
 * @return The geometryInfo property value.
 * @see #setGeometryInfo
 */
public cbit.vcell.geometry.GeometryInfo getGeometryInfo() {
	return fieldGeometryInfo;
}


/**
 * Insert the method's description here.
 * Creation date: (2/14/01 3:50:24 PM)
 */
private void refreshTree() {
	if (getGeometryInfo()!=null){
		try {
			setRoot(createVersionSubTree(getGeometryInfo()));
		}catch (DataAccessException e){
			e.printStackTrace(System.out);
		}
	}else{
		setRoot(new BioModelNode("empty"));
	}
}


/**
 * Sets the documentManager property (cbit.vcell.clientdb.DocumentManager) value.
 * @param documentManager The new value for the property.
 * @see #getDocumentManager
 */
public void setDocumentManager(cbit.vcell.clientdb.DocumentManager documentManager) {
	fieldDocumentManager = documentManager;
	refreshTree();
}


/**
 * Sets the geometryInfo property (cbit.vcell.geometry.GeometryInfo) value.
 * @param geometryInfo The new value for the property.
 * @see #getGeometryInfo
 */
public void setGeometryInfo(cbit.vcell.geometry.GeometryInfo geometryInfo) {
	fieldGeometryInfo = geometryInfo;
	refreshTree();
}
}
