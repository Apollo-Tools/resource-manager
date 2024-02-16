package at.uibk.dps.rm.util.misc;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * The MultiValuedMapCollector class is a custom implementation of the Java Collector interface
 * for MultiValuedMap.
 */
public class MultiValuedMapCollector<T, K, V> implements Collector<T, MultiValuedMap<K, V>,
    MultiValuedMap<K, V>> {

    private final Function<T, K> keyMapper;
    private final Function<T, V> valueMapper;

    /**
     * Create a new instance with the keyMapper and valueMappers.
     *
     * @param keyMapper the key mapper
     * @param valueMapper the function mapper
     */
    public MultiValuedMapCollector(Function<T, K> keyMapper, Function<T, V> valueMapper) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public Supplier<MultiValuedMap<K, V>> supplier() {
        return ArrayListValuedHashMap::new;
    }

    @Override
    public BiConsumer<MultiValuedMap<K, V>, T> accumulator() {
        return (map, element) -> map.put(keyMapper.apply(element), valueMapper.apply(element));
    }

    @Override
    public BinaryOperator<MultiValuedMap<K, V>> combiner() {
        return (map1, map2) -> {
            map1.putAll(map2);
            return map1;
        };
    }

    @Override
    public Function<MultiValuedMap<K, V>, MultiValuedMap<K, V>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.noneOf(Characteristics.class);
    }
}
