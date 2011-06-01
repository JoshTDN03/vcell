/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.gui.graph;
/**
 * The event set listener interface for the graph feature.
 */
public interface GraphListener extends java.util.EventListener {
/**
 * 
 * @param event cbit.vcell.graph.GraphEvent
 */
void graphChanged(cbit.gui.graph.GraphEvent event);
}
