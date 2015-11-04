package com.readytalk.gradle.js

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.security.MessageDigest

class NpmCachePlugin implements Plugin<Project> {
  Project project

  void apply(Project project) {
    this.project = project
    project.plugins.apply(JsPlugin)

    project.with {
      tasks.replace('installGrunt').configure {}

      tasks.replace('npmInstall').configure {
        inputs.file 'package.json'
        inputs.property 'NodeVersion', project.node.version

        def hashFile = file("${buildDir}/.package_json_hash")
        outputs.file hashFile

        doLast {
          String hash = project.node.version + " - " + project.node.npmVersion + " - " + sha1(file('package.json'))
          File f = new File("${System.properties['user.home']}/.node_modules_cache/${hash}.tar.gz")
          if (f.exists()) {
            exec {
              executable 'rm'
              args '-rf', 'node_modules'
            }

            file('node_modules').mkdir()

            exec {
              executable 'tar'
              args 'xf', f.path
              workingDir 'node_modules'
            }
          } else {

            // We want to always wipe and reinstall when there's a new package.json.
            // That way, we catch problems where a package was removed that something still needed.
            // Otherwise, the local build might succeed because it still has the removed package in node_modules.
            exec {
              executable 'rm'
              args '-rf', 'node_modules'
            }

            def npm = tasks.create(name: 'real_npmInstall', type: NpmTask)

            npm.args = ['install']
            npm.execute()

            f.parentFile.mkdirs()

            exec {
              executable 'tar'
              args 'czf', f.path, '.'
              workingDir 'node_modules'
            }
          }

          hashFile.parentFile.mkdirs()
          hashFile.text = hash
        }
      }
    }
  }

  private String sha1(File f) {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    f.eachByte 4096, {bytes, size ->
      md.update(bytes, 0, size);
    }
    return md.digest().collect {String.format "%02x", it}.join();
  }
}
