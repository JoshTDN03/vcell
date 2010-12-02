package org.vcell.util.gui;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import javax.swing.border.*;
/**
 * Dummy subclass of TitledBorder that has a no-parameter constructor so it can
 * be recognized by VAJava as a bean implementing the border interface.
 * Creation date: (2/11/2001 6:21:38 PM)
 * @author: Ion Moraru
 */
@SuppressWarnings("serial")
public class TitledBorderBean extends TitledBorder implements Border {
/**
 * Insert the method's description here.
 * Creation date: (2/11/2001 10:01:11 PM)
 */
public TitledBorderBean() {
	this("");
}
/**
 * TitledBorderBean constructor comment.
 * @param title java.lang.String
 */
public TitledBorderBean(String title) {
	super(title);
}
/**
 * TitledBorderBean constructor comment.
 * @param border javax.swing.border.Border
 */
public TitledBorderBean(Border border) {
	super(border);
}
/**
 * TitledBorderBean constructor comment.
 * @param border javax.swing.border.Border
 * @param title java.lang.String
 */
public TitledBorderBean(Border border, String title) {
	super(border, title);
}
/**
 * TitledBorderBean constructor comment.
 * @param border javax.swing.border.Border
 * @param title java.lang.String
 * @param titleJustification int
 * @param titlePosition int
 */
public TitledBorderBean(Border border, String title, int titleJustification, int titlePosition) {
	super(border, title, titleJustification, titlePosition);
}
/**
 * TitledBorderBean constructor comment.
 * @param border javax.swing.border.Border
 * @param title java.lang.String
 * @param titleJustification int
 * @param titlePosition int
 * @param titleFont java.awt.Font
 */
public TitledBorderBean(Border border, String title, int titleJustification, int titlePosition, java.awt.Font titleFont) {
	super(border, title, titleJustification, titlePosition, titleFont);
}
/**
 * TitledBorderBean constructor comment.
 * @param border javax.swing.border.Border
 * @param title java.lang.String
 * @param titleJustification int
 * @param titlePosition int
 * @param titleFont java.awt.Font
 * @param titleColor java.awt.Color
 */
public TitledBorderBean(Border border, String title, int titleJustification, int titlePosition, java.awt.Font titleFont, java.awt.Color titleColor) {
	super(border, title, titleJustification, titlePosition, titleFont, titleColor);
}
}
