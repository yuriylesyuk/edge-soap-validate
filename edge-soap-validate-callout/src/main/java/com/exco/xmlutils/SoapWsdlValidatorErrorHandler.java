package com.exco.xmlutils;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SoapWsdlValidatorErrorHandler implements ErrorHandler {
	private List<String> exceptions = new ArrayList<>();;
	
	private void addException( String error ) {
		
		exceptions.add( error );
	}
	
    public void warning(SAXParseException e) throws SAXException {
    	
    	addException( "WARNING: " + e.getMessage() );
    }

    public void error(SAXParseException e) throws SAXException {

    	addException( "ERROR: " + e.getMessage() );
    }

    public void fatalError(SAXParseException e) throws SAXException {
    	
    	addException( "FATALERROR: " + e.getMessage() );
    }
    
    public boolean isValid() {
    	return exceptions.size() == 0;
    }
    
    public List<String> getExceptions(){
    	
    	return exceptions;
    }

}
