package com.readytalk.gradle.js.tasks

class AsarTask extends NodeTask {
  @InputDirectory
  def File inputDir

  @OutputFile
  def File outputFile

  @TaskAction
  void build() {
    outputFile.parentFile.mkdirs();
    script = file('node_modules/asar/bin/asar')
    args = inputDir.absolutePath, outputFile.absolutePath
    super.exec()
  }
}
