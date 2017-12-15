package platform.utils.interfaces;

public interface IService<DTO> {
    
    // void delete(final Collection<String> id);
    
    void delete(final String id);
    
    DTO getById(final String id);
    
    // void merge(final Collection<DTO> dto);
    
    void merge(final DTO dto);
    
}
