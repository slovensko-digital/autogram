#!/bin/bash

declare -A properties
IFS="="
while read -r key value
do
    # Empty lines and comments
    if [[ -z "$key" ]] || [[ -z "$value" ]] || [[ $key == \#* ]]; then
        continue
    fi

    properties[$key]=$value
done < "./build.properties"
unset IFS

# Defaults for optional properties
# TODO: version, copyright

# TODO: Use platform-specific properties with MSCAPI open on Windows

jpackage=$1
appDirectory=$2
jdkDirectory=$3
platform=$4
output=$5
jvmoptions="--illegal-access=warn --add-opens jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED"

$jpackage\
    --input "$appDirectory"\
    --runtime-image "$jdkDirectory"\
    --main-jar ./whitelabel.jar\
    --name "${properties[name]}"\
    --app-version "${properties[version]}"\
    --copyright "${properties[copyright]}"\
    --vendor "${properties[vendor]}"\
    --java-options "$jvmoptions"\
    --icon ./icon.png\
    --license-file "$appDirectory/LICENSE"\
    --dest "$output"
# TODO: Use --resource-dir for resource overrides
