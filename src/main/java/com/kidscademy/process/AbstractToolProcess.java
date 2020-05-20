package com.kidscademy.process;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

import js.log.Log;
import js.log.LogFactory;
import js.util.Strings;

public abstract class AbstractToolProcess {
    private static final Log log = LogFactory.getLog(AbstractToolProcess.class);

    private static final long FFMPEG_TIMEOUT = 16000L;

    public abstract <T> T exec(Type resultType, String command) throws IOException;

    public void exec(String command) throws IOException {
	exec(VoidResult.class, command);
    }

    protected static Process start(List<String> command) throws IOException {
	ProcessBuilder builder = new ProcessBuilder(command);
	// redirect STDERR to STDOUT so that reading process.getInputStream get them
	// both
	builder.redirectErrorStream(true);
	log.debug("Create process |%s|.", Strings.join(command));
	return builder.start();
    }

    protected static void wait(Process process, Thread stdinThread, Object lock) throws IOException {
	long timeout = FFMPEG_TIMEOUT;
	long timestamp = System.currentTimeMillis() + timeout;

	synchronized (lock) {
	    stdinThread.start();
	    while (timeout > 0) {
		try {
		    lock.wait(timeout);
		    timeout = timestamp - System.currentTimeMillis();
		} catch (InterruptedException e) {
		    continue;
		}
		break;
	    }
	}

	if (timeout <= 0) {
	    process.destroy();
	    throw new IOException("FFmpeg process timeout. See stdout logs for process printout.");
	}

	int returnCode = -1;
	try {
	    returnCode = process.waitFor();
	} catch (InterruptedException e) {
	    log.error(e);
	}
	if (returnCode != 0) {
	    throw new IOException(String
		    .format("FFmpeg process fail. Exit code |%d|. See stdout logs for process printout.", returnCode));
	}
    }

    protected static void close(Reader reader) {
	try {
	    reader.close();
	} catch (IOException e) {
	    log.error(e);
	}
    }
}
