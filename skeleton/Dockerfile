FROM android-sdk as skeleton

RUN mkdir -p $HOME/.gradle
RUN mkdir -p /app/git

COPY build/libs/application.jar /app/application.jar
COPY gradle.properties /home/skeleton/.gradle/gradle.properties
