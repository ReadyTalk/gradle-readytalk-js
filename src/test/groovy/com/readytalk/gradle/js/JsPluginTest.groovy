package com.readytalk.gradle.js

import com.moowork.gradle.node.NodePlugin
import nebula.test.PluginProjectSpec
import nebula.test.ProjectSpec
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class JsPluginTest extends PluginProjectSpec {
  final String pluginName = 'com.readytalk.js'
  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  @Ignore("Deprecated")
  def "Applies node and grunt/gulp plugins"() {
    expect: "a project without plugins applied"
      ! project.plugins.with {
        hasPlugin(BasePlugin)
        hasPlugin(NodePlugin)
      }

    when: "js plugin is applied"
      project.apply plugin: 'com.readytalk.js'

    then: "project has other js plugins applied"
      project.plugins.with {
        hasPlugin(BasePlugin)
        hasPlugin(NodePlugin)
      }
  }

  @Ignore("Deprecated")
  @Unroll("Sets up dependencies for #tool tasks")
  def "Sets up dependencies"() {
    when: "js plugin is applied"
      project.apply plugin: 'com.readytalk.js'

    and: "we add a #tool task"
      project.task("${tool}Build", type: toolClass)

    then: "the #tool task depends on npmInstall and install #tool"
      def dependencies = project.tasks.withType(toolClass)*.getDependsOn().flatten()
      dependencies.contains('npmInstall')
      dependencies.contains("install${tool.capitalize()}".toString())

    where:
    tool << ['grunt', 'gulp']
    toolClass << [GruntTask, GulpTask]
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
