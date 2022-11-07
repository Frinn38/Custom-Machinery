package fr.frinn.custommachinery.impl.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class IntRange extends Range<Integer> {

    public static final Codec<IntRange> CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(createFromString(s));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e.getMessage());
        }
    }, IntRange::toString);

    private static final Map<String, IntRange> CACHE_SPEC = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Restriction<Integer> EVERYTHING = new Restriction<>(null, false, null, false);

    public static final IntRange ALL = new IntRange(Collections.singletonList(EVERYTHING));

    private IntRange(List<Restriction<Integer>> restrictions) {
        super(restrictions);
    }

    /**
     * <p>
     * Create an integer range from a string representation
     * </p>
     * Some spec examples are:
     * <ul>
     * <li><code>1</code> Accept only 1</li>
     * <li><code>[1,42)</code> Accept 1 (included) to 42 (not included)</li>
     * <li><code>[1,42]</code> Accept 1 to 42 (both included)</li>
     * <li><code>[1,)</code> Accept 1 and higher</li>
     * <li><code>(,1],[42,)</code> Accept up to 1 (included) and 42 or higher</li>
     * </ul>
     *
     * @param spec string representation of an integer range
     * @return a new {@link IntRange} object that represents the spec
     * @throws IllegalArgumentException If the String couldn't be parsed as an {@link IntRange}.
     *
     */
    public static IntRange createFromString(String spec) throws IllegalArgumentException {
        if(spec == null)
            throw new IllegalArgumentException("Can't parse an integer range fromm a null String");

        if(spec.isEmpty() || spec.equals("*"))
            return ALL;

        IntRange cached = CACHE_SPEC.get(spec);
        if(cached != null)
            return cached;

        List<Restriction<Integer>> restrictions = new ArrayList<>();
        String process = spec;
        Integer upperBound = null;
        Integer lowerBound = null;

        while(process.startsWith( "[" ) || process.startsWith( "(" )) {
            int index1 = process.indexOf( ')' );
            int index2 = process.indexOf( ']' );

            int index = index2;
            if(index2 < 0 || index1 < index2) {
                if(index1 >= 0)
                    index = index1;
            }

            if(index < 0)
                throw new IllegalArgumentException("Unbounded range: \"" + spec + "\"");

            Restriction<Integer> restriction = parseRestriction(process.substring(0, index + 1));
            if(lowerBound == null)
                lowerBound = restriction.lowerBound();

            if(upperBound != null) {
                if(restriction.lowerBound() == null || restriction.lowerBound().compareTo(upperBound) < 0)
                    throw new IllegalArgumentException("Ranges overlap: \"" + spec + "\"");
            }
            restrictions.add(restriction);

            upperBound = restriction.upperBound();

            process = process.substring(index + 1).trim();

            if(process.startsWith(","))
                process = process.substring(1).trim();
        }

        if(process.length() > 0) {
            if(restrictions.size() > 0)
                throw new IllegalArgumentException("Only fully-qualified sets allowed in multiple set scenario: \"" + spec + "\"");
            else {
                try {
                    Integer bound = Integer.parseInt(process);
                    restrictions.add(new Restriction<>(bound, true, bound, true));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid integer range, \"" + process + "\" is not a number");
                }
            }
        }

        cached = new IntRange(restrictions);
        CACHE_SPEC.put(spec, cached);
        return cached;
    }

    private static Restriction<Integer> parseRestriction(String spec ) throws IllegalArgumentException {
        boolean lowerBoundInclusive = spec.startsWith("[");
        boolean upperBoundInclusive = spec.endsWith("]");

        String process = spec.substring(1, spec.length() - 1).trim();

        Restriction<Integer> restriction;

        int index = process.indexOf(',');

        if(index < 0) {
            if(!lowerBoundInclusive || !upperBoundInclusive)
                throw new IllegalArgumentException("Single version must be surrounded by []: " + spec);

            Integer version = Integer.parseInt(process);

            restriction = new Restriction<>(version, lowerBoundInclusive, version, upperBoundInclusive);
        }
        else {
            String lowerBound = process.substring(0, index).trim();
            String upperBound = process.substring(index + 1).trim();
            if(lowerBound.equals(upperBound))
                throw new IllegalArgumentException("Range cannot have identical boundaries: " + spec);

            Integer lowerVersion = null;
            if(lowerBound.length() > 0)
                lowerVersion = Integer.parseInt(lowerBound);

            Integer upperVersion = null;
            if(upperBound.length() > 0)
                upperVersion = Integer.parseInt(upperBound);

            if(upperVersion != null && lowerVersion != null && upperVersion.compareTo(lowerVersion) < 0)
                throw new IllegalArgumentException("Range defies version ordering: " + spec);

            restriction = new Restriction<>(lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive);
        }

        return restriction;
    }
}
