package de.usd.cstchef.operations.encryption;

import javax.crypto.Cipher;
import javax.swing.JComboBox;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.encryption.CipherUtils.CipherInfo;
import de.usd.cstchef.operations.signature.KeystoreOperation;

@OperationInfos(name = "RSA Decryption", category = OperationCategory.ENCRYPTION, description = "Decrypt input using a private key")
public class RsaDecryption extends KeystoreOperation {

    private static String[] inOutModes = new String[] { "Raw", "Hex", "Base64" };

    protected String algorithm = "RSA";
    protected String cipherMode = "ECB";

    protected JComboBox<String> inputMode;
    protected JComboBox<String> outputMode;
    protected JComboBox<String> paddings;

    public RsaDecryption() {
        super();
        this.createMyUI();
    }

    protected byte[] perform(byte[] input) throws Exception {

        if( ! this.keyAvailable.isSelected() )
            throw new IllegalArgumentException("No private key available.");

        String padding = (String)paddings.getSelectedItem();
        Cipher cipher = Cipher.getInstance(String.format("%s/%s/%s", algorithm, cipherMode, padding));
        cipher.init(Cipher.DECRYPT_MODE, this.selectedEntry.getPrivateKey());

        String selectedInputMode = (String)inputMode.getSelectedItem();
        String selectedOutputMode = (String)outputMode.getSelectedItem();

        if( selectedInputMode.equals("Hex") )
            input = Hex.decode(input);
        if( selectedInputMode.equals("Base64") )
            input = Base64.decode(input);

        byte[] encrypted = cipher.doFinal(input);

        if( selectedOutputMode.equals("Hex") )
            encrypted = Hex.encode(encrypted);
        if( selectedOutputMode.equals("Base64") )
            encrypted = Base64.encode(encrypted);

        return encrypted;
    }

    public void createMyUI() {

        super.createMyUI();

        CipherUtils utils = CipherUtils.getInstance();
        CipherInfo info = utils.getCipherInfo(this.algorithm);

        this.paddings = new JComboBox<>(info.getPaddings());
        this.addUIElement("Padding", this.paddings);

        this.inputMode = new JComboBox<>(inOutModes);
        this.addUIElement("Input", this.inputMode);

        this.outputMode = new JComboBox<>(inOutModes);
        this.addUIElement("Output", this.outputMode);
    }

}
