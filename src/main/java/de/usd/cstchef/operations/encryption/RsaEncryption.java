package de.usd.cstchef.operations.encryption;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Cipher;
import javax.swing.JComboBox;

import org.bouncycastle.util.encoders.Hex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.encryption.CipherUtils.CipherInfo;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.signature.KeystoreOperation;
import de.usd.cstchef.view.ui.VariableTextArea;

@OperationInfos(name = "RSA Encryption", category = OperationCategory.ENCRYPTION, description = "Encrypt input using a public key")
public class RsaEncryption extends KeystoreOperation {

    private VariableTextArea publicKeyTextArea;

    private JComboBox<String> typeComboBox;

    private static String[] inOutModes = new String[] { "Raw", "Hex", "Base64" };

    protected String algorithm = "RSA";
    protected String cipherMode = "ECB";

    protected JComboBox<String> inputMode;
    protected JComboBox<String> outputMode;
    protected JComboBox<String> paddings;

    private String lastSelection = "PEM";

    public RsaEncryption() {
        super();
        this.createUIForPEM();
    }


    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        if(typeComboBox.getSelectedItem().equals("PEM")) {
            String publicKeyString = this.publicKeyTextArea.getBytes().toString();

            if(publicKeyString.length() == 0) {
                throw new IllegalArgumentException("No public key available.");
            }

            StringBuilder publicKeyLines = new StringBuilder();
            BufferedReader buffRead = new BufferedReader(new StringReader(publicKeyString));

            String line;
            while((line = buffRead.readLine()) != null) {
                publicKeyLines.append(line);
            }

            String publicKeyPEM = publicKeyLines.toString();
            publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
		    publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
		    publicKeyPEM = publicKeyPEM.replaceAll("\\s+","");

            byte[] publicKeyEncodedBytes = Base64.getDecoder().decode(publicKeyPEM);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyEncodedBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return factory.createByteArray(cipher.doFinal(input.getBytes()));
        }
        else if(typeComboBox.getSelectedItem().equals("KeyStore")) {
            if( ! this.certAvailable.isSelected() )
                throw new IllegalArgumentException("No certificate available.");

            String padding = (String)paddings.getSelectedItem();
            Cipher cipher = Cipher.getInstance(String.format("%s/%s/%s", algorithm, cipherMode, padding));
            cipher.init(Cipher.ENCRYPT_MODE, this.cert.getPublicKey());

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
        this.publicKeyTextArea = new VariableTextArea();
        this.addUIElement("Public Key", this.publicKeyTextArea);
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