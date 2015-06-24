#!/bin/bash

export JAVA_HOME=/usr/bin
export CLASS_PATH=../conf

for i in ../lib/*.jar; do
    CLASS_PATH=$CLASSPATH:$i
done

$JAVA_HOME/bin/java -classpath $CLASS_PATH org.dreamwork.smart.car.server.Main ../conf/car.cfg