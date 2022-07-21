package com.ctrip.framework.drc.fetcher.event.transaction;

import com.ctrip.framework.drc.fetcher.resource.context.BaseTransactionContext;

/**
 * @Author Slight
 * Sep 19, 2019
 */
public interface BeginEvent<T extends BaseTransactionContext> extends com.ctrip.framework.drc.fetcher.event.transaction.BaseBeginEvent<T>, LWMAware, LWMSource {

}
