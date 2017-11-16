package platform.ui.workbench.properties.model;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import platform.model.Descriptor;
import platform.model.commons.Col;
import platform.model.commons.Img;
import platform.model.commons.Pos;
import platform.model.io.Formatter;
import platform.ui.swt.SWTUtils;
import platform.ui.workbench.properties.IPropertyDescriptor;
import platform.ui.workbench.properties.editors.BooleanCellEditor;
import platform.ui.workbench.properties.editors.ColCellEditor;
import platform.ui.workbench.properties.editors.PosCellEditor;

public class PropertyDescriptor
        extends ColumnLabelProvider
        implements IPropertyDescriptor {
    
    private final Descriptor<?> descriptor;
    private final boolean       isRelation;
    
    public PropertyDescriptor(final Descriptor<?> pd, final boolean isRelation) {
        this.descriptor = pd;
        this.isRelation = isRelation;
    }
    
    @Override
    public CellEditor createPropertyEditor(final Composite parent) {
        if (this.descriptor.getClazz() == String.class) {
            return new TextCellEditor(parent);
        } else if (this.descriptor.getClazz() == Col.class) {
            return new ColCellEditor(parent);
        } else if (this.descriptor.getClazz() == Boolean.class) {
            return new BooleanCellEditor(parent);
        } else if (this.descriptor.getClazz() == Pos.class) {
            return new PosCellEditor(parent, "x", "y", Short.MIN_VALUE, Short.MAX_VALUE); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }
    
    @Override
    public Color getBackground(final Object element) {
        if (element instanceof Col) {
            final Col color = (Col) element;
            return new Color(Display.getCurrent(), color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
        return null;
    }
    
    @Override
    public String getCategory() {
        if (this.isRelation) {
            return "Relation"; //$NON-NLS-1$
        }
        return this.descriptor.getCategory();
    }
    
    @Override
    public String getDescription() {
        return null;
    }
    
    @Override
    public String getDisplayName() {
        return this.descriptor.getLabel();
    }
    
    @Override
    public String[] getFilterFlags() {
        if (this.descriptor.getCategory() == null) {
            return null;
        }
        return new String[] { this.descriptor.getCategory() };
    }
    
    @Override
    public String getId() {
        return (this.isRelation ? "Relation " : "") + this.descriptor.getId(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    public Image getImage(final Object element) {
        if (element instanceof Img) {
            return SWTUtils.getImageFromUrl(((Img) element).getPath(), 16, 16);
        }
        return null;
    }
    
    @Override
    public ILabelProvider getLabelProvider() {
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getText(final Object element) {
        final Class<Object> clazz = (Class<Object>) this.descriptor.getClazz();
        final Formatter<Object> pf = Formatter.getFormatter(clazz);
        if (pf != null) {
            return pf.toString(element);
        }
        return element.toString();
    }
    
    @Override
    public boolean isCompatibleWith(final IPropertyDescriptor anotherProperty) {
        return this.getId().equals(anotherProperty.getId());
    }
    
}
