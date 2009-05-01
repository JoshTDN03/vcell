package cbit.vcell.dictionary;

import java.util.Vector;

import org.vcell.util.document.KeyValue;

import cbit.vcell.server.DataAccessException;
import cbit.vcell.server.SessionLog;
import cbit.sql.Field;
import cbit.sql.Table;

/**
 * Represents a table for storing compound information in a database
 * Creation date: (6/25/2002 3:36:45 PM)
 * @author: Steven Woolley
 */
public class CompoundAliasTable extends Table {
    private static final String TABLE_NAME = "vc_compoundalias";
    public static final String REF_TYPE =
        "REFERENCES " + TABLE_NAME + "(" + Table.id_ColumnName + ")";

    public final Field compoundRef = 	new Field("compoundRef", 	"number", CompoundTable.REF_TYPE+" ON DELETE CASCADE NOT NULL");
    public final Field name = 			new Field("name", 			"varchar2(256)", "NOT NULL");
    public final Field preferred = 		new Field("preferred", 		"varchar2(1)", "NOT NULL");//'T'rue or 'F'alse

    private final Field fields[] = { compoundRef, name, preferred };

    public static final CompoundAliasTable table = new CompoundAliasTable();
    /**
     * Creates a new CompoundTable object with the defined table values and fields
     * Creation date: (6/25/2002 3:53:09 PM)
     */
    public CompoundAliasTable() {
        super(TABLE_NAME);
        addFields(fields);
    }
    /**
     * This method was created in VisualAge.
     * @return java.lang.String
     * @param compound Compound
     */
    public String getSQLUpdateList(
	    KeyValue newKey,
        KeyValue argCompoundRef,
        String argName,
        boolean bPreferred) {
        StringBuffer buffer = new StringBuffer();
		buffer.append(id + "=" + newKey + "'");
        buffer.append(compoundRef + "=" + argCompoundRef + ",");
        buffer.append(name + "='" + org.vcell.util.TokenMangler.getSQLEscapedString(argName) + "',");
        buffer.append(preferred + "='" + (bPreferred?"T":"F") + "'");
        return buffer.toString();
    }
    /**
     * Returns an SQL String with a value list taken from the parameter Compound
     * @return java.lang.String
     * @param key KeyValue
     * @param compound Compound
     */
    public String getSQLValueList(
        KeyValue newKey,
        KeyValue argCompoundRef,
        String argName,
        boolean bPreferred) {

        //	int defaultCharge = 0;

        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        buffer.append(newKey + ",");
        buffer.append(argCompoundRef + ",");
        buffer.append("'" + org.vcell.util.TokenMangler.getSQLEscapedString(argName) + "',");
        buffer.append("'" + (bPreferred?"T":"F") + "'");
        buffer.append(")");
        return buffer.toString();
    }
}
