package com.readytalk.gradle.js

import groovy.json.JsonBuilder
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

class NpmCacheIntegTest extends IntegrationSpec {
   def "caches node_modules tarball"() {
     setup:
     File cache = directory('npmcache')
     File packagejson = createFile('package.json')

     buildFile << """
apply plugin: 'com.readytalk.npmcache'
tasks.npmInstall.ext.cacheDir = file('${cache.absolutePath}')
"""

     def json = new JsonBuilder()
     json {
       name 'npmcache-test'
       version '0.0.0'
       devDependencies {
         //TODO: use a dummy package instead of a real one
         'require-dir' '>=0.0.0'
       }
     }

     packagejson << json.toString()

     String tarball = "npmcache/${JsPlugin.DEFAULT_NODE_VERSION}-${JsPlugin.DEFAULT_NPM_VERSION}-${NpmCachePlugin.sha1(packagejson)}.tar.gz"
     String wrappedTaskPattern = "Executing task ':${NpmCachePlugin.NPM_INSTALL_WRAPPER}'"

     when:
     ExecutionResult result = runTasksSuccessfully('npmInstall')
     ExecutionResult rebuild = runTasksSuccessfully('npmInstall')
     ExecutionResult retry = runTasksSuccessfully('clean', 'npmInstall')

     then:
     result.wasExecuted('npmInstall')
     result.standardOutput.contains(wrappedTaskPattern)

     fileExists('node_modules/require-dir')
     fileExists(tarball)

     rebuild.wasUpToDate('npmInstall')
     !(rebuild.standardOutput.contains(wrappedTaskPattern))

     retry.wasExecuted('npmInstall')
     !(retry.standardOutput.contains(wrappedTaskPattern))
   }
}
