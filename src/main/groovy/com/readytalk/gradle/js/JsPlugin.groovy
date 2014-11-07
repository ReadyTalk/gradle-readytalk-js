package com.readytalk.gradle.js

import com.moowork.gradle.grunt.GruntPlugin
import com.moowork.gradle.grunt.GruntTask
import com.moowork.gradle.grunt.GruntInstallTask
import com.moowork.gradle.node.task.NpmInstallTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import groovy.json.JsonSlurper

class JsPlugin implements Plugin<Project> {
  @Override
  void apply(final Project project) {
    project.plugins.apply(BasePlugin)
    project.plugins.apply(GruntPlugin)

    project.tasks.withType(GruntTask) {
      dependsOn NpmInstallTask.NAME
    }

    project.tasks.withType(GruntInstallTask) {
      dependsOn NpmInstallTask.NAME
    }

    project.node {
        version = '0.10.32'
        npmVersion = '2.1.7'
        download = true
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
