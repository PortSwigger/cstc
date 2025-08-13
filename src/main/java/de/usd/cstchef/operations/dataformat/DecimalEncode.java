package de.usd.cstchef.operations.dataformat;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(name = "Decimal Encode", category = OperationCategory.DATAFORMAT, description = "Convert characters to space-separated decimal values")
public class DecimalEncode extends Operation {

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        byte[] inputBytes = input.getBytes();  // safe for binary/text data

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < inputBytes.length; i++) {
            output.append(inputBytes[i] & 0xFF); // unsigned byte to int
            if (i != inputBytes.length - 1) {
                output.append(" ");
            }
        }

        // ✅ Debug log: input and output string values
        System.out.println("[DecimalEncode] Input: " + input.toString());
        System.out.println("[DecimalEncode] Encoded Output: " + output);

        return factory.createByteArray(output.toString());
    }
}

