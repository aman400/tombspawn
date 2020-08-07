object Versions {
    const val jgitVersion = "5.7.0.202003110725-r"
    const val junitVersion = "4.13"
    const val ktorVersion = "1.3.2-1.4.0-rc"
    const val shadowJar = "5.2.0"
    const val daggerVersion = "2.28.3"
    const val exposed = "0.26.1"
    const val mockito = "3.2.4"
    const val grpc = "1.30.2"
}

object Classpaths {
    const val shadowJar = "com.github.jengelman.gradle.plugins:shadow:${Versions.shadowJar}"
}

object Jgit {
    const val lib = "org.eclipse.jgit:org.eclipse.jgit:${Versions.jgitVersion}"
}

object Kotlin {
    const val version = "1.4.0-rc"

    const val classpath = "org.jetbrains.kotlin:kotlin-gradle-plugin:${version}"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${version}"
    const val html = "org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1-1.4.0-rc"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8-1.4.0-rc"
}

object Auth {
    const val jwt = "com.auth0:java-jwt:3.8.1"
}

object Gson {
    const val lib = "com.google.code.gson:gson:2.8.6"
}

object Log {
    const val logback = "ch.qos.logback:logback-classic:1.2.3"
}

object Docker {
    const val lib = "com.github.docker-java:docker-java:3.2.1"
}

object Database {
    const val mysql = "mysql:mysql-connector-java:8.0.19"
    const val pool = "com.zaxxer:HikariCP:3.4.2"
    const val redis = "org.redisson:redisson:3.12.0"
}

object Exposed {
    const val core = "org.jetbrains.exposed:exposed-core:${Versions.exposed}"
    const val dao = "org.jetbrains.exposed:exposed-dao:${Versions.exposed}"
    const val jdbc = "org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}"
}

object Dagger {
    const val lib = "com.google.dagger:dagger:${Versions.daggerVersion}"
    const val annotationProcessor = "com.google.dagger:dagger-compiler:${Versions.daggerVersion}"
    const val assistedInjectLib = "com.squareup.inject:assisted-inject-annotations-dagger2:0.5.1"
    const val assistedInjectProcessor = "com.squareup.inject:assisted-inject-processor-dagger2:0.5.1"
}

object Apache {
    const val commonIO = "commons-io:commons-io:2.6"
    const val zip = "org.apache.commons:commons-compress:1.19"
    const val apacheCommons = "org.apache.commons:commons-io:1.3.2"
    const val text = "org.apache.commons:commons-text:1.8"
}

object Ktor {
    const val netty = "io.ktor:ktor-server-netty:${Versions.ktorVersion}"
    const val json = "io.ktor:ktor-client-json:${Versions.ktorVersion}"
    const val gson = "io.ktor:ktor-client-gson:${Versions.ktorVersion}"
    const val apache = "io.ktor:ktor-client-apache:${Versions.ktorVersion}"
    const val core = "io.ktor:ktor-server-core:${Versions.ktorVersion}"
    const val locations = "io.ktor:ktor-locations:${Versions.ktorVersion}"
    const val ktorGson = "io.ktor:ktor-gson:${Versions.ktorVersion}"
    const val sessions = "io.ktor:ktor-server-sessions:${Versions.ktorVersion}"
    const val jwt = "io.ktor:ktor-auth-jwt:${Versions.ktorVersion}"
    const val jvmLogging = "io.ktor:ktor-client-logging-jvm:${Versions.ktorVersion}"
    const val curl = "io.ktor:ktor-client-curl:${Versions.ktorVersion}"
}

object GRPC {
    const val netty = "io.grpc:grpc-netty-shaded:${Versions.grpc}"
    const val protobuf = "io.grpc:grpc-protobuf:${Versions.grpc}"
    const val stub = "io.grpc:grpc-stub:${Versions.grpc}"
    const val annot = "javax.annotation:javax.annotation-api:1.3.2"
}

object React {
    private const val reactVersion = "16.13.1-pre.110-kotlin-1.4.0-rc"
    private const val kotlinWrapper = "1.0.1-pre.110-kotlin-1.4.0-rc"

    const val react = "org.jetbrains:kotlin-react:$reactVersion"
    const val reactDom = "org.jetbrains:kotlin-react-dom:$reactVersion"
    const val css = "org.jetbrains:kotlin-css-js:1.0.0-pre.110-kotlin-1.4.0-rc"
    const val styled = "org.jetbrains:kotlin-styled:1.0.0-pre.110-kotlin-1.4.0-rc"
    const val extensions = "org.jetbrains:kotlin-extensions:$kotlinWrapper"
    const val html = "org.jetbrains.kotlinx:kotlinx-html-js:0.7.1-1.4.0-rc"
    const val common = "org.jetbrains.kotlinx:kotlinx-html-common:0.7.1-1.4.0-rc"

}

object Testing {
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoInline = "org.mockito:mockito-inline:${Versions.mockito}"
    const val ktor = "io.ktor:ktor-server-tests:${Versions.ktorVersion}"
    const val junit = "junit:junit:${Versions.junitVersion}"
    const val hamcrest = "org.hamcrest:hamcrest-all:1.3"
    const val javaHamcrest = "org.hamcrest:java-hamcrest:2.0.0.0"

    const val clientMock = "io.ktor:ktor-client-mock:${Versions.ktorVersion}"
    const val clientMockJvm = "io.ktor:ktor-client-mock-jvm:${Versions.ktorVersion}"
}