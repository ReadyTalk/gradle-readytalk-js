* 1.0.0
    - Removed dependency on upstream Gulp/Grunt plugins, now only uses main `com.moowork.node` plugin
    - Inject `node_modules/.bin` directory into PATH for NodeTask tasks
    - Inject optional `executable` property for NodeTasks
      automatically sets script to name of executable in `node_modules/.bin`
    - Update default node version to `0.12.7`
    - Updated README to reflect current suggest practices

* 0.4.0
    - Update upstream moowork node plugins to 0.11
    - Update nodew script to use relative paths
      This allows it to be safely checked into project repositories
