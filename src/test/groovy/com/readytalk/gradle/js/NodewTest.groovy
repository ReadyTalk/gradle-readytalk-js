package com.readytalk.gradle.js

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.task.SetupTask
import com.moowork.gradle.node.task.NpmSetupTask
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.process.ExecResult
import spock.lang.Ignore

//TODO: Rename
class NodewTest extends IntegrationSpec {
  def "generates nodew wrapper defaulting to calling node"() {
    setup:
    Closure<String> nodew = { String... commands ->
      def out = "${projectDir}/nodew ${commands.join(' ')}".execute()
      println out.err.readLines().join('\n')
      return out.text
    }

    when:
    buildFile << applyPlugin(JsPlugin)
    buildFile << """
project.tasks.${SetupTask.NAME}.doLast {
  logger.quiet "NODE_VERSION=v\${node.version}"
}"""
    ExecutionResult result = runTasksSuccessfully(SetupTask.NAME)

    then:
    fileExists('nodew')
    result.standardOutput.contains(nodew('node', '-v'))
  }

  def "nodew can be sourced by bash"() {
    when:
    buildFile << applyPlugin(JsPlugin)
    buildFile << """
task bashTest(type: Exec) {
  dependsOn '${SetupTask.NAME}'
  commandLine = [
    'bash', '-c',
    'source nodew; node -v'
  ]
}
"""

    then:
    ExecutionResult result = runTasksSuccessfully('bashTest')
    result.standardOutput.contains(JsPlugin.DEFAULT_NODE_VERSION)
  }

  def "nodew immediately uses correct npm version"() {
    when:
    buildFile << applyPlugin(JsPlugin)
    buildFile << """
node {
  version = '4.2.4'
  npmVersion = '3.5.4'
}
def testTask = project.tasks.create(name: 'nodeExecTest', type: NodeTask)
testTask.executable = 'npm'
testTask.args = ['-v']
"""
    ExecutionResult result = runTasksSuccessfully('nodeExecTest')

    then:
    fileExists('node_modules/.bin/node')
    fileExists('node_modules/.bin/npm')
    result.standardOutput.contains('3.5.4')
  }
}
