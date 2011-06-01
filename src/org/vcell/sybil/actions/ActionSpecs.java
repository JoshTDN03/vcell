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

import javax.swing.Icon;

import org.vcell.sybil.util.misc.IconFetcher;


public class ActionSpecs {

	protected String name;
	protected String shortSpec;
	protected String longSpec;
	protected Icon icon;
	
	public String getName() { return name; };
	public String getShortSpec() { return shortSpec; };
	public String getLongSpec() { return longSpec; };
	public Icon getIcon() { return icon; };
	
	public ActionSpecs(String nameNew, String shortSpecNew, String longSpecNew, Icon iconNew) {
		name = nameNew;
		shortSpec = shortSpecNew;
		longSpec = longSpecNew;
		icon = iconNew;
	}
	
	public ActionSpecs(String nameNew, String shortSpecNew, String longSpecNew, String newIconLocNew) {
		name = nameNew;
		shortSpec = shortSpecNew;
		longSpec = longSpecNew;
		icon = IconFetcher.fetch(newIconLocNew);
	}
	
	public ActionSpecs(String nameNew, String shortSpecNew, String longSpecNew) {
		name = nameNew;
		shortSpec = shortSpecNew;
		longSpec = longSpecNew;
		icon = null;
	}
	
}
