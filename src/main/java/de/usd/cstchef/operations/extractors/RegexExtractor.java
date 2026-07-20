package de.usd.cstchef.operations.extractors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.view.ui.VariableTextField;

@OperationInfos(
    name = "Regex",
    category = OperationCategory.EXTRACTORS,
    description = """
            <html>
              <body>
                <p>
                  More information: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
                </p>

                <h2>Characters</h2>
                <table cellspacing="0" cellpadding="3">
                  <tr><td><code>x</code></td><td>The character x</td></tr>
                  <tr><td><code>\\</code></td><td>The backslash character</td></tr>
                  <tr><td><code>\\t</code></td><td>The tab character ('&#92;u0009')</td></tr>
                  <tr><td><code>\\n</code></td><td>The newline (line feed) character ('&#92;u000A')</td></tr>
                  <tr><td><code>\\r</code></td><td>The carriage-return character ('&#92;u000D')</td></tr>
                  <tr><td><code>\\f</code></td><td>The form-feed character ('&#92;u000C')</td></tr>
                  <tr><td><code>\\a</code></td><td>The alert (bell) character ('&#92;u0007')</td></tr>
                  <tr><td><code>\\e</code></td><td>The escape character ('&#92;u001B')</td></tr>
                  <tr><td><code>\\cx</code></td><td>The control character corresponding to x</td></tr>
                </table>

                <h2>Character classes</h2>
                <table cellspacing="0" cellpadding="3">
                  <tr><td><code>[abc]</code></td><td>a, b, or c (simple class)</td></tr>
                  <tr><td><code>[^abc]</code></td><td>Any character except a, b, or c (negation)</td></tr>
                  <tr><td><code>[a-zA-Z]</code></td><td>a through z or A through Z, inclusive (range)</td></tr>
                  <tr><td><code>[a-d[m-p]]</code></td><td>a through d, or m through p: [a-dm-p] (union)</td></tr>
                  <tr><td><code>[a-z&amp;&amp;[def]]</code></td><td>d, e, or f (intersection)</td></tr>
                  <tr><td><code>[a-z&amp;&amp;[^bc]]</code></td><td>a through z, except for b and c: [ad-z] (subtraction)</td></tr>
                  <tr><td><code>[a-z&amp;&amp;[^m-p]]</code></td><td>a through z, and not m through p: [a-lq-z] (subtraction)</td></tr>
                </table>

                <h2>Predefined character classes</h2>
                <table cellspacing="0" cellpadding="3">
                  <tr><td><code>.</code></td><td>Any character (may or may not match line terminators)</td></tr>
                  <tr><td><code>\\d</code></td><td>A digit: [0-9]</td></tr>
                  <tr><td><code>\\D</code></td><td>A non-digit: [^0-9]</td></tr>
                  <tr><td><code>\\h</code></td><td>A horizontal whitespace character: [ \\t\\xA0&#92;u1680&#92;u180e&#92;u2000-&#92;u200a&#92;u202f&#92;u205f&#92;u3000]</td></tr>
                  <tr><td><code>\\H</code></td><td>A non-horizontal whitespace character: [^\\h]</td></tr>
                  <tr><td><code>\\s</code></td><td>A whitespace character: [ \\t\\n\\x0B\\f\\r]</td></tr>
                  <tr><td><code>\\S</code></td><td>A non-whitespace character: [^\\s]</td></tr>
                  <tr><td><code>\\v</code></td><td>A vertical whitespace character: [\\n\\x0B\\f\\r\\x85&#92;u2028&#92;u2029]</td></tr>
                  <tr><td><code>\\V</code></td><td>A non-vertical whitespace character: [^\\v]</td></tr>
                  <tr><td><code>\\w</code></td><td>A word character: [a-zA-Z_0-9]</td></tr>
                  <tr><td><code>\\W</code></td><td>A non-word character: [^\\w]</td></tr>
                </table>

                <h2>Boundary matchers</h2>
                <table cellspacing="0" cellpadding="3">
                  <tr><td><code>^</code></td><td>The beginning of a line</td></tr>
                  <tr><td><code>$</code></td><td>The end of a line</td></tr>
                  <tr><td><code>\\b</code></td><td>A word boundary</td></tr>
                  <tr><td><code>\\B</code></td><td>A non-word boundary</td></tr>
                  <tr><td><code>\\A</code></td><td>The beginning of the input</td></tr>
                  <tr><td><code>\\G</code></td><td>The end of the previous match</td></tr>
                  <tr><td><code>\\Z</code></td><td>The end of the input but for the final terminator, if any</td></tr>
                  <tr><td><code>\\z</code></td><td>The end of the input</td></tr>
                </table>
              </body>
            </html>
            """
)
public class RegexExtractor extends Operation {

    private static String LIST_MATCHES = "List matches";
    private static String LIST_GROUPS = "List capture groups";

    protected VariableTextField regexTxt;
    protected JComboBox<String> outputBox;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        Pattern p = Pattern.compile(this.regexTxt.getText());
        Matcher m = p.matcher(input.toString());
        String outputType = (String) this.outputBox.getSelectedItem();

        StringBuffer buf = new StringBuffer();

        while (m.find()) {
            if (outputType.equals(LIST_MATCHES)) {
                buf.append(m.group()).append("\n");
            } else {
                for (int i = 1; i <= m.groupCount(); i++) {
                    buf.append(m.group(i)).append("\n");
                }
            }
        }

        if( buf.length() > 0 )
            buf.setLength(buf.length() - 1);

        return factory.createByteArray(buf.toString());
    }

    @Override
    public void createUI() {
        this.regexTxt = new VariableTextField();
        this.addUIElement("Regex", this.regexTxt);

        this.outputBox = new JComboBox<>(new String[] { LIST_MATCHES, LIST_GROUPS });
        this.addUIElement("Output format", this.outputBox);
    }
}
