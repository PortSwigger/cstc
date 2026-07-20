package de.usd.cstchef.view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import burp.BurpUtils;
import de.usd.cstchef.view.filter.Filter;
import de.usd.cstchef.view.filter.FilterState.BurpOperation;

public class RequestFilterDialog extends JPanel {

    private static RequestFilterDialog instance = null;
    private static final List<String> FILTER_LABELS = Arrays.asList("Proxy", "Repeater", "Scanner", "Intruder", "Extender", "Sequencer");
    private static final int PANEL_COLUMN_GAP = 12;

    public static RequestFilterDialog getInstance() {
        if (RequestFilterDialog.instance == null) {
            RequestFilterDialog.instance = new RequestFilterDialog();
        }
        return RequestFilterDialog.instance;
    }

    private RequestFilterDialog() {
        rebuild();
    }

    private void rebuild() {
        List<RecipePanel> filterableRecipePanels = BurpUtils.getInstance().getView().getFilterableRecipePanels();
        this.setLayout(new GridLayout(0, filterableRecipePanels.size() + 1, PANEL_COLUMN_GAP, 0));
        this.removeAll();
        this.add(createLabelPanel());

        for (RecipePanel recipePanel : filterableRecipePanels) {
            this.add(createPanel(recipePanel));
        }

        this.revalidate();
        this.repaint();
    }

    private JPanel createLabelPanel() {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(FILTER_LABELS.size() + 1, 0));
        labelPanel.add(new JLabel(""));
        for (String label : FILTER_LABELS) {
            labelPanel.add(new JLabel(label));
        }

        return labelPanel;
    }

    private JPanel createPanel(RecipePanel recipePanel) {
        BurpOperation operation = recipePanel.getOperation();
        String recipePanelName = recipePanel.getRecipeName();
        LinkedHashMap<Filter, Boolean> filterMask = BurpUtils.getInstance().getFilterState().getFilterMask(operation, recipePanelName);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(FILTER_LABELS.size() + 1, 0));
        panel.add(new JLabel(recipePanelName));
        for (Map.Entry<Filter, Boolean> entry : filterMask.entrySet()) {
            Filter filter = entry.getKey();
            boolean selected = entry.getValue();
            int selectionOrder = BurpUtils.getInstance().getFilterState()
                    .getFilterSelectionOrder(operation, recipePanelName, filter.getToolType());

            JCheckBox box = new JCheckBox();
            box.setSelected(selected);
            box.setText(createSelectionOrderLabel(selectionOrder));
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BurpUtils.getInstance().getFilterState()
                            .updateFilterSelection(operation, recipePanelName, filter.getToolType(), box.isSelected());

                    BurpUtils.getInstance().getView().preventRaceConditionOnVariables();

                    BurpUtils.getInstance().getView().updateInactiveWarnings();
                    rebuild();
                }
            });
            panel.add(box);
        }

        return panel;
    }

    public void updateFilterSettings(){
        rebuild();
    }

    public LinkedHashMap<Filter, Boolean> getFilterMask(BurpOperation operation, String recipePanelName) {
        return BurpUtils.getInstance().getFilterState().getFilterMask(operation, recipePanelName);
    }

    private String createSelectionOrderLabel(int selectionOrder) {
        if (selectionOrder <= 0) {
            return "";
        }

        return Integer.toString(selectionOrder);
    }
}
