/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.gui.graphinfo;

/*   RDFNodeRenderer  --- by Oliver Ruebenacker, UCHC --- February to March 2009
 *   Renders table cells containing RDFNodes
 */

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.DefaultTableCellRenderer;

import org.vcell.sybil.rdf.NodeUtil;
import org.vcell.sybil.rdf.RDFToken;

public class RDFTokenRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1129859274668534510L;

	protected Map<RDFNodeStyle.FontStyle, Font> fontMap = new HashMap<RDFNodeStyle.FontStyle, Font>();
	
	public Font font(RDFNodeStyle.FontStyle style) {
		Font font = fontMap.get(style);
		if(font == null) {
			font = style.deriveFont(getFont());
			fontMap.put(style, font);
		}
		return font;
	}

	@Override
	public void setValue(Object value) {
		RDFToken token = (RDFToken) value;
		setFont(font(RDFNodeStyle.fontStyle(token.statement().getPredicate())));
		setForeground(RDFNodeStyle.color(token.statement().getPredicate()));			
		String nodeText = NodeUtil.toString(token.node());
		setText("<html>" + nodeText + "</html>");
		setToolTipText(nodeText);
	}

}
