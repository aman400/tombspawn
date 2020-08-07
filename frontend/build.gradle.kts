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
    implementation(kotlin("stdlib-js"))
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

}

kotlin {
    target {
        useCommonJs()
        browser {
            webpackTask {
                cssSupport.enabled = true
                output.libraryTarget = COMMONJS
            }

            dceTask {
                keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
            }

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