package platform.model;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;

public abstract class ARelation
        extends AObject
        implements IRelation {
    
    private final INode source;
    private final INode target;
    
    protected ARelation(final Descriptor<? extends IRelation> type, final INode source, final INode target) {
        this(type, null, null, source, target);
    }
    
    protected ARelation(final Descriptor<? extends IRelation> type, final String id, final Collection<Attribute> attributes, final INode source, final INode target) {
        super(type, id, attributes, source.getRoot());
        Assert.isTrue(target != null && target != source && !target.equals(source), "target must not be null, same or equals to source"); //$NON-NLS-1$
        this.source = source;
        this.target = target;
    }
    
    @Override
    public final INode getSource() {
        return this.source;
    }
    
    @Override
    public final INode getTarget() {
        return this.target;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Descriptor<? extends IRelation> getType() {
        return (Descriptor<? extends IRelation>) super.getType();
    }
    
}
