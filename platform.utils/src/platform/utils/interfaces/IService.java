package platform.utils.interfaces;

public interface IService<DTO> {
    
    void delete(final String id);
    
    DTO getById(final String id);
    
    void merge(final DTO dto);
    
}
