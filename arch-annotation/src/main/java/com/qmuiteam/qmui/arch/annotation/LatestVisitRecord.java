package com.qmuiteam.qmui.arch.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation can be used when you want to revert to last Fragment(Activity) that
 * was visited before the app exited.
 * <p>
 * if annotated for subclass of QMUIFragment, such as FragmentA, it must be annotated
 * in the subclass of QMUIFragmentActivity, such as FragmentActivityA. FragmentActivityA
 * must be annotated by FirstFragments or DefaultFirstFragment and the value must contain
 * FragmentA.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LatestVisitRecord {
}
