#!/bin/bash
rm -Rf dist/*
rm -Rf target/*
#yarn install && yarn lint && yarn test && yarn run webpack
npm install && npm run webpack