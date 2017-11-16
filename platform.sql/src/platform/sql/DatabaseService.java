package platform.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import platform.utils.Strings;

@SuppressWarnings("nls")
public class DatabaseService {
    
    public static Connection getConnection(final DatabaseDescriptor descriptor, final boolean db) {
        try {
            final String completeUrl = descriptor.getUrl() + (db ? Strings.SLASH + descriptor.getDb() : Strings.EMPTY); // $NON-NLS-1$
            return DriverManager.getConnection(completeUrl, descriptor.getUser(), descriptor.getPassword());
        } catch (final SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    public int count(final DatabaseDescriptor descriptor, final String table) {
        try (final Connection connection = DatabaseService.getConnection(descriptor, true);
                final Statement statement = connection.createStatement();
                final ResultSet result = statement.executeQuery("SELECT COUNT(*) as count from " + descriptor.quote() + table + descriptor.quote() + ";")) { //$NON-NLS-1$ //$NON-NLS-2$
            result.next();
            return result.getInt("count"); //$NON-NLS-1$
        } catch (final SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public boolean createDatabase(final DatabaseDescriptor descriptor) {
        try (final Connection connection = DatabaseService.getConnection(descriptor, descriptor.alwaysUseDb());
                final Statement statement = connection.createStatement()) {
            final String sql = "CREATE SCHEMA IF NOT EXISTS " + descriptor.quote() + descriptor.getDb() + descriptor.quote() + descriptor.getCharset() + ";";
            return statement.execute(sql); // $NON-NLS-1$
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean dropDatabase(final DatabaseDescriptor descriptor) {
        try (final Connection connection = DatabaseService.getConnection(descriptor, descriptor.alwaysUseDb());
                final Statement statement = connection.createStatement()) {
            final String sql = "DROP SCHEMA IF EXISTS " + descriptor.quote() + descriptor.getDb() + descriptor.quote() + ";";
            return statement.execute(sql);
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String selectSingleLine(final DatabaseDescriptor descriptor, final String table, final String field, final String where) {
        final String sql = "SELECT " + descriptor.quote() + field + descriptor.quote() + " as field FROM " + descriptor.quote() + table + descriptor.quote() + where + " ;";
        try (final Connection connection = DatabaseService.getConnection(descriptor, true);
                final Statement statement = connection.createStatement();
                final ResultSet result = statement.executeQuery(sql);) { // $NON-NLS-1$
            result.next();
            return result.getString("field");
        } catch (final SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    
}
