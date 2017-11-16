package platform.utils.interfaces;

public interface IMapper<ENTITY, MODEL> {
    
    ENTITY toEntity(MODEL object);
    
    MODEL toModel(ENTITY entity);
    
}
