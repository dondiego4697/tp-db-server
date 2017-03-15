package sample.sql;

/**
 * Created by Denis on 15.03.2017.
 */
public class SQLService {

    public interface Callback{
        void onSuccess();
        void onError(int err);
    }


}
