package cbit.vcell.modeldb;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import org.vcell.util.document.KeyValue;

import cbit.sql.Field;
import cbit.sql.Table;
/**
 * This type was created in VisualAge.
 */
public class AvailableTable extends cbit.sql.Table {
	private static final String TABLE_NAME = "vc_available";
	public static final String REF_TYPE = "REFERENCES " + TABLE_NAME + "(" + Table.id_ColumnName + ")";

	public final Field insertDate			= new Field("insertDate",			"date",			"NOT NULL ");
	public final Field isAvailable			= new Field("isAvailable",			"varchar2(5)",	"NOT NULL ");
	public final Field letUserAskForCallback= new Field("letUserAskForCallback","varchar2(5)",	"");
	public final Field offlineMessage		= new Field("offlineMessage",		"varchar2(512)","");

	private final Field fields[] = {insertDate,isAvailable,letUserAskForCallback,offlineMessage};
	
	public static final AvailableTable table = new AvailableTable();
/**
 * ModelTable constructor comment.
 */
private AvailableTable() {
	super(TABLE_NAME);
	addFields(fields);
}
/**
 * Insert the method's description here.
 * Creation date: (1/8/2002 4:21:18 PM)
 * @return java.lang.String
 */
public static final String getCreateInitAvailStatusSQL(KeyValue key) {
	String sql = "INSERT INTO "+AvailableTable.table.getTableName()+
			" VALUES ("+key.toString()+","+
						"SYSDATE"+","+	//insertDate
						"'true'"+","+	//isAvailable
						"NULL"+","+		//letUserAskForCallback
						"NULL"+")";		//offlineMessage
	return sql;
}
}
