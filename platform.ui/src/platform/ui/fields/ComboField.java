package platform.ui.fields;

import org.eclipse.swt.widgets.Combo;

import platform.ui.fields.validators.ControlValidators.IControlValidator;

public final class ComboField
        extends AField<Combo, String> {
    
    public ComboField(final Combo control, final String label, final IForm form, final IControlValidator<Combo> validator) {
        this(control, label, null, form, validator);
    }
    
    private ComboField(final Combo control, final String label, final String[] comboValues, final IForm form, final IControlValidator<Combo> validator) {
        super(control, label, control.getText(), form, validator);
        if (comboValues != null) {
            for (final String comboValue : comboValues) {
                control.add(comboValue);
            }
        }
    }
    
    @Override
    public String getValue() {
        if (this.control == null || this.control.isDisposed()) {
            return null;
        }
        return this.control.getText();
    }
    
    @Override
    public boolean hasChanged() {
        if (this.control == null || this.control.isDisposed()) {
            return false;
        }
        return !this.control.getText().equals(this.value);
    }
    
    @Override
    public void resetDefault() {
        if (this.control == null || this.control.isDisposed()) {
            return;
        }
        this.control.setText(this.value);
    }
    
}
