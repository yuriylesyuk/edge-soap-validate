//    <Get assignTo="flow.soap.schemas" index="1">
//        <Key>
//            <Parameter ref="flow.soap.schema.1"/>


//       { 
//           "schemas": [ 
//               { "file": "path/file1.xsd", "content": "file-1-xsd" },
//               { "file": "path/file2.xsd", "content": "file-2-xsd" },
//               { "file": "path/file.wsdl", "content": "file-3-wsdl" }
//           ],
//           "wsdl": "path/file.wsdl"
//        }

var schemadef = JSON.parse( context.getVariable("flow.soap.schemadef") );

for(var i = 0; i < schemadef.schemas.length; i++){
    context.setVariable( "flow.soap.schema."+ (i+1), schemadef.schemas[i].file )
    context.setVariable( "flow.soap.content."+ (i+1), schemadef.schemas[i].content )
}
context.setVariable( "flow.soap.schemas.length", schemadef.schemas.length.toFixed() )

context.setVariable( "flow.soap.wsdl", schemadef.wsdl )

