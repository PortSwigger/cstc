package de.usd.cstchef.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import burp.BurpUtils;
import burp.Logger;
import burp.api.montoya.core.BurpSuiteEdition;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.persistence.PersistedObject;
import de.usd.cstchef.view.filter.FilterState;
import de.usd.cstchef.view.filter.FilterState.BurpOperation;
import de.usd.cstchef.view.ui.ButtonTabComponent;
import de.usd.cstchef.view.ui.ButtonType;
import de.usd.cstchef.view.ui.NotificationMenu;

public class View extends JPanel {

    private ArrayList<RecipePanel> recipePanels = new ArrayList<RecipePanel>();

    private JTabbedPane tabbedPane = new JTabbedPane();

    private String[] recipePanelNames = { "Outgoing Requests", "Incoming Responses", "Formatting" };

    private NotificationMenu cstcMenu = new NotificationMenu("CSTC", false);
    private Color defaultMenuForeground;

    public View(){
        this(new FilterState());
    }

    public View(FilterState state) {
        Security.addProvider(new BouncyCastleProvider());

        this.setLayout(new BorderLayout());
        //JTabbedPane tabbedPane = new JTabbedPane();

        recipePanels.add(new RecipePanel(BurpOperation.OUTGOING, recipePanelNames[0]));
        recipePanels.add(new RecipePanel(BurpOperation.INCOMING, recipePanelNames[1]));
        recipePanels.add(new RecipePanel(BurpOperation.FORMAT, recipePanelNames[2]));

        
        ButtonTabComponent.initPopUpMenu(this, tabbedPane);

        for(int i = 0; i < 3; i++) {
            tabbedPane.add(recipePanels.get(i).getRecipeName(), recipePanels.get(i));
        }

        initTabButton(0, ButtonType.NONE, recipePanelNames[0]);
        initTabButton(1, ButtonType.NONE, recipePanelNames[1]);
        initTabButton(2, ButtonType.ADD, recipePanelNames[2]);
        
        tabbedPane.setBackgroundAt(0, getColor(BurpOperation.OUTGOING));
        tabbedPane.setBackgroundAt(1, getColor(BurpOperation.INCOMING));

        this.add(tabbedPane);

        Object[] options = { "Close" };
        JMenuItem openFilterDialogItem = new JMenuItem("Filter Dialog");
        openFilterDialogItem.addActionListener(e -> {
            JOptionPane.showOptionDialog(null, RequestFilterDialog.getInstance(), "Request Filter",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (!BurpUtils.getInstance().getApi().burpSuite().version().edition()
                    .equals(BurpSuiteEdition.COMMUNITY_EDITION)) {
                saveFilterState();
            }
        });

        cstcMenu.add(openFilterDialogItem);
        defaultMenuForeground = cstcMenu.getForeground();

        BurpUtils.getInstance().getApi().userInterface().menuBar().registerMenu(cstcMenu);
    }

    public JTabbedPane getTabbedPane() {
        return this.tabbedPane;
    }

    public void setupTabButtonsAfterRestore() {
        for(int i = recipePanels.size() - 1; i >= 0; i--) {
            if(i == recipePanels.size() - 1) {
                if(i == 2) {
                    initTabButton(i, ButtonType.ADD, recipePanels.get(i).getRecipeName());
                    return;
                }
                else {
                    initTabButton(i, ButtonType.CLOSEANDADD, recipePanels.get(i).getRecipeName());
                }
            }
            else if(i > 2) {
                initTabButton(i, ButtonType.CLOSE, recipePanels.get(i).getRecipeName());
            }
        }
    }

    public void clearRecipePanels() {
        for (RecipePanel recipePanel : recipePanels) {
            BurpUtils.getInstance().getFilterState().removeRecipePanel(recipePanel.getRecipeName(), recipePanel.getOperation());
        }
        recipePanels.clear();
        tabbedPane.removeAll();
        RequestFilterDialog.getInstance().updateFilterSettings();
    }

    public void removeRecipePanel(int i) {
        RecipePanel recipePanel = recipePanels.remove(i);
        BurpUtils.getInstance().getFilterState().removeRecipePanel(recipePanel.getRecipeName(), recipePanel.getOperation());
        RequestFilterDialog.getInstance().updateFilterSettings();
        updateInactiveWarnings();
    }

    public void addRecipePanel(RecipePanel recipePanel) {
        recipePanels.add(recipePanel);
        BurpUtils.getInstance().getFilterState().registerRecipePanel(recipePanel.getRecipeName(), recipePanel.getOperation());
        tabbedPane.add(recipePanel.getRecipeName(), recipePanel);

        tabbedPane.setBackgroundAt(recipePanels.size() - 1, getColor(recipePanel.getOperation()));
        RequestFilterDialog.getInstance().updateFilterSettings();
        updateInactiveWarnings();
    }

    public int getNumOfRecipePanels() {
        return recipePanels.size();
    }

    public RecipePanel getRecipePanelAtIndex(int n) {
        return recipePanels.get(n);
    }

    public List<RecipePanel> getFilterableRecipePanels() {
        return recipePanels.stream()
                .filter(recipePanel -> !recipePanel.getOperation().equals(BurpOperation.FORMAT))
                .collect(Collectors.toList());
    }

    public void initTabButton(int i, ButtonType buttonType, String title) {
        tabbedPane.setTabComponentAt(i,
                 new ButtonTabComponent(this, buttonType, title));
    }

    public Color getColor(BurpOperation operation) {
        if(operation == BurpOperation.OUTGOING) {
            return new Color(0, 255, 255, 75);
        }
        else if(operation == BurpOperation.INCOMING) {
            return new Color(255, 95, 31, 75);
        }
        else {
            return new Color(255, 255, 255, 255);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("CSTC");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        View view = new View();

        frame.setContentPane(view);
        frame.setSize(800, 600);
        frame.setVisible(true);
//        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
    }

    public void updateInactiveWarnings() {
        boolean filterActive = false;

        for(int i = 0; i < recipePanels.size(); i++) {
            if(!recipePanels.get(i).getOperation().equals(BurpOperation.FORMAT)) {
                recipePanels.get(i).showInactiveWarning();
                if(BurpUtils.getInstance().getFilterState().hasActiveFilters(recipePanels.get(i).getOperation(), recipePanels.get(i).getRecipeName())) {
                    recipePanels.get(i).hideInactiveWarning();
                    filterActive = true;
                }
            }
        }

        cstcMenu.setText("CSTC");
        cstcMenu.setNotificationVisible(filterActive);
        cstcMenu.setForeground(filterActive ? new Color(0xff6633) : defaultMenuForeground);
    }

    public void preventRaceConditionOnVariables() {
        for(int i = 0; i < recipePanels.size(); i++) {
            recipePanels.get(i).disableAutobakeIfFilterActive();
        }
    }

    public void saveRecipePanelChanges() {
        if (!BurpUtils.getInstance().getApi().burpSuite().version().edition().equals(BurpSuiteEdition.COMMUNITY_EDITION)) {
            PersistedObject savedState = BurpUtils.getInstance().getApi().persistence().extensionData();
            PersistedList<String> listOfRecipePanels = PersistedList.persistedStringList();
            for(int i = 0; i < recipePanels.size(); i++) {
                listOfRecipePanels.add(getRecipePanelAtIndex(i).getRecipeName());
                listOfRecipePanels.add(getRecipePanelAtIndex(i).getOperation().toString());
                listOfRecipePanels.add(getRecipePanelAtIndex(i).getPersistenceId());
            }
            savedState.setStringList("listOfRecipePanels", listOfRecipePanels);
        }
    }

    public void saveFilterState() {
        PersistedObject savedState = BurpUtils.getInstance().getApi().persistence().extensionData();
        try {
            savedState.setString("FilterState",
                    new ObjectMapper().writeValueAsString(BurpUtils.getInstance().getFilterState()));
        } catch (Exception e) {
            Logger.getInstance().err(
                    "Could not save the filter state to the Burp project. If you are running Burp Suite Community Edition, this behavior is expected since saving project files is exclusive to BurpSuite Pro users.\n"
                            + e.getMessage());
        }
    }
}
