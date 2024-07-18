package com.telda;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.KeyException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CronSchedulerTest {
    AtomicBoolean flag = new AtomicBoolean();

    @BeforeEach
    void initialize() {
        flag.set(false);
    }

    @Test
    void testRunOnce() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(50);
            flag.set(true);
            return ret;
        }, 50, TimeUnit.MILLISECONDS);
        assertEquals(Job.JobStatus.WAITING, job.getStatus());

        Thread.sleep(75);
        assertEquals(Job.JobStatus.RUNNING, job.getStatus());
        assertFalse(flag.get());
        Thread.sleep(50);
        assertTrue(flag.get());
        assertEquals(ret, job.getReturnValues().get(0));
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testMultipleRuns() throws InterruptedException {
        int ret = 45;
        Job<String, Integer> job = CronScheduler.run(() -> {
            Thread.sleep(50);
            flag.set(true);
            return ret;
        }, 100, TimeUnit.MILLISECONDS);
        Thread.sleep(75);
        for (int i = 0; i < 10; i++) {
            assertEquals(Job.JobStatus.WAITING, job.getStatus());
            flag.set(false);
            Thread.sleep(50);
            assertEquals(Job.JobStatus.RUNNING, job.getStatus());
            assertFalse(flag.get());
            Thread.sleep(50);
            assertTrue(flag.get());
            assertEquals(ret, job.getReturnValues().get(job.getReturnValues().size() - 1));
        }
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void test3Runs() throws InterruptedException {
        int ret = 1032;
        Job<String, Integer> job = CronScheduler.run(() -> {
            Thread.sleep(50);
            flag.set(true);
            return ret;
        }, 100, TimeUnit.MILLISECONDS, 3);
        Thread.sleep(75);
        for (int i = 0; i < 3; i++) {
            assertEquals(Job.JobStatus.WAITING, job.getStatus());
            flag.set(false);
            Thread.sleep(50);
            assertEquals(Job.JobStatus.RUNNING, job.getStatus());
            assertFalse(flag.get());
            Thread.sleep(50);
            assertTrue(flag.get());
            assertEquals(ret, job.getReturnValues().get(job.getReturnValues().size() - 1));
        }
        Thread.sleep(50);
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    @Disabled
    void testCronRun() throws InterruptedException {
        int ret = 230;
        Job<String, Integer> job = CronScheduler.run(() -> {
            flag.set(true);
            return ret;
        }, "*/1 * * * *"); // Run every minute
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        flag.set(false);
        Thread.sleep(60000);
        assertTrue(flag.get());
        assertEquals(ret, job.getReturnValues().get(0));
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testPauseAndResume() throws InterruptedException {
        int ret = 102;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(2);
            flag.set(true);
            return ret;
        }, 2, TimeUnit.MILLISECONDS);
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        Thread.sleep(1);
        // Will execute after 1ms
        job.pause();
        assertEquals(Job.JobStatus.PAUSED, job.getStatus());
        Thread.sleep(1);
        // Clock didn't change
        job.resume();
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        Thread.sleep(2);
        // Should have been executing for 1ms
        assertEquals(Job.JobStatus.RUNNING, job.getStatus());
        Thread.sleep(1);
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testPauseAndResumeReset() throws InterruptedException {
        int ret = 102;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(2);
            flag.set(true);
            return ret;
        }, 2, TimeUnit.MILLISECONDS);
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        Thread.sleep(1);
        // Will execute after 1ms
        job.pause();
        assertEquals(Job.JobStatus.PAUSED, job.getStatus());
        Thread.sleep(1);
        // Clock didn't change
        job.resume(true);
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        Thread.sleep(3);
        // Should have been executing for 1ms
        assertEquals(Job.JobStatus.RUNNING, job.getStatus());
        Thread.sleep(1);
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testStop() throws InterruptedException {
        int ret = 102;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag.set(true);
            return ret;
        }, 2, TimeUnit.MILLISECONDS);
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        Thread.sleep(1);
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
        Thread.sleep(2);
        assertFalse(flag.get());
    }

    @Test
    void testRunWithID() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(2);
            flag.set(true);
            return ret;
        }, 1, TimeUnit.MILLISECONDS, "id1");
        assertEquals("id1", job.getId());
        assertEquals(Job.JobStatus.WAITING, job.getStatus());

        Thread.sleep(1);
        assertEquals(Job.JobStatus.RUNNING, job.getStatus());
        assertFalse(flag.get());
        Thread.sleep(2);
        assertTrue(flag.get());
        assertEquals(ret, job.getReturnValues().get(0));
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testUniqueID() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag.set(true);
            return ret;
        }, 1, TimeUnit.MILLISECONDS, "id1");
        assertThrows(KeyException.class, () ->{
            CronScheduler.runOnce(() -> {
                Thread.sleep(2);
                flag.set(true);
                return ret;
            }, 1, TimeUnit.MILLISECONDS, "id1");
        });
        job.stop();
    }

    @Test
    void testUniqueIDAndDelete() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag.set(true);
            return ret;
        }, 1, TimeUnit.MILLISECONDS, "id1");
        job.stop();
        assertDoesNotThrow(() ->{
            CronScheduler.runOnce(() -> {
                Thread.sleep(2);
                flag.set(true);
                return ret;
            }, 1, TimeUnit.MILLISECONDS, "id1");
        });
        Thread.sleep(2);
    }

    @Test
    void testRetrieveWithID() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag.set(true);
            return ret;
        }, 5, TimeUnit.HOURS, "id1");

        Job<String, Integer> job2 = CronScheduler.getJob("id1");
        assertEquals(job, job2);
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
        assertEquals(Job.JobStatus.STOPPED, job2.getStatus());
    }

    @Test
    void testRetrieveWithGeneratedID() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag.set(true);
            return ret;
        }, 5, TimeUnit.HOURS);

        Job<String, Integer> job2 = CronScheduler.getJob(job.getId());
        assertEquals(job, job2);
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
        assertEquals(Job.JobStatus.STOPPED, job2.getStatus());
    }

    @Test
    void testRetrieveWithIDAfterStopping() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag.set(true);
            return ret;
        }, 5, TimeUnit.HOURS, "id1");
        job.stop();
        Job<String, Integer> job2 = CronScheduler.getJob("id1");
        assertNull(job2);
    }

    @Test
    void testExecutionTime() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(1);
            flag.set(true);
            return ret;
        }, 1, TimeUnit.MILLISECONDS);
        Thread.sleep(3);
        assertEquals(1,1e-5, job.getExecutionTimes().get(0));
    }

    @Test
    void testCallback() throws InterruptedException {
        int ret = 15;
        AtomicInteger actualRet = new AtomicInteger();
        AtomicReference<Double> executionTime = new AtomicReference<>((double) 0);
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(1);
            flag.set(true);
            return ret;
        }, 1, TimeUnit.MILLISECONDS);
        job.addOnFinishCallback((Integer result, double wait) -> {
            actualRet.set(result);
            executionTime.set(wait);
        });
        Thread.sleep(3);
        assertEquals(ret, actualRet.get());
        assertEquals(1, 1e-5, executionTime.get());
    }

    @Test
    void testNegativeRuns() {
        int ret = 20;
        assertThrows(IllegalArgumentException.class, () -> {
            Job<String, Integer> job = CronScheduler.run(() -> {
                Thread.sleep(1);
                flag.set(true);
                return ret;
            }, 1, TimeUnit.MILLISECONDS, -1);
        });
    }
}