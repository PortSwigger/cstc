package de.usd.cstchef.operations.string;

import org.bouncycastle.util.Arrays;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.Utils.MessageType;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;

@OperationInfos(name = "Reverse", category = OperationCategory.STRING, description = "Returns the reversed input.")
public class Reverse extends Operation {

	@Override
	protected ByteArray perform(ByteArray input, MessageType messageType) throws Exception {
		return factory.createByteArray(Arrays.reverse(input.getBytes()));
	}

}