/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.gui.graph.locations;

/*   Location  --- by Oliver Ruebenacker, UCHC --- February 2009
 *   Locations of shapes used by Sybil
 */

import java.awt.Point;

public interface BasicLocation {

	public int x();
	public int y();
	public Point p();;

	public static final NullLocation Origin = new NullLocation();
	
	public static class NullLocation implements BasicLocation {

		public static final Point nullPoint = new Point();
		
		public Point p() { return nullPoint; }
		public int x() { return 0; }
		public int y() { return 0; }
		
	}
}
