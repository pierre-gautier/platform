package platform.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import platform.hibernate.HibernateDao;
import platform.hibernate.model.mapper.NodeEntityMapper;
import platform.hibernate.model.mapper.RelationEntityMapper;
import platform.liquibase.LiquibaseService;
import platform.model.Attribute;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.commons.Descriptors;
import platform.model.commons.Node;
import platform.model.commons.Relation;
import platform.model.commons.Root;
import platform.model.commons.Types;
import platform.model.utils.NodeUtils;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseDescriptorFactories;
import platform.sql.DatabaseService;
import platform.utils.interfaces.IDao;

@SuppressWarnings("nls")
public class TestHibernate {
    
    public static IRelation find(final INode node, final String id) {
        for (final IRelation relation : node.getRelations()) {
            if (relation.getId().equals(id)) {
                return relation;
            }
            final IRelation found = TestHibernate.find(relation.getTarget(), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    private final DatabaseDescriptor descriptor = DatabaseDescriptorFactories.INSTANCE.create("jdbc:mysql://localhost", "testhibernate", "root", "root"); //$NON-NLS-1$
    private final IRoot              root       = new Root("test-root");
    private final LiquibaseService   liquibase  = new LiquibaseService();
    private final DatabaseService    database   = new DatabaseService();
    
    private IDao<IRelation, String>  relationDao;
    private IDao<INode, String>      nodeDao;
    
    @After
    public void after() {
        this.database.dropDatabase(this.descriptor);
    }
    
    @Before
    public void before() {
        this.database.createDatabase(this.descriptor);
        try {
            final URL url = platform.liquibase.model.Activator.getContext().getBundle().getResource("resources/model.mysql.sql"); //$NON-NLS-1$
            final String resourcePath = new File(FileLocator.toFileURL(url).getPath()).toString();
            this.liquibase.apply(this.descriptor, resourcePath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final NodeEntityMapper nodeMapper = new NodeEntityMapper(this.root);
        final RelationEntityMapper relationMapper = new RelationEntityMapper(nodeMapper);
        nodeMapper.setMapper(relationMapper);
        this.nodeDao = new HibernateDao<>(this.descriptor, nodeMapper);
        this.relationDao = new HibernateDao<>(this.descriptor, relationMapper);
    }
    
    @Test
    public void test() {
        this.testCreate();
        this.testRetrieveRelations();
        this.testRetrieveNodes();
        this.testUpdate();
        this.testDelete();
    }
    
    public void testCreate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 01, 02);
        
        Assert.assertEquals(0, this.database.count(this.descriptor, "node"));
        Assert.assertEquals(0, this.database.count(this.descriptor, "node_attribute"));
        this.nodeDao.create(Arrays.asList(this.root));
        Assert.assertEquals(1, this.database.count(this.descriptor, "node"));
        Assert.assertEquals(1, this.database.count(this.descriptor, "node_attribute"));
        
        final Collection<Attribute> values = new ArrayList<>(4);
        values.add(new Attribute(Descriptors.ID, "546546736"));
        values.add(new Attribute(Descriptors.LABEL, "la belle"));
        values.add(new Attribute(Descriptors.ACTIVE, Boolean.TRUE));
        values.add(new Attribute(Descriptors.DATE, calendar.toInstant()));
        
        final INode node1 = new Node(Types.NODE, "1", values, this.root);
        final INode node2 = new Node(Types.NODE, "2", values, this.root);
        final INode node3 = new Node(Types.NODE, "3", values, this.root);
        final IRelation relation1 = new Relation(Types.RELATION, "1", null, this.root, node1);
        final IRelation relation2 = new Relation(Types.RELATION, "2", values, node1, node2);
        final IRelation relation3 = new Relation(Types.RELATION, "3", null, node2, node3);
        
        Assert.assertEquals(1, this.database.count(this.descriptor, "node"));
        Assert.assertEquals(1, this.database.count(this.descriptor, "node_attribute"));
        this.nodeDao.create(Arrays.asList(node1));
        Assert.assertEquals(2, this.database.count(this.descriptor, "node"));
        Assert.assertEquals(5, this.database.count(this.descriptor, "node_attribute"));
        
        Assert.assertEquals(0, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(0, this.database.count(this.descriptor, "relation_attribute"));
        this.root.addRelations(Arrays.asList(relation1));
        this.relationDao.create(Arrays.asList(relation1));
        Assert.assertEquals(1, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(0, this.database.count(this.descriptor, "relation_attribute"));
        
        Assert.assertEquals(2, this.database.count(this.descriptor, "node"));
        Assert.assertEquals(5, this.database.count(this.descriptor, "node_attribute"));
        this.nodeDao.create(Arrays.asList(node2, node3));
        Assert.assertEquals(4, this.database.count(this.descriptor, "node"));
        Assert.assertEquals(13, this.database.count(this.descriptor, "node_attribute"));
        
        Assert.assertEquals(1, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(0, this.database.count(this.descriptor, "relation_attribute"));
        node1.addRelations(Arrays.asList(relation2));
        this.relationDao.create(Arrays.asList(relation2));
        Assert.assertEquals(2, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(4, this.database.count(this.descriptor, "relation_attribute"));
        
        Assert.assertEquals(2, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(4, this.database.count(this.descriptor, "relation_attribute"));
        node2.addRelations(Arrays.asList(relation3));
        this.relationDao.create(Arrays.asList(relation3));
        Assert.assertEquals(3, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(4, this.database.count(this.descriptor, "relation_attribute"));
        
    }
    
    public void testDelete() {
        
        final INode node2 = new Node(Types.NODE, "2", null, this.root);
        final INode node3 = new Node(Types.NODE, "3", null, this.root);
        final IRelation relation0 = new Relation(Types.RELATION, "0", null, node3, node2);
        
        this.relationDao.delete(Arrays.asList(relation0));
        
        Assert.assertEquals(3, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(4, this.database.count(this.descriptor, "relation_attribute"));
        
        final IRelation relation1 = TestHibernate.find(this.root, "1");
        
        this.relationDao.delete(Arrays.asList(relation1));
        Assert.assertEquals(2, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(4, this.database.count(this.descriptor, "relation_attribute"));
        
        final IRelation relation2 = TestHibernate.find(this.root, "2");
        
        this.relationDao.delete(Arrays.asList(relation2));
        Assert.assertEquals(1, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(0, this.database.count(this.descriptor, "relation_attribute"));
        
        final IRelation relation3 = TestHibernate.find(this.root, "3");
        
        this.relationDao.delete(Arrays.asList(relation3));
        Assert.assertEquals(0, this.database.count(this.descriptor, "relation"));
        Assert.assertEquals(0, this.database.count(this.descriptor, "relation_attribute"));
        
    }
    
    public void testRetrieveNodes() {
        
        Assert.assertEquals(0, this.nodeDao.retrieve(null).size());
        Assert.assertEquals(0, this.nodeDao.retrieve(Arrays.asList("17")).size());
        Assert.assertEquals(0, this.nodeDao.retrieve(Arrays.asList("17", null, "18")).size());
        
        Assert.assertEquals(2, this.nodeDao.retrieve(Arrays.asList("2", "3")).size());
        Assert.assertEquals(2, this.nodeDao.retrieve(Arrays.asList("2", "3", null, "17")).size());
        
        final Collection<INode> nodes = this.nodeDao.retrieve(Arrays.asList("1"));
        
        Assert.assertEquals(1, nodes.size());
        
        final INode node = nodes.iterator().next();
        
        Assert.assertEquals("1", node.getId());
        Assert.assertEquals(1, node.getRelations().size());
        
        final INode child = node.getRelations().get(0).getTarget();
        
        Assert.assertEquals("2", child.getId());
        Assert.assertEquals(1, child.getRelations().size());
        
        final INode grandChild = child.getRelations().get(0).getTarget();
        
        Assert.assertEquals("3", grandChild.getId());
        Assert.assertEquals(0, grandChild.getRelations().size());
        
    }
    
    public void testRetrieveRelations() {
        
        Assert.assertEquals(0, this.relationDao.retrieve(null).size());
        Assert.assertEquals(0, this.relationDao.retrieve(Arrays.asList("17")).size());
        
        Assert.assertEquals(3, this.relationDao.retrieve(Arrays.asList("1", "2", "3", null, "17")).size());
        Assert.assertEquals(2, this.relationDao.retrieve(Arrays.asList("1", "2")).size());
        
        final Collection<IRelation> relations = this.relationDao.retrieve(Arrays.asList("1"));
        
        Assert.assertEquals(1, relations.size());
        
        final IRelation relation = relations.iterator().next();
        Assert.assertEquals("1", relation.getId());
        Assert.assertEquals(1, relation.getTarget().getRelations().size());
        
        final IRelation child = relation.getTarget().getRelations().get(0);
        
        Assert.assertEquals("2", child.getId());
        Assert.assertEquals(1, child.getTarget().getRelations().size());
        
        final IRelation grandchild = child.getTarget().getRelations().get(0);
        
        Assert.assertEquals("3", grandchild.getId());
        Assert.assertEquals(0, grandchild.getTarget().getRelations().size());
        
    }
    
    public void testUpdate() {
        
        final INode source = NodeUtils.find(this.root, "1");
        source.addAttribute(Descriptors.LABEL, "la moche");
        source.addAttribute(Descriptors.ID, "666");
        
        this.nodeDao.update(Arrays.asList(source));
        
        Assert.assertEquals(13, this.database.count(this.descriptor, "node_attribute"));
        Assert.assertEquals("la moche", this.database.selectSingleLine(this.descriptor, "node_attribute", "value", " WHERE node_id = '1' AND name = 'label'"));
        Assert.assertEquals("666", this.database.selectSingleLine(this.descriptor, "node_attribute", "value", " WHERE node_id = '1' AND name = 'id'"));
        
    }
    
}
