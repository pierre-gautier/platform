package platform.ui.workbench.properties.editors;

import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import platform.model.commons.Col;

public class ColCellEditor
        extends ColorCellEditor {
    
    public ColCellEditor(final Composite parent) {
        super(parent);
    }
    
    @Override
    protected Object doGetValue() {
        final RGB rgb = (RGB) super.doGetValue();
        return new Col(rgb.red, rgb.green, rgb.blue);
    }
    
    @Override
    protected void doSetValue(final Object value) {
        if (value instanceof Col) {
            final Col col = (Col) value;
            super.doSetValue(new RGB(col.getRed(), col.getGreen(), col.getBlue()));
        } else {
            super.doSetValue(value);
        }
    }
    
    @Override
    protected Object openDialogBox(final Control cellEditorWindow) {
        final ColorDialog dialog = new ColorDialog(cellEditorWindow.getShell());
        Object value = this.getValue();
        if (value != null) {
            final Col col = (Col) value;
            dialog.setRGB(new RGB(col.getRed(), col.getGreen(), col.getBlue()));
        }
        value = dialog.open();
        if (value != null) {
            final RGB rgb = (RGB) value;
            value = new Col(rgb.red, rgb.green, rgb.blue);
        }
        return dialog.getRGB();
    }
    
    @Override
    protected void updateContents(final Object value) {
        if (value instanceof Col) {
            final Col col = (Col) value;
            super.updateContents(new RGB(col.getRed(), col.getGreen(), col.getBlue()));
        } else {
            super.updateContents(value);
        }
    }
}
