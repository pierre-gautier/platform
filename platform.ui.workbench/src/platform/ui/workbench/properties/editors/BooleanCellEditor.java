package platform.ui.workbench.properties.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class BooleanCellEditor
        extends ComboBoxCellEditor {
    
    private static String[] VALUES = { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
    private CCombo          localReference;
    
    public BooleanCellEditor(final Composite parent) {
        super(parent, BooleanCellEditor.VALUES);
    }
    
    @Override
    protected Control createControl(final Composite parent) {
        this.localReference = (CCombo) super.createControl(parent);
        return this.localReference;
    }
    
    @Override
    protected Object doGetValue() {
        return Boolean.valueOf(this.localReference.getText());
    }
    
    @Override
    protected void doSetValue(final Object value) {
        Assert.isTrue(this.localReference != null && value instanceof Boolean);
        this.localReference.select(Boolean.TRUE ? 0 : 1);
    }
    
}
