package platform.rest.whatsup;

import platform.utils.interfaces.IMapper;
import platform.whatsup.WhatsupEvent;

public class WhatsupDtoMapper
        implements IMapper<WhatsupEventDto, WhatsupEvent> {
    
    @Override
    public WhatsupEventDto toEntity(final WhatsupEvent object) {
        return new WhatsupEventDto(object.getType(), object.getId(), object.getAction());
    }
    
    @Override
    public WhatsupEvent toModel(final WhatsupEventDto model) {
        return new WhatsupEvent(model.getType(), model.getId(), model.getAction());
    }
    
}
