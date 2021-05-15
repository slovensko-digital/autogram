#!/bin/bash
IFS="="
while read -r key value
do
    # Empty lines and comments
    if [[ -z "$key" ]] || [[ -z "$value" ]] || [[ $key == \#* ]]; then
        continue
    fi

    safekey=$(echo "$key" | tr . _)
    trimmedvalue=$(echo "$value" | sed 's/[[:space:]]*$//g' | sed 's/^[[:space:]]*//g')
    declare "properties_$safekey=$trimmedvalue"
done < "./build.properties"
unset IFS

jpackage=$1
appDirectory=$2
jdkDirectory=$3
resourcesDir=$4
platform=$5
version=$6
output=$7

jvmOptions="--illegal-access=warn --add-opens jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED"
arguments=(
    "--input" "$appDirectory"
    "--runtime-image" "$jdkDirectory"
    "--main-jar" "whitelabel.jar"
    "--name" "$properties_name"
    "--app-version" "${properties_version:-$version}"
    "--copyright" "$properties_copyright"
    "--vendor" "$properties_vendor"
    "--icon" "$resourcesDir/Octosign.png"
    "--license-file" "$appDirectory/LICENSE"
    "--resource-dir" "$resourcesDir"
    "--dest" "$output"
    "--verbose"
)

if [[ "$platform" == "win" ]]; then
    cp "$resourcesDir/main.template.wxs" "$resourcesDir/main.wxs"
    sed -i -e "s/PROTOCOL_NAME/$properties_protocol/g" "$resourcesDir/main.wxs"

    arguments+=(
        "--type" "msi"
        "--icon" "$resourcesDir/Octosign.ico"
        "--java-options" "$jvmOptions --add-opens jdk.crypto.mscapi/sun.security.mscapi=ALL-UNNAMED"
    )

    if [[ ! -z "$properties_win_upgradeUUID"  ]]; then
        arguments+=(
            "--win-upgrade-uuid" "$properties_win_upgradeUUID"
        )
    fi

    if [[ -z "$properties_win_menu" ]] || [[ "$properties_win_menu" -ne "0" ]]; then
        arguments+=(
            "--win-menu"
            "--win-menu-group" "${properties_win_menuGroup:-$properties_vendor}"
        )
    fi

    if [[ "$properties_win_perUserInstall" == "1" ]]; then
        arguments+=(
            "--win-per-user-install"
        )
    fi

    if [[ "$properties_win_shortcut" == "1" ]]; then
        arguments+=(
            "--win-shortcut"
        )
    fi

fi

# TODO: Use platform-specific Linux properties

# TODO: Use platform-specific macOS properties

$jpackage "${arguments[@]}"
# TODO: Use --resource-dir for resource overrides
