package de.usd.cstchef.operations.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import burp.Logger;
import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;

@OperationInfos(name = "Zip", category = OperationCategory.COMPRESSION, description = "Zip input string using PKZIP")

public class Zip extends Operation {

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("data");
            zos.putNextEntry(entry);
            zos.write(input.getBytes());
            zos.closeEntry();
            zos.finish();
            return factory.createByteArray(baos.toByteArray());
        }   
        catch(Exception ex){
            Logger.getInstance().err(ex.getMessage());
            throw new IOException("Cannot parse input values");
        }               
    }
}
