package license.editor.application;

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
import org.eclipse.jface.dialogs.MessageDialog;

import platform.dao.DaoStrategy;
import platform.hibernate.HibernateDao;
import platform.hibernate.model.NodeEntity;
import platform.hibernate.model.RelationEntity;
import platform.hibernate.model.mapper.NodeEntityMapper;
import platform.hibernate.model.mapper.RelationEntityMapper;
import platform.liquibase.LiquibaseService;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.commons.AttributesStrategy;
import platform.model.license.LicenseRoot;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseDescriptorFactories;
import platform.sql.DatabaseDescriptorParser;
import platform.sql.DatabaseService;
import platform.ui.swt.SWTUtils;
import platform.utils.Configuration;
import platform.xml.XMLStrategy;

public class LicenseEditorAddon {
    
    private static DatabaseDescriptor MYSQL = DatabaseDescriptorFactories.INSTANCE.create("jdbc:mysql://localhost", "licenses", "root", "root"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    private final LicenseRoot         root;
    private final XMLStrategy         xmlStrategy;
    
    @Inject
    public LicenseEditorAddon(final LicenseRoot root) {
        this.root = root;
        this.xmlStrategy = new XMLStrategy(this.root, Configuration.file("licenses.xml"), true); //$NON-NLS-1$
        this.root.addStrategy(new AttributesStrategy());
        this.root.addStrategy(this.xmlStrategy);
    }
    
    @PostConstruct
    public void postConstruct() {
        
        final File databasePropertiesFile = Configuration.file("database.properties"); //$NON-NLS-1$
        
        DatabaseDescriptor databaseDescriptor = null;
        
        try (InputStream in = new FileInputStream(databasePropertiesFile)) {
            databaseDescriptor = DatabaseDescriptorParser.load(in);
        } catch (final IOException e1) {
            MessageDialog.openInformation(SWTUtils.createShellDialog(), "Information", "No database has been found\nUsing embeded database to persist data"); //$NON-NLS-1$//$NON-NLS-2$
            databaseDescriptor = LicenseEditorAddon.MYSQL;
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
        
    }
    
    @PreDestroy
    public void preDestroy() {
        this.xmlStrategy.save();
    }
}
