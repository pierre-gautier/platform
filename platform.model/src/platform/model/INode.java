package platform.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface INode
        extends IObject {
    
    void addRelations(final Collection<IRelation> relations);
    
    List<IRelation> getRelations();
    
    List<IRelation> getRelations(Set<Descriptor<IRelation>> types);
    
    @Override
    Descriptor<? extends INode> getType();
    
    void registerRelationListener(IRelationListener listener);
    
    void removeRelations(Collection<IRelation> relations);
    
    void unregisterRelationListener(IRelationListener listener);
    
}
