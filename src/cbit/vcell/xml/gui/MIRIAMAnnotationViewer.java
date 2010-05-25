package cbit.vcell.xml.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.vcell.sybil.models.sbbox.SBBox.NamedThing;
import org.vcell.util.gui.DialogUtils;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.meta.Identifiable;
import cbit.vcell.biomodel.meta.VCMetaData;
import cbit.vcell.biomodel.meta.registry.Registry;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class MIRIAMAnnotationViewer extends JPanel {
	private JTextArea LoStextArea;
	private JTextArea PPtextArea;
	private BioModel biomodel = null;
	private final MIRIAMAnnotationEditor miriamAnnotationEditor;
	
	public MIRIAMAnnotationViewer() {
		super();
		setLayout(new BorderLayout());

		final JTabbedPane tabbedPane = new JTabbedPane();
		add(tabbedPane);

		miriamAnnotationEditor = new MIRIAMAnnotationEditor();
		tabbedPane.addTab("MIRIAM Annotation Table", null, miriamAnnotationEditor, null);
		
		miriamAnnotationEditor.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						try{
//							if(e.getActionCommand().equals(MIRIAMAnnotationEditor.ACTION_OK)){
//								close(mIRIAMAnnotationEditorFrame,getJDesktopPane());
//							}else 
							if(e.getActionCommand().equals(MIRIAMAnnotationEditor.ACTION_DELETE)){
								VCMetaData metaData = biomodel.getVCMetaData();
								Identifiable identifiable = miriamAnnotationEditor.getSelectedIdentifiable();
								if(identifiable == null){
									DialogUtils.showInfoDialog(MIRIAMAnnotationViewer.this, "Not yet Implemented deletion of individual links");
									return;
								}
								miriamAnnotationEditor.removeSelectedRefGroups();
								miriamAnnotationEditor.setBioModel(biomodel);
							}else if(e.getActionCommand().equals(MIRIAMAnnotationEditor.ACTION_ADD)){
								final String ID_CHOICE = "Identifier";
								final String DATE_CHOICE = "Date";
//								final String CREATOR_CHOICE = "Creator";
//								final String FREETEXT_CHOICE = "Free Text";
								String choice =
									(String)DialogUtils.showListDialog(MIRIAMAnnotationViewer.this, new String[] {/*CREATOR_CHOICE,*/ID_CHOICE,DATE_CHOICE/*,FREETEXT_CHOICE*//*,"Creator","Date"*/}, "Choose Annotation Type");
								if(choice != null){
									if(choice.equals(ID_CHOICE)){
										miriamAnnotationEditor.addIdentifierDialog();
									}else if(choice.equals(DATE_CHOICE)){
										miriamAnnotationEditor.addTimeUTCDialog();
									}/*else if(choice.equals(CREATOR_CHOICE)){
										miriamAnnotationEditor.addCreatorDialog();
									}*//*else if (choice.equals(FREETEXT_CHOICE)){
										miriamAnnotationEditor.addFreeTextDialog();
									}*/
									miriamAnnotationEditor.setBioModel(biomodel);
								}
							}
						}catch(Exception e2){
							DialogUtils.showErrorDialog(MIRIAMAnnotationViewer.this, "Error during Edit action\n"+e2.getMessage(), e2);
						}
					}
				}
			);

		final JScrollPane PPscrollPane = new JScrollPane();
		tabbedPane.addTab("Pretty Print View", null, PPscrollPane, null);

		PPtextArea = new JTextArea();
		PPtextArea.setColumns(40);
		PPtextArea.setRows(4);
		PPscrollPane.setViewportView(PPtextArea);

		final JScrollPane LoSscrollPane = new JScrollPane();
		tabbedPane.addTab("List Of Statements View", null, LoSscrollPane, null);

		LoStextArea = new JTextArea();
		LoStextArea.setColumns(20);
		LoStextArea.setRows(4);
		LoSscrollPane.setViewportView(LoStextArea);
	}

	public void setBiomodel(BioModel biomodel) {
		this.biomodel = biomodel;
		
		// Miriam Annotation Editor table
		miriamAnnotationEditor.setBioModel(biomodel);
		
		// Pretty Print
		Model rdfModel = biomodel.getVCMetaData().getRdfData();
		RDFWriter writer = rdfModel.getWriter("N3");
		StringWriter sw = new StringWriter();
		writer.write(rdfModel, sw, biomodel.getVCMetaData().getBaseURI());
		sw.append("\n\n ResourceMappings : \n");
		Set<Registry.Entry> entrySet = biomodel.getVCMetaData().getRegistry().getAllEntries();
		for (Registry.Entry entry : entrySet) {
			NamedThing namedThing = entry.getNamedThing();
			if (namedThing!=null){
				sw.append(namedThing.resource().getURI());
				Identifiable identifiable = entry.getIdentifiable();
				sw.append(" ============= " + biomodel.getVCID(identifiable).toASCIIString());
				sw.append("\n");
			}
		}

//		Element root = XmlUtil.stringToXML(sw.getBuffer().toString(), null);
		PPtextArea.setText(sw.getBuffer().toString());
		
		// List of Statements View
		StringBuffer strBuffer = new StringBuffer();
		StmtIterator statementIterator = rdfModel.listStatements();
		while (statementIterator.hasNext()) {
			Statement st = statementIterator.nextStatement();
			strBuffer.append(st.getSubject()+";\t" + st.getPredicate()+";\t" + st.getObject()+"\n");
		}
		strBuffer.append("\n\n ResourceMappings : \n");
		entrySet = biomodel.getVCMetaData().getRegistry().getAllEntries();
		for (Registry.Entry entry : entrySet) {
			NamedThing namedThing = entry.getNamedThing();
			if (namedThing!=null){
				strBuffer.append(namedThing.resource().getURI());
				Identifiable identifiable = entry.getIdentifiable();
				strBuffer.append(" ============= " + biomodel.getVCID(identifiable).toASCIIString());
				strBuffer.append("\n");				
			}
		}
		LoStextArea.setText(strBuffer.toString());
	}

	
	
}
