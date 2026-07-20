package de.usd.cstchef.operations.compression;

import burp.BurpUtils;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.utilities.CompressionType;

import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;

@OperationInfos(name = "Deflate", category = OperationCategory.COMPRESSION, description = "Deflate input string")

public class Deflate extends Operation {

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        ByteArray compressedData = BurpUtils.getInstance().getApi().utilities().compressionUtils().compress(input, CompressionType.DEFLATE);

        return compressedData;

    }
}
