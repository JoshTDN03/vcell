package org.vcell.vis.vismesh;

import org.vcell.vis.core.Vect3D;

public class VisPolygon {
	
	private int[] pointIndices;
	private final int level;
	private final int boxNumber;
	private final int boxIndex;
	private final double fraction;
	private final int regionIndex;
	
	public VisPolygon(int[] pointIndices, int level, int boxNumber, int boxIndex, double fraction, int regionIndex) {
		this.level = level;
		this.pointIndices = pointIndices;
		this.boxNumber = boxNumber;
		this.boxIndex = boxIndex;
		this.fraction = fraction;
		this.regionIndex = regionIndex;
	}

	public int[] getPointIndices() {
		return pointIndices;
	}

	public int getLevel(){
		return level;
	}
	
	public int getBoxNumber() {
		return boxNumber;
	}

	public int getBoxIndex() {
		return boxIndex;
	}

	public double getFraction() {
		return fraction;
	}
	
	public int getRegionIndex(){
		return regionIndex;
	}
	
	public void setPointIndices(int[] pointIndices){
		this.pointIndices = pointIndices;
	}
	
	public String toString(){
		return "VisPolygon@"+hashCode()+": level="+level+", box="+boxNumber+", boxIndex="+boxIndex+", fraction="+fraction+", vertices="+pointIndices;
	}

	public Vect3D getCentroid(VisMesh visMesh) {
		double x=0;
		double y=0;
		double z=0;
		int numP = pointIndices.length;
		for (int pointIndex : pointIndices){
			VisPoint point = visMesh.getPoints().get(pointIndex);
			x += point.x;
			y += point.y;
			z += point.z;
		}
		return new Vect3D(x/numP,y/numP,z/numP);
	}

}
