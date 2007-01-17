package cbit.vcell.math.gui;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.vcell.desktop.Annotation;
import cbit.vcell.desktop.BioModelNode;
import java.awt.Font;
import cbit.vcell.math.*;
import cbit.vcell.desktop.VCellBasicCellRenderer;
/**
 * Insert the type's description here.
 * Creation date: (7/27/2000 6:30:41 PM)
 * @author: 
 */
import javax.swing.*;
 
public class MathDescriptionCellRenderer extends VCellBasicCellRenderer {
/**
 * MyRenderer constructor comment.
 */
public MathDescriptionCellRenderer() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (7/27/2000 6:41:57 PM)
 * @return java.awt.Component
 */
public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	JLabel component = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	boolean bLoaded = false;
	//
	try {
	if (value instanceof BioModelNode) {
		BioModelNode node = (BioModelNode) value;
		//

		if (node.getUserObject() instanceof PdeEquation) {
			PdeEquation pdeEquation = (PdeEquation)node.getUserObject();
			Variable var = pdeEquation.getVariable();
			component.setToolTipText("PDE Equation");
			//   \u2207 = nabla ... del operator
			//   \u2219 = dot
			String DEL = "\u2207";
			String PARTIAL_DIFF = "d"; //"\u2202";     //  '\u2202' is partial differentiation    'd' is regular diff
			String Super2 = "\u00b2";
			String DOT = "\u2219";
			//String diffusionTerm = DEL+" "+DOT+" "+"("+pdeEquation.getDiffusionExpression()+" "+DEL+" "+var.getName()+")";
			String diffusionTerm = "";
			if (pdeEquation.getVelocityX()!=null || pdeEquation.getVelocityY()!=null || pdeEquation.getVelocityZ()!=null){
				if (pdeEquation.getDiffusionExpression().isZero()){
					// reaction/advection
					diffusionTerm = "- "+DEL+" "+DOT+"( velocity "+var.getName()+" )";
				}else{
					// reaction/diffusion/advection
					diffusionTerm = DEL+" "+DOT+" ("+pdeEquation.getDiffusionExpression().infix()+" "+DEL+"  "+var.getName()+"   -   velocity "+var.getName()+")";
				}
			}else{
				diffusionTerm = "("+pdeEquation.getDiffusionExpression().infix()+") "+DEL+Super2+" "+var.getName();
			}
			String sourceTerm = pdeEquation.getRateExpression().infix();
			if (!sourceTerm.equals("0.0")){
				component.setText(PARTIAL_DIFF+"["+var.getName()+"]/"+PARTIAL_DIFF+"t = "+diffusionTerm+" + "+sourceTerm);
			}else{
				component.setText(PARTIAL_DIFF+"["+var.getName()+"]/"+PARTIAL_DIFF+"t = "+diffusionTerm);
			}
		} else if (node.getUserObject() instanceof OdeEquation) {
			OdeEquation odeEquation = (OdeEquation)node.getUserObject();
			Variable var = odeEquation.getVariable();
			component.setToolTipText("ODE Equation");
			component.setText("d["+var.getName()+"]/dt = "+odeEquation.getRateExpression().infix());
		} else if (node.getUserObject() instanceof MembraneRegionEquation) {
			MembraneRegionEquation membraneRegionEquation = (MembraneRegionEquation)node.getUserObject();
			Variable var = membraneRegionEquation.getVariable();
			component.setToolTipText("Membrane Region Equation");
			component.setText("Membrane Region Equation for "+var.getName());
		} else if (node.getUserObject() instanceof JumpCondition) {
			JumpCondition jumpCondition = (JumpCondition)node.getUserObject();
			Variable var = jumpCondition.getVariable();
			component.setToolTipText("Jump Condition");
			component.setText("Flux for "+var.getName());
		} else if (node.getUserObject() instanceof Constant) {
			Constant constant = (Constant)node.getUserObject();
			component.setToolTipText("Constant");
			component.setText(constant.getName()+" = "+constant.getExpression().infix());
		} else if (node.getUserObject() instanceof Function) {
			Function function = (Function)node.getUserObject();
			component.setToolTipText("Function");
			component.setText(function.getName()+" = "+function.getExpression().infix());
		} else if (node.getUserObject() instanceof SubDomain) {
			SubDomain subDomain = (SubDomain)node.getUserObject();
			component.setToolTipText("SubDomain");
			component.setText(subDomain.getName());
		} else if (node.getUserObject() instanceof FastSystem) {
			component.setToolTipText("Fast System");
			component.setText("Fast System");
		} else if (node.getUserObject() instanceof FastInvariant) {
			FastInvariant fi = (FastInvariant)node.getUserObject();
			component.setToolTipText("Fast Invariant");
			component.setText("fast invariant: "+fi.getFunction().infix());
		} else if (node.getUserObject() instanceof FastRate) {
			FastRate fr = (FastRate)node.getUserObject();
			component.setToolTipText("Fast Rate");
			component.setText("fast rate: "+fr.getFunction().infix());
		} else if (node.getUserObject() instanceof Annotation) {
			Annotation annotation = (Annotation)node.getUserObject();
			component.setToolTipText("Annotation");
			component.setText("\""+annotation+"\"");
		} else{
		}
		if (selectedFont==null && component.getFont()!=null) { selectedFont = component.getFont().deriveFont(Font.BOLD); }
		if (unselectedFont==null && component.getFont()!=null) { unselectedFont = component.getFont().deriveFont(Font.PLAIN); }
		
		if (bLoaded){
			component.setFont(selectedFont);
		}else{
			component.setFont(unselectedFont);
		}
	}
	}catch (Throwable e){
		e.printStackTrace(System.out);
	}
	//
	return component;
}
}
