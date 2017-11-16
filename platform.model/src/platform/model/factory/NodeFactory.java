package platform.model.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRoot;

@SuppressWarnings("unchecked")
public enum NodeFactory
        implements INodeFactory {
    
    INSTANCE;
    
    private final Map<Descriptor<? extends INode>, Class<? extends INode>> classes = new HashMap<>();
    
    @Override
    public <T extends INode> T copy(final T from) {
        return (T) this.create(from.getType(), from.getId(), from.getAttributes(), from.getRoot());
    }
    
    @Override
    public <T extends INode> T create(final Descriptor<T> type, final String id, final Collection<Attribute> attributes, final IRoot root) {
        final Class<T> clazz = (Class<T>) this.classes.get(type);
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getConstructor(Descriptor.class, String.class, Collection.class, IRoot.class)
                    .newInstance(type, id, attributes, root);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Collection<Descriptor<? extends INode>> getSupportedTypes() {
        return this.classes.keySet();
    }
    
    public void registerType(final Descriptor<? extends INode> type, final Class<? extends INode> clazz) {
        Assert.isTrue(this.classes.put(type, clazz) == null, "type already registered"); //$NON-NLS-1$
    }
    
}
