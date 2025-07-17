package org.processing.java.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.jetbrains.compose.reload.gradle.ComposeHotReloadPlugin
import org.jetbrains.compose.reload.gradle.ComposeHotRun

class ProcessingHotReloadPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(ComposeHotReloadPlugin::class.java)

        project.repositories.google()

        project.afterEvaluate {
            project.tasks.named("hotRun", ComposeHotRun::class.java){ task ->
                task.isAutoReloadEnabled.set(true)
            }
            project.tasks.named("run").configure { task ->
                task.dependsOn("hotRun")
            }
        }
    }
}