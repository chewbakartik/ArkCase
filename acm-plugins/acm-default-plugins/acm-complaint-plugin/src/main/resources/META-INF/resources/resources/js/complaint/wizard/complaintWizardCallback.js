/**
 * ComplaintWizard.Callback
 *
 * Callback handlers for server responses
 *
 * @author jwu
 */
ComplaintWizard.Callback = {
    initialize : function() {
        Acm.Dispatcher.addEventListener(this.EVENT_APPROVERS_RETRIEVED, this.onApproversRetrieved);
        Acm.Dispatcher.addEventListener(this.EVENT_COMPLAIN_SAVED, this.onComplaintSaved);
        Acm.Dispatcher.addEventListener(this.EVENT_COMPLAIN_SUBMITTED, this.onComplaintSubmitted);
    }

    ,EVENT_APPROVERS_RETRIEVED  : "complaint-wizard-approvers-retrieved"
    ,EVENT_COMPLAIN_SAVED		: "complaint-wizard-complaint-saved"
    ,EVENT_COMPLAIN_SUBMITTED    : "complaint-wizard-complaint-submitted"


    ,onApproversRetrieved : function(Callback, response) {
        var success = false;
        if (response) {
            ComplaintWizard.Object.initApprovers(response);
            success = true;
        }

        if (!success) {
            Acm.Dialog.error("Failed to retrieve approvers");
        }
    }
    ,onComplaintSaved : function(Callback, response) {
        var success = false;
        if (response) {
            if (Acm.isNotEmpty(response.complaintId)) {
                ComplaintWizard.Object.setComplaintData(response);
                success = true;
            }
        }

        if (!success) {
            Acm.Dialog.error("Failed to create or save complaint");
        }
    }
    ,onComplaintSubmitted : function(Callback, response) {
        var success = false;
        if (response) {
                //validate response.length == Complaint.getComplaint().approvers.length
                success = true;
        }

        if (!success) {
            Acm.Dialog.error("Error occurred for complaint approval submission");
        }
    }
};
