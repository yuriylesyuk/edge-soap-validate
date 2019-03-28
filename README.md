# SOAP WSDL Validation Callout with Resource Resolver

Currently built-in [SoapMessageValidation](https://docs.apigee.com/api-platform/reference/policies/message-validation-policy) Apigee Edge policy has restriction of processing only single WSDL file that should contain all XSD schemas inside.

That is not what happens in a real life. This callout removes the restriction and allows you to upload a collection of XSD schema files that use &lt;import&gt; element with relative schemaLocation path/file reference. 



## Walkthrough

Setup
Solution components:

* java callout for soap/wsdl validation
* edge proxy 
* kvm-xml-ctl.sh script 

?. Configure .netrc for your Management server, SaaS or On-Premises for -n curl option
vi ~/.netrc
machine api.enterprise.apigee.com login <org-admin-email> password <password>

?. Populate environment variables

Example for Cloud
```
export ORG=<your-org>
export ENV=<env>
export MSURL=https://api.enterprise.apigee.com
export APIURL=http://$ORG-$ENV.apigee.net
```

Example for OnPrem
```
export ORG=org5
export ENV=prod
export MSURL=http://10.154.0.9:8080
export APIURL=http://10.154.0.9:9013
```
?. test variables and mapi credentials

curl -n $MSURL/v1/o/$ORG/e/$ENV

?. set up path to the project's /bin folder

cd <project-path>edge-soap-validate/bin

```
export PATH=$PATH:$PWD

kvm-ctl.sh
```

?. Create kvms schemas and groupschemas and populate them

?.  cd to directory with schema folder
```
cd edge-soap-validate/docs/royal-mail-local-collect-v2-soap-1.0.3
```

?. Populate schema catalog and schema definitions
```
kvm-ctl.sh createkvm rm-schemas
kvm-ctl.sh createkvm rm-messages


kvm-ctl.sh putvalue rm-messages getLocalCollectionRequest @../rm-localcollect-schema-def.json

kvm-ctl.sh putvalue rm-schemas "localcollect_messages--localCollectAPIMessagesV2_1.xsd" "@localcollect_messages/localCollectAPIMessagesV2_1.xsd"

kvm-ctl.sh putvalue rm-schemas "localcollect_types--localCollectAPITypesV2_1.xsd" "@localcollect_types/localCollectAPITypesV2_1.xsd"
kvm-ctl.sh putvalue rm-schemas "rmg_commons--ReferenceDataV4.2.xsd" "@rmg_commons/ReferenceDataV4.2.xsd"

kvm-ctl.sh putvalue rm-schemas "rmg_commons--DatatypesV4_2.xsd" "@rmg_commons/DatatypesV4_2.xsd"

kvm-ctl.sh putvalue rm-schemas "rmg_commons--CommonIntegrationSchemaV3_0.xsd" "@rmg_commons/CommonIntegrationSchemaV3_0.xsd"

kvm-ctl.sh putvalue rm-schemas LocalCollectAPIv2_1.wsdl @LocalCollectAPIv2_1.wsdl

NOTE: use deletekey action if required:
kvm-ctl.sh deletekey rm-messages getLocalCollectionRequest

kvm-ctl.sh listkvms
```

### Deploy the proxy

?. Define two more environment variables for deploy.sh script
```
export ORG_ADMIN_USERNAME=<edge-org-admin-email>
read -sp 'Password: ' ORG_ADMIN_PASSWORD
export ORG_ADMIN_PASSWORD
```

?. Validate and correct virtual host reference as required in the e/edge-soap-validate-bundle/apiproxy/proxies/default.xml file.
```
    <HTTPProxyConnection>
        <BasePath>/rm-localcollection</BasePath>
        <Properties/>
        <VirtualHost>default</VirtualHost>
    </HTTPProxyConnection>
```

?. ./deploy.sh

?. Test
```
curl -d @docs/request-successful.xml http://dbcedge-eval-prod.apigee.net/rm-localcollection
```


## Execution


### Example of request with validation errors
```
$ curl http://10.154.0.9:9015/rm-localcollection -d @request-with-errors.xml

ERROR: cvc-complex-type.2.4.a: Invalid content was found starting with element 'institutsNummer'. One of '{"http://dyns":produktId}' is expected.bash-3.2$ 
bash-3.2$ 
```
Or for an request with HOST header:
```
curl  -H "Content-Type: application/xml" -H "Host: $ORG-prod.exco.com" $APIURL/soapvalidate -d @test-request-seqerror.xml
```

### Example of successfull request

```
curl http://10.154.0.9:9015/rm-localcollection -d @request-successful.xml

<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">    <SOAP-ENV:Header>        <v3:integrationHeader xmlns:v3="http://www.royalmailgroup.co
m/integration/core/V3">...
```


<hr>

# Roayl Mail Web Service Definition

As a realistic example of a web service definition with realistic complexity of schema location references, we are going to use Royal Mail [Collect Location](https://developer.royalmail.net/node/1858266) service.





## Direct SOAP Specification File Link:

[Royal Mail Local Collect V2 (SOAP)_1.0.3.zip](https://developer.royalmail.net/sites/developer.royalmail.net/files/apiwsdl/5b7697e90cf2a0dd26ba2b8c/Royal%2BMail%2BLocal%2BCollect%2BV2%2B%2528SOAP%2529_1.0.3.zip)

<hr>



## Royal Mail Client Application [Optional]

If desired, you can register as a third-party developer for Royal Mail API and make example even more realistic by passing the incoming request to the Location Collect server if it is successfully validated.

### Steps to register Application

?. Navigate to https://developer.royalmail.net/user/register

?. Create an Account

E-mail address: 
dbcedge@gmail.com

Password: <password>

?. Press Create new account button.

?. Click on the link in your email to active an account.

?. Login into your developer account.

?. Select My Apps menu item.

?. Click on register your application link.

? Register an application:
Name: dbc-edge-royalmail

?. Submit.

?. After the application is successfully create, copy your Client Id. It is a string in a form:

a16e0576-eb06-5108-a3a0-91c07de8ce0

?. Initialise the CLIENT_ID variable in your shell environment, I.e.:

export CLIENT_ID=a15e0475-eb05-4108-a3a0-91c07de58ce0


### Test Request
curl --request POST \
  --url https://api.royalmail.net/ \
  --header 'accept: application/soap+xml' \
  --header 'content-type: text/xml' \
  --header 'soapaction: http://royalmail.com/LocalCollectInterfacev3_0/getLocalCollectLocations' \
  --header 'x-ibm-client-id: REPLACE_THIS_KEY' \
  --data '@docs/request.xml'




# Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## License

This material is copyright 2018-2019, Google LLC.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.

## Status

This is a community supported project. There is no warranty for this code.
If you have problems or questions, ask on [commmunity.apigee.com](https://community.apigee.com).
