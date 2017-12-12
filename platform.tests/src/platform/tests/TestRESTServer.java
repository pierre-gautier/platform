package platform.tests;

import java.util.Arrays;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import platform.model.IRoot;
import platform.model.commons.Node;
import platform.model.commons.Relation;
import platform.model.commons.Root;
import platform.model.commons.Types;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.rest.client.model.NodeDtoClient;
import platform.rest.model.NodeDto;
import platform.rest.model.NodeDtoServer;
import platform.rest.server.RESTServer;
import platform.utils.interfaces.IService;

@SuppressWarnings("nls")
public class TestRESTServer {
    
    private static final Integer PORT = 8888;
    private static final String  URL  = "http://localhost:" + TestRESTServer.PORT;
    private static final String  API  = "/api";
    
    @Test
    public void test() {
        final IRoot root = new Root("test-rest-server");
        root.addRelations(Arrays.asList(new Relation(Types.RELATION, root, new Node(Types.NODE, "found", null, root))));
        root.addRelations(Arrays.asList(new Relation(Types.RELATION, root, new Node(Types.NODE, "found2", null, root))));
        root.addRelations(Arrays.asList(new Relation(Types.RELATION, root, new Node(Types.NODE, "found3", null, root))));
        
        final IService<NodeDto> service = new NodeDtoServer(root, true);
        
        final RESTServer server = new RESTServer(TestRESTServer.PORT, TestRESTServer.API, service);
        
        try {
            server.startNonBlocking();
        } catch (final Exception e1) {
            e1.printStackTrace();
        }
        
        final Response responseJson = ClientBuilder.newClient().target(TestRESTServer.URL + TestRESTServer.API + "/swagger.json").request().get();
        Assert.assertEquals(200, responseJson.getStatus());
        
        final String output = responseJson.readEntity(String.class);
        
        Assert.assertTrue(output.contains("/nodes"));
        Assert.assertTrue(output.contains("/nodes/{id}"));
        Assert.assertTrue(output.contains("/relations"));
        Assert.assertTrue(output.contains("/relations/{id}"));
        
        final Response responseUi = ClientBuilder.newClient().target(TestRESTServer.URL + TestRESTServer.API + "/ui").request().get();
        Assert.assertEquals(200, responseUi.getStatus());
        
        final Response responseIdFound1 = ClientBuilder.newClient().target(TestRESTServer.URL + TestRESTServer.API + "/nodes/found").request().get();
        Assert.assertEquals(200, responseIdFound1.getStatus());
        
        final platform.rest.model.NodeDto node = responseIdFound1.readEntity(platform.rest.model.NodeDto.class);
        Assert.assertNotNull(node);
        Assert.assertEquals("found", node.getId());
        
        final Response responseIdNotFound = ClientBuilder.newClient().target(TestRESTServer.URL + TestRESTServer.API + "/nodes/notfound").request().get();
        Assert.assertEquals(404, responseIdNotFound.getStatus());
        
        final NodeDtoClient client = new NodeDtoClient(root, TestRESTServer.URL + TestRESTServer.API + "/nodes", false);
        
        Assert.assertNull(client.getById("notfound"));
        Assert.assertNotNull(client.getById("found"));
        
        client.merge(new Node(Types.NODE, "posted1", null, root));
        
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e1) {
            Assert.fail();
        }
        
        Assert.assertNotNull(NodeUtils.find(root, new TraversalContext(), "posted1"));
        
        try {
            server.stop();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
    }
}
