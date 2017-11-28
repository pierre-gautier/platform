package license.server.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;

import platform.dao.DaoStrategy;
import platform.hibernate.HibernateDao;
import platform.hibernate.model.NodeEntity;
import platform.hibernate.model.RelationEntity;
import platform.hibernate.model.mapper.NodeEntityMapper;
import platform.hibernate.model.mapper.RelationEntityMapper;
import platform.jetter.JetterService;
import platform.jetter.model.NodeDto;
import platform.jetter.model.NodeDtoServer;
import platform.jetter.model.RelationDto;
import platform.jetter.model.RelationDtoServer;
import platform.liquibase.LiquibaseService;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.commons.AttributesStrategy;
import platform.model.license.LicenseRoot;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseDescriptorFactories;
import platform.sql.DatabaseDescriptorParser;
import platform.sql.DatabaseService;
import platform.utils.Configuration;
import platform.utils.interfaces.IService;

public class LicenseServerAddon {
    
    private static DatabaseDescriptor MYSQL = DatabaseDescriptorFactories.INSTANCE.create("jdbc:mysql://localhost", "licenses-server", "root", "root"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    private final LicenseRoot         root;
    
    @Inject
    public LicenseServerAddon(final LicenseRoot root) {
        this.root = root;
        this.root.addStrategy(new AttributesStrategy());
    }
    
    @PostConstruct
    public void postConstruct() {
        
        final File databasePropertiesFile = Configuration.file("database.properties"); //$NON-NLS-1$
        
        DatabaseDescriptor databaseDescriptor = null;
        
        try (InputStream in = new FileInputStream(databasePropertiesFile)) {
            databaseDescriptor = DatabaseDescriptorParser.load(in);
        } catch (final IOException e1) {
            e1.printStackTrace();
            databaseDescriptor = LicenseServerAddon.MYSQL;
            try (Writer out = new FileWriter(databasePropertiesFile)) {
                DatabaseDescriptorParser.save(databaseDescriptor, out);
            } catch (final IOException e2) {
                e2.printStackTrace();
            }
        }
        
        new DatabaseService().createDatabase(databaseDescriptor);
        
        try {
            final URL url = platform.liquibase.model.Activator.getContext().getBundle().getResource("resources/model.mysql.sql"); //$NON-NLS-1$
            final String resourcePath = new File(FileLocator.toFileURL(url).getPath()).toString();
            final LiquibaseService liquibase = new LiquibaseService();
            liquibase.apply(databaseDescriptor, resourcePath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        
        final NodeEntityMapper nodeMapper = new NodeEntityMapper(this.root);
        final RelationEntityMapper relationMapper = new RelationEntityMapper(nodeMapper);
        nodeMapper.setMapper(relationMapper);
        
        final HibernateDao<INode, String, NodeEntity> nodeDao = new HibernateDao<>(databaseDescriptor, nodeMapper);
        final HibernateDao<IRelation, String, RelationEntity> relationDao = new HibernateDao<>(databaseDescriptor, relationMapper);
        final DaoStrategy daoStrategy = new DaoStrategy(this.root, nodeDao, relationDao);
        
        daoStrategy.load();
        
        this.root.addStrategy(daoStrategy);
        
        final IService<NodeDto> nodeService = new NodeDtoServer(this.root, true);
        final IService<RelationDto> relationService = new RelationDtoServer(this.root);
        final JetterService jetter = new JetterService(8080, "/api", nodeService, relationService); //$NON-NLS-1$
        
        try {
            jetter.start();
        } catch (final Exception e1) {
            e1.printStackTrace();
        }
        
    }
    
    @PreDestroy
    public void preDestroy() {
        // do nothing
    }
}
