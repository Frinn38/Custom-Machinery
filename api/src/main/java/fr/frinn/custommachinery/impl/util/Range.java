package fr.frinn.custommachinery.impl.util;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Range<T extends Comparable<T>> {

    private final List<Restriction<T>> restrictions;

    public Range(List<Restriction<T>> restrictions) {
        this.restrictions = restrictions;
    }

    public List<Restriction<T>> getRestrictions() {
        return this.restrictions;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (Iterator<Restriction<T>> i = this.restrictions.iterator(); i.hasNext();) {
            Restriction<?> r = i.next();

            buf.append(r.toString());

            if (i.hasNext())
                buf.append( ',' );
        }

        return buf.toString();
    }

    /**
     * Search for a thing (T) that match the range.
     * @param things A list of things (T) to test against the range.
     * @return The greater thing (T) that match the range, or null if none match.
     */
    public T match(List<T> things) {
        T matched = null;
        for(T thing : things) {
            if (contains(thing)) {
                // valid - check if it is greater than the currently matched version
                if (matched == null || thing.compareTo(matched) > 0)
                    matched = thing;
            }
        }
        return matched;
    }

    /**
     * Test if a thing (T) match the range.
     * @param thing The thing (T) to test against the range.
     * @return True if the thing (T) match the range, false otherwise.
     */
    public boolean contains(T thing) {
        for(Restriction<T> restriction : this.restrictions)
            if(restriction.contains(thing))
                return true;
        return false;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;

        if(!(obj instanceof Range<?> range))
            return false;

        return Objects.equals(this.restrictions, range.restrictions);
    }

    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (this.restrictions == null ? 0 : this.restrictions.hashCode() );
        return hash;
    }
}
