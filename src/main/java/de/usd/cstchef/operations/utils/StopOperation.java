package de.usd.cstchef.operations.utils;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(name = "Stop Operation", category = OperationCategory.UTILS, description = "Terminates the execution of the recipe and returns the result of the last operation.")
public class StopOperation extends Operation {

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        this.setOperationSkip(10000);
        this.setLaneSkip(10000);
        return input;
    }

}
