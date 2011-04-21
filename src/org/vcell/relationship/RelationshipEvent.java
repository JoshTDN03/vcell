package org.vcell.relationship;

import java.util.EventObject;

@SuppressWarnings("serial")
public class RelationshipEvent extends EventObject{
	private int operationType = CHANGED;

	private RelationshipObject relationshipObject;

	public static final int CHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 2;
	
	private static final String operationNames[] = { "changed", "added", "removed" };

	public RelationshipEvent(RelationshipModel relationshipModel, RelationshipObject relationshipObject, int operationType) {
		super(relationshipModel);
		this.relationshipObject = relationshipObject;
		this.operationType = operationType;
	}
	
	public int getOperationType(){
		return operationType;
	}

	public String getOperationName() {
		return operationNames[operationType];
	}

	@Override
	public String toString() {
		return "RelationshipEvent:" + getOperationName();
	}
	
	public RelationshipObject getRelationshipObject() {
		return relationshipObject;
	}
}
