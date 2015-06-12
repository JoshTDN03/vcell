/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.desktop;
import javax.swing.JTree;

import org.vcell.util.document.User;
 
@SuppressWarnings("serial")
public class VCDocumentDbCellRenderer extends VCellBasicCellRenderer {
	
	protected User sessionUser = null;

/**
 * MyRenderer constructor comment.
 */
public VCDocumentDbCellRenderer(User argSessionUser) {
	super();
	this.sessionUser = argSessionUser;
}

public VCDocumentDbCellRenderer() {
	this(null);
}

public final void setSessionUser(User sessionUser) {
	this.sessionUser = sessionUser;
}

public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	if (value instanceof BioModelNode) {
		BioModelNode node = (BioModelNode) value;
		Object userObject = node.getUserObject();
		if (VCDocumentDbTreeModel.SHARED_BIO_MODELS.equals(userObject)
				|| VCDocumentDbTreeModel.SHARED_MATH_MODELS.equals(userObject)
				|| VCDocumentDbTreeModel.SHARED_GEOMETRIES.equals(userObject)
				|| VCDocumentDbTreeModel.Public_BioModels.equals(userObject)
				|| VCDocumentDbTreeModel.Public_MathModels.equals(userObject)
				|| VCDocumentDbTreeModel.PUBLIC_GEOMETRIES.equals(userObject)
				|| VCDocumentDbTreeModel.Education.equals(userObject)
				|| VCDocumentDbTreeModel.BNGRulesBased.equals(userObject)
				|| VCDocumentDbTreeModel.Tutorials.equals(userObject)
				) {
			setText(getText() + " (" + node.getChildCount() + ")");
		}
	}
	return this;
}
}
