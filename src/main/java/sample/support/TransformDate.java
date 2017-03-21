package sample.support;

/**
 * Created by Denis on 21.03.2017.
 */
public class TransformDate {


    public static String transformWithAppend00(String date){
        final StringBuilder time = new StringBuilder(date);
        time.replace(10, 11, "T");
        time.append(":00");
        return time.toString();
    }

    public static String transformWithAppend0300(String date){
        final StringBuilder time = new StringBuilder(date);
        time.replace(10, 11, "T");
        time.append("+03:00");
        return time.toString();
    }
}
