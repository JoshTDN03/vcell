/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.mapping.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import org.vcell.util.Issue;
import org.vcell.util.gui.DefaultScrollTableCellRenderer;

import cbit.vcell.client.desktop.biomodel.IssueManager;
import cbit.vcell.geometry.GeometryClass;
import cbit.vcell.mapping.FeatureMapping;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.model.Structure;
import cbit.vcell.units.VCUnitDefinition;

@SuppressWarnings("serial")
public class StructureMappingTableRenderer extends DefaultScrollTableCellRenderer
{
	public static class TextIcon implements Icon {
		private String text = null;
		private int width = 20;
		private int height = 20;
		private int superScriptStartIndex = -1;
		private int superScriptEndIndex = -1;
		private Color userColor = null;
		 
		public TextIcon(String text) {
			this(text, null);
		}
		public TextIcon(String text, Color c) {
			this(text, -1, -1, c);
		}
		public TextIcon(String text, int start, int end) {
			this(text, start, end, null);
		}
		public TextIcon(String text, int start, int end, Color c) {
			this.text = text;
			this.superScriptStartIndex = start;
			this.superScriptEndIndex = end;
			this.userColor = c;
		}
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setColor(c.getBackground());
		    g2d.fillRect(x, y, width, height);
		    if (userColor != null) {
		    	g2d.setColor(userColor);
		    } else {
		    	g2d.setColor(c.getForeground());
		    }
            Font font = g2d.getFont();
            font = font.deriveFont(font.getSize2D() - 1);
            FontMetrics fm = g.getFontMetrics();
            int width = getIconWidth() - fm.stringWidth(text);
            int xoffset = 0;
            if (width > 0) {
            	xoffset = width / 2;
            }
            g2d.setFont(font);
            
            int yoffset = 13;
            if (superScriptStartIndex >= 0 && superScriptEndIndex >= 0) {
            	AttributedString as = new AttributedString(text);
            	as.addAttribute(TextAttribute.SIZE, font.getSize2D());
            	as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, superScriptStartIndex, superScriptEndIndex);
            	g2d.drawString(as.getIterator(), x, y + yoffset);            	
            } else {
            	g2d.drawString(text, x + xoffset, y + yoffset);
            }
			g2d.dispose();
		}
		
		public int getIconWidth() {
			return width;
		}
		
		public int getIconHeight() {
			return height;
		}
	};
	
	private static final HashMap<String, TextIcon> unitIconHash = new HashMap<String, StructureMappingTableRenderer.TextIcon>();
	
	public StructureMappingTableRenderer() {
		super();
		setHorizontalTextPosition(SwingConstants.LEFT);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setIcon(null);
		if (table.getModel() instanceof StructureMappingTableModel) {
			StructureMappingTableModel structureMappingTableModel = (StructureMappingTableModel)table.getModel();
			if (value instanceof Structure) {
				Structure structure = (Structure)value;
				setText(structure.getName());
			} else if (value instanceof Double && structureMappingTableModel.isNewSizeColumn(column)) {
				StructureMapping structureMapping = structureMappingTableModel.getStructureMapping(row);
				if (structureMappingTableModel.isNonSpatial()) {	
					VCUnitDefinition unitDefinition = structureMapping.getStructure().getStructureSize().getUnitDefinition();
					TextIcon sizeIcon = unitIconHash.get(unitDefinition.getSymbol());
					if (sizeIcon==null){
						sizeIcon = new TextIcon("[ "+unitDefinition.getSymbolUnicode()+" ]");
						unitIconHash.put(unitDefinition.getSymbol(), sizeIcon);
					}
					setIcon(sizeIcon);
				} else {
					if (structureMapping.getUnitSizeParameter()!=null){
						VCUnitDefinition unitDefinition = structureMapping.getUnitSizeParameter().getUnitDefinition();
						TextIcon sizeIcon = unitIconHash.get(unitDefinition.getSymbol());
						if (sizeIcon==null){
							sizeIcon = new TextIcon("[ "+unitDefinition.getSymbolUnicode()+" ]");
							unitIconHash.put(unitDefinition.getSymbol(), sizeIcon);
						}
						setIcon(sizeIcon);
					}
				}
			}
			if (structureMappingTableModel.isSubdomainColumn(column)) { // can be null
				if (value == null) {
					setText("Unmapped");
					setForeground(Color.red);
				} else {
					if (value instanceof GeometryClass) {
						setText(((GeometryClass)value).getName());
					} else {
						setText(value.toString());
					}
				}
			}

			List<Issue> issueList = structureMappingTableModel.getIssues(row, column);
			if (issueList.size() > 0) {
				setToolTipText(IssueManager.getHtmlIssueMessage(issueList));
				if (column == 0) {
					setBorder(new MatteBorder(1,1,1,0,Color.red));
				} else if (column == table.getColumnCount() - 1) {
					setBorder(new MatteBorder(1,0,1,1,Color.red));
				} else {
					setBorder(new MatteBorder(1,0,1,0,Color.red));
				}
			} else {
				String toolTip = structureMappingTableModel.getToolTip(row, column);
				setToolTipText(toolTip);
				setBorder(DEFAULT_GAP);
			}
		}
		return this;
	}
}
