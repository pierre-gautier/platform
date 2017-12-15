package platform.tests;

import org.junit.Assert;
import org.junit.Test;

import platform.model.INode;
import platform.model.IRelation;
import platform.model.commons.Root;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;

public class TestNodeUtils {
    
    private static final int DEPTH            = 5;
    private static final int NODE_COUNT_FOR_5 = 326;
    
    private String           lastIdCreated    = null;
    
    @Test
    public void test() {
        
        // create root and 326 nodes and their relations
        final Root root = new Root("test-service"); //$NON-NLS-1$
        this.create(root, TestNodeUtils.DEPTH);
        
        // test different traversal utility methods
        Assert.assertEquals(TestNodeUtils.NODE_COUNT_FOR_5, NodeUtils.cunt(root, null));
        Assert.assertEquals(TestNodeUtils.NODE_COUNT_FOR_5, NodeUtils.cunt(root, new TraversalContext()));
        Assert.assertNotNull(NodeUtils.find(root, null, this.lastIdCreated));
        Assert.assertNotNull(NodeUtils.find(root, this.lastIdCreated));
        
        final INode parent = root.getRelations().get(0).getTarget()
                .getRelations().get(0).getTarget()
                .getRelations().get(0).getTarget();
        final INode child = parent.getRelations().get(0).getTarget();
        
        // insert second path
        TestUtils.addRelation(parent, child);
        
        // test with context
        Assert.assertEquals(TestNodeUtils.NODE_COUNT_FOR_5, NodeUtils.cunt(root, new TraversalContext()));
        Assert.assertNotNull(NodeUtils.find(root, this.lastIdCreated));
        
        // test without
        Assert.assertNotEquals(TestNodeUtils.NODE_COUNT_FOR_5, NodeUtils.cunt(root, null));
        Assert.assertNotNull(NodeUtils.find(root, null, this.lastIdCreated));
        
        // insert loop
        TestUtils.addRelation(child, parent);
        
        // test with context
        Assert.assertEquals(TestNodeUtils.NODE_COUNT_FOR_5, NodeUtils.cunt(root, new TraversalContext()));
        Assert.assertNotNull(NodeUtils.find(root, this.lastIdCreated));
        
        // test without
        StackOverflowError expected = null;
        try {
            Assert.assertNotEquals(TestNodeUtils.NODE_COUNT_FOR_5, NodeUtils.cunt(root, null));
        } catch (final StackOverflowError e) {
            expected = e;
        }
        
        Assert.assertTrue(expected != null);
        
        expected = null;
        try {
            Assert.assertNotNull(NodeUtils.find(root, null, this.lastIdCreated));
        } catch (final StackOverflowError e) {
            expected = e;
        }
        
        Assert.assertTrue(expected != null);
    }
    
    private void create(final INode parent, final int level) {
        for (int i = 0; i < level; i++) {
            this.lastIdCreated = TestUtils.addRelation(parent);
        }
        for (final IRelation relation : parent.getRelations()) {
            this.create(relation.getTarget(), level - 1);
        }
    }
    
}
