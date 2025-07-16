package org.example;

@FunctionalInterface
public interface ProgressCallback {
    void progress(int page);
}
