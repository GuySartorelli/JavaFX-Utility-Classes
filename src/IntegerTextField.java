//Variation on answers from https://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx
//mixed with an answer from https://stackoverflow.com/questions/15159988/javafx-2-2-textfield-maxlength
//and then altered to fit my desires

import java.util.regex.Pattern;
import java.util.function.UnaryOperator;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

/**
 * Text field that only accepts integer values as input.
 * Any text pasted into this text field will be stripped of any non-integer characters so that only the integers remain.
 * Input may be optionally restricted to a maximum number of characters and to allow or disallow negative values.
 * @see javafx.scene.control.TextField
 * @author Guy Sartorelli
 *
 */
public class IntegerTextField extends TextField {
    private Pattern integerPattern;
    private Pattern nonIntegerPattern;
    private boolean allowsNegativeValues = true;
    private int maxChars = -1;
    
    /**
     * Creates a TextField with empty text content.
     */
    public IntegerTextField() {
        super();
        this.setupRegex();
        super.setTextFormatter(new TextFormatter<String>(new UnaryOperator<TextFormatter.Change>() {

            @Override
            public Change apply(Change change) {
                if (change.isContentChange()) {
                    String newValue = change.getControlNewText();
                    int newLength = newValue.length();
                    
                    if (!integerPattern.matcher(newValue).matches()) {
                        newValue = nonIntegerPattern.matcher(newValue).replaceAll("");
                        newLength = newValue.length();
                    }
                    if (isNegative()) newLength -= 1;
                    if (maxChars > 0 && newLength > maxChars) {
                        newValue = newValue.substring(0, maxChars);
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
        this.nonIntegerPattern = Pattern.compile(String.format("%s[^\\d]", allowNegativesRegex));
    }
    
    /**
     * Sets the value of the property text.
     * @param input int: Integer representation of initial text content
     * @throws IllegalArgumentException if the textfield does not allow negative values, and the input is a negative integer.
     * @see TextField.setText(String)
     */
    public void setText(int input) throws IllegalArgumentException {
        if (!this.allowsNegativeValues && input < 0) throw new IllegalArgumentException("The IntegerTextField does not allow negative values");
        setText(input);
    }
    
    /**
     * Gets the value of the property text as an int.
     * @return int: the value of the property text as an int
     */
    public int getInt() {
        return Integer.parseInt(this.getText());
    }
    
    /**
     * Returns true if the value of the property text starts with "-".
     * @return boolean: true if the value of the property text starts with "-"
     */
    public boolean isNegative() {
        return (allowsNegativeValues) ? getText().startsWith("-") : false;
    }
    
    /**
     * Sets this field to allow or disallow negative values as input. 
     * @param allow
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
     * Sets the maximum number of characters (excluding hyphen) allowed as input.
     * @param maxChars int: the maximum number of characters (excluding hyphen) allowed as input
     */
    public void setMaxChars(int maxChars) {
        this.maxChars = maxChars;
    }
    
    /**
     * Returns the maximum number of characters (excluding hyphen) allowed as input.
     * @return int: the maximum number of characters (excluding hyphen) allowed as input
     */
    public int getMaxChars() {
        return this.maxChars;
    }
}
