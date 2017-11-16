package platform.ui.fields;

import org.eclipse.swt.widgets.Text;

import platform.ui.fields.validators.ControlValidators.IControlValidator;
import platform.utils.Strings;

public final class IntegerTextField
        extends ATextField<Integer> {
    
    public IntegerTextField(final Text text, final String label, final Integer value, final IForm form, final IControlValidator<Text> validator) {
        super(text, label, value, form, validator);
    }
    
    @Override
    public Integer getValue() {
        if (this.control == null || this.control.isDisposed()) {
            return null;
        }
        return Strings.parseInt(this.control.getText(), this.value);
    }
    
}
