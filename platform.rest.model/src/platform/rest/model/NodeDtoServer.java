package platform.rest.model;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
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
import platform.model.commons.Types;
import platform.model.factory.RelationFactories;
import platform.model.utils.NodeUtils;
import platform.utils.Strings;
import platform.utils.interfaces.IService;

@Path("/nodes")
@Api(value = "nodes", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
public class NodeDtoServer
        implements IService<NodeDto> {
    
    private final NodeDtoMapper nodeMapper;
    private final boolean       allowRoot;
    
    public NodeDtoServer(final IRoot root, final boolean allowRoot) {
        super();
        this.allowRoot = allowRoot;
        this.nodeMapper = new NodeDtoMapper(root);
    }
    
    public void delete(final Collection<String> dtos) {
        for (final String dto : dtos) {
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
        while (true) {
            final IRelation relation = NodeUtils.findParentRelation(this.nodeMapper.getRoot(), id);
            if (relation == null) {
                return;
            }
            relation.getSource().removeRelations(Arrays.asList(relation));
        }
    }
    
    @GET
    @Override
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public NodeDto getById(@PathParam(value = "id") final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return null;
        }
        if (!this.allowRoot && this.nodeMapper.getRoot().getId().equals(id)) {
            throw new NotAuthorizedException(id);
        }
        final INode node = NodeUtils.find(this.nodeMapper.getRoot(), id);
        if (node == null) {
            throw new NotFoundException();
        }
        return this.nodeMapper.toEntity(node);
    }
    
    public void merge(final Collection<NodeDto> dtos) {
        for (final NodeDto dto : dtos) {
            this.merge(dto);
        }
    }
    
    @PUT
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void merge(final NodeDto entity) {
        if (entity == null) {
            return;
        }
        final INode node = this.nodeMapper.toModel(entity);
        final INode current = NodeUtils.find(this.nodeMapper.getRoot(), entity.getId());
        if (current != null) {
            NodeUtils.merge(current, node);
        } else {
            final IRelation relation = RelationFactories.INSTANCE.create(Types.RELATION, null, null, this.nodeMapper.getRoot(), node);
            this.nodeMapper.getRoot().addRelations(Arrays.asList(relation));
        }
    }
    
}