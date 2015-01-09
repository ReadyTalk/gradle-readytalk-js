package com.readytalk.gradle.js.tasks

import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class AsarTask extends NodeTask {
  @InputDirectory
  def File inputDir

  @OutputFile
  def File outputFile

  @TaskAction
  void build() {
    outputFile.parentFile.mkdirs();
    script = file('node_modules/asar/bin/asar')
    args = [ inputDir.absolutePath, outputFile.absolutePath ]
    super.exec()
  }
}
