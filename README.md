# JavaFX-Utility-Classes
A series of classes written to extend JavaFX functionality

### IntegerTextField:
A variation on answers from https://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx mixed with an answer from https://stackoverflow.com/questions/15159988/javafx-2-2-textfield-maxlength and then altered to fit my desires.
A TextField that only accepts integer values as input.
Any text pasted into this text field will be stripped of any non-integer characters so that only the integers remain.
Input may be optionally restricted to a maximum number of digits and to allow or disallow negative values.

### CurrencyTextField:
A TextField that only accepts currency values as input.
Any text pasted into this text field will be stripped of any non-currency characters so that only the valid characters remain.
Valid characters include the symbol corresponding to the chosen enum value from CurrencyTextField.CurrencySymbol followed by a single digit, then a period, then two more digits.

CurrencySymbol values with the appendix **_OR_NONE** are allowed to either have their corresponding symbol at the front of the text value or have no symbol at all. If a CurrencySymbol without that appendix is used, the corresponding symbol must always be at the front of the text value unless there is no text present.
The CurrencySymbol value **NONE** must not have any currency symbol. The CurrencySymbols **ANY** and **ANY_OR_NONE** can have any symbol that corresponds with any valid CurrencySymbol value.