package platform.ui.fields;

import org.eclipse.swt.widgets.Button;

import platform.ui.fields.validators.ControlValidators.IControlValidator;

public final class CheckField
        extends AField<Button, Boolean> {
    
    public CheckField(final Button control, final String label, final Boolean value, final IForm form, final IControlValidator<Button> validator) {
        super(control, label, value != null ? value : Boolean.FALSE, form, validator);
    }
    
    @Override
    public Boolean getValue() {
        if (this.control == null || this.control.isDisposed()) {
            return false;
        }
        return this.control.getSelection();
    }
    
    @Override
    public boolean hasChanged() {
        if (this.control == null || this.control.isDisposed()) {
            return false;
        }
        return this.value != this.control.getSelection();
    }
    
    @Override
    public void resetDefault() {
        if (this.control == null || this.control.isDisposed()) {
            return;
        }
        this.control.setSelection(this.value);
    }
    
}
