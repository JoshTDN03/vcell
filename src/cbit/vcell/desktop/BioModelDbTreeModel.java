package cbit.vcell.desktop;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JTree;

import org.vcell.util.DataAccessException;
import org.vcell.util.document.BioModelInfo;
import org.vcell.util.document.GroupAccess;
import org.vcell.util.document.User;
import org.vcell.util.document.Version;

import cbit.vcell.clientdb.DatabaseEvent;
import cbit.vcell.desktop.VCellBasicCellRenderer.VCDocumentInfoNode;
/**
 * Insert the type's description here.
 * Creation date: (2/14/01 3:33:23 PM)
 * @author: Jim Schaff
 */
@SuppressWarnings("serial")
public class BioModelDbTreeModel extends VCDocumentDbTreeModel {		 
/**
 * BioModelDbTreeModel constructor comment.
 * @param root javax.swing.tree.TreeNode
 */
public BioModelDbTreeModel(JTree tree) {
	super(tree);
	tutorialModelsNode = new BioModelNode(Tutorials, true);
	educationModelsNode = new BioModelNode(Education, true);
	publicModelsNode = new BioModelNode(Public_BioModels, true);
}

/**
 * Insert the method's description here.
 * Creation date: (11/28/00 1:06:51 PM)
 * @return cbit.vcell.desktop.BioModelNode
 * @param docManager cbit.vcell.clientdb.DocumentManager
 */
protected void createBaseTree() throws DataAccessException {
	if (rootNode.getChildCount() == 0) {
		rootNode.add(myModelsNode);
		rootNode.add(sharedModelsNode);
		rootNode.add(publicModelsNode);
		rootNode.add(tutorialModelsNode);
		rootNode.add(educationModelsNode);
	}
	rootNode.setUserObject("Biological Models");
	sharedModelsNode.setUserObject(SHARED_BIO_MODELS);
	
	BioModelInfo bioModelInfos[] = getDocumentManager().getBioModelInfos();
	
	//
	// get list of users (owners)
	//
	Vector<User> userList = new Vector<User>();
	User loginUser = getDocumentManager().getUser();
	userList.addElement(loginUser);
	for (int i=0;i<bioModelInfos.length;i++){
		BioModelInfo bioModelInfo = bioModelInfos[i];
		if (!userList.contains(bioModelInfo.getVersion().getOwner())){
			userList.addElement(bioModelInfo.getVersion().getOwner());
		}
	}
	//
	// for each user
	//
	TreeMap<String, BioModelNode> treeMap = new TreeMap<String, BioModelNode>(new Comparator<String>() {

		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}		
	});
	for (int ownerIndex=0;ownerIndex<userList.size();ownerIndex++){
		User owner = (User)userList.elementAt(ownerIndex);
		BioModelNode ownerNode = createUserSubTree(owner, bioModelInfos);
		if(owner.equals(loginUser) || ownerNode.getChildCount() > 0){
			treeMap.put(owner.getName(), ownerNode);
		}
	}
	//
	// create final tree
	//
	BioModelNode ownerNode = (BioModelNode)treeMap.remove(loginUser.getName());
	myModelsNode.setUserObject(loginUser);
	myModelsNode.removeAllChildren();
	for (int c = 0; c < ownerNode.getChildCount();) {
		BioModelNode childNode = (BioModelNode) ownerNode.getChildAt(c);
		myModelsNode.add(childNode);
	}	
	sharedModelsNode.removeAllChildren();
	tutorialModelsNode.removeAllChildren();
	educationModelsNode.removeAllChildren();
	publicModelsNode.removeAllChildren();
	for (String username : treeMap.keySet()) {
		BioModelNode userNode = treeMap.get(username);
		BioModelNode parentNode = sharedModelsNode;
		boolean bSpecificUser = true;
		if (username.equals(USER_tutorial)) {
			parentNode = tutorialModelsNode;
		} else if (username.equals(USER_Education)) {
			parentNode = educationModelsNode;
		} else {
			bSpecificUser = false;
		}
		for (int c = 0; c < userNode.getChildCount();) {
			BioModelNode childNode = (BioModelNode) userNode.getChildAt(c);
			VCDocumentInfoNode vcdDocumentInfoNode = (VCDocumentInfoNode) childNode.getUserObject();
			if (!bSpecificUser) {
				parentNode = sharedModelsNode;
				BigDecimal groupid = GroupAccess.GROUPACCESS_NONE;
				Version version = vcdDocumentInfoNode.getVCDocumentInfo().getVersion();
				if (version != null && version.getGroupAccess() != null) {
					groupid = version.getGroupAccess().getGroupid();
				}
				if (groupid.equals(GroupAccess.GROUPACCESS_ALL)) {
					parentNode = publicModelsNode;
				}
			}
			// when added to other node, this childNode was removed from userNode
			parentNode.add(childNode);
		}
	}
}

/**
 * Insert the method's description here.
 * Creation date: (11/28/00 1:06:51 PM)
 * @return cbit.vcell.desktop.BioModelNode
 * @param docManager cbit.vcell.clientdb.DocumentManager
 */
private BioModelNode createUserSubTree(User user, BioModelInfo bioModelInfos[]) throws DataAccessException {
	//
	// for each user
	//
	BioModelNode rootNode = new BioModelNode(user,true);
	for (int i = 0; i < bioModelInfos.length; i ++){
		BioModelInfo bioModelInfo = bioModelInfos[i];
		
		if (bioModelInfo.getVersion().getOwner().equals(user)){
			if (!meetSearchCriteria(bioModelInfo)) {
				continue;
			}
			
			BioModelNode bioModelNode = new BioModelNode(new VCDocumentInfoNode(bioModelInfo),true);
			rootNode.add(bioModelNode);
			//
			// get list of bioModels with the same branch
			//
			Vector<BioModelInfo> bioModelBranchList = new Vector<BioModelInfo>();
			bioModelBranchList.addElement(bioModelInfo);
			for (i = i + 1; i < bioModelInfos.length; i ++){
				if (bioModelInfos[i].getVersion().getBranchID().equals(bioModelInfo.getVersion().getBranchID())){
					if (!meetSearchCriteria(bioModelInfos[i])) {
						continue;
					}
					bioModelBranchList.add(0,bioModelInfos[i]);
				}else{
					i--;
					break;
				}
			}
			BioModelInfo bioModelInfosInBranch[] = null;
			if (getLatestOnly()){
				bioModelInfosInBranch = new BioModelInfo[1];
				bioModelInfosInBranch[0] = (BioModelInfo)bioModelBranchList.elementAt(0);
			}else{
				bioModelInfosInBranch = new BioModelInfo[bioModelBranchList.size()];
				bioModelBranchList.copyInto(bioModelInfosInBranch);
			}
			for (int versionCount=0;versionCount<bioModelInfosInBranch.length;versionCount++){
				bioModelNode.add(createVersionSubTree(bioModelInfosInBranch[versionCount]));
			}
		}
	}
	return rootNode;
}
/**
 * Insert the method's description here.
 * Creation date: (11/28/00 2:41:43 PM)
 * @param bioModelNode cbit.vcell.desktop.BioModelNode
 * @param bioModelInfo cbit.vcell.biomodel.BioModelInfo
 */
private BioModelNode createVersionSubTree(BioModelInfo bmInfo) throws DataAccessException {
	BioModelNode versionNode = new BioModelNode(bmInfo,false);
	versionNode.setRenderHint(BioModelNode.MAX_ERROR_LEVEL, new Integer(getMaxErrorLevel(bmInfo)));
	return versionNode;
}
/**
 * 
 * @param event cbit.vcell.clientdb.DatabaseEvent
 */
public void databaseDelete(DatabaseEvent databaseEvent) {
	if (databaseEvent.getOldVersionInfo() instanceof BioModelInfo){
		BioModelInfo removedBioModelInfo = (BioModelInfo)databaseEvent.getOldVersionInfo();
		BioModelNode removedNode = ((BioModelNode)getRoot()).findNodeByUserObject(removedBioModelInfo);
		if (removedNode.getParent()!=null && removedNode.getSiblingCount()==1){ // just this one version
			removedNode = (BioModelNode)removedNode.getParent();
		}
		removeNodeFromParent(removedNode);
	}
}
/**
 * 
 * @param event cbit.vcell.clientdb.DatabaseEvent
 */
public void databaseInsert(DatabaseEvent databaseEvent) {
	if (databaseEvent.getNewVersionInfo() instanceof BioModelInfo){
		try {
			BioModelInfo insertedBioModelInfo = (BioModelInfo)databaseEvent.getNewVersionInfo();
			//
			// get parent of updated version
			//
			//   model1                           (VCDocumentInfoNode)
			//      Fri Sept 2, 2001 12:00:00     (BioModelInfo)
			//      Fri Sept 1, 2001 10:00:00     (BioModelInfo)
			//
			//
			BioModelNode newVersionNode = createVersionSubTree(insertedBioModelInfo);
			
			//
			// find owner node (if it is displayed)
			//
			User owner = insertedBioModelInfo.getVersion().getOwner();
			BioModelNode ownerRoot = ((BioModelNode)getRoot()).findNodeByUserObject(owner);
			BioModelNode parentNode = null;
			if (ownerRoot!=null){
				parentNode = ownerRoot.findNodeByUserObject(new VCDocumentInfoNode(insertedBioModelInfo));
			}
			
			if (parentNode==null){
				//
				// fresh insert 
				// Have to create parent node, for all versions of this biomodel, 
				// and stick it in the correct order in the tree.
				//
				parentNode = new BioModelNode(new VCDocumentInfoNode(insertedBioModelInfo),true);
				parentNode.insert(newVersionNode,0);
				//
				// if owner node exists, add BioModel parent and fire events to notify of the insertion
				//
				if (ownerRoot!=null){
					ownerRoot.insert(parentNode,0);  // !!!!!!!!!!!!!! new insert on top (index=0) .... should do insertion sort !!!!
					insertNodeInto(parentNode,ownerRoot,0);
				}
			}else{
				//
				// already versions there (just add child in the correct position within parent)
				//
				parentNode.insert(newVersionNode,0); // !!!!!!!!!! right now ignore order !!!!!!!!!!
				insertNodeInto(newVersionNode,parentNode,0);
			}
		} catch (DataAccessException e){
			e.printStackTrace(System.out);
			System.out.println("exception responding to databaseInsert(), refreshing whole tree");
			refreshTree();
		}
	}
}
/**
 * 
 * @param event cbit.vcell.clientdb.DatabaseEvent
 */
public void databaseRefresh(DatabaseEvent event) {

	//Our parent will tell us what to do
}
/**
 * 
 * @param event cbit.vcell.clientdb.DatabaseEvent
 */
public void databaseUpdate(DatabaseEvent databaseEvent) {
	//
	// the ClientDocumentManager usually throws an UPDATE when changing public/private status
	//
	if (databaseEvent.getOldVersionInfo() instanceof BioModelInfo){
		BioModelNode node = ((BioModelNode)getRoot()).findNodeByUserObject(databaseEvent.getOldVersionInfo());
		if (node != null) {
			node.setUserObject(databaseEvent.getNewVersionInfo());
			nodeChanged(node);
		}
	}
}

/**
 * Insert the method's description here.
 * Creation date: (11/6/2002 4:41:23 PM)
 * @return int
 * @param bioModelInfo cbit.vcell.biomodel.BioModelInfo
 * @deprecated
 */
private int getMaxErrorLevel(BioModelInfo bioModelInfo) {
	//if (getDocumentManager()!=null){
		//int worstErrorLevel = cbit.vcell.modeldb.SimContextStatus.ERROR_NONE;
		//cbit.vcell.clientdb.DocumentManager docManager = getDocumentManager();
		//try {
			//BioModelMetaData bmMetaData = docManager.getBioModelMetaData(bioModelInfo);
			//java.util.Enumeration enum1 = bmMetaData.getSimulationContextInfos();
			//while (enum1.hasMoreElements()){
				//SimulationContextInfo scInfo = (SimulationContextInfo)enum1.nextElement();
				//if (scInfo.getSimContextStatus()!=null){
					//worstErrorLevel = Math.max(worstErrorLevel,scInfo.getSimContextStatus().getErrorLevel());
				//}
			//}
			//return worstErrorLevel;
		//}catch (cbit.vcell.server.DataAccessException e){
			//e.printStackTrace();
		//}
	//}
	return BioModelNode.ERROR_NONE;
}

}
