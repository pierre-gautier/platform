package platform.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

public class CustomScale
        implements SelectionListener,
        ModifyListener {
    
    private final Text  text;
    private final Scale scale;
    
    public CustomScale(final Composite parent, final int style) {
        final Composite composite = SWTUtils.createComposite(parent, 2, false, 0, 0);
        composite.setLayoutData(SWTUtils.createGridData(SWT.FILL, SWT.CENTER, SWT.DEFAULT, SWT.DEFAULT));
        this.scale = new Scale(composite, style);
        this.scale.addSelectionListener(this);
        this.text = SWTUtils.createText(composite);
        this.text.setLayoutData(SWTUtils.createGridData(SWT.CENTER, SWT.CENTER, 30, SWT.DEFAULT));
        this.text.addModifyListener(this);
    }
    
    public Scale getScale() {
        return this.scale;
    }
    
    public int getSelection() {
        return this.scale.getSelection();
    }
    
    @Override
    public void modifyText(final ModifyEvent event) {
        try {
            this.scale.setSelection(Integer.parseInt(this.text.getText()));
        } catch (final NumberFormatException e) {
            this.scale.setSelection(0);
        }
    }
    
    public void setEnabled(final boolean enabled) {
        this.scale.setEnabled(enabled);
        this.text.setEnabled(enabled);
    }
    
    public void setIncrement(final int increment) {
        this.scale.setIncrement(increment);
    }
    
    public void setLayoutData(final Object layoutData) {
        this.scale.setLayoutData(layoutData);
    }
    
    public void setMaximum(final int max) {
        this.scale.setMaximum(max);
    }
    
    public void setMinimum(final int min) {
        this.scale.setMinimum(min);
    }
    
    public void setPageIncrement(final int pageIncrement) {
        this.scale.setPageIncrement(pageIncrement);
    }
    
    public void setSelection(final int selection) {
        this.text.setText(String.valueOf(selection));
        this.scale.setSelection(selection);
    }
    
    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
        this.text.setText(String.valueOf(this.scale.getSelection()));
    }
    
    @Override
    public void widgetSelected(final SelectionEvent e) {
        this.text.setText(String.valueOf(this.scale.getSelection()));
    }
    
}
