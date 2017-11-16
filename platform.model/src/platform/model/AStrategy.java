package platform.model;

import java.util.Collection;
import java.util.List;

public abstract class AStrategy
        implements IStrategy {
    
    @Override
    public void addAttributes(final IObject object, final Collection<Attribute> values) {
        // to implement
    }
    
    @Override
    public void addRelations(final INode node, final Collection<IRelation> add) {
        // to implement
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        // to implement
    }
    
    @Override
    public INode find(final String id) {
        // to implement
        return null;
    }
    
    @Override
    public <T> T getAttribute(final IObject object, final Descriptor<T> descriptor, final Object value) {
        // to implement
        return null;
    }
    
    @Override
    public void getSelection(final List<IRelation> selection) {
        // to implement
    }
    
    @Override
    public void relationsAdded(final INode node, final Collection<IRelation> added) {
        // to implement
    }
    
    @Override
    public void relationsRemoved(final INode node, final Collection<IRelation> removed) {
        // to implement
    }
    
    @Override
    public void removeRelations(final INode node, final Collection<IRelation> add) {
        // to implement
    }
    
    @Override
    public void selectionChanged(final Object source, final IRoot root) {
        // to implement
    }
    
    @Override
    public void setSelection(final Collection<? extends IRelation> selection) {
        // to implement
    }
    
}
