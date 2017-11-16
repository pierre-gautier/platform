package platform.model;

import java.util.Collection;
import java.util.List;

public interface IStrategy
        extends IAttributeListener,
        ISelectionListener,
        IRelationListener {
    
    void addAttributes(IObject object, Collection<Attribute> attributes);
    
    void addRelations(INode node, Collection<IRelation> add);
    
    INode find(String id);
    
    <T> T getAttribute(IObject object, Descriptor<T> descriptor, Object value);
    
    void getSelection(List<IRelation> selection);
    
    void removeRelations(INode node, Collection<IRelation> remove);
    
    void setSelection(Collection<? extends IRelation> selection);
    
}
