'use strict';

angular.module('dashboard.relOrganizations', ['adf.provider'])
    .config(function (dashboardProvider) {
        dashboardProvider
            .widget('relOrganizations', {
                    title: 'dashboard.widgets.relOrganizations.title',
                    description: 'dashboard.widgets.relOrganizations.description',
                    controller: 'Dashboard.RelatedOrganizationsController',
                    reload: true,
                    templateUrl: 'modules/dashboard/views/components/related-organizations-widget.client.view.html',
                    commonName: 'relOrganizations'
                }
            );
    })
    .controller('Dashboard.RelatedOrganizationsController', ['$scope', '$stateParams', '$translate',
        'Organization.InfoService', 'Helper.ObjectBrowserService', 'Helper.UiGridService',
        function ($scope, $stateParams, $translate,
                  OrganizationInfoService, HelperObjectBrowserService, HelperUiGridService) {

            var modules = [
                {
                    name: "ORGANIZATION",
                    configName: "organizations",
                    getInfo: OrganizationInfoService.getOrganizationInfo,
                    validateInfo: OrganizationInfoService.validateOrganizationInfo
                }
            ];

            var module = _.find(modules, function (module) {
                return module.name == $stateParams.type;
            });

            $scope.gridOptions = {
                enableColumnResizing: true,
                columnDefs: []
            };

            var gridHelper = new HelperUiGridService.Grid({scope: $scope});

            new HelperObjectBrowserService.Component({
                scope: $scope
                , stateParams: $stateParams
                , moduleId: module.configName
                , componentId: "main"
                , retrieveObjectInfo: module.getInfo
                , validateObjectInfo: module.validateInfo
                , onObjectInfoRetrieved: function (objectInfo) {
                    onObjectInfoRetrieved(objectInfo);
                }
                , onConfigRetrieved: function (componentConfig) {
                    onConfigRetrieved(componentConfig);
                }
            });

            var onObjectInfoRetrieved = function (objectInfo) {
                //FIX this when relations between organizations are done on backend
                //setWidgetsGridData(objectInfo.relOrganizations);
            };

            var onConfigRetrieved = function (componentConfig) {
                var widgetInfo = _.find(componentConfig.widgets, function (widget) {
                    return widget.id === "relOrganizations";
                });
                $scope.gridOptions.columnDefs = widgetInfo ? widgetInfo.columnDefs : [];
            };
        }
    ]);