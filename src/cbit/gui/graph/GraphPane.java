package cbit.gui.graph;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.vcell.util.gui.JDesktopPaneEnhanced;
import org.vcell.util.gui.JInternalFrameEnhanced;

@SuppressWarnings("serial")
public class GraphPane extends JPanel implements GraphListener, MouseListener, Scrollable {
	
	private GraphModel graphModel = null;
	protected boolean needsLayout = true;
	
	public GraphPane() {
		super();
		initialize();
		addMouseListener(this);
	}

	public void clear(java.awt.Graphics g) {
		super.paint(g);
	}

	public Frame getFrame() {
		Component comp = getParent();
		while (comp!=null){
			if (comp instanceof Frame) {
				return (Frame) comp;
			}
			comp = comp.getParent();
		}
		return null;
	}

	public GraphModel getGraphModel() {
		return graphModel;
	}


	private JScrollPane getJScrollPaneParent() {
		if (getParent() instanceof JScrollPane){
			return (JScrollPane) getParent();
		} else if (getParent() instanceof JViewport && getParent().getParent() instanceof JScrollPane){
			return (JScrollPane) getParent().getParent();
		} else {
			return null;
		}
	}


	/**
	 * Returns the preferred size of the viewport for a view component.
	 * For example the preferredSize of a JList component is the size
	 * required to acommodate all of the cells in its list however the
	 * value of preferredScrollableViewportSize is the size required for
	 * JList.getVisibleRowCount() rows.   A component without any properties
	 * that would effect the viewport size should just return 
	 * getPreferredSize() here.
	 * 
	 * @return The preferredSize of a JViewport whose view is this Scrollable.
	 * @see JViewport#getPreferredSize
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getPreferredSize() {
		if (graphModel != null) {
			Dimension prefSize = graphModel.getPreferedCanvasSize((java.awt.Graphics2D)getGraphics());
			if (getJScrollPaneParent()!=null){
				Rectangle viewBorderBounds = getJScrollPaneParent().getViewportBorderBounds();
				prefSize = new Dimension(Math.max(viewBorderBounds.width, prefSize.width),
						Math.max(viewBorderBounds.height, prefSize.height));
			}
			return prefSize;
		}else{
			return super.getPreferredSize();
		}

	}


	/**
	 * Components that display logical rows or columns should compute
	 * the scroll increment that will completely expose one block
	 * of rows or columns, depending on the value of orientation. 
	 * <p>
	 * Scrolling containers, like JScrollPane, will use this method
	 * each time the user requests a block scroll.
	 * 
	 * @param visibleRect The view area visible within the viewport
	 * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
	 * @param direction Less than zero to scroll up/left, greater than zero for down/right.
	 * @return The "block" increment for scrolling in the specified direction.
	 * @see JScrollBar#setBlockIncrement
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (SwingConstants.VERTICAL == orientation){
			return visibleRect.height/4;
		} else {
			return visibleRect.width/4;
		}
	}


	/**
	 * Return true if a viewport should always force the height of this 
	 * Scrollable to match the height of the viewport.  For example a 
	 * columnar text view that flowed text in left to right columns 
	 * could effectively disable vertical scrolling by returning
	 * true here.
	 * <p>
	 * Scrolling containers, like JViewport, will use this method each 
	 * time they are validated.  
	 * 
	 * @return True if a viewport should force the Scrollables height to match its own.
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


	/**
	 * Return true if a viewport should always force the width of this 
	 * Scrollable to match the width of the viewport.  For example a noraml 
	 * text view that supported line wrapping would return true here, since it
	 * would be undesirable for wrapped lines to disappear beyond the right
	 * edge of the viewport.  Note that returning true for a Scrollable
	 * whose ancestor is a JScrollPane effectively disables horizontal
	 * scrolling.
	 * <p>
	 * Scrolling containers, like JViewport, will use this method each 
	 * time they are validated.  
	 * 
	 * @return True if a viewport should force the Scrollables width to match its own.
	 */
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}


	/**
	 * Components that display logical rows or columns should compute
	 * the scroll increment that will completely expose one new row
	 * or column, depending on the value of orientation.  Ideally, 
	 * components should handle a partially exposed row or column by 
	 * returning the distance required to completely expose the item.
	 * <p>
	 * Scrolling containers, like JScrollPane, will use this method
	 * each time the user requests a unit scroll.
	 * 
	 * @param visibleRect The view area visible within the viewport
	 * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
	 * @param direction Less than zero to scroll up/left, greater than zero for down/right.
	 * @return The "unit" increment for scrolling in the specified direction
	 * @see JScrollBar#setUnitIncrement
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 1;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @param o java.util.Observable
	 * @param obj java.lang.Object
	 */
	public void graphChanged(GraphEvent graphEvent) {
		updateAll();			
	}


	/**
	 * Called whenever the part throws an exception.
	 * @param exception java.lang.Throwable
	 */
	private void handleException(Throwable exception) {
		System.out.println("--------- UNCAUGHT EXCEPTION --------- in CartoonCanvas");
		exception.printStackTrace(System.out);
	}

	private void initialize() {
		try {
			// user code begin {1}
			// user code end
			setName("GraphPane");
			setLayout(null);
			setBackground(java.awt.Color.gray);
			setSize(150, 150);
		} catch (Throwable ivjExc) {
			handleException(ivjExc);
		}
		// user code begin {2}
		// user code end
	}

	/**
	 * Returns the value of a flag that indicates whether
	 * this component can be traversed using
	 * Tab or Shift-Tab keyboard focus traversal.  If this method
	 * returns "false", this component may still request the keyboard
	 * focus using <code>requestFocus()</code>, but it will not automatically
	 * be assigned focus during tab traversal.
	 * @return    <code>true</code> if this component is
	 *            focus-traverable; <code>false</code> otherwise.
	 * @since     JDK1.1
	 */

	//override isFocusTraversable() to return true (Canvas returns false by default) 
	//catch the mouse-down event on the component and invoke requestFocus() (to implement click-to-type for your component) 
	//when your component gets focus, provide visual feedback indicating it has the focus 

	@Override
	public boolean isFocusTraversable() {
		return true;
	}

	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame();
			JDesktopPane desktop = new JDesktopPaneEnhanced();
			JInternalFrame iframe = new JInternalFrameEnhanced("", true, true, true, true);
			JPanel panel = new JPanel();
			GraphPane aGraphPane = new GraphPane();
			panel.setName("JInternalFrameContentPane");
			panel.setLayout(new BorderLayout());
			panel.add(aGraphPane, "Center");
			panel.add(new javax.swing.JButton("hello"), "South");
			iframe.setName("JInternalFrame");
			iframe.setBounds(50, 50, 400, 300);
			iframe.setTitle("GraphPane");
			iframe.getContentPane().add(panel);
			//		iframe.setContentPane(panel);
			desktop.setName("JDesktopPane1");
			desktop.add(iframe, iframe.getName());
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(desktop, "Center");
			frame.setSize(iframe.getSize());
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				};
			});
			frame.setVisible(true);
		} catch (Throwable exception) {
			System.err.println("Exception occurred in main() of cbit.vcell.graph.GraphPane");
			exception.printStackTrace(System.out);
		}
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		//override isFocusTraversable() to return true (Canvas returns false by default) 
		//catch the mouse-down event on the component and invoke requestFocus() (to implement click-to-type for your component) 
		//when your component gets focus, provide visual feedback indicating it has the focus 
		requestFocus();
	}

	public void mouseReleased(MouseEvent e) {}

	@Override
	public void paintComponent(Graphics argGraphics) {
		super.paintComponent(argGraphics);
		try {
			Graphics2D g = (Graphics2D) argGraphics;
			if (graphModel!=null){
				if(needsLayout) {
					GraphContainerLayout containerLayout = graphModel.getContainerLayout();
					containerLayout.layout(graphModel, g, getSize());
					needsLayout = false;					
				}
				graphModel.paint(g,this);
			}	
		}catch (Exception e){
			handleException(e);
		}		
	}

	public void setGraphModel(GraphModel argGraphModel) {

		if (this.graphModel != null){
			this.graphModel.removeGraphListener(this);
		}

		this.graphModel = argGraphModel;

		if (this.graphModel != null){
			this.graphModel.addGraphListener(this);
		}

		updateAll();
	}

	public void updateAll() {
		needsLayout = true;
		try {
			if (graphModel!=null){
				if (getJScrollPaneParent()!=null){
					invalidate();
					getJScrollPaneParent().revalidate();
				}
				repaint();
			}
		}catch (Exception e){
			handleException(e);
		}			
	}

}