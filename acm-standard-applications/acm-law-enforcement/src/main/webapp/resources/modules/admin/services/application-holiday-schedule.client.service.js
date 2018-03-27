'use strict';

angular.module('admin').factory('Admin.HolidayScheduleService', [ '$http', function($http) {

    return ({
        getHolidaySchedule : getHolidaySchedule,
        saveHolidaySchedule : saveHolidaySchedule
    });

    function getHolidaySchedule() {
        return $http({
            method : 'GET',
            url : 'api/latest/service/holidayConfig'
        });
    }

    function saveHolidaySchedule(holidayConfig) {
        return $http({
            method : 'POST',
            url : 'api/latest/service/holidayConfig',
            data : holidayConfig,
            headers : {
                "Content-Type" : "application/json"
            }
        });
    }
} ]);