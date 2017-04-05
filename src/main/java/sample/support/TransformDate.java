package sample.support;

/**
 * Created by Denis on 21.03.2017.
 */
public class TransformDate {


    public static String transformWithAppend00(String date) {
        final StringBuilder time = new StringBuilder(date);
        time.replace(10, 11, "T");
        time.append(":00");
        return time.toString();
    }

    public static String transformWithAppend0300(String date) {
        final StringBuilder time = new StringBuilder(date);

        if (time.substring(time.length() - 3).equals("+00")) {
            time.replace(time.length() - 3, time.length(), "");
        } else if (time.substring(time.length() - 3).equals("+03")) {
            time.replace(time.length() - 3, time.length(), "");
        }
        time.replace(10, 11, "T");
        time.append("+03:00");
        return time.toString();
    }

    public static String replaceOnSpace(String date) {
        final StringBuilder time = new StringBuilder(date);
        time.replace(10, 11, " ");
        return time.toString();
    }
}
