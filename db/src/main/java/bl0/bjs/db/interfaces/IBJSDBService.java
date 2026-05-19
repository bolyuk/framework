package bl0.bjs.db.interfaces;

import bl0.bjs.services.interfaces.IService;
import org.hibernate.Session;

import java.util.function.Function;

public interface IBJSDBService extends IService {
    <R> R transaction(Function<Session, R> work);
    <R> R transaction(Session session, Function<Session, R> work);
}
