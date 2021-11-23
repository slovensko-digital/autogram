#!/bin/bash
jpackage=$1
appDirectory=$2
jdkDirectory=$3
resourcesDir=$4
platform=$5
version=$6
output=$7

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
done < "$resourcesDir/build.properties"
unset IFS

jvmOptions="--add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED"
arguments=(
    "--input" "$appDirectory"
    "--runtime-image" "$jdkDirectory"
    "--main-jar" "whitelabel.jar"
    "--name" "$properties_name"
    "--app-version" "${properties_version:-$version}"
    "--copyright" "$properties_copyright"
    "--vendor" "$properties_vendor"
    "--icon" "./Octosign.png"
    "--license-file" "$appDirectory/LICENSE"
    "--resource-dir" "./"
    "--dest" "$output"
)

if [[ "$platform" == "win" ]]; then
    cp "./main.template.wxs" "./main.wxs"
    sed -i -e "s/PROTOCOL_NAME/$properties_protocol/g" "./main.wxs"

    arguments+=(
        "--type" "msi"
        "--icon" "./Octosign.ico"
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
    cp "./Octosign.template.desktop" "./Octosign.desktop"
    sed -i -e "s/PROTOCOL_NAME/$properties_protocol/g" "./Octosign.desktop"

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
        "--java-options" "$jvmOptions"
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

if [[ "$platform" == "mac" ]]; then
    cp "./Info.plist.template" "./Info.plist"
    sed -i.bak "s/PROTOCOL_NAME/$properties_protocol/g" "./Info.plist" && rm "./Info.plist.bak"

    arguments+=(
        "--type" "pkg"
        "--icon" "./Octosign.icns"
        "--java-options" "$jvmOptions"
        "--mac-app-category" "${properties_mac_appCategory:-business}"
        # Building on mac requires modifying of image files
        # So the temp files have to be on relative path
        "--temp" "./DTempFiles"
    )

    if [[ ! -z "$properties_mac_identifier"  ]]; then
        arguments+=(
            "--mac-package-identifier" "$properties_mac_identifier"
        )
    fi

    if [[ ! -z "$properties_mac_name"  ]]; then
        arguments+=(
            "--mac-package-name" "$properties_mac_name"
        )
    fi

    if [[ "$properties_mac_sign" == "1" ]]; then
        arguments+=(
            "--mac-sign"
            "--mac-signing-keychain" "$properties_mac_signingKeychain"
            "--mac-signing-key-user-name" "$properties_mac_signingKeyUserName"
        )
    fi

    $jpackage "${arguments[@]}"

    # See --temp argument above
    rm -rf ./DTempFiles
fi
