package platform.jetter.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import platform.jetter.model.mapper.NodeDtoMapper;
import platform.jetter.model.mapper.RelationDtoMapper;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.commons.Root;
import platform.model.commons.Types;
import platform.model.factory.RelationFactories;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;

@Api(value = "nodes", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
@Path("/nodes")
public class NodeServer
        implements NodeService {
    
    private final IRoot         root;
    private final NodeDtoMapper nodeMapper;
    private final boolean       allowRoot;
    
    public NodeServer() {
        this(new Root("node-service"), true); //$NON-NLS-1$
    }
    
    public NodeServer(final IRoot root, final boolean allowRoot) {
        super();
        this.root = root;
        this.allowRoot = allowRoot;
        this.nodeMapper = new NodeDtoMapper(this.root);
        this.nodeMapper.setMapper(new RelationDtoMapper(this.nodeMapper));
    }
    
    @POST
    @Override
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final Collection<String> ids) {
        final Collection<IRelation> rootRelations = this.root.getRelations();
        final Collection<IRelation> relations = new ArrayList<>(ids.size());
        for (final String id : ids) {
            for (final IRelation relation : rootRelations) {
                if (relation.getTarget().getId().equals(id)) {
                    relations.add(relation);
                    break;
                }
            }
        }
        this.root.removeRelations(relations);
    }
    
    @DELETE
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final String id) {
        this.delete(Arrays.asList(id));
    }
    
    @GET
    @Override
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public NodeDto getById(@PathParam(value = "id") final String id) {
        if (!this.allowRoot && this.root.getId().equals(id)) {
            throw new NotAuthorizedException(id);
        }
        final INode node = NodeUtils.find(this.root, new TraversalContext(), id);
        if (node == null) {
            throw new NotFoundException();
        }
        return this.nodeMapper.toEntity(node);
    }
    
    @GET
    @Override
    @Path("/type/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<NodeDto> getByType(@PathParam(value = "type") final String type) {
        final Descriptor<IRelation> descriptor = Descriptor.getDescriptor(type);
        if (descriptor == null) {
            return Collections.emptyList();
        }
        final Collection<IRelation> relations = this.root.getRelations(new HashSet<>(Arrays.asList(descriptor)));
        final Collection<NodeDto> nodes = new ArrayList<>(relations.size());
        for (final IRelation relation : relations) {
            nodes.add(this.nodeMapper.toEntity(relation.getTarget()));
        }
        return nodes;
    }
    
    @POST
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void post(final Collection<NodeDto> entities) {
        final Collection<IRelation> relations = new ArrayList<>(entities.size());
        final Collection<IRelation> rootRelations = this.root.getRelations();
        loop: for (final NodeDto entity : entities) {
            final INode target = this.nodeMapper.toModel(entity);
            if (target == null) {
                System.err.println("target is null, continuing"); //$NON-NLS-1$
                continue loop;
            }
            for (final IRelation relation : rootRelations) {
                if (relation.getTarget().getId().equals(target.getId())) {
                    NodeUtils.merge(relation.getTarget(), new TraversalContext(), target);
                    continue loop;
                }
            }
            final IRelation relation = RelationFactories.INSTANCE.create(Types.RELATION, target.getId(), null, this.root, target);
            if (relation != null) {
                relations.add(relation);
            }
        }
        try {
            this.root.addRelations(relations);
        } catch (final Exception e) {
            throw new ForbiddenException("At least one relation is invalid, the content has not been modified"); //$NON-NLS-1$
        }
    }
    
    @PUT
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void put(final Collection<NodeDto> entities) {
        for (final NodeDto entity : entities) {
            final INode current = NodeUtils.find(this.root, new TraversalContext(), entity.getId());
            if (current != null) {
                NodeUtils.merge(current, new TraversalContext(), this.nodeMapper.toModel(entity));
            }
        }
    }
    
}