package platform.jetter.model;

import java.util.Collection;

public interface NodeService {
    
    void delete(final Collection<String> ids);
    
    void delete(final String id);
    
    NodeDto getById(final String id);
    
    Collection<NodeDto> getByType(final String type);
    
    void post(final Collection<NodeDto> entities);
    
    void put(final Collection<NodeDto> entities);
    
}
