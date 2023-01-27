package com.clientservernn.server.guiFX;

public interface ComparableTo <T extends Number> {
    T getCompareIndex ();

    static <T extends ComparableTo<?>>  int compareByIndex (T ob1, T ob2){
        return Long.compare(ob1.getCompareIndex().longValue(), ob2.getCompareIndex().longValue());
    }
    static <T extends ComparableTo<?>>  int compareReverseIndex (T ob1, T ob2){
        return Long.compare(ob2.getCompareIndex().longValue(), ob1.getCompareIndex().longValue());
    }
}
