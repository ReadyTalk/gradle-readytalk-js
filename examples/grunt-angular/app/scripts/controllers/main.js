'use strict';

/**
 * @ngdoc function
 * @name exampleProjectApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the exampleProjectApp
 */
angular.module('exampleProjectApp')
  .controller('MainCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
