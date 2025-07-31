package de.usd.cstchef.operations.datetime;

import java.text.SimpleDateFormat;
import java.util.Date;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.view.ui.VariableTextField;

@OperationInfos(name = "Date Time", category = OperationCategory.DATES, description = DateTime.helpText)
public class DateTime extends Operation {

    private VariableTextField patternTxt;
    private final static String helpText = """
            Returns the current date time formatted with the provided date time pattern.

            G - Era designator

            y - Year

            Y - Week year

            M - Month in year (context sensitive)
            L - Month in year (standalone form)

            w - Week in year
            W - Week in month

            D - Day in year
            d - Day in month
            F - Day of week in month
            E - Day name in week
            u - Day number of week (1 = Monday...)

            a - Am/pm marker

            H - Hour in day (0-23)
            k - Hour in day (1-24)
            K - Hour in am/pm (0-11)
            h - Hour in am/pm (1-12)

            m - Minute in hour

            s - Second in minute

            S - Millisecond
            
            z - Time zone
            Z - Time zone
            X - Time zone

            Example:
            Y-M-d, h:m a    1980-5-17, 3:35 PM
            """;;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        String pattern = this.patternTxt.getText().trim();
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return factory.createByteArray(format.format(new Date()));
    }

    public void createUI() {
        this.patternTxt = new VariableTextField();
        this.addUIElement("Pattern", this.patternTxt);
    }

}
