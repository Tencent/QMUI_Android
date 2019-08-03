package com.qmuiteam.qmui.arch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All possibilities for the first fragment loaded after starting the activity
 * used for subclasses of QMUIFragmentActivity
 * the value must be subclasses of QMUIFragment
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface FirstFragments {
    Class<?>[] value();
}
