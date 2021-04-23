#!/bin/bash

# latest
git config pull.rebase false
git fetch origin
git reset --hard origin/master

# build
lein clean
lein uberjar

mv target/uberjar/athens-backend.jar ~/athens/app

# reload
systemctl daemon-reload
systemctl enable daemon-tools.service
systemctl start daemon-tools.service
systemctl restart daemon-tools.service