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

import java.awt.Component;
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

    private JComboBox<String> pemPadding;
    private JComboBox<String> pemOaepHash;
    private JComboBox<String> pemMgf1Hash;

    private JComboBox<String> typeComboBox;

    private static String[] inOutModes = new String[] { "Raw", "Hex", "Base64" };

    protected String algorithm = "RSA";
    protected String cipherMode = "ECB";

    protected JComboBox<String> inputMode;
    protected JComboBox<String> outputMode;
    protected JComboBox<String> paddings;
    protected JComboBox<String> ksOaepHash;
    protected JComboBox<String> ksMgf1Hash;

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

            Cipher cipher = RsaCipherBuilder.build(Cipher.ENCRYPT_MODE, publicKey,
                    (String) pemPadding.getSelectedItem(),
                    (String) pemOaepHash.getSelectedItem(),
                    (String) pemMgf1Hash.getSelectedItem());
            return factory.createByteArray(cipher.doFinal(input.getBytes()));
        }
        else if(typeComboBox.getSelectedItem().equals("KeyStore")) {
            if( ! this.certAvailable.isSelected() )
                throw new IllegalArgumentException("No certificate available.");

            String padding = (String)paddings.getSelectedItem();
            Cipher cipher;
            if(RsaCipherBuilder.isOaep(padding)) {
                // Named OAEP transformations (e.g. OAEPWITHSHA-256ANDMGF1PADDING) leave MGF1 at
                // SHA-1 in SunJCE, so build an explicit OAEPParameterSpec with both chosen digests.
                cipher = RsaCipherBuilder.build(Cipher.ENCRYPT_MODE, this.cert.getPublicKey(),
                        RsaCipherBuilder.PADDING_OAEP,
                        (String) ksOaepHash.getSelectedItem(),
                        (String) ksMgf1Hash.getSelectedItem());
            } else {
                cipher = Cipher.getInstance(String.format("%s/%s/%s", algorithm, cipherMode, padding));
                cipher.init(Cipher.ENCRYPT_MODE, this.cert.getPublicKey());
            }

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

        this.paddings = new JComboBox<>(RsaCipherBuilder.keyStorePaddings(info.getPaddings()));
        this.addUIElement("Padding", this.paddings);

        this.ksOaepHash = new JComboBox<>(RsaCipherBuilder.DIGESTS);
        this.ksOaepHash.setSelectedItem(RsaCipherBuilder.DEFAULT_DIGEST);
        this.addUIElement("OAEP Hash", this.ksOaepHash);

        this.ksMgf1Hash = new JComboBox<>(RsaCipherBuilder.DIGESTS);
        this.ksMgf1Hash.setSelectedItem(RsaCipherBuilder.DEFAULT_DIGEST);
        this.addUIElement("MGF1 Hash", this.ksMgf1Hash);

        this.inputMode = new JComboBox<>(inOutModes);
        this.addUIElement("Input", this.inputMode);

        this.outputMode = new JComboBox<>(inOutModes);
        this.addUIElement("Output", this.outputMode);

        // The OAEP/MGF1 digests only apply to OAEP paddings, so only show them then.
        this.paddings.addActionListener(e -> updateKeyStoreOaepVisibility());
        updateKeyStoreOaepVisibility();
    }

    private void updateKeyStoreOaepVisibility() {
        boolean oaep = RsaCipherBuilder.isOaep((String) this.paddings.getSelectedItem());
        setRowVisible(this.ksOaepHash, oaep);
        setRowVisible(this.ksMgf1Hash, oaep);
        validate();
        repaint();
        updateStepPanel();
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

        this.pemPadding = new JComboBox<>(RsaCipherBuilder.PADDINGS);
        this.addUIElement("Padding", this.pemPadding);

        this.pemOaepHash = new JComboBox<>(RsaCipherBuilder.DIGESTS);
        this.pemOaepHash.setSelectedItem(RsaCipherBuilder.DEFAULT_DIGEST);
        this.addUIElement("OAEP Hash", this.pemOaepHash);

        this.pemMgf1Hash = new JComboBox<>(RsaCipherBuilder.DIGESTS);
        this.pemMgf1Hash.setSelectedItem(RsaCipherBuilder.DEFAULT_DIGEST);
        this.addUIElement("MGF1 Hash", this.pemMgf1Hash);

        // The OAEP/MGF1 digests only apply to OAEP padding, so only show them then.
        this.pemPadding.addActionListener(e -> updatePemOaepVisibility());
        updatePemOaepVisibility();
    }

    // Show the OAEP/MGF1 hash rows only while OAEP padding is selected.
    private void updatePemOaepVisibility() {
        boolean oaep = RsaCipherBuilder.PADDING_OAEP.equals(this.pemPadding.getSelectedItem());
        setRowVisible(this.pemOaepHash, oaep);
        setRowVisible(this.pemMgf1Hash, oaep);
        validate();
        repaint();
        updateStepPanel();
    }

    private void setRowVisible(Component comp, boolean visible) {
        Component row = comp.getParent();
        Component[] comps = getContentBoxComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == row) {
                comps[i].setVisible(visible);
                if (i + 1 < comps.length) {
                    comps[i + 1].setVisible(visible); // the spacing strut that follows the row
                }
                break;
            }
        }
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