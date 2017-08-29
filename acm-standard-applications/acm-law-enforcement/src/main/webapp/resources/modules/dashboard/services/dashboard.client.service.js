'use strict';

/**
 *@ngdoc service
 *@name dashboard:Dashboard.DashboardService
 *
 *@description
 *
 *{@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/modules/dashboard/services/dashboard..client.service.js modules/dashboard/services/dashboard.client.service.js}
 *
 *  The ArkCaseDashboard.provider is used to customize Angular Dashboard Framework provider with ArkCase's own templates. It also expose dashboard provider functions to be used.
 */
angular.module('dashboard').factory('Dashboard.DashboardService', ['$resource', '$timeout'
    , 'Acm.StoreService', 'UtilService', 'Config.LocaleService', 'ArkCaseDashboard'
    , function ($resource, $timeout
        , Store, Util, LocaleService, ArkCaseDashboard
    ) {
        var Service = $resource('', {}, {
            getConfig: {
                method: 'GET',
                url: 'api/v1/plugin/dashboard/get',
                params: {
                    moduleName: "@moduleName"
                },
                data: ''
            },
            queryCasesByQueue: {
                method: 'GET',
                url: 'api/v1/plugin/casefile/number/by/queue',
                isArray: false,
                data: ''
            },
            queryCasesByStatus: {
                method: 'GET',
                url: 'api/v1/plugin/casebystatus/:period',
                isArray: true,
                data: ''
            },
            queryNewComplaints: {
                method: 'GET',
                url: 'api/v1/plugin/search/advancedSearch?q=object_type_s\\:COMPLAINT+' +
                'AND+create_date_tdt\\:[NOW-1MONTH TO NOW]',
                isArray: false,
                data: ''
            },
            queryNewCases: {
                method: 'GET',
                url: 'api/v1/plugin/search/advancedSearch?q=object_type_s\\:CASE_FILE+' +
                'AND+NOT+status_lcs\\:DELETED+AND+create_date_tdt\\:[NOW-1MONTH TO NOW]',
                isArray: false
            },
            queryMyTasks: {
                method: 'GET',
                url: 'api/v1/plugin/search/advancedSearch?q=(assignee_id_lcs\\::userId'
                + '+OR+candidate_group_ss\\::userGroupList)'
                + '+AND+object_type_s\\:TASK+'
                + 'AND+status_lcs\:(ACTIVE OR UNCLAIMED)&start=:startWith&n=:pageSize&s=:sortBy :sortDir',
                isArray: false,
                data: ''
            },
            queryWorkflowReport: {
                method: 'GET',
                url: 'api/v1/plugin/task/businessProcessTasks?start=:startWith&n=:pageSize&s=:sortBy :sortDir',
                isArray: false,
                data: ''
            },
            queryMyComplaints: {
                method: 'GET',
                url: 'api/v1/plugin/search/advancedSearch?q=(assignee_id_lcs\\::userId+' +
                'OR+(assignee_id_lcs\\:""+AND+assignee_group_id_lcs\\::userGroupList))+' +
                'AND+object_type_s\\:COMPLAINT+' +
                'AND+NOT+status_lcs\\:CLOSED&start=:startWith&n=:pageSize&s=:sortBy :sortDir',
                isArray: false,
                data: ''
            },
            queryMyCases: {
                method: 'GET',
                url: 'api/v1/plugin/search/advancedSearch?q=(assignee_id_lcs\\::userId+' +
                'OR+(assignee_id_lcs\\:""+AND+assignee_group_id_lcs\\::userGroupList))+' +
                'AND+object_type_s\\:CASE_FILE+' +
                'AND+NOT+status_lcs\\:CLOSED&start=:startWith&n=:pageSize&s=:sortBy :sortDir',
                isArray: false,
                data: ''
            },
            queryTeamWorkload: {
                method: 'GET',
                url: 'api/v1/plugin/task/getListByDueDate/:due',
                isArray: true,
                data: ''
            },
            getWidgetsPerRoles: {
                method: 'GET',
                url: 'api/latest/plugin/dashboard/widgets/get',
                isArray: true
            },
            saveConfig: {
                method: 'POST',
                url: 'api/v1/plugin/dashboard/set'
            }
        });

        /**
         * @ngdoc method
         * @name localeUseTypical
         * @methodOf dashboard:Object.Dashboard.DashboardService
         *
         * @param {Object} scope, Angular $scope of caller controller
         *
         * @description
         * This function combine the typical usage to set up to use ArkCase Dashboard. It checks and reads any previous
         * cached locale info, and set to correct locale on locale change event.
         */
        Service.localeUseTypical = function(scope) {
            var setLocale = function(iso) {
                $timeout(function () {
                    ArkCaseDashboard.setLocale(iso);
                }, 0);
            };

            var localeData = LocaleService.getLocaleData();
            var locales = Util.goodMapValue(localeData, "locales", LocaleService.DEFAULT_LOCALES);
            var localeCode = Util.goodMapValue(localeData, "code", LocaleService.DEFAULT_CODE);
            var localeIso = Util.goodMapValue(localeData, "iso", LocaleService.DEFAULT_ISO);
            LocaleService.useLocale(localeCode);
            setLocale(localeIso);

            //var localeData = LocaleService.getLocaleData();
            //var locales = Util.goodMapValue(localeData, "locales", LocaleService.DEFAULT_LOCALES);
            scope.$bus.subscribe('$translateChangeSuccess', function (data) {
                //var locale = _.find(locales, {code: data.language});
                var locale = LocaleService.findLocale(data.language);
                var iso = Util.goodMapValue(locale, "iso", LocaleService.DEFAULT_ISO);
                setLocale(iso);
            });

        };


        //make old code compatible. remove fixOldCode_removeLater() after enough time for all users run the new code
        Service.fixOldCode_removeLater = function(moduleName, model) {
            var oldCode = "modules/dashboard/templates/dashboard-title.html" != model.titleTemplateUrl
                && "modules/dashboard/templates/module-dashboard-title.html" != model.titleTemplateUrl;

            if ("DASHBOARD" == moduleName) {
                model.titleTemplateUrl = 'modules/dashboard/templates/dashboard-title.html';
                model.editTemplateUrl = 'modules/dashboard/templates/dashboard-edit.html';
                model.addTemplateUrl = 'modules/dashboard/templates/widget-add.html';
                model.title = "dashboard.title";
            } else {
                model.titleTemplateUrl = 'modules/dashboard/templates/module-dashboard-title.html';
            }

            if ("Dashboard" == model.title) {
                model.title = "dashboard.title";
            }

            if (model.rows) {
                model.rows.forEach(function(row){
                    var columns = row.columns;
                    if (columns) {
                        columns.forEach(function(column){
                            var widgets = column.widgets;
                            if (widgets) {
                                widgets.forEach(function(widget){
                                    if ("modules/dashboard/templates/widget-title.html" != widget.titleTemplateUrl) {
                                        oldCode = true;
                                    }
                                    widget.titleTemplateUrl = "modules/dashboard/templates/widget-title.html";
                                });
                            }
                        });
                    }
                });
            }

            if (oldCode) {
                $timeout(function () {
                    Service.saveConfig({
                        dashboardConfig: angular.toJson(model),
                        module: moduleName
                    });
                }, 0);
            }
        };
        //TODO: remove fixOldCode_removeLater() and its usage in each module

        return Service;
    }
]);