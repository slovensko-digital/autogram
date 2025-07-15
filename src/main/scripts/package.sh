#!/bin/bash

set -e

jpackage=${1}
appDirectory=${2}
jdkDirectory=${3}
resourcesDir=${4}
platform=${5}
version=${6}
output=${7}

# Determine whether jpackage supports the new --arch parameter
# Modern jpackage versions use --arch instead of --target-arch
archOption="--arch"
if "$jpackage" --help 2>&1 | grep -q -- '--target-arch' && ! "$jpackage" --help 2>&1 | grep -q -- '--arch'; then
    archOption="--target-arch"
fi

function checkExitCode() {
    exitValue=${1}
    if [[ ${exitValue} -ne 0 ]]; then
        exit "${exitValue}"
    fi
}

IFS="="
while read -r key value; do
    # Empty lines and comments
    if [[ -z "${key}" ]] || [[ -z "${value}" ]] || [[ ${key} == \#* ]]; then
        continue
    fi

    safekey=$(echo "${key}" | tr . _)
    trimmedvalue=$(echo "${value}" | sed 's/[[:space:]]*$//g' | sed 's/^[[:space:]]*//g')
    declare "properties_${safekey}=${trimmedvalue}"
done < "${resourcesDir}/build.properties"
unset IFS

jvmOptions="-Dfile.encoding=UTF-8 \
    -Dprism.maxvram=2G \
    --add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED \
    --add-exports jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED \
    --add-opens java.base/java.security=ALL-UNNAMED \
    --add-opens jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED \
    --enable-native-access=javafx.graphics"
    
arguments=(
    "--input" "${appDirectory}"
    "--runtime-image" "${jdkDirectory}"
    "--main-jar" "autogram.jar"
    "--app-version" "${properties_version:-$version}"
    "--copyright" "${properties_copyright}"
    "--vendor" "${properties_vendor}"
    "--icon" "./Autogram.png"
    "--license-file" "${appDirectory}/LICENSE"
    "--resource-dir" "./"
    "--dest" "${output}"
    "--description" "${properties_description}"
)

if [[ "${platform}" == "win" ]]; then
    cp "./main.template.wxs" "./main.wxs"
    sed -i -e "s/PROTOCOL_NAME/${properties_protocol}/g" "./main.wxs"

    arguments+=(
        "--name" "${properties_name}"
        "--description" "${properties_name}"
        "--type" "msi"
        "--icon" "./Autogram.ico"
        "--java-options" "${jvmOptions} --add-opens jdk.crypto.mscapi/sun.security.mscapi=ALL-UNNAMED"
        "--win-menu"
        "--add-launcher" "autogram-cli=$resourcesDir/windows-cli-build.properties"
    )

    if [[ -n "${properties_win_upgradeUUID}" ]]; then
        arguments+=(
            "--win-upgrade-uuid" "${properties_win_upgradeUUID}"
        )
    fi

    if [[ -z "${properties_win_menu}" ]] || [[ "${properties_win_menu}" -ne "0" ]]; then
        arguments+=(
            "--win-menu"
            "--win-menu-group" "${properties_win_menuGroup:-$properties_vendor}"
        )
    fi

    if [[ "${properties_win_perUserInstall}" == "1" ]]; then
        arguments+=(
            "--win-per-user-install"
        )
    fi

    if [[ "${properties_win_shortcut}" == "1" ]]; then
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

    if [[ -n "${properties_linux_appCategory}" ]]; then
        arguments+=(
            "--linux-app-category" "${properties_linux_appCategory}"
        )
    fi

    if [[ -n "${properties_linux_packageDeps}" ]]; then
        arguments+=(
            "--linux-package-deps" "${properties_linux_packageDeps}"
        )
    fi

    lowercase_name=$(echo "${properties_name}" | tr '[:upper:]' '[:lower:]')

    arguments+=(
        "--name" "${lowercase_name}"
        "--java-options" "${jvmOptions}"
        "--linux-menu-group" "${properties_linux_menuGroup:-Office}"
        "--install-dir" "${properties_linux_installDir}"
    )

    if [[ "${properties_linux_shortcut}" == "1" ]]; then
        arguments+=(
            "--linux-shortcut"
        )
    fi

    # Load variables of distribution
    . /etc/os-release

    if [ "${ID}" == "fedora" ]; then
        arguments+=(
            "--type" "rpm"
        )
    fi

    if [ "${ID}" == "debian" ] || [ "${ID}" == "ubuntu" ]; then
        arguments+=(
            "--type" "deb"
        )
        if [[ -n "${properties_linux_debMaintainer}" ]]; then
            arguments+=(
                "--linux-deb-maintainer" "${properties_linux_debMaintainer}"
            )
        fi
    fi

    $jpackage "${arguments[@]}"
    checkExitCode $?
fi

if [[ "${platform}" == "mac-universal" ]]; then
    cp "./Info.plist.template" "./Info.plist"
    sed -i.bak "s/PROTOCOL_NAME/${properties_protocol}/g" "./Info.plist" && rm "./Info.plist.bak"

    baseArguments=(
        "--input" "${appDirectory}"
        "--runtime-image" "${jdkDirectory}"
        "--main-jar" "autogram.jar"
        "--app-version" "${properties_version:-$version}"
        "--copyright" "${properties_copyright}"
        "--vendor" "${properties_vendor}"
        "--resource-dir" "./"
        "--description" "${properties_description}"
        "--name" "${properties_name}"
        "--icon" "./Autogram.icns"
        "--java-options" "${jvmOptions}"
        "--mac-app-category" "${properties_mac_appCategory:-business}"
        "--temp" "./DTempFiles"
        "--type" "app-image"
    )

    if [[ -n "${properties_mac_identifier}" ]]; then
        baseArguments+=("--mac-package-identifier" "${properties_mac_identifier}")
    fi

    if [[ -n "${properties_mac_name}" ]]; then
        baseArguments+=("--mac-package-name" "${properties_mac_name}")
    fi

    signingArguments=()
    if [[ "${properties_mac_sign}" == "1" ]]; then
        export JPACKAGE_MAC_SIGN="1"
        if [[ -z "${APPLE_DEVELOPER_IDENTITY}" ]] || [[ -z "${APPLE_KEYCHAIN_PATH}" ]]; then
            echo "Missing APPLE_DEVELOPER_IDENTITY or APPLE_KEYCHAIN_PATH env variable"
            exit 1
        fi

        mac_signingKeyUserName=$(echo ${APPLE_DEVELOPER_IDENTITY} | sed -ne 's/Developer ID Application\:[[:space:]]\(.*\)[[:space:]]([0-9A-Z]*)/\1/p')
        signingArguments=(
            "--mac-sign"
            "--mac-signing-keychain" "${APPLE_KEYCHAIN_PATH}"
            "--mac-signing-key-user-name" "${mac_signingKeyUserName}"
            "--mac-entitlements" "./Autogram.entitlements"
        )
    fi

    # Build a single app image for the current architecture
    # Universal binary creation requires architecture-specific options not available in this jpackage version
    appImageDir="${output}/app-image"
    mkdir -p "${appImageDir}"
    $jpackage "${baseArguments[@]}" "${signingArguments[@]}" --dest "${appImageDir}"
    exitValue=$?
    rm -rf ./DTempFiles
    checkExitCode $exitValue

    appName="${properties_name}.app"

    $jpackage \
        --app-image "${appImageDir}/${appName}" \
        --name "${properties_name}" \
        --type pkg \
        --icon "./Autogram.icns" \
        --app-version "${properties_version:-$version}" \
        --resource-dir "./" \
        --dest "${output}" \
        --description "${properties_description}" \
        --mac-app-category "${properties_mac_appCategory:-business}" \
        --mac-package-identifier "${properties_mac_identifier}" \
        ${signingArguments[@]}
    exitValue=$?
    rm -rf ./DTempFiles
    checkExitCode $exitValue
fi

if [[ "${platform}" == "mac" ]]; then
    cp "./Info.plist.template" "./Info.plist"
    sed -i.bak "s/PROTOCOL_NAME/${properties_protocol}/g" "./Info.plist" && rm "./Info.plist.bak"

    arguments+=(
        "--name" "${properties_name}"
        "--type" "pkg"
        "--icon" "./Autogram.icns"
        "--java-options" "${jvmOptions}"
        "--mac-app-category" "${properties_mac_appCategory:-business}"
        # Building on mac requires modifying of image files
        # So the temp files have to be on relative path
        "--temp" "./DTempFiles"
    )

    if [[ -n "${properties_mac_identifier}" ]]; then
        arguments+=(
            "--mac-package-identifier" "${properties_mac_identifier}"
        )
    fi

    if [[ -n "${properties_mac_name}" ]]; then
        arguments+=(
            "--mac-package-name" "${properties_mac_name}"
        )
    fi

    if [[ "${properties_mac_sign}" == "1" ]]; then
        export JPACKAGE_MAC_SIGN="1"
        if [[ -z "${APPLE_DEVELOPER_IDENTITY}" ]] || [[ -z "${APPLE_KEYCHAIN_PATH}" ]]; then
            echo "Missing APPLE_DEVELOPER_IDENTITY or APPLE_KEYCHAIN_PATH env variable"
            exit 1
        fi

        mac_signingKeyUserName=$(echo ${APPLE_DEVELOPER_IDENTITY} | sed -ne 's/Developer ID Application\:[[:space:]]\(.*\)[[:space:]]([0-9A-Z]*)/\1/p')
        arguments+=(
            "--mac-sign"
            "--mac-signing-keychain" "${APPLE_KEYCHAIN_PATH}"
            "--mac-signing-key-user-name" "${mac_signingKeyUserName}"
            "--mac-entitlements" "./Autogram.entitlements"
        )
    fi

    # cwd je ./src/main/scripts/resources
    $jpackage "${arguments[@]}"
    exitValue=$?
    # See --temp argument above
    rm -rf ./DTempFiles

    checkExitCode $exitValue
fi
