package platform.model.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;

public enum RelationFactory
        implements IRelationFactory {
    
    INSTANCE;
    
    private final Map<Descriptor<? extends IRelation>, Class<? extends IRelation>> classes = new HashMap<>();
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRelation> T create(final Descriptor<T> type, final String id, final Collection<Attribute> attributes, final INode source, final INode target) {
        final Class<T> clazz = (Class<T>) this.classes.get(type);
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getConstructor(Descriptor.class, String.class, Collection.class, INode.class, INode.class)
                    .newInstance(type, id, attributes, source, target);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Collection<Descriptor<? extends IRelation>> getSupportedTypes() {
        return this.classes.keySet();
    }
    
    public void registerType(final Descriptor<? extends IRelation> type, final Class<? extends IRelation> clazz) {
        Assert.isTrue(this.classes.put(type, clazz) == null, "type already registered"); //$NON-NLS-1$
    }
    
}
