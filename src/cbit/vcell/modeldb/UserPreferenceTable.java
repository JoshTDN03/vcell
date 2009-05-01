package cbit.vcell.modeldb;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/

import cbit.sql.Field;
import cbit.sql.Table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.vcell.util.BeanUtils;
import org.vcell.util.Preference;
import org.vcell.util.TokenMangler;

/**
 * This type was created in VisualAge.
 */
public class UserPreferenceTable extends cbit.sql.Table {
	private static final String TABLE_NAME = "vc_userpref";
	public static final String REF_TYPE = "REFERENCES " + TABLE_NAME + "(" + Table.id_ColumnName + ")";

	public final Field userRef 			= new Field("userRef",				"integer",			"NOT NULL "+UserTable.REF_TYPE+" ON DELETE CASCADE");
	public final Field userPrefKey 		= new Field("userPrefKey",			"varchar("+Preference.MAX_KEY_LENGTH+")",	"NOT NULL");
	public final Field userPrefValue	= new Field("userPrefValue",		"varchar("+Preference.MAX_VALUE_LENGTH+")", "NOT NULL");
	

	private final Field fields[] = {userRef,userPrefKey,userPrefValue};
	
	public static final UserPreferenceTable table = new UserPreferenceTable();

/**
 * ModelTable constructor comment.
 */
private UserPreferenceTable() {
	super(TABLE_NAME);
	addFields(fields);
}


/**
 * Insert the method's description here.
 * Creation date: (6/10/2004 4:12:51 PM)
 * @return java.util.Dictionary
 * @param rset java.sql.ResultSet
 */
public Preference[] getUserPreferences(ResultSet rset) throws SQLException{

	Vector<Preference> preferenceList = new Vector<Preference>();
	while (rset.next()){
		String propKey = rset.getString(UserPreferenceTable.table.userPrefKey.getUnqualifiedColName());
		String propValue = rset.getString(UserPreferenceTable.table.userPrefValue.getUnqualifiedColName());
		preferenceList.add(
			new Preference(
				TokenMangler.getSQLRestoredString(propKey),TokenMangler.getSQLRestoredString(propValue)));
	}

	return (Preference[])BeanUtils.getArray(preferenceList,Preference.class);
}
}