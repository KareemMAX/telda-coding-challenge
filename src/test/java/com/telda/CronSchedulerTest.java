package com.telda;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}