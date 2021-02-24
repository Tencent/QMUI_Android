#!/usr/bin/env bash

#./deploy.sh qmui publishToMavenLocal
#./deploy.sh arch publishToMavenLocal
#./deploy.sh type publishToMavenLocal

#./deploy.sh qmui publish
#./deploy.sh arch publish
#./deploy.sh type publish

#./deploy.sh qmui bintrayUpload
#./deploy.sh arch bintrayUpload
#./deploy.sh type bintrayUpload

if [[ "qmui" == "$1" ]]
then
    buildCmd="./gradlew :qmui:clean :qmui:build qmui:$2"
    $buildCmd
elif [[ "arch" == "$1" ]]
then
    buildCmd="./gradlew :arch:clean :arch:build :arch:$2"
    $buildCmd
    buildCmd="./gradlew :arch-annotation:clean :arch-annotation:build :arch-annotation:$2"
    $buildCmd
    buildCmd="./gradlew :arch-compiler:clean :arch-compiler:build :arch-compiler:$2"
    $buildCmd
elif [[ "type" == "$1" ]]
then
    buildCmd="./gradlew :type:clean :type:build type:$2"
    $buildCmd
elif [[ "lint" == "$1" ]]
then
    buildCmd="./gradlew :lint:clean :lint:build type:$2"
    $buildCmd
fi