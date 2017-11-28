package platform.jetter.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.utils.Strings;
import platform.utils.collections.CollectionsUtils;
import platform.utils.interfaces.IService;

@Api(value = "relations", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
@Path("/relations")
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
    
    @POST
    @Override
    @Path("/delete")
    @SuppressWarnings("unchecked")
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final Collection<String> ids) {
        if (CollectionsUtils.isNullOrEmpty(ids)) {
            return;
        }
        final Map<INode, Collection<IRelation>> relations = CollectionsUtils.createMultiMap(1, LinkedList.class);
        for (final String id : ids) {
            final IRelation relation = NodeUtils.findRelation(this.root, new TraversalContext(), id);
            if (relation != null) {
                relations.get(relation.getSource()).add(relation);
            }
        }
        for (final Entry<INode, Collection<IRelation>> entry : relations.entrySet()) {
            entry.getKey().removeRelations(entry.getValue());
        }
    }
    
    @DELETE
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return;
        }
        this.delete(Arrays.asList(id));
    }
    
    @GET
    @Override
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RelationDto> getAll() {
        final Collection<IRelation> relations = this.root.getRelations(null);
        final Collection<RelationDto> dtos = new ArrayList<>(relations.size());
        for (final IRelation relation : relations) {
            dtos.add(this.relationMapper.toEntity(relation));
        }
        return dtos;
    }
    
    @GET
    @Override
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RelationDto getById(@PathParam(value = "id") final String id) {
        if (Strings.isNullEmptyOrBlank(id)) {
            return null;
        }
        return null;
    }
    
    @GET
    @Override
    @Path("/type/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RelationDto> getByType(@PathParam(value = "type") final String type) {
        final Descriptor<IRelation> descriptor = Descriptor.getDescriptor(type);
        if (descriptor == null) {
            return Collections.emptyList();
        }
        final Collection<IRelation> relations = this.root.getRelations(new HashSet<>(Arrays.asList(descriptor)));
        final Collection<RelationDto> dtos = new ArrayList<>(relations.size());
        for (final IRelation relation : relations) {
            dtos.add(this.relationMapper.toEntity(relation));
        }
        return dtos;
    }
    
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public void post(final Collection<RelationDto> entities) {
        if (CollectionsUtils.isNullOrEmpty(entities)) {
            return;
        }
        final Map<String, Collection<RelationDto>> sourceIdToRelations = CollectionsUtils.createMultiMap(1, LinkedList.class);
        for (final RelationDto entity : entities) {
            sourceIdToRelations.get(entity.getSourceId()).add(entity);
        }
        final Map<INode, Collection<IRelation>> sourceToRelations = CollectionsUtils.createMultiMap(sourceIdToRelations.size(), LinkedList.class);
        for (final Entry<String, Collection<RelationDto>> entry : sourceIdToRelations.entrySet()) {
            final INode source = NodeUtils.find(this.root, new TraversalContext(), entry.getKey());
            if (source != null) {
                for (final RelationDto relation : entry.getValue()) {
                    sourceToRelations.get(source).add(this.relationMapper.toModel(relation, source));
                }
            }
        }
        for (final Entry<INode, Collection<IRelation>> entry : sourceToRelations.entrySet()) {
            entry.getKey().addRelations(entry.getValue());
        }
    }
    
    @PUT
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void put(final Collection<RelationDto> entities) {
        if (CollectionsUtils.isNullOrEmpty(entities)) {
            return;
        }
        for (final RelationDto entity : entities) {
            final IRelation relation = NodeUtils.findRelation(this.root, new TraversalContext(), entity.getId());
            if (relation != null) {
                NodeUtils.mergeRelation(relation, new TraversalContext(), this.relationMapper.toModel(entity, relation.getSource()));
            }
        }
    }
    
}