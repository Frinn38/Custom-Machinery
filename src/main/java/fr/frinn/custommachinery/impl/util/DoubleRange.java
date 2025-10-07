package fr.frinn.custommachinery.impl.util;

import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class DoubleRange extends Range<Double> {

    public static final NamedCodec<DoubleRange> CODEC = NamedCodec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(createFromString(s));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }, DoubleRange::toString, "Double range");

    private static final Map<String, DoubleRange> CACHE_SPEC = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Restriction<Double> EVERYTHING = new Restriction<>(null, false, null, false);

    public static final DoubleRange ALL = new DoubleRange(Collections.singletonList(EVERYTHING));

    private DoubleRange(List<Restriction<Double>> restrictions) {
        super(restrictions);
    }

    /**
     * <p>
     * Create a double range from a string representation
     * </p>
     * Some spec examples are:
     * <ul>
     * <li><code>1</code> Accept only 1</li>
     * <li><code>[1.0,42.0)</code> Accept 1.0 (included) to 42.0 (not included)</li>
     * <li><code>[1.0,42.0]</code> Accept 1.0 to 42.0 (both included)</li>
     * <li><code>[1.0,)</code> Accept 1.0 and higher</li>
     * <li><code>(,1.0],[42.0,)</code> Accept up to 1.0 (included) and 42.0 or higher</li>
     * </ul>
     *
     * @param spec string representation of a double range
     * @return a new {@link DoubleRange} object that represents the spec
     * @throws IllegalArgumentException If the String couldn't be parsed as a {@link DoubleRange}.
     *
     */
    public static DoubleRange createFromString(String spec) throws IllegalArgumentException {
        if(spec == null)
            throw new IllegalArgumentException("Can't parse a double range fromm a null String");

        if(spec.isEmpty() || spec.equals("*"))
            return ALL;

        DoubleRange cached = CACHE_SPEC.get(spec);
        if(cached != null)
            return cached;

        List<Restriction<Double>> restrictions = new ArrayList<>();
        String process = spec;
        Double upperBound = null;
        Double lowerBound = null;

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

            Restriction<Double> restriction = parseRestriction(process.substring(0, index + 1));
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
                    double bound = Double.parseDouble(process);
                    restrictions.add(new Restriction<>(bound, true, bound, true));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid integer range, \"" + process + "\" is not a number");
                }
            }
        }

        cached = new DoubleRange(restrictions);
        CACHE_SPEC.put(spec, cached);
        return cached;
    }

    public static DoubleRange of(Object o) throws IllegalArgumentException {
        if(o == null)
            throw new IllegalArgumentException("Cannot build IntRange from null");

        if(o instanceof CharSequence string)
            return createFromString(string.toString());

        if(o instanceof Number number)
            return new DoubleRange(Collections.singletonList(new Restriction<>(number.doubleValue(), true, number.doubleValue(), true)));

        throw new IllegalArgumentException("Cannot build IntRange from " + o);
    }

    private static Restriction<Double> parseRestriction(String spec ) throws IllegalArgumentException {
        boolean lowerBoundInclusive = spec.startsWith("[");
        boolean upperBoundInclusive = spec.endsWith("]");

        String process = spec.substring(1, spec.length() - 1).trim();

        Restriction<Double> restriction;

        int index = process.indexOf(',');

        if(index < 0) {
            if(!lowerBoundInclusive || !upperBoundInclusive)
                throw new IllegalArgumentException("Single version must be surrounded by []: " + spec);

            Double version = Double.parseDouble(process);

            restriction = new Restriction<>(version, lowerBoundInclusive, version, upperBoundInclusive);
        }
        else {
            String lowerBound = process.substring(0, index).trim();
            String upperBound = process.substring(index + 1).trim();
            if(lowerBound.equals(upperBound))
                throw new IllegalArgumentException("Range cannot have identical boundaries: " + spec);

            Double lowerVersion = null;
            if(lowerBound.length() > 0)
                lowerVersion = Double.parseDouble(lowerBound);

            Double upperVersion = null;
            if(upperBound.length() > 0)
                upperVersion = Double.parseDouble(upperBound);

            if(upperVersion != null && lowerVersion != null && upperVersion.compareTo(lowerVersion) < 0)
                throw new IllegalArgumentException("Range defies version ordering: " + spec);

            restriction = new Restriction<>(lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive);
        }

        return restriction;
    }
}