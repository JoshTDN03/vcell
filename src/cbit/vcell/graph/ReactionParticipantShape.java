package cbit.vcell.graph;
/*
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
 */
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import cbit.gui.graph.EdgeShape;
import cbit.gui.graph.GraphModel;
import cbit.gui.graph.Shape;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.ReactionParticipant;

public abstract class ReactionParticipantShape extends EdgeShape {
	protected ReactionParticipant reactionParticipant = null;

	private Point2D.Double lastp2ctrl = null;
	private Point2D.Double lastp1ctrl = null;

	public ReactionParticipantShape(ReactionParticipant reactionParticipant, ReactionStepShape reactionStepShape,
			SpeciesContextShape speciesContextShape, GraphModel graphModel) {
		super(speciesContextShape, reactionStepShape, graphModel);
		this.reactionParticipant = reactionParticipant;
	}

	@Override protected final CubicCurve2D.Double getCurve() {
		// TODO is this the best place for layout?
		refreshLayoutSelf();
		// default behavior of control points is for direction at ends to follow secant between end-points.
		if(lastCurve_Start == null || !lastCurve_Start.equals(start) ||
				lastCurve_End == null || !lastCurve_End.equals(end)){
			lastp1ctrl = 
				new Point2D.Double((1.0 - FRACT_WEIGHT)*start.getX() + FRACT_WEIGHT*end.getX(),
					(1.0 - FRACT_WEIGHT)*start.getY() + FRACT_WEIGHT*end.getY());
		}
		Point2D.Double p2ctrl = 
			new Point2D.Double(FRACT_WEIGHT*start.getX() + (1.0 - FRACT_WEIGHT)*end.getX(),
					FRACT_WEIGHT*start.getY() + (1.0 - FRACT_WEIGHT)*end.getY());
		// calculate tangent direction at "reactionStep"
		double tangentX = 0.0;
		double tangentY = 0.0;
		if (endShape instanceof ReactionStepShape){
			ReactionStepShape reactionStepShape = (ReactionStepShape) endShape;
			for(Shape shape : graphModel.getShapes()) {
				if (shape instanceof ReactionParticipantShape && 
						((ReactionParticipantShape) shape).endShape == reactionStepShape){
					ReactionParticipantShape rpShape = (ReactionParticipantShape)shape;
					double dx = rpShape.start.getX()-rpShape.end.getX();
					double dy = rpShape.start.getY()-rpShape.end.getY();
					double len = dx*dx+dy*dy;
					if (shape instanceof ProductShape){
						ProductShape ps = (ProductShape) shape;
						tangentX += (ps.start.getX() - ps.end.getX())/len;
						tangentY += (ps.start.getY() - ps.end.getY())/len;
					}else if (shape instanceof ReactantShape){
						ReactantShape rs = (ReactantShape) shape;
						tangentX -= (rs.start.getX() - rs.end.getX())/len;
						tangentY -= (rs.start.getY() - rs.end.getY())/len;
					}
				}
			}
		}
		double tangentLength = Math.sqrt(tangentX*tangentX + tangentY*tangentY);
		if (tangentLength != 0) {
			tangentX = tangentX*CONTROL_WEIGHT/tangentLength;
			tangentY = tangentY*CONTROL_WEIGHT/tangentLength;
		}
		//tangentX = controlWeight;
		//tangentY = 0.0;
		if(this instanceof CatalystShape){
			// choose side based on inner product with displacement vector between catalyst and reactionStep
			if(((start.getX() - end.getX())*tangentY - (start.getY() - end.getY())*tangentX) > 0){
				p2ctrl.setLocation(end.getX() + tangentY, end.getY() - tangentX);
			}else{
				p2ctrl.setLocation(end.getX() - tangentY, end.getY() + tangentX);
			}
		} else if(this instanceof ProductShape){
			p2ctrl.setLocation(end.getX()+tangentX, end.getY()+tangentY);	
		} else if(this instanceof ReactantShape){
			p2ctrl.setLocation(end.getX()-tangentX,end.getY()-tangentY);	
		} else if(this instanceof FluxShape){
			// choose side based on inner product with displacement vector between catalyst and reactionStep
			if(((start.getX() - end.getX())*tangentX + (start.getY() - end.getY())*tangentY) > 0){
				p2ctrl.setLocation(end.getX() + tangentX, end.getY() + tangentY);
			} else {
				p2ctrl.setLocation(end.getX() - tangentX, end.getY() - tangentY);
			}
		}

		if(lastCurve != null && 
				lastCurve_Start != null && lastCurve_Start.equals(start) &&
				lastCurve_End != null && lastCurve_End.equals(end) &&
				lastp2ctrl != null && lastp2ctrl.equals(p2ctrl)){
			//Do Nothing
		} else {		
			lastCurve = 
				new CubicCurve2D.Double(start.getX(), start.getY() ,lastp1ctrl.getX(), lastp1ctrl.getY(),
						p2ctrl.getX(),p2ctrl.getY(),end.getX(),end.getY());
			lastCurve_Start = new Point(start);
			lastCurve_End = new Point(end);
			lastp2ctrl = p2ctrl;
		}
		return lastCurve;
	}

	@Override public Object getModelObject() { return reactionParticipant; }
	
	public ReactionParticipant getReactionParticipant() { return reactionParticipant; }
	
	public ReactionStepShape getReactionStepShape() { return (ReactionStepShape) endShape; }

	@Override
	public void paintSelf(Graphics2D g2D, int parentOffsetX, int parentOffsetY) {
		// draw cubic spline with horizontal reactant-end (p' = 0) at reaction
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		CubicCurve2D.Double cubicCurve = getCurve();
		// render curve (make CatalystShapes draw with a dashed line)
		g2D.setColor(forgroundColor);
		if(getLineStyle() == LINE_STYLE_DASHED){
			Stroke oldStroke = g2D.getStroke();
			g2D.setStroke(DASHED_STROKE);
			g2D.draw(cubicCurve);
			g2D.setStroke(oldStroke);
		} else {
			g2D.draw(cubicCurve);
		}
		int arrowDirection = 0;
		if (this instanceof ProductShape){
			arrowDirection = 1;
		}
		if (this instanceof ReactantShape){
			arrowDirection = -1;
		}
		if (this instanceof FluxShape){
			FluxShape fluxShape = (FluxShape)this;
			if (fluxShape.getReactionStepShape() != null && reactionParticipant != null) {
				if (reactionParticipant.getSpeciesContext().getStructure() == ((Membrane)getReactionStepShape().getReactionStep().getStructure()).getInsideFeature()) {
					arrowDirection = 1;
				} else {
					arrowDirection = -1;
				}
			}
		}
		if (arrowDirection == 1) {
			double arcLength = integrateArcLength(cubicCurve, 0.0, 1.0, 10);
			double centerT = getParameterAtArcLength(cubicCurve, 0.0, 1.0, arcLength/2, 20);
			Point2D center = evaluate(cubicCurve, centerT);
			double backT = intersectWithCircle(cubicCurve, centerT, 1.0, center.getX(), center.getY(), 4);
			Point2D back = evaluate(cubicCurve, backT);
			double frontT = intersectWithCircle(cubicCurve, centerT, 0.0, center.getX(), center.getY(), 4);
			Point2D front = evaluate(cubicCurve,frontT);
			GeneralPath arrow = getArrow(front, back, 7);
			g2D.fill(arrow);
		}
		if (arrowDirection == -1){
			double arcLength = integrateArcLength(cubicCurve, 0.0, 1.0, 10);
			double centerT = getParameterAtArcLength(cubicCurve, 0.0, 1.0, arcLength/2+2, 20);
			Point2D center = evaluate(cubicCurve, centerT);
			double backT = intersectWithCircle(cubicCurve, centerT, 0.0, center.getX(), center.getY(), 4);
			Point2D back = evaluate(cubicCurve, backT);
			double frontT = intersectWithCircle(cubicCurve, centerT, 1.0, center.getX(), center.getY(), 4);
			Point2D front = evaluate(cubicCurve,frontT);
			GeneralPath arrow = getArrow(front,back,7);
			g2D.fill(arrow);
		}
		// draw label
		if (getLabel() != null && getLabel().length()>0) {
			g2D.drawString(getLabel(), (start.x + end.x) / 2, (start.y + end.y) / 2);
		}
		return;
	}
}