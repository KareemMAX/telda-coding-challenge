package com.telda;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CronSchedulerTest {
    boolean flag = false;

    @BeforeEach
    void initialize() {
        flag = false;
    }

    @Test
    void testRunOnce() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(2);
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS);
        assertEquals(Job.JobStatus.WAITING, job.getStatus());

        Thread.sleep(1);
        assertEquals(Job.JobStatus.RUNNING, job.getStatus());
        assertFalse(flag);
        Thread.sleep(2);
        assertTrue(flag);
        assertEquals(ret, job.getReturnValues().get(0));
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testMultipleRuns() throws InterruptedException {
        int ret = 45;
        Job<String, Integer> job = CronScheduler.run(() -> {
            Thread.sleep(2);
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 10; i++) {
            assertEquals(Job.JobStatus.WAITING, job.getStatus());
            flag = false;
            Thread.sleep(1);
            assertEquals(Job.JobStatus.RUNNING, job.getStatus());
            assertFalse(flag);
            Thread.sleep(2);
            assertTrue(flag);
            assertEquals(ret, job.getReturnValues().get(job.getReturnValues().size() - 1));
        }
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void test3Runs() throws InterruptedException {
        int ret = 1032;
        Job<String, Integer> job = CronScheduler.run(() -> {
            Thread.sleep(2);
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 3; i++) {
            assertEquals(Job.JobStatus.WAITING, job.getStatus());
            flag = false;
            Thread.sleep(1);
            assertEquals(Job.JobStatus.RUNNING, job.getStatus());
            assertFalse(flag);
            Thread.sleep(2);
            assertTrue(flag);
            assertEquals(ret, job.getReturnValues().get(job.getReturnValues().size() - 1));
        }
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testCronRun() throws InterruptedException {
        int ret = 230;
        Job<String, Integer> job = CronScheduler.run(() -> {
            flag = true;
            return ret;
        }, "*/1 * * * *"); // Run every minute
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        flag = false;
        Thread.sleep(60000);
        assertTrue(flag);
        assertEquals(ret, job.getReturnValues().get(0));
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testPauseAndResume() throws InterruptedException {
        int ret = 102;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(2);
            flag = true;
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
            flag = true;
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
            flag = true;
            return ret;
        }, 2, TimeUnit.MILLISECONDS);
        assertEquals(Job.JobStatus.WAITING, job.getStatus());
        Thread.sleep(1);
        job.stop();
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
        Thread.sleep(2);
        assertFalse(flag);
    }

    @Test
    void testRunWithID() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(2);
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS, "id1");
        assertEquals("id1", job.getId());
        assertEquals(Job.JobStatus.WAITING, job.getStatus());

        Thread.sleep(1);
        assertEquals(Job.JobStatus.RUNNING, job.getStatus());
        assertFalse(flag);
        Thread.sleep(2);
        assertTrue(flag);
        assertEquals(ret, job.getReturnValues().get(0));
        assertEquals(Job.JobStatus.STOPPED, job.getStatus());
    }

    @Test
    void testUniqueID() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS, "id1");
        assertThrows(KeyException.class, () ->{
            CronScheduler.runOnce(() -> {
                Thread.sleep(2);
                flag = true;
                return ret;
            }, 1, TimeUnit.MILLISECONDS, "id1");
        });
        job.stop();
    }

    @Test
    void testUniqueIDAndDelete() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS, "id1");
        job.stop();
        assertDoesNotThrow(() ->{
            CronScheduler.runOnce(() -> {
                Thread.sleep(2);
                flag = true;
                return ret;
            }, 1, TimeUnit.MILLISECONDS, "id1");
        });
        Thread.sleep(2);
    }

    @Test
    void testRetrieveWithID() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            flag = true;
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
            flag = true;
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
            flag = true;
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
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS);
        Thread.sleep(3);
        assertEquals(1,1e-5, job.getExecutionTimes().get(0));
    }

    @Test
    void testCallback() throws InterruptedException {
        int ret = 15;
        Job<String, Integer> job = CronScheduler.runOnce(() -> {
            Thread.sleep(1);
            flag = true;
            return ret;
        }, 1, TimeUnit.MILLISECONDS);
        job.addOnFinishCallback((Integer result, double wait) -> {
            assertEquals(ret, result);
            assertEquals(1, 1e-5, wait);
        });
        Thread.sleep(3);
    }
}