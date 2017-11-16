package platform.liquibase;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.core.formattedsql.FormattedSqlChangeLogSerializer;
import platform.sql.DatabaseDescriptor;
import platform.sql.DatabaseService;
import platform.utils.Strings;

public class LiquibaseService {
    
    public void apply(final DatabaseDescriptor descriptor, final String changelogPath) {
        try (final Connection connection = DatabaseService.getConnection(descriptor, true)) {
            final DatabaseConnection databaseConnection = new JdbcConnection(connection);
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);
            final ResourceAccessor accessor = new FileSystemResourceAccessor();
            final Liquibase liquibase = new Liquibase(changelogPath, accessor, database);
            liquibase.update(Strings.EMPTY);
        } catch (final SQLException e) {
            e.printStackTrace();
        } catch (final DatabaseException e) {
            e.printStackTrace();
        } catch (final LiquibaseException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void generate(final DatabaseDescriptor descriptor, final String changelogPath, final String author) {
        try (final Connection connection = DatabaseService.getConnection(descriptor, true);
                final PrintStream stream = new PrintStream(changelogPath)) {
            final DatabaseConnection databaseConnection = new JdbcConnection(connection);
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);
            final ResourceAccessor accessor = new FileSystemResourceAccessor();
            final Liquibase liquibase = new Liquibase(changelogPath, accessor, database);
            final CatalogAndSchema cas = new CatalogAndSchema(descriptor.getDb(), descriptor.getDb());
            final DiffToChangeLog diff = new DiffToChangeLog(new DiffOutputControl());
            diff.setChangeSetPath(changelogPath);
            diff.setChangeSetAuthor(author);
            final ChangeLogSerializer serializer = new FormattedSqlChangeLogSerializer();
            liquibase.generateChangeLog(cas, diff, stream, serializer);
        } catch (final SQLException e) {
            e.printStackTrace();
        } catch (final DatabaseException e) {
            e.printStackTrace();
        } catch (final LiquibaseException e) {
            e.printStackTrace();
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
}
