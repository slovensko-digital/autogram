#!/bin/bash -e
jpackage=$1
appDirectory=$2
jdkDirectory=$3
resourcesDir=$4
platform=$5
version=$6
output=$7

function checkExitCode() {
    exitValue=$1
    if [[ $exitValue -ne 0 ]]; then
        exit $exitValue
    fi
}

IFS="="
while read -r key value; do
    # Empty lines and comments
    if [[ -z "$key" ]] || [[ -z "$value" ]] || [[ $key == \#* ]]; then
        continue
    fi

    safekey=$(echo "$key" | tr . _)
    trimmedvalue=$(echo "$value" | sed 's/[[:space:]]*$//g' | sed 's/^[[:space:]]*//g')
    declare "properties_$safekey=$trimmedvalue"
done <"$resourcesDir/build.properties"
unset IFS

jvmOptions="-Dfile.encoding=UTF-8 --add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED"
arguments=(
    "--input" "$appDirectory"
    "--runtime-image" "$jdkDirectory"
    "--main-jar" "autogram.jar"
    "--app-version" "${properties_version:-$version}"
    "--copyright" "$properties_copyright"
    "--vendor" "$properties_vendor"
    "--icon" "./Autogram.png"
    "--license-file" "$appDirectory/LICENSE"
    "--resource-dir" "./"
    "--dest" "$output"
    "--description" "$properties_description"
)

if [[ "$platform" == "win" ]]; then
    cp "./main.template.wxs" "./main.wxs"
    sed -i -e "s/PROTOCOL_NAME/$properties_protocol/g" "./main.wxs"

    arguments+=(
        "--name" "$properties_name"
        "--type" "msi"
        "--icon" "./Autogram.ico"
        "--java-options" "$jvmOptions --add-opens jdk.crypto.mscapi/sun.security.mscapi=ALL-UNNAMED"
        "--win-shortcut-prompt"
        "--win-menu"
    )

    if [[ ! -z "$properties_win_upgradeUUID" ]]; then
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
    checkExitCode $?
fi

if [[ "$platform" == "linux" ]]; then
    cp "./autogram.template.desktop" "./autogram.desktop"
    sed -i -e "s/PROTOCOL_NAME/$properties_protocol/g" "./autogram.desktop"

    if [[ ! -z "$properties_linux_debMaintainer" ]]; then
        arguments+=(
            "--linux-deb-maintainer" "$properties_linux_debMaintainer"
        )
    fi

    if [[ ! -z "$properties_linux_appCategory" ]]; then
        arguments+=(
            "--linux-app-category" "$properties_linux_appCategory"
        )
    fi

    if [[ ! -z "$properties_linux_packageDeps" ]]; then
        arguments+=(
            "--linux-package-deps" "$properties_linux_packageDeps"
        )
    fi

    lowercase_name=$(echo "$properties_name" | tr '[:upper:]' '[:lower:]')

    arguments+=(
        "--name" "$lowercase_name"
        "--java-options" "$jvmOptions"
        "--linux-rpm-license-type" "${properties_linux_rpmLicenseType:-MIT}"
        "--linux-menu-group" "${properties_linux_menuGroup:-Office}"
        "--install-dir" "$properties_linux_installDir"
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
    checkExitCode $?

    if [[ -f "/etc/lsb-release" ]]; then
        arguments+=(
            "--type" "deb"
        )
        $jpackage "${arguments[@]}"
        checkExitCode $?
    fi
fi

if [[ "$platform" == "mac" ]]; then
    cp "./Info.plist.template" "./Info.plist"
    sed -i.bak "s/PROTOCOL_NAME/$properties_protocol/g" "./Info.plist" && rm "./Info.plist.bak"

    arguments+=(
        "--name" "$properties_name"
        "--type" "pkg"
        "--icon" "./Autogram.icns"
        "--java-options" "$jvmOptions"
        "--mac-app-category" "${properties_mac_appCategory:-business}"
        "--mac-entitlements" "./Autogram.entitlements"
        # Building on mac requires modifying of image files
        # So the temp files have to be on relative path
        "--temp" "./DTempFiles"
    )

    if [[ ! -z "$properties_mac_identifier" ]]; then
        arguments+=(
            "--mac-package-identifier" "$properties_mac_identifier"
        )
    fi

    if [[ ! -z "$properties_mac_name" ]]; then
        arguments+=(
            "--mac-package-name" "$properties_mac_name"
        )
    fi

    if [[ "$properties_mac_sign" == "1" ]]; then
        export JPACKAGE_MAC_SIGN="1"
        if [[ -z "$APPLE_DEVELOPER_IDENTITY" ]] || [[ -z "$APPLE_KEYCHAIN_PATH" ]]; then
            echo "Missing APPLE_DEVELOPER_IDENTITY or APPLE_KEYCHAIN_PATH env variable"
            exit 1
        fi

        mac_signingKeyUserName=$(echo $APPLE_DEVELOPER_IDENTITY | sed -ne 's/Developer ID Application\:[[:space:]]\(.*\)[[:space:]]([0-9A-Z]*)/\1/p')
        arguments+=(
            "--mac-sign"
            "--mac-signing-keychain" "$APPLE_KEYCHAIN_PATH"
            "--mac-signing-key-user-name" "$mac_signingKeyUserName"
        )
    fi

    # cwd je ./src/main/scripts/resources
    $jpackage "${arguments[@]}"
    exitValue=$?
    # See --temp argument above
    rm -rf ./DTempFiles

    checkExitCode $exitValue
fi
