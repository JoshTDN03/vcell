package edu.uchc.vcell.expression.internal;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
/* JJT: 0.2.2 */
import net.sourceforge.interval.ia_math.IANarrow;
import net.sourceforge.interval.ia_math.RealInterval;

import org.vcell.expression.ExpressionBindingException;
import org.vcell.expression.ExpressionException;
import org.vcell.expression.NameScope;
import org.vcell.expression.SymbolTable;
import org.vcell.expression.SymbolTableEntry;

public class ASTAssignNode extends SimpleNode {

	static {
		System.out.println("ASTAssignNode LHS is not bound to symbols, used for display purposes only");
	};

  ASTAssignNode() {
	super(-1);
  }    
//
// ignore symbol info for LHS (assumed only for annotation purposes)
//

public void bind(SymbolTable symbolTable) throws ExpressionBindingException {
    jjtGetChild(1).bind(symbolTable);
}
public String code() throws ExpressionException {
	StringBuffer buffer = new StringBuffer();

	if (jjtGetNumChildren()!=2){
		throw new ExpressionException("ASTAssignNode, expected 2 children");
	}	

	buffer.append(jjtGetChild(0).code());
	buffer.append(" = ");
	buffer.append(jjtGetChild(1).code());

	return buffer.toString();
}            
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Node
 * @exception java.lang.Exception The exception description.
 */
public Node copyTree() {
	ASTAssignNode node = new ASTAssignNode();
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
	ASTAssignNode node = new ASTAssignNode();
	for (int i=0;i<jjtGetNumChildren();i++){
		node.jjtAddChild(jjtGetChild(i).copyTreeBinary());
	}
	return node;	
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 * @param independentVariable java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public Node differentiate(String independentVariable) throws ExpressionException {
	throw new ExpressionException("differentiation not supported");	 
}
public double evaluateConstant() throws ExpressionException {
	throw new ExpressionException("AssignNode cannot be evaluated as a constant");
}    
public RealInterval evaluateInterval(RealInterval intervals[]) throws ExpressionException {
	if (jjtGetNumChildren()!=2){
		throw new RuntimeException("Expected two children");
	}

	Node lhs = jjtGetChild(0);
	Node rhs = jjtGetChild(1);
	lhs.setInterval(rhs.evaluateInterval(intervals),intervals);
	setInterval(lhs.getInterval(intervals),intervals);
	return getInterval(intervals);
}    
public double evaluateVector(double values[]) throws ExpressionException {
	throw new ExpressionException("ASTAssignNode.evaluateVector() undefined operation, assign lvalue???");
/*
	double eval = jjtGetChild(1).interpret();
//	jjtGetChild(0).assign(eval); 
	return eval;	 
*/
}    
/**
 * This method was created by a SmartGuide.
 * @exception java.lang.Exception The exception description.
 */
public Node flatten() throws ExpressionException {

	ASTAssignNode assignNode = new ASTAssignNode();
	
	assignNode.jjtAddChild(jjtGetChild(0).flatten());
	assignNode.jjtAddChild(jjtGetChild(1).flatten());

	return assignNode;
}
/**
 *
 * ignore symbol info for LHS (assumed only for annotation purposes)
 *
 * @return cbit.vcell.parser.SymbolTableEntry
 * @param symbol java.lang.String
 */
public SymbolTableEntry getBinding(String symbol) {
	return jjtGetChild(1).getBinding(symbol);
}
/**
 *
 * ignore symbol info for LHS (assumed only for annotation purposes)
 *
 * @return java.lang.String[]
 * @exception java.lang.Exception The exception description.
 */
public String[] getSymbols(int language, NameScope nameScope) {
	return jjtGetChild(1).getSymbols(language,nameScope);
}
public String infixString(int lang, NameScope nameScope) {
	StringBuffer buffer = new StringBuffer();
	 
	buffer.append(jjtGetChild(0).infixString(lang, nameScope));
	if (lang == LANGUAGE_ECLiPSe){
		buffer.append(" $= ");
	}else{
		buffer.append(" == ");
	}
	buffer.append(jjtGetChild(1).infixString(lang, nameScope));

	return buffer.toString();
}        
/**
 * Insert the method's description here.
 * Creation date: (6/20/01 11:04:41 AM)
 * @return boolean
 */
public boolean narrow(RealInterval intervals[]) throws ExpressionBindingException{
	if (jjtGetNumChildren()!=2){
		throw new RuntimeException("Expected two children");
	}

	return IANarrow.narrow_colon_equals(getInterval(intervals),jjtGetChild(0).getInterval(intervals),jjtGetChild(1).getInterval(intervals))
			&& jjtGetChild(0).narrow(intervals)
			&& jjtGetChild(1).narrow(intervals)
			&& IANarrow.narrow_colon_equals(getInterval(intervals),jjtGetChild(0).getInterval(intervals),jjtGetChild(1).getInterval(intervals));
}
/**
 *
 * ignore symbol info for LHS (assumed only for annotation purposes)
 *
 */
public void substituteBoundSymbols() throws ExpressionException {
    jjtGetChild(1).substituteBoundSymbols();
}
}
