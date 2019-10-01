object Versions {
    const val jgitVersion = "5.4.3.201909031940-r"
    const val junitVersion = "4.12"
    const val kotlin = "1.3.50"
    const val ktorVersion = "1.3.0-beta-1"
    const val shadowJar = "5.1.0"
}

object Classpaths {
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val shadowJar = "com.github.jengelman.gradle.plugins:shadow:${Versions.shadowJar}"
}

object Jgit {
    const val lib = "org.eclipse.jgit:org.eclipse.jgit:${Versions.jgitVersion}"
}

object TestDeps {
    const val junit = "junit:junit:${Versions.junitVersion}"
}

object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
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

    const val test = "io.ktor:ktor-server-tests:${Versions.ktorVersion}"
}