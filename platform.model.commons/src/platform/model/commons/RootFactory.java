package platform.model.commons;

import java.util.Arrays;
import java.util.Collection;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRoot;
import platform.model.factory.INodeFactory;

public enum RootFactory
        implements INodeFactory {
    
    INSTANCE;
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode> T copy(final T from) {
        return (T) this.create(from.getType(), from.getId(), from.getAttributes(), from.getRoot());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode> T create(final Descriptor<T> type, final String id, final Collection<Attribute> attributes, final IRoot root) {
        final Descriptor<IRoot> actualType = (Descriptor<IRoot>) type;
        return (T) new Root(actualType, id, attributes, null);
    }
    
    @Override
    public Collection<Descriptor<? extends INode>> getSupportedTypes() {
        return Arrays.asList(Types.ROOT);
    }
    
}
