package com.ctrip.framework.drc.console.mock.helpers;

/**
 * @author shenhaibo
 * @version 1.0
 * date: 2020-08-21
 */
public interface ConfigChanged<T extends Enum<T>> {

    T getChangedType();
}