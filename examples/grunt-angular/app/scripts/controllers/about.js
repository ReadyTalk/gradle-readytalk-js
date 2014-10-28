'use strict';

/**
 * @ngdoc function
 * @name exampleProjectApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the exampleProjectApp
 */
angular.module('exampleProjectApp')
  .controller('AboutCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
