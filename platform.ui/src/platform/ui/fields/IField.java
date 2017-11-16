package platform.ui.fields;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public interface IField<C extends Control, V> {
    
    C getControl();
    
    Label getLabel();
    
    V getValue();
    
    boolean hasChanged();
    
    void resetDefault();
    
    void setValue(V value);
    
    boolean validate();
    
}
