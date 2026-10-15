#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
diretorio="target/jwt-dev"
mkdir -p "$diretorio"

if [[ ! -f "$diretorio/privateKey.pem" || ! -f "$diretorio/publicKey.pem" ]]; then
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$diretorio/privateKey.pem"
    openssl pkey -in "$diretorio/privateKey.pem" -pubout -out "$diretorio/publicKey.pem"
    chmod 600 "$diretorio/privateKey.pem"
fi

printf 'Chaves JWT de desenvolvimento disponíveis em %s\n' "$diretorio"
