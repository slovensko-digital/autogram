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

    $jpackage "${arguments[@]}"
fi

if [[ "$platform" == "linux" ]]; then
    cp "$resourcesDir/Octosign.template.desktop" "$resourcesDir/Octosign.desktop"
    sed -i -e "s/PROTOCOL_NAME/$properties_protocol/g" "$resourcesDir/Octosign.desktop"

    if [[ ! -z "$properties_linux_debMaintainer"  ]]; then
        arguments+=(
            "--linux-deb-maintainer" "$properties_linux_debMaintainer"
        )
    fi

    if [[ ! -z "$properties_linux_appCategory"  ]]; then
        arguments+=(
            "--linux-app-category" "$properties_linux_appCategory"
        )
    fi

    if [[ ! -z "$properties_linux_packageDeps"  ]]; then
        arguments+=(
            "--linux-package-deps" "$properties_linux_packageDeps"
        )
    fi

    arguments+=(
        "--linux-rpm-license-type" "${properties_linux_rpmLicenseType:-MIT}"
        "--linux-menu-group" "${properties_linux_menuGroup:-Office}"
    )

    if [[ "$properties_linux_shortcut" == "1" ]]; then
        arguments+=(
            "--linux-shortcut"
        )
    fi

    # Build both .rpm and .deb from Linux - should work from Debian-like distro but not the other way around
    arguments+=(
        "--type" "rpm"
    )
    $jpackage "${arguments[@]}"

    if [[ -f "/etc/lsb-release" ]]; then
        arguments+=(
            "--type" "deb"
        )
        $jpackage "${arguments[@]}"
    fi
fi

# TODO: Use platform-specific macOS properties
