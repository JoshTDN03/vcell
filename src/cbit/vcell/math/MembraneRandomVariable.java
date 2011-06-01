/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.math;

import org.vcell.util.CommentStringTokenizer;

import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;

public class MembraneRandomVariable extends RandomVariable {
	public MembraneRandomVariable(String name, Expression seed, Distribution dist) {
		super(name, seed, dist);
	}
	
	public MembraneRandomVariable(String name, MathDescription mathDesc, CommentStringTokenizer tokens) throws MathFormatException, ExpressionException {
		super(name, mathDesc, tokens);
	}

	@Override
	protected String getVCMLTag() {
		return VCML.MembraneRandomVariable;
	}
}
