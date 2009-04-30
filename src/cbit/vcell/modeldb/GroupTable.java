package cbit.vcell.modeldb;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import org.vcell.util.document.KeyValue;

import cbit.sql.*;
/**
 * This type was created in VisualAge.
 */
public class GroupTable extends cbit.sql.Table {
	private static final String TABLE_NAME = "vc_group";
	public static final String REF_TYPE = "REFERENCES " + TABLE_NAME + "(" + Table.id_ColumnName + ")";

	public final Field groupid				= new Field("groupid",			"integer",	"NOT NULL");
	public final Field userRef				= new Field("userRef",			"integer",	"NOT NULL "+UserTable.REF_TYPE+" ON DELETE CASCADE");
	public final Field isHiddenFromOwner	= new Field("isHiddenFromOwner","integer",	"NOT NULL");
	public final Field groupMemberHash		= new Field("groupMemberHash",	"integer",	"NOT NULL");

	private final Field fields[] = {groupid,userRef,isHiddenFromOwner,groupMemberHash};
	
	public static final GroupTable table = new GroupTable();
/**
 * ModelTable constructor comment.
 */
private GroupTable() {
	super(TABLE_NAME);
	addFields(fields);
}
/**
 * Insert the method's description here.
 * Creation date: (1/8/2002 4:21:18 PM)
 * @return java.lang.String
 */
public static final String getCreateGroupPrivateSQL(KeyValue key) {
	boolean isHiddenFromOwner = false;
	String sql = "INSERT INTO "+GroupTable.table.getTableName()+
			" VALUES ("+key.toString()+","+
						cbit.vcell.server.GroupAccess.GROUPACCESS_NONE+","+	//groupID
						UserTable.VOID_ID+","+								//userRef
						(isHiddenFromOwner?"1":"0")+","+					//hiddenFromOwner
						cbit.vcell.server.GroupAccess.GROUPACCESS_NONE+")";	//hash
	return sql;
}
/**
 * Insert the method's description here.
 * Creation date: (1/8/2002 4:21:18 PM)
 * @return java.lang.String
 */
public static final String getCreateGroupPublicSQL(KeyValue key) {
	boolean isHiddenFromOwner = false;
	String sql = "INSERT INTO "+cbit.vcell.modeldb.GroupTable.table.getTableName()+
			" VALUES ("+key.toString()+","+
						cbit.vcell.server.GroupAccess.GROUPACCESS_ALL+","+	//groupID
						UserTable.VOID_ID+","+								//userRef
						(isHiddenFromOwner?"1":"0")+","+					//hiddenFromOwner
						cbit.vcell.server.GroupAccess.GROUPACCESS_ALL+")";	//hash
	return sql;
}
}
