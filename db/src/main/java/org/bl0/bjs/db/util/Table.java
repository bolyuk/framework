package org.bl0.bjs.db.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.bl0.bjs.db.BJSDB;
import org.bl0.bjs.db.interfaces.HasID;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public class Table<T, ID extends Serializable> {

    protected final BJSDB db;
    protected final Class<T> entityClass;
    protected final Session session;

    public Table(BJSDB db, Class<T> entityClass) {
        this.db = db;
        this.entityClass = entityClass;
        this.session = null;
    }

    public Table(BJSDB db, Class<T> entityClass, Session session) {
        this.db = db;
        this.entityClass = entityClass;
        this.session = session;
    }

    public Optional<T> findById(ID id) {
        return db.transaction(session,s -> Optional.ofNullable(s.find(entityClass, id)));
    }

    public List<T> findAll() {
        return db.transaction(session,s ->
                s.createQuery("from " + entityClass.getSimpleName(), entityClass)
                        .getResultList()
        );
    }

    public T save(T entity) {
        return db.transaction(session,s -> {
            Object id = ((HasID<?>) entity).getID();
            if (id == null) s.persist(entity);
            else s.merge(entity);
            return entity;
        });
    }

    public List<T> save(List<T> entities) {
        return db.transaction(session, s -> {
            for (T e : entities) {
                Object id = ((HasID<?>) e).getID();
                if (id == null) s.persist(e);
                else s.merge(e);
            }
            s.flush();
            return entities;
        });
    }

    public void delete(T entity) {
        db.transaction(session,s -> {
            s.remove(entity);
            return null;
        });
    }

    public void deleteById(ID id) {
        db.transaction(session,s -> {
            T ref = s.find(entityClass, id);
            if (ref != null) s.remove(ref);
            return null;
        });
    }

    public <V> Optional<T> findOneBy(String field, V value) {
        return db.transaction(session, (s) ->
                s.createQuery(
                                "from " + entityClass.getSimpleName()
                                        + " e where e." + field + " = :value",
                                entityClass
                        )
                        .setParameter("value", value)
                        .getResultStream()
                        .findFirst()
        );
    }

    public List<T> findAllIn(String field, Collection<?> values) {
        if (values == null || values.isEmpty()) return List.of(); // чтобы не делать IN ()
        final String hql = "from " + entityClass.getSimpleName() + " e where e." + field + " in (:vals)";
        return db.transaction(session, s ->
                s.createQuery(hql, entityClass)
                        .setParameterList("vals", values)
                        .getResultList()
        );
    }


    public <V> List<T> findBy(String field, V value) {
        return db.transaction(session, (s) ->
                s.createQuery(
                                "from " + entityClass.getSimpleName()
                                        + " e where e." + field + " = :value",
                                entityClass
                        )
                        .setParameter("value", value)
                        .getResultList()
        );
    }

    public List<T> findBy(Function<CriteriaBuilder, Predicate[]> where) {
        return db.transaction(session, s -> {
            CriteriaBuilder cb = s.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            Predicate[] preds = where.apply(cb);
            cq.select(root).where(preds);
            return s.createQuery(cq).getResultList();
        });
    }

    public List<T> findBy(Map<String, ?> filters) {
        return db.transaction(session, s -> {
            String alias = "e";
            StringBuilder hql = new StringBuilder("from ")
                    .append(entityClass.getSimpleName()).append(" ").append(alias).append(" where 1=1");

            int i = 0;
            Map<String, Object> params = new HashMap<>();

            for (Map.Entry<String, ?> entry : filters.entrySet()) {
                String field = entry.getKey();
                Object val = entry.getValue();
                if (val == null) {
                    hql.append(" and ").append(alias).append(".").append(field).append(" is null");
                } else if (val instanceof Collection<?>) {
                    String p = "p" + (i++);
                    hql.append(" and ").append(alias).append(".").append(field).append(" in (:").append(p).append(")");
                    params.put(p, val);
                } else {
                    String p = "p" + (i++);
                    hql.append(" and ").append(alias).append(".").append(field).append(" = :").append(p);
                    params.put(p, val);
                }
            }

            var q = s.createQuery(hql.toString(), entityClass);
            params.forEach(q::setParameter);
            return q.getResultList();
        });
    }

    public static <R,B extends Serializable> Table<R, B> build(Class<R> r, Class<B> b, BJSDB db, Session session) {
        return new Table<R, B>(db, r, session);
    }
}