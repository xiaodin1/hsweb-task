package org.hswebframework.task.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ScheduleLockManager {

    Lock tryGetLock(String lockName, long timeout, TimeUnit timeUnit);

}
