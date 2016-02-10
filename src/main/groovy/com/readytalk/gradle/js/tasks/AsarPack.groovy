package com.readytalk.gradle.js.tasks

import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile

class AsarPack extends NodeTask {
  @InputDirectory
  def File inputDir

  @OutputFile
  def File outputFile

  @Override
  void exec() {
    outputFile.parentFile.mkdirs();

    setScript(project.file('node_modules/asar/bin/asar'))
    setArgs(['pack', inputDir.absolutePath, outputFile.absolutePath])

    super.exec()
  }
}
