#!/bin/sh

## This is only file used for testing on macos - it just removes certificates

TLS_DIR="$HOME/Library/Application Support/Autogram/tls"

# Any of these might work
# security remove-trusted-cert "$TLS_DIR/autogram-cert.pem"
# security delete-certificate -c "localhost.arcicode.com" "$HOME/Library/Keychains/login.keychain-db"
security delete-certificate -Z $(openssl x509 -in "$TLS_DIR/autogram-cert.pem" -outform DER | shasum -a 1 ) "$HOME/Library/Keychains/login.keychain-db"

rm -fr "$TLS_DIR"
