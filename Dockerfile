FROM openjdk:8-jdk-slim

RUN echo $JAVA_HOME

ENV APPLICATION_USER tombspawn
RUN useradd -ms /bin/bash $APPLICATION_USER

ENV ANDROID_HOME /opt/android/sdk/
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools
ENV REPO_OS_OVERRIDE linux
ENV GRADLE_HOME /home/${APPLICATION_USER}/.gradle

RUN apt-get update
RUN apt-get install unzip
RUN echo y | apt-get install wget

RUN mkdir -p ${ANDROID_HOME} \
    && mkdir /home/${APPLICATION_USER}/.android \
    && chown -R ${APPLICATION_USER} /home/${APPLICATION_USER}/.android \
    && cd /opt \
    && wget -q https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip -O android-sdk-tools.zip \
    && unzip -q android-sdk-tools.zip -d ${ANDROID_HOME} \
    && rm -f android-sdk-tools.zip \
    && echo y | sdkmanager --install "tools" "build-tools;29.0.2" "build-tools;28.0.3" "platform-tools" "platforms;android-28" \
        "extras;google;google_play_services" "extras;google;m2repository" "extras;android;m2repository" "extras;android;gapid;1" \
         "extras;android;gapid;3" "extras;google;instantapps" "extras;google;webdriver" --verbose \
    && echo y | sdkmanager --licenses \
    && chown -R ${APPLICATION_USER} ${ANDROID_HOME}

RUN apt-get update && apt-get install -y git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

RUN mkdir $GRADLE_HOME
COPY /scripts/gradle.properties ${GRADLE_HOME}/gradle.properties
RUN chown -R $APPLICATION_USER $GRADLE_HOME

USER $APPLICATION_USER

COPY grave/build/libs/application.jar /app/application.jar
WORKDIR /app

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "application.jar", "-config=application.conf"]