package com.clientservernn.client.additional;


public record ComparablePair<K extends Number,V>(K key, V value) implements Comparable<ComparablePair<? ,?>> {

    @Override
    public int compareTo(ComparablePair o) {
        return Long.compare(o.key().longValue(), this.key.longValue());
    }

}
