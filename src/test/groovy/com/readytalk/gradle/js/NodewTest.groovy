package com.readytalk.gradle.js

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.task.SetupTask
import com.moowork.gradle.node.task.NpmSetupTask
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.process.ExecResult

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

  def "nodew immediately uses correct npm version"() {
    when:
    buildFile << applyPlugin(JsPlugin)
    buildFile << """
node {
  version = '0.12.7'
  npmVersion = '2.14.4'
}
def testTask = project.tasks.create(name: 'nodeExecTest', type: NodeTask)
testTask.executable = 'npm'
testTask.args = ['-v']
"""
    ExecutionResult result = runTasksSuccessfully('nodeExecTest')

    then:
    fileExists('node_modules/.bin/node')
    fileExists('node_modules/.bin/npm')
    result.standardOutput.contains('2.14.4')
  }
}
