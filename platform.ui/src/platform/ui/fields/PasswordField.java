package platform.ui.fields;

import org.eclipse.swt.widgets.Text;

import platform.ui.fields.validators.ControlValidators.IControlValidator;

public final class PasswordField
        extends TextField {
    
    public PasswordField(final Text text, final String label, final String value, final IForm form, final IControlValidator<Text> validator) {
        super(text, label, value, form, validator);
    }
    
}
