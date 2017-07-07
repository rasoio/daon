package daon.analysis.ko.model;

import org.apache.lucene.util.IntsRef;

import java.io.Serializable;
import java.util.Arrays;


public class ConnectionIntsRef implements Comparable<ConnectionIntsRef>, Serializable {

    private IntsRef input;

    private long freq;

    public ConnectionIntsRef(IntsRef intsRef, long freq) {

        input = intsRef;

        this.freq = freq;
    }

    public IntsRef getInput() {
        return input;
    }

    public long getFreq() {
        return freq;
    }

    public void setFreq(long freq) {
        this.freq = freq;
    }

    @Override
    public int compareTo(ConnectionIntsRef other) {
        return this.getInput().compareTo(other.getInput());
    }


    @Override
    public String toString() {
        return "ConnectionIntsRef{" +
                "input=" + input +
                ", freq=" + freq +
                '}';
    }
}
