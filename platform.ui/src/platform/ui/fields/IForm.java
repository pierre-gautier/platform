package platform.ui.fields;

import org.eclipse.swt.widgets.Control;

public interface IForm {
    
    boolean hasChanged();
    
    boolean isValid();
    
    void registerChangeTrackingControl(Control control);
    
    void registerChangeTrackingRunnable(Runnable runnable);
    
    void registerValidationTrackingControl(Control control);
    
    void registerValidationTrackingRunnable(Runnable runnable);
    
    void setChange(Control control, boolean value);
    
    void setValidation(Control control, boolean value);
    
}
