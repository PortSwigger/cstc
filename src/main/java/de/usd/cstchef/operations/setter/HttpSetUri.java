package de.usd.cstchef.operations.setter;

import javax.swing.JCheckBox;

import burp.BurpUtils;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.Utils.MessageType;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.view.ui.VariableTextField;

@OperationInfos(name = "Set HTTP URI", category = OperationCategory.SETTER, description = "Sets the specified variable as the uri.")
public class HttpSetUri extends Operation {

    private VariableTextField uriTxt;
    private JCheckBox checkbox;

    @Override
    public void createUI() {
        this.uriTxt = new VariableTextField();
        this.addUIElement("Uri", this.uriTxt);

        this.checkbox = new JCheckBox("Keep parameters");
        this.checkbox.setSelected(false);
        this.addUIElement(null, this.checkbox, "checkbox1");
    }

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        MessageType messageType = parseMessageType(input);

        if(messageType == MessageType.RESPONSE) {
            throw new IllegalArgumentException("Input is not a valid HTTP request.");
        }

        if(this.uriTxt.getText().equals("")) {
            return input;
        }

        if(messageType == MessageType.REQUEST){
            try {
                MontoyaApi api = BurpUtils.getInstance().getApi();
    
                int length = input.length();
    
                int firstMark = api.utilities().byteUtils().indexOf(input.getBytes(), " ".getBytes(), false, 0, length);
                int lineMark = api.utilities().byteUtils().indexOf(input.getBytes(), " ".getBytes(), false, firstMark + 1, length);
    
                int secondMark = api.utilities().byteUtils().indexOf(input.getBytes(), "?".getBytes(), false, firstMark + 1, length);
    
                if (!this.checkbox.isSelected() || secondMark < 0 || secondMark >= lineMark) {
                    secondMark = lineMark;
                }
    
                ByteArray method = BurpUtils.subArray(input, 0, firstMark + 1);
                ByteArray newUri = this.uriTxt.getBytes();
                ByteArray rest = BurpUtils.subArray(input, secondMark, length);
    
                ByteArray newRequest = method.withAppended(newUri).withAppended(rest);
    
                return newRequest;
    
            } catch (Exception e) {
                throw new IllegalArgumentException("Input is not a valid request");
            }
        }
        
        return parseRawMessage(input);
        
    }

}
