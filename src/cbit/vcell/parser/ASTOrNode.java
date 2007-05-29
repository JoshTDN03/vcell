package cbit.vcell.parser;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
/* JJT: 0.2.2 */
import net.sourceforge.interval.ia_math.*;

public class ASTOrNode extends SimpleNode {


ASTOrNode() {
	super(ExpressionParserTreeConstants.JJTORNODE);
}  
ASTOrNode(int id) {
	super(id);
if (id != ExpressionParserTreeConstants.JJTORNODE){ System.out.println("ASTOrNode(), id = "+id); }
}  

	public boolean isBoolean() {
		  return true;
	}

  public void bind(SymbolTable symbolTable) throws ExpressionBindingException
  {
	  super.bind(symbolTable);
	  setInterval(new RealInterval(0.0,1.0),null);  // either true or false
  }    
  /**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Node
 * @exception java.lang.Exception The exception description.
 */
public Node copyTree() {
	ASTOrNode node = new ASTOrNode();
	for (int i=0;i<jjtGetNumChildren();i++){
		node.jjtAddChild(jjtGetChild(i).copyTree());
	}
	return node;	
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Node
 * @exception java.lang.Exception The exception description.
 */
public Node copyTreeBinary() {
	int i=0;
	ASTOrNode node = new ASTOrNode();
	ASTOrNode rootNode = node;
	while (i<jjtGetNumChildren()){
		node.jjtAddChild(jjtGetChild(i++).copyTreeBinary());
		if (i<jjtGetNumChildren()-1){
			ASTOrNode tempNode = new ASTOrNode();
			node.jjtAddChild(tempNode);
			node = tempNode;
		}else{
			node.jjtAddChild(jjtGetChild(i++).copyTreeBinary());
			break;
		}
	}
	return rootNode;	
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 * @param independentVariable java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public Node differentiate(String independentVariable) throws ExpressionException {
	return new ASTFloatNode(0.0);
}
public double evaluateConstant() throws ExpressionException {
	double sum = 0;
	ExpressionException savedExpression = null;
	for (int i=0;i<jjtGetNumChildren();i++){
		try {
			if (jjtGetChild(i).evaluateConstant() != 0){
				return 1;
			}
		}catch (ExpressionException e){
			savedExpression = e;
		}
	}
	if (savedExpression!=null){
		throw savedExpression;
	}else{
		return sum;
	}
}    
public RealInterval evaluateInterval(RealInterval intervals[]) throws ExpressionException {
	if (jjtGetNumChildren()!=2){
		throw new ExpressionException("Expected two children");
	}
	RealInterval first = jjtGetChild(0).evaluateInterval(intervals);
	RealInterval second = jjtGetChild(1).evaluateInterval(intervals);
	setInterval(IAMath.vcell_or(first,second),intervals);
	return getInterval(intervals);
}    
public double evaluateVector(double values[]) throws ExpressionException {
	for (int i=0;i<jjtGetNumChildren();i++){
		if (jjtGetChild(i).evaluateVector(values) != 0){
			return 1.0;
		}
	}
	return 0.0;	 
}    
/**
 * This method was created by a SmartGuide.
 * @exception java.lang.Exception The exception description.
 */
public Node flatten() throws ExpressionException {

	try {
		double value = evaluateConstant();
		return new ASTFloatNode(value);
	}catch (Exception e){}		

	ASTOrNode orNode = new ASTOrNode();
	java.util.Vector<Node> tempChildren = new java.util.Vector<Node>();

	for (int i=0;i<jjtGetNumChildren();i++){
		tempChildren.addElement(jjtGetChild(i).flatten());
	}
	
	for (int j=0;j<tempChildren.size();j++){
		Node node = (SimpleNode)tempChildren.elementAt(j);
		if (node instanceof ASTFloatNode){
			if (node.evaluateConstant() != 0){
				return new ASTFloatNode(1.0);
			}
		}else if (node instanceof ASTOrNode){
			for (int i = 0; i < node.jjtGetNumChildren(); i++){
				tempChildren.add(node.jjtGetChild(i));
			}
			tempChildren.remove(node);
		}
	}

	for (int k=0;k<tempChildren.size();k++){
		orNode.jjtAddChild(tempChildren.elementAt(k));
	}

	return orNode;
}
  public String infixString(int lang, NameScope nameScope)
  {
	  StringBuffer buffer = new StringBuffer();
	 
	  buffer.append("(");

	  for (int i=0;i<jjtGetNumChildren();i++){
		if (i>0) {
			if (lang == LANGUAGE_ECLiPSe){
				buffer.append(" or ");
			}else{
				buffer.append(" || ");
			}
		}
		buffer.append(jjtGetChild(i).infixString(lang,nameScope));
	  }

	  buffer.append(")");

	  return buffer.toString();
  }    
/**
 * Insert the method's description here.
 * Creation date: (6/20/01 11:04:41 AM)
 * @return boolean
 */
public boolean narrow(RealInterval intervals[]) throws ExpressionBindingException {
	return IANarrow.vcell_narrow_or(getInterval(intervals),jjtGetChild(0).getInterval(intervals),jjtGetChild(1).getInterval(intervals))
			&& jjtGetChild(0).narrow(intervals)
			&& jjtGetChild(1).narrow(intervals)
			&& IANarrow.vcell_narrow_or(getInterval(intervals),jjtGetChild(0).getInterval(intervals),jjtGetChild(1).getInterval(intervals));
}
}
