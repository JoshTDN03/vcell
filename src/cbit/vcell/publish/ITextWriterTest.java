/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.publish;
import java.awt.print.PageFormat;
import java.io.File;
import java.io.FileOutputStream;

import cbit.util.xml.XmlUtil;

/**
 * Test class for the publish package.
 * Creation date: (4/18/2003 2:19:17 PM)
 * @author: John Wagner & Rashad Badrawi
 */
 
public class ITextWriterTest {



public ITextWriterTest() {
	super();
}


/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		if (args.length < 2) {
			System.err.println("Usage: ITextWriterTest inputFileName [-b|-m|-g]");
			System.exit(0);
		}
		org.jdom.Element root = XmlUtil.readXML(new File(args[0])).getRootElement();
		cbit.vcell.xml.XmlReader xmlReader = new cbit.vcell.xml.XmlReader(true);//new cbit.vcell.xml.XmlReader(false);
		PDFWriter pdfWriter = new PDFWriter();
		RTFWriter rtfWriter = new RTFWriter();
		HTMWriter htmWriter = new HTMWriter();
		//
		FileOutputStream pdfFileOutputStream = new FileOutputStream("C:\\publish\\ITextWriterTest.pdf");
		FileOutputStream rtfFileOutputStream = new FileOutputStream("C:\\publish\\ITextWriterTest.rtf");
		FileOutputStream htmFileOutputStream = new FileOutputStream("C:\\publish\\ITextWriterTest.html");
		//
		PageFormat pageFormat = java.awt.print.PrinterJob.getPrinterJob().defaultPage();
		pageFormat.setOrientation(PageFormat.PORTRAIT);
		if (args[1].equals("-b")) {
			cbit.vcell.biomodel.BioModel bioModel =  xmlReader.getBioModel(root);
			pdfWriter.writeBioModel(bioModel, pdfFileOutputStream, pageFormat);
			rtfWriter.writeBioModel(bioModel, rtfFileOutputStream, pageFormat);
			htmWriter.writeBioModel(bioModel, htmFileOutputStream, pageFormat);
		} else if (args[1].equals("-m")) {
			cbit.vcell.mathmodel.MathModel mathModel = xmlReader.getMathModel(root);
			pdfWriter.writeMathModel(mathModel, pdfFileOutputStream, pageFormat);
			rtfWriter.writeMathModel(mathModel, rtfFileOutputStream, pageFormat);
			htmWriter.writeMathModel(mathModel, htmFileOutputStream, pageFormat);
		} else if (args[1].equals("-g")) {
			cbit.vcell.geometry.Geometry geom = xmlReader.getGeometry(root);
			pdfWriter.writeGeometry(geom, pdfFileOutputStream, pageFormat);
			rtfWriter.writeGeometry(geom, rtfFileOutputStream, pageFormat);
			htmWriter.writeGeometry(geom, htmFileOutputStream, pageFormat);
		}
		System.exit(0);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of ITextWRiterTest.main()...");
		exception.printStackTrace(System.out);
	}
}
}
