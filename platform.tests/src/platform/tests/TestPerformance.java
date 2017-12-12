package platform.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import platform.dao.DaoStrategy;
import platform.hibernate.HibernateDao;
import platform.hibernate.model.mapper.NodeEntityMapper;
import platform.hibernate.model.mapper.RelationEntityMapper;
import platform.liquibase.LiquibaseService;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.commons.AttributesStrategy;
import platform.model.commons.Root;
import platform.model.commons.Types;
import platform.model.factory.NodeFactories;
import platform.model.factory.RelationFactories;
import platform.rest.client.model.NodeDtoClient;
import platform.rest.client.model.RelationDtoClient;
import platform.rest.model.NodeDto;
import platform.rest.model.NodeDtoServer;
import platform.rest.server.RESTServer;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseDescriptorFactories;
import platform.sql.DatabaseService;
import platform.utils.Configuration;
import platform.utils.interfaces.IDao;
import platform.utils.interfaces.IService;
import platform.ws.WSStrategy;
import platform.xml.XMLStrategy;

@SuppressWarnings("nls")
public class TestPerformance {
    
    private static final Integer            PORT       = 8888;
    private static final String             URL        = "http://localhost:" + TestPerformance.PORT;
    private static final String             API        = "/api";
    
    private static final DatabaseDescriptor descriptor = DatabaseDescriptorFactories.INSTANCE.create("jdbc:mysql://localhost", "testperformance", "root", "root"); //$NON-NLS-1$
    private static final LiquibaseService   liquibase  = new LiquibaseService();
    private static final DatabaseService    database   = new DatabaseService();
    
    private IDao<IRelation, String>         relationDao;
    private IDao<INode, String>             nodeDao;
    
    private IRoot                           rootServer;
    private IRoot                           rootClient;
    private XMLStrategy                     xmlStrategy;
    private WSStrategy                      wsStrategy;
    private RESTServer                      server;
    
    @After
    public void after() {
        try {
            this.server.stop();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    @Before
    public void before() {
        // server
        
        this.rootServer = new Root("test-server-performance-server");
        
        TestPerformance.database.createDatabase(TestPerformance.descriptor);
        try {
            final URL url = platform.liquibase.model.Activator.getContext().getBundle().getResource("resources/model.mysql.sql"); //$NON-NLS-1$
            final String resourcePath = new File(FileLocator.toFileURL(url).getPath()).toString();
            TestPerformance.liquibase.apply(TestPerformance.descriptor, resourcePath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final NodeEntityMapper nodeEntityMapper = new NodeEntityMapper(this.rootServer);
        final RelationEntityMapper relationEntityMapper = new RelationEntityMapper(nodeEntityMapper);
        nodeEntityMapper.setMapper(relationEntityMapper);
        this.nodeDao = new HibernateDao<>(TestPerformance.descriptor, nodeEntityMapper);
        this.relationDao = new HibernateDao<>(TestPerformance.descriptor, relationEntityMapper);
        
        this.rootServer.addStrategy(new DaoStrategy(this.rootServer, this.nodeDao, this.relationDao));
        
        final IService<NodeDto> service = new NodeDtoServer(this.rootServer, true);
        this.server = new RESTServer(TestPerformance.PORT, TestPerformance.API, service);
        try {
            this.server.startNonBlocking();
        } catch (final Exception e1) {
            e1.printStackTrace();
        }
        // client
        this.rootClient = new Root("test-server-performance-client");
        this.xmlStrategy = new XMLStrategy(this.rootClient, Configuration.file("test-server-performance.xml"), false); //$NON-NLS-1$
        
        final IService<INode> nodeService = new NodeDtoClient(this.rootClient, TestPerformance.URL + TestPerformance.API + "/nodes", false);
        final IService<IRelation> relationService = new RelationDtoClient(this.rootClient, TestPerformance.URL + TestPerformance.API + "/relations", false);
        
        this.wsStrategy = new WSStrategy(this.rootClient, nodeService, relationService);
        this.rootClient.addStrategy(new AttributesStrategy());
        this.rootClient.addStrategy(this.xmlStrategy);
        this.rootClient.addStrategy(this.wsStrategy);
    }
    
    // @Test
    // public void test1000() {
    // this.test(1000);
    // }
    //
    // @Test
    // public void test10000() {
    // this.test(10 * 1000);
    // }
    //
    // @Test
    // public void test100000() {
    // this.test(100 * 1000);
    // }
    //
    // @Test
    // public void test1000000() {
    // this.test(1000 * 1000);
    // }
    
    private void test(final int number) {
        
        final long start = System.currentTimeMillis();
        
        final Collection<IRelation> relations = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            final INode node = NodeFactories.INSTANCE.create(Types.NODE, null, null, this.rootClient);
            relations.add(RelationFactories.INSTANCE.create(Types.RELATION, null, null, this.rootClient, node));
        }
        
        this.rootClient.addRelations(relations);
        
        Assert.assertEquals(number, this.rootServer.getRelations().size());
        
        System.out.println("took " + (System.currentTimeMillis() - start) + " ms to create, add, send to ws and save as xml " + number + " nodes");
        
    }
}
