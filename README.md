# Cron scheduler

A simple in-process scheduler meant to solve [telda's coding challenge](https://github.com/teldabank/coding-challenges).
It accepts a `Callable` and executes it either periodically or after a delay.

## Technical decisions

### Language
The language chosen for this challenge is Java.

My main choices for the language were Java, Python, and JavaScript. Mainly because those were the languages I have
used mostly to write production code during my career. I was leaning towards Python and JavaScript since they support
asynchronous execution through the `async` keyword. But due to both being a dynamically typed languages, I felt more
comfortable doing the task in Java. In addition, I've experience writing tests in JUnit.

### Interface design
During the interface design phase, I made it dependent on the existing Java classes for lambda functions (`Callable`)
which was the standard for passing functions to `Thread`, and `TimeUnit` enum that holds possible time units that can be
used to describe a time period. This decision was made to make sure that the library is interoperable with other Java
libraries.

Also, all functions are generic to give the user a choice of which type of `ID` or function return time.

In addition, the interface allows the user to omit the ID from the job creation by assigning it a random UUID. And the
user can specify a number of runs as well.

Please note: the choice of adopting `Callable` instead of `Runnable` was because I personally wanted the jobs to return
values, as well as allowing the job to throw exceptions. While `Runnable` is not allowing both. Although `Runnable`
might be a more suitable choice in production systems, I choose `Callable` because I wanted to write a return value
retrieval function. This can be scrapped or improved in next iterations.

### Job execution
In essence, the job is executed in a looping thread with `Thread.sleep()`, with an addition of a helper function to
determine the current state of the job (running / waiting / ...). Since the current state changes when a thread starts
or finishes. In order to capture when a thread ends, a parallel waiter thread is spawned and joined with the job thread.

### Testing
Initially, I meant while testing to test for durations around 1-2 ms. But due to an inaccuracy in `Thread.sleep()`
([1](https://jvm-gaming.org/t/thread-sleep-accuracy/30773), [2](https://stackoverflow.com/questions/18736681/how-accurate-is-thread-sleep))
I changed the sleep values to be 25-75 ms instead.

## Trade-offs

### `Callable` instead of `Runnable`
Because `Callable` is used as an argument for the job lambda function, which forces the user to return a value. But
using `Runnable` won't allow return of values entirely, which the user might need. Also, the user might not throw
exceptions unless wrapped with `try catch` blocks.

### Narrow cases in tests
As the system is based on the processor clock, I can't simulate longer periods in testing. So, testing only test 25-75 ms
periods.

## Examples

### Run code once after an hour
```java
Job<String, Integer> job = CronScheduler.runOnce(() -> {
    // Code
}, 1, TimeUnit.HOURS);
```

### Run code every minute
```java
Job<String, Integer> job = CronScheduler.run(() -> {
    // Code
}, 1, TimeUnit.MINUTES);
```

### Run code every minute 3 times
```java
Job<String, Integer> job = CronScheduler.run(() -> {
    // Code
}, 1, TimeUnit.MINUTES, 3);
```

### Run, pause and stop a job
```java
Job<String, Integer> job = CronScheduler.run(() -> {
    // Code
}, 1, TimeUnit.HOURS);

job.pause();
job.resume();
job.stop();
```


### Retrieve a job by ID

```java
Job<String, Integer> job = CronScheduler.run(() -> {
    // Code
}, 1, TimeUnit.MINUTES, 3, "id1");
Job<String, Integer> retrivedJob = CronScheduler.getJob("Id1");
```

## Future improvements

- Adding `Runnable` support
- Adding Cron expressions support (i.e. `"* * * * *"`)

## Footnotes
I tried working on cron expressions support, but realized that the algorithm is too complicated and I didn't have the
time to think more about it. You can find a partial implementation of it with complete tests in the `cron` branch.
