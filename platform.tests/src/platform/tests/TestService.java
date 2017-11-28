package platform.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import platform.jetter.model.NodeDto;
import platform.jetter.model.NodeDtoServer;
import platform.jetter.model.RelationDto;
import platform.model.INode;
import platform.model.IRoot;
import platform.model.commons.Descriptors;
import platform.model.commons.Root;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.utils.Strings;
import platform.utils.interfaces.IService;

@SuppressWarnings("nls")
public class TestService {
    
    private static final Map<String, String> labelToProperties(final String label) {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("label", label);
        return attributes;
        
    }
    
    private final IRoot             root    = new Root("test-all-root");
    private final IService<NodeDto> service = new NodeDtoServer(this.root, true);
    
    @Test
    public void test() {
        
        this.testPost();
        this.testPatch();
        this.testDelete();
        
        this.testPostTree();
        
    }
    
    private void testDelete() {
        
        this.service.delete(Arrays.asList("deux", "trois"));
        
        Assert.assertEquals(1, this.root.getRelations().size());
        
    }
    
    private void testPatch() {
        
        final INode node = NodeUtils.find(this.root, new TraversalContext(), "un");
        
        Assert.assertEquals("moche", node.getAttribute(Descriptors.LABEL));
        Assert.assertEquals(Boolean.TRUE, node.getAttribute(Descriptors.ACTIVE));
        
        final Map<String, String> attributes1 = new HashMap<>();
        attributes1.put("label", "belle");
        attributes1.put("active", "false");
        
        this.service.put(Arrays.asList(new NodeDto("un", "node", attributes1)));
        
        Assert.assertEquals("belle", node.getAttribute(Descriptors.LABEL));
        Assert.assertEquals(Boolean.FALSE, node.getAttribute(Descriptors.ACTIVE));
        
        final Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("active", "prout");
        attributes2.put("label", "gueule");
        
        this.service.put(Arrays.asList(new NodeDto("un", "node", attributes2)));
        
        Assert.assertEquals(Boolean.FALSE, node.getAttribute(Descriptors.ACTIVE));
        Assert.assertEquals("gueule", node.getAttribute(Descriptors.LABEL));
        
        this.service.put(Arrays.asList(new NodeDto("un", "node", null)));
        
        Assert.assertEquals(Boolean.TRUE, node.getAttribute(Descriptors.ACTIVE));
        Assert.assertEquals(Strings.EMPTY, node.getAttribute(Descriptors.LABEL));
        
    }
    
    private void testPost() {
        
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("label", "moche");
        attributes.put("active", "true");
        
        final Collection<NodeDto> nodes = new ArrayList<>();
        nodes.add(new NodeDto("un", "node", attributes));
        nodes.add(new NodeDto("deux", "node", attributes));
        nodes.add(new NodeDto("trois", "node", attributes));
        
        this.service.post(nodes);
        
        Assert.assertEquals(3, this.root.getRelations().size());
        
        Assert.assertNotNull(NodeUtils.find(this.root, new TraversalContext(), "un"));
        Assert.assertNotNull(NodeUtils.find(this.root, new TraversalContext(), "deux"));
        Assert.assertNotNull(NodeUtils.find(this.root, new TraversalContext(), "trois"));
        
        try {
            this.service.post(nodes);
        } catch (final Exception e) {
            // ignore
        }
        
        Assert.assertEquals(3, this.root.getRelations().size());
    }
    
    private void testPostTree() {
        
        final NodeDto parent = new NodeDto("parent", "node", TestService.labelToProperties("parent"));
        final NodeDto child1 = new NodeDto("child1", "node", TestService.labelToProperties("child1"));
        final NodeDto child2 = new NodeDto("child1", "node", TestService.labelToProperties("child1"));
        final NodeDto grandchild1 = new NodeDto("grandchild1", "node", TestService.labelToProperties("grandchild1"));
        
        final Collection<RelationDto> relationships = new ArrayList<>();
        relationships.add(new RelationDto("relation-parent-to-child1", "relation", parent.getId(), child1, TestService.labelToProperties("relation-parent-to-child1")));
        relationships.add(new RelationDto("relation-parent-to-child2", "relation", parent.getId(), child2, TestService.labelToProperties("relation-parent-to-child2")));
        
        parent.setRelationships(relationships);
        
        final Collection<RelationDto> relationships2 = new ArrayList<>();
        relationships2.add(new RelationDto("child1-to-grandchild1", "relation", child1.getId(), grandchild1, TestService.labelToProperties("relation-child1-to-grandchild1")));
        
        child1.setRelationships(relationships2);
        
        this.service.post(Arrays.asList(parent));
        
        final INode node = NodeUtils.find(this.root, new TraversalContext(), "parent");
        
        Assert.assertNotNull(node);
        Assert.assertEquals(2, node.getRelations().size());
        
        final INode nodeChild1 = NodeUtils.find(this.root, new TraversalContext(), "child1");
        
        Assert.assertNotNull(nodeChild1);
        Assert.assertEquals(1, nodeChild1.getRelations().size());
        
    }
    
}
