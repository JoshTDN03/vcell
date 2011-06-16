/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.models.tree.pckeyword;

/*   ErrorResponseWrapper  --- by Oliver Ruebenacker, UCHC --- December 2009
 *   Wrapper for a tree node with a PCKeywordResponse
 */

import org.vcell.sybil.util.http.pathwaycommons.PCExceptionResponse;

public class ExceptionResponseWrapper extends ResponseWrapper {

	public ExceptionResponseWrapper(PCExceptionResponse response) {
		super(response);
		Exception exception = response.exception();
		append("Class: " + exception.getClass());
		append("Message: " + exception.getMessage());
		Throwable cause = exception.getCause();
		if(cause != null) {
			append("Cause class: " + cause.getClass());			
			append("Cause message: " + cause.getMessage());			
		}
	}
	
	@Override
	public String toString() {
		Exception exception = data().exception();
		return "Exception " + exception.getClass().getSimpleName() + ": " + exception.getMessage();
	}
	
	@Override
	public PCExceptionResponse data() { return (PCExceptionResponse) super.data(); }

}
