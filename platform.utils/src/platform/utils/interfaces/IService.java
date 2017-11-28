package platform.utils.interfaces;

import java.util.Collection;

public interface IService<DTO> {
    
    void delete(final Collection<String> ids);
    
    void delete(final String id);
    
    Collection<DTO> getAll();
    
    DTO getById(final String id);
    
    Collection<DTO> getByType(final String type);
    
    void post(final Collection<DTO> dtos);
    
    void put(final Collection<DTO> dtos);
    
}
