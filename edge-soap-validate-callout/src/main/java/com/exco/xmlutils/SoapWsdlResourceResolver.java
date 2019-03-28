package com.exco.xmlutils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class SoapWsdlResourceResolver implements LSResourceResolver {

	
	private Map<String, String> schemas =null;
	private URI currentDirURI = null;

	
	public SoapWsdlResourceResolver( Map<String, String> schemas ) {
		
		this.schemas = schemas;
		this.currentDirURI = Paths.get(".").toUri();
	}
	
	@Override
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
				
		String basePathURI = baseURI==null?"file:/":baseURI;
		String schemaPath = resolveResourcePath( basePathURI, systemId );
		String schema = schemas.get( schemaPath );
		if( schema == null ) {
			throw new IllegalArgumentException( "SoapWsdlResourceResolver: Schema is unknown: " +schemaPath + " for systemId: " + systemId );
		}
		
		
		SoapWsdlInput inputStream = null;
		try {
			inputStream = new SoapWsdlInput( publicId, systemId, basePathURI, new ByteArrayInputStream( schema.getBytes("UTF-8") ) );
		}catch(UnsupportedEncodingException e) {
			throw new RuntimeException( e );
		}
		return inputStream;
	}
	
	private String resolveResourcePath( String baseURI, String systemId ) {
		
		// https://superuser.com/questions/352133/why-do-file-urls-start-with-3-slashes				
		
		URI p = URI.create(baseURI);		
		URI pp = p.getPath().endsWith("/") ? p.resolve("..") : p.resolve(".")
;
		//URI.create(baseURI).resolve("..");
		
		//Paths.get(baseURI).getParent().toUri();
		
		//URI r = currentDirURI.relativize( pp );
		
		return pp.resolve(systemId).normalize().toString();
	}	
}
