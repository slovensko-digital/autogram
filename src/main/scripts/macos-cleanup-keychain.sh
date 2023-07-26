#!/usr/bin/env bash -e

security list-keychains -d user -s login.keychain
security default-keychain -s login.keychain
