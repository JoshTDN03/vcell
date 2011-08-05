/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.desktop.biomodel.pathway.shapes;

import java.awt.Color;
import org.vcell.pathway.Conversion;
import cbit.vcell.client.desktop.biomodel.pathway.PathwayGraphModel;

public class BioPaxConversionShape extends BioPaxShape {

	public BioPaxConversionShape(Conversion conversion, PathwayGraphModel graphModel) {
		super(conversion, graphModel);
	}
	
	protected Color getDefaultBackgroundColor() { return Color.yellow; }
	protected int getPreferredWidth() { return 10; }
	protected int getPreferredHeight() { return 10; }
			
	public Conversion getConversion() {
		return (Conversion) getModelObject();
	}

}
