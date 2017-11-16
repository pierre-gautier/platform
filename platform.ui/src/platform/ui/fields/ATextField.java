package platform.ui.fields;

import org.eclipse.swt.widgets.Text;

import platform.ui.fields.validators.ControlValidators.IControlValidator;

public abstract class ATextField<T>
        extends AField<Text, T> {
    
    public ATextField(final Text control, final String label, final T value, final IForm form, final IControlValidator<Text> validator) {
        super(control, label, value, form, validator);
    }
    
    @Override
    public final boolean hasChanged() {
        if (!this.checkControl()) {
            return false;
        }
        return !this.control.getText().equals(this.value.toString());
    }
    
    @Override
    public final void resetDefault() {
        if (!this.checkControl()) {
            return;
        }
        this.control.setText(this.value.toString());
    }
    
}
