gradle-readytalk-js
===================

[![Build Status](http://goo.gl/L6U01H)](http://goo.gl/k3KEE2)
[![Coverage Status](http://goo.gl/7uC90R)](http://goo.gl/Q8wikd)
[![License](http://goo.gl/pPDj6N)](http://goo.gl/93tPwk)
[![Download](http://goo.gl/OFVFVx)](http://goo.gl/wOAuo0)

Conventions for ReadyTalk JavaScript projects using [node.js][]

[node.js]: http://nodejs.org/

## Goals ##
The main goal of this project is to establish and maintain common conventions
and functionality for ReadyTalk JavaScript projects. The [gradle-readytalk-js][]
plugin will wrap existing node.js-related plugins to provide:

- Management of the version of the `node` and `npm` environment builds run in.
- A single entry point for the CI build/test/publish process.
- Conventions around the layout of ReadyTalk JavaScript projects.

[gradle-readytalk-js]: http://oss.readytalk.com/gradle-readytalk-js/

Most of the functionality this plugin applies is actually provided by the
`com.moowork.node` plugin:

- [Gradle Node Plugin](https://github.com/srs/gradle-node-plugin)

The long-term vision of this project is to merge all or most conventions and
functionality contained here upstream and reorganize those projects into base /
convention plugins in much the same way the out-of-the-box `java-base` and
`java` Gradle plugins are set up.

It's also expected that some of the functionality here might get migrated to the
upstream plugin if it's universal enough.

## Usage ##
Releases of this plugin are hosted on [Gradle's Plugin Portal][]. See the
[ReadyTalk JS Plugin][] page for more information on how to apply the plugin.

[Gradle's Plugin Portal]: https://plugins.gradle.org
[ReadyTalk JS Plugin]: https://plugins.gradle.org/plugin/com.readytalk.js

```groovy
plugins {
  id 'com.readytalk.js' version '1.2.0'
}
```

### Development SNAPSHOTs ###
Development `SNAPSHOT`s for every commit and successful build on the master
branch are published to the [OJO][] repostory. To use a `SNAPSHOT` version,
you will need to set up the OJO repository and use the 'classic' method to
apply the plugin.

```groovy
buildscript {
  repositories {
    maven {
      name 'JFrog OSS snapshot repo'
      url  'https://oss.jfrog.org/oss-snapshot-local/'
    }
  }
  dependencies {
    classpath "com.readytalk.gradle:gradle-readytalk-js:1.2.0-SNAPSHOT"
  }
}

apply plugin: "com.readytalk.js"
```

[OJO]: https://oss.jfrog.org

## Calling into node-based tools
This plugin extends the NodeTask type from `com.moowork.node` to simplify
running CLI-based node tools like grunt. For example, to call into grunt:

```groovy
//Runs the 'ci' task in grunt
task gruntCi(type: NodeTask) {
  executable = 'grunt'
  args = ['ci']
}
```

It is expected that any node-based tool is included in your `package.json`,
e.g. to use grunt you'll need to include the `grunt-cli` package as a
devDependency, just like you would if you were using node directly.

Don't try to manually install tools like grunt via gradle - it's much simpler
to just include it as a normal devDependency and let `npm install` handle it.

## Philosophy on Gradle / Grunt Interaction ##
It is strongly suggested you minimize the amount of interaction between Gradle
and the other tools. For example, don't use Gradle to manage the task
dependencies of the Grunt build. Instead, try to have only one or two Grunt
tasks called from the Gradle build and have the Grunt build take care of itself.

Future versions of this plugin will establish some conventional tasks and task
dependencies once some best-practices have been sussed out. The probable
direction will be to have a `gruntBuild` task and a `gruntIntegTest` task
configured properly with inputs and outputs for up-to-date checks.

The `gruntBuild` task will be responsible for creating a fully-releasable
production application distribution and running all unit tests, static analysis,
and any other quality checks which:

 - Execute quickly (`gruntBuild` should take < 5 minutes)
 - Do not require the application to be deployed.
 - Do not rely on any external resources.

The `gruntIntegTest` task will be responsible for running any sorts of test
which  require application deployment, external resources, or longer-running
tests. The `gruntIntegTest` task should just run the tests and allow other
parts of the Gradle build to bring up VMs or Docker containers or other testing
infrastructure as necessary and clean them up once the `gruntIntegTest` task
completes execution.

## node wrapper script

By default, the plugin also generates an executable nodew script as part of
the nodeSetup task. This script will run commands in the context of the local
node/npm install, and will regenerate them if needed. This script is intended
for local developer convenience only, and should not be checked in as it relies
on absolute paths. Set 'generateNodeWrapper=false' in gradle.properties to disable

For developer convenience, the nodew script can also be sourced from bash shells
to import the PATH settings into the local session, similar to virtualenv for
python.

## npm cache plugin (EXPERIMENTAL)

This project includes an optional `com.readytalk.npmcache` plugin as well, which
works somewhat similar to the npm-cache utility. As long as the package.json doesn't
change, it will attempt to resolve node_modules from a local cached tarball instead,
which is created the first time npm install completes for that package.json.

```groovy
apply plugin: 'com.readytalk.npmcache'
```
