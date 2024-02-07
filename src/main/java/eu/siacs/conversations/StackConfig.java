package eu.siacs.conversations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface StackConfig {
    ExecutorService executorService = Executors.newCachedThreadPool();
}
