package com.readytalk.gradle.js

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.task.NpmInstallTask
import com.moowork.gradle.node.task.NpmSetupTask
import com.moowork.gradle.node.task.SetupTask
import com.moowork.gradle.node.util.PlatformHelper
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.ExtraPropertiesExtension

class JsPlugin implements Plugin<Project> {
  //TODO: Option for "latest LTS"
  static final String DEFAULT_NODE_VERSION = '4.2.4'
  private Project project

  @Override
  void apply(final Project project) {
    this.project = project

    project.plugins.apply(BasePlugin)
    project.plugins.apply(NodePlugin)

    if(!project.hasProperty('generateNodeWrapper')) {
      project.extensions.extraProperties.set('generateNodeWrapper', true)
    }

    nodeExt.with {
      version = DEFAULT_NODE_VERSION
      npmVersion = '' //By default, just use whatever comes bundled with node
      download = true
      workDir = project.file("${project.buildDir}/nodejs")
    }

    injectNodewSetup()

    //TODO: We ought to read the package.json for references to supported node versions
    //      and default to those if not specified in the gradle extension
    if (project.version == 'unspecified' && project.file('package.json').exists()) {
      project.version = getPackageJsonVersion(project.file('package.json'))
    }

    injectBinPath()
  }

  private NodeExtension getNodeExt() {
    project.extensions.findByType(NodeExtension)
  }

  String getNodeHome() {
    "${nodeExt.workDir}/node-v${nodeExt.version}-${new PlatformHelper().osName}-x64"
  }

  def injectBinPath() {
    project.tasks.withType(NodeTask).all { NodeTask task ->
      //Inject node_modules/.bin directory into path
      //TODO: This really belongs upstream, since it hijacks the execOverrides block
      //      I can't preserve the closure because the upstream plugin inexplicably marks it write-only
      task.setExecOverrides {
        it.environment.PATH =
                "${nodeHome}/bin:${nodeExt.nodeModulesDir}/node_modules/.bin:${System.env.PATH}"
      }

      //Inject syntactic sugar for calling node-based tools
      def ext = task.extensions.findByType(ExtraPropertiesExtension)
      ext.set('executable', '')

      project.afterEvaluate {
        if(ext.get('executable') && ext.get('executable') != 'npm') {
          task.dependsOn project.tasks.findByName(NpmInstallTask.NAME)
        }
      }

      task.doFirst {
        if(ext.get('executable')) {
          def execName = ext.get('executable')
          def bin = project.file(
                  "${nodeExt.nodeModulesDir.absolutePath}/node_modules/.bin/${execName}"
          )
          if(bin?.exists()) {
            task.script = bin
          } else {
            def hinter = [
                    'grunt': 'grunt-cli'
            ].withDefault {it}
            //TODO: We could probably query npm or check package.json too
            throw new IllegalStateException("Can't find executable ${bin?.absolutePath} for task ${task.path}\n" +
                    "You may need to add ${hinter.get(execName)} to your package.json file, e.g.:\n" +
                    "./nodew npm install --save-dev ${hinter.get(execName)}")
          }
        }
      }
    }

  }

  private def relPath(File path) {
    String relative = project.projectDir.toPath().relativize(path.toPath()).toString()
    relative == '' ? '${BASE_PATH}' : '${BASE_PATH}/' + relative
  }

  def void injectNodewSetup() {
    project.tasks.withType(SetupTask).all { SetupTask task ->
      task.inputs.properties([
              nodeVersion: nodeExt.version,
              npmVersion: nodeExt.npmVersion
      ])

      def ext = task.extensions.extraProperties
      task.outputs.file(project.file('nodew'))
      task.outputs.upToDateWhen {
        def nodewFile = project.file('nodew')
        ext.set('nodewText', generateNodewText())
        ext.get('nodewText') == (nodewFile?.exists() ? nodewFile.text : '') &&
                project.file("${nodeHome}/bin/node").exists() &&
                ( nodeExt.npmVersion == '' || //Either using default npm version or check that explicit version is correct
                        "${nodeHome}/bin/node ${nodeHome}/lib/node_modules/npm/cli.js -v".execute().text == nodeExt.npmVersion )
      }

      task.doLast {
        def wrapperFile = project.file('nodew')
        wrapperFile.text = ext.get('nodewText')
        wrapperFile.executable = true

        //Faster and cleaner alternative to the upstream NpmSetupTask
        if(nodeExt.npmVersion && nodeExt.npmVersion !=
                getPackageJsonVersion(project.file("${nodeHome}/lib/node_modules/npm/package.json"))) {
          project.exec {
            workingDir = "${nodeHome}/lib"
            commandLine = [
                    "${nodeHome}/bin/node",
                    "node_modules/npm/cli.js",
                    "install",
                    "npm@${nodeExt.npmVersion}"
            ]
          }
        }

        //Run the nodew script with a dummy command to generate symlinks
        def initialize = "${wrapperFile.absolutePath} true".execute().text
        if(!initialize.empty) {
          project.logger.warn("WARNING: nodew script check failed, and may not work correctly!")
        }

      }
    }

    //Disable in favor of the injected setup above
    project.tasks.withType(NpmSetupTask) { NpmSetupTask task ->
      task.dependsOn project.tasks.findByName(SetupTask.NAME)
      task.onlyIf {
        logger.info "${NpmSetupTask.NAME} skipped: npm setup already handled by ${SetupTask.NAME}"
        false
      }
    }
  }

  String generateNodewText() {
"""#!/usr/bin/env bash

#NOTE: This file is autogenerated by the com.readytalk.js gradle plugin

#http://stackoverflow.com/a/2684300
if [[ "\${BASH_SOURCE[0]}" == "\${0}" ]]; then
  ORIGIN="\${BASH_SOURCE[0]}"
  PLATFORM="\$(uname -s | tr '[:upper:]' '[:lower:]')"
  BASE_PATH="\$(dirname \$0)"

  NODE_HOME="\${NODE_HOME:-${relPath(project.buildDir)}/nodejs/node-v${nodeExt.version}-\${PLATFORM}-x64}"

  LOCAL_NODE_BIN="${relPath(nodeExt.nodeModulesDir)}/node_modules/.bin"

  mkdir -p "\${LOCAL_NODE_BIN}"

  if [[ ! -d "\${NODE_HOME}" ]]; then
    "${relPath(project.rootDir)}/gradlew" "${project.tasks.findByName(SetupTask.NAME).path}" -PgenerateNodeWrapper=true
    exec -c "\${0}" "\${@}"
  fi

  #Need full path for symlink to work properly
  if readlink --canonicalize &> /dev/null; then
    NODE_HOME_FULL="\$(readlink --canonicalize "\${NODE_HOME}")"
  else
    NODE_HOME_FULL="\$(cd "\${NODE_HOME}"; pwd)"
  fi

  NPM_HOME="\${NPM_HOME:-\${NODE_HOME_FULL}/lib/node_modules/npm/bin}"

  node="\${NODE_HOME_FULL}/bin/node"
  npm="\${NPM_HOME}/npm-cli.js"

  if [[ -x "\${node}" ]]; then
    ln -fs "\${node}" "\${LOCAL_NODE_BIN}/node"
  fi

  if [[ -x "\${npm}" ]]; then
    ln -fs "\${npm}" "\${LOCAL_NODE_BIN}/npm"
  fi

  FULL_LOCAL_BIN="\$(cd "\${LOCAL_NODE_BIN}"; pwd)"

  export PATH="\${FULL_LOCAL_BIN}:\$(echo -n "\$PATH" | sed -E "s|\${FULL_LOCAL_BIN}:?||")"

  if [[ \$# -eq 0 ]]; then
    echo "Run single command: ./nodew COMMAND"
    echo "Setup local shell (bash-only): source nodew"
    echo "\nnodew is a wrapper around the project-local node/npm installation"
    echo "e.g. '. /nodew node -v' will print the project-local node version"
  else
    "\${@}"
  fi
else
  # Just in case
  "\$(pwd)/\${BASH_SOURCE[0]}" &>/dev/null
  export PATH="\$("\$(pwd)/\${BASH_SOURCE[0]}" sh -c 'echo \$PATH')"
fi
"""
  }

  private static getPackageJsonVersion(File packageJson) {
    def packageSlurper = new JsonSlurper()
    def json = packageSlurper.parse(packageJson)
    return json.version
  }
}
