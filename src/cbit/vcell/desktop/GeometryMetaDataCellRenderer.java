package cbit.vcell.desktop;
import cbit.vcell.mathmodel.*;
import cbit.util.*;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.vcell.server.User;
import java.awt.Font;
import java.math.BigDecimal;
import cbit.sql.KeyValue;
import cbit.sql.Version;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.math.*;
import cbit.vcell.solver.*;
import cbit.vcell.mapping.*;
import cbit.vcell.model.*;
import cbit.vcell.biomodel.*;
import cbit.vcell.geometry.GeometryInfo;
/**
 * Insert the type's description here.
 * Creation date: (7/27/2000 6:30:41 PM)
 * @author: 
 */
import javax.swing.*;
 
public class GeometryMetaDataCellRenderer extends VCellBasicCellRenderer {
/**
 * MyRenderer constructor comment.
 */
public GeometryMetaDataCellRenderer() {
	super();
}


/**
 * Insert the method's description here.
 * Creation date: (5/8/01 8:35:45 AM)
 * @return javax.swing.Icon
 * @param nodeUserObject java.lang.Object
 */
protected void setComponentProperties(JLabel component, BioModelInfo bioModelInfo) {

	super.setComponentProperties(component,bioModelInfo);  // to load in defaults (e.g. icon)
	
	Version version = bioModelInfo.getVersion();
	component.setToolTipText("BioModel that uses this geometry");
	component.setText("\""+version.getName()+"\" ("+version.getDate().toString()+")");
}


/**
 * Insert the method's description here.
 * Creation date: (5/8/01 8:35:45 AM)
 * @return javax.swing.Icon
 * @param nodeUserObject java.lang.Object
 */
protected void setComponentProperties(JLabel component, GeometryInfo geometryInfo) {

	super.setComponentProperties(component,geometryInfo);
	
	component.setToolTipText("Geometry Info");
	String description = null;
	if (geometryInfo.getDimension()>0){
		String sizeStr = null;
		String originStr = null;
		String microns = '\u03BC'+"m";
		switch (geometryInfo.getDimension()){
			case 1: {
				sizeStr = geometryInfo.getExtent().getX()+" "+microns;
				originStr = geometryInfo.getOrigin().getX()+" "+microns;
				break;
			}
			case 2: {
				sizeStr = "("+geometryInfo.getExtent().getX()+","+geometryInfo.getExtent().getY()+") "+microns;
				originStr = "("+geometryInfo.getOrigin().getX()+","+geometryInfo.getOrigin().getY()+") "+microns;
				break;
			}
			case 3: {
				sizeStr = "("+geometryInfo.getExtent().getX()+","+geometryInfo.getExtent().getY()+","+geometryInfo.getExtent().getZ()+") "+microns;
				originStr = "("+geometryInfo.getOrigin().getX()+","+geometryInfo.getOrigin().getY()+","+geometryInfo.getOrigin().getZ()+") "+microns;
				break;
			}
		}
		component.setText(geometryInfo.getDimension()+"D "+((geometryInfo.getImageRef()!=null)?"image":"analytic")+": size="+sizeStr+", origin="+originStr);
		
	}else{
		component.setText("compartmental");
	}
	
}


/**
 * Insert the method's description here.
 * Creation date: (5/8/01 8:35:45 AM)
 * @return javax.swing.Icon
 * @param nodeUserObject java.lang.Object
 */
protected void setComponentProperties(JLabel component, MathModelInfo mathModelInfo) {

	super.setComponentProperties(component,mathModelInfo);  // to load in defaults (e.g. icon)
	
	Version version = mathModelInfo.getVersion();
	component.setToolTipText("Mathematical Model that uses this Geometry");
	component.setText("\""+version.getName()+"\" ("+version.getDate().toString()+")");
}
}