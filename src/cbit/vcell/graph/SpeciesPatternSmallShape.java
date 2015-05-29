package cbit.vcell.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vcell.model.rbm.MolecularComponent;
import org.vcell.model.rbm.MolecularComponentPattern;
import org.vcell.model.rbm.MolecularTypePattern;
import org.vcell.model.rbm.SpeciesPattern;
import org.vcell.model.rbm.MolecularComponentPattern.BondType;
import org.vcell.model.rbm.SpeciesPattern.Bond;
import org.vcell.util.Displayable;
import org.vcell.util.Issue;

import cbit.vcell.client.desktop.biomodel.RbmTreeCellRenderer;
import cbit.vcell.graph.AbstractComponentShape.BondPair;

public class SpeciesPatternSmallShape extends AbstractComponentShape {

	private static final int separationWidth = 1;		// width between 2 molecular type patterns
	private int xPos = 0;
	private int yPos = 0;
	private int width = 0;
	private List<MolecularTypeSmallShape> speciesShapes = new ArrayList<MolecularTypeSmallShape>();
	private boolean isSelected = false;
	
	final Graphics graphicsContext;
	
	private Displayable owner;
	private SpeciesPattern sp;
	private String endText = new String();	// we display this after the Shape, it's position is outside "width"
	
	List <BondPair> bondPairs = new ArrayList <BondPair>();

	public SpeciesPatternSmallShape(int xPos, int yPos, SpeciesPattern sp, Graphics graphicsContext, Displayable owner,
			boolean isSelected) {
		this.owner = owner;
		this.sp = sp;
		this.xPos = xPos;
		this.yPos = yPos;
		this.graphicsContext = graphicsContext;
		this.isSelected = isSelected;

		int xPattern = xPos;
		if(sp == null) {
			// plain species context, no pattern
			MolecularTypeSmallShape stls = new MolecularTypeSmallShape(xPattern, yPos, graphicsContext, owner);
			speciesShapes.add(stls);
			return;
		}
		int numPatterns = sp.getMolecularTypePatterns().size();
		for(int i = 0; i<numPatterns; i++) {
			MolecularTypePattern mtp = sp.getMolecularTypePatterns().get(i);
			MolecularTypeSmallShape stls = new MolecularTypeSmallShape(xPattern, yPos, mtp, graphicsContext, owner);
			xPattern += stls.getWidth() + separationWidth; 
			speciesShapes.add(stls);
		}
		this.width = xPattern-xPos;
		
		// bonds - we have to deal with them here because they may be cross-molecular type patterns
		// WARNING: we assume that the order of the MolecularTypeLargeShapes in speciesShapes 
		// is the same as the order of the Molecular Type Patterns in the SpeciesPattern sp
		for(int i=0; i<numPatterns; i++) {
			MolecularTypeSmallShape stssFrom = speciesShapes.get(i);
			MolecularTypePattern mtpFrom = stssFrom.getMolecularTypePattern();
			int numComponents = mtpFrom.getComponentPatternList().size();
			for(int j=0; j<numComponents; j++) {
				MolecularComponentSmallShape mcssFrom = stssFrom.getComponentShape(j);
				MolecularComponent mcFrom = mtpFrom.getMolecularType().getComponentList().get(j);
				MolecularComponentPattern mcpFrom = mtpFrom.getMolecularComponentPattern(mcFrom);
				if(mcpFrom.getBondType().equals(BondType.Specified)) {
					Bond b = mcpFrom.getBond();
					if(b == null) {		// it's half of a bond at this time, we skip it for now
						System.out.println("Null bond for " + mcpFrom.getMolecularComponent().getDisplayName());
						break;
					}
					MolecularTypePattern mtpTo = b.molecularTypePattern;
					MolecularTypeSmallShape stssTo = getShape(mtpTo); 
					MolecularComponentPattern mcpTo = b.molecularComponentPattern;
					if(stssTo == null) {
						System.err.println("Null 'to' MolecularTypeSmallShape for " + mcpFrom.getMolecularComponent().getDisplayName());
						continue;
					}
					MolecularComponentSmallShape mcssTo = stssTo.getShape(mcpTo);
					if(mcssTo == null) {
						System.err.println("Null 'to' MolecularComponentSmallShape for " + mcpFrom.getMolecularComponent().getDisplayName());
						continue;
					}
					Point from = new Point(mcssFrom.getX()+mcssFrom.getWidth()/2, mcssFrom.getY()+mcssFrom.getHeight());
					Point to = new Point(mcssTo.getX()+mcssTo.getWidth()/2, mcssTo.getY()+mcssFrom.getHeight());
					if(from.x < to.x) {		// the bonds with from.x > to.x are duplicates
						BondPair bp = new BondPair(mcpFrom.getBondId(), from, to);
						bondPairs.add(bp);
					} 
				}
			}
		}
	}
	
	private MolecularTypeSmallShape getShape(MolecularTypePattern mtpThat) {
		for(MolecularTypeSmallShape stls : speciesShapes) {
			MolecularTypePattern mtpThis = stls.getMolecularTypePattern();
			if(mtpThis == mtpThat) {
				return stls;
			}
		}
		return null;
	}
	
	public SpeciesPattern getSpeciesPattern() {
		return sp;
	}

	public int getX(){
		return xPos;
	}
	public int getY(){
		return yPos;
	}
	public int getWidth(){
		return width;
	}
	public int getRightEnd(){		// get the x of the right end of the species pattern
		int xRightmostMolecularType = 0;
		int widthRightmostMolecularType = 0;
		
		for(MolecularTypeSmallShape stls : speciesShapes) {
			int xCurrentMolecularType = stls.getX();
			if(xRightmostMolecularType < xCurrentMolecularType) {
				xRightmostMolecularType = xCurrentMolecularType;
				widthRightmostMolecularType = stls.getWidth();
			}
		}
		return xRightmostMolecularType + widthRightmostMolecularType;
	}
	public void addEndText(final String string) {
		this.endText = string;
	}
	
	public void paintSelf(Graphics g) {
		final int offset = 2;			// initial lenth of vertical bar

		for(MolecularTypeSmallShape stls : speciesShapes) {
			stls.paintSelf(g);
		}

		Graphics2D g2 = (Graphics2D)g;
		Color colorOld = g2.getColor();
		Font fontOld = g.getFont();

		// bonds
		for(int i=0; i<bondPairs.size(); i++) {
			BondPair bp = bondPairs.get(i);

			if(isSelected) {
				g2.setColor(Color.white);
			} else {
				g2.setColor(Color.black);
			}
			g2.drawLine(bp.from.x, bp.from.y+1, bp.from.x, bp.from.y+offset);
			g2.drawLine(bp.to.x, bp.to.y+1, bp.to.x, bp.to.y+offset);
			g2.drawLine(bp.from.x, bp.from.y+offset, bp.to.x, bp.to.y+offset);

			g2.setFont(fontOld);
			g2.setColor(colorOld);
		}
		
		if(!endText.isEmpty()) {
			Font font = MolecularComponentLargeShape.deriveComponentFontBold(graphicsContext);
			Color fontColor = Color.black;
			g2.setFont(font);
			g2.setColor(fontColor);
			g2.drawString(endText, getRightEnd() + 4, yPos + 9);
		}

		g2.setFont(fontOld);
		g2.setColor(colorOld);
	}
}
