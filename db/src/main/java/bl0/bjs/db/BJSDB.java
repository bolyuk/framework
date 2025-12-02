package bl0.bjs.db;

import bl0.bjs.db.util.HibernateUtil;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.logging.ILogger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.function.Function;

public class BJSDB {
    private final ILogger logger;
    private SessionFactory sessionFactory;

    public BJSDB(IContext ctx){
        this.logger = ctx.generateLogger(this.getClass());
    }

    public void connect(String host, String dbName, String username, String password){
        sessionFactory = HibernateUtil.buildSessionFactory(host, dbName, username, password);
    }

    public boolean isConnected(){
        return sessionFactory != null;
    }

    public void shutdown(){
        throwIfFactoryNull();
        sessionFactory.close();
    }

    public Session openSession(){
        throwIfFactoryNull();
        return sessionFactory.openSession();
    }

    public <R> R transaction(Function<Session, R> work) {
        throwIfFactoryNull();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                R result = work.apply(session);
                tx.commit();
                return result;
            } catch (Exception e) {
                tx.rollback();
                logger.err("DB transaction failed", e);
                throw e;
            }
        }
    }

    public <R> R transaction(Session session, Function<Session, R> work) {
        if (session == null) {
            return transaction(work);
        }

        Transaction tx = session.getTransaction();
        boolean isNew = !tx.isActive();

        if (isNew) {
            tx.begin();
        }

        try {
            R result = work.apply(session);
            if (isNew) {
                tx.commit();
            }
            return result;
        } catch (Exception e) {
            if (isNew) {
                tx.rollback();
            }
            logger.err("DB transaction failed", e);
            throw e;
        }
    }

    private void throwIfFactoryNull(){
        if (sessionFactory == null)
            throw new NullPointerException("sessionFactory is null");
    }

}
