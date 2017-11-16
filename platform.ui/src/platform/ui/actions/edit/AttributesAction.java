package platform.ui.actions.edit;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.IObject;
import platform.ui.actions.APlatformAction;
import platform.ui.actions.IUndoHandler;

public class AttributesAction
        extends APlatformAction {
    
    private final Collection<Attribute>         attributes;
    private final Collection<? extends IObject> objects;
    
    public AttributesAction(final Collection<? extends IObject> objects, final Collection<Attribute> attrbiutes) {
        super("Create relationType"); //$NON-NLS-1$
        this.attributes = attrbiutes;
        this.objects = objects;
    }
    
    @Override
    public int getAccelerator() {
        return SWT.MOD1 + 'N';
    }
    
    @Override
    protected IUndoHandler internalRun() {
        final Collection<Descriptor<?>> descriptors = new ArrayList<>(this.attributes.size());
        for (final Attribute attribute : this.attributes) {
            descriptors.add(attribute.getDescriptor());
        }
        final IUndoHandler undo = new AttributesUndoHandler(this.objects, descriptors);
        for (final IObject object : this.objects) {
            object.addAttributes(this.attributes);
        }
        return undo;
    }
    
}
