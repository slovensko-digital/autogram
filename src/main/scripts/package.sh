#!/bin/bash
IFS="="
while read -r key value
do
    # Empty lines and comments
    if [[ -z "$key" ]] || [[ -z "$value" ]] || [[ $key == \#* ]]; then
        continue
    fi

    safekey=$(echo "$key" | tr . _)
    declare "properties_$safekey=$value"
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
jvmOptions="--illegal-access=warn --add-opens jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED"

$jpackage\
    --input "$appDirectory"\
    --runtime-image "$jdkDirectory"\
    --main-jar ./whitelabel.jar\
    --name "$properties_name"\
    --app-version "$properties_version"\
    --copyright "$properties_copyright"\
    --vendor "$properties_vendor"\
    --java-options "$jvmOptions"\
    --icon ./icon.png\
    --license-file "$appDirectory/LICENSE"\
    --dest "$output"
# TODO: Use --resource-dir for resource overrides
