FROM openjdk:8-jdk-slim

ARG USER_ID
ARG GROUP_ID

RUN apt-get update \
     && apt-get clean \
     && echo y | apt-get install unzip \
     && echo y | apt-get install wget \
     && echo y | apt-get install curl \
     && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV REPO_OS_OVERRIDE linux

ENV DOCKER_CHANNEL stable
ENV DOCKER_VERSION 19.03.5

RUN set -eux; \
	\
# this "case" statement is generated via "update.sh"
	apkArch="$(uname -m)"; \
	case "$apkArch" in \
# amd64
		x86_64) dockerArch='x86_64' ;; \
# arm32v6
		armhf) dockerArch='armel' ;; \
# arm32v7
		armv7) dockerArch='armhf' ;; \
# arm64v8
		aarch64) dockerArch='aarch64' ;; \
		*) echo >&2 "error: unsupported architecture ($apkArch)"; exit 1 ;;\
	esac; \
	\
	if ! wget -O docker.tgz "https://download.docker.com/linux/static/${DOCKER_CHANNEL}/${dockerArch}/docker-${DOCKER_VERSION}.tgz"; then \
		echo >&2 "error: failed to download 'docker-${DOCKER_VERSION}' from '${DOCKER_CHANNEL}' for '${dockerArch}'"; \
		exit 1; \
	fi; \
	\
	tar --extract \
		--file docker.tgz \
		--strip-components 1 \
		--directory /usr/local/bin/ \
	; \
	rm docker.tgz; \
	\
	dockerd --version; \
	docker --version

COPY scripts/remote/docker-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["sh"]

RUN mkdir /app

RUN mkdir /skeleton
COPY /skeleton/build/libs/application.jar /skeleton/build/libs/application.jar
COPY /skeleton/Dockerfile /skeleton/Dockerfile

COPY grave/build/libs/application.jar /app/application.jar
WORKDIR /app


CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "application.jar", "/app/application.json"]