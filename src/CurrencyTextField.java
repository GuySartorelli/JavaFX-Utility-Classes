
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

/**
 * TextField that only accepts currency values as input.
 * Any text pasted into this text field will be stripped of any non-currency characters so that only the valid characters remain.
 * <p>Valid characters include the symbol corresponding to the chosen enum value from CurrencyTextField.CurrencySymbol followed by a single digit, then a period, then two more digits.</p>
 * <p>CurrencySymbol values with the appendix <b>_OR_NONE</b> are allowed to either have their corresponding symbol at the front of the text value or
 * have no symbol at all. If a CurrencySymbol without that appendix is used, the corresponding symbol must always be at the front of the text value
 * unless there is no text present.
 * The CurrencySymbol value <b>NONE</b> must not have any currency symbol. The CurrencySymbols <b>ANY</b> and <b>ANY_OR_NONE</b> can have any symbol that corresponds
 * with any valid CurrencySymbol value.</p>
 * <p><b>NOTE</b> calling the setTextFormatter() method on a CurrencyTextField will break it, as this implementation uses that method to set up its restrictions.
 * Unfortunately that method is set as final in javafx.scene.control.TextInputControl (from which this class inherits it) so I cannot override it to restrict its usage.</p>
 * @see javafx.scene.control.TextField
 * @author Guy Sartorelli
 *
 */
public class CurrencyTextField extends TextField {
    /**Enum representation of valid currency symbols used by the CurrencyTextField class
     * @see CurrencyTextField
     * @author Guy Sartorelli
     */
    public enum CurrencySymbol{
        NONE("", ".", true),
        DOLLARS("$", ".", false),
        DOLLARS_OR_NONE("$", ".", true),
        EURO("€",",", false),
        EURO_OR_NONE("€",",", true),
        POUNDS("£", ".", false),
        POUNDS_OR_NONE("£", ".", true),
        YEN("¥", ".", false),
        YEN_OR_NONE("¥", ".", true),
        ANY("\u00A4", ".", false),
        ANY_OR_NONE("\u00A4", ".", true);
        
        private String symbol;
        private String delimiter;
        private boolean isNoneType;
        private static Set<String> symbols = new HashSet<String>();
        
        static {
            for (CurrencySymbol symbol : CurrencySymbol.values()) {
                if (symbol != NONE) symbols.add(symbol.getSymbol());
            }
        }
        
        CurrencySymbol(String symbol, String delimiter, boolean isNoneType) {
            this.symbol = symbol;
            this.delimiter = delimiter;
            this.isNoneType = isNoneType;
        }
        
        /**Returns a String representation of the symbol.<br>
         * In the case of <b>ANY</b> and <b>ANY_OR_NONE</b> the representation is the universal symbol &#x00A4*/
        public String getSymbol() {
            return symbol;
        }
        
        /**Returns the delimiter used to separate decimals for this currency*/
        public String getDelimiter() {
            return delimiter;
        }
        
        /**Returns a Set of String representations of all valid symbols.*/
        public static Set<String> getSymbols() {
            return symbols;
        }
        
        public boolean isNoneType() {
            return isNoneType;
        }
    }
    
    private CurrencySymbol currencySymbol;
    private Pattern CURRENCY_PATTERN;
    private Pattern NON_CURRENCY_PATTERN;
    private int maxDollarDigits = -1;
    
    //TODO add missing maxDollarDigits implementation
    //TODO consider a maxDecimalPlaces for use in accountancy nonsense
    
    /**
     * Default constructor: currency symbol set to CurrencySymbol.ANY_OR_NONE and no default text.
     */
    public CurrencyTextField() {
        this(CurrencySymbol.ANY_OR_NONE);
    }
    
    /**
     * Constructor: currency symbol set to CurrencySymbol.ANY_OR_NONE, default text set.
     * @param text String: default text to display in the text field (must be currency format)
     */
    public CurrencyTextField(String text) {
        this(CurrencySymbol.ANY_OR_NONE);
        if (!CURRENCY_PATTERN.matcher(text).matches()) throw new IllegalArgumentException("Text must be in valid currency format");
        setText(text);
    }
    
    /**
     * Constructor: currency symbol defined by user and no default text.
     * @param symbol CurrencySymbol: the currency symbol used by this text field
     */
    public CurrencyTextField(CurrencySymbol symbol) {
        super();
        this.currencySymbol = symbol;
        setupRegex();
        setTextFormatter(new TextFormatter<String>(new UnaryOperator<TextFormatter.Change>() {

            //formatting on text being altered
            @Override
            public Change apply(Change change) {
                if (change.isContentChange()) {
                    String newValue = change.getControlNewText();
                    if (currencySymbol.getDelimiter().equals(",")) newValue = newValue.replaceAll(".", ",");
                    else newValue = newValue.replaceAll(",", ".");
                    int newLength = newValue.length();
                    
                    if (!CURRENCY_PATTERN.matcher(newValue).matches()) {
                        newValue = NON_CURRENCY_PATTERN.matcher(newValue).replaceAll("");
                        newLength = newValue.length();
                    }
//                    if (maxChars > 0 && newLength > maxChars) {
//                        newValue = newValue.substring(0, maxChars);
//                    }
                    change.setText(newValue);
                    change.setRange(0, change.getControlText().length());
                }
                return change;
            }
        }));
        
        //final formatting on lose focus
        focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> focusProperty, Boolean oldFocusValue, Boolean hasFocus)
            {
                if(!hasFocus)
                {
                    onLoseFocus();
                }
            }
        });
    }
    
    /**
     * Constructor: currency symbol defined by user, default text set.
     * @param symbol CurrencySymbol: the currency symbol used by this text field
     * @param text String: default text to display in the text field (must be currency format)
     */
    public CurrencyTextField(CurrencySymbol symbol, String text) {
        this(symbol);
        if (!CURRENCY_PATTERN.matcher(text).matches()) throw new IllegalArgumentException("Text must be in valid currency format");
        setText(text);
    }
    
    /**
     * Sets up the regular expressions which limit which values can be input to the text field
     */
    private void setupRegex() {
        //the specifics of the regex used is explained at the bottom of this file
        String currencyRegex = "";
        String currencySymbols = "";
        if (currencySymbol == CurrencySymbol.ANY || currencySymbol == CurrencySymbol.ANY_OR_NONE) {
            currencyRegex = "[";
            for (String symbol : CurrencySymbol.getSymbols()) {
                currencyRegex += symbol;
                currencySymbols += symbol;
            }
            currencyRegex += "]";
        } else {
            currencyRegex = currencySymbol.getSymbol();
            currencySymbols = currencySymbol.getSymbol();
        }
        if (currencySymbol != CurrencySymbol.NONE && currencySymbol.isNoneType()) currencyRegex += "?";
        
        currencyRegex += "\\d+(\\"+currencySymbol.getDelimiter()+"\\d{0,2})?";
        String nonCurrencyRegex = String.format("[^.,\\d%s]+", currencySymbols);
        if (currencySymbol != CurrencySymbol.NONE) nonCurrencyRegex += String.format("|(?<=.)[%s]+", currencySymbols);
        nonCurrencyRegex += "|(?<=[,.].{0,20})[.,]+|(?<=[,.]\\d{2}.{0,20})\\d+";
        CURRENCY_PATTERN = Pattern.compile(currencyRegex);
        NON_CURRENCY_PATTERN = Pattern.compile(nonCurrencyRegex, Pattern.DOTALL);
    }
    
    /**
     * Helper method to format the text as a currency when the field loses focus.<br>
     * Adds a currency symbol if required by omitted.<br>
     * Adds a 0 before the decimal if omitted.<br>
     * Adds one or two 0s after the decimal place if less than two 0s are present.<br>
     * e.g. ".4" will be formatted to "$0.40" if currency symbol is set to DOLLARS and to "0.40" if set to DOLLARS_OR_NONE.
     */
    private void onLoseFocus() {
        String currentText = getText();
        if (currentText.length() == 0) return;
        String delimiter = currencySymbol.getDelimiter();
        
        //add currency symbol and 0 before decimal
        if (currencySymbol != CurrencySymbol.NONE && startsWithSymbol()){
            if (currentText.charAt(1) == delimiter.charAt(0)) {
                currentText = currencySymbol.getSymbol() + "0" + currentText.substring(1);
            }
        } else {
            if (currentText.startsWith(delimiter)) {
                currentText = "0" + currentText;
            }
            if (!currencySymbol.isNoneType()) {
                currentText = currencySymbol.getSymbol() + currentText;
            }
        }
        
        //add delimiter and cents if necessary
        if (!currentText.contains(delimiter)) currentText += delimiter + "00";
        else {
            System.out.println(currentText);
            int digitsAfterDecimal = currentText.substring(currentText.indexOf(delimiter)+1).length();
            if (digitsAfterDecimal == 1) currentText += "0";
            if (digitsAfterDecimal == 0) currentText += "00";
        }
        
        setText(currentText);
    }
    
    /**
     * Returns the CurrencySymbol enum value for this text field
     * @return the CurrencySymbol enum value for this text field
     */
    public CurrencySymbol getCurrencySymbol() {
        return currencySymbol;
    }
    
    /**
     * Gets the value of the property text as a double.
     * @return double: the value of the property text as a double 
     */
    public double getDouble() {
        String value = startsWithSymbol() ? getText().replace(currencySymbol.getSymbol(), "") : getText(); 
        return Double.parseDouble(value);
    }
    
    /**
     * Sets the value of the property text.
     * @param text double: the value of the property text
     */
    public void setText(double text) {
        setText(String.valueOf(text));
    }
    
    /**
     * Sets the maximum number of digits allowed before a decimal point (default unlimited)<br>
     * To reset this to an unlimited number, set it to -1.
     * @param chars int: the maximum number of digits allowed before a decimal point
     */
    public void setMaxDollarChars(int chars) {
        if (chars < -1) throw new IllegalArgumentException("Values below -1 are invalid.");
        maxDollarDigits = chars;
    }
    
    /**
     * Returns the maximum number of digits allowed before the decimal place.<br>
     * If no value has been set the value will be -1 which represents an unlimited number of digits are allowed.
     * @return int: the maximum number of digits allowed before the decimal place
     */
    public int getMaxDollarChars() {
        return maxDollarDigits;
    }
    
    /**
     * Returns true if the text starts with the currency symbol chosen for this text field.<br>
     * IF currency symbol is ANY or ANY_OR_NONE returns true if the text starts with any symbol in CurrencySymbol.getSymbols().<br>
     * If currency symbol is NONE, returns false.
     * @return true if text starts with appropriate currency symbol (always false if symbol type is NONE)
     */
    public boolean startsWithSymbol() {
        if (currencySymbol == CurrencySymbol.NONE) return false;
        if (currencySymbol == CurrencySymbol.ANY || currencySymbol == CurrencySymbol.ANY_OR_NONE) {
            return CurrencySymbol.getSymbols().contains(String.valueOf(getText().charAt(0)));
        }
        return getText().startsWith(currencySymbol.getSymbol());
    }
}

/*
REGEX in setupRegex is pretty nuts, so it is explained here.
Note that where in the look-behind I have had to replace the potentially infinite symbols with specific ranges
because Java does not support infinite look-behinds. I have arbitrarily chosen a range of 0-20 which should be more than sufficient
for any copy-pasted values that would be remotely considered reasonable
To check if text IS ALREADY a currency, I use the following:
[$€]?\d+([,.]\d{0,2})?
This says: "match strings where there is zero or one of the currency symbols, followed by at least one digit,
then a period or comma (depending on currency), then between zero and two more digits"

In order to remove characters that are NOT appropriate for a currency string, it gets a little complicated.
I could have done this in four separate steps as follows:
[^.,\d$€]+  catches innapropriate characters
says "match and one or more non-digit, non-comma, non-period, non currency symbol"

(?<=.)[$€]+     catches currency symbols that follow anything (currency symbols should always be first)
says "match one or more currency symbol that follows any character including a new line

(?<=[,.].*)[.,]+      catches commas and periods that are not the first comma or period.
says "match one or more comma or period which follows a comma or period 0 or more of any other character including new lines"

(?<=[,.]\d{2})\d+    catches too many digits after a decimal
says "match any one or more digits preceeded by a comma or period and 2 digits"

OR all in one step:
[^.,\d$€]+|(?<=.)[$€]+|(?<=[,.].*)[.,]+|(?<=[,.]\d{2}.*)\d+
Catches all innapropriate characters, any currency sign that is not the first character, any period or comma that is not the first period or comma,
and any digits that occur after the first two digits that succeed a period or comma.
says "match one or more characters that are not a comma, period, currency symbol, or digit
    OR one or more currency symbol following any character including a new line
    OR one or more period or comma which follows a period or comma and 0 or more of any character including a new line
    OR one or more digits which follow a comma or period and exactly 2 digits and 0 or more of any character including a new line
*/
