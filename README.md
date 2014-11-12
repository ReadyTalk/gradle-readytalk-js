gradle-readytalk-js
===================

[![Build Status](https://travis-ci.org/ReadyTalk/gradle-readytalk-js.svg?branch=master)] (https://travis-ci.org/ReadyTalk/gradle-readytalk-js)
[![Coverage Status](https://img.shields.io/coveralls/ReadyTalk/gradle-readytalk-js.svg)](https://coveralls.io/r/ReadyTalk/gradle-readytalk-js?branch=master)
[![Project Status](http://stillmaintained.com/ReadyTalk/gradle-readytalk-js.png)](http://stillmaintained.com/ReadyTalk/gradle-readytalk-js)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://readytalk.mit-license.org)
[![Download](https://api.bintray.com/packages/readytalk/plugins/gradle-readytalk-js/images/download.svg)] (https://bintray.com/readytalk/plugins/gradle-readytalk-js/_latestVersion)

Conventions for ReadyTalk JavaScript projects using Node and Grunt.

## Goals ##
The main goal of this project is to establish and maintain common conventions
and functionality for ReadyTalk JavaScript projects. The gradle-readytalk-js
plugin will wrap existing node.js-related and other ReadyTalk Gradle plugins to
provide:

- Management of the version of the `node` and `npm` environment builds run in.
- A single entry point for the CI build/test/publish process.
- Conventions around the layout of ReadyTalk JavaScript projects.

Most of the functionality this plugin applies is actually provided by two other
plugins:

- Gradle Node Plugin
- Gradle Grunt Plugin

The long-term vision of this project is to merge all or most conventions and
functionality contained here upstream and reorganize those projects into base /
convention plugins in much the same way the out-of-the-box java-base and java
Gradle plugins are set up.

## Usage ##
Releases of this plugin are hosted at BinTray (http://bintray.com) and is part
of jcenter repository. Development SNAPSHOTs for every commit and successful
build are published to the OJO repostory.

Setup the plugin like this:

```groovy
plugins {
  id 'com.readytalk.js' version '0.1.0'
}
```

Or using the old (pre 2.1) way:

```groovy
buildscript {
	repositories {
		jcenter()
    // If you want to use a SNAPSHOT build, add the OJO repository:
    maven {
      name 'JFrog OSS snapshot repo'
      url  'https://oss.jfrog.org/oss-snapshot-local/'
    }
	}
	dependencies {
		classpath 'com.readytalk.gradle:gradle-readytalk-js:0.1.0'
  }
}
```

Include the plugin in your build.gradle file like this:

```groovy
apply plugin: 'com.readytalk.js'
```

## Philosophy on Gradle / Grunt Interaction ##
It is strongly suggested you minimize the amount of interaction between Gradle
and the other tools. For example, don't use Gradle to manage the task
dependencies of the Grunt build. Instead, try to have only one or two grunt
tasks called from the Gradle build and have the grunt build take care of itself.

Future versions of this plugin will establish some conventional tasks and task
dependencies once some best-practices have been sussed out. The probable
direction will be to have a `gruntBuild` task and a `gruntIntegTest` task
configured properly with inputs and outputs for up-to-date checks.

The `gruntBuild` task will be responsible for creating the full application dist
and running all unit tests, static analysis, and any other quality checks which:

 - Execute quickly (the entire `gruntBuild` task should take less than 5 minutes)
 - Do not require the application to be deployed.
 - Do not rely on any external resources.

The `gruntIntegTest` task will be responsible for running any sorts of test
which  require application deployment, external resources, or longer-running
tests. The `gruntIntegTest` task should just run the tests and allow other
parts of the Gradle build to bring up VMs or Docker containers or other testing
infrastructure as necessary and clean them up once the `gruntIntegTest` task
completes execution.
