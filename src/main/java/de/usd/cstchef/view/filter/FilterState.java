package de.usd.cstchef.view.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import burp.api.montoya.core.ToolType;

public class FilterState implements Serializable{
    private static final int FILTER_DISABLED_ORDER = 0;

    @JsonDeserialize(keyUsing = FilterStateDeserializer.class)
    private LinkedHashMap<Filter, Boolean> incomingFilterSettings;
    @JsonDeserialize(keyUsing = FilterStateDeserializer.class)
    private LinkedHashMap<Filter, Boolean> outgoingFilterSettings;
    private LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> incomingRecipeFilterSettings;
    private LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> outgoingRecipeFilterSettings;

    public FilterState(LinkedHashMap<Filter, Boolean> incomingFilterSettings,
            LinkedHashMap<Filter, Boolean> outgoingFilterSettings) {
        this.incomingFilterSettings = incomingFilterSettings;
        this.outgoingFilterSettings = outgoingFilterSettings;
        this.incomingRecipeFilterSettings = new LinkedHashMap<String, LinkedHashMap<Filter, Boolean>>();
        this.outgoingRecipeFilterSettings = new LinkedHashMap<String, LinkedHashMap<Filter, Boolean>>();
    }

    public FilterState() {
        this(new LinkedHashMap<Filter, Boolean>(), new LinkedHashMap<Filter, Boolean>());
    }

    public void setFilterMask(LinkedHashMap<Filter, Boolean> filterMask, BurpOperation operation) {
        switch (operation) {
            case INCOMING:
                incomingFilterSettings = filterMask;
                break;
            case OUTGOING:
                outgoingFilterSettings = filterMask;
                break;
            default:
                break;
        }
    }

    public LinkedHashMap<Filter, Boolean> getFilterMask(BurpOperation operation) {
        switch (operation) {
            case INCOMING:
                return incomingFilterSettings;
            case OUTGOING:
                return outgoingFilterSettings;
            default:
                return new LinkedHashMap<Filter, Boolean>();
        }
    }

    public void setFilterMask(String recipePanelName, LinkedHashMap<Filter, Boolean> filterMask, BurpOperation operation) {
        getRecipeFilterSettings(operation).put(recipePanelName, copyFilterMask(filterMask));
    }

    public LinkedHashMap<Filter, Boolean> getFilterMask(BurpOperation operation, String recipePanelName) {
        if (operation == BurpOperation.FORMAT) {
            return new LinkedHashMap<Filter, Boolean>();
        }

        LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> recipeFilterSettings = getRecipeFilterSettings(operation);
        if (!recipeFilterSettings.containsKey(recipePanelName)) {
            recipeFilterSettings.put(recipePanelName, createInitialFilterMask(operation));
        }

        normalizeRecipeFilterSelectionOrders(operation);

        return recipeFilterSettings.get(recipePanelName);
    }

    public void updateFilterSelection(BurpOperation operation, String recipePanelName, ToolType toolType, boolean selected) {
        if (operation == BurpOperation.FORMAT) {
            return;
        }

        LinkedHashMap<Filter, Boolean> filterMask = getFilterMask(operation, recipePanelName);
        Filter filter = getFilter(filterMask, toolType);

        if (filter == null) {
            return;
        }

        if (selected) {
            filter.setValue(getNextSelectionOrder(operation, toolType));
            filterMask.put(filter, true);
            return;
        }

        filterMask.put(filter, false);
        filter.setValue(FILTER_DISABLED_ORDER);
        normalizeRecipeFilterSelectionOrder(operation, toolType);
    }

    public int getFilterSelectionOrder(BurpOperation operation, String recipePanelName, ToolType toolType) {
        LinkedHashMap<Filter, Boolean> filterMask = getFilterMask(operation, recipePanelName);
        Filter filter = getFilter(filterMask, toolType);

        if (filter == null || !Boolean.TRUE.equals(filterMask.get(filter))) {
            return FILTER_DISABLED_ORDER;
        }

        normalizeRecipeFilterSelectionOrder(operation, toolType);
        return filter.getValue();
    }

    public void setFilterMask(LinkedHashMap<Filter, Boolean> incomingFilterMask,
            LinkedHashMap<Filter, Boolean> outgoingFilterMask) {
        this.incomingFilterSettings = incomingFilterMask;
        this.outgoingFilterSettings = outgoingFilterMask;
    }

    public boolean shouldProcess(BurpOperation operation, ToolType toolType) {
        LinkedHashMap<Filter, Boolean> filterSettings;
        switch (operation) {
            case INCOMING:
                filterSettings = incomingFilterSettings;
                break;
            case OUTGOING:
                filterSettings = outgoingFilterSettings;
                break;
            default:
                filterSettings = new LinkedHashMap<>();
                break;
        }

        for (Map.Entry<Filter, Boolean> entry : filterSettings.entrySet()) {
            Filter filter = entry.getKey();
            if(filter.getToolType() == toolType){
                return entry.getValue() == true;
            }
        }
        return false;
    }

    public boolean shouldProcess(BurpOperation operation, ToolType toolType, String recipePanelName) {
        return shouldProcess(getFilterMask(operation, recipePanelName), toolType);
    }

    public boolean hasActiveFilters(BurpOperation operation, String recipePanelName) {
        for (Boolean filterEnabled : getFilterMask(operation, recipePanelName).values()) {
            if (Boolean.TRUE.equals(filterEnabled)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasActiveFilters(BurpOperation operation) {
        for (LinkedHashMap<Filter, Boolean> filterMask : getRecipeFilterSettings(operation).values()) {
            for (Boolean filterEnabled : filterMask.values()) {
                if (Boolean.TRUE.equals(filterEnabled)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void registerRecipePanel(String recipePanelName, BurpOperation operation) {
        if (operation == BurpOperation.FORMAT) {
            return;
        }

        getFilterMask(operation, recipePanelName);
    }

    public void removeRecipePanel(String recipePanelName, BurpOperation operation) {
        if (operation == BurpOperation.FORMAT) {
            return;
        }

        getRecipeFilterSettings(operation).remove(recipePanelName);
    }

    public void renameRecipePanel(String oldRecipePanelName, String newRecipePanelName, BurpOperation operation) {
        if (operation == BurpOperation.FORMAT || oldRecipePanelName.equals(newRecipePanelName)) {
            return;
        }

        LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> recipeFilterSettings = getRecipeFilterSettings(operation);
        LinkedHashMap<Filter, Boolean> filterMask = recipeFilterSettings.remove(oldRecipePanelName);
        if (filterMask == null) {
            filterMask = createInitialFilterMask(operation);
        }
        recipeFilterSettings.put(newRecipePanelName, filterMask);
    }

    public LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> getRecipeFilterMasks(BurpOperation operation) {
        normalizeRecipeFilterSelectionOrders(operation);
        return getRecipeFilterSettings(operation);
    }

    public LinkedHashMap<Filter,Boolean> getIncomingFilterSettings() {
        return this.incomingFilterSettings;
    }

    public void setIncomingFilterSettings(LinkedHashMap<Filter,Boolean> incomingFilterSettings) {
        this.incomingFilterSettings = incomingFilterSettings;
    }

    public LinkedHashMap<Filter,Boolean> getOutgoingFilterSettings() {
        return this.outgoingFilterSettings;
    }

    public void setOutgoingFilterSettings(LinkedHashMap<Filter,Boolean> outgoingFilterSettings) {
        this.outgoingFilterSettings = outgoingFilterSettings;
    }

    public LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> getIncomingRecipeFilterSettings() {
        return getRecipeFilterSettings(BurpOperation.INCOMING);
    }

    public void setIncomingRecipeFilterSettings(
            LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> incomingRecipeFilterSettings) {
        this.incomingRecipeFilterSettings = incomingRecipeFilterSettings;
    }

    public LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> getOutgoingRecipeFilterSettings() {
        return getRecipeFilterSettings(BurpOperation.OUTGOING);
    }

    public void setOutgoingRecipeFilterSettings(
            LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> outgoingRecipeFilterSettings) {
        this.outgoingRecipeFilterSettings = outgoingRecipeFilterSettings;
    }

    public String toString(){
        return "Incoming: " + getRecipeFilterSettings(BurpOperation.INCOMING).toString() + "\nOutgoing: "
                + getRecipeFilterSettings(BurpOperation.OUTGOING).toString();
    }

    private LinkedHashMap<String, LinkedHashMap<Filter, Boolean>> getRecipeFilterSettings(BurpOperation operation) {
        switch (operation) {
            case INCOMING:
                if (incomingRecipeFilterSettings == null) {
                    incomingRecipeFilterSettings = new LinkedHashMap<String, LinkedHashMap<Filter, Boolean>>();
                }
                return incomingRecipeFilterSettings;
            case OUTGOING:
                if (outgoingRecipeFilterSettings == null) {
                    outgoingRecipeFilterSettings = new LinkedHashMap<String, LinkedHashMap<Filter, Boolean>>();
                }
                return outgoingRecipeFilterSettings;
            default:
                return new LinkedHashMap<String, LinkedHashMap<Filter, Boolean>>();
        }
    }

    private LinkedHashMap<Filter, Boolean> createInitialFilterMask(BurpOperation operation) {
        LinkedHashMap<Filter, Boolean> legacyFilterMask = getFilterMask(operation);
        if (legacyFilterMask != null && !legacyFilterMask.isEmpty()) {
            return copyFilterMask(legacyFilterMask);
        }

        return createDefaultFilterMask();
    }

    private LinkedHashMap<Filter, Boolean> createDefaultFilterMask() {
        LinkedHashMap<Filter, Boolean> defaultFilterMask = new LinkedHashMap<Filter, Boolean>();
        List<ToolType> toolTypes = Arrays.asList(
                ToolType.PROXY,
                ToolType.REPEATER,
                ToolType.SCANNER,
                ToolType.INTRUDER,
                ToolType.EXTENSIONS,
                ToolType.SEQUENCER);

        for (ToolType toolType : toolTypes) {
            defaultFilterMask.put(new Filter(toolType, FILTER_DISABLED_ORDER), false);
        }

        return defaultFilterMask;
    }

    private LinkedHashMap<Filter, Boolean> copyFilterMask(LinkedHashMap<Filter, Boolean> filterMask) {
        LinkedHashMap<Filter, Boolean> copy = new LinkedHashMap<Filter, Boolean>();

        for (Map.Entry<Filter, Boolean> entry : filterMask.entrySet()) {
            Filter sourceFilter = entry.getKey();
            copy.put(new Filter(sourceFilter.getToolType(), sourceFilter.getValue()), entry.getValue());
        }

        return copy;
    }

    private boolean shouldProcess(LinkedHashMap<Filter, Boolean> filterSettings, ToolType toolType) {
        for (Map.Entry<Filter, Boolean> entry : filterSettings.entrySet()) {
            Filter filter = entry.getKey();
            if(filter.getToolType() == toolType){
                return entry.getValue() == true;
            }
        }

        return false;
    }

    private Filter getFilter(LinkedHashMap<Filter, Boolean> filterMask, ToolType toolType) {
        for (Filter filter : filterMask.keySet()) {
            if (filter.getToolType() == toolType) {
                return filter;
            }
        }

        return null;
    }

    private int getNextSelectionOrder(BurpOperation operation, ToolType toolType) {
        normalizeRecipeFilterSelectionOrder(operation, toolType);

        int nextSelectionOrder = 1;
        for (LinkedHashMap<Filter, Boolean> filterMask : getRecipeFilterSettings(operation).values()) {
            Filter filter = getFilter(filterMask, toolType);
            if (filter != null && Boolean.TRUE.equals(filterMask.get(filter))) {
                nextSelectionOrder = Math.max(nextSelectionOrder, filter.getValue() + 1);
            }
        }

        return nextSelectionOrder;
    }

    private void normalizeRecipeFilterSelectionOrders(BurpOperation operation) {
        for (ToolType toolType : Arrays.asList(
                ToolType.PROXY,
                ToolType.REPEATER,
                ToolType.SCANNER,
                ToolType.INTRUDER,
                ToolType.EXTENSIONS,
                ToolType.SEQUENCER)) {
            normalizeRecipeFilterSelectionOrder(operation, toolType);
        }
    }

    private void normalizeRecipeFilterSelectionOrder(BurpOperation operation, ToolType toolType) {
        List<FilterOrderEntry> selectedFilters = new ArrayList<FilterOrderEntry>();
        int recipeIndex = 0;

        for (LinkedHashMap<Filter, Boolean> filterMask : getRecipeFilterSettings(operation).values()) {
            Filter filter = getFilter(filterMask, toolType);
            if (filter == null) {
                recipeIndex++;
                continue;
            }

            if (Boolean.TRUE.equals(filterMask.get(filter))) {
                selectedFilters.add(new FilterOrderEntry(filter, filter.getValue(), recipeIndex));
            } else {
                filter.setValue(FILTER_DISABLED_ORDER);
            }

            recipeIndex++;
        }

        selectedFilters.sort(Comparator
                .comparingInt(FilterOrderEntry::getSelectionOrder)
                .thenComparingInt(FilterOrderEntry::getRecipeIndex));

        int normalizedSelectionOrder = 1;
        for (FilterOrderEntry entry : selectedFilters) {
            entry.getFilter().setValue(normalizedSelectionOrder++);
        }
    }

    private static class FilterOrderEntry {
        private final Filter filter;
        private final int selectionOrder;
        private final int recipeIndex;

        private FilterOrderEntry(Filter filter, int selectionOrder, int recipeIndex) {
            this.filter = filter;
            this.selectionOrder = selectionOrder;
            this.recipeIndex = recipeIndex;
        }

        private Filter getFilter() {
            return filter;
        }

        private int getSelectionOrder() {
            return selectionOrder;
        }

        private int getRecipeIndex() {
            return recipeIndex;
        }
    }

    public enum BurpOperation {
        INCOMING,
        OUTGOING,
        FORMAT;

        public String toString(){
            switch(this){
                case INCOMING: return "Incoming";
                case OUTGOING: return "Outgoing";
                case FORMAT: return "Formatting";
                default: return "";
            }
        }
    }
}
