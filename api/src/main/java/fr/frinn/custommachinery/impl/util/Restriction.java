package fr.frinn.custommachinery.impl.util;

import org.jetbrains.annotations.Nullable;

public record Restriction<T extends Comparable<T>>(@Nullable T lowerBound, boolean lowerBoundInclusive,
                                                   @Nullable T upperBound, boolean upperBoundInclusive) {

    public boolean contains(T thing) {
        if (this.lowerBound != null) {
            int comparison = this.lowerBound.compareTo(thing);

            if (comparison == 0 && !this.lowerBoundInclusive)
                return false;

            if (comparison > 0)
                return false;
        }

        if (this.upperBound != null) {
            int comparison = this.upperBound.compareTo(thing);

            if (comparison == 0 && !this.upperBoundInclusive)
                return false;

            return comparison >= 0;
        }

        return true;
    }

    public String toFormattedString() {
        if(this.lowerBound == null && this.upperBound == null)
            return "Any";
        else if(this.lowerBound != null && this.upperBound == null)
            return (this.lowerBoundInclusive ? "From " : "Greater than ") + this.lowerBound;
        else if(this.lowerBound == null)
            return (this.upperBoundInclusive ? "Up to " : "Less than ") + this.upperBound;
        else if(this.lowerBound == this.upperBound)
            return "Only " + this.lowerBound;
        else
            return "Between " + this.lowerBound + (this.lowerBoundInclusive ? " (included)" : "") + " and " + this.upperBound + (this.upperBoundInclusive ? " (included)" : "");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof Restriction<?> restriction))
            return false;

        if (this.lowerBound != null && !this.lowerBound.equals(restriction.lowerBound))
            return false;
        else if (restriction.lowerBound != null)
            return false;

        if (this.lowerBoundInclusive != restriction.lowerBoundInclusive)
            return false;

        if (this.upperBound != null && !this.upperBound.equals(restriction.upperBound))
            return false;
        else if (restriction.upperBound != null)
            return false;

        return this.upperBoundInclusive == restriction.upperBoundInclusive;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(lowerBoundInclusive() ? '[' : '(');
        if (lowerBound() != null)
            buf.append(lowerBound());

        buf.append(',');

        if (upperBound() != null)
            buf.append(upperBound());

        buf.append(upperBoundInclusive() ? ']' : ')');

        return buf.toString();
    }
}
