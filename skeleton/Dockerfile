FROM openjdk:8-jdk-slim

ENV APPLICATION_USER skeleton

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
RUN mkdir -p $HOME/android-sdk
RUN mkdir -p /app/git

ENV ANDROID_SDK_DIR $HOME/android-sdk
ENV ANDROID_HOME $HOME/android-sdk

COPY build/libs/application.jar /app/application.jar
WORKDIR /app
RUN /bin/bash /app/setup_android_sdk.sh -b -a