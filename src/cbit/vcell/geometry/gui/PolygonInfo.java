package cbit.vcell.geometry.gui;
/**
 * Insert the type's description here.
 * Creation date: (7/7/2004 12:02:40 PM)
 * @author: Jim Schaff
 */
class PolygonInfo implements Comparable {
	java.awt.Polygon polygon = null;
	double depth = 0.0;
	double flatShadeColorScale;
	double notFlatShadeColorScale;
	int surfaceIndex;
	int polygonIndex;

/**
 * PolygonInfo constructor comment.
 */
public PolygonInfo(
    java.awt.Polygon argPolygon,
    double argDepth,
    double argFlatShadeColorScale,
    double argNotFlatShadeColorScale,
    int argSurfaceIndex,
    int argPolygonIndex) {

	    
    super();
    polygon = argPolygon;
    depth = argDepth;
    flatShadeColorScale = argFlatShadeColorScale;
    notFlatShadeColorScale = argNotFlatShadeColorScale;
    surfaceIndex = argSurfaceIndex;
    polygonIndex = argPolygonIndex;
}


public int compareTo(java.lang.Object o) {
	PolygonInfo otherPolygon = (PolygonInfo)o;
	return (depth>otherPolygon.depth ? -1 : (depth==otherPolygon.depth ? 0 : 1));
}
}