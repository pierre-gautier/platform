package platform.ui.fields;

import org.eclipse.swt.widgets.Scale;

public class ScaleField
        extends AField<Scale, Integer> {
    
    public ScaleField(final Scale scale, final String label, final Integer value, final IForm form) {
        super(scale, label, value, form, null);
    }
    
    @Override
    public Integer getValue() {
        if (this.control == null || this.control.isDisposed()) {
            return null;
        }
        return this.control.getSelection();
    }
    
    @Override
    public boolean hasChanged() {
        if (this.control == null || this.control.isDisposed()) {
            return false;
        }
        return !this.value.equals(this.control.getSelection());
    }
    
    @Override
    public void resetDefault() {
        if (this.control == null || this.control.isDisposed()) {
            return;
        }
        this.control.setSelection(this.value);
    }
    
}
