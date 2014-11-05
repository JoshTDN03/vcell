package cbit.vcell.client.desktop.biomodel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.vcell.model.rbm.ComponentStateDefinition;
import org.vcell.model.rbm.ComponentStatePattern;
import org.vcell.model.rbm.MolecularComponent;
import org.vcell.model.rbm.MolecularComponentPattern;
import org.vcell.model.rbm.MolecularComponentPattern.BondType;
import org.vcell.model.rbm.MolecularType;
import org.vcell.model.rbm.MolecularTypePattern;
import org.vcell.model.rbm.SpeciesPattern.Bond;
import org.vcell.util.gui.VCellIcons;

import cbit.vcell.client.desktop.biomodel.RbmDefaultTreeModel.BondLocal;
import cbit.vcell.client.desktop.biomodel.RbmDefaultTreeModel.ReactionRuleParticipantLocal;
import cbit.vcell.client.desktop.biomodel.RbmDefaultTreeModel.SpeciesPatternLocal;
import cbit.vcell.client.desktop.biomodel.RbmDefaultTreeModel.StateLocal;
import cbit.vcell.client.desktop.biomodel.RbmTreeCellEditor.MolecularComponentPatternCellEditor;
import cbit.vcell.desktop.BioModelNode;
import cbit.vcell.model.RbmObservable;
import cbit.vcell.model.ReactionRule.ReactionRuleParticipantType;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.util.VCellErrorMessages;
@SuppressWarnings("serial")
public class RbmTreeCellRenderer extends DefaultTreeCellRenderer {
	
	protected Dimension componentPatternPreferredSizeWithWords = new Dimension(650, 50);
	protected Dimension componentPatternPreferredSizeWithoutWords = new Dimension(400, 50);
	
	// http://www.w3schools.com/cssref/css_colornames.asp
    public static final Color aliceblue = new Color(0xf0f8ff);
    public static final Color antiquewhite = new Color(0xfaebd7);
    public static final Color aqua = new Color(0x00ffff);
    public static final Color aquamarine = new Color(0x7fffd4);
    public static final Color azure = new Color(0xf0ffff);
    public static final Color beige = new Color(0xf5f5dc);
    public static final Color bisque = new Color(0xffe4c4);
    public static final Color black = new Color(0x000000);
    public static final Color blanchedalmond = new Color(0xffebcd);
    public static final Color blue = new Color(0x0000ff);
    public static final Color blueviolet = new Color(0x8a2be2);
    public static final Color brown = new Color(0xa52a2a);
    public static final Color burlywood = new Color(0xdeb887);
    public static final Color cadetblue = new Color(0x5f9ea0);
    public static final Color chartreuse = new Color(0x7fff00);
    public static final Color chocolate = new Color(0xd2691e);
    public static final Color coral = new Color(0xff7f50);
    public static final Color cornflowerblue = new Color(0x6495ed);
    public static final Color cornsilk = new Color(0xfff8dc);
    public static final Color crimson = new Color(0xdc143c);
    public static final Color cyan = new Color(0x00ffff);
    public static final Color darkblue = new Color(0x00008b);
    public static final Color darkcyan = new Color(0x008b8b);
    public static final Color darkgoldenrod = new Color(0xb8860b);
    public static final Color darkgray = new Color(0xa9a9a9);
    public static final Color darkgreen = new Color(0x006400);
    public static final Color darkkhaki = new Color(0xbdb76b);
    public static final Color darkmagenta = new Color(0x8b008b);
    public static final Color darkolivegreen = new Color(0x556b2f);
    public static final Color darkorange = new Color(0xff8c00);
    public static final Color darkorchid = new Color(0x9932cc);
    public static final Color darkred = new Color(0x8b0000);
    public static final Color darksalmon = new Color(0xe9967a);
    public static final Color darkseagreen = new Color(0x8fbc8f);
    public static final Color darkslateblue = new Color(0x483d8b);
    public static final Color darkslategray = new Color(0x2f4f4f);
    public static final Color darkturquoise = new Color(0x00ced1);
    public static final Color darkviolet = new Color(0x9400d3);
    public static final Color deeppink = new Color(0xff1493);
    public static final Color deepskyblue = new Color(0x00bfff);
    public static final Color dimgray = new Color(0x696969);
    public static final Color dodgerblue = new Color(0x1e90ff);
    public static final Color firebrick = new Color(0xb22222);
    public static final Color floralwhite = new Color(0xfffaf0);
    public static final Color forestgreen = new Color(0x228b22);
    public static final Color fuchsia = new Color(0xff00ff);
    public static final Color gainsboro = new Color(0xdcdcdc);
    public static final Color ghostwhite = new Color(0xf8f8ff);
    public static final Color gold = new Color(0xffd700);
    public static final Color goldenrod = new Color(0xdaa520);
    public static final Color gray = new Color(0x808080);
    public static final Color green = new Color(0x008000);
    public static final Color greenyellow = new Color(0xadff2f);
    public static final Color honeydew = new Color(0xf0fff0);
    public static final Color hotpink = new Color(0xff69b4);
    public static final Color indianred = new Color(0xcd5c5c);
    public static final Color indigo = new Color(0x4b0082);
    public static final Color ivory = new Color(0xfffff0);
    public static final Color khaki = new Color(0xf0e68c);
    public static final Color lavender = new Color(0xe6e6fa);
    public static final Color lavenderblush = new Color(0xfff0f5);
    public static final Color lawngreen = new Color(0x7cfc00);
    public static final Color lemonchiffon = new Color(0xfffacd);
    public static final Color lightblue = new Color(0xadd8e6);
    public static final Color lightcoral = new Color(0xf08080);
    public static final Color lightcyan = new Color(0xe0ffff);
    public static final Color lightgoldenrodyellow = new Color(0xfafad2);
    public static final Color lightgreen = new Color(0x90ee90);
    public static final Color lightgrey = new Color(0xd3d3d3);
    public static final Color lightpink = new Color(0xffb6c1);
    public static final Color lightsalmon = new Color(0xffa07a);
    public static final Color lightseagreen = new Color(0x20b2aa);
    public static final Color lightskyblue = new Color(0x87cefa);
    public static final Color lightslategray = new Color(0x778899);
    public static final Color lightsteelblue = new Color(0xb0c4de);
    public static final Color lightyellow = new Color(0xffffe0);
    public static final Color lime = new Color(0x00ff00);
    public static final Color limegreen = new Color(0x32cd32);
    public static final Color linen = new Color(0xfaf0e6);
    public static final Color magenta = new Color(0xff00ff);
    public static final Color maroon = new Color(0x800000);
    public static final Color mediumaquamarine = new Color(0x66cdaa);
    public static final Color mediumblue = new Color(0x0000cd);
    public static final Color mediumorchid = new Color(0xba55d3);
    public static final Color mediumpurple = new Color(0x9370db);
    public static final Color mediumseagreen = new Color(0x3cb371);
    public static final Color mediumslateblue = new Color(0x7b68ee);
    public static final Color mediumspringgreen = new Color(0x00fa9a);
    public static final Color mediumturquoise = new Color(0x48d1cc);
    public static final Color mediumvioletred = new Color(0xc71585);
    public static final Color midnightblue = new Color(0x191970);
    public static final Color mintcream = new Color(0xf5fffa);
    public static final Color mistyrose = new Color(0xffe4e1);
    public static final Color moccasin = new Color(0xffe4b5);
    public static final Color navajowhite = new Color(0xffdead);
    public static final Color navy = new Color(0x000080);
    public static final Color oldlace = new Color(0xfdf5e6);
    public static final Color olive = new Color(0x808000);
    public static final Color olivedrab = new Color(0x6b8e23);
    public static final Color orange = new Color(0xffa500);
    public static final Color orangered = new Color(0xff4500);
    public static final Color orchid = new Color(0xda70d6);
    public static final Color palegoldenrod = new Color(0xeee8aa);
    public static final Color palegreen = new Color(0x98fb98);
    public static final Color paleturquoise = new Color(0xafeeee);
    public static final Color palevioletred = new Color(0xdb7093);
    public static final Color papayawhip = new Color(0xffefd5);
    public static final Color peachpuff = new Color(0xffdab9);
    public static final Color peru = new Color(0xcd853f);
    public static final Color pink = new Color(0xffc0cb);
    public static final Color plum = new Color(0xdda0dd);
    public static final Color powderblue = new Color(0xb0e0e6);
    public static final Color purple = new Color(0x800080);
    public static final Color red = new Color(0xff0000);
    public static final Color rosybrown = new Color(0xbc8f8f);
    public static final Color royalblue = new Color(0x4169e1);
    public static final Color saddlebrown = new Color(0x8b4513);
    public static final Color salmon = new Color(0xfa8072);
    public static final Color sandybrown = new Color(0xf4a460);
    public static final Color seagreen = new Color(0x2e8b57);
    public static final Color seashell = new Color(0xfff5ee);
    public static final Color sienna = new Color(0xa0522d);
    public static final Color silver = new Color(0xc0c0c0);
    public static final Color skyblue = new Color(0x87ceeb);
    public static final Color slateblue = new Color(0x6a5acd);
    public static final Color slategray = new Color(0x708090);
    public static final Color snow = new Color(0xfffafa);
    public static final Color springgreen = new Color(0x00ff7f);
    public static final Color steelblue = new Color(0x4682b4);
    public static final Color tan = new Color(0xd2b48c);
    public static final Color teal = new Color(0x008080);
    public static final Color thistle = new Color(0xd8bfd8);
    public static final Color tomato = new Color(0xff6347);
    public static final Color turquoise = new Color(0x40e0d0);
    public static final Color violet = new Color(0xee82ee);
    public static final Color wheat = new Color(0xf5deb3);
    public static final Color white = new Color(0xffffff);
    public static final Color whitesmoke = new Color(0xf5f5f5);
    public static final Color yellow = new Color(0xffff00);
    public static final Color yellowgreen = new Color(0x9acd32);
    
	public static Color[] bondHtmlColors = {null, 
		blue,
		blueviolet,
		brown,
		burlywood ,
		cadetblue,
		chocolate,
		coral,
		cornflowerblue,
		crimson,
		darkblue,
		darkcyan,
		darkgoldenrod,
		darkgreen,
		darkmagenta,
		darkolivegreen,
		darkorange,
		darkorange,
		darkorchid,
		darkred,
		darksalmon,
		darkviolet,
		deeppink,
		deepskyblue,
		dodgerblue,
		firebrick,
		forestgreen,
		fuchsia,
		goldenrod,
		green,
		hotpink,
		indianred ,
		indigo ,
		magenta,
		maroon,
		mediumblue,
		mediumorchid,
		mediumseagreen,
		mediumvioletred,
		orange,
		orangered,
		royalblue,
		saddlebrown,
		salmon,
		seagreen,
		sienna,
		steelblue,
		teal,
		tomato,
	};
	
	public RbmTreeCellRenderer() {
		super();
		setBorder(new EmptyBorder(0, 2, 0, 0));		
	}
	
	private String toHtml(MolecularTypePattern mtp, MolecularComponent mc, boolean bSelected, boolean bShowWords) {
		String componentText = (bShowWords ? "Component" : "") + " <b>" + mc.getName() + "<sub>" + mc.getIndex() + "</sub></b>";
		String stateText = "";
		String bondText = "";
		MolecularComponentPattern mcp = mtp.getMolecularComponentPattern(mc);
		if (mcp != null /*&& !mcp.isImplied()*/) {			
			if (mcp.getComponentStatePattern() == null) {
				//stateText = "State(-): <b>NA</b>";
				stateText = "";
			} else if(mcp.getComponentStatePattern().isAny()) {
				if(bShowWords) {
					stateText = "State(~): <b>Any</b>";
				} else {
//					stateText = "<b>Any</b>";
				}
			} else {
				if(bShowWords) {
					stateText = "State(~): <b>" + mcp.getComponentStatePattern().getComponentStateDefinition().getName() + "</b>";
				} else {
					stateText = "~ <b>" + mcp.getComponentStatePattern().getComponentStateDefinition().getName() + "</b>";
				}
			}
			BondType bondType = mcp.getBondType();
			switch (bondType) {
			case None:
			case Possible:
			case Exists:
				bondText = "Bond(!" + bondType.symbol + "): <b>" + bondType.name()+ "</b>";
				break;
			case Specified:
				Bond bond = mcp.getBond();
				if (bond == null) {
					bondText = "Bond(!" + bondType.symbol + "): <b>" + mcp.getBondId() + "</b>";
				} else {
					int id = mcp.getBondId();
					String colorTextStart = bSelected ? "" : "<font color=" + "\"rgb(" + bondHtmlColors[id].getRed() + "," + bondHtmlColors[id].getGreen() + "," + bondHtmlColors[id].getBlue() + ")\">";
					String colorTextEnd = bSelected ? "" : "</font>";
					if(bShowWords) {
						bondText = "Bond(" + colorTextStart + "<b>! " + mcp.getBondId() + "</b>" + colorTextEnd + "):<sub>&nbsp;</sub>" + colorTextStart 
							+ "Molecule <b>" + bond.molecularTypePattern.getMolecularType().getName() 
							+ "<sub>" + bond.molecularTypePattern.getIndex() + "</sub></b> Component <b>" 
							+ bond.molecularComponentPattern.getMolecularComponent().getName() + "<sub>" 
							+ bond.molecularComponentPattern.getMolecularComponent().getIndex() + "</sub></b>"
							+ colorTextEnd;
					} else {
						bondText = "Bond(" + colorTextStart + "<b>! " + mcp.getBondId() + "</b>" + colorTextEnd + "):<sub>&nbsp;</sub>" + colorTextStart 
								+ " <b>" + bond.molecularTypePattern.getMolecularType().getName() 
								+ "<sub>" + bond.molecularTypePattern.getIndex() + "</sub></b> <b>" 
								+ bond.molecularComponentPattern.getMolecularComponent().getName() + "<sub>" 
								+ bond.molecularComponentPattern.getMolecularComponent().getIndex() + "</sub></b>"
								+ colorTextEnd;
					}
				}
				break;
			}
		}
		String htmlText = "<html><table width=" + (bShowWords ? componentPatternPreferredSizeWithWords.getWidth() : componentPatternPreferredSizeWithoutWords.getWidth()) + "><tr>" +
				"<td width=" + (bShowWords ? 25 : 15) + "%>" + componentText + "</td>" +	// numbers are percent
				"<td width=25%>" + stateText + "<sub>&nbsp</sub>" + "</td>" +
				"<td width=" + (bShowWords ? 50 : 60) + "%>" + bondText + "<sub>&nbsp</sub>" + "</td>" +
				"</tr></table></font></html>";
		return htmlText;
	}
	
//	public static String toHtml(MolecularTypePattern mtp, boolean bShowWords) {
//		return "<html> " + (bShowWords ? "Molecule" : "") + " <b>" + mtp.getMolecularType().getName() + "<sub>" + mtp.getIndex() + "</sub></b></html>";
//	}
	
	@Override
	public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {	
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		setBorder(null);
		if (value instanceof BioModelNode) {
			BioModelNode node = (BioModelNode)value;
			Object userObject = node.getUserObject();
			String text = null;
			Icon icon = null;
			String toolTip = null;
			if (userObject instanceof MolecularType) {
				MolecularType mt = (MolecularType) userObject;
				text = RbmTreeCellRenderer.toHtml(mt);
				icon = VCellIcons.rbmMolecularTypeIcon;
			} else if (userObject instanceof MolecularTypePattern) {
				MolecularTypePattern molecularTypePattern = (MolecularTypePattern) userObject;
				boolean bShowWords = node.getParent() == null || !(((BioModelNode)node.getParent()).getUserObject() instanceof ReactionRuleParticipantLocal);
				text = toHtml(molecularTypePattern, bShowWords);
				toolTip = toHtml(molecularTypePattern, true);
				icon = VCellIcons.rbmMolecularTypeIcon;
			} else if (userObject instanceof MolecularComponent) {
				BioModelNode parentNode = (BioModelNode) node.getParent();
				Object parentObject = parentNode == null ? null : parentNode.getUserObject();
				icon = VCellIcons.rbmMolecularComponentIcon;
				MolecularComponent mc = (MolecularComponent) userObject;
				
				if (parentObject instanceof MolecularType) {
					text = RbmTreeCellRenderer.toHtml(mc, true);
					FontMetrics fm = getFontMetrics(getFont());		// here is how to set the cell minimum size !!!
				    int width = fm.stringWidth(text);
				    setMinimumSize(new Dimension(width + 50, fm.getHeight() + 5));
				} else if (parentObject instanceof MolecularTypePattern) {
					//setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0), GuiConstants.TAB_PANEL_BORDER));
					MolecularTypePattern mtp = (MolecularTypePattern) parentObject;
					boolean bShowWords = parentNode.getParent() == null || !(((BioModelNode)parentNode.getParent()).getUserObject() instanceof ReactionRuleParticipantLocal);
					text = toHtml(mtp, mc, sel, bShowWords);
					toolTip = toHtml(mtp, mc, sel, true);
					if(!mtp.getMolecularComponentPattern(mc).isbVisible()) {
						BioModelNode parentNode2 = (BioModelNode)parentNode.getParent();
						Object parentObject2 = parentNode2 == null ? null : parentNode2.getUserObject();
						if(parentObject2 instanceof SpeciesContext) {
							// we want to change the icon only for the species patterns of species contexts
							// but not for the species patterns of observables / reaction rules
							icon = VCellIcons.rbmMolecularComponentErrIcon;
						}
					}
				}
			} else if (userObject instanceof MolecularComponentPattern) {
				MolecularComponentPattern mcp = (MolecularComponentPattern) userObject;
				text = mcp.getMolecularComponent().getName();
				icon = VCellIcons.rbmMolecularComponentIcon;
//			} else if (userObject instanceof ComponentStatePattern) {
//				ComponentStatePattern cs = (ComponentStatePattern) userObject;
//				text = RbmTreeCellRenderer.toHtml(cs);
//				//icon = VCellIcons.rbmComponentStateIcon;
			} else if (userObject instanceof ComponentStateDefinition) {
				ComponentStateDefinition cs = (ComponentStateDefinition) userObject;
				text = RbmTreeCellRenderer.toHtml(cs);
				icon = VCellIcons.rbmComponentStateIcon;
			} else if (userObject instanceof RbmObservable) {
				RbmObservable ob = (RbmObservable) userObject;
				text = ob.getName();
				icon = VCellIcons.rbmObservableIcon;
				toolTip = "Observable: " + text + " (" + ob.getType() + ")";
			} else if (userObject instanceof ReactionRuleParticipantLocal) {
				ReactionRuleParticipantLocal rrp = (ReactionRuleParticipantLocal) userObject;
				text = rrp.type.name() + " " + rrp.index;
				icon = rrp.type == ReactionRuleParticipantType.Reactant ? VCellIcons.rbmReactantIcon : VCellIcons.rbmProductIcon;
			}
			setText(text);
			setIcon(icon);
			setToolTipText(toolTip == null ? text : toolTip);
		}
		return this;
	}

//	static String toHtml(Bond bond) {
//		return "<html>" + toHtml0(bond) + "</html>";
//	}
	private static String toHtml0(Bond bond) {
//		return "Molecule <b>" + bond.molecularTypePattern.getMolecularType().getName() 
//				+ "<sub>" + bond.molecularTypePattern.getIndex() + "</sub></b> Component <b>" +
//				bond.molecularComponentPattern.getMolecularComponent().getName() + "<sub>" + bond.molecularComponentPattern.getMolecularComponent().getIndex() + "</sub></b>";
		return " <b>" + bond.molecularTypePattern.getMolecularType().getName() 
				+ "<sub>" + bond.molecularTypePattern.getIndex() + "</sub></b> (<b>" +
				bond.molecularComponentPattern.getMolecularComponent().getName() + "<sub>" + bond.molecularComponentPattern.getMolecularComponent().getIndex() + "</sub></b>)";
	}
	
	static String bondToHtml(MolecularComponentPattern mcp, boolean bSelected) {
		BondType defaultType = BondType.Possible;
		String bondText = "Bond(!" + defaultType.symbol + "): <b>" + BondType.Possible.name() + "</b>";
		if (mcp != null) {			
			BondType bondType = mcp.getBondType();
			if (bondType == BondType.Specified) {
				Bond bond = mcp.getBond();
				if (bond == null) {
					bondText = "";
				} else {
					int id = mcp.getBondId();
					String colorTextStart = bSelected ? "" : "<font color=" + "\"rgb(" + bondHtmlColors[id].getRed() + "," + bondHtmlColors[id].getGreen() + "," + bondHtmlColors[id].getBlue() + ")\">";
					String colorTextEnd = bSelected ? "" : "</font>";
					bondText = "Bond(" + colorTextStart + "<b>! " + mcp.getBondId() + "</b>" + colorTextEnd + "):<sub>&nbsp;</sub>" + colorTextStart 
							+ toHtml0(bond) + colorTextEnd;
				}
			} else {
				bondText = "Bond(!" + bondType.symbol + "): <b>" + bondType.name()+ "</b>";						
			}
		}
		String htmlText = "<html>" + bondText + "</html>";
		return htmlText;
	}

	private static String toHtml(MolecularType mt) {
		return "<html> Molecule <b>" + mt.getName() + "</sub></b></html>";
//		return "<html><b>" + mt.getName() + "</sub></b></html>";
	}

	static String toHtml(MolecularComponent mc, boolean bShowWords) {
		return "<html> " + (bShowWords ? "Component" : "") + " <b>" + mc.getName() + "<sub>" + mc.getIndex() + "</sub></b> </html>";
	}
	static String toHtml(MolecularComponent mc, int owner) {
		String text = null;
		switch (owner) {
		case MolecularComponentPatternCellEditor.species:
		case MolecularComponentPatternCellEditor.observable:
			text = "<html> " + "Component" + " <b>" + mc.getName() + "<sub>" + mc.getIndex() + "</sub></b></html>";
			break;
		case MolecularComponentPatternCellEditor.reaction:
			text = "<html> " + "" + " <b>" + mc.getName() + "<sub>" + mc.getIndex() + "</sub></b></html>";
			break;
		default:
			text = "<html> " + "Component" + " <b>" + mc.getName() + "<sub>" + mc.getIndex() + "</sub></b></html>";
			break;
		}
		return text;
	}

	private static String toHtml(ComponentStateDefinition cs) {
		return "<html>State <b>" + cs.getName() + "</b></html>";
	}
	
	// ====================================================================================
	public static final String toHtml(ReactionRuleParticipantLocal rrp, boolean bShowWords) {
		String text = rrp.speciesPattern.getSpeciesPattern().toString();
		String htmlText = rrp.type.name() + " " + rrp.index + ": <b>" + text + "</b>";
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}
	public static final String toHtmlWithTip(ReactionRuleParticipantLocal rrp, boolean bShowWords) {
		String text = rrp.speciesPattern.getSpeciesPattern().toString();
		text =  rrp.type.name() + " " + rrp.index + ": " + text;
		String htmlText = text + VCellErrorMessages.RightClickToAddMolecules;
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}
	public static final String toHtml(SpeciesPatternLocal spl, boolean bShowWords) {
//		String text = "SpeciesPattern " + spl.index;
		String text = spl.speciesPattern.toString();
		String htmlText = "SpeciesPattern " + spl.index + ": " + "<b>" + text + "</b>";
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}
	public static final String toHtmlWithTip(SpeciesPatternLocal spl, boolean bShowWords) {
		String text = spl.speciesPattern.toString();
		text = "SpeciesPattern " + spl.index + ": " + text;
		String htmlText = text + VCellErrorMessages.RightClickToAddMolecules;
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}
	public static final String toHtml(MolecularTypePattern mtp, boolean bShowWords) {
//		return "<html> " + (bShowWords ? "Molecule" : "") + " <b>" + mtp.getMolecularType().getName() + "<sub>" + mtp.getIndex() + "</sub></b></html>";
		return "<html> " + (bShowWords ? "Species Type" : "") + " <b>" + mtp.getMolecularType().getName() + "</b></html>";
	}
	public static final String toHtmlWithTip(MolecularTypePattern mtp, boolean bShowWords) {
		String text = (bShowWords ? "Species Type" : "") + " <b>" + mtp.getMolecularType().getName() + "</b>";
		String htmlText = text + VCellErrorMessages.ClickShowAllComponents;
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}
	public static final String toHtml(MolecularComponentPattern mcp, boolean bShowWords) {
		String text = (bShowWords ? "Component" : "") + " <b>" + mcp.getMolecularComponent().getName() + "</b>";
		MolecularComponent mc = mcp.getMolecularComponent();
		if(mc.getComponentStateDefinitions().size() > 0) {	// we don't show the state if nothing to choose from
			StateLocal sl = new StateLocal(mcp);
			text += "&#160;&#160;&#160;" + toHtmlWork(sl, bShowWords);
		}
		return "<html> " + text + "</html>";
	}
	public static final String toHtmlWithTip(MolecularComponentPattern mcp, boolean bShowWords) {
		String text = (bShowWords ? "Component" : "") + " <b>" + mcp.getMolecularComponent().getName() + "</b>";
		String htmlText = text + VCellErrorMessages.RightClickComponentToEdit;
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}
	public static final String toHtml(StateLocal sl, boolean bShowWords) {
		String text = toHtmlWork(sl, bShowWords);
		String htmlText = "<html>" + text + "</html>";
		return htmlText;
	}
	public static final String toHtmlWithTip(StateLocal sl, boolean bShowWords) {
		String text = toHtmlWork(sl, bShowWords);
		String htmlText = text + VCellErrorMessages.RightClickComponentForState;
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}
	// S(s!1,t!1).S(t) + S(s!2).S(tyr!2) -> S(s,tyr!+) + S(tyr~Y!?).S(tyr~Y)
	public static final String toHtml(BondLocal bl, boolean bSelected) {
		String text = toHtmlWork(bl, bSelected);
		String htmlText = "<html>" + text + "</html>";
		return htmlText;
	}
	public static final String toHtmlWithTip(BondLocal bl, boolean bSelected) {
		String text = toHtmlWork(bl, bSelected);
		String htmlText = text + VCellErrorMessages.RightClickComponentForBond;
		htmlText = "<html>" + htmlText + "</html>";
		return htmlText;
	}

	private static final String toHtmlWork(StateLocal sl, boolean bShowWords) {
		String stateText = "";
		MolecularComponentPattern mcp = sl.getMolecularComponentPattern();
		ComponentStatePattern csp = mcp.getComponentStatePattern();
		if (mcp != null /*&& !mcp.isImplied()*/) {
			if (csp == null) {
				if(bShowWords) {
					stateText = "State(-): <b>None</b>";
				} else {
					stateText = "<b>None</b>";
				}
			} else if(csp.isAny()) {
				if(bShowWords) {
					stateText = "State(~): <b>Any</b>";
				} else {
					stateText = "<b>Any</b>";
				}
			} else {
				if(bShowWords) {
					stateText = "State(~): <b>" + csp.getComponentStateDefinition().getName() + "</b>";
				} else {
					stateText = "~ <b>" + csp.getComponentStateDefinition().getName() + "</b>";
				}
			}
		}
		return stateText;
	}
	private static final String toHtmlWork(BondLocal bl, boolean bSelected) {
		MolecularComponentPattern mcp = bl.getMolecularComponentPattern();
		BondType defaultType = BondType.Possible;
		String bondText = " Bond(<b>" + defaultType.symbol + "</b>): " + "<b>" + BondType.Possible.name() + "</b>";
		if (mcp != null) {
			BondType bondType = mcp.getBondType();
			if (bondType == BondType.Specified) {
				Bond bond = mcp.getBond();
				if (bond == null) {
					bondText = "";
				} else {
					int id = mcp.getBondId();
					String colorTextStart = bSelected ? "" : "<font color=" + "\"rgb(" + bondHtmlColors[id].getRed() + "," + bondHtmlColors[id].getGreen() + "," + bondHtmlColors[id].getBlue() + ")\">";
					String colorTextEnd = bSelected ? "" : "</font>";
					
					bondText = colorTextStart + "<b>" + mcp.getBondId() + "</b>" + colorTextEnd;		// <sub>&nbsp;</sub>
					bondText = " Bound(" + bondText + ") to: " + colorTextStart + toHtml(bond) + colorTextEnd;
				}
			} else {
				bondText =  " Bond(<b>" + bondType.symbol + "</b>): " + "<b>" + bondType.name() + "</b>";
			}
		}
		return bondText;
	}
	public static final String toHtml(Bond bond) {	// TODO: must be made private eventually
		String bondText = " Species Type <b>" + bond.molecularTypePattern.getMolecularType().getName();
//		bondText += "<sub>" + bond.molecularTypePattern.getIndex() + "</sub></b> Component <b>";
		bondText += "&nbsp;&nbsp;&nbsp;</b>Component <b>";
		bondText +=	bond.molecularComponentPattern.getMolecularComponent().getName() + "</b>";
		//  ... + "<sub>" + bond.molecularComponentPattern.getMolecularComponent().getIndex() + "</sub>)"
		return bondText;
	}
	
	
	
	
}