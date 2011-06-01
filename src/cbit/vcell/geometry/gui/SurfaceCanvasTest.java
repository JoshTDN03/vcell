/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.geometry.gui;

import cbit.vcell.geometry.surface.FilterSpecification;
import cbit.vcell.geometry.surface.TaubinSmoothingSpecification;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.RegionImage;
import cbit.vcell.geometry.surface.SurfaceCollection;
/**
 * Insert the type's description here.
 * Creation date: (11/26/2003 1:13:27 PM)
 * @author: Jim Schaff
 */
public class SurfaceCanvasTest {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	try {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		SurfaceCanvas aSurfaceCanvas;
		aSurfaceCanvas = new SurfaceCanvas();
		frame.setContentPane(aSurfaceCanvas);
		frame.setSize(aSurfaceCanvas.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.show();
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);

		Geometry geometry = cbit.vcell.geometry.GeometryTest.getExample_er_cytsol3D();
		//for (int i = 0; i < 100; i++){
			//double d = i/100.0;
		cbit.image.VCImage image = geometry.getGeometrySpec().createSampledImage(geometry.getGeometrySpec().getDefaultSampledImageSize());
		cbit.vcell.geometry.RegionImage regionImage =
		new RegionImage(image,geometry.getDimension(),geometry.getExtent(),geometry.getOrigin(),.3);
		SurfaceCollection surfaceCollection = regionImage.getSurfacecollection();
		System.out.println("smoothing");
		cbit.vcell.geometry.surface.TaubinSmoothing taubin = new cbit.vcell.geometry.surface.TaubinSmoothing();
		FilterSpecification filterSpec = new FilterSpecification(0.3,0.7,0.2,0.2);
		TaubinSmoothingSpecification taubinSpec = TaubinSmoothingSpecification.fromFilterSpecification(filterSpec);
		taubin.smooth(surfaceCollection, taubinSpec);
		System.out.println("painting");
		aSurfaceCanvas.setSurfaceCollection(surfaceCollection);
		//}
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JPanel");
		exception.printStackTrace(System.out);
	}
}
}
