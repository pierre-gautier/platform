package platform.ui.fields;

import org.eclipse.swt.widgets.Text;

import platform.ui.fields.validators.ControlValidators.IControlValidator;

public class TextField
        extends ATextField<String> {
    
    public TextField(final Text control, final String label, final String value, final IForm form, final IControlValidator<Text> validator) {
        super(control, label, value, form, validator);
        
    }
    
    @Override
    public String getValue() {
        if (!this.checkControl()) {
            return null;
        }
        return this.control.getText();
    }
    
}
