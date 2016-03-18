package cbit.vcell.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;
import javax.swing.JButton;

import org.vcell.util.gui.JToolBarToggleButton;

import cbit.vcell.graph.ResizeCanvasShape.Sign;

public class StructureToolShape implements Icon {

	private enum State { normal, selected };

	private final State state;
	private final int diameter = 20;

	public StructureToolShape(State state) {
		super();
		this.state = state;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {

		if(c == null) {
			return;
		}
		if(!(c instanceof JToolBarToggleButton)) {
			return;
		}
		JToolBarToggleButton b = (JToolBarToggleButton)c;
		
		Graphics2D g2 = (Graphics2D)g;
		Color colorOld = g2.getColor();
		Paint paintOld = g2.getPaint();
		Stroke strokeOld = g2.getStroke();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(1.8f));
		
		Color c1, c2, c3;
		int xx, yy;
		if(state == State.normal) {
			c1 = SpeciesPatternLargeShape.componentBad.darker();
			c2 = Color.black;
			xx = x+1;
			yy = y+1;
		} else {		// button pressed
			c1 = SpeciesPatternLargeShape.componentBad;
			c2 = Color.red.darker().darker().darker();
//			c2 = SpeciesPatternLargeShape.componentBad.darker().darker().darker();
			xx = x+2;
			yy = y+2;
		}
		double w = 16;		// external red circle
		double h = 16;
		Ellipse2D e1 = new Ellipse2D.Double(xx, yy, w, h);
		GradientPaint gp1 = new GradientPaint(25, 25, c1, 35, 32, c2, true);
		g2.setPaint(gp1);
		g2.draw(e1);

		if(state == State.normal) {
			c1 = Color.black;
			c2 = SpeciesPatternLargeShape.componentGreen.darker();
			xx = x+5;
			yy = y+5;
		} else {		// button pressed
			c1 = Color.green.darker().darker().darker();
			c2 = SpeciesPatternLargeShape.componentGreen.darker();
			xx = x+6;
			yy = y+6;
		}
		w = 8;			// inner green circle
		h = 8;
		Ellipse2D e2 = new Ellipse2D.Double(xx, yy, w, h);
		GradientPaint gp2 = new GradientPaint(25, 25, c1, 28, 28, c2, true);
		g2.setPaint(gp2);
		g2.draw(e2);
	
		g2.setStroke(strokeOld);
		g2.setColor(colorOld);
		g2.setPaint(paintOld);
	}

	@Override
	public int getIconWidth() {
		return diameter;
	}
	@Override
	public int getIconHeight() {
		return diameter;
	}
	
	public static void setStructureToolMod(JToolBarToggleButton button) {
		ReactionCartoonEditorPanel.setToolBarButtonSizes(button);
		Icon iconNormal = new StructureToolShape(State.normal);
		Icon iconSelected = new StructureToolShape(State.selected);
		button.setName("StructureButton");
		button.setIcon(iconNormal);
		button.setSelectedIcon(iconSelected);
		button.setFocusPainted(false);
		button.setFocusable(false);
		button.setToolTipText("Structure Tool");
	}


}
