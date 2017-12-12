package platform.rest.whatsup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import platform.whatsup.WhatsupEvent;
import platform.whatsup.WhatsupStrategy;

@Api(value = "whatsup", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
@Path("/whatsup")
public class WhatsupDtoServer {
    
    private final WhatsupStrategy  strategy;
    private final WhatsupDtoMapper mapper;
    
    public WhatsupDtoServer(final WhatsupStrategy strategy) {
        this.strategy = strategy;
        this.mapper = new WhatsupDtoMapper();
    }
    
    @GET
    @Path("/index")
    @Produces(MediaType.APPLICATION_JSON)
    public int getCurrentIndext() {
        return this.strategy.getCurrentIndex();
    }
    
    @GET
    @Path("/{index}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WhatsupEventDto> getEventsFrom(@PathParam(value = "index") final int index) {
        final List<WhatsupEvent> events = this.strategy.getEventsFrom(index);
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        final List<WhatsupEventDto> dtos = new ArrayList<>(events.size());
        for (final WhatsupEvent event : events) {
            dtos.add(this.mapper.toEntity(event));
        }
        return dtos;
    }
    
}