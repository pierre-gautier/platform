package platform.model;

import java.util.Collection;

public interface IRelationListener {
    
    void relationsAdded(INode node, Collection<IRelation> added);
    
    void relationsRemoved(INode node, Collection<IRelation> removed);
    
}
