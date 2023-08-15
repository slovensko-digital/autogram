function create_cert() {
    TLS_DIR="$HOME/Library/Application Support/Autogram/tls"

    osascript -e 'return display dialog "Teraz vygenerujeme Váš osobný SSL certifikát. Následne si od Vás inštalátor vyžiada heslo, aby certifikát mohol byť pridaný ako dôveryhodný pre spojenie so Safari." with icon caution'
    if [ $? -ne 0 ]; then
        return 0
    fi

    mkdir -p "$TLS_DIR"

    # Create temp openssl configuration
    SSL_CONFIG_TMP=$(mktemp)
    echo "[ req ]
req_extensions = v3_req
x509_extensions = v3_req
distinguished_name = dn
prompt = no
encrypt_key = no

[ v3_req ]
subjectAltName = @alt_names
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth

certificatePolicies = 1.2.3.4

[dn]
C=SK
O=SSD
CN=loopback.autogram.slovensko.digital

[CA_default]
copy_extension=copy

[alt_names]
DNS.1=loopback.autogram.slovensko.digital

" >$SSL_CONFIG_TMP

    cat $SSL_CONFIG_TMP

    /usr/bin/openssl req -nodes -x509 -newkey rsa:4096 -keyout "$TLS_DIR/autogram-key.pem" -out "$TLS_DIR/autogram-cert.pem" -sha256 -days 365 -config $SSL_CONFIG_TMP

    /usr/bin/openssl pkcs12 -export -in "$TLS_DIR/autogram-cert.pem" -inkey "$TLS_DIR/autogram-key.pem" -out "$TLS_DIR/autogram-pkcs12-cert.p12" -name "autogram-pkcs12-cert" -passout pass:

    security -v add-trusted-cert -r trustRoot -p basic -p ssl -k $HOME/Library/Keychains/login.keychain-db "$TLS_DIR/autogram-cert.pem"

    # Cleanup openssl config
    rm $SSL_CONFIG_TMP
}

function remove_cert(){
    TLS_DIR="$HOME/Library/Application Support/Autogram/tls"
    security delete-certificate -Z $(openssl x509 -in "$TLS_DIR/autogram-cert.pem" -outform DER | shasum -a 1 ) "$HOME/Library/Keychains/login.keychain-db"
}
