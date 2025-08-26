package de.usd.cstchef.operations.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import burp.Logger;
import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;

@OperationInfos(name = "Unzip", category = OperationCategory.COMPRESSION, description = "Unzip input string using PKZIP")

public class Unzip extends Operation {

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
            ZipInputStream zis = new ZipInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null){
                throw new IOException("No zip entry found");
            }                

            byte[] buffer = new byte[4096];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            zis.closeEntry();
            return factory.createByteArray(baos.toByteArray());
        }
        catch(Exception ex){
            Logger.getInstance().err(ex.getMessage());
            throw new IOException("Cannot parse input values");
        }               
    }
}
