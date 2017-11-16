package platform.ui.fields;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.widgets.Control;

public final class Form
        implements IForm {
    
    private static final int             VALIDATED = 1;
    private static final int             CHANGED   = 1 << 1;
    
    private final Map<Runnable, Integer> trackingRunnableToTrackedStatus;
    private final Map<Control, Integer>  trackingControlToTrackedStatus;
    private final Map<Control, Integer>  trackedControlToTrackedStatus;
    
    private boolean                      changed;
    private boolean                      valid;
    private final boolean                trackValidation;
    private final boolean                trackChange;
    
    public Form() {
        this(true, true);
    }
    
    public Form(final boolean trackChange, final boolean trackValidation) {
        this.trackChange = trackChange;
        this.trackValidation = trackValidation;
        this.trackedControlToTrackedStatus = new HashMap<>(4);
        this.trackingControlToTrackedStatus = new HashMap<>(4);
        this.trackingRunnableToTrackedStatus = new HashMap<>(4);
    }
    
    @Override
    public boolean hasChanged() {
        return !this.trackChange || this.changed;
    }
    
    @Override
    public boolean isValid() {
        return !this.trackValidation || this.valid;
    }
    
    @Override
    public void registerChangeTrackingControl(final Control control) {
        this.registerControl(control, Form.CHANGED);
    }
    
    @Override
    public void registerChangeTrackingRunnable(final Runnable runnable) {
        this.registerRunnable(runnable, Form.CHANGED);
    }
    
    @Override
    public void registerValidationTrackingControl(final Control control) {
        this.registerControl(control, Form.VALIDATED);
    }
    
    @Override
    public void registerValidationTrackingRunnable(final Runnable runnable) {
        this.registerRunnable(runnable, Form.VALIDATED);
    }
    
    @Override
    public void setChange(final Control control, final boolean value) {
        if (control == null) {
            return;
        }
        Integer status = this.trackedControlToTrackedStatus.get(control);
        if (status == null) {
            status = 0;
        }
        if (value) {
            status |= Form.CHANGED;
        } else {
            status &= 0 | Form.VALIDATED;
        }
        control.addDisposeListener(e -> Form.this.trackedControlToTrackedStatus.remove(control));
        this.trackedControlToTrackedStatus.put(control, status);
        this.apply();
    }
    
    @Override
    public void setValidation(final Control control, final boolean value) {
        if (control == null) {
            return;
        }
        Integer status = this.trackedControlToTrackedStatus.get(control);
        if (status == null) {
            status = 0;
        }
        if (value) {
            status |= Form.VALIDATED;
        } else {
            status &= 0 | Form.CHANGED;
        }
        control.addDisposeListener(e -> Form.this.trackedControlToTrackedStatus.remove(control));
        this.trackedControlToTrackedStatus.put(control, status);
        this.apply();
    }
    
    private void apply() {
        
        this.valid = true;
        for (final int status : this.trackedControlToTrackedStatus.values()) {
            if ((status & Form.VALIDATED) != Form.VALIDATED) {
                this.valid = false;
                break;
            }
        }
        
        this.changed = false;
        for (final int change : this.trackedControlToTrackedStatus.values()) {
            this.changed |= (change & Form.CHANGED) == Form.CHANGED;
        }
        
        this.applyToControls();
        this.runRunnables();
    }
    
    private void applyToControls() {
        for (final Entry<Control, Integer> entry : this.trackingControlToTrackedStatus.entrySet()) {
            final Control control = entry.getKey();
            if (control != null && !control.isDisposed()) {
                final int action = entry.getValue();
                final boolean trackingChange = this.trackChange && (action & Form.CHANGED) == Form.CHANGED;
                final boolean trackingValidation = this.trackValidation && (action & Form.VALIDATED) == Form.VALIDATED;
                control.setEnabled((!trackingChange || this.changed) && (!trackingValidation || this.valid));
            }
        }
    }
    
    private void registerControl(final Control control, final int action) {
        if (control == null) {
            return;
        }
        Integer currentAction = this.trackingControlToTrackedStatus.get(control);
        if (currentAction == null) {
            currentAction = 0;
        }
        control.addDisposeListener(e -> Form.this.trackingControlToTrackedStatus.remove(control));
        this.trackingControlToTrackedStatus.put(control, currentAction | action);
        this.apply();
    }
    
    private void registerRunnable(final Runnable runnable, final int action) {
        if (runnable == null) {
            return;
        }
        Integer currentActioin = this.trackingRunnableToTrackedStatus.get(runnable);
        if (currentActioin == null) {
            currentActioin = 0;
        }
        this.trackingRunnableToTrackedStatus.put(runnable, action | currentActioin);
        this.apply();
    }
    
    private void runRunnables() {
        for (final Entry<Runnable, Integer> entry : this.trackingRunnableToTrackedStatus.entrySet()) {
            final Runnable runnable = entry.getKey();
            if (runnable != null) {
                final int action = entry.getValue();
                final boolean trackingChange = (action & Form.CHANGED) == Form.CHANGED;
                final boolean trackingValidation = (action & Form.VALIDATED) == Form.VALIDATED;
                if (!trackingChange || this.changed && !trackingValidation || this.valid) {
                    runnable.run();
                }
            }
        }
    }
    
}
