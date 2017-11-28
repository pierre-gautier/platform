package platform.jetter.model;

import java.util.Collection;

import platform.utils.interfaces.IService;

@SuppressWarnings("nls")
public class TimingService<T>
        implements IService<T> {
    
    private final IService<T> service;
    
    public TimingService(final IService<T> service) {
        this.service = service;
    }
    
    @Override
    public void delete(final Collection<String> ids) {
        final long start = System.currentTimeMillis();
        this.service.delete(ids);
        System.err.println("took " + (System.currentTimeMillis() - start) + " to delete " + ids.size() + " ids");
    }
    
    @Override
    public void delete(final String id) {
        final long start = System.currentTimeMillis();
        this.service.delete(id);
        System.err.println("took " + (System.currentTimeMillis() - start) + " to delete " + id);
    }
    
    @Override
    public Collection<T> getAll() {
        final long start = System.currentTimeMillis();
        final Collection<T> entities = this.service.getAll();
        System.err.println("took " + (System.currentTimeMillis() - start) + " to getAll " + entities.size() + " entities");
        return entities;
    }
    
    @Override
    public T getById(final String id) {
        final long start = System.currentTimeMillis();
        final T entity = this.service.getById(id);
        System.err.println("took " + (System.currentTimeMillis() - start) + " to getById " + id);
        return entity;
    }
    
    @Override
    public Collection<T> getByType(final String type) {
        final long start = System.currentTimeMillis();
        final Collection<T> entities = this.service.getByType(type);
        System.err.println("took " + (System.currentTimeMillis() - start) + " to getByType " + type + " " + entities.size() + " entities");
        return entities;
    }
    
    @Override
    public void post(final Collection<T> entities) {
        final long start = System.currentTimeMillis();
        this.service.post(entities);
        System.err.println("took " + (System.currentTimeMillis() - start) + " to post " + entities.size() + " entities");
    }
    
    @Override
    public void put(final Collection<T> entities) {
        final long start = System.currentTimeMillis();
        this.service.put(entities);
        System.err.println("took " + (System.currentTimeMillis() - start) + " to put " + entities.size() + " entities");
    }
    
}
