#!/bin/bash
rm -rf Node
mkdir -p Node
cat host |while read ip
do 
  echo $ip
  mkdir -p Node/$ip
  scp -r $ip:/tmp3/linux_src/hadoop-2.6.0/logs/userlogs/$1 Node/$ip
done
chmod 777 -R Node
find Node | grep stderr > tmp
cat tmp | while read file
do
  echo $file
  cat $file >> Node/error
done
