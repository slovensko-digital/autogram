#!/bin/bash

cd /app
export JAVA_HOME=/usr/lib/jvm/$(ls /usr/lib/jvm/ | grep bellsoft-java24-full)
export PATH=$JAVA_HOME/bin:$PATH
./mvnw versions:set -DnewVersion=${VERSION}
./mvnw -B -C -V package -P system-jdk
for ext in deb rpm; do
    for i in $(ls target/autogram*.${ext} 2>/dev/null); do
        mv "$i" "target/${PREFIX}_$(basename "$i")"
    done
    ls -lah ./target
    cp target/${PREFIX}_*.${ext} /data/
done
