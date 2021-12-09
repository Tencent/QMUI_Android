package com.qmuiteam.qmui.util;

public abstract class OnceReadValue<P, T> {

    private boolean isRead = false;
    private T cacheValue;

    public T get(P param){
        if(!isRead){
            isRead = true;
            cacheValue = read(param);
        }
        return cacheValue;
    }

    protected abstract T read(P param);
}
