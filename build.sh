#/bin/bash

cd /Users/aman/git/LazySocket && ./gradlew -q assembleWithArgs -Purl=abc.com -PfilePath=./public/ && cd - && rm -rf ./public && mv /Users/aman/git/LazySocket/app/public ./