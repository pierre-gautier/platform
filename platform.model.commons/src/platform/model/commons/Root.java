package platform.model.commons;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import platform.model.ARoot;
import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.IStrategy;
import platform.model.factory.RelationFactories;

public class Root
        extends ARoot {
    
    public Root(final Descriptor<? extends IRoot> type, final String id, final Collection<Attribute> attributes, final List<IStrategy> strategies) {
        super(type, id, attributes, strategies);
        this.registerSelectionListener(Master.INSTANCE);
        final IRelation relation = RelationFactories.INSTANCE.create(Types.RELATION, null, null, Master.INSTANCE, this);
        if (relation != null) {
            Master.INSTANCE.addRelations(Arrays.asList(relation));
        }
    }
    
    public Root(final String id) {
        this(Types.ROOT, id, Arrays.asList(new Attribute(Descriptors.LABEL, id)), null);
    }
    
}
