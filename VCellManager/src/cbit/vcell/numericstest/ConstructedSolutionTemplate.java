package cbit.vcell.numericstest;

import cbit.vcell.math.*;
import java.util.Enumeration;

import org.vcell.expression.ExpressionFactory;
import org.vcell.expression.IExpression;

/**
 * Insert the type's description here.
 * Creation date: (5/12/2003 10:53:40 AM)
 * @author: Anuradha Lakshminarayana
 */
public class ConstructedSolutionTemplate {
	private SolutionTemplate solutionTemplates[] = null;
/**
 * ConstructedSolutionTemplate constructor comment.
 */
public ConstructedSolutionTemplate(cbit.vcell.math.MathDescription mathDesc) {
	super();
	initialize(mathDesc);
}
/**
 * Insert the method's description here.
 * Creation date: (5/12/2003 10:55:37 AM)
 * @return cbit.vcell.parser.Expression
 * @param varName java.lang.String
 * @param domainName java.lang.String
 */
public SolutionTemplate getSolutionTemplate(String varName, String domainName) {
	for (int i = 0; i < solutionTemplates.length; i++){
		if (solutionTemplates[i].getVarName().equals(varName) && solutionTemplates[i].getDomainName().equals(domainName)){
			return solutionTemplates[i];
		}
	}
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (5/12/2003 10:55:37 AM)
 * @return cbit.vcell.parser.Expression
 * @param varName java.lang.String
 * @param domainName java.lang.String
 */
public SolutionTemplate[] getSolutionTemplates() {
	return solutionTemplates;
}
/**
 * Insert the method's description here.
 * Creation date: (5/12/2003 11:04:13 AM)
 * @param mathDesc cbit.vcell.math.MathDescription
 */
private void initialize(cbit.vcell.math.MathDescription mathDesc) {
	//
	// for all valid combinations of variables/domains ... add a solution template with a default solution.
	//
	java.util.Vector solutionTemplateList = new java.util.Vector();
	
	Enumeration enumSubDomains = mathDesc.getSubDomains();
	while (enumSubDomains.hasMoreElements()){
		SubDomain subDomain = (SubDomain)enumSubDomains.nextElement();
		Enumeration enumEquations = subDomain.getEquations();
		while (enumEquations.hasMoreElements()){
			Equation equation = (Equation)enumEquations.nextElement();
			Variable var = equation.getVariable();
			String baseName = " "+var.getName()+"_"+subDomain.getName();
			String amplitudeName = baseName+"_A";
			String tau1Name = baseName+"_tau1";
			String tau2Name = baseName+"_tau2";
			if (equation instanceof OdeEquation){
				try {
					IExpression exp = ExpressionFactory.createExpression(amplitudeName+" * (1.0 + exp(-t/"+tau1Name+")*sin(2*"+Math.PI+"/"+tau2Name+"*t))");
					solutionTemplateList.add(new SolutionTemplate(equation.getVariable().getName(),subDomain.getName(),exp));
				}catch (org.vcell.expression.ExpressionException e){
					e.printStackTrace(System.out);
					throw new RuntimeException(e.getMessage());
				}
			}else if (equation instanceof PdeEquation){
				try {
					IExpression exp = ExpressionFactory.createExpression(amplitudeName+" * (1.0 + exp(-t/"+tau1Name+") + "+tau2Name+"*x)");
					solutionTemplateList.add(new SolutionTemplate(equation.getVariable().getName(),subDomain.getName(),exp));
				}catch (org.vcell.expression.ExpressionException e){
					e.printStackTrace(System.out);
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}

	this.solutionTemplates = (SolutionTemplate[])cbit.util.BeanUtils.getArray(solutionTemplateList,SolutionTemplate.class);
}
}
