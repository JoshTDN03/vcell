/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.vmicro.workflow.scratch;

import cbit.vcell.VirtualMicroscopy.ImageDataset;
import cbit.vcell.VirtualMicroscopy.ROI;

public interface VFrap_ROISourceData {

	enum VFRAP_ROI_ENUM { ROI_CELL, ROI_BLEACHED, ROI_BACKGROUND};

	void addReplaceRoi(ROI originalROI);

	ROI getCurrentlyDisplayedROI();

	ImageDataset getImageDataset();

	ROI getRoi(String name);

}

