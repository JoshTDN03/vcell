package cbit.vcell.numericstest;
import cbit.vcell.solver.SimulationInfo;
import cbit.vcell.mathmodel.MathModelInfo;
import cbit.vcell.solver.test.VariableComparisonSummary;
import java.math.BigDecimal;
/**
 * Insert the type's description here.
 * Creation date: (10/16/2004 2:14:39 PM)
 * @author: Frank Morgan
 */
public abstract class TestCriteriaNew implements java.io.Serializable {

	private SimulationInfo simInfo;
	private SimulationInfo regressionSimInfo;
	private Double maxRelError;
	private Double maxAbsError;
	private VariableComparisonSummary[] varComparisonSummaries;
	private BigDecimal tcritKey;
	private String reportStatus;
	private String reportStatusMessage;

	public static final String TCRIT_STATUS_NEEDSREPORT = "NEEDSREPORT";
	public static final String TCRIT_STATUS_RPERROR = "RPERROR";//report failed
	public static final String TCRIT_STATUS_FAILEDVARS = "FAILEDVARS";//report run, some vars failed criteria
	public static final String TCRIT_STATUS_PASSED = "PASSED";//report run, all pased
	public static final String TCRIT_STATUS_NOREFREGR = "NOREFREGR";//can't report until set
	public static final String TCRIT_STATUS_SIMFAILED = "SIMFAILED";//sim failed
	public static final String TCRIT_STATUS_SIMRUNNING = "SIMRUNNING";//sim running
	public static final String TCRIT_STATUS_SIMNOTRUNFAILDONE = "SIMNOTRUNFAILDONE";//sim not run,fail or done
	public static final String TCRIT_STATUS_NODATA = "NODATA";//sim never run

/**
 * TestCriteriaNew constructor comment.
 */
public TestCriteriaNew(
    BigDecimal argTcritKey,
    SimulationInfo argSimInfo,
    SimulationInfo argRegrSimInfo,
    Double argMaxRelError,
    Double argMaxAbsError,
    VariableComparisonSummary[] argVariableComparisonSummary,
    String argReportStatus,
    String argReportStatusMessage) {

    if (argSimInfo == null || argReportStatus == null) {
        throw new IllegalArgumentException(
            this.getClass().getName() + " SimulationInfo and Reportstatus can't be null");
    }
    simInfo = argSimInfo;
    regressionSimInfo = argRegrSimInfo;
    maxRelError = argMaxRelError;
    maxAbsError = argMaxAbsError;
    varComparisonSummaries = argVariableComparisonSummary;
    tcritKey = argTcritKey;
    reportStatus = argReportStatus;
    reportStatusMessage = argReportStatusMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (10/16/2004 2:33:35 PM)
 * @return double
 */
public Double getMaxAbsError() {
	return maxAbsError;
}


/**
 * Insert the method's description here.
 * Creation date: (10/16/2004 2:33:35 PM)
 * @return double
 */
public Double getMaxRelError() {
	return maxRelError;
}


/**
 * Insert the method's description here.
 * Creation date: (10/16/2004 2:33:35 PM)
 * @return cbit.vcell.solver.SimulationInfo
 */
public cbit.vcell.solver.SimulationInfo getRegressionSimInfo() {
	return regressionSimInfo;
}


/**
 * Insert the method's description here.
 * Creation date: (11/15/2004 1:12:28 PM)
 * @return java.lang.String
 */
public String getReportStatus() {
	return reportStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (11/15/2004 1:12:28 PM)
 * @return java.lang.String
 */
public String getReportStatusMessage() {
	return reportStatusMessage;
}


/**
 * Insert the method's description here.
 * Creation date: (10/16/2004 2:33:35 PM)
 * @return cbit.vcell.solver.SimulationInfo
 */
public cbit.vcell.solver.SimulationInfo getSimInfo() {
	return simInfo;
}


/**
 * Insert the method's description here.
 * Creation date: (10/16/2004 2:33:35 PM)
 * @return cbit.vcell.mathmodel.MathModelInfo
 */
public BigDecimal getTCritKey() {
	return tcritKey;
}


/**
 * Insert the method's description here.
 * Creation date: (10/16/2004 2:33:35 PM)
 * @return cbit.vcell.solver.test.VariableComparisonSummary[]
 */
public cbit.vcell.solver.test.VariableComparisonSummary[] getVarComparisonSummaries() {
	return varComparisonSummaries;
}
}