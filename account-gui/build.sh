#!/bin/bash
rm -Rf public/bundle*
rm -Rf target/*
yarn install --force && yarn test && yarn build
