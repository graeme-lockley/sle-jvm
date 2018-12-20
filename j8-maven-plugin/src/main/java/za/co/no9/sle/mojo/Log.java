package za.co.no9.sle.mojo;

import org.jetbrains.annotations.NotNull;


public class Log implements za.co.no9.sle.tools.build.Log {
    private org.apache.maven.plugin.logging.Log log;

    public Log(org.apache.maven.plugin.logging.Log log) {
        this.log = log;
    }


    @Override
    public void error(@NotNull String message) {
        log.error(message);
    }

    @Override
    public void info(@NotNull String message) {
        log.info(message);
    }
}
