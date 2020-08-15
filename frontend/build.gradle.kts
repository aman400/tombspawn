import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("org.jetbrains.kotlin.js")
    kotlin("plugin.serialization") version Kotlin.version
}

group = "com.tombspawn"
version = "1.0"

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    maven { setUrl("https://dl.bintray.com/kotlin/kotlinx/") }
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev/") }
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js", KotlinCompilerVersion.VERSION))
    implementation(Kotlin.serialization)
    implementation(React.css)

    implementation(React.react)
    implementation(npm("react", "16.13.1"))
    implementation(npm("uuid", "8.3.0"))

    implementation(React.reactDom)
    implementation(npm("react-dom", "16.13.1"))

    implementation(React.reactRouterDom)
    implementation(npm("react-router-dom", "5.2.0"))

    implementation(React.styled)
    implementation(npm("styled-components", "5.1.1"))
    implementation(npm("inline-style-prefixer", "6.0.0"))

    implementation(React.extensions)
    implementation(Kotlin.coroutines)

    implementation(npm("semantic-ui-react", "1.1.1", generateExternals = false))
    implementation(npm("semantic-ui-css", "2.4.1", generateExternals = false))
    implementation(npm("sass", "1.26.10", generateExternals = false))


    implementation(devNpm("sass-loader", "9.0.3"))
    implementation(devNpm("babel-loader", "8.1.0"))
    implementation(devNpm("@babel/core", "7.11.1"))
    implementation(devNpm("@babel/preset-env", "7.11.0"))
    implementation(devNpm("babel-preset-env", "1.7.0"))
    implementation(devNpm("@babel/preset-react", "7.10.4"))
    implementation(devNpm("@babel/polyfill", "7.10.4"))
    implementation(devNpm("file-loader", "6.0.0"))
    implementation(devNpm("url-loader", "4.1.0"))
    implementation(devNpm("ttf-loader", "1.0.2"))
}

kotlin {
    target {
        browser {
            webpackTask {
                cssSupport.enabled = true
                outputFileName = "dashboard.js"
            }

//            dceTask {
//                keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
//            }

            runTask {
                cssSupport.enabled = true
                this.outputFileName = "dashboard.js"
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
}