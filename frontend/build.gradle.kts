import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.COMMONJS

plugins {
    id("org.jetbrains.kotlin.js")
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
    implementation(kotlin("stdlib-js", "1.4.0-rc"))
    implementation(React.css)

    implementation(React.react)
    implementation(npm("react", "16.13.1"))

    implementation(React.reactDom)
    implementation(npm("react-dom", "16.13.1"))

    implementation(React.styled)
    implementation(npm("styled-components", "5.1.1"))
    implementation(npm("inline-style-prefixer", "6.0.0"))

    implementation(React.extensions)
    implementation(Kotlin.coroutines)

    implementation(npm("semantic-ui-react", "1.1.1", generateExternals = false))
    implementation(npm("semantic-ui-css", "*", generateExternals = false))
    implementation(npm("sass-loader", "9.0.3", generateExternals = false))
    implementation(npm("sass", "1.26.10", generateExternals = false))
    implementation(npm("babel-loader", "8.1.0", generateExternals = false))
    implementation(npm("@babel/core", "7.11.1", generateExternals = false))
    implementation(npm("@babel/preset-env", "7.11.0", generateExternals = false))
    implementation(npm("file-loader", "6.0.0", generateExternals = false))
    implementation(npm("url-loader", "4.1.0", generateExternals = false))
    implementation(npm("ttf-loader", "1.0.2", generateExternals = false))
}

kotlin {
    target {
        useCommonJs()
        browser {
            distribution {
                directory = file("$projectDir/output/")
            }
            webpackTask {
                this.cssSupport.rules
                cssSupport.enabled = true
                output.libraryTarget = COMMONJS
                outputFileName = "dashboard.js"
            }

//            dceTask {
//                keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
//            }

            runTask {
                cssSupport.enabled = true
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