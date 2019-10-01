FROM openjdk:8-jdk-alpine

ENV APPLICATION_USER tombspawn
RUN adduser -D -g '' $APPLICATION_USER

ENV ANDROID_HOME /opt/android/sdk/
ENV API_LEVELS platforms;android-28
ENV BUILD_TOOLS_VERSIONS build-tools;29.0.2
ENV ANDROID_EXTRAS extra-android-m2repository,extra-google-google_play_services,extra-google-m2repository
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools
ENV REPO_OS_OVERRIDE linux

RUN apk update && apk add --no-cache bash unzip libstdc++

RUN mkdir -p ${ANDROID_HOME} \
    && chown -R ${APPLICATION_USER} ${ANDROID_HOME} \
    && cd /opt \
    && wget -q https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip -O android-sdk-tools.zip \
    && unzip -q android-sdk-tools.zip -d ${ANDROID_HOME} \
    && rm -f android-sdk-tools.zip \
    && echo yes | sdkmanager --install "tools" "build-tools;29.0.2" "platform-tools" "platforms;android-28" \
        "extras;google;google_play_services" --verbose \
    && echo yes | sdkmanager --licenses

#Add support for git
RUN apk --update add git less openssh && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*

#Add support for curl
RUN apk --no-cache add curl

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY build/libs/application.jar /app/application.jar
WORKDIR /app

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "application.jar", "-config=application.conf"]