package platform.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import platform.liquibase.LiquibaseService;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseDescriptorFactories;
import platform.sql.DatabaseService;

@SuppressWarnings("nls")
public class TestSQL {
    
    private final DatabaseDescriptor descriptor = DatabaseDescriptorFactories.INSTANCE.create("jdbc:mysql://localhost", "testh2", "root", "root");
    private final LiquibaseService   liquibase  = new LiquibaseService();
    private final DatabaseService    database   = new DatabaseService();
    
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
    }
    
    @Test
    public void test() {
        System.out.println(this.database.count(this.descriptor, "node")); //$NON-NLS-1$
    }
    
}
