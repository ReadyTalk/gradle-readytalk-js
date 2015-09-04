package com.readytalk.gradle.js

import com.moowork.gradle.grunt.GruntInstallTask
import com.moowork.gradle.grunt.GruntPlugin
import com.moowork.gradle.grunt.GruntTask
import com.moowork.gradle.gulp.GulpInstallTask
import com.moowork.gradle.gulp.GulpPlugin
import com.moowork.gradle.gulp.GulpTask
import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.task.NpmInstallTask
import com.moowork.gradle.node.task.SetupTask
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class JsPlugin implements Plugin<Project> {
  private Project project

  @Override
  void apply(final Project project) {
    this.project = project

    project.plugins.apply(BasePlugin)
    project.plugins.apply(GruntPlugin)
    project.plugins.apply(GulpPlugin)

    if(!project.hasProperty('generateNodeWrapper')) {
      project.extensions.extraProperties.set('generateNodeWrapper', true)
    }
    injectNodewSetup()

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
      version = '0.12.4'
      npmVersion = '2.1.7'
      download = true
      workDir = project.file("${project.buildDir}/nodejs")
    }

    if (project.version == 'unspecified' && project.file('package.json').exists()) {
      project.version = getPackageJsonVersion(project)
    }
  }

  def void injectNodewSetup() {
    NodeExtension nodeExt = project.extensions.findByType(NodeExtension)
    project.tasks.withType(SetupTask)*.doLast {
      if(project.generateNodeWrapper) {
        def wrapperFile = project.file('nodew')
        wrapperFile.text = """#!/usr/bin/env bash

PLATFORM="\$(uname -s | tr '[:upper:]' '[:lower:]')"
SCRIPT_PATH="${project.projectDir}"

#These can be overridden via environment variables if desired
NODE_HOME="\${NODE_HOME:-\${SCRIPT_PATH}/build/nodejs/node-v${nodeExt.version}-\${PLATFORM}-x64}"
NPM_HOME="\${NPM_HOME:-\${NODE_HOME}/lib/node_modules/npm/bin}"

LOCAL_NODE_BIN="${nodeExt.nodeModulesDir}/node_modules/.bin"

node="\${NODE_HOME}/bin/node"
npm="\${NPM_HOME}/npm-cli.js"

if [[ ! -d "\${NODE_HOME}" || ! -d "\${LOCAL_NODE_BIN}" ]]; then
  "${project.rootDir.absolutePath}/gradlew" "${project.path}:${SetupTask.NAME}" -PgenerateNodeWrapper=true
  exec -c "\${SCRIPT_PATH}/\$0" "\${@}"
fi

if [[ -x "\${node}" ]]; then
  ln -fs "\${node}" "\${LOCAL_NODE_BIN}/node"
fi

if [[ -x "\${npm}" ]]; then
  ln -fs "\${npm}" "\${LOCAL_NODE_BIN}/npm"
fi

export PATH="\${LOCAL_NODE_BIN}:\${PATH}"

\${@}
"""
        wrapperFile.executable = true
      }
    }
  }

  private static getPackageJsonVersion(final Project project) {
    def packageSlurper = new JsonSlurper()
    def packageJson = packageSlurper.parse(project.file('package.json'))
    return packageJson.version
  }
}
