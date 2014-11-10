/**
 * TaskList.Object
 *
 * manages screen objects
 *
 * @author jwu
 */
TaskList.Object = {
    create : function() {

        var ti = this.getTreeInfo();
        /*var tiApp = App.getComplaintTreeInfo();
        if (tiApp) {
            ti.initKey = tiApp.initKey;
            ti.start = tiApp.start;
            ti.n = tiApp.n;
            ti.s = tiApp.s;
            ti.q = tiApp.q;
            ti.complaintId = tiApp.complaintId;
            App.setComplaintTreeInfo(null);
        }*/

        var items = $(document).items();
        var taskId = items.properties("taskId").itemValue();
        if (Acm.isNotEmpty(taskId)) {
            ti.taskId = taskId;
        }
        /*var token = items.properties("token").itemValue();
        this.setToken(token);*/

        this.$noTaskFoundMeassge = $("#noTaskFoundMeassge");
        this.showObject(this.$noTaskFoundMeassge, false);


        this.$btnSignConfirm    = $("#signatureConfirmBtn");
        this.$btnReject         = $("button[data-title='Reject']");
        this.$btnSignConfirm.click(function(e) {TaskList.Event.onClickBtnSignConfirm(e);});
        this.$btnReject.click(function(e) {TaskList.Event.onClickBtnReject(e);});

        //parent object information
        this.$lnkParentObjTitle          = $("#parentObjTitle");
        this.$lnkParentNumber = $("#parentObjNumber");
        this.$lnkParentObjIncidentDate = $("#parentObjIncidentDate");
        this.$lnkParentObjPriority       = $("#parentObjPriority");
        this.$lnkParentObjAssigned       = $("#parentObjAssigned");
        this.$lnkParentObjSubjectType  = $("#parentObjSubjectType");
        this.$lnkParentObjStatus         = $("#parentObjStatus");
        // end of parent object information


        //modal dialog buttons
        this.$btnApproveTask = $("#btnApprove");
        this.$btnReassignTask = $("#btnReassign");
        this.$btnUnassignTask = $("#btnUnassign");
        this.$btnCompleteTask = $("#btnComplete");
        this.$btnRejectTask = $("#btnReject");
        this.$btnDeleteTask = $("#btnDelete");


        this.$lnkStatus = $("#status");
        this.$lnkStatus.editable('disable');

        this.$lnkTaskSubject = $("#taskSubject");

        this.$lnkTaskSubject.editable({placement: 'bottom'
            ,emptytext: "N/A"
            ,color: "black"

        ,success: function(response, newValue) {
                TaskList.Event.onSaveTitle(newValue);
            }
        });


        this.$perCompleted		= $("#percentageCompleted");
        this.$perCompleted.editable({placement: 'bottom'
            ,emptytext: "N/A"
            ,success: function(response, newValue) {
                TaskList.Event.onSavePerComplete(newValue);
            }
        });
        this.$lnkStartDate      = $("#startDate");
        this.$lnkStartDate.editable({placement: 'bottom'
            ,emptytext: "N/A"
            ,format: 'mm/dd/yyyy'
            ,viewformat: 'mm/dd/yyyy'
            ,datepicker: {
                weekStart: 1
            }
            ,success: function(response, newValue) {
                TaskList.Event.onSaveStartDate(newValue);
            }
        });

        this.$lnkDueDate        = $("#dueDate");
        this.$lnkDueDate.editable({placement: 'bottom'
            ,emptytext: "N/A"
            ,format: 'mm/dd/yyyy'
            ,viewformat: 'mm/dd/yyyy'
            ,datepicker: {
                weekStart: 1
            }
            ,success: function(response, newValue) {
                TaskList.Event.onSaveDueDate(newValue);
            }
        });

        this.$lnkPriority       = $("#priority");

        this.$lnkOwner          = $("#taskOwner");
        this.$lnkOwner.editable({placement: 'bottom'
            ,emptytext: "N/A"
            ,success: function(response, newValue) {
                TaskList.Event.onSaveOwner(newValue);
            }
        });

        this.$divDetails        = $(".taskDetails");
        this.$btnEditDetails    = $("#tabDetails button:eq(0)");
        this.$btnSaveDetails    = $("#tabDetails button:eq(1)");
        this.$btnEditDetails.on("click", function(e) {TaskList.Event.onClickBtnEditDetails(e);});
        this.$btnSaveDetails.on("click", function(e) {TaskList.Event.onClickBtnSaveDetails(e);});


        this.$lnkOwner          = $("#taskOwner");
        this.$lnkOwner.editable({placement: 'bottom'
            ,emptytext: "N/A"
            ,success: function(response, newValue) {
                TaskList.Event.onSaveOwner(newValue);
            }
        });

        this.$listSignature     = $("#signatureList");

        // forms
        this.$formSignature     = $("#signatureConfirmForm");

        // modals
        this.$modalSignConfirm  = $("#signatureModal");

        //fancy tree
        this.$tree = $("#tree");
        this._useFancyTree(this.$tree);

        this.$divNotes = $("#divNotes");
        TaskList.JTable.createJTableNotes(this.$divNotes);

        this.$divDocuments = $("#divDocuments");
        TaskList.JTable.createJTableDocuments(this.$divDocuments);

        this.$divHistory = $("#divHistory");
        TaskList.JTable.createJTableEvents(this.$divHistory);

        this.$divWorkflowOverview = $("#divWorkflowOverview");
        TaskList.JTable.createJTableWorkflowOverview(this.$divWorkflowOverview);

        this.$divReworkInstructions = $(".taskReworkInstructions");

        this.$divAttachments = $("#divAttachments");
        TaskList.JTable.createJTableAttachments(this.$divAttachments);

    }

    //  Use this to build the Admin tree structure
    //------------------ Tree  ------------------
    //

    ,_treeInfo: {
        start           : 0
        ,n              : 50
        ,total          : -1
        ,s              : null
        ,q              : null
        ,initKey        : null
        ,taskId    : 0
    }
    ,getTreeInfo: function() {
        return this._treeInfo;
    }
    ,getTaskIdByKey: function(key) {
        return this._parseKey(key).taskId;
    }
    ,getPageIdByKey: function(key) {
        return this._parseKey(key).pageId;
    }
    ,_parseKey: function(key) {
        var parts = {pageId: -1, taskId: 0, sub: ""};
        if (Acm.isEmpty(key)) {
            return parts;
        }

        var arr = key.split(".");
        if (1 <= arr.length) {
            var pageId = parseInt(arr[0]);
            if (! isNaN(pageId)) {
                parts.pageId = pageId;
            }
        }
        if (2 <= arr.length) {
            var taskId;
            if(arr[1].indexOf("adHoc") != -1){
                var adHocTaskId = arr[1];
                taskId = adHocTaskId.replace("adHoc", "");
                taskId = parseInt(taskId);
            }
            else{
                taskId = parseInt(arr[1]);
            }
            if (! isNaN(taskId)) {
                parts.taskId = taskId;
            }
        }
        if (3 <= arr.length) {
            parts.sub = arr[2];
        }
        return parts;
    }

    ,getNodeTypeByKey: function(key) {
        if (Acm.isEmpty(key)) {
            return null;
        }
        var arr = key.split(".");
        if (1 == arr.length) {
            if ("prevPage" == key) {
                return "prevPage";
            } else if ("nextPage" == key) {
                return "nextPage";
            } else { //if ($.isNumeric(arr[0])) {
                return "p";
            }
        } else if (2 == arr.length) {
            if(arr[1].indexOf("adHoc") != -1){
                return "taskAdHoc";
            }
            else{
                return "task";
            }
        } else if (3 == arr.length) {
            return "task" + arr[2];
        }
         else if (4 == arr.length) {
            return "taskAdHoc";
        }
        else if (5 == arr.length) {
            return "task" + arr[4];
        }

        return null;
    }
    ,_mapNodeTab: {
        task     : ["tabDetails",
                    "tabDocuments",
                    "tabNotes",
                    "tabHistory",
                    "tabReworkInstructions",
                    "tabWorkflowOverview",
                    "tabAttachments"],

        taskAdHoc     : ["tabDetails",
                        "tabNotes",
                        "tabHistory",
                        "tabWorkflowOverview",
                        "tabAttachments",
                        "tabDocuments"],

        taskDetails  : ["tabDetails"],
        taskDocuments: ["tabDocuments"],
        taskNotes    : ["tabNotes"],
        taskHistory  : ["tabHistory"],
        taskReworkInstructions : ["tabReworkInstructions"],
        taskWorkflowOverview : ["tabWorkflowOverview"],
        taskAttachments: ["tabAttachments"]
    }
    ,_getTabIdsByKey: function(key) {
        var nodeType = this.getNodeTypeByKey(key);
        var tabIds = ["tabBlank"];
        for (var key in this._mapNodeTab) {
            if (nodeType == key) {
                tabIds = this._mapNodeTab[key];
                break;
            }
        }
        return tabIds;
    }
    ,showTab: function(key) {
        var tabIds = ["tabBlank"
            ,"tabDetails"
            ,"tabDocuments"
            ,"tabNotes"
            ,"tabHistory"
            ,"tabWorkflowOverview"
            ,"tabReworkInstructions"
            ,"tabAttachments"
        ];
        var tabIdsToShow = this._getTabIdsByKey(key);
        for (var i = 0; i < tabIds.length; i++) {
            var show = this._foundItemInArray(tabIds[i], tabIdsToShow);
            Acm.Object.show($("#" + tabIds[i]), show);
        }
    }
    ,_foundItemInArray: function(item, arr) {
        for (var i = 0; i < arr.length; i++) {
            if (item == arr[i]) {
                return true;
            }
        }
        return false;
    }

    ,refreshTaskTreeNode: function(task) {
        if (!task) {
            task = TaskList.getTask();
        }
        if (task && task.taskId) {
            var key;
            if(task.adhocTask == true){
                key = "adHoc" + task.taskId;
            }
            else{key = task.taskId;}
            var node = this.$tree.fancytree("getTree").getNodeByKey(this._getTaskKey(key));
            //var node = this.$tree.fancytree("getActiveNode");
            if (node) {
                var nodeTitle = Acm.goodValue(Acm.getDateFromDatetime(task.dueDate) + "," + task.priority + "," + task.title);
                node.setTitle(nodeTitle);
            }
        }
    }
    ,_getTaskKey: function(taskId) {
        var treeInfo = TaskList.Object.getTreeInfo();
        var start = treeInfo.start;
        var pageId = start.toString();
        return pageId + "." + taskId;
    }
    ,refreshTree: function(key) {
        this.tree.reload().done(function(){
            if (Acm.isNotEmpty(key)) {
                TaskList.Object.tree.activateKey(key);
            }
        });
    }
    ,activeTreeNode: function(key) {
        this.tree.activateKey(key);
    }
    ,expandAllTreeNode: function(key) {
        this.tree.activateKey(key);
    }
    ,_useFancyTree: function($s) {

        $s.fancytree({
            activate: function(event, data){
                var node = data.node;
                TaskList.Event.onActivateTreeNode(node);
            }
            ,renderNode: function(event, data) {
                // Optionally tweak data.node.span
                var node = data.node;
                var key = node.key;
                var acmIcon = null; //node.data.acmIcon;
                var nodeType = TaskList.Object.getNodeTypeByKey(key);
                if ("prevPage" == nodeType) {
                    acmIcon = "<i class='i i-arrow-up'></i>";
                } else if ("nextPage" == nodeType) {
                    acmIcon = "<i class='i i-arrow-down'></i>";
                }
                if (acmIcon) {
                    var span = node.span;
                    var $spanIcon = $(span.children[1]);
                    $spanIcon.removeClass("fancytree-icon");
                    $spanIcon.html(acmIcon);
                }
            }
            ,source: function() {
                return TaskList.Object.treeSource();
            } //end source

        }); //end fancytree

        this.tree = this.$tree.fancytree("getTree");

        $s.contextmenu({
            //delegate: "span.fancytree-title",
            delegate: ".fancytree-title",
            beforeOpen: function(event, ui) {
                var node = $.ui.fancytree.getNode(ui.target);
                TaskList.Object.$tree.contextmenu("replaceMenu", TaskList.Object._getMenu(node));
                node.setActive();
            },
            select: function(event, ui) {
                var node = $.ui.fancytree.getNode(ui.target);
                alert("select " + ui.cmd + " on " + node);
            }
        });
    }
    ,treeSource: function() {
        var builder = AcmEx.FancyTreeBuilder.reset();
        var treeInfo = TaskList.Object.getTreeInfo();
        var acmIcon = null;

        var start = treeInfo.start;
        var tasks = TaskList.cachePage.get(start);
        if (null == tasks || 0 >= tasks.length) {
            return builder.getTree();
        }

        if (0 < treeInfo.start) {
            builder.addLeaf({
                key: "prevPage",
                title: treeInfo.start + " records above...",
                tooltip: "Review previous records",
                expanded: false,
                folder: false,
                acmIcon: "<i class='i i-arrow-up'></i>"
            });
        }

        //populate task data
        var pageId = start.toString();
        for (var i = 0; i < tasks.length; i++) {
            var taskSolr = tasks[i];
            var taskBranchID = taskSolr.object_id_s;

            //check if task is associated with an object or not
            var adHoc;
            if(taskSolr.adhocTask_b == true){
                adHoc = true;
            }
            else{
                adHoc = false;
            }
            //&& task.due_dt != null
            //
            var taskBranchTitle;
            if(taskSolr.title_t != null && taskSolr.priority_s != null && taskSolr.due_dt != null){
                taskBranchTitle = Acm.getDateFromDatetime(taskSolr.due_dt) + "," + taskSolr.priority_s +","+ taskSolr.title_t;
            }
            else if(taskSolr.title_t != null && taskSolr.priority_s != null){
                taskBranchTitle = taskSolr.priority_s +","+ taskSolr.title_t;
            }
            else if(taskSolr.title_t != null){
                taskBranchTitle = taskSolr.title_t;
            }
            else{
                taskBranchTitle = "No title";
            }
            if(adHoc == false){
                builder.addBranch({key: pageId + "." + taskBranchID                      //level 1: /Task
                    , title: taskBranchTitle,
                    tooltip: taskSolr.name,
                    expanded: false
                })

                    .addLeaf({key: pageId + "." + taskBranchID + ".Details"                   //level 2: /Task/Details
                        , title: "Details"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".ReworkInstructions"                   //level 2: /Task/Rework Instructions
                        , title: "Rework Instructions"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".Documents"                   //level 2: /Task/Documents
                        , title: "Documents Under Review"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".Attachments"                   //level 2: /Task/Attachments
                        , title: "Attachments"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".Notes"                   //level 2: /Task/Notes
                        , title: "Notes"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".WorkflowOverview"                   //level 2: /Task/Workflow Overview
                        , title: "Workflow Overview"
                    })
                    .addLeafLast({key: pageId + "." + taskBranchID + ".History"                   //level 2: /Task/History
                        , title: "History"
                    })

            }
            else{

                builder.addBranch({key: pageId + "." + "adHoc"+taskBranchID                      //level 1: /Task
                    , title: taskBranchTitle,
                    tooltip: taskSolr.name,
                    expanded: false
                })

                    .addLeaf({key: pageId + "." + taskBranchID + ".Details"                   //level 2: /Task/Details
                        , title: "Details"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".Attachments"                   //level 2: /Task/Attachments
                        , title: "Attachments"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".Notes"                   //level 2: /Task/Notes
                        , title: "Notes"
                    })
                    .addLeaf({key: pageId + "." + taskBranchID + ".WorkflowOverview"                   //level 2: /Task/Workflow Overview
                        , title: "Workflow Overview"
                    })
                    .addLeafLast({key: pageId + "." + taskBranchID + ".History"                   //level 2: /Task/History
                        , title: "History"
                    })
            }
        }

        if ((0 > treeInfo.total)                                    //unknown size
            || (treeInfo.total - treeInfo.n > treeInfo.start)) {   //no more page left
            var title = (0 > treeInfo.total)? "More records..."
                : (treeInfo.total - treeInfo.start - treeInfo.n) + " more records...";
            builder.addLeafLast({key: "nextPage"
                ,title: title
                ,tooltip: "Load more records"
                ,expanded: false
                ,folder: false
                ,acmIcon: "<i class='i i-arrow-down'></i>"
            });
        }
        return builder.getTree();
     //end of tasks
    }
    ,_getMenu: function(node) {
        var key = node.key;
        var menu = [
            {title: "Menu:" + key, cmd: "cut", uiIcon: "ui-icon-scissors"},
            {title: "Copy", cmd: "copy", uiIcon: "ui-icon-copy"},
            {title: "Paste", cmd: "paste", uiIcon: "ui-icon-clipboard", disabled: false },
            {title: "----"},
            {title: "Edit", cmd: "edit", uiIcon: "ui-icon-pencil", disabled: true },
            {title: "Delete", cmd: "delete", uiIcon: "ui-icon-trash", disabled: true },
            {title: "More", children: [
                {title: "Sub 1", cmd: "sub1"},
                {title: "Sub 2", cmd: "sub1"}
            ]}
        ];
        return menu;
    }

    //----------------- end of tree -----------------



    ,showObject : function(obj, show) {
        Acm.Object.show(obj, show);
    }

    ,setSignatureList: function(val) {
        this.$listSignature.empty();
        this.$listSignature.append(val);
    }
    ,getSignatureForm: function() {
        return this.$formSignature;
    }
    ,hideSignatureModal: function() {
        this.$modalSignConfirm.modal('hide');
    }


    ,setValueLnkTaskSubject: function(txt) {
        this.$lnkTaskSubject.editable("setValue", txt);
    }
    ,setValueLnkPerCompleted : function(txt) {
        if ( txt ) {
            this.$perCompleted.editable("setValue", txt)
        }
        else {
            this.$perCompleted.editable("setValue", "0")
        }
    }
    ,setValueTaskOwner : function(owner) {
        if ( owner ) {
            this.$lnkOwner.editable("setValue", owner, false);
        }
        else {
            this.$lnkOwner.editable("setValue", "N/A", false);
        }

    }
    ,setValueLnkPriority: function(txt) {
        if (txt) {
            this.$lnkPriority.editable("setValue", txt);
        }
        else {
            this.$lnkPriority.editable("setValue", "N/A");
        }
    }
    ,setValueLnkStartDate : function(date) {
        if ( date ) {
            this.$lnkStartDate.editable("setValue", date, true);
        }
        else {
            this.$lnkStartDate.editable("setValue", "N/A", true);
        }
    }
    ,setValueLnkDueDate: function(date) {
        if ( date ) {
            this.$lnkDueDate.editable("setValue", date, true);
        }
        else {
            this.$lnkDueDate.editable("setValue", "N/A", true);
        }
    }
    ,setValueAssignedStatus : function(status) {
        if ( status ) {
            this.$lnkStatus.editable("setValue", status);
        }
        else {
            this.$lnkStatus.editable("setValue", "N/A");
        }
    }

    ,setValueDetails : function(details) {
        if ( details ) {
            Acm.Object.setHtml(this.$divDetails, details);
        }
        else {
            Acm.Object.setHtml(this.$divDetails, "");
        }
    }

    ,setNumericValueLnkPriority: function(txt) {
        var priorityValue;
        if(txt == "Low"){
            priorityValue = 25;
        }
        else if(txt == "Medium"){
            priorityValue = 50;
        }
        else if (txt == "High"){
            priorityValue = 75;
        }
        else {
            priorityValue = 90;
        }
        this.$lnkPriority.editable("setValue", priorityValue);
    }



    //parent object information setters
    ,setLnkParentObjTitle: function(txt) {
        if (txt) {
            Acm.Object.setText(this.$lnkParentObjTitle, txt);
        }
        else {
            Acm.Object.setText(this.$lnkParentObjTitle, "N/A");
        }
    }
    ,setValueLnkParentObjNumber: function(txt) {
        if (txt) {
            Acm.Object.setText(this.$lnkParentNumber, txt);
        }
        else {
            Acm.Object.setText(this.$lnkParentNumber, "N/A");
        }
    }
    ,setValueLnkParentObjIncidentDate: function(date) {
        if ( date ) {
            Acm.Object.setText(this.$lnkParentObjIncidentDate, date);
        }
        else {
            Acm.Object.setText(this.$lnkParentObjIncidentDate, "N/A");
        }
    }
    ,setLnkParentObjPriority: function(txt) {
        if (txt) {
            Acm.Object.setText(this.$lnkParentObjPriority, txt);
        }
        else {
            Acm.Object.setText(this.$lnkParentObjPriority, "N/A");
        }
    }
    ,setLnkParentObjAssigned: function(txt) {
        if (txt) {
            Acm.Object.setText(this.$lnkParentObjAssigned, txt);
        }
        else {
            Acm.Object.setText(this.$lnkParentObjAssigned, "N/A");
        }
    }
    ,setLnkParentObjSubjectType: function(txt) {
        if(txt){
            Acm.Object.setText(this.$lnkParentObjSubjectType, txt);
        }
        else{
            Acm.Object.setText(this.$lnkParentObjSubjectType, "N/A");
        }
    }
    ,setLnkParentObjStatus: function(txt) {
        if(txt){
            Acm.Object.setText(this.$lnkParentObjStatus, txt);
        }
        else{
            Acm.Object.setText(this.$lnkParentObjStatus, "N/A");
        }
    }

    ,initPriority: function(data) {
        var choices = []; //[{value: "", text: "Choose Priority"}];
        $.each(data, function(idx, val) {
            var opt = {};
            opt.value = val;
            opt.text = val;
            choices.push(opt);
        });

        this.$lnkPriority.editable({placement: 'bottom'
            ,emptytext: "N/A"
            ,value: ""
            ,source: choices
            ,success: function(response, newValue) {
                TaskList.Event.onSavePriority(newValue);
            }
        });
    }

    ,updateDetail: function(task) {
        if(task.adhocTask){
            this.$btnApproveTask.hide();
            this.$btnRejectTask.hide();
            this.$btnCompleteTask.show();
            this.$btnDeleteTask.show();

            //this.refreshJTableDetails();
            this.refreshJTableAttachments();
            this.refreshJTableNotes();
            this.refreshJTableWorkflowOverview();
            this.refreshJTableHistory();
        }
        else{
            this.$btnCompleteTask.hide();
            this.$btnDeleteTask.hide();
            this.$btnApproveTask.show();
            this.$btnRejectTask.show();

            //this.refreshJTableDetails();
            this.refreshJTableAttachments();
            this.refreshJTableNotes();
            this.refreshJTableWorkflowOverview();
            this.refreshJTableHistory();
            this.refreshJTableDocuments();
            this.refreshJTableInstructions();
        }
        TaskList.Object.refreshTaskTreeNode(task);
        this.setValueLnkTaskSubject(task.title);
        this.setValueLnkPerCompleted(task.percentComplete);
        this.setValueLnkStartDate(Acm.getDateFromDatetime(task.taskStartDate));
        this.setValueLnkDueDate(Acm.getDateFromDatetime(task.dueDate));
        this.setValueLnkPriority(task.priority);
        this.setValueTaskOwner(task.owner);
        this.setValueAssignedStatus(task.status);
        this.setValueDetails(task.details);




    }

    ,updateParentObjDetail: function(parentObj) {
        this.setLnkParentObjTitle(parentObj.title);
        this.setValueLnkParentObjIncidentDate(Acm.getDateFromDatetime(parentObj.incidentDate));
        this.setLnkParentObjPriority(parentObj.priority);
        this.setLnkParentObjAssigned(parentObj.assignee);
        this.setLnkParentObjStatus(parentObj.status);
        this.setLnkParentObjSubjectType(parentObj.subjectType);
        this.setValueLnkParentObjNumber(parentObj.number);
    }

    ,editDivDetails: function() {
        AcmEx.Object.editSummerNote(this.$divDetails);
    }
    ,saveDivDetails: function() {
        return AcmEx.Object.saveSummerNote(this.$divDetails);
    }
    ,refreshJTableNotes: function(){
        AcmEx.Object.jTableLoad(this.$divNotes);

    }
    ,refreshJTableAttachments: function(){
        AcmEx.Object.jTableLoad(this.$divAttachments);

    }
    ,refreshJTableDetails: function(){
        AcmEx.Object.jTableLoad(this.$divDetails);

    }
    ,refreshJTableDocuments: function(){
        AcmEx.Object.jTableLoad(this.$divDocuments);

    }
    ,refreshJTableHistory: function(){
        AcmEx.Object.jTableLoad(this.$divHistory);

    }
    ,refreshJTableInstructions: function(){
        AcmEx.Object.jTableLoad(this.$divReworkInstructions);

    }
    ,refreshJTableWorkflowOverview: function(){
        AcmEx.Object.jTableLoad(this.$divWorkflowOverview);

    }
};




