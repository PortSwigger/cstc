package de.usd.cstchef.operations.dataformat;

import burp.BurpUtils;
import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(name = "Decimal Decode", category = OperationCategory.DATAFORMAT, description = "Decode space-separated decimal integers to characters")
public class DecimalDecode extends Operation {

    // ✅ Static block runs when the class is loaded
    static {
        System.out.println("[DecimalDecode] Class loaded");
    }

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        System.out.println("[DecimalDecode] Input: " + input.toString());

        String[] parts = input.toString().trim().split("\\s+");

        byte[] decodedBytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            decodedBytes[i] = (byte) Integer.parseInt(parts[i]);
        }

        String output = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("[DecimalDecode] Output: " + output);

        return factory.createByteArray(output);
    }
}
