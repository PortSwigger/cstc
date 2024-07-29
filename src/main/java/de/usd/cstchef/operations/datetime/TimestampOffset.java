package de.usd.cstchef.operations.datetime;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner.stdDSA;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.Utils.MessageType;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.view.ui.VariableTextField;

@OperationInfos(name = "Unix Timestamp Offset", category = OperationCategory.DATES, description = "Returns a Epoch timestamp shifted into future or past.")
public class TimestampOffset extends Operation {

    private VariableTextField offsetTxt;
    private JComboBox<String> interval;
    private JCheckBox toPast;
    private JCheckBox milliseconds;

    @Override
    protected ByteArray perform(ByteArray input, MessageType messageType) throws Exception {        
        
        String interval = (String)this.interval.getSelectedItem();
        TimeOffsets intervalEnum = TimeOffsets.getEnumObj(interval);
        int offset = Integer.parseInt(offsetTxt.getText());
        boolean milliseconds = this.milliseconds.isSelected();
             
        
    	long calculatedOffset = this.getOffset(intervalEnum, offset, milliseconds);
    	long timestamp = 0;
    	if(milliseconds) {
    		timestamp = System.currentTimeMillis();
    	}
    	else {
    		timestamp = System.currentTimeMillis() / 1000L;
    	}
    	
    	long shiftedTimestamp = 0;
        if(this.toPast.isSelected()) {
        	shiftedTimestamp = timestamp - calculatedOffset;       	
        }
        else {
        	shiftedTimestamp = timestamp + calculatedOffset;
        }
        
        return factory.createByteArray(String.valueOf(shiftedTimestamp));
    }
    
    private long getOffset(TimeOffsets interval, int offset, boolean milliseconds) {
    	int unitOffset = 0;
    	switch(interval) {
    		case SECONDS:
    			unitOffset = 1;
    			break;
    		case MINUTES:
    			unitOffset = 60;
    			break;
    		case HOURS:
    			unitOffset = 60 * 60;
    			break;
    		case DAYS:
    			unitOffset = 60 * 60 * 24;
    			break;
    		case WEEKS:
    			unitOffset = 60 * 60 * 24 * 7;
    			break;
    		default:
    			throw new IllegalArgumentException("Unkown interval");    			
    	}
    	
    	return milliseconds ? (unitOffset * offset * 1000) : (unitOffset * offset);
    }

    public void createUI() {
        this.offsetTxt = new VariableTextField();
        this.addUIElement("Offset", this.offsetTxt);
        
        this.interval = new JComboBox<String>();
        this.interval.addItem(TimeOffsets.getName(TimeOffsets.SECONDS));
        this.interval.addItem(TimeOffsets.getName(TimeOffsets.MINUTES));
        this.interval.addItem(TimeOffsets.getName(TimeOffsets.HOURS));
        this.interval.addItem(TimeOffsets.getName(TimeOffsets.DAYS));
        this.interval.addItem(TimeOffsets.getName(TimeOffsets.WEEKS));
        this.interval.setSelectedIndex(1);
        this.addUIElement("Interval", this.interval);
        
        this.toPast = new JCheckBox();
        this.toPast.setSelected(true);
        this.toPast.setToolTipText("If checked the timestamp is shifted to the past, otherwise to the future.");
        this.addUIElement("Shift to past", this.toPast);
        
        this.milliseconds = new JCheckBox();
        this.milliseconds.setSelected(false);
        this.milliseconds.setToolTipText("If set the timestamp is returned in the milliseconds format. Otherwise in seconds format.");
        this.addUIElement("Milliseconds", this.milliseconds);
    }
    
    private enum TimeOffsets{
    	SECONDS, MINUTES, HOURS, DAYS, WEEKS;
    	
    	public static String getName(TimeOffsets offsets) {
    		switch(offsets) {
    			case SECONDS:
    				return "Seconds";
    			case MINUTES:
    				return "Minutes";
    			case HOURS:
    				return "Hours";
    			case DAYS:
    				return "Days";
    			case WEEKS:
    				return "Weeks";
    			default:
    				return "";
    		}
    	}
    	
    	public static TimeOffsets getEnumObj(String timeOffsetStr) {
    		if(timeOffsetStr.equalsIgnoreCase(TimeOffsets.getName(TimeOffsets.SECONDS))) {
    			return TimeOffsets.SECONDS;
    		}
    		else if (timeOffsetStr.equalsIgnoreCase(TimeOffsets.getName(TimeOffsets.MINUTES))) {
    			return TimeOffsets.MINUTES;
    		}
    		else if (timeOffsetStr.equalsIgnoreCase(TimeOffsets.getName(TimeOffsets.HOURS))) {
    			return TimeOffsets.HOURS;
    		}
    		else if (timeOffsetStr.equalsIgnoreCase(TimeOffsets.getName(TimeOffsets.DAYS))) {
    			return TimeOffsets.DAYS;
    		}
    		else if (timeOffsetStr.equalsIgnoreCase(TimeOffsets.getName(TimeOffsets.WEEKS))) {
    			return TimeOffsets.WEEKS;
    		}
    		else {
    			throw new IllegalArgumentException("Unkown Unit");
    		}
    	}
    	
    }

}
