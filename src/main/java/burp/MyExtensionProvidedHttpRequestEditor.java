package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import de.usd.cstchef.view.RecipePanel;
import de.usd.cstchef.view.View;
import de.usd.cstchef.view.filter.FilterState;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MyExtensionProvidedHttpRequestEditor implements ExtensionProvidedHttpRequestEditor
{
    private static final int REAPPLY_INTERVAL_MS = 500;

    private final HttpRequestEditor requestEditor;
    private HttpRequestResponse requestResponse;
    private final MontoyaApi api;
    private final View view;
    private final ToolType toolType;
    private final Timer reapplyTimer;
    private ByteArray lastAppliedRequest = ByteArray.byteArray();

    MyExtensionProvidedHttpRequestEditor(EditorCreationContext creationContext, View view)
    {
        this.api = BurpUtils.getInstance().getApi();
        this.view = view;
        this.toolType = creationContext.toolSource().toolType();
        requestEditor = api.userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);

        this.reapplyTimer = new Timer(REAPPLY_INTERVAL_MS, event -> {
            if (shouldRefreshVisibleEditor()) {
                reapplyRecipe();
            }
        });
        this.reapplyTimer.start();
    }

    @Override
    public HttpRequest getRequest()
    {
        return requestResponse.request();
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse)
    {
        this.requestResponse = requestResponse;
        applyBakedRequest(true);
    }

    private void applyBakedRequest(boolean force)
    {
        ByteArray result = bakeRequest();

        if (!force && Arrays.equals(this.lastAppliedRequest.getBytes(), result.getBytes())) {
            return;
        }

        this.lastAppliedRequest = result;
        this.requestEditor.setRequest(HttpRequest.httpRequest(result));
    }

    private ByteArray bakeRequest()
    {
        if (requestResponse == null || requestResponse.request() == null) {
            return ByteArray.byteArray();
        }

        ByteArray originalRequest = requestResponse.request().toByteArray();
        ByteArray result = originalRequest;

        for (RecipePanel recipePanel : getOrderedRecipePanels(FilterState.BurpOperation.OUTGOING, toolType)) {
            result = recipePanel.bake(result, originalRequest);
        }

        return result;
    }

    public void reapplyRecipe()
    {
        if (this.requestResponse == null) {
            return;
        }

        this.applyBakedRequest(false);
    }

    private boolean shouldRefreshVisibleEditor()
    {
        Component editorComponent = requestEditor.uiComponent();
        if (!editorComponent.isDisplayable()) {
            return false;
        }

        if (editorComponent.isShowing()) {
            return true;
        }

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, editorComponent);
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse)
    {
        return requestResponse.request() != null;
    }

    @Override
    public String caption()
    {
        return "CSTC";
    }

    @Override
    public Component uiComponent()
    {
        return requestEditor.uiComponent();
    }

    @Override
    public Selection selectedData()
    {
        return requestEditor.selection().isPresent() ? requestEditor.selection().get() : null;
    }

    @Override
    public boolean isModified()
    {
        return requestEditor.isModified();
    }

    private List<RecipePanel> getOrderedRecipePanels(FilterState.BurpOperation operation, ToolType toolType)
    {
        List<OrderedRecipePanel> orderedRecipePanels = new ArrayList<>();
        int recipeIndex = 0;

        for (RecipePanel recipePanel : view.getFilterableRecipePanels()) {
            if (!recipePanel.getOperation().equals(operation)) {
                recipeIndex++;
                continue;
            }

            int selectionOrder = BurpUtils.getInstance().getFilterState()
                    .getFilterSelectionOrder(operation, recipePanel.getRecipeName(), toolType);
            if (selectionOrder <= 0) {
                recipeIndex++;
                continue;
            }

            orderedRecipePanels.add(new OrderedRecipePanel(recipePanel, selectionOrder, recipeIndex));
            recipeIndex++;
        }

        orderedRecipePanels.sort(Comparator
                .comparingInt(OrderedRecipePanel::getSelectionOrder)
                .thenComparingInt(OrderedRecipePanel::getRecipeIndex));

        List<RecipePanel> sortedRecipePanels = new ArrayList<>();
        for (OrderedRecipePanel orderedRecipePanel : orderedRecipePanels) {
            sortedRecipePanels.add(orderedRecipePanel.getRecipePanel());
        }

        return sortedRecipePanels;
    }

    private static class OrderedRecipePanel
    {
        private final RecipePanel recipePanel;
        private final int selectionOrder;
        private final int recipeIndex;

        private OrderedRecipePanel(RecipePanel recipePanel, int selectionOrder, int recipeIndex)
        {
            this.recipePanel = recipePanel;
            this.selectionOrder = selectionOrder;
            this.recipeIndex = recipeIndex;
        }

        private RecipePanel getRecipePanel()
        {
            return recipePanel;
        }

        private int getSelectionOrder()
        {
            return selectionOrder;
        }

        private int getRecipeIndex()
        {
            return recipeIndex;
        }
    }
}