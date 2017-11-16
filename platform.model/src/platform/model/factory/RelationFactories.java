package platform.model.factory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;

public enum RelationFactories
        implements IRelationFactory {
    
    INSTANCE;
    
    private final Map<Descriptor<? extends IRelation>, IRelationFactory> factories = new HashMap<>(8);
    
    @Override
    public <T extends IRelation> T create(final Descriptor<T> type, final String id, final Collection<Attribute> attributes, final INode source, final INode target) {
        final IRelationFactory factory = this.factories.get(type);
        if (factory == null) {
            return RelationFactory.INSTANCE.create(type, id, attributes, source, target);
        }
        return factory.create(type, id, attributes, source, target);
    }
    
    @Override
    public Set<Descriptor<? extends IRelation>> getSupportedTypes() {
        return Collections.unmodifiableSet(this.factories.keySet());
    }
    
    public void register(final IRelationFactory factory) {
        Assert.isTrue(factory != null && factory.getSupportedTypes() != null, "factory and supported types must not be null"); //$NON-NLS-1$
        for (final Descriptor<? extends IRelation> supportedType : factory.getSupportedTypes()) {
            this.factories.put(supportedType, factory);
        }
    }
    
}
