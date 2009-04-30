package cbit.vcell.server;

import org.vcell.util.document.User;

/**
 * Insert the type's description here.
 * Creation date: (9/17/2004 4:16:30 PM)
 * @author: Ion Moraru
 */
public class PerformanceMonitoringFacility implements cbit.rmi.event.PerformanceMonitorListener {
	private User user = null;
	private SessionLog sessionLog = null;

/**
 * Insert the method's description here.
 * Creation date: (9/17/2004 4:28:47 PM)
 * @param vcConn cbit.vcell.server.VCellConnection
 */
public PerformanceMonitoringFacility(User user, SessionLog sessionLog) {
	this.user = user;
	this.sessionLog = sessionLog;
}


/**
 * Insert the method's description here.
 * Creation date: (9/17/2004 4:22:31 PM)
 * @param pme cbit.rmi.event.PerformanceMonitorEvent
 */
public void performanceMonitorEvent(cbit.rmi.event.PerformanceMonitorEvent pme) {
	// for now, just log them
	String logEntry = "Performance Monitor: ";
	logEntry += user + "; ";
	logEntry += pme.getEventTypeName() + "; ";
	logEntry += pme.getPerfData().getMethodName() + "; ";
	for (int i = 0; i < pme.getPerfData().getEntries().length; i++){
		logEntry += pme.getPerfData().getEntries()[i].getIdentifier() + ": ";	
		logEntry += pme.getPerfData().getEntries()[i].getValue() + "; ";	
	}
	sessionLog.print(logEntry);
}
}