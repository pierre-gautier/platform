package platform.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.internal.ThreadLocalSessionContext;

import platform.sql.DatabaseDescriptor;
import platform.utils.Strings;
import platform.utils.interfaces.IDao;

public class HibernateDao<MODEL, ID, ENTITY>
        implements IDao<MODEL, ID> {
    
    private static Collection<Class<?>> ENTITIES = new ArrayList<>();
    
    public static void register(final Class<?> entity) {
        HibernateDao.ENTITIES.add(entity);
    }
    
    private final SessionFactory               sessionFactory;
    private final IEntityMapper<ENTITY, MODEL> mapper;
    
    public HibernateDao(final DatabaseDescriptor database, final IEntityMapper<ENTITY, MODEL> mapper) {
        final StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.URL, database.getUrl() + Strings.SLASH + database.getDb())
                .applySetting(AvailableSettings.USER, database.getUser())
                .applySetting(AvailableSettings.PASS, database.getPassword())
                // .applySetting(AvailableSettings.SHOW_SQL, Boolean.TRUE.toString())
                .applySetting(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, ThreadLocalSessionContext.class.getName())
                .build();
        final MetadataSources sources = new MetadataSources(standardRegistry);
        for (final Class<?> entity : HibernateDao.ENTITIES) {
            sources.addAnnotatedClass(entity);
        }
        this.sessionFactory = sources.getMetadataBuilder().build().getSessionFactoryBuilder().build();
        this.mapper = mapper;
    }
    
    @Override
    public final void create(final Collection<MODEL> toCreate) {
        try {
            this.sessionFactory.getCurrentSession().beginTransaction();
            for (final MODEL node : toCreate) {
                final ENTITY entity = this.mapper.toEntity(node);
                if (entity != null) {
                    this.sessionFactory.getCurrentSession().saveOrUpdate(entity);
                }
            }
            this.sessionFactory.getCurrentSession().getTransaction().commit();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            this.sessionFactory.getCurrentSession().getTransaction().rollback();
        }
    }
    
    @Override
    public final void delete(final Collection<MODEL> toDelete) {
        try {
            this.sessionFactory.getCurrentSession().beginTransaction();
            for (final MODEL node : toDelete) {
                final ENTITY entity = this.mapper.toEntity(node);
                if (entity != null) {
                    this.sessionFactory.getCurrentSession().delete(entity);
                }
            }
            this.sessionFactory.getCurrentSession().getTransaction().commit();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            this.sessionFactory.getCurrentSession().getTransaction().rollback();
        }
    }
    
    @Override
    public final Collection<MODEL> retrieve(final Collection<ID> ids) {
        try {
            this.sessionFactory.getCurrentSession().beginTransaction();
            final CriteriaBuilder criteriaBuilder = this.sessionFactory.getCurrentSession().getCriteriaBuilder();
            final CriteriaQuery<ENTITY> criteriaQuery = criteriaBuilder.createQuery(this.mapper.entityClass());
            final Root<ENTITY> root = criteriaQuery.from(this.mapper.entityClass());
            criteriaQuery.select(root).where(root.get(this.mapper.entityId()).in(Arrays.asList(ids)));
            final Collection<ENTITY> entities = this.sessionFactory.getCurrentSession().createQuery(criteriaQuery).getResultList();
            final Collection<MODEL> objects = new ArrayList<>(entities.size());
            for (final ENTITY entity : entities) {
                final MODEL object = this.mapper.toModel(entity);
                if (object != null) {
                    objects.add(object);
                }
            }
            return objects;
        } catch (final RuntimeException e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            this.sessionFactory.getCurrentSession().getTransaction().commit();
        }
    }
    
    @Override
    public final void update(final Collection<MODEL> toUpdate) {
        this.create(toUpdate);
    }
    
}
