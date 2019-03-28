package com.exco.xmlutils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.exco.xmlutils.SoapWsdlValidator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 */
public class SoapWsdlValidatorTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SoapWsdlValidatorTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( SoapWsdlValidatorTest.class );
    }

    /**
     * 
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public void testApp() throws IOException, URISyntaxException
    {

    	
    	String wsdlfolder = "/com/exco/royal-mail-local-collect-v2-soap-1.0.3/";
    	
		String soap = new String(Files.readAllBytes(Paths.get( SoapWsdlValidatorTest.class.getResource("/com/exco/test-request.xml").toURI() )));
		
		
		String xsd1 = new String(Files.readAllBytes(Paths.get( SoapWsdlValidatorTest.class.getResource( wsdlfolder+"localcollect_messages/localCollectAPIMessagesV2_1.xsd").toURI() )));;
		String xsd2 = new String(Files.readAllBytes(Paths.get( SoapWsdlValidatorTest.class.getResource( wsdlfolder+"rmg_commons/CommonIntegrationSchemaV3_0.xsd").toURI() )));;
		String xsd3 = new String(Files.readAllBytes(Paths.get( SoapWsdlValidatorTest.class.getResource( wsdlfolder+"rmg_commons/DatatypesV4_2.xsd").toURI() )));;
		String xsd4 = new String(Files.readAllBytes(Paths.get( SoapWsdlValidatorTest.class.getResource( wsdlfolder+"localcollect_types/localCollectAPITypesV2_1.xsd").toURI() )));;
		
		
		String wsdlxml = new String(Files.readAllBytes(Paths.get( SoapWsdlValidatorTest.class.getResource( wsdlfolder+"LocalCollectAPIv2_1.wsdl").toURI() )));;
		
		
		String schemadef = "{\n" + 
				"    \"schemas\": [ \n" + 
				"        { \"file\": \"localcollect_messages/localCollectAPIMessagesV2_1.xsd\", \"content\": \"localcollect_messages/localCollectAPIMessagesV2_1.xsd\" },\n" + 
				"        { \"file\": \"localcollect_types/localCollectAPITypesV2_1.xsd\", \"content\": \"localcollect_types/localCollectAPITypesV2_1.xsd\" },\n" + 
				"        \n" + 
				"        { \"file\": \"rmg_commons/ReferenceDataV4.2.xsd\", \"content\": \"rmg_commons/ReferenceDataV4.2.xsd\" },\n" + 
				"        { \"file\": \"rmg_commons/DatatypesV4_2.xsd\", \"content\": \"rmg_commons/DatatypesV4_2.xsd\" },\n" + 
				"        { \"file\": \"rmg_commons/CommonIntegrationSchemaV3_0.xsd\", \"content\": \"rmg_commons/CommonIntegrationSchemaV3_0.xsd\" },\n" + 
				"            \n" + 
				"        { \"file\": \"LocalCollectAPIv2_1.wsdl\", \"content\": \"LocalCollectAPIv2_1.wsdl\" }\n" + 
				"    ],\n" + 
				"    \"wsdl\": \"LocalCollectAPIv2_1.wsdl\"\n" + 
				"}";
	
		
	    Map<String, String> properties = new HashMap<String, String>();;
	    
	    properties.put( "wsdl", "LocalCollectAPIv2_1.wsdl" );
	    
	    // prefix for xml.i and schema.i context variables
	    properties.put( "schemaprefix", "flow.soap.schema." );
	    properties.put( "xmlprefix", "flow.soap.xml." );
	    properties.put( "length", "6" );
	    
	    
	    
	    
		String wsdl =  "file:/" + "LocalCollectAPIv2_1.wsdl";
		
	    
		Map<String, String> schemas = new HashMap<String, String>();
		
		// import/schemaLocation
		schemas.put( "file:/localcollect_messages/localCollectAPIMessagesV2_1.xsd", xsd1 );
		schemas.put( "file:/localcollect_types/localCollectAPITypesV2_1.xsd", xsd4 );
		schemas.put( "file:/rmg_commons/CommonIntegrationSchemaV3_0.xsd", xsd2 );
		schemas.put( "file:/rmg_commons/DatatypesV4_2.xsd", xsd3 );
		
		schemas.put( "file:/LocalCollectAPIv2_1.wsdl", wsdlxml );

		SoapWsdlValidator validator = new SoapWsdlValidator( properties );
		
		
		
		try {
			validator.validate( soap, wsdl, schemas );
			
			
		}catch( Exception e) {
			
			System.out.println(e);
		}
		
		

		assertTrue( true );
    }
}
