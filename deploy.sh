#!/usr/bin/env bash

#./deploy.sh qmui publishToMavenLocal
#./deploy.sh arch publishToMavenLocal
#./deploy.sh type publishToMavenLocal
#./deploy.sh compose-core publishToMavenLocal
#./deploy.sh compose publishToMavenLocal
#./deploy.sh photo publishToMavenLocal

#./deploy.sh qmui publish
#./deploy.sh arch publish
#./deploy.sh type publish
#./deploy.sh compose-core publish
#./deploy.sh compose publish
#./deploy.sh photo publish

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
    buildCmd="./gradlew :type:clean :type:build :type:$2"
    $buildCmd
elif [[ "compose-core" == "$1" ]]
then
    buildCmd="./gradlew :compose-core:clean :compose-core:build :compose-core:$2"
    $buildCmd
elif [[ "compose" == "$1" ]]
then
    buildCmd="./gradlew :compose:clean :compose:build :compose:$2"
    $buildCmd
elif [[ "photo" == "$1" ]]
then
    buildCmd="./gradlew :photo:clean :photo:build :photo:$2"
    $buildCmd
    buildCmd="./gradlew :photo-coil:clean :photo-coil:build :photo-coil:$2"
    $buildCmd
    buildCmd="./gradlew :photo-glide:clean :photo-glide:build :photo-glide:$2"
    $buildCmd
fi