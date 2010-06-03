package cbit.vcell.modeldb;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.sql.*;

import org.vcell.util.DataAccessException;
import org.vcell.util.SessionLog;
import org.vcell.util.TokenMangler;
import org.vcell.util.document.KeyValue;

import cbit.vcell.geometry.*;
import cbit.vcell.parser.*;
import cbit.sql.*;
/**
 * This type was created in VisualAge.
 */
public class SubVolumeTable extends cbit.sql.Table {
	private static final String TABLE_NAME = "vc_subvolume";
	public static final String REF_TYPE = "REFERENCES " + TABLE_NAME + "(" + Table.id_ColumnName + ")";

	public final Field name		 	= new Field("name",				"varchar(255)",	"NOT NULL");
	public final Field imageRegionRef= new Field("imageRegionRef",	"integer",		ImageRegionTable.REF_TYPE);
	public final Field geometryRef	= new Field("geometryRef",		"integer",		"NOT NULL "+GeometryTable.REF_TYPE+" ON DELETE CASCADE");
	public final Field expression 	= new Field("expression",		"varchar(1024)",	"");
	public final Field handle	 	= new Field("handle",			"integer",		"NOT NULL");
	public final Field ordinal	 	= new Field("ordinal",			"integer",		"NOT NULL");
	
	private final Field fields[] = {name,imageRegionRef,geometryRef,expression,handle,ordinal};
	
	public static final SubVolumeTable table = new SubVolumeTable();
/**
 * ModelTable constructor comment.
 */
private SubVolumeTable() {
	super(TABLE_NAME);
	addFields(fields);
}
/**
 * This method was created in VisualAge.
 * @return Model
 * @param rset ResultSet
 * @param log SessionLog
 */
public SubVolume getAnalyticOrCompartmentSubVolume(KeyValue key, ResultSet rset, SessionLog log) throws SQLException, ExpressionException, DataAccessException {

	//KeyValue key = new KeyValue(rset.getBigDecimal(id.toString(),0));
	String svName = rset.getString(name.toString());
	int handleValue = rset.getInt(handle.toString());
	
	String expString = rset.getString(expression.toString());
	if (rset.wasNull()){
		return new CompartmentSubVolume(key,handleValue);
	}else{
		Expression exp = new Expression(expString);
		return new AnalyticSubVolume(key,svName,exp,handleValue);
	}
}
/**
 * This method was created in VisualAge.
 * @return Model
 * @param rset ResultSet
 * @param log SessionLog
 */
public ImageSubVolume getImageSubVolume(KeyValue key, ResultSet rset, SessionLog log, cbit.image.VCPixelClass pixelClass) throws SQLException, DataAccessException {

	//KeyValue key = new KeyValue(rset.getBigDecimal(id.toString(),0));
	String svName = rset.getString(name.toString());
	int handleValue = rset.getInt(handle.toString());
	
	ImageSubVolume imageSubVolume = new ImageSubVolume(key,pixelClass,handleValue);
	try {
		imageSubVolume.setName(svName);
	}catch (java.beans.PropertyVetoException e){
		e.printStackTrace(System.out);
		throw new DataAccessException(e.getMessage());
	}
	
	return imageSubVolume;
}
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 * @param key KeyValue
 * @param modelName java.lang.String
 */
public String getSQLValueList(InsertHashtable hash, KeyValue key, Geometry geom, SubVolume sv,KeyValue geomKey, int ordinalValue) throws DataAccessException {

	StringBuffer buffer = new StringBuffer();
	buffer.append("(");
	buffer.append(key + ",");
	buffer.append("'" + sv.getName() + "',");
	if (sv instanceof ImageSubVolume) {
		ImageSubVolume isv = (ImageSubVolume) sv;
		KeyValue pixelClassKey = hash.getDatabaseKey(isv.getPixelClass());
		if (pixelClassKey==null){
			pixelClassKey = isv.getPixelClass().getKey();
		}
		if (pixelClassKey ==null){
			throw new DataAccessException("can't get a KeyValue for pixelClass while inserting an imageSubVolume");
		}
		buffer.append(pixelClassKey + ",");
	} else {
		buffer.append("null" + ",");
	}
	//buffer.append(geom.getKey() + ",");
	buffer.append(geomKey + ",");
	if (sv instanceof AnalyticSubVolume) {
		buffer.append("'" + TokenMangler.getSQLEscapedString(((AnalyticSubVolume) sv).getExpression().infix()) + "',");
	} else {
		buffer.append("null"+",");
	}
	buffer.append(sv.getHandle() + ",");
	buffer.append(ordinalValue + ")");
	
	return buffer.toString();
}
}
