package cbit.vcell.simulation;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import org.vcell.util.CommentStringTokenizer;
import org.vcell.util.DataAccessException;

import cbit.vcell.math.*;
/**
 * Insert the type's description here.
 * Creation date: (11/2/2000 3:59:34 PM)
 * @author: 
 */
public class TimeStep implements java.io.Serializable, org.vcell.util.Matchable {
	private double fieldMinimumTimeStep = 1.0E-8;
	private double fieldDefaultTimeStep = 0.1;
	private double fieldMaximumTimeStep = 1.0;
/**
 * TimeStep constructor comment.
 */
public TimeStep() {
	super();
}
/**
 * TimeStep constructor comment.
 */
public TimeStep(double minimumTimeStep, double defaultTimestep, double maximumTimeStep) {
	super();
	fieldMinimumTimeStep = minimumTimeStep;
	fieldDefaultTimeStep = defaultTimestep;
	fieldMaximumTimeStep = maximumTimeStep;
}
/**
 * TimeStep constructor comment.
 */
public TimeStep(TimeStep timeStep) {
	super();
	fieldMinimumTimeStep = timeStep.getMinimumTimeStep();
	fieldDefaultTimeStep = timeStep.getDefaultTimeStep();
	fieldMaximumTimeStep = timeStep.getMaximumTimeStep();
}
/**
 * Checks for internal representation of objects, not keys from database
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(org.vcell.util.Matchable object) {
	if (this == object) {
		return (true);
	}
	if (object != null && object instanceof TimeStep) {
		TimeStep timeStep = (TimeStep) object;
		if (getMinimumTimeStep() != timeStep.getMinimumTimeStep()) return (false);
		if (getDefaultTimeStep() != timeStep.getDefaultTimeStep()) return (false);
		if (getMaximumTimeStep() != timeStep.getMaximumTimeStep()) return (false);
		return true;
	}
	return (false);
}
/**
 * Gets the defaultTimeStep property (double) value.
 * @return The defaultTimeStep property value.
 * @see #setDefaultTimeStep
 */
public double getDefaultTimeStep() {
	return fieldDefaultTimeStep;
}
/**
 * Gets the maximumTimeStep property (double) value.
 * @return The maximumTimeStep property value.
 * @see #setMaximumTimeStep
 */
public double getMaximumTimeStep() {
	return fieldMaximumTimeStep;
}
/**
 * Gets the minimumTimeStep property (double) value.
 * @return The minimumTimeStep property value.
 * @see #setMinimumTimeStep
 */
public double getMinimumTimeStep() {
	return fieldMinimumTimeStep;
}
/**
 * Insert the method's description here.
 * Creation date: (11/7/00 12:04:47 AM)
 * @return java.lang.String
 */
public String getVCML() {
	//
	// write format as follows:
	//
	//   TimeStep {
	//		DefaultTimeStep		0.0
	//		MinimumTimeStep		1e-7
	//		MaximumTimeStep		0.01
	//   }
	//
	//	
	StringBuffer buffer = new StringBuffer();
	
	buffer.append(VCML.TimeStep+" "+VCML.BeginBlock+"\n");
	
	buffer.append("   "+VCML.DefaultTimeStep+" "+getDefaultTimeStep()+"\n");
	buffer.append("   "+VCML.MinimumTimeStep+" "+getMinimumTimeStep()+"\n");
	buffer.append("   "+VCML.MaximumTimeStep+" "+getMaximumTimeStep()+"\n");

	buffer.append(VCML.EndBlock+"\n");

	return buffer.toString();
}
/**
 * Insert the method's description here.
 * Creation date: (10/24/00 3:45:12 PM)
 * @return java.lang.String
 */
public void readVCML(CommentStringTokenizer tokens) throws DataAccessException {
	//
	// read format as follows:
	//
	//   TimeStep {
	//		DefaultTimeStep		0.0
	//		MinimumTimeStep		1e-7
	//		MaximumTimeStep		0.01
	//   }
	//
	//	
	try {
		String token = tokens.nextToken();
		if (token.equalsIgnoreCase(VCML.TimeStep)) {
			token = tokens.nextToken();
			if (!token.equalsIgnoreCase(VCML.BeginBlock)) {
				throw new DataAccessException(
					"unexpected token " + token + " expecting " + VCML.BeginBlock); 
			}
		}
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken();
			if (token.equalsIgnoreCase(VCML.EndBlock)) {
				break;
			}
			if (token.equalsIgnoreCase(VCML.DefaultTimeStep)) {
				token = tokens.nextToken();
				fieldDefaultTimeStep = Double.parseDouble(token);
				continue;
			}
			if (token.equalsIgnoreCase(VCML.MinimumTimeStep)) {
				token = tokens.nextToken();
				fieldMinimumTimeStep = Double.parseDouble(token);
				continue;
			}
			if (token.equalsIgnoreCase(VCML.MaximumTimeStep)) {
				token = tokens.nextToken();
				fieldMaximumTimeStep = Double.parseDouble(token);
				continue;
			}
			throw new DataAccessException("unexpected identifier " + token);
		}
	} catch (Throwable e) {
		throw new DataAccessException(
			"line #" + (tokens.lineIndex()+1) + " Exception: " + e.getMessage()); 
	}
}
}
