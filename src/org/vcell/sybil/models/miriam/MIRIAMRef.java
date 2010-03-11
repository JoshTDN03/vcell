package org.vcell.sybil.models.miriam;

/*   MIRIAMRef  --- by Oliver Ruebenacker, UCHC --- March 2010
 *   A MIRIAM reference consisting of a datatype and a value or id
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.vcell.sybil.util.keys.KeyOfTwo;

public class MIRIAMRef extends KeyOfTwo<String, String> {
	
	public MIRIAMRef(String type, String id) { super(type, id); }

	public String type() { return a(); }
	public String id() { return b(); }
	
	public static final String encoding = "UTF-8";

	public static String encode(String raw) { 
		try {return URLEncoder.encode(raw, encoding); } 
		catch (UnsupportedEncodingException e) { e.printStackTrace(); return null; } 
	}
	
	public static String decode(String encoded) {
		try { return URLDecoder.decode(encoded, encoding); } 
		catch (UnsupportedEncodingException e) { e.printStackTrace(); return null; }
	}
	
	public String urn() { return "urn:miriam:" + encode(type()) + ":" + encode(id()); }
	
	@SuppressWarnings("serial")
	public static class URNParseFailureException extends Exception {
		public URNParseFailureException(String message) { super(message); }
	}
	
	public static MIRIAMRef createFromURN(String urn) throws URNParseFailureException {
		String[] split = urn.split(":");
		if(split.length != 4) { 
			throw new URNParseFailureException(urn + ": Need four components, but got " + split.length); 
		} 
		if(!split[0].equals("urn")) {
			throw new URNParseFailureException(urn + ": First component needs to be 'urn', but is '" 
					+ split[0] + "'");
		}
		if(!split[1].equals("miriam")) {
			throw new URNParseFailureException(urn + "Second component needs to be 'miriam', but is '" 
					+ split[1] + "'");
		}
		String type = decode(split[2]);
		String id = decode(split[3]);
		return new MIRIAMRef(type, id);
	}
	
}