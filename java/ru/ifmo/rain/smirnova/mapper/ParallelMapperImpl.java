package ru.ifmo.rain.smirnova.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.List;

public class ParallelMapperImpl implements ParallelMapper {
    private final TaskQueue tasks;
    private final List<Thread> threads;

    public ParallelMapperImpl(int cnt) {
        if (cnt <= 0) {
            throw new IllegalArgumentException("Incorrect amount of threads to create");
        }
        tasks = new TaskQueue();
        threads = new ArrayList<>();
        for (int i = 0; i < cnt; ++i) {
            threads.add(new Thread(new Worker(tasks)));
            threads.get(i).start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        if (f == null || args == null) {
            throw new IllegalArgumentException("One of given arguments is null");
        }
        List<R> res = new ArrayList<>(Collections.nCopies(args.size(), null));
        TaskManager manager = new TaskManager(args.size());
        for (int i = 0; i < args.size(); i++) {
            tasks.putTask(new Task<T, R>(f, args.get(i), res, i, manager));
        }
        manager.waitForResult();
        return res;
    }

    @Override
    public void close() {
        for (Thread th : threads) {
            th.interrupt();
        }
        for (Thread th : threads) {
            try {
                th.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private class Worker implements Runnable {
        private final TaskQueue tasks;

        private Worker(TaskQueue tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Task task = tasks.getTask();
                    task.execute();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }

    public class Task<T, R> {
        private final Function<? super T, ? extends R> f;
        private final T arg;
        private final List<R> dstL;
        private final int dstI;
        private final TaskManager manager;

        public Task(Function<? super T, ? extends R> f, T arg, List<R> dstL, int dstI, TaskManager manager) {
            this.f = f;
            this.arg = arg;
            this.dstL = dstL;
            this.dstI = dstI;
            this.manager = manager;
        }

        public void execute() {
            dstL.set(dstI, f.apply(arg));
            synchronized (manager) {
                manager.count();
                if (manager.isDone()) {
                    manager.notify();
                }
            }
        }
    }

    class TaskManager {
        private int remain;

        TaskManager(int cntTasks) {
            remain = cntTasks;
        }

        synchronized void count() {
            remain--;
        }

        boolean isDone() {
            return remain == 0;
        }

        synchronized void waitForResult() throws InterruptedException {
            while (!isDone()) {
                wait();
            }
        }
    }

    public class TaskQueue {
        private static final int MAX_SIZE = 1000000;
        private final Queue<Task> tasks;

        public TaskQueue() {
            this.tasks = new LinkedList<>();
        }

        public synchronized Task getTask() throws InterruptedException {
            while (tasks.isEmpty()) {
                wait();
            }
            Task task = tasks.poll();
            notify();
            return task;
        }

        public synchronized void putTask(Task task) throws InterruptedException {
            while (tasks.size() == MAX_SIZE) {
                wait();
            }
            tasks.add(task);
            notify();
        }
    }
}