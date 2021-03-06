package org.hswebframework.task.scheduler

import org.hswebframework.task.DefaultEventSubscriberPublisher
import org.hswebframework.task.job.JobDetail
import org.hswebframework.task.lock.LocalLockManager
import org.hswebframework.task.scheduler.memory.InMemoryJobRepository
import org.hswebframework.task.scheduler.memory.InMemoryScheduleHistoryRepository
import org.hswebframework.task.scheduler.memory.InMemoryTaskRepository
import org.hswebframework.task.worker.DefaultTaskWorker
import org.hswebframework.task.worker.DefaultTaskWorkerManager
import org.hswebframework.task.worker.TaskWorkerManager
import org.hswebframework.task.worker.executor.RunnableTaskBuilder
import org.hswebframework.task.worker.executor.TaskExecutor
import org.hswebframework.task.worker.executor.supports.DefaultRunnableTaskBuilder
import org.hswebframework.task.worker.executor.supports.JavaMethodInvokeTaskProvider
import org.hswebframework.task.worker.executor.supports.ThreadPoolTaskExecutor
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author zhouhao
 * @since 1.0.0
 */
class DefaultTaskSchedulerTest extends Specification {

    TaskScheduler scheduler;


    def setup() {
        //初始化taskWorker
        TaskWorkerManager manager = new DefaultTaskWorkerManager()

        RunnableTaskBuilder builder = new DefaultRunnableTaskBuilder()
        builder.addProvider(new JavaMethodInvokeTaskProvider())

        TaskExecutor executor = new ThreadPoolTaskExecutor(builder)

        DefaultTaskWorker worker = new DefaultTaskWorker(executor)
        worker.setId("worker-001")
        worker.setHost("localhost")
        worker.setGroups(["default"] as String[])
        worker.setName("测试worker")
        manager.register(worker)

        //创建一个任务
        def jobRepository = new InMemoryJobRepository()
        jobRepository.save(new JobDetail(
                id: "testJob",
                jobType: "java-method",
                content: "org.hswebframework.task.scheduler.TestJob.execute",
                executeTimeOut: 100000,
                parallel: false
        ))

        //初始化调度器

        scheduler = new DefaultTaskScheduler()
        scheduler.setEventPublisher(new DefaultEventSubscriberPublisher())
        scheduler.setHistoryRepository(new InMemoryScheduleHistoryRepository())
        scheduler.setJobRepository(jobRepository)
        scheduler.setSchedulerFactory(new DefaultSchedulerFactory())
        scheduler.setSchedulerId("test")
        scheduler.setTaskRepository(new InMemoryTaskRepository())
        scheduler.setLockManager(new LocalLockManager())
        scheduler.setTaskFactory(new DefaultTaskFactory())
        scheduler.setTaskWorkerManager(manager)
        scheduler.startup();
    }

    def "测试启动调度"() {
        given:
        scheduler.scheduleJob("testJob", Schedulers.period(Executors.newSingleThreadScheduledExecutor(), 100, 100, TimeUnit.MILLISECONDS))
        Thread.sleep(5000)
        scheduler.shutdown(true)
        expect:
        TestJob.atomicLong.get() != 0

    }
}
