/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.models.graph.manipulator.categorizer;

/*   GraphGrouper  --- by Oliver Ruebenacker, UCHC --- January 2008 to March 2010
 *   Creates the standard groups and chains
 */

import org.vcell.sybil.models.graph.UIGraph;
import org.vcell.sybil.models.graph.UIShape;
import org.vcell.sybil.models.graph.manipulator.GraphManipulationException;
import org.vcell.sybil.models.graphcomponents.group.PatternChain;
import org.vcell.sybil.models.graphcomponents.group.PatternGroup;
import org.vcell.sybil.models.ontology.Evaluator;
import org.vcell.sybil.models.ontology.NoEvaluatorException;

public class GraphGrouper<S extends UIShape<S>, G extends UIGraph<S, G>> 
extends EvaluatorGraphManipulator<S, G> {

	public GraphGrouper() { }
	public GraphGrouper(Evaluator evaluator) { super(evaluator); }

	public void applyToGraph(G graph) throws GraphManipulationException {
		if(evaluator == null) { throw new GraphManipulationException(new NoEvaluatorException()); }
		try {
			PatternGroup.createStandardNodeGroups(graph, evaluator);
			PatternChain.createStandardEdgeChains(graph, evaluator);
		} catch (NoEvaluatorException e) {
			throw new GraphManipulationException(e);
		}
	}
}
