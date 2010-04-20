package cbit.vcell.client.task;

import java.awt.Component;
import java.util.Hashtable;

import org.vcell.util.UserCancelException;
import org.vcell.util.document.User;
import org.vcell.util.document.VCDocument;
import org.vcell.util.document.VCDocumentInfo;
import org.vcell.util.gui.DialogUtils;

import cbit.vcell.client.*;
import cbit.vcell.clientdb.DocumentManager;
/**
 * Insert the type's description here.
 * Creation date: (5/31/2004 6:03:16 PM)
 * @author: Ion Moraru
 */
public class NewName extends AsynchClientTask {
	
	public NewName() {
		super("Getting document name", TASKTYPE_SWING_BLOCKING);
	}

/**
 * Insert the method's description here.
 * Creation date: (5/31/2004 6:04:14 PM)
 * @param hashTable java.util.Hashtable
 * @param clientWorker cbit.vcell.desktop.controls.ClientWorker
 */
public void run(Hashtable<String, Object> hashTable) throws java.lang.Exception {
	DocumentWindowManager documentWindowManager = (DocumentWindowManager)hashTable.get("documentWindowManager");
	VCDocument document = documentWindowManager.getVCDocument();
	if (document.getDocumentType() == VCDocument.MATHMODEL_DOC) {
		if (((MathModelWindowManager)documentWindowManager).hasUnappliedChanges()) {
			String msg = "Changes have been made in VCML Editor, please click \"Apply Changes\" or \"Cancel\" to proceed.";
			PopupGenerator.showErrorDialog(documentWindowManager, msg);
			throw UserCancelException.CANCEL_UNAPPLIED_CHANGES;			
		}
	}

	MDIManager mdiManager = (MDIManager)hashTable.get("mdiManager");
	String oldName = document.getName();
	
	User user = mdiManager.getFocusedWindowManager().getRequestManager().getDocumentManager().getUser();
	DocumentManager documentManager = mdiManager.getFocusedWindowManager().getRequestManager().getDocumentManager();
	VCDocumentInfo[] vcDocumentInfos = new VCDocumentInfo[0];
	String documentTypeDescription = "unknown";
	if(document.getDocumentType() == VCDocument.MATHMODEL_DOC){
		documentTypeDescription = "MathModel";
		vcDocumentInfos = documentManager.getMathModelInfos();
	}else if(document.getDocumentType() == VCDocument.BIOMODEL_DOC){
		documentTypeDescription = "BioModel";
		vcDocumentInfos = documentManager.getBioModelInfos();
	}else if(document.getDocumentType() == VCDocument.GEOMETRY_DOC){
		documentTypeDescription = "Geometry";
		vcDocumentInfos = documentManager.getGeometryInfos();
	}
	String newDocumentName = (oldName==null?"New"+documentTypeDescription:oldName);
	while(true){
		newDocumentName =
			mdiManager.getDatabaseWindowManager().showSaveDialog(
					document.getDocumentType(),
					(Component)hashTable.get("currentDocumentWindow"),
					newDocumentName);
			if (newDocumentName == null || newDocumentName.trim().length()==0){
				newDocumentName = null;
				DialogUtils.showWarningDialog(
						(Component)hashTable.get("currentDocumentWindow"),
						"New "+documentTypeDescription+" name cannot be empty.");
				continue;
			}
			//Check name conflict
			boolean bNameConflict = false;
			for (int i = 0; i < vcDocumentInfos.length; i++) {
				if(vcDocumentInfos[i].getVersion().getOwner().compareEqual(user)){
					if(vcDocumentInfos[i].getVersion().getName().equals(newDocumentName)){
						bNameConflict = true;
						break;
					}
				}
			}
			if(bNameConflict){
				DialogUtils.showWarningDialog((Component)hashTable.get("currentDocumentWindow"),
				"A "+documentTypeDescription+" with name '"+newDocumentName+"' already exists.  Choose a different name.");
				continue;
			}else{
				break;
			}
		}
	document.setName(newDocumentName);
	hashTable.put("newName", newDocumentName);
}

}