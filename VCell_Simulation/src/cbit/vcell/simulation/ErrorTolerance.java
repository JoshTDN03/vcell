package cbit.vcell.simulation;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import org.vcell.util.CommentStringTokenizer;
import org.vcell.util.DataAccessException;

import cbit.vcell.math.VCML;
/**
 * Insert the type's description here.
 * Creation date: (11/2/2000 4:01:40 PM)
 * @author: 
 */
public class ErrorTolerance implements java.io.Serializable, org.vcell.util.Matchable {
	private double fieldAbsoluteErrorTolerance = 1.0E-09;
	private double fieldRelativeErrorTolerance = 1.0E-09;
/**
 * ErrorTolerance constructor comment.
 */
public ErrorTolerance() {
	super();
}
/**
 * ErrorTolerance constructor comment.
 */
public ErrorTolerance(double absoluteErrorTolerance, double relativeErrorTolerance) {
	super();
	fieldAbsoluteErrorTolerance = absoluteErrorTolerance;
	fieldRelativeErrorTolerance = relativeErrorTolerance;
}
/**
 * ErrorTolerance constructor comment.
 */
public ErrorTolerance(ErrorTolerance errorTolerance) {
	super();
	fieldAbsoluteErrorTolerance = errorTolerance.getAbsoluteErrorTolerance();
	fieldRelativeErrorTolerance = errorTolerance.getRelativeErrorTolerance();
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
	if (object != null && object instanceof ErrorTolerance) {
		ErrorTolerance errorTolerance = (ErrorTolerance) object;
		if (getAbsoluteErrorTolerance() != errorTolerance.getAbsoluteErrorTolerance()) return (false);
		if (getRelativeErrorTolerance() != errorTolerance.getRelativeErrorTolerance()) return (false);
		return true;
	}
	return (false);
}
/**
 * Gets the absoluteErrorTolerance property (double) value.
 * @return The absoluteErrorTolerance property value.
 * @see #setAbsoluteErrorTolerance
 */
public double getAbsoluteErrorTolerance() {
	return fieldAbsoluteErrorTolerance;
}
/**
 * Gets the relativeErrorTolerance property (double) value.
 * @return The relativeErrorTolerance property value.
 * @see #setRelativeErrorTolerance
 */
public double getRelativeErrorTolerance() {
	return fieldRelativeErrorTolerance;
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
	//   ErrorTolerance {
	//		AbsoluteErrorTolerance	0.00001
	//		RelativeErrorTolerance	0.001
	//   }
	//
	//	
	StringBuffer buffer = new StringBuffer();
	
	buffer.append(VCML.ErrorTolerance+" "+VCML.BeginBlock+"\n");
	
	buffer.append("   "+VCML.AbsoluteErrorTolerance+" "+getAbsoluteErrorTolerance()+"\n");
	buffer.append("   "+VCML.RelativeErrorTolerance+" "+getRelativeErrorTolerance()+"\n");

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
	//   ErrorTolerance {
	//		AbsoluteErrorTolerance	0.00001
	//		RelativeErrorTolerance	0.001
	//   }
	//
	//	
	try {
		String token = tokens.nextToken();
		if (token.equalsIgnoreCase(VCML.ErrorTolerance)) {
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
			if (token.equalsIgnoreCase(VCML.AbsoluteErrorTolerance)) {
				token = tokens.nextToken();
				fieldAbsoluteErrorTolerance = Double.parseDouble(token);
				continue;
			}
			if (token.equalsIgnoreCase(VCML.RelativeErrorTolerance)) {
				token = tokens.nextToken();
				fieldRelativeErrorTolerance = Double.parseDouble(token);
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
