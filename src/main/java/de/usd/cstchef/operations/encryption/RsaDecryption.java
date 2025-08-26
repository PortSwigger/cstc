package de.usd.cstchef.operations.encryption;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Cipher;
import javax.swing.JComboBox;

import org.bouncycastle.util.encoders.Hex;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.encryption.CipherUtils.CipherInfo;
import de.usd.cstchef.operations.signature.KeystoreOperation;
import de.usd.cstchef.view.ui.VariableTextArea;

@OperationInfos(name = "RSA Decryption", category = OperationCategory.ENCRYPTION, description = "Decrypt input using a private key")
public class RsaDecryption extends KeystoreOperation {

    private VariableTextArea privateKeyTextArea;

    private JComboBox<String> typeComboBox;

    private static String[] inOutModes = new String[] { "Raw", "Hex", "Base64" };

    protected String algorithm = "RSA";
    protected String cipherMode = "ECB";

    protected JComboBox<String> inputMode;
    protected JComboBox<String> outputMode;
    protected JComboBox<String> paddings;

    private String lastSelection = "PEM";

    public RsaDecryption() {
        super();
        this.createUIForPEM();
    }

    protected ByteArray perform(ByteArray input) throws Exception {

        if(typeComboBox.getSelectedItem().equals("PEM")) {
            String privateKeyString = this.privateKeyTextArea.getBytes().toString();

            if(privateKeyString.length() == 0) {
                throw new IllegalArgumentException("No private key available.");
            }

            StringBuilder privateKeyLines = new StringBuilder();
            BufferedReader buffRead = new BufferedReader(new StringReader(privateKeyString));

            String line;
            while((line = buffRead.readLine()) != null) {
                privateKeyLines.append(line);
            }

            String privateKeyPEM = privateKeyLines.toString();
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----", "");
		    privateKeyPEM = privateKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
		    privateKeyPEM = privateKeyPEM.replaceAll("\\s+","");

            byte[] privateKeyEncodedBytes = Base64.getDecoder().decode(privateKeyPEM);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyEncodedBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return factory.createByteArray(cipher.doFinal(input.getBytes()));
        }
        else if(typeComboBox.getSelectedItem().equals("KeyStore")) {
            if( ! this.keyAvailable.isSelected() )
                throw new IllegalArgumentException("No private key available.");

            String padding = (String)paddings.getSelectedItem();
            Cipher cipher = Cipher.getInstance(String.format("%s/%s/%s", algorithm, cipherMode, padding));
            cipher.init(Cipher.DECRYPT_MODE, this.selectedEntry.getPrivateKey());

            String selectedInputMode = (String)inputMode.getSelectedItem();
            String selectedOutputMode = (String)outputMode.getSelectedItem();
            byte[] in = new byte[0];
            if( selectedInputMode.equals("Hex") )
                in = Hex.decode(input.getBytes());
            if( selectedInputMode.equals("Base64") )
                in = Base64.getDecoder().decode(input.getBytes());

            byte[] encrypted = cipher.doFinal(input.getBytes());

            if( selectedOutputMode.equals("Hex") )
                encrypted = Hex.encode(encrypted);
            if( selectedOutputMode.equals("Base64") )
                encrypted = Base64.getEncoder().encode(encrypted);

            return factory.createByteArray(encrypted);
        }
        else {
            return factory.createByteArray("");
        }
    }

    @Override
    public void createUI() {
        this.typeComboBox = new JComboBox<String>();
        this.typeComboBox.addItem("PEM");
        this.typeComboBox.addItem("KeyStore");

        ActionListener comboBoxActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                /* The method should only run if the ActionEvent was triggered by the user in the UI (e.getModifiers() == 16).
                 * Not, for example, when loading a recipe (e.getModifiers() == 0)
                 */
                if(e.getModifiers() == 16) {
                    switch((String) typeComboBox.getSelectedItem()) {
                        case "PEM":
                        if(!lastSelection.equals("PEM")) {
                            clearUI();
                            createUIForPEM();
                        }
                            break;
                        case "KeyStore":
                        if(!lastSelection.equals("KeyStore")) {
                            clearUI();
                            createMyUI();
                        }
                            break;
                    }
                    lastSelection = (String) typeComboBox.getSelectedItem();
                    validate();
                    repaint();
                    updateStepPanel();
                }
            }

        };

        typeComboBox.addActionListener(comboBoxActionListener);
        this.addUIElement("Input Type", typeComboBox);
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

    private void clearUI() {
        Iterator<String> iterator = this.getUIElements().keySet().iterator();

        while(iterator.hasNext()) {
            String key = iterator.next();
            if(!key.equals("Input Type")) {
                iterator.remove();
                this.clearContentBox(2);
            }
        }
        this.validate();
        this.repaint();
    }

    private void createUIForPEM() {
        this.privateKeyTextArea = new VariableTextArea();
        this.addUIElement("Private Key", this.privateKeyTextArea);
    }

    public void updateStepPanel() {
        super.updateStepPanel();
    }

    @Override
    public void load(Map<String, Object> parameters) {
        if(parameters.get("Input Type") == null /* before v1.3.6 */ || parameters.get("Input Type").equals("KeyStore")) {
            this.clearUI();
            this.createMyUI();
        }
        else if(parameters.get("Input Type").equals("PEM")) {
            this.clearUI();
            this.createUIForPEM();
        }
        super.load(parameters);
    }

}
