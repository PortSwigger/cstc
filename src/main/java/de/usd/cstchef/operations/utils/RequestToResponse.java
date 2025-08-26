package de.usd.cstchef.operations.utils;

import org.apache.commons.lang3.NotImplementedException;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(name = "Request To Response", category = OperationCategory.UTILS, description = "Returns the request to the response.\nOnly usable in Incoming Responses.")
public class RequestToResponse extends Operation {


    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        throw new NotImplementedException("This method is not implemented for the operation " + this.getClass().getAnnotation(OperationInfos.class).name());
    }

    @Override
    public ByteArray perform(ByteArray input, ByteArray requestToResponse) {
        if(this.getRecipeStepPanel().getOperation().toString() != "Incoming") {
            throw new IllegalArgumentException("This operation is only available in Incoming Responses.");
        }
        else {
            if(requestToResponse != null) {
                return requestToResponse;
            }
            else {
                throw new IllegalArgumentException("No request could be loaded.");
            }
        }
    }

    public void createUI() {
    }
}
