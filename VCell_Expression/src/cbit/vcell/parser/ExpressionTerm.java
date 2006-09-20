package cbit.vcell.parser;

/**
 * Insert the type's description here.
 * Creation date: (1/23/2003 7:26:12 PM)
 * @author: Jim Schaff
 */
public class ExpressionTerm {
	private java.lang.String fieldOperator = null;
	private cbit.vcell.parser.Expression[] fieldOperands = null;
/**
 * ExpressionTerm constructor comment.
 */
public ExpressionTerm(String operator, Expression operands[]) {
	super();
	this.fieldOperator = operator;
	this.fieldOperands = operands;
}
/**
 * Gets the operands property (cbit.vcell.parser.Expression[]) value.
 * @return The operands property value.
 * @see #setOperands
 */
public cbit.vcell.parser.Expression[] getOperands() {
	return fieldOperands;
}
/**
 * Gets the operator property (java.lang.String) value.
 * @return The operator property value.
 * @see #setOperator
 */
public java.lang.String getOperator() {
	return fieldOperator;
}
}
