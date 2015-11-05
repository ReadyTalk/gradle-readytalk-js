* 1.1.1
    - Added integration test for npmcache plugin
    - Ensure npmcache sets up node before running npm install

* 1.1.0
    - Added experimental com.readytalk.npmcache plugin
      stores node_modules as tarball based on package.json

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
