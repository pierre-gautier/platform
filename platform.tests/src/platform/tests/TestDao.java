package platform.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import platform.dao.DaoStrategy;
import platform.hibernate.HibernateDao;
import platform.hibernate.model.NodeEntity;
import platform.hibernate.model.RelationEntity;
import platform.hibernate.model.mapper.NodeEntityMapper;
import platform.hibernate.model.mapper.RelationEntityMapper;
import platform.liquibase.LiquibaseService;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.IStrategy;
import platform.model.commons.Root;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseDescriptorFactories;
import platform.sql.DatabaseService;

@SuppressWarnings("nls")
public class TestDao {
    
    private final DatabaseDescriptor descriptor = DatabaseDescriptorFactories.INSTANCE.create("jdbc:mysql://localhost", "testdao", "root", "root"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private final LiquibaseService   liquibase  = new LiquibaseService();
    private final DatabaseService    database   = new DatabaseService();
    
    @After
    public void after() {
        this.database.dropDatabase(this.descriptor);
    }
    
    @Before
    public void before() {
        this.database.dropDatabase(this.descriptor);
        this.database.createDatabase(this.descriptor);
        try {
            final URL url = platform.liquibase.model.Activator.getContext().getBundle().getResource("resources/model.mysql.sql"); //$NON-NLS-1$
            final String resourcePath = new File(FileLocator.toFileURL(url).getPath()).toString();
            this.liquibase.apply(this.descriptor, resourcePath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void test() {
        
        final IRoot root = new Root("test-dao-root");
        
        final NodeEntityMapper nodeMapper = new NodeEntityMapper(root);
        final RelationEntityMapper relationMapper = new RelationEntityMapper(nodeMapper);
        nodeMapper.setMapper(relationMapper);
        final HibernateDao<INode, String, NodeEntity> nodeDao = new HibernateDao<>(this.descriptor, nodeMapper);
        final HibernateDao<IRelation, String, RelationEntity> relationDao = new HibernateDao<>(this.descriptor, relationMapper);
        
        final IStrategy strategy = new DaoStrategy(root, nodeDao, relationDao);
        
        root.addStrategy(strategy);
        
    }
}
