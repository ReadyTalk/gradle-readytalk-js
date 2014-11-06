gradle-readytalk-js
===================

[![Build Status](https://travis-ci.org/ReadyTalk/gradle-readytalk-js.svg?branch=master)](https://travis-ci.org/ReadyTalk/gradle-readytalk-js)

Conventions for ReadyTalk JavaScript projects using Node, Grunt, and Angular.

## Goals ##
The main goal of this project is to establish and maintain common conventions
and functionality for ReadyTalk JavaScript projects. The gradle-readytalk-js
plugin will wrap existing node.js-related and other ReadyTalk Gradle plugins
to provide:

- Management of the version of node and npm the build runs in.
- A single entry point for the CI build/test/publish process.
- Functionality and conventions for application artifact publication.
- Ability to publish node.js libraries via npm.
