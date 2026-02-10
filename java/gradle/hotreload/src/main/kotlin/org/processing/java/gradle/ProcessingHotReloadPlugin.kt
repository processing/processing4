package org.processing.java.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.jetbrains.compose.reload.gradle.ComposeHotReloadPlugin

class ProcessingHotReloadPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(ComposeHotReloadPlugin::class.java)

        project.repositories.google()
        project.extensions.getByType(JavaPluginExtension::class.java).toolchain {
            it.languageVersion.set(JavaLanguageVersion.of(21))
            it.vendor.set(JvmVendorSpec.JETBRAINS)
        }

        project.afterEvaluate {
            project.tasks.named("build").configure { task ->
                task.finalizedBy("reload")
            }
            project.tasks.named("run").configure { task ->
                task.dependsOn("hotRun")
            }
        }
    }
}