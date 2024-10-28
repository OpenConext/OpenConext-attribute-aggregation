#!/bin/bash
rm -Rf dist/*
rm -Rf target/*
#yarn install && yarn lint && yarn test && yarn run webpack
source $NVM_DIR/nvm.sh
nvm use
npm install && npm run webpack