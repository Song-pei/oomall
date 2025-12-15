#!/bin/bash
docker rm $(docker ps -a -q)

cd /root/oomall2025/conf/mysql
git pull
#rm -r sql
#unzip sql.zip

for M in 'payment' 'shop' 'product'  'region' 'freight'
do
  docker exec -i $(docker container ls -aq -f name=mysql.*) mysql -udemouser -pJavaEE2025-10-27 -D $M < /root/oomall2025/conf/mysql/sql/$M.sql
done