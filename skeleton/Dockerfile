FROM openjdk:8-jdk-slim

ENV APPLICATION_USER skeleton
ENV ANDROID_SDK_DIR /home/skeleton/android-sdk
ENV ANDROID_HOME /home/skeleton/android-sdk

RUN useradd -ms /bin/bash $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    curl

COPY setup_android_sdk.sh /app/setup_android_sdk.sh
RUN chmod +x /app/setup_android_sdk.sh

USER $APPLICATION_USER
RUN mkdir -p $HOME/.gradle
RUN mkdir -p /app/git

COPY build/libs/application.jar /app/application.jar
COPY gradle.properties /home/skeleton/.gradle/gradle.properties
WORKDIR /app
RUN /bin/bash /app/setup_android_sdk.sh -b -a