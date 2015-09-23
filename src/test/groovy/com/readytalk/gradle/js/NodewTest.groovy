package com.readytalk.gradle.js

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.task.SetupTask
import com.moowork.gradle.node.task.NpmSetupTask
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.gradle.BuildResult
import org.gradle.api.Project

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
}
