package platform.tests;

import java.util.Arrays;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import platform.jersey.NodeDtoClient;
import platform.jetter.JetterService;
import platform.jetter.model.NodeDto;
import platform.jetter.model.NodeDtoServer;
import platform.model.IRoot;
import platform.model.commons.Node;
import platform.model.commons.Relation;
import platform.model.commons.Root;
import platform.model.commons.Types;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.utils.interfaces.IService;

@SuppressWarnings("nls")
public class TestJetter {
    
    private static final Integer PORT = 8888;
    private static final String  URL  = "http://localhost:" + TestJetter.PORT;
    private static final String  API  = "/api";
    
    @Test
    public void test() {
        final IRoot root = new Root("test-jetter");
        root.addRelations(Arrays.asList(new Relation(Types.RELATION, root, new Node(Types.NODE, "found", null, root))));
        root.addRelations(Arrays.asList(new Relation(Types.RELATION, root, new Node(Types.NODE, "found2", null, root))));
        root.addRelations(Arrays.asList(new Relation(Types.RELATION, root, new Node(Types.NODE, "found3", null, root))));
        
        final IService<NodeDto> service = new NodeDtoServer(root, true);
        
        final JetterService jetter = new JetterService(TestJetter.PORT, TestJetter.API, service);
        
        try {
            jetter.startNonBlocking();
        } catch (final Exception e1) {
            e1.printStackTrace();
        }
        
        final Response responseJson = ClientBuilder.newClient().target(TestJetter.URL + TestJetter.API + "/swagger.json").request().get();
        Assert.assertEquals(200, responseJson.getStatus());
        
        final String output = responseJson.readEntity(String.class);
        
        Assert.assertTrue(output.contains("/nodes"));
        Assert.assertTrue(output.contains("/nodes/id/{id}"));
        Assert.assertTrue(output.contains("/nodes/type/{type}"));
        Assert.assertTrue(output.contains("/relations"));
        
        final Response responseUi = ClientBuilder.newClient().target(TestJetter.URL + TestJetter.API + "/ui").request().get();
        Assert.assertEquals(200, responseUi.getStatus());
        
        final Response responseIdFound = ClientBuilder.newClient().target(TestJetter.URL + TestJetter.API + "/nodes/id/found").request().get();
        Assert.assertEquals(200, responseIdFound.getStatus());
        
        final platform.jetter.model.NodeDto node = responseIdFound.readEntity(platform.jetter.model.NodeDto.class);
        Assert.assertNotNull(node);
        Assert.assertEquals("found", node.getId());
        
        final Response responseIdNotFound = ClientBuilder.newClient().target(TestJetter.URL + TestJetter.API + "/nodes/id/notfound").request().get();
        Assert.assertEquals(404, responseIdNotFound.getStatus());
        
        final Response responseTypeNode = ClientBuilder.newClient().target(TestJetter.URL + TestJetter.API + "/nodes/type/node").request().get();
        Assert.assertEquals(200, responseTypeNode.getStatus());
        
        final platform.jetter.model.NodeDto[] nodesByTypeNode = responseTypeNode.readEntity(platform.jetter.model.NodeDto[].class);
        Assert.assertNotNull(nodesByTypeNode);
        Assert.assertEquals(0, nodesByTypeNode.length);
        
        final Response responseTypeRelation = ClientBuilder.newClient().target(TestJetter.URL + TestJetter.API + "/nodes/type/relation").request().get();
        Assert.assertEquals(200, responseTypeRelation.getStatus());
        
        final platform.jetter.model.NodeDto[] nodesByTypeRelation = responseTypeRelation.readEntity(platform.jetter.model.NodeDto[].class);
        Assert.assertNotNull(nodesByTypeRelation);
        Assert.assertEquals(3, nodesByTypeRelation.length);
        
        final NodeDtoClient client = new NodeDtoClient(root, TestJetter.URL + TestJetter.API + "/nodes", false);
        
        Assert.assertNull(client.getById("notfound"));
        Assert.assertNotNull(client.getById("found"));
        
        Assert.assertEquals(0, client.getByType("node").size());
        Assert.assertEquals(3, client.getByType("relation").size());
        
        client.post(Arrays.asList(new Node(Types.NODE, "posted1", null, root)));
        
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e1) {
            Assert.fail();
        }
        
        Assert.assertNotNull(NodeUtils.find(root, new TraversalContext(), "posted1"));
        
        try {
            jetter.stop();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
    }
}
