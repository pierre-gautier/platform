package platform.hibernate;

import platform.utils.interfaces.IMapper;

public interface IEntityMapper<ENTITY, MODEL>
        extends IMapper<ENTITY, MODEL> {
    
    Class<ENTITY> entityClass();
    
    String entityId();
    
}
