package de.usd.cstchef.operations.compression;

import burp.BurpUtils;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.utilities.CompressionType;

import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;

@OperationInfos(name = "Inflate", category = OperationCategory.COMPRESSION, description = "Inflate input string")

public class Inflate extends Operation {

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        ByteArray data = BurpUtils.getInstance().getApi().utilities().compressionUtils().decompress(input, CompressionType.DEFLATE);

        return data;
    }
}
