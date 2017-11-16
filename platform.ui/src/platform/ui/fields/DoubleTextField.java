package platform.ui.fields;

import org.eclipse.swt.widgets.Text;

import platform.ui.fields.validators.ControlValidators.IControlValidator;
import platform.utils.Strings;

public final class DoubleTextField
        extends ATextField<Double> {
    
    public DoubleTextField(final Text text, final String label, final Double value, final IForm form, final IControlValidator<Text> validator) {
        super(text, label, value, form, validator);
    }
    
    @Override
    public Double getValue() {
        if (this.control == null || this.control.isDisposed()) {
            return null;
        }
        return Strings.parseDouble(this.control.getText(), this.value);
    }
    
}
