/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.rdf.reason.builtin;

/*   Reproduce  --- by Oliver Ruebenacker, UCHC --- June 2008
 *   Builtin for SYBREAM to bind all unbound variables in a list of node in a reproducible way.
 *   When presented with a list of nodes the first time, binds all variables to new blank nodes.
 *   When presented with the same list again, bind to the same blank nodes again.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.vcell.sybil.rdf.reason.SYBREAMO;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;

public class MakeAndKeep extends BaseBuiltin {

	protected Map<List<Node>, List<Node>> bindings;
	
	public MakeAndKeep() { 
		bindings = new HashMap<List<Node>, List<Node>>();
	}
	
	@Override
	public String getURI() { return SYBREAMO.ns + "makeAndKeep"; }
	
	public String getName() { return "makeAndKeep"; }
	
	@Override
	public int getArgLength() { return 0; }
	
	@Override
	public boolean bodyCall(Node[] args, int len, RuleContext context) {
		BindingEnvironment env = context.getEnv();
		boolean atLeastOneUnbound = false;
		List<Node> keyList = new Vector<Node>();
		for(Node node : args) {
			Node nodeG = env.getGroundVersion(node);
			keyList.add(nodeG);
			if(nodeG.isVariable()) { atLeastOneUnbound = true; }
		}
		if(atLeastOneUnbound) {
			List<Node> boundList = bindings.get(keyList);
			if(boundList == null) {
				boundList = new Vector<Node>();
				for(int i = 0; i < args.length; ++i) {
					Node nodeG = keyList.get(i);
					if(nodeG.isVariable()) { boundList.add(Node.createAnon()); } 
					else { boundList.add(nodeG); }
				}
				bindings.put(keyList, boundList);
			}
			for(int i = 0; i < args.length; ++i) {
				if(keyList.get(i).isVariable()) {
					env.bind(keyList.get(i), boundList.get(i));
				}
			}
		}
		return true;
	}

	
}
