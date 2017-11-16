package platform.model.factory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRoot;

public enum NodeFactories
        implements INodeFactory {
    
    INSTANCE;
    
    private final Map<Descriptor<? extends INode>, INodeFactory> factories = new HashMap<>(8);
    
    @Override
    public <T extends INode> T copy(final T from) {
        final INodeFactory factory = this.factories.get(from.getType());
        if (factory == null) {
            return NodeFactory.INSTANCE.copy(from);
        }
        return factory.copy(from);
    }
    
    @Override
    public <T extends INode> T create(final Descriptor<T> type, final String id, final Collection<Attribute> attributes, final IRoot root) {
        final INodeFactory factory = this.factories.get(type);
        if (factory == null) {
            return NodeFactory.INSTANCE.create(type, id, attributes, root);
        }
        return factory.create(type, id, attributes, root);
    }
    
    @Override
    public Collection<Descriptor<? extends INode>> getSupportedTypes() {
        return Collections.unmodifiableSet(this.factories.keySet());
    }
    
    public void register(final INodeFactory factory) {
        Assert.isTrue(factory != null && factory.getSupportedTypes() != null, "factory and supported types must not be null"); //$NON-NLS-1$
        for (final Descriptor<? extends INode> supportedType : factory.getSupportedTypes()) {
            this.factories.put(supportedType, factory);
        }
    }
    
}
