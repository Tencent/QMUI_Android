package com.qmuiteam.qmui.util;

public abstract class OnceReadValue<P, T> {

    private volatile boolean isRead = false;
    private T cacheValue;

    public T get(P param){
        if(isRead){
            return cacheValue;
        }
        synchronized (this){
            if(!isRead){
                cacheValue = read(param);
                isRead = true;
            }
        }
        return cacheValue;
    }

    protected abstract T read(P param);
}
