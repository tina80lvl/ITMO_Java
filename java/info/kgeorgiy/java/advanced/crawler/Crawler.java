package info.kgeorgiy.java.advanced.crawler;

import java.io.IOException;

/**
 * Crawls web sites.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface Crawler extends AutoCloseable {
    Result download(String url, int depth) throws IOException;

    void close();
}
