package com.qmuiteam.qmui.arch.annotation;

import com.qmuiteam.qmui.arch.QMUIFragment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultFirstFragment {
    Class<? extends QMUIFragment> value();
}
