#!/bin/bash

# Usage:

#  export ORG=dbcedge-eval
#  export ENV=test
#  export MSURL=https://api.enterprise.apigee.com
#  

function check_envvars() {
    varlist=$1

    varsnotset="F"

    for v in $varlist; do
        if [ -z "${!v}" ]; then
            echo "Required environment variable $v is not set."
            varsnotset="T"
        fi
    done

    if [ "$varsnotset" = "T" ]; then
        echo ""
        echo "Aborted. Please set up required variables."
        exit 1
    fi
}

check_envvars "ORG ENV MSURL"

ACTION=$1
kvm=$2

function urlencode(){

    echo -n "$1" | awk '{ gsub(/\//,"%2F"); printf("%s", $0) }'
}

if [ "createkvm"  == "$ACTION" ]; then
    check_envvars "kvm"
echo $kvm
    curl $CURL_DEBUG -n -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps -d @- << EOF
{ "name": "$kvm"  }
EOF


elif [ "listkvms"  == "$ACTION" ]; then

    curl $CURL_DEBUG -n -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps


elif [ "listkvmkeys"  == "$ACTION" ]; then

    if [[ "$MSURL" =~ .*api\.enterprise\.apigee\.com.* ]]; then

    
        curl $CURL_DEBUG -n -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps/$kvm/keys


    else

        curl $CURL_DEBUG -s -n -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps/$kvm | awk '/\]/{inkvm=0}
/\[/{inkvm=1}
inkvm==1 && /"name" \:/{print $3}'

    fi


elif [ "getvalue"  == "$ACTION" ]; then
    key=$3
    
    # fetch entry
    curl $CURL_DEBUG -n -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps/$kvm/entries/`urlencode $key`

elif [ "deletekey"  == "$ACTION" ]; then
    key=$3
    
    # delete entry
    curl $CURL_DEBUG -n -X DELETE -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps/$kvm/entries/`urlencode $key`
    
elif [ "putvalue"  == "$ACTION" ]; then

    key=$3
    file=$4

    function getpayload(){

	if [ "${file:0:1}" == "@" ]; then
	    awk '{ gsub(/\\/, "\\\\"); gsub(/\r/,""); gsub(/\t/,"\\t"); gsub(/"/,"\\\""); printf( "%s\\n",$0 ); }' ${file:1}
	else
	    printf "$file"
	fi
    }

    # update an entry in kvm
    if [[ "$MSURL" =~ .*api\.enterprise\.apigee\.com.* ]]; then
	# cps version, i.e., edge cloud
        curl -s $CURL_DEBUG -n -X POST -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps/$kvm/entries -d @- << EOF
{"name":"$key","value" : "`getpayload`" }
EOF
    
    
    else
        # non-cps version, i.e. on premises
        curl -s $CURL_DEBUG -n -X PUT -H "Content-Type: application/json" $MSURL/v1/organizations/$ORG/environments/$ENV/keyvaluemaps/$kvm -d @- << EOF
{"name":"$kvm","entry":[{"name":"`urlencode $key`","value" : "`getpayload`" }]
}
EOF

    fi


else

    echo "Unknown Action: $ACTION"
    echo ""
    echo "Supported actions:"
    echo ""
    echo "    kvm-ctl.sh createkvm <kvm>"
    echo "    kvm-ctl.sh listkvms"
    echo "    kvm-ctl.sh listkvmkeys <kvm>"
    echo "    kvm-ctl.sh getvalue <kvm> <key>"
    echo "    kvm-ctl.sh deletekey <kvm> <key>"
    echo "    kvm-ctl.sh putvalue <kvm> <key> <string or @file>"

fi
