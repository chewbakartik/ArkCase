'use strict';

/**
 * @ngdoc service
 * @name services:Ecm.RecordService
 *
 * @description
 *
 * {@link https://github.com/Armedia/ACM3/blob/develop/acm-user-interface/ark-web/src/main/webapp/resources/services/ecm/ecm-record.client.service.js services/ecm/ecm-record.client.service.js}

 * Record service for ECM.
 */
angular.module('services').factory('Ecm.RecordService', ['$resource', 'StoreService', 'UtilService'
    , function ($resource, Store, Util) {

        var Service = $resource('proxy/arkcase/api/latest/service', {}, {
            /**
             * @ngdoc method
             * @name _declareAsRecord
             * @methodOf services:Ecm.RecordService
             *
             * @description
             * Declare files and folders as record
             *
             * @param {Object} params Map of input parameter
             * @param {String} params.objectType  Object type
             * @param {Number} params.objectId  Object ID
             *
             * @returns {Object} Object returned by $resource
             */
            _declareAsRecord: {
                method: 'POST'
                , url: 'proxy/arkcase/api/latest/service/ecm/declare/:objectType/:objectId'
                , isArray: true
            }
        });

        /**
         * @ngdoc method
         * @name declareAsRecord
         * @methodOf services:Ecm.RecordService
         *
         * @description
         * Send email
         *
         * @param {String} objectType  Object type
         * @param {Number} objectId  Object ID
         * @param {Object} declareAsRecordData File data to declare as record
         *
         * @returns {Object} Object returned by $resource
         */
        Service.declareAsRecord = function (objectType, objectId, declareAsRecordData) {
            return Util.serviceCall({
                service: Service._declareAsRecord
                , param: {objectType: objectType, objectId: objectId}
                , data: declareAsRecordData
                , onSuccess: function (data) {
                    if (Service.validateDeclareAsRecord(data)) {
                        return data;
                    }
                }
            });
        };

        /**
         * @ngdoc method
         * @name validateDeclareAsRecord
         * @methodOf services:Ecm.RecordService
         *
         * @description
         * Validate declare as record data
         *
         * @param {Object} data  Data to be validated
         *
         * @returns {Boolean} Return true if data is valid
         */
        Service.validateDeclareAsRecord = function (data) {
            if (!Util.isArray(data)) {
                return false;
            }
            return true;
        };

        return Service;
    }
]);