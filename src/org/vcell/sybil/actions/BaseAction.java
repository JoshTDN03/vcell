/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.actions;

/*   BaseAction  --- by Oliver Ruebenacker, UCHC --- May 2007 to January 2009
 *   Actions to be integrated with the ActionBase tree
 */

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.vcell.sybil.util.enumerations.OneElementEnum;
import org.vcell.sybil.util.enumerations.SmartEnum;


public abstract class BaseAction extends SpecAction implements ActionBranch {

	private static final long serialVersionUID = -3194054148089660599L;
	protected CoreManager coreManager;
	
	public BaseAction(ActionSpecs newSpecs, CoreManager coreManager) {
		super(newSpecs);
		this.coreManager = coreManager;
	}
	
	public CoreManager coreManager() { return coreManager; };

	public SmartEnum<BaseAction> actions() {
		return new OneElementEnum<BaseAction>(this);
	}
	
	public SmartEnum<ActionBranch> branches() {
		return new OneElementEnum<ActionBranch>(this);
	}
	
	@Override
	public Component requester(ActionEvent event) { return RequesterProvider.requester(event, coreManager); }
	
}
