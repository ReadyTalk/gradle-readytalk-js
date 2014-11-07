package com.readytalk.gradle.js

import com.moowork.gradle.grunt.GruntPlugin
import com.moowork.gradle.grunt.GruntTask
import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.task.NpmInstallTask
import spock.lang.Specification
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.testfixtures.ProjectBuilder

class JsPluginTest extends Specification {
  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  def "Applies node and grunt plugins"() {
    expect: "a project without plugins applied"
      ! project.plugins.with {
        hasPlugin(BasePlugin)
        hasPlugin(GruntPlugin)
        hasPlugin(NodePlugin)
      }

    when: "js plugin is applied"
      project.apply plugin: 'com.readytalk.js'

    then: "project has other js plugins applied"
      project.plugins.with {
        hasPlugin(BasePlugin)
        hasPlugin(GruntPlugin)
        hasPlugin(NodePlugin)
      }
  }

  def "Sets up npmInstall dependency for GruntTasks"() {
    when: "js plugin is applied"
      project.apply plugin: 'com.readytalk.js'

    and: "we add a grunt task"
      project.task('gruntBuild', type: GruntTask)

    then: "the grunt task depends on npmInstall"
      project.tasks.withType(GruntTask)*.getDependsOn().flatten().contains(NpmInstallTask.NAME)
  }

  def "Sets project version from package.json"() {
    given: "a project with a package.json file."
      // Create a package.json file
      def packageJsonVersion = "1.0.0"
      project.file('package.json') << """\
{
  "name": "test",
  "version": "${packageJsonVersion}",
}
"""
    expect: "version to be unspecified"
      project.version == 'unspecified'

    when: "plugin is applied"
      project.apply plugin: 'com.readytalk.js'

    then: "the gradle project version matches the version from package.json"
      project.version == packageJsonVersion
  }

}
