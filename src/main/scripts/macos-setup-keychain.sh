#!/usr/bin/env bash -e

security unlock-keychain $APPLE_KEYCHAIN_PATH
security list-keychains -d user -s $APPLE_KEYCHAIN_PATH login.keychain
security default-keychain -s $APPLE_KEYCHAIN_PATH