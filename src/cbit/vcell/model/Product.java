package cbit.vcell.model;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import org.vcell.util.Matchable;
import org.vcell.util.document.KeyValue;


public class Product extends ReactionParticipant
{
/**
 * This method was created in VisualAge.
 * @param reactionStep cbit.vcell.model.ReactionStep
 */
Product(KeyValue key, ReactionStep reactionStep) {
	super(key,reactionStep);
}


public Product(KeyValue key, SimpleReaction parent,SpeciesContext speciesContext, int stoichiometry) 
{
	super(key,parent,speciesContext, stoichiometry);
}   


/**
 * This method was created in VisualAge.
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(Matchable obj) {
	if (obj instanceof Product){
		Product p = (Product)obj;
		return compareEqual0(p);
	}else{
		return false;
	}
}


/**
 * This method was created by a SmartGuide.
 * @param tokens java.util.StringTokenizer
 * @exception java.lang.Exception The exception description.
 */
public void fromTokens(cbit.util.CommentStringTokenizer tokens, Model model) throws Exception {

	String scName = tokens.nextToken();				// read speciesContext name
	String speciesName = tokens.nextToken();		// read species name
	String structureName = tokens.nextToken();		// read structure name
	String stoichStr = tokens.nextToken();			// read Stoichiometry
	SpeciesContext sc = model.getSpeciesContext(scName);
	if (sc==null){
		throw new Exception("speciesContext "+scName+" not found");
	}
	setSpeciesContext(sc);
	setStoichiometry((new Integer(stoichStr)).intValue());
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	String scName = (getSpeciesContext()!=null)?(getSpeciesContext().getName()):"null";
	return "Product(id="+getKey()+", speciesContext="+scName+"')";
}


/**
 * This method was created by a SmartGuide.
 * @param ps java.io.PrintStream
 * @exception java.lang.Exception The exception description.
 */
public void writeTokens(java.io.PrintWriter pw) {
	pw.println("\t\t"+VCMODL.Product+" "+getSpeciesContext().getName()+" "+getStoichiometry());
}
}