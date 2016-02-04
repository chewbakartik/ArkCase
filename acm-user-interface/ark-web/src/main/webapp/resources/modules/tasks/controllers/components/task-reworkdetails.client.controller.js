'use strict';

angular.module('tasks').controller('Tasks.ReworkDetailsController', ['$scope', '$stateParams', '$translate'
    , 'UtilService', 'ConfigService', 'Task.InfoService', 'MessageService', 'Helper.ObjectBrowserService'
    , function ($scope, $stateParams, $translate
        , Util, ConfigService, TaskInfoService, MessageService, HelperObjectBrowserService) {

        new HelperObjectBrowserService.Component({
            moduleId: "tasks"
            , componentId: "reworkdetails"
            , scope: $scope
            , stateParams: $stateParams
            , retrieveObjectInfo: TaskInfoService.getTaskInfo
            , validateObjectInfo: TaskInfoService.validateTaskInfo
            , onObjectInfoRetrieved: function (taskInfo) {
                $scope.taskInfo = taskInfo;
            }
        });


        $scope.options = {
            focus: true
            //,height: 120
        };

        //$scope.editDetails = function() {
        //    $scope.editor.summernote({focus: true});
        //}
        $scope.saveDetails = function () {
            //$scope.editor.destroy();
            var taskInfo = Util.omitNg($scope.taskInfo);
            TaskInfoService.saveTaskInfo(taskInfo).then(
                function (taskInfo) {
                    $scope.$emit("report-object-updated", taskInfo);
                    MessageService.info($translate.instant("tasks.comp.details.informSaved"));
                    return taskInfo;
                }
            );
        };
    }
]);