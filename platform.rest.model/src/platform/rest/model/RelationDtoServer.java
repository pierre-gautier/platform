package platform.rest.model;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.utils.Strings;
import platform.utils.interfaces.IService;

@Path("/relations")
@Api(value = "relations", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
public class RelationDtoServer
        implements IService<RelationDto> {
    
    private final IRoot             root;
    private final RelationDtoMapper relationMapper;
    
    public RelationDtoServer(final IRoot root) {
        super();
        this.root = root;
        final NodeDtoMapper nodeMapper = new NodeDtoMapper(this.root);
        this.relationMapper = new RelationDtoMapper(nodeMapper);
    }
    
    public void delete(final Collection<String> ids) {
        for (final String dto : ids) {
            this.delete(dto);
        }
    }
    
    @DELETE
    @Override
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(@PathParam(value = "id") final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return;
        }
        final IRelation relation = NodeUtils.findRelation(this.root, id);
        if (relation != null) {
            relation.getSource().removeRelations(Arrays.asList(relation));
        }
    }
    
    @GET
    @Override
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RelationDto getById(@PathParam(value = "id") final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return null;
        }
        final IRelation relation = NodeUtils.findRelation(this.root, id);
        if (relation != null) {
            return this.relationMapper.toEntity(relation);
        }
        throw new NotFoundException();
    }
    
    public void merge(final Collection<RelationDto> dtos) {
        for (final RelationDto dto : dtos) {
            this.merge(dto);
        }
    }
    
    @PUT
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void merge(final RelationDto dto) {
        if (dto == null) {
            return;
        }
        final IRelation relation = NodeUtils.findRelation(this.root, dto.getId());
        if (relation != null) {
            NodeUtils.mergeRelation(relation, new TraversalContext(0, 0), this.relationMapper.toModel(dto));
            return;
        }
        final INode source = NodeUtils.find(this.root, dto.getSourceId());
        if (source != null) {
            source.addRelations(Arrays.asList(this.relationMapper.toModel(dto)));
        }
    }
    
}