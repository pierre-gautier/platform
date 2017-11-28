package platform.ui.actions.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.IObject;
import platform.model.utils.NodeUtils;
import platform.ui.actions.IUndoHandler;
import platform.utils.collections.CollectionsUtils;

public class AttributesUndoHandler
        implements IUndoHandler {
    
    private final Map<IObject, Collection<Attribute>> references;
    private final Collection<Descriptor<?>>           descriptors;
    private final boolean                             removeNotFound;
    
    public AttributesUndoHandler(final Collection<? extends IObject> objects, final Collection<Descriptor<?>> descriptors) {
        this.references = new WeakHashMap<>(objects.size());
        this.descriptors = descriptors;
        this.removeNotFound = CollectionsUtils.isNullOrEmpty(descriptors);
        for (final IObject object : objects) {
            final Collection<Attribute> values;
            if (CollectionsUtils.isNullOrEmpty(descriptors)) {
                values = object.getAttributes();
            } else {
                values = new ArrayList<>(descriptors.size());
                for (final Descriptor<?> descriptor : descriptors) {
                    values.add(Attribute.unchecked(descriptor, object.getAttribute(descriptor)));
                }
            }
            this.references.put(object, values);
        }
    }
    
    public AttributesUndoHandler(final IObject object, final Collection<Descriptor<?>> descriptors) {
        this(Arrays.asList(object), descriptors);
    }
    
    @Override
    public IUndoHandler undo() {
        final Collection<IObject> objects = new ArrayList<>(this.references.size());
        for (final Entry<IObject, Collection<Attribute>> entry : this.references.entrySet()) {
            objects.add(entry.getKey());
        }
        final IUndoHandler undoHandler = new AttributesUndoHandler(objects, this.descriptors);
        for (final Entry<IObject, Collection<Attribute>> entry : this.references.entrySet()) {
            NodeUtils.updateAttributes(entry.getKey(), entry.getValue(), this.removeNotFound);
        }
        return undoHandler;
    }
    
}
