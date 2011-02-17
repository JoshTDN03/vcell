package cbit.vcell.constraints.gui;

import cbit.vcell.constraints.ConstraintContainerImpl;
import cbit.gui.graph.ElipseShape;
import cbit.gui.graph.visualstate.VisualState;
import cbit.gui.graph.visualstate.imp.ImmutableVisualState;
import java.awt.Dimension;
import java.awt.Graphics2D;

public abstract class ConstraintGraphNode extends ElipseShape {
	protected int radius = 8;
	private int degree = 0;

	public ConstraintGraphNode(ConstraintsGraphModel graphModel, int argDegree) {
		super(graphModel);
		defaultBG = java.awt.Color.white;
		defaultFGselect = java.awt.Color.black;
		backgroundColor = defaultBG;
		this.degree = argDegree;
	}

	@Override
	public VisualState createVisualState() {
		return new ImmutableVisualState(this, VisualState.PaintLayer.NODE);
	}

	protected ConstraintContainerImpl getConstraintContainerImpl() {
		return ((ConstraintsGraphModel)graphModel).getConstraintContainerImpl();
	}

	public int getDegree() {
		return degree;
	}

	@Override
	public Dimension getPreferedSizeSelf(Graphics2D g) {
		java.awt.FontMetrics fm = g.getFontMetrics();
		setLabelSize(fm.stringWidth(getLabel()), fm.getMaxAscent() + fm.getMaxDescent());
		getSpaceManager().setSizePreferred((radius*2), (radius*2));
		return getSpaceManager().getSizePreferred();
	}

	public void refreshLayoutSelf() {
		int centerX = getSpaceManager().getSize().width/2;
		labelPos.x = centerX - getLabelSize().width/2; 
		labelPos.y = 0;		
	}
	
	public void setDegree(int newDegree) {
		if (newDegree < 0){
			throw new IllegalArgumentException("degree must be non-negative");
		}
		degree = newDegree;
	}
}