package platform.rest.client.model;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;

import platform.model.INode;
import platform.model.IRoot;
import platform.rest.model.NodeDto;
import platform.rest.model.NodeDtoMapper;
import platform.utils.Strings;
import platform.utils.interfaces.IService;

public class NodeDtoClient
        implements IService<INode> {
    
    private final String        url;
    private final Client        client;
    private final NodeDtoMapper mapper;
    private final boolean       async;
    
    public NodeDtoClient(final IRoot root, final String url, final boolean async) {
        super();
        this.url = url;
        this.async = async;
        this.mapper = new NodeDtoMapper(root);
        this.client = ClientBuilder.newClient();
    }
    
    // @Override
    // public void delete(final Collection<String> ids) {
    // if (CollectionsUtils.isNullOrEmpty(ids)) {
    // return;
    // }
    // final String[] array = ids.toArray(new String[ids.size()]);
    // final Builder builder = this.client.target(this.url).path("delete").request();
    // if (this.async) {
    // builder.async().post(Entity.json(array));
    // } else {
    // builder.post(Entity.json(array));
    // }
    // }
    
    @Override
    public void delete(final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return;
        }
        final Builder builder = this.client.target(this.url).path(id).request();
        if (this.async) {
            builder.async().delete();
        } else {
            builder.delete();
        }
    }
    
    // @Override
    // public Collection<INode> getAll() {
    // final NodeDto[] dtos = this.client.target(this.url).path("all").request().get(NodeDto[].class);
    // final Collection<INode> nodes = new ArrayList<>(dtos.length);
    // for (final NodeDto dto : dtos) {
    // nodes.add(this.mapper.toModel(dto));
    // }
    // return nodes;
    // }
    
    @Override
    public INode getById(final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return null;
        }
        try {
            return this.mapper.toModel(this.client.target(this.url).path(id).request().get(NodeDto.class));
        } catch (final NotFoundException e) {
            return null;
        }
    }
    
    // @Override
    // public Collection<INode> getByType(final String type) {
    // if (Strings.isNullEmptyOrBlank(type)) {
    // return Collections.emptyList();
    // }
    // final NodeDto[] dtos = this.client.target(this.url).path("type").path(type).request().get(NodeDto[].class);
    // final Collection<INode> nodes = new ArrayList<>(dtos.length);
    // for (final NodeDto dto : dtos) {
    // nodes.add(this.mapper.toModel(dto));
    // }
    // return nodes;
    //
    // }
    
    // @Override
    // public void post(final Collection<INode> nodes) {
    // if (CollectionsUtils.isNullOrEmpty(nodes)) {
    // return;
    // }
    // final NodeDto[] array = new NodeDto[nodes.size()];
    // int i = 0;
    // for (final INode node : nodes) {
    // array[i++] = this.mapper.toEntity(node);
    // }
    // final Builder builder = this.client.target(this.url).request();
    // if (this.async) {
    // builder.async().post(Entity.json(array));
    // } else {
    // builder.post(Entity.json(array));
    // }
    // }
    
    @Override
    public void merge(final INode node) {
        if (node == null) {
            return;
        }
        final NodeDto dto = this.mapper.toEntity(node);
        final Builder builder = this.client.target(this.url).request();
        if (this.async) {
            builder.async().put(Entity.json(dto));
        } else {
            builder.put(Entity.json(dto));
        }
    }
    
}