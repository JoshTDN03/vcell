/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.solver.ode;

import cbit.vcell.server.*;
import cbit.vcell.util.ColumnDescription;
import cbit.vcell.math.*;
import java.util.*;
/**
 *  This will have a list of Variables (NB: ReservedVariable.TIME is a ReservedVariable,
 *  and a ReservedVariable is a Variable...also, StateVariables are NOT Variables, but
 *  they are equivalent to them, and indeed are constructed from them.  So, the cols of
 *  this class will be represented by a vector of Variables, and the rows will be a
 *  vector of double[]...
 *  This guy probably has some synchronization problems...
 */
/**
 * Insert the class' description here.
 * Creation date: (8/19/2000 8:59:02 PM)
 * @author: John Wagner
 */
public class ODESolverResultSet extends cbit.vcell.util.RowColumnResultSet implements cbit.vcell.simdata.SimDataConstants, java.io.Serializable {
	public static final String TIME_COLUMN = ReservedVariable.TIME.getName();
/**
 * SimpleODEData constructor comment.
 *  JMW : THIS NEEDS TO BE FIXED...THIS CONSTRUCTOR SHOULD NOT
 *  BE DOING ANY COLUMN CONSTRUCTION AT ALL!
 */
public ODESolverResultSet() {
	super();
}

public boolean isMultiTrialData()
{
	if(getColumnDescriptionsCount() > 0)
	{
		int totalcol = getColumnDescriptionsCount();
		for(int i=0; i<totalcol; i++)
		{
			ColumnDescription cd = getColumnDescriptions(i);
			if (cd.getName().equals("TrialNo"))
				return true;
		}
	}
	return false;
}
}
