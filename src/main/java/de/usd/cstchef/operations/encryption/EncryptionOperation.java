package de.usd.cstchef.operations.encryption;

import javax.crypto.Cipher;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.Utils.MessageType;

public abstract class EncryptionOperation extends CryptOperation {

    public EncryptionOperation(String alogrithm) {
        super(alogrithm);
    }

    @Override
    protected ByteArray perform(ByteArray input, MessageType messageType) throws Exception {
        return factory.createByteArray(this.crypt(input.getBytes(), Cipher.ENCRYPT_MODE, this.algorithm, (String) this.cipherMode.getSelectedItem(),
                (String) this.paddings.getSelectedItem()));
    }

}
