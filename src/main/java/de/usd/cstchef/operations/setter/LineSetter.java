package de.usd.cstchef.operations.setter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.view.ui.VariableTextArea;
import de.usd.cstchef.view.ui.VariableTextField;

@OperationInfos(name = "Line Setter", category = OperationCategory.SETTER, description = "Sets a line to the specified value.")
public class LineSetter extends SetterOperation {

    private VariableTextField lineNumberField;
    private VariableTextArea content;
    private JCheckBox append;
    private JComboBox<String> formatBox;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        int lineNumber = -1;
        try {
            String number = lineNumberField.getText();
            lineNumber += Integer.valueOf(number);
        } catch( Exception e ) {
            return input;
        }

        String lineBreak = "\\r\\n";
        switch ((String) this.formatBox.getSelectedItem()) {
        case "\\r\\n":
            lineBreak = "\\r\\n";
            break;
        case "\\r":
            lineBreak = "\\r";
            break;
        case "\\n":
            lineBreak = "\\n";
            break;
        }

        List<String> inputLines = new ArrayList<String>();
        for(String str : input.toString().split(lineBreak)) {
            inputLines.add(str);
        }

        if(lineNumber < 0 || lineNumber >= inputLines.size())
            return input;

        if(!append.isSelected()) {
            inputLines.set(lineNumber, content.getText());
        }
        else {
            if(lineNumber == -1) {
                lineNumber = 0;
            }
            inputLines.add(lineNumber, content.getText());
        }

        String result;
        if(lineBreak.equals("\r\n")) {
            result = String.join("\r\n", inputLines);
        }
        else if(lineBreak.equals("\r")) {
            result = String.join("\r", inputLines);
        }
        else {
            result = String.join("\n", inputLines);
        }

        return factory.createByteArray(result);
    }

    @Override
    public void createUI() {
        this.lineNumberField = new VariableTextField();
        this.addUIElement("Line number", this.lineNumberField);
        this.append = new JCheckBox("Insert at");
        this.append.setSelected(false);
        this.addUIElement(null, this.append, "checkbox1");
        this.content = new VariableTextArea();
        this.addUIElement("Content", this.content);

        this.formatBox = new JComboBox<>(new String[] {"\\r\\n", "\\r", "\\n"});
        this.formatBox.setSelectedItem("\\r\\n");
        this.addUIElement("Line break", this.formatBox);
    }

}
