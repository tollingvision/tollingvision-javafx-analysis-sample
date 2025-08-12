import org.gradle.internal.os.OperatingSystem

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink")          version "3.1.1"
}

dependencies {
    implementation(platform("io.grpc:grpc-bom:1.73.0"))

    implementation(platform("io.netty:netty-bom:4.2.2.Final"))
    implementation(platform("com.google.cloud:libraries-bom:26.62.0"))

    implementation("io.grpc:grpc-netty")
    implementation("com.smart-cloud-solutions:tollingvision:2.6.1")

    runtimeOnly("com.google.protobuf:protobuf-java:4.31.1")
    implementation("com.google.protobuf:protobuf-java-util:4.31.1")
    runtimeOnly("com.google.errorprone:error_prone_annotations:2.38.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    modularity.inferModulePath.set(true)
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

application {
    mainClass.set("com.smartcloudsolutions.tollingvision.samples.AnalysisSampleApp")
}

javafx {
    version = "17"
    modules = listOf("javafx.controls", "javafx.swing")
}

jlink {
    forceMerge(".*")
    mergedModule { additive = true }

    imageName.set("analysis-sample-runtime")
    options.addAll("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")

    jpackage {
        installerName = "AnalysisSample"

        // platform-specific installer format
        val os = OperatingSystem.current()
        installerType = when {
            os.isMacOsX   -> "dmg"
            os.isWindows  -> "exe"
            else          -> "deb"   // Linux
        }
    }
}
