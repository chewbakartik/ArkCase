'use strict';

angular.module('admin').controller(
        'Admin.ReportsConfigController',
        [
                '$scope',
                'Admin.ReportsConfigService',
                'LookupService',
                '$q',
                '$sce',

                function($scope, reportsConfigService, LookupService, $q, $sce) {
                    var deferred = $q.defer();

                    $scope.reports = [];
                    $scope.reportsData = {
                        "chooseObject" : $scope.reports,
                        "selectedNotAuthorized" : [],
                        "selectedAuthorized" : []
                    };
                    $scope.reportsMap = [];
                    $scope.userGroupsAll = [];
                    $scope.reportsConfig = null;
                    $scope.reportDesignerUrl = null;

                    $scope.execute = function() {
                        var tempReportsPromise = reportsConfigService.getReports();
                        var tempUserGroupsPromise = reportsConfigService.getUserGroups();
                        var tempReportsUserGroupsPromise = reportsConfigService.getReportsUserGroups();
                        var promiseServerConfig = LookupService.getConfig("acm-reports-server-config");
                        $scope.reportsData.chooseObject = [];
                        $scope.reportsMap = [];
                        $scope.userGroupsAll = [];
                        //wait all promises to resolve
                        $q.all([ tempReportsPromise, tempUserGroupsPromise, tempReportsUserGroupsPromise, promiseServerConfig ]).then(
                                function(payload) {
                                    //get all reports
                                    angular.forEach(payload[0].data, function(report) {
                                        var element = new Object;
                                        element.name = report["title"];
                                        element.key = report["propertyName"];
                                        $scope.reportsData.chooseObject.push(element);
                                        $scope.reportsMap[report["propertyName"]] = report;
                                    });

                                    //get all user groups
                                    angular.forEach(payload[1].data.response.docs, function(userGroup) {
                                        $scope.userGroupsAll[userGroup["object_id_s"]] = userGroup;
                                    });

                                    //get all reports user groups
                                    $scope.reportsUserGroups = payload[2].data;

                                    $scope.reportsConfig = payload[3];

                                    var url = $scope.reportsConfig['PENTAHO_SERVER_URL'] + '/pentaho';
                                    $scope.reportDesignerUrl = $sce.trustAsResourceUrl(url);
                                });
                    }

                    $scope.execute();

                    //callback function when report is selected
                    $scope.onObjSelect = function(selectedObject, authorized, notAuthorized) {

                        //set authorized groups
                        angular.forEach($scope.reportsUserGroups[selectedObject.key], function(element) {
                            //we need to create wrapper, since reportUserGroups doesn't have name which directive expect to have
                            var authObject = {};
                            authObject.key = element;
                            authObject.name = element;
                            authorized.push(authObject);
                        });

                        //set not authorized groups.
                        // Logic: iterate all user groups and if not already exists in selected report user groups, add to the array
                        for ( var key in $scope.userGroupsAll) {
                            if (!$scope.reportsUserGroups || !$scope.reportsUserGroups[selectedObject.key]
                                    || $scope.reportsUserGroups[selectedObject.key].indexOf(key) == -1) {
                                var notAuthObject = {};
                                notAuthObject.key = key;
                                notAuthObject.name = key;
                                notAuthorized.push(notAuthObject);
                            }
                        }
                    };

                    //callback function when groups are moved
                    $scope.onAuthRoleSelected = function(selectedObject, authorized, notAuthorized) {
                        //get authorized user groups for selected report and save all reports user groups
                        $scope.reportsUserGroups[selectedObject.key] = [];
                        angular.forEach(authorized, function(element) {
                            $scope.reportsUserGroups[selectedObject.key].push(element.key);
                        });
                        reportsConfigService.saveReportsUserGroups($scope.reportsUserGroups);

                        //recreate reports array
                        var reports = [];
                        for ( var key in $scope.reportsMap) {
                            if ($scope.reportsUserGroups && $scope.reportsUserGroups[key]) {
                                var injected = $scope.reportsUserGroups[key].length === 0 ? false : true;
                                $scope.reportsMap[key].injected = injected;
                            }

                            reports.push($scope.reportsMap[key]);
                        }
                        reportsConfigService.saveReports(reports).then(function() {
                            deferred.resolve();
                        }, function() {
                            deferred.reject();
                        });

                        return deferred.promise;
                    };

                    $scope.openPentaho = function() {
                        if ($scope.reportDesignerUrl) {
                            window.open($scope.reportDesignerUrl, '_blank');
                        }
                    }

                    $scope.syncReports = function() {
                        reportsConfigService.syncReports().then(function() {
                            $scope.execute();
                            deferred.resolve();
                        }, function() {
                            deferred.reject();
                        });
                    }
                } ]);