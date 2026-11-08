package com.buildflow.worker.execution;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class CommandRunner {

    private static final Logger log = LoggerFactory.getLogger(CommandRunner.class);

    private static final boolean WINDOWS =
            System.getProperty("os.name").toLowerCase().startsWith("windows");
    
    private final long timeoutSeconds;
    private final int maxOutputChars;

    public CommandRunner(@Value("${worker.job-timeout-seconds:600}") long timeoutSeconds,
                         @Value("${worker.max-output-chars:65536}") int maxOutputChars) {
        this.timeoutSeconds = timeoutSeconds;
        this.maxOutputChars = maxOutputChars;
    }

    public record Result(int exitCode, String output) {}

    public Result run(String command) throws IOException, InterruptedException {
        Path workDir = Files.createTempDirectory("buildflow-job-");
        Path logFile = Files.createTempFile("buildflow-", ".log");
        try {
            ProcessBuilder pb = WINDOWS
                    ? new ProcessBuilder("cmd.exe", "/c", command)
                    : new ProcessBuilder("sh", "-c", command);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true);
            pb.redirectOutput(logFile.toFile());

            Process process = pb.start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                process.waitFor();
                return new Result(-1, readTail(logFile)
                        + "\n[buildflow] Killed: exceeded " + timeoutSeconds + "s timeout");
            }
            return new Result(process.exitValue(), readTail(logFile));
        } finally {
            deleteQuietly(logFile);
            deleteQuietly(workDir);
        }
    }

    private String readTail(Path file) throws IOException {
        Charset charset = Charset.forName(System.getProperty("native.encoding", "UTF-8"));
        String text = Files.readString(file, charset);
        if (text.length() <= maxOutputChars) {
            return text;
        }
        return "[buildflow] Output truncated, showing last " + maxOutputChars + " chars\n"
                + text.substring(text.length() - maxOutputChars);
    }

    private void deleteQuietly(Path path) {
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            log.warn("Could not delete {}", path, e);
        }
    }
}