package com.readytalk.gradle.js

import com.moowork.gradle.grunt.GruntInstallTask
import com.moowork.gradle.grunt.GruntPlugin
import com.moowork.gradle.grunt.GruntTask
import com.moowork.gradle.gulp.GulpInstallTask
import com.moowork.gradle.gulp.GulpPlugin
import com.moowork.gradle.gulp.GulpTask
import com.moowork.gradle.node.task.NpmInstallTask
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class JsPlugin implements Plugin<Project> {
  @Override
  void apply(final Project project) {
    project.plugins.apply(BasePlugin)
    project.plugins.apply(GruntPlugin)
    project.plugins.apply(GulpPlugin)

    project.tasks.withType(GruntTask) {
      dependsOn NpmInstallTask.NAME, GruntPlugin.GRUNT_INSTALL_NAME
    }

    project.tasks.withType(GruntInstallTask) {
      dependsOn NpmInstallTask.NAME
    }

    project.tasks.withType(GulpTask) {
      dependsOn NpmInstallTask.NAME, GulpPlugin.GULP_INSTALL_NAME
    }

    project.tasks.withType(GulpInstallTask) {
      dependsOn NpmInstallTask.NAME
    }

    project.node {
      version = '0.10.32'
      npmVersion = '2.1.7'
      download = true
      workDir = project.file("${project.buildDir}/nodejs")
    }

    if (project.version == 'unspecified' && project.file('package.json').exists()) {
      project.version = getPackageJsonVersion(project)
    }
  }

  private static getPackageJsonVersion(final Project project) {
    def packageSlurper = new JsonSlurper()
    def packageJson = packageSlurper.parse(project.file('package.json'))
    return packageJson.version
  }
}
