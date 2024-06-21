package fr.frinn.custommachinery.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

public class CMCollectors {

    public static <T> Collector<T, List<T>, List<T>> toImmutableList() {
        return Collector.of(ArrayList::new, List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }, Collections::unmodifiableList);
    }
}
