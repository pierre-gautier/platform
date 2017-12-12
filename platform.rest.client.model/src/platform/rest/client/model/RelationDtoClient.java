package platform.rest.client.model;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;

import platform.model.IRelation;
import platform.model.IRoot;
import platform.rest.model.NodeDtoMapper;
import platform.rest.model.RelationDto;
import platform.rest.model.RelationDtoMapper;
import platform.utils.Strings;
import platform.utils.interfaces.IService;

@SuppressWarnings("nls")
public class RelationDtoClient
        implements IService<IRelation> {
    
    private final String            url;
    private final Client            client;
    private final RelationDtoMapper mapper;
    private final boolean           async;
    
    public RelationDtoClient(final IRoot root, final String url, final boolean async) {
        super();
        this.url = url;
        this.async = async;
        final NodeDtoMapper nodeMapper = new NodeDtoMapper(root);
        this.mapper = new RelationDtoMapper(nodeMapper);
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
    // public Collection<IRelation> getAll() {
    // final RelationDto[] dtos = this.client.target(this.url).path("all").request().get(RelationDto[].class);
    // final Collection<IRelation> relations = new ArrayList<>(dtos.length);
    // for (final RelationDto dto : dtos) {
    // relations.add(this.mapper.toModel(dto));
    // }
    // return relations;
    // }
    
    @Override
    public IRelation getById(final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return null;
        }
        try {
            return this.mapper.toModel(this.client.target(this.url).path("id").path(id).request().get(RelationDto.class));
        } catch (final NotFoundException e) {
            return null;
        }
    }
    
    // @Override
    // public Collection<IRelation> getByType(final String type) {
    // if (Strings.isNullEmptyOrBlank(type)) {
    // return Collections.emptyList();
    // }
    // final RelationDto[] dtos = this.client.target(this.url).path("type").path(type).request().get(RelationDto[].class);
    //
    // final Collection<IRelation> relations = new ArrayList<>(dtos.length);
    // for (final RelationDto dto : dtos) {
    // relations.add(this.mapper.toModel(dto));
    // }
    // return relations;
    //
    // }
    
    // @Override
    // public void post(final Collection<IRelation> relations) {
    // if (CollectionsUtils.isNullOrEmpty(relations)) {
    // return;
    // }
    // final RelationDto[] array = new RelationDto[relations.size()];
    // int i = 0;
    // for (final IRelation relation : relations) {
    // array[i++] = this.mapper.toEntity(relation);
    // }
    // final Builder builder = this.client.target(this.url).request();
    // if (this.async) {
    // builder.async().post(Entity.json(array));
    // } else {
    // builder.post(Entity.json(array));
    // }
    // }
    
    @Override
    public void merge(final IRelation relation) {
        if (relation == null) {
            return;
        }
        final RelationDto dto = this.mapper.toEntity(relation);
        final Builder builder = this.client.target(this.url).request();
        if (this.async) {
            builder.async().put(Entity.json(dto));
        } else {
            builder.put(Entity.json(dto));
        }
    }
    
}
