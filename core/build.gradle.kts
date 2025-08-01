import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.internal.os.OperatingSystem

plugins {
    id("java")
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("src")
        }
        resources{
            srcDirs("src")
            exclude("**/*.java")
        }
    }
    test{
        java{
            srcDirs("test")
        }
    }
}

val lwjglVersion = "3.3.6"

val lwjglNatives = Pair(
	System.getProperty("os.name")!!,
	System.getProperty("os.arch")!!
).let { (name, arch) ->
	when {
		arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
			if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
				"natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
			else if (arch.startsWith("ppc"))
				"natives-linux-ppc64le"
			else if (arch.startsWith("riscv"))
				"natives-linux-riscv64"
			else
				"natives-linux"
		arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) }     ->
			"natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
		arrayOf("Windows").any { name.startsWith(it) }                ->
			"natives-windows"
		else                                                                            ->
			throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
	}
}

dependencies {
    implementation(libs.jogl)
    implementation(libs.gluegen)

    // TODO: Improve this
    implementation(files("library/JavaANGLE.jar"))
    testImplementation(libs.junit)

    // LWJGL
	implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
	implementation("org.lwjgl", "lwjgl")
	implementation("org.lwjgl", "lwjgl-egl")
	implementation("org.lwjgl", "lwjgl-glfw")
	implementation("org.lwjgl", "lwjgl-opengles")
	implementation ("org.lwjgl", "lwjgl", classifier = lwjglNatives)
	implementation ("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
	implementation ("org.lwjgl", "lwjgl-opengles", classifier = lwjglNatives)

}



mavenPublishing{
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom{
        name.set("Processing Core")
        description.set("Processing Core")
        url.set("https://processing.org")
        licenses {
            license {
                name.set("LGPL")
                url.set("https://www.gnu.org/licenses/lgpl-2.1.html")
            }
        }
        developers {
            developer {
                id.set("steftervelde")
                name.set("Stef Tervelde")
            }
            developer {
                id.set("benfry")
                name.set("Ben Fry")
            }
        }
        scm{
            url.set("https://github.com/processing/processing4")
            connection.set("scm:git:git://github.com/processing/processing4.git")
            developerConnection.set("scm:git:ssh://git@github.com/processing/processing4.git")
        }
    }
}


tasks.test {
    useJUnit()
}
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.compileJava{
    options.encoding = "UTF-8"
}
