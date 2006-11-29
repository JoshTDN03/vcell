package cbit.gui.graph;

import cbit.gui.graph.GraphModel;
import cbit.gui.graph.GraphEvent;
import cbit.gui.graph.EdgeShape;
import cbit.gui.graph.ContainerShape;
import cbit.gui.graph.Shape;
import cbit.util.graph.Graph;
/**
 * Insert the type's description here.
 * Creation date: (7/8/2003 9:11:57 AM)
 * @author: Jim Schaff
 */
public class SimpleGraphModel extends GraphModel {
	private cbit.util.graph.Graph fieldGraph = new cbit.util.graph.Graph();
	private GraphShapeFactory graphShapeFactory = new DefaultGraphShapeFactory();
	
	public static interface GraphShapeFactory {
		public NodeShape getNodeShape(cbit.util.graph.Node node, GraphModel graphModel, Graph graph);
		public GraphEdgeShape getEdgeShape(cbit.util.graph.Edge edge, NodeShape beginShape, NodeShape endShape, GraphModel graphModel, Graph graph);
		public String getTitle();
	}
	
	public static class DefaultGraphShapeFactory implements GraphShapeFactory {
		private boolean bDisplayDirected;
		public DefaultGraphShapeFactory(){
			this(false);
		}
		public DefaultGraphShapeFactory(boolean displayDirected){
			this.bDisplayDirected = displayDirected;
		}
		public NodeShape getNodeShape(cbit.util.graph.Node node, GraphModel graphModel, Graph graph) {
			NodeShape nodeShape = new NodeShape(node,graphModel,graph.getDegree(node));
			nodeShape.setLabel(node.getName());
			return nodeShape;
		}
		public GraphEdgeShape getEdgeShape(cbit.util.graph.Edge edge, NodeShape beginShape, NodeShape endShape, GraphModel graphModel, Graph graph){
			GraphEdgeShape graphEdgeShape = new GraphEdgeShape(edge,beginShape,endShape,graphModel,bDisplayDirected,false);
			return graphEdgeShape;
		}
		public String getTitle(){
			return "graph diagram";
		}
		public boolean isDisplayDirected() {
			return bDisplayDirected;
		}
		public void setDisplayDirected(boolean displayDirected) {
			bDisplayDirected = displayDirected;
		}
	}
	
/**
 * SimpleGraphModel constructor comment.
 */
public SimpleGraphModel() {
	super();
}
/**
 * Gets the graph property (cbit.vcell.mapping.potential.Graph) value.
 * @return The graph property value.
 * @see #setGraph
 */
public cbit.util.graph.Graph getGraph() {
	return fieldGraph;
}
/**
 * Insert the method's description here.
 * Creation date: (7/8/2003 9:11:57 AM)
 */
public void refreshAll() {
	clearAllShapes();
	if (getGraph() == null){
		fireGraphChanged(new GraphEvent(this));
		return;
	}
	ContainerShape containerShape = new SimpleContainerShape(getGraph(),this,graphShapeFactory.getTitle());
	addShape(containerShape);
	cbit.util.graph.Node nodes[] = getGraph().getNodes();
	for (int i = 0; i < nodes.length; i++){
		NodeShape nodeShape = graphShapeFactory.getNodeShape(nodes[i],this,fieldGraph);
		containerShape.addChildShape(nodeShape);
		addShape(nodeShape);
	}
	cbit.util.graph.Edge edges[] = getGraph().getEdges();
	for (int i = 0; i < edges.length; i++){
		NodeShape node1 = (NodeShape)getShapeFromModelObject(edges[i].getNode1());
		NodeShape node2 = (NodeShape)getShapeFromModelObject(edges[i].getNode2());
		EdgeShape edgeShape = graphShapeFactory.getEdgeShape(edges[i],node1,node2,this,fieldGraph);
		containerShape.addChildShape(edgeShape);
		addShape(edgeShape);
	}
	fireGraphChanged(new GraphEvent(this));
}
/**
 * Sets the graph property (cbit.vcell.mapping.potential.Graph) value.
 * @param graph The new value for the property.
 * @see #getGraph
 */
public void setGraph(cbit.util.graph.Graph graph) {
	cbit.util.graph.Graph oldValue = fieldGraph;
	fieldGraph = graph;
	firePropertyChange("graph", oldValue, graph);
	refreshAll();
}
/**
 * This method was created in VisualAge.
 */
public void setRandomLayout(boolean bRandomize) {

	try {
		//
		// assert random characteristics
		//
		Shape topShape = getTopShape();
		if (topShape instanceof ContainerShape){
			ContainerShape containerShape = (ContainerShape)topShape;
			containerShape.setRandomLayout(bRandomize);
		}
		
	}catch (Exception e){
		System.out.println("top shape not found");
		e.printStackTrace(System.out);
	}

}
public GraphShapeFactory getGraphShapeFactory() {
	return graphShapeFactory;
}
public void setGraphShapeFactory(GraphShapeFactory graphShapeFactory) {
	this.graphShapeFactory = graphShapeFactory;
}
}
