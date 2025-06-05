package de.usd.cstchef.operations.utils;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(name = "Jump Operation", category = OperationCategory.UTILS, description = "Skip an amount of operations and/or lanes.")
public class UnconditionalJump extends Operation {

    private JSpinner operations;
    private JSpinner lanes;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        // The stop of the recipe execution is implemented in doBake in RecipePanel
        this.setOperationSkip((int)operations.getValue());
        this.setLaneSkip((int)lanes.getValue());
        return input;
    }

    @Override
    public void createUI() {
        super.createUI();
        this.operations = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        this.addUIElement("# Operations", this.operations);
        this.lanes = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        this.addUIElement("# Lanes", this.lanes);
    }

}
