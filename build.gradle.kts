import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.*
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.VerificationReportsFormats.MARKDOWN
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.VerificationReportsFormats.PLAIN

plugins {
    id("idea")
    id("java") // Java support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    id("jacoco") // Code coverage
    alias(libs.plugins.sonarqube) // SonarQube
}

group = "org.jboss.tools.intellij"
version = providers.gradleProperty("projectVersion").get() // Plugin version
val ideaVersion = providers.gradleProperty("platformVersion").get()
val devtoolsCommonForTests = "com.redhat.devtools.intellij:intellij-common:" + libs.devtools.common + ":test"

// Set the JVM language level used to build the project.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenLocal()
    maven { url = uri("https://repository.jboss.org") }
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(ideaVersion)

        // Bundled Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        // for local plugin -> https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin-faq.html#how-to-add-a-dependency-on-a-plugin-available-in-the-file-system
        //plugins.set(listOf(file("/path/to/plugin/")))

        pluginVerifier()

        testFramework(TestFrameworkType.Platform)
    }

    components {
        withModule("com.redhat.devtools.intellij:intellij-common") {
            withVariant("intellijPlatformComposedJar") {
                attributes {
                    attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
                }
            }
        }
    }

    implementation(libs.openshift.client)
    implementation(libs.devtools.common)
    implementation(libs.keycloak)
    implementation(libs.jjwt.impl)
    implementation(libs.jjwt.jackson)
    implementation(libs.converter.jackson)

    // for unit tests
    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.easytesting)
    testImplementation(libs.mockserver.client)
    testImplementation(libs.mockserver.netty)

    constraints {
        implementation("io.undertow:undertow-core:2.3.15.Final") { // keycloak
            because("https://security.snyk.io/vuln/SNYK-JAVA-IOUNDERTOW-6567186")
        }
        implementation("org.bouncycastle:bcprov-jdk18on:1.78.1") { // keycloak
            because("https://security.snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-6612984")
        }
        implementation("com.squareup.okhttp3:okhttp:4.12.0") { // converter.jackson/retrofit
            because("https://security.snyk.io/vuln/SNYK-JAVA-COMSQUAREUPOKHTTP3-2958044")
        }
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }

    publishing {
        token = providers.gradleProperty("jetBrainsToken")
        channels = providers.gradleProperty("jetBrainsChannel").map { listOf(it) }
    }

    pluginVerification {
        failureLevel = listOf(INVALID_PLUGIN, COMPATIBILITY_PROBLEMS, MISSING_DEPENDENCIES)
        verificationReportsFormats = listOf(MARKDOWN, PLAIN)
        ides {
            recommended()
        }
        freeArgs = listOf(
            "-mute",
            "TemplateWordInPluginId,TemplateWordInPluginName"
        )
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    fun supportsEnhancedClassRedefinition(): Boolean {
        val platformVersion = findProperty("platformVersion").toString().toFloatOrNull()
        return platformVersion != null
                && platformVersion >= 2024.1
    }

    runIde {
        if (supportsEnhancedClassRedefinition()) {
            jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=fatjar")
        }
        systemProperty("com.redhat.devtools.intellij.telemetry.mode", "debug")
        findProperty("tools.dl.path")?.let { systemProperty("tools.dl.path", it) }
        //systemProperty("jboss.sandbox.api.endpoint", "http://localhost:3000") // enable when running sandbox locally, see below
    }

    test {
        systemProperty("com.redhat.devtools.intellij.telemetry.mode", "disabled")
        jvmArgs("-Djava.awt.headless=true")
        useJUnit()
        testLogging {
            events(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showStackTraces = true
            showStandardStreams = false
        }
    }

    withType<Test> {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    jacocoTestReport {
        executionData.setFrom(fileTree(layout.buildDirectory).include("/jacoco/*.exec"))
        classDirectories.setFrom(instrumentCode)
        reports {
            xml.required = true
        }
    }

    jacocoTestCoverageVerification {
        classDirectories.setFrom(instrumentCode)
    }

    sonar {
        properties {
            property("sonar.projectKey", "redhat-developer_intellij-openshift-connector")
            property("sonar.organization", "redhat-developer")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.junit.reportsPath", layout.buildDirectory.dir("test-results").get().asFile.absolutePath)
            property("sonar.gradle.skipCompile", "true")
        }
    }

    register("runSandbox", JavaExec::class.java) {
        group = "Execution"
        description = "Run the Sandbox registration server in port 3000"
        classpath = sourceSets.test.get().runtimeClasspath
        mainClass.set("org.jboss.tools.intellij.openshift.ui.sandbox.SandboxRegistrationServerMock")
    }

    printProductsReleases {
        channels = listOf(ProductRelease.Channel.EAP)
        types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
        untilBuild = provider { null }
    }
}

sourceSets {
    create("it") {
        description = "integrationTest"
        java.srcDir("src/it/java")
        resources.srcDir("src/it/resources")
        compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.test.get().compileClasspath
        runtimeClasspath += output + compileClasspath + sourceSets.test.get().runtimeClasspath
    }
}

configurations {
    runtimeClasspath {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "bundledPlugin", module = "Docker") // suppress multiple SLF4J providers
    }
}

val integrationTest by intellijPlatformTesting.testIde.registering {
    task {
        systemProperty("com.redhat.devtools.intellij.telemetry.mode", "disabled")
        findProperty("tools.dl.path")?.let { systemProperty("tools.dl.path", it) }
        description = "Runs the integration tests."
        group = "verification"
        testClassesDirs = sourceSets["it"].output.classesDirs
        classpath = sourceSets["it"].runtimeClasspath
        testLogging {
            events(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showStackTraces = true
            showStandardStreams = false
        }
        jvmArgs("-Djava.awt.headless=true")
        useJUnitPlatform()
        shouldRunAfter(tasks["test"])
    }

    dependencies {
        testRuntimeOnly(libs.junit.jupiter.engine)
        testImplementation(libs.junit.platform.launcher)
        testImplementation(libs.junit.jupiter.api)
        testImplementation(libs.devtools.common.ui.test)
    }
    /*dependencies {
        testRuntimeOnly(libs.junit.vintage.engine)
        testImplementation()
        testImplementation(libs.junit.platform.suite)
        testImplementation(libs.junit.jupiter)
        testImplementation()
        testImplementation()
        testImplementation(devtoolsCommonForTests)
        testImplementation()
        testImplementation(libs.awaitility)
    }*/
}

val integrationUITest by intellijPlatformTesting.testIde.registering {
    task {
        systemProperty("com.redhat.devtools.intellij.telemetry.mode", "disabled")
        findProperty("tools.dl.path")?.let { systemProperty("tools.dl.path", it) }
        findProperty("testProjectLocation")?.let { systemProperty("testProjectLocation", it) }
        systemProperties["CLUSTER_ALREADY_LOGGED_IN"] = System.getenv("CLUSTER_ALREADY_LOGGED_IN") ?: false
        description = "Runs the cluster integration UI tests."
        group = "verification"
        testClassesDirs = sourceSets["it"].output.classesDirs
        classpath = sourceSets["it"].runtimeClasspath
        testLogging {
            events(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showStackTraces = true
            showStandardStreams = true
        }
        jvmArgs("-Djava.awt.headless=false") // use of clipboard in AboutPublicTest, set to false
        val includes = if (System.getenv("CLUSTER_ALREADY_LOGGED_IN") == null) "**/PublicTestsSuite.class" else "**/ClusterTestsSuite.class"
        include(includes)
        useJUnitPlatform {
            includeTags("ui-test")
        }
        shouldRunAfter(tasks["test"])
    }

    dependencies {
        testRuntimeOnly(libs.junit.jupiter.engine)
        testImplementation(libs.junit.platform.launcher)
        testImplementation(libs.junit.jupiter.api)
        testImplementation(libs.devtools.common.ui.test)
    }
/*
    dependencies {
        testImplementation(libs.junit.platform.launcher)
        testImplementation(libs.junit.platform.suite)
        testImplementation(libs.junit.jupiter)
        testImplementation(libs.junit.jupiter.api)
        testImplementation(libs.junit.jupiter.engine)
        testImplementation(devtoolsCommonForTests)
        testImplementation(libs.devtools.common.ui.test)
        testImplementation(libs.awaitility)
    }
    */

}

// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#runIdeForUiTests
val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-Djb.consents.confirmation.enabled=false",
            )
        }

        systemProperty("robot-server.port", System.getProperty("robot-server.port"))
        systemProperty("com.redhat.devtools.intellij.telemetry.mode", "disabled")
    }

    plugins {
        robotServerPlugin()
    }
}

// below is only to correctly configure IDEA project settings
idea {
    module {
        testSources.from(sourceSets["it"].java.srcDirs)
    }
}
