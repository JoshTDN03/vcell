package cbit.vcell.modeldb;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.sql.*;
/**
 * This type was created in VisualAge.
 */
public class TFTestCaseTable extends cbit.sql.Table {
	private static final String TABLE_NAME = "vc_tfTestCase";
	public static final String REF_TYPE = "REFERENCES " + TABLE_NAME + "(" + Table.id_ColumnName + ")";

    private static final String[] tftc_table_constraints =
    			new String[] {
    			"tc_tsr_mm_bm_unq UNIQUE (testSuiteRef,mathModelRef,bmappref)",
				"tc_tsr_must_mmbm CHECK (not (mathmodelref is null and bmappref is null)  and  not (mathmodelref is not null and bmappref is not null))"};

    public final Field testSuiteRef = 		new Field("testSuiteRef",		"INTEGER",			"NOT NULL "+TFTestSuiteTable.REF_TYPE+" ON DELETE CASCADE");
	public final Field mathModelRef = 		new Field("mathModelRef",		"INTEGER",			MathModelTable.REF_TYPE);
	public final Field tcSolutionType =		new Field("tcSolutionType",		"VARCHAR2(64)",		"NOT NULL ");
	public final Field tcAnnotation =		new Field("tcAnnotation",		"VARCHAR2(512)",	"");
	public final Field creationDate =		new Field("creationDate",		"DATE",				"NOT NULL");
	public final Field bmAppRef = 			new Field("bmAppRef",			"INTEGER",			BioModelSimContextLinkTable.REF_TYPE);
	
	private final Field fields[] = {testSuiteRef,mathModelRef,tcSolutionType,tcAnnotation,creationDate,bmAppRef};
	
	public static final TFTestCaseTable table = new TFTestCaseTable();
	

/**
 * ModelTable constructor comment.
 */
private TFTestCaseTable() {
	super(TABLE_NAME,tftc_table_constraints);
	addFields(fields);
}
public String getCreateTriggerSQL(){
	return 
	"CREATE OR REPLACE TRIGGER VCELL.TC_LOCK_TRIG"+"\n"+
	"BEFORE DELETE OR INSERT OR UPDATE"+"\n"+
	"ON VCELL."+TFTestCaseTable.table.getTableName()+"\n"+
	"REFERENCING NEW AS NEW OLD AS OLD"+"\n"+
	"FOR EACH ROW"+"\n"+
	"DECLARE"+"\n"+
	"PRAGMA AUTONOMOUS_TRANSACTION;"+"\n"+
	"testsuiteid NUMBER;"+"\n"+
	"lockState NUMBER;"+"\n"+
	"BEGIN"+"\n"+
	"IF INSERTING THEN"+"\n"+
	"testsuiteid :=:NEW."+TFTestCaseTable.table.testSuiteRef.getUnqualifiedColName()+";"+"\n"+
	"ELSIF UPDATING THEN"+"\n"+
	"testsuiteid :=:OLD."+TFTestCaseTable.table.testSuiteRef.getUnqualifiedColName()+";"+"\n"+
	"ELSIF DELETING THEN"+"\n"+
	"testsuiteid :=:OLD."+TFTestCaseTable.table.testSuiteRef.getUnqualifiedColName()+";"+"\n"+
	"END IF;"+"\n"+
	   "SELECT "+TFTestSuiteTable.table.isLocked.getQualifiedColName()+"\n"+
	   "INTO lockstate"+"\n"+
	   "FROM "+
	   TFTestSuiteTable.table.getTableName()+"\n"+
	   "WHERE "+TFTestSuiteTable.table.id.getQualifiedColName()+" = testsuiteid;"+"\n"+
	   "IF"+"\n"+
	  " 	 lockstate != 0"+"\n"+
	   "THEN"+"\n"+
	   "	   raise_application_error(-20100,'Test Suite locked',true);"+"\n"+
	  "END IF;"+"\n"+
	"END;";

}
}
