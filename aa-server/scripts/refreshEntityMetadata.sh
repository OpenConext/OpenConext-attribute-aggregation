#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
SP_ENTITY_META_DATA=$SCRIPT_DIR/../src/main/resources/service-registry/saml20-sp-remote.json

if [ ! -f $SP_ENTITY_META_DATA ];
  then
    echo "File $SP_ENTITY_META_DATA does not exists."
    exit 1
fi

rm -fr $SP_ENTITY_META_DATA;

curl https://tools.surfconext.nl/export/saml20-sp-remote.json >> $SP_ENTITY_META_DATA