#!/bin/bash

# works with
# ubuntu(20 lts)

# assumes execution from proj dir
proj_dir=`pwd`

# make
apt-get install build-essential

# daemontools
wget https://cr.yp.to/daemontools/daemontools-0.76.tar.gz
mkdir -p /usr/lib/daemontools
tar -xvf daemontools-0.76.tar.gz -C /usr/lib/daemontools
cd /usr/lib/daemontools/admin/daemontools-0.76
sed -i 's/extern int errno/#include <errno.h>\n/' src/error.h
./package/install

# setup paths
cd $proj_dir
mkdir -p ~/athens/db/athens-sync
mkdir -p ~/athens/app
mkdir -p ~/athens/log/

# service paths
mkdir -p /service/athens
mkdir -p /service/athens/supervise
cp ./deployment/setup/run /service/athens/run
chmod +x /service/athens/run
chmod 700 /service/athens/supervise

# service setup
cp -f ./deployment/setup/daemon-service.txt /etc/systemd/system/daemon-tools.service
systemctl daemon-reload