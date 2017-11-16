package platform.ui.fields;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;

import platform.model.commons.Col;

public class ColorField
        extends AField<Button, Col> {
    
    private final ColorSelector selector;
    
    public ColorField(final ColorSelector selector, final String label, final Col value, final IForm form) {
        super(selector.getButton(), label, value, form, null);
        this.selector = selector;
        this.resetDefault();
    }
    
    @Override
    public Col getValue() {
        if (!this.checkControl()) {
            return null;
        }
        final RGB rgb = this.selector.getColorValue();
        return new Col(rgb.red, rgb.green, rgb.blue);
    }
    
    @Override
    public boolean hasChanged() {
        final RGB rgb = this.selector.getColorValue();
        return !rgb.equals(new RGB(this.value.getRed(), this.value.getGreen(), this.value.getBlue()));
    }
    
    @Override
    public void resetDefault() {
        if (this.selector != null) {
            this.selector.setColorValue(new RGB(this.value.getRed(), this.value.getGreen(), this.value.getBlue()));
        }
    }
    
}
