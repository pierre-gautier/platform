package platform.jersey;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import platform.jetter.model.NodeDto;
import platform.jetter.model.NodeService;
import platform.jetter.model.mapper.NodeDtoMapper;
import platform.jetter.model.mapper.RelationDtoMapper;
import platform.model.IRoot;
import platform.model.commons.Root;

public class NodeClient
        implements NodeService {
    
    private final IRoot         root;
    private final NodeDtoMapper nodeMapper;
    private final String        url;
    private final Client        client;
    
    public NodeClient(final IRoot root, final String url) {
        super();
        this.url = url;
        this.root = root;
        this.nodeMapper = new NodeDtoMapper(this.root);
        this.nodeMapper.setMapper(new RelationDtoMapper(this.nodeMapper));
        this.client = ClientBuilder.newClient();
        
    }
    
    public NodeClient(final String url) {
        this(new Root("node-service"), url); //$NON-NLS-1$
    }
    
    @Override
    public void delete(final Collection<String> ids) {
        final String[] array = ids.toArray(new String[ids.size()]);
        this.client.target(this.url)
                .path("delete") //$NON-NLS-1$
                .request()
                .async()
                .post(Entity.json(array));
    }
    
    @Override
    public void delete(final String id) {
        this.client.target(this.url)
                .path(id)
                .request()
                .async()
                .delete();
    }
    
    @Override
    public NodeDto getById(final String id) {
        try {
            return this.client
                    .target(this.url)
                    .path("id")//$NON-NLS-1$
                    .path(id)
                    .request()
                    .get(NodeDto.class);
        } catch (final NotFoundException e) {
            return null;
        }
    }
    
    @Override
    public Collection<NodeDto> getByType(final String type) {
        final NodeDto[] nodes = this.client
                .target(this.url)
                .path("type") //$NON-NLS-1$
                .path(type)
                .request()
                .get(NodeDto[].class);
        return Arrays.asList(nodes);
        
    }
    
    @Override
    public void post(final Collection<NodeDto> entities) {
        final NodeDto[] array = entities.toArray(new NodeDto[entities.size()]);
        this.client
                .target(this.url)
                .request()
                .async()
                .post(Entity.json(array));
    }
    
    @Override
    public void put(final Collection<NodeDto> entities) {
        final NodeDto[] array = entities.toArray(new NodeDto[entities.size()]);
        this.client
                .target(this.url)
                .request()
                .async()
                .put(Entity.json(array));
    }
    
}