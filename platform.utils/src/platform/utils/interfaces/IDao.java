package platform.utils.interfaces;

import java.util.Collection;

public interface IDao<MODEL, ID> {
    
    void create(Collection<MODEL> toCreate);
    
    void delete(Collection<MODEL> toDelete);
    
    Collection<MODEL> retrieve(Collection<ID> ids);
    
    void update(Collection<MODEL> toUpdate);
    
}
