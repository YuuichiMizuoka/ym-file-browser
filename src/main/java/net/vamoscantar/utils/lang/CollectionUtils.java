package net.vamoscantar.utils.lang;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> T lastElement(T[] elements) {
        return elements[elements.length - 1];
    }

}
