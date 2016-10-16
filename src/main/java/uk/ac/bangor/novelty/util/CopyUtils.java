package uk.ac.bangor.novelty.util;

import java.io.*;

/**
 * @author Will Faithfull
 */
public class CopyUtils {

    public static <T> T deepCopy(T t, Class<T> clazz) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(t);
            oos.flush();
        } finally {
            oos.close();
            bos.close();
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        return clazz.cast(new ObjectInputStream(bis).readObject());
    }
}