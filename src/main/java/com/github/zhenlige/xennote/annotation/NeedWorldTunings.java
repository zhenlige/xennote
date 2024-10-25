package com.github.zhenlige.xennote.annotation;

import com.github.zhenlige.xennote.WorldTunings;

import java.lang.annotation.Documented;

/**
 * The method is or calls {@link WorldTunings}{@code .getCurrent()} and thus needs to be called after synchronizing {@link WorldTunings}.
 */
@Documented
public @interface NeedWorldTunings {
}
