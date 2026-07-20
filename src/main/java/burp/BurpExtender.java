package burp;

import com.fasterxml.jackson.databind.ObjectMapper;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.BurpSuiteEdition;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.persistence.PersistedObject;
import de.usd.cstchef.view.RecipePanel;
import de.usd.cstchef.view.RequestFilterDialog;
import de.usd.cstchef.view.View;
import de.usd.cstchef.view.filter.FilterState;
import de.usd.cstchef.view.filter.FilterState.BurpOperation;
import de.usd.cstchef.view.ui.ButtonTabComponent;

public class BurpExtender implements BurpExtension {

    private final String extensionName = "CSTC";
    private View view;

    @Override
    public void initialize(MontoyaApi api) {
        BurpUtils.getInstance().init(api);
        this.view = new View();
        BurpUtils.getInstance().setView(view);
        api.extension().setName(extensionName);
        api.userInterface().registerContextMenuItemsProvider(new CstcContextMenuItemsProvider(api, view));
        api.http().registerHttpHandler(new CstcHttpHandler(view));
        api.userInterface().registerSuiteTab(extensionName, view);
        api.userInterface().registerHttpRequestEditorProvider(new MyHttpRequestEditorProvider(view));
        api.userInterface().registerHttpRequestEditorProvider(new MyHttpRequestEditorProviderFormatting(view));
        api.userInterface().registerHttpResponseEditorProvider(new MyHttpResponseEditorProviderFormatting(view));

        if (!api.burpSuite().version().edition().equals(BurpSuiteEdition.COMMUNITY_EDITION)) {
            PersistedObject persistence = api.persistence().extensionData();
            restoreRecipePanels(persistence);
            restoreInput(persistence);
            restoreRecipe(persistence);
            restoreFilterState(persistence);
        }
        view.updateInactiveWarnings();
    }

    private void restoreInput(PersistedObject persistence) {
        try {
            for(int i = 0; i < view.getNumOfRecipePanels(); i++) {
                RecipePanel recipePanel = view.getRecipePanelAtIndex(i);
                String input = persistence.getString(recipePanel.getPersistedInputKey());
                if (input == null) {
                    BurpOperation operation = recipePanel.getOperation();
                    input = persistence.getString(operation + "-Input");
                }
                recipePanel.restoreInput(input);
            }
        } catch (Exception e) {
            Logger.getInstance().log(
                    "Could not restore the input for one or multiple panels. If this is the first time using CSTC in a project, you can ignore this message.");
        }
    }

    private void restoreRecipe(PersistedObject persistence) {
        try {
            for(int i = 0; i < view.getNumOfRecipePanels(); i++) {
                RecipePanel recipePanel = view.getRecipePanelAtIndex(i);
                String recipe = persistence.getString(recipePanel.getPersistedRecipeKey());
                if (recipe == null) {
                    BurpOperation operation = recipePanel.getOperation();
                    recipe = persistence.getString(operation + "-Recipe");
                }

                if (recipe == null) {
                    continue;
                }

                recipePanel.restoreState(recipe);
            }
        } catch (Exception e) {
            Logger.getInstance().log(
                    "Could not restore the recipe for one or multiple panels. If this is the first time using CSTC in a project, you can ignore this message.");
        }
    }

    private void restoreFilterState(PersistedObject persistence) {
        try {
            BurpUtils.getInstance().setFilterState(new ObjectMapper().readValue(persistence.getString("FilterState"), FilterState.class));
            RequestFilterDialog.getInstance().updateFilterSettings();
            view.preventRaceConditionOnVariables();
        } catch (Exception e) {
            Logger.getInstance().log(
                    "Could not restore the filter state. If this is the first time using CSTC in a project, you can ignore this message. ");
        }
    }

    private void restoreRecipePanels(PersistedObject persistence) {
        try {
            PersistedList<String> listOfRecipePanels = persistence.getStringList("listOfRecipePanels");
            if(listOfRecipePanels == null) {
                throw new NullPointerException("listOfRecipePanels is null.");
            }
            view.clearRecipePanels();
            
            int step = listOfRecipePanels.size() % 3 == 0 ? 3 : 2;
            for(int i = 0; i < listOfRecipePanels.size() - (step - 1); i += step) {
                String operation = listOfRecipePanels.get(i + 1);
                BurpOperation burpOperation = operation.equals("Outgoing") ? BurpOperation.OUTGOING : operation.equals("Incoming") ? BurpOperation.INCOMING : BurpOperation.FORMAT;
                if (step == 3) {
                    view.addRecipePanel(new RecipePanel(burpOperation, listOfRecipePanels.get(i), listOfRecipePanels.get(i + 2)));
                } else {
                    view.addRecipePanel(new RecipePanel(burpOperation, listOfRecipePanels.get(i)));
                }
            }

            ButtonTabComponent.updateIndexOfLastComp(view.getNumOfRecipePanels() - 1);
            view.setupTabButtonsAfterRestore();
        } catch (Exception e) {
            Logger.getInstance().log(
                    "Could not restore all recipe panels. If this is the first time using CSTC in a project, you can ignore this message.");
        }
    }
}
