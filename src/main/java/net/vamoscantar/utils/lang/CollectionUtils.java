package net.vamoscantar.utils.lang;

import java.util.Arrays;
import java.util.List;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> T lastElement(T[] elements) {
        return elements[elements.length - 1];
    }

    public static <T> List<T> findNeighbours(List<T> list, T element) {
        var index = list.indexOf(element);

        var leftNeighbour = index <= 0 ? null : list.get(index - 1);
        var rightNeighbour = index >= list.size() - 1 ? null : list.get(index + 1);

        return Arrays.asList(leftNeighbour, rightNeighbour);
    }

}
