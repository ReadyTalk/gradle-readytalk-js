gradle-readytalk-js
===================

[![Build Status](http://goo.gl/L6U01H)](http://goo.gl/k3KEE2)
[![Coverage Status](http://goo.gl/7uC90R)](http://goo.gl/Q8wikd)
[![License](http://goo.gl/pPDj6N)](http://goo.gl/93tPwk)
[![Download](http://goo.gl/OFVFVx)](http://goo.gl/wOAuo0)

Conventions for ReadyTalk JavaScript projects using [node.js][] and [Grunt][].

[node.js]: http://nodejs.org/
[Grunt]: http://gruntjs.com/

## Goals ##
The main goal of this project is to establish and maintain common conventions
and functionality for ReadyTalk JavaScript projects. The [gradle-readytalk-js][]
plugin will wrap existing node.js-related plugins to provide:

- Management of the version of the `node` and `npm` environment builds run in.
- A single entry point for the CI build/test/publish process.
- Conventions around the layout of ReadyTalk JavaScript projects.

[gradle-readytalk-js]: http://oss.readytalk.com/gradle-readytalk-js/

Most of the functionality this plugin applies is actually provided by two other
plugins:

- [Gradle Node Plugin](https://github.com/srs/gradle-node-plugin)
- [Gradle Grunt Plugin](https://github.com/srs/gradle-grunt-plugin)

The long-term vision of this project is to merge all or most conventions and
functionality contained here upstream and reorganize those projects into base /
convention plugins in much the same way the out-of-the-box `java-base` and
`java` Gradle plugins are set up.

## Usage ##
Releases of this plugin are hosted on [Gradle's Plugin Portal][]. See the
[ReadyTalk JS Plugin][] page for more information on how to apply the plugin.

[Gradle's Plugin Portal]: https://plugins.gradle.org
[ReadyTalk JS Plugin]: https://plugins.gradle.org/plugin/com.readytalk.js

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
    classpath "com.readytalk.gradle:gradle-readytalk-js:0.3.2-SNAPSHOT"
  }
}

apply plugin: "com.readytalk.js"
```

[OJO]: https://oss.jfrog.org

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
