//Variation on answers from https://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx
//mixed with an answer from https://stackoverflow.com/questions/15159988/javafx-2-2-textfield-maxlength
//and then altered to fit my desires

import java.util.regex.Pattern;
import java.math.BigInteger;
import java.util.function.UnaryOperator;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

/**
 * Text field that only accepts integer values as input.
 * Any text pasted into this text field will be stripped of any non-integer characters so that only the integers remain.
 * Input may be optionally restricted to a maximum number of digits and to allow or disallow negative values.
 * <p><b>NOTE</b> calling the setTextFormatter() method on an IntegerTextField will break it, as this implementation uses that method to set up its restrictions.
 * Unfortunately that method is set as final in javafx.scene.control.TextInputControl (from which this class inherits it) so I cannot override it to restrict its usage.</p>
 * @see javafx.scene.control.TextField
 * @author Guy Sartorelli
 *
 */
public class IntegerTextField extends TextField {
    private Pattern integerPattern;
    private Pattern nonIntegerPattern;
    private boolean allowsNegativeValues = true;
    private int maxDigits = -1;
    String minValue = null;
    String maxValue = null;
    
    /**
     * Creates a TextField with empty text content.
     */
    public IntegerTextField() {
        super();
        this.setupRegex();
        super.setTextFormatter(new TextFormatter<String>(new UnaryOperator<TextFormatter.Change>() {

            @Override
            public Change apply(Change change) {
//                change.
                if (change.isContentChange()) {
                    String newValue = change.getControlNewText();
                    int newLength = newValue.length();
                    
                    if (!integerPattern.matcher(newValue).matches()) {
                        newValue = nonIntegerPattern.matcher(newValue).replaceAll("");
                        newLength = newValue.length();
                    }
                    if (isNegative()) newLength -= 1;
                    if (maxDigits > 0 && newLength > maxDigits) {
                        newValue = newValue.substring(0, maxDigits);
                    }
                    change.setText(newValue);
                    change.setRange(0, change.getControlText().length());
                }
                return change;
            }
            
        }));
    }
    
    /**
     * Creates a TextField with initial text content.
     * @param input int: Integer representation of initial text content
     */
    public IntegerTextField(int input) {
        this();
        setText(input);
    }
    
    /**
     * Creates a TextField with initial text content.
     * @param input String: String representation of initial text content
     * @throws IllegalArgumentException if text is not a representation of an integer
     */
    public IntegerTextField(String input) throws IllegalArgumentException {
        this();
        if (!integerPattern.matcher(input).matches()) throw new IllegalArgumentException("Text must be integer only");
        setText(input);
    }
    
    /**
     * Utility method to setup regular expressions for input validation
     */
    private void setupRegex() {
        String allowNegativesRegex = (this.allowsNegativeValues) ? "-?" : "";
        this.integerPattern = Pattern.compile(String.format("%s[0-9]*", allowNegativesRegex));

        String removeHyphensNotStartOfString = (this.allowsNegativeValues) ? "(?<=.)-+|" : "";
        allowNegativesRegex = (this.allowsNegativeValues) ? "\\-" : "";
        this.nonIntegerPattern = Pattern.compile(String.format("%s[^%s0-9]+", removeHyphensNotStartOfString, allowNegativesRegex));
    }
    
    /**
     * Sets the value of the property text.
     * @param input int: Integer representation of initial text content
     * @throws IllegalArgumentException if the textfield does not allow negative values, and the input is a negative value
     * @see javafx.scene.control.TextInputControl#setText
     */
    public void setText(int input) throws IllegalArgumentException {
        if (!this.allowsNegativeValues && input < 0) throw new IllegalArgumentException("This IntegerTextField does not allow negative values");
        setText(String.valueOf(input));
    }
    
    /**
     * Sets the value of the property text.
     * @param input long: Long representation of initial text content
     * @throws IllegalArgumentException if the textfield does not allow negative values, and the input is a negative value
     * @see javafx.scene.control.TextInputControl#setText
     */
    public void setText(long input) throws IllegalArgumentException {
        if (!this.allowsNegativeValues && input < 0) throw new IllegalArgumentException("This IntegerTextField does not allow negative values");
        setText(String.valueOf(input));
    }
    
    /**
     * Sets the value of the property text.
     * @param input BigInteger: BigInteger representation of initial text content
     * @throws IllegalArgumentException if the textfield does not allow negative values, and the input is a negative value
     * @see javafx.scene.control.TextInputControl#setText
     */
    public void setText(BigInteger input) throws IllegalArgumentException {
        String inputString = input.toString();
        if (!this.allowsNegativeValues && inputString.startsWith("-")) throw new IllegalArgumentException("This IntegerTextField does not allow negative values");
        setText(inputString);
    }
    
    /**
     * Gets the value of the property text as an int.
     * @return int: the value of the property text as an int
     * @throws NumberFormatException if the value is greater than Integer.MAX_VALUE or less than Integer.MIN_VALUE
     */
    public int getInt() throws NumberFormatException {
        String value = this.getText();
        if (value.length() == 0 || value.equals("-")) return 0;
        if (value.compareTo(String.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new NumberFormatException("Value of IntegerTextField is greater than Integer.MAX_VALUE: " + value);
        } else if (this.isNegative() && value.compareTo(String.valueOf(Integer.MIN_VALUE)) > 0) {
            throw new NumberFormatException("Value of IntegerTextField is less than Integer.MIN_VALUE: " + value);
        }
        
        return Integer.parseInt(value);
    }
    
    /**
     * Gets the value of the property text as a long.
     * @return long: the value of the property text as a long
     * @throws NumberFormatException if the value is greater than Long.MAX_VALUE or less than Long.MIN_VALUE
     */
    public long getLong() throws NumberFormatException {
        String value = this.getText();
        if (value.length() == 0 || value.equals("-")) return 0;
        if (value.compareTo(String.valueOf(Long.MAX_VALUE)) > 0) {
            throw new NumberFormatException("Value of IntegerTextField is greater than Integer.MAX_VALUE: " + value);
        } else if (this.isNegative() && value.compareTo(String.valueOf(Long.MIN_VALUE)) > 0) {
            throw new NumberFormatException("Value of IntegerTextField is less than Integer.MIN_VALUE: " + value);
        }
        
        return Long.parseLong(value);
    }
    
    /**
     * Gets the value of the property text as a BigInteger.
     * @return BigInteger: the value of the property text as a BigInteger
     * @throws ArithmeticException when the result is out of the range support by BigInteger of -2^Integer.MAX_VALUE (exclusive) to +2^Integer.MAX_VALUE (exclusive)
     */
    public BigInteger getBigInteger() throws ArithmeticException {
        return new BigInteger(getText());
    }
    
    /**
     * Returns true if the value of the property text starts with "-".
     * @return boolean: true if the value of the property text starts with "-"
     */
    public boolean isNegative() {
        return (allowsNegativeValues) ? getText().startsWith("-") : false;
    }
    
    /**
     * Sets this field to allow or disallow negative values as input.<br>
     * If the textfield has a negative value when this method is called and set to false, the hyphen
     * will be removed, resulting in a positive value (e.g. "-500" would become "500").
     * @param allow boolean: allow or disallow negative values as input
     */
    public void setAllowNegativeValues(boolean allow) {
        boolean previouslyAllowedNegativeValues = this.allowsNegativeValues; 
        this.allowsNegativeValues = allow;
        this.setupRegex();
        
        if (previouslyAllowedNegativeValues && getText().startsWith("-")){
            setText(getText().replace("-", ""));
        }
    }
    
    /**
     * Returns true if this text field allows negative values as input.
     * @return boolean: true if this text field allows negative values as input
     */
    public boolean allowsNegativeValues() {
        return this.allowsNegativeValues;
    }
    
    /**
     * Sets the maximum number of digits allowed as input. For inifinite allowed digits, set this to a negative value 
     * @param maxDigits int: the maximum number of digits allowed as input
     */
    public void setMaxDigits(int maxDigits) {
        this.maxDigits = maxDigits;
    }
    
    /**
     * Returns the maximum number of digits allowed as input. A negative value signifies infinite digits are allowed.
     * @return int: the maximum number of digits allowed as input
     */
    public int getMaxDigits() {
        return this.maxDigits;
    }
    
    public void clearMinValueRestriction() {
        this.minValue = null;
    }
    
    public void setMinValueRestriction(int value) {
        this.minValue = String.valueOf(value);
    }
    
    public void setMinValueRestriction(long value) {
        this.minValue = String.valueOf(value);
    }

    public void setMinValueRestriction(BigInteger value) {
        this.minValue = value.toString();
    }

    public void clearMaxValueRestriction() {
        this.maxValue = null;
    }
    
    public void setMaxValueRestriction(int value) {
        this.maxValue = String.valueOf(value);
    }
    
    public void setMaxValueRestriction(long value) {
        this.maxValue = String.valueOf(value);
    }

    public void setMaxValueRestriction(BigInteger value) {
        this.maxValue = value.toString();
    }
}
