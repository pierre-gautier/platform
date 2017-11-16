package platform.model.license;

import java.util.Arrays;
import java.util.Collection;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRoot;
import platform.model.factory.INodeFactory;

public enum LicenseFactory
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
        return (T) new License(id, attributes, root);
    }
    
    @Override
    public Collection<Descriptor<? extends INode>> getSupportedTypes() {
        return Arrays.asList(LicenseDescriptors.LICENSE);
    }
    
}
