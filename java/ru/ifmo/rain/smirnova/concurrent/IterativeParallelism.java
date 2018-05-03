package ru.ifmo.rain.smirnova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        mapper = null;
    }

    private void checkForNull(Object x) {
        if (x == null) {
            throw new IllegalArgumentException("One of given argument is null");
        }
    }

    private void joinAll(Thread[] threads) throws InterruptedException {
        InterruptedException exception = null;
        for (Thread th : threads) {
            try {
                th.join();
            } catch (InterruptedException e) {
                if (exception == null) {
                    exception = new InterruptedException("One of working threads was interrupted: " + e.getMessage());
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        if (exception != null)
            throw exception;
    }

    private <T> List<List<? extends T>> splitList(int cnt, List<? extends T> list) {
        List<List<? extends T>> res = new ArrayList<>();
        int blockSize = list.size() / cnt;
        int extra = list.size() % cnt;
        for (int i = 0, l = 0, curSize; i < cnt; i++) {
            curSize = blockSize;
            if (extra > 0) {
                curSize++;
                extra--;
            }
            res.add(list.subList(l, l + curSize));
            l += curSize;
        }
        return res;
    }

    private <T, R> R parallelCalc(int cnt, List<? extends T> values, Function<Stream<? extends T>, R> threadCalc, Function<Stream<R>, R> merge) throws InterruptedException {
        if (cnt <= 0 || values == null) {
            throw new IllegalArgumentException("Incorrect amount of threads or given list is null");
        }
        cnt = Integer.min(cnt, values.size());
        List<List<? extends T>> parts = splitList(cnt, values);
        List<R> tmp;
        if (mapper == null) {
            Thread[] threads = new Thread[cnt];
            tmp = new ArrayList<>(Collections.nCopies(cnt, null));
            for (int i = 0; i < threads.length; ++i) {
                threads[i] = new Thread(new Calculate<>(parts.get(i), tmp, i, threadCalc));
                threads[i].start();
            }
            joinAll(threads);
        } else {
            tmp = mapper.map(threadCalc,
                    parts.stream().map(Collection::stream).collect(Collectors.toList()));
        }
        return merge.apply(tmp.stream());
    }

    private class Calculate<T, R> implements Runnable {
        private List<? extends T> list;
        private List<R> dst;
        private int idx;
        private Function<Stream<? extends T>, R> calc;

        Calculate(List<? extends T> list, List<R> dst, int idx, Function<Stream<? extends T>, R> calc) {
            this.list = list;
            this.dst = dst;
            this.idx = idx;
            this.calc = calc;
        }

        @Override
        public void run() {
            dst.set(idx, calc.apply(list.stream()));
        }
    }

    @Override
    public <T> T maximum(int cnt, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelCalc(cnt, values,
                s -> s.max(Comparator.nullsFirst(comparator)).orElse(null),
                s -> s.max(Comparator.nullsFirst(comparator)).orElse(null)
        );
    }

    @Override
    public <T> T minimum(int cnt, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelCalc(cnt, values,
                s -> s.min(Comparator.nullsLast(comparator)).orElse(null),
                s -> s.min(Comparator.nullsLast(comparator)).orElse(null)
        );
    }

    @Override
    public <T> boolean all(int cnt, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        checkForNull(predicate);
        return parallelCalc(cnt, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch((x) -> x.equals(true))
        );
    }

    @Override
    public <T> boolean any(int cnt, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        checkForNull(predicate);
        return parallelCalc(cnt, values,
                s -> s.anyMatch(predicate),
                s -> s.anyMatch(x -> x)
        );
    }
}