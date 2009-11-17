package cbit.vcell.opt;

import org.vcell.util.Matchable;

import cbit.vcell.VirtualMicroscopy.ROI;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
/**
 * Insert the type's description here.
 * Creation date: (3/3/00 12:13:49 AM)
 * @author: 
 */
public class Parameter implements Matchable{
	private String name = null;
	private double lowerBound = Double.NEGATIVE_INFINITY;
	private double upperBound = Double.POSITIVE_INFINITY;
	private double initialGuess = 0.0;
	private double scale = 1.0;

/**
 * OptimizationVariable constructor comment.
 * @param name java.lang.String
 */
public Parameter(String argName, double argLowerBound, double argUpperBound, double argScale, double argInitialGuess) {
	this.name = argName;
	this.lowerBound = argLowerBound;
	this.upperBound = argUpperBound;
	if (lowerBound > upperBound) {
		throw new RuntimeException("Lower bound cannot be greater than upper bound for parameter "+name);
	}
	this.scale = argScale;
	if (scale <= 0.0) {
		throw new RuntimeException("Scale should be positive");
	}
	this.initialGuess = argInitialGuess;
	if ((initialGuess < lowerBound) || (initialGuess > upperBound)) {
		throw new RuntimeException("Initial guess for parameter "+name+" should be between lower and upper bounds.");
	}
}


/**
 * Insert the method's description here.
 * Creation date: (3/3/00 3:08:46 PM)
 * @return double
 */
public double getInitialGuess() {
	return initialGuess;
}


/**
 * Insert the method's description here.
 * Creation date: (3/3/00 1:04:53 AM)
 * @return double
 */
public double getLowerBound() {
	return lowerBound;
}


/**
 * Insert the method's description here.
 * Creation date: (8/3/2005 3:24:25 PM)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return name;
}


/**
 * Insert the method's description here.
 * Creation date: (8/4/2005 2:12:49 PM)
 * @return double
 */
public double getScale() {
	return this.scale;
}


/**
 * Insert the method's description here.
 * Creation date: (3/3/00 1:04:53 AM)
 * @return double
 */
public double getUpperBound() {
	return upperBound;
}

//Implemented Matchable Nov. 2009
public boolean compareEqual(Matchable obj) 
{
	if (this == obj) {
		return true;
	}
	if (obj != null && obj instanceof Parameter) 
	{
		Parameter param = (Parameter) obj;
		if (name != param.getName())
		{
			return false;
		}
		if(lowerBound != param.getLowerBound())
		{
			return false;
		}
		if(upperBound != param.getUpperBound())
		{
			return false;
		}
		if(initialGuess != param.getInitialGuess())
		{
			return false;
		}
		if(scale != param.getScale())
		{
			return false;
		}
		return true;
	}
	return false;
}

public Parameter duplicate()
{
	Parameter result = new Parameter(this.name, this.lowerBound, this.upperBound, this.scale, this.initialGuess);
	return result;
}

}