package de.usd.cstchef.operations.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.Utils.MessageType;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.view.ui.VariableTextField;

@OperationInfos(name = "Write File", category = OperationCategory.MISC, description = "Appends data to the end of a file.")
public class WriteFile extends Operation implements ActionListener {

    private final JFileChooser fileChooser = new JFileChooser();
    private VariableTextField fileNameTxt;
    private JCheckBox overwriteCheckbox;
    private String lastPath = "";
    private FileOutputStream out;

    @Override
    protected ByteArray perform(ByteArray input, MessageType messageType) throws Exception {
        String path = fileNameTxt.getText();

        if (!path.isEmpty()) {
            out = new FileOutputStream(path, this.overwriteCheckbox.isSelected());
            out.write(input.getBytes());
            out.write('\n');
            out.close();
        }
        return input;
    }

    public void createUI() {
        this.fileNameTxt = new VariableTextField();
        this.fileNameTxt.setEditable(false);
        this.addUIElement("Filename", this.fileNameTxt);

        JButton chooseFileButton = new JButton("Select file");
        chooseFileButton.addActionListener(this);
        this.addUIElement(null, chooseFileButton, false, "button1");

        this.overwriteCheckbox = new JCheckBox("Append Contents");
        this.overwriteCheckbox.setSelected(true);
        this.addUIElement("Append Contents", overwriteCheckbox);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            this.fileNameTxt.setText(file.getAbsolutePath());
        }
    }

    @Override
    public void onRemove() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

}
