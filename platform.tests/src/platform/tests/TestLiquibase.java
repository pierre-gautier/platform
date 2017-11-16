package platform.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import platform.liquibase.LiquibaseService;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseDescriptorFactories;
import platform.sql.DatabaseService;

@SuppressWarnings("nls")
public class TestLiquibase {
    
    private final DatabaseDescriptor descriptor = DatabaseDescriptorFactories.INSTANCE.create("jdbc:mysql://localhost", "testliquibase", "root", "root");
    
    private final LiquibaseService   liquibase  = new LiquibaseService();
    private final DatabaseService    database   = new DatabaseService();
    
    @After
    public void after() {
        this.database.dropDatabase(this.descriptor);
    }
    
    @Before
    public void before() {
        this.database.createDatabase(this.descriptor);
    }
    
    @Test
    public void test() {
        this.liquibase.apply(this.descriptor, "resources/changelog1.xml");
        this.liquibase.generate(this.descriptor, "resources/test1.mysql.sql", "test");
        this.liquibase.apply(this.descriptor, "resources/changelog2.xml");
        this.liquibase.generate(this.descriptor, "resources/test2.mysql.sql", "test");
        this.database.dropDatabase(this.descriptor);
        this.database.createDatabase(this.descriptor);
        this.liquibase.apply(this.descriptor, "resources/test2.mysql.sql");
    }
    
}
