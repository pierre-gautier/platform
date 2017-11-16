package platform.ui.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import platform.ui.fields.validators.ControlValidators.IControlValidator;
import platform.ui.swt.SWTUtils;

public abstract class TextWithButtonField
        extends ATextField<String>
        implements SelectionListener {
    
    public TextWithButtonField(final Composite parent, final String label, final String buttonLabel, final String value, final IForm form, final IControlValidator<Text> validator) {
        super(SWTUtils.createText(SWTUtils.createComposite(parent, 2, false, 0, 0)), null, value, form, validator);
        final Composite textParent = this.getControl().getParent();
        SWTUtils.createLabel(parent, label).moveAbove(textParent);
        SWTUtils.createButtonPush(textParent, SWT.END, SWT.CENTER, buttonLabel, this);
    }
    
    @Override
    public String getValue() {
        if (!this.checkControl()) {
            return null;
        }
        return this.control.getText();
    }
    
    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
        this.widgetSelected(e);
    }
    
    @Override
    public void widgetSelected(final SelectionEvent e) {
        final String filePath = this.getButtonValue();
        if (filePath != null) {
            this.getControl().setText(filePath);
        }
    }
    
    protected abstract String getButtonValue();
    
}
