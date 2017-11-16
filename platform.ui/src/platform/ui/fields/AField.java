package platform.ui.fields;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import platform.ui.fields.validators.ControlValidators;
import platform.ui.fields.validators.ControlValidators.IControlValidator;
import platform.ui.swt.SWTUtils;

public abstract class AField<C extends Control, V>
        implements IField<C, V>,
        DisposeListener,
        Listener {
    
    private static final String MODIFIED_MESSAGE = "this value has been modified"; //$NON-NLS-1$
    private static final int    BOTTOM           = SWT.BOTTOM | SWT.LEFT;
    private static final int    TOP              = SWT.TOP | SWT.LEFT;
    
    private static void register(final Control control, final Listener listener) {
        control.addListener(SWT.Paint, listener);
        control.addListener(SWT.Modify, listener);
        control.addListener(SWT.FocusIn, listener);
        control.addListener(SWT.Selection, listener);
    }
    
    protected final C                  control;
    protected V                        value;
    
    private final IControlValidator<C> validator;
    private final IForm                form;
    
    private Label                      label;
    private boolean                    notifyChange;
    private ControlDecoration          topDecoration;
    private ControlDecoration          bottomDecoration;
    
    protected AField(final C control, final String label, final V value, final IForm form, final IControlValidator<C> validator) {
        super();
        
        this.form = form;
        this.value = value;
        this.control = control;
        
        if (label != null) {
            this.label = SWTUtils.createLabel(control.getParent(), label);
            this.label.moveAbove(this.control);
        }
        
        this.resetDefault();
        this.validator = validator;
        
        if (validator != null) {
            this.bottomDecoration = new ControlDecoration(control, AField.BOTTOM, control.getParent());
            this.bottomDecoration.hide();
            
            this.topDecoration = new ControlDecoration(control, AField.TOP, control.getParent());
            this.topDecoration.setDescriptionText(AField.MODIFIED_MESSAGE);
            this.topDecoration.setImage(ControlValidators.IMAGE_INFO);
            this.topDecoration.hide();
            
        }
        
        AField.register(control, this);
        this.control.addDisposeListener(this);
    }
    
    @Override
    public C getControl() {
        return this.control;
    }
    
    @Override
    public final Label getLabel() {
        return this.label;
    }
    
    @Override
    public void handleEvent(final Event event) {
        if (this.form != null) {
            this.form.setChange(this.control, this.hasChanged());
            this.form.setValidation(this.control, this.validate());
        }
        if (this.validator != null) {
            if (this.validate()) {
                this.bottomDecoration.hide();
            } else {
                this.bottomDecoration.setDescriptionText(this.validator.getMessage());
                this.bottomDecoration.setImage(this.validator.getImage());
                this.bottomDecoration.show();
            }
            if (this.hasChanged() && this.notifyChange) {
                this.topDecoration.show();
            } else {
                this.topDecoration.hide();
            }
        }
    }
    
    @Override
    public void setValue(final V value) {
        Assert.isNotNull(value, "value must not be null"); //$NON-NLS-1$
        this.value = value;
        this.resetDefault();
        this.handleEvent(null);
    }
    
    @Override
    public boolean validate() {
        if (this.validator == null) {
            return true;
        }
        return this.validator.validate(this.control);
    }
    
    @Override
    public void widgetDisposed(final DisposeEvent e) {
        if (this.getLabel() != null && !this.getLabel().isDisposed()) {
            this.getLabel().dispose();
        }
    }
    
    protected final boolean checkControl() {
        return this.getControl() != null && !this.getControl().isDisposed();
    }
}
