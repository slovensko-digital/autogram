#!/bin/bash

set -e

jpackage=${1}
appDirectory=${2}
jdkDirectory=${3}
resourcesDir=${4}
platform=${5}
version=${6}
output=${7}

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
    --add-exports jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED \
    --add-opens jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED \
    --add-opens java.base/java.security=ALL-UNNAMED \
    --add-opens jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED \
    --enable-native-access=javafx.graphics,javafx.web,ALL-UNNAMED"
    
arguments=(
    "--app-version" "${properties_version:-$version}"
    "--copyright" "${properties_copyright}"
    "--vendor" "${properties_vendor}"
    "--resource-dir" "./"
    "--dest" "${output}"
    "--description" "${properties_description}"
)

# Options of the application image (launchers and bundled runtime)
appImageArguments=(
    "--input" "${appDirectory}"
    "--runtime-image" "${jdkDirectory}"
    "--main-jar" "autogram.jar"
    "--icon" "./Autogram.png"
)

# Options of the installer package
installerArguments=(
    "--license-file" "${appDirectory}/LICENSE"
    "--file-associations" "${resourcesDir}/FApdf.properties"
    "--file-associations" "${resourcesDir}/FAasice.properties"
)

if [[ "${platform}" == "win" ]]; then
    cp "./main.template.wxs" "./main.wxs"
    sed -i -e "s/PROTOCOL_NAME/${properties_protocol}/g" "./main.wxs"

    arguments+=(
        "--name" "${properties_name}"
        "--description" "${properties_name}"
    )

    appImageArguments+=(
        "--icon" "./Autogram.ico"
        "--java-options" "${jvmOptions} --add-opens jdk.crypto.mscapi/sun.security.mscapi=ALL-UNNAMED"
        "--add-launcher" "autogram-cli=$resourcesDir/windows-cli-build.properties"
    )

    installerArguments+=(
        "--type" "msi"
        "--win-menu"
    )

    if [[ -n "${properties_win_upgradeUUID}" ]]; then
        installerArguments+=(
            "--win-upgrade-uuid" "${properties_win_upgradeUUID}"
        )
    fi

    if [[ -z "${properties_win_menu}" ]] || [[ "${properties_win_menu}" -ne "0" ]]; then
        installerArguments+=(
            "--win-menu"
            "--win-menu-group" "${properties_win_menuGroup:-$properties_vendor}"
        )
    fi

    if [[ "${properties_win_perUserInstall}" == "1" ]]; then
        installerArguments+=(
            "--win-per-user-install"
        )
    fi

    if [[ "${properties_win_shortcut}" == "1" ]]; then
        installerArguments+=(
            "--win-shortcut"
        )
    fi

    packageTypes=" ${WINDOWS_PACKAGE_TYPES:-msi msix} "

    # Build the application image first so that its executables can be signed
    # before they are packed into the MSI
    appImage="${output}/${properties_name}"
    rm -rf "${appImage}"

    $jpackage "${arguments[@]}" "${appImageArguments[@]}" "--type" "app-image"
    checkExitCode $?

    # When packaging an app image, jpackage gives every additional launcher the same
    # shortcuts as the main launcher, so the CLI launcher is removed from the app image
    # metadata to be packaged as a plain executable without shortcuts
    sed -i -e 's|<add-launcher name="autogram-cli"[^>]*>.*</add-launcher>||' "${appImage}/app/.jpackage.xml"

    if [[ "${packageTypes}" == *" msi "* ]]; then
        if [[ -z "${AZURE_KEY_VAULT_URI}" ]] || ! command -v AzureSignTool > /dev/null; then
            echo "Warning: AZURE_KEY_VAULT_URI or AzureSignTool not available, skipping code signing of executables"
        else
            # Already signed files (e.g. from the JDK vendor) keep their original signature
            find "${appImage}" -type f \( -iname "*.exe" -o -iname "*.dll" \) > "${output}/files-to-sign.txt"

            # jpackage creates the launchers read-only
            chmod u+w "${appImage}"/*.exe

            AzureSignTool sign --description "${properties_name}" \
                -kvu "${AZURE_KEY_VAULT_URI}" -kvi "${AZURE_CLIENT_ID}" -kvt "${AZURE_TENANT_ID}" \
                -kvs "${AZURE_CLIENT_SECRET}" -kvc "${AZURE_CERT_NAME}" \
                -tr http://timestamp.digicert.com --skip-signed -v \
                --input-file-list "${output}/files-to-sign.txt"
            checkExitCode $?
        fi

        $jpackage "${arguments[@]}" "${installerArguments[@]}" "--app-image" "${appImage}"
        checkExitCode $?
    fi

    if [[ "${packageTypes}" == *" msix "* ]]; then
        makeappx=$(ls "/c/Program Files (x86)/Windows Kits/10/bin/"10.*/x64/makeappx.exe 2> /dev/null | sort -V | tail -n 1)
        windowsSdkBin=${WINDOWS_SDK_BIN:-${makeappx%/makeappx.exe}}
        if [[ ! -x "${windowsSdkBin}/makeappx.exe" ]]; then
            echo "Warning: Windows SDK not found, skipping MSIX package (WINDOWS_SDK_BIN can point to its bin/<version>/x64 directory)"
        else
            IFS=. read -r major minor build _ <<< "${properties_version:-$version}"
            msixVersion="${major}.${minor:-0}.${build:-0}.0"
            msixLayout="${output}/msix"
            msixResources="${output}/msix-resources"
            rm -rf "${msixLayout}" "${msixResources}"

            cp -r "${appImage}" "${msixLayout}"
            cp -r ./msix/Assets "${msixLayout}/Assets"
            sed -e "s|IDENTITY_NAME|${properties_win_msix_identityName:-$properties_name}|g" \
                -e "s|IDENTITY_PUBLISHER|${properties_win_msix_publisher:-CN=$properties_name}|g" \
                -e "s|IDENTITY_VERSION|${msixVersion}|g" \
                -e "s|PUBLISHER_DISPLAY_NAME|${properties_win_msix_publisherDisplayName:-$properties_vendor}|g" \
                -e "s|APP_NAME|${properties_name}|g" \
                -e "s|APP_DESCRIPTION|${properties_description}|g" \
                -e "s|PROTOCOL_NAME|${properties_protocol}|g" \
                ./msix/AppxManifest.xml > "${msixLayout}/AppxManifest.xml"

            mkdir -p "${msixResources}"
            cp -r ./msix/Assets "${msixResources}/Assets"
            # Windows tools take options starting with a slash, which Git Bash would convert to paths
            export MSYS_NO_PATHCONV=1
            "${windowsSdkBin}/makepri.exe" new /pr "$(cygpath -w "${msixResources}")" /cf "$(cygpath -w ./msix/priconfig.xml)" \
                /mn "$(cygpath -w "${msixLayout}/AppxManifest.xml")" /of "$(cygpath -w "${msixLayout}/resources.pri")" /o
            checkExitCode $?

            "${windowsSdkBin}/makeappx.exe" pack /d "$(cygpath -w "${msixLayout}")" \
                /p "$(cygpath -w "${output}/${properties_name}-${msixVersion}.msix")" /o /h SHA256
            checkExitCode $?
            unset MSYS_NO_PATHCONV
        fi
    fi
fi

if [[ "$platform" == "linux" ]]; then
    arguments+=("${appImageArguments[@]}" "${installerArguments[@]}")

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

if [[ "${platform}" == "mac" ]]; then
    # jpackage requires --temp to be missing or empty. Remove artifacts from an
    # interrupted prior build and clean this build's temporary files on any exit.
    rm -rf ./DTempFiles
    trap 'rm -rf ./DTempFiles' EXIT

    arguments+=("${appImageArguments[@]}" "${installerArguments[@]}")

    cp "./Info.plist.template" "./Info.plist"
    sed -i.bak "s/PROTOCOL_NAME/${properties_protocol}/g" "./Info.plist" && rm "./Info.plist.bak"

    arguments+=(
        "--name" "${properties_name}"
        "--type" "pkg"
        "--icon" "./Autogram.icns"
        "--java-options" "${jvmOptions}"
        "--mac-app-category" "${properties_mac_appCategory:-business}"
        "--mac-entitlements" "./Autogram.entitlements"
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
        if [[ -z "${APPLE_DEVELOPER_IDENTITY}" ]] || [[ -z "${APPLE_KEYCHAIN_PATH}" ]]; then
            echo "Warning: APPLE_DEVELOPER_IDENTITY or APPLE_KEYCHAIN_PATH not set, skipping code signing"
        else
            export JPACKAGE_MAC_SIGN="1"

            mac_signingKeyUserName=$(echo ${APPLE_DEVELOPER_IDENTITY} | sed -ne 's/Developer ID Application\:[[:space:]]\(.*\)[[:space:]]([0-9A-Z]*)/\1/p')
            arguments+=(
                "--mac-sign"
                "--mac-signing-keychain" "${APPLE_KEYCHAIN_PATH}"
                "--mac-signing-key-user-name" "${mac_signingKeyUserName}"
            )
        fi
    fi

    # cwd je ./src/main/scripts/resources
    $jpackage "${arguments[@]}"
    exitValue=$?

    checkExitCode $exitValue
fi
