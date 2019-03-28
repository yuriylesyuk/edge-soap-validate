package com.exco.xmlutils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.apigee.flow.execution.Action;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;

public class SoapWsdlValidator implements Execution {

	private static final Logger LOGGER = LoggerFactory.getLogger(SoapWsdlValidator.class);

	
	static final String JAXP_SCHEMA_LANGUAGE="http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA="http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE="http://java.sun.com/xml/jaxp/properties/schemaSource";
	
    private Map<String, String> properties;
	
   	public SoapWsdlValidator( Map<String, String> properties) {
   			
   			this.properties = properties;
   	}
    
    public static String readStringFromFile(String filePath) throws IOException {
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
        return fileContent;
    }
	
    private Source[] getSchemas( String wsdl ) throws ParserConfigurationException, TransformerException {
    	
    	Source wsdlSource = new StreamSource(new java.io.StringReader(wsdl));
        
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	dbf.setNamespaceAware( true );
    	
    	
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document wsdlDoc = db.newDocument();
	
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.transform(wsdlSource, new DOMResult(wsdlDoc));
	
	    NodeList schemaNodes = wsdlDoc.getElementsByTagNameNS( XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
	
	    int schemasTotal = schemaNodes.getLength();
	
	    Source[] schemas = new Source[schemasTotal];
	
	    for (int i = 0; i < schemasTotal; i++) {
	        schemas[i] = new DOMSource(schemaNodes.item(i));
	    }
	    
	    return schemas;
    }
	
    private Source[] concatSchemas(Source[] src, Source[] add) {
	    return Stream.of(src, add).flatMap(Stream::of)
                .toArray(Source[]::new);
    }


    public void validate( String soap, String wsdl, Map<String, String> schemas ) {
    	
    	String soap11schema = null;
    	
		try {
			
			soap11schema = (new BufferedReader(  
					new InputStreamReader( 
							SoapWsdlValidator.class.getResourceAsStream("/schemas/soapEnvelope11.xsd"), "UTF-8"
					))).lines().collect(Collectors.joining(""));
		} catch (UnsupportedEncodingException e) {

			throw new RuntimeException( e );
		}

		try {
			
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            Source[] schemasCollection = null;

            try{

            	schemasCollection = getSchemas( schemas.get(  "file:/" + wsdl ) );

            }catch( ParserConfigurationException e) {
            	throw new RuntimeException( e );

            }catch( TransformerException e) {
                   throw new RuntimeException( e );

            };

            schemasCollection = concatSchemas( schemasCollection, new Source[]{new StreamSource(new java.io.StringReader( soap11schema ))} );

            
            schemaFactory.setResourceResolver( new SoapWsdlResourceResolver(schemas) );
            
            
            
            Schema schema = schemaFactory.newSchema( schemasCollection );
            
            
            ValidatorHandler validatorHandler = schema.newValidatorHandler();

            validatorHandler.setResourceResolver( new SoapWsdlResourceResolver( schemas ));
            
            
            SoapWsdlValidatorErrorHandler errorHandler = new SoapWsdlValidatorErrorHandler();
            validatorHandler.setErrorHandler( errorHandler );
            
            
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            
            parserFactory.setNamespaceAware(true);
            parserFactory.setValidating(false);
            
            SAXParser parser = parserFactory.newSAXParser();
            
            XMLReader reader = parser.getXMLReader();
            
            reader.setContentHandler( validatorHandler );


            reader.parse(new InputSource( new StringReader( soap ) ));


            if( !errorHandler.isValid() ) {
				throw new RuntimeException( errorHandler.getExceptions().stream()
				        .map( n -> n.toString() )
				        .collect( Collectors.joining( "" ) )
				    );
			}

		} catch (SAXException e) {
	    	throw new RuntimeException( e );
		
		} catch (IOException e) {
	    	throw new RuntimeException( e );
		
		} catch (ParserConfigurationException e) {
	    	throw new RuntimeException( e );
		}

    }

    	
	public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {
		
	    try {	    	
	    	
	    	String wsdl = ((String)messageContext.getVariable( this.properties.get("wsdl") ) );
	    	String schemaprefix = ((String)this.properties.get("schemaprefix") );
	    	String xmlprefix = ((String)this.properties.get("xmlprefix") );
			int length = Integer.parseInt( (String)messageContext.getVariable( this.properties.get("length") )  ); 
	    			
	    	String soap = (String)messageContext.getVariable( "message.content" );

	    	//
	    	Map<String, String> schemas = new HashMap<String, String>();
			
			for( int i =1; i <= length; i++ ) {
				
				schemas.put( 
					"file:/" + (String)messageContext.getVariable( schemaprefix + String.format( "%d", i ) ),
					(String)messageContext.getVariable( xmlprefix + String.format( "%d", i ) ) 
				);
				
messageContext.setVariable( "X-DEBUG-msg"+String.format( "%d", i ),"file:/" + (String)messageContext.getVariable( schemaprefix + String.format( "%d", i ) )  ); 	    	
				
			}		
			
						
messageContext.setVariable( "X-DEBUG-msg", "hereBEF" ); 	    	
			validate( soap, wsdl, schemas );
messageContext.setVariable( "X-DEBUG-msg", "hereAFT" ); 	    	
							
			return ExecutionResult.SUCCESS;
			
	    } catch (Exception e) {

            ExecutionResult executionResult = new ExecutionResult(false, Action.ABORT);

            executionResult.setErrorResponse(e.getMessage());
            executionResult.addErrorResponseHeader("ExceptionClass", e.getClass().getName());

            messageContext.setVariable("JAVA_ERROR", e.getMessage());
            messageContext.setVariable("JAVA_STACKTRACE", Arrays.toString(Thread.currentThread().getStackTrace()));
            return executionResult;
	    }
    }
	
    protected Logger getLogger() {
        return LOGGER;
    }
    
	public static void main(String[] args) throws IOException {
		
		if( args.length < 2) {
			
			System.out.println("Insufficient number of arguments.");
			System.out.println("example usage");
			System.out.println("SoapWsdlValidator <soapmessage> <wsdlschema>");
			
			System.exit(0);
		}
		
		String soap = args[0];
		String wsdl = args[1];
		
		String soapstring = readStringFromFile(soap);
		String wsdlstring = readStringFromFile(wsdl);
		
		//String wsdls[] = { wsdlstring };
		// TODO: Refactor
		
		Map<String, String> properties = new HashMap<String, String>();;
		 
		Map<String,String> schemas = new HashMap<String,String>();

		SoapWsdlValidator validator = new SoapWsdlValidator( properties );
		
		String message = "xxx";
		
		try {
			validator.validate( soapstring, message, schemas );
		}catch( Exception e) {
			
			System.out.println(e);
		}
	}
}
