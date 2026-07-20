package de.usd.cstchef.operations.extractors;

import javax.swing.JComboBox;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.view.ui.VariableTextField;

@OperationInfos(name = "Line Extractor", category = OperationCategory.EXTRACTORS, description = "Extracts the specified line number.")
public class LineExtractor extends Operation {

    protected VariableTextField lineNumberField;
    protected JComboBox<String> formatBox;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        int lineNumber = -1;
        try {
            String number = lineNumberField.getText();
            lineNumber += Integer.valueOf(number);
        } catch(Exception e) {
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


        String[] inputLines = input.toString().split(lineBreak);

        if(lineNumber < 0 || lineNumber >= inputLines.length)
            return input;

        return factory.createByteArray(inputLines[lineNumber]);
    }

    @Override
    public void createUI() {
        this.lineNumberField = new VariableTextField();
        this.addUIElement("Line number", this.lineNumberField);
        this.formatBox = new JComboBox<>(new String[] {"\\r\\n", "\\r", "\\n"});
        this.formatBox.setSelectedItem("\\r\\n");
        this.addUIElement("Line break", this.formatBox);
    }

}
