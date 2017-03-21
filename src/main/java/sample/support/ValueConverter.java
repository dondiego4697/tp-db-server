package sample.support;

/**
 * Created by Denis on 21.03.2017.
 */
public class ValueConverter {

    public static String toHex(Integer value) {
        if(value < 0){
            return String.format("%1$06x", 0);
        }
        return String.format("%1$06x", value);
    }
}
