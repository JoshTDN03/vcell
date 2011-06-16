/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.gui.graph;

/*   RubberBandRectShape  --- by Oliver Ruebenacker, UCHC --- July 2007 to February 2009
 *   Some shape for Sybil.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import org.vcell.sybil.models.graphcomponents.RDFGraphComponent;

public final class RubberBandRectShape extends RectangleShape {
	
	private Point start = new Point();
	private Point end = new Point();

	public RubberBandRectShape(Point start, Point end, Graph graphNew, RDFGraphComponent newSybComp) {
		super(graphNew, newSybComp);
		this.start = start;
		this.end = end;
		setColorFG(Color.red);
		location.setP((start.x + end.x)/2, (start.y + end.y)/2);
		updateScreenSize = true;
		updatePreferedSize = true;
		usePreferedForScreenSize = true;
	}
	
	@Override
	public Dimension getPreferedSize(Graphics2D g) {
		preferedSize.width = Math.abs(end.x - start.x);
		preferedSize.height = Math.abs(end.y - start.y);
		return preferedSize;
	}

	public void setEnd(Point end) { 
		this.end = end; 
		location.setP((start.x + end.x)/2, (start.y + end.y)/2);
	}
	
	public void drag(Point end, Graphics2D g) { setEnd(end); this.updateSizes(g); }

	@Override
	public PaintLevel paintLevel() { return PaintLevel.Container; }

}
