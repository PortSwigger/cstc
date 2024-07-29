package de.usd.cstchef.view.filter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import burp.api.montoya.core.ToolSource;

public class FilterState implements Serializable{
    @JsonDeserialize(keyUsing = FilterStateDeserializer.class)
    private LinkedHashMap<Filter, Boolean> incomingFilterSettings;
    @JsonDeserialize(keyUsing = FilterStateDeserializer.class)
    private LinkedHashMap<Filter, Boolean> outgoingFilterSettings;

    public FilterState(LinkedHashMap<Filter, Boolean> incomingFilterSettings,
            LinkedHashMap<Filter, Boolean> outgoingFilterSettings) {
        this.incomingFilterSettings = incomingFilterSettings;
        this.outgoingFilterSettings = outgoingFilterSettings;
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

    public void setFilterMask(LinkedHashMap<Filter, Boolean> incomingFilterMask,
            LinkedHashMap<Filter, Boolean> outgoingFilterMask) {
        this.incomingFilterSettings = incomingFilterMask;
        this.outgoingFilterSettings = outgoingFilterMask;
    }

    public boolean shouldProcess(BurpOperation operation, ToolSource toolSource) {
        LinkedHashMap<Filter, Boolean> filterSettings;
        int filterMask = 0;
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
            if(filter.getToolType().equals(toolSource.toolType())){
                return entry.getValue() == true;
            }
        }
        return false;
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

    public String toString(){
        return "Incoming: " + this.incomingFilterSettings.toString() + "\nOutgoing: " + this.outgoingFilterSettings.toString();
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
