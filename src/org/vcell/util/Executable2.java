package org.vcell.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import cbit.vcell.resource.ResourceUtil;

/**
 * alternate Executable implementation which uses threaded readers
 * @author gweatherby
 *
 */
public class Executable2 implements IExecutable {
	
	private File workingDir;
	private String commands[];
	private String out;
	private String err;
	private int exitCode;
	private AtomicReference<ExecutableStatus> status;
	private AtomicReference<Thread> runThread;

	public Executable2(String ... commands) {
		super();
		this.commands = commands;
		workingDir = null;
		out = null;
		err = null;
		status = new AtomicReference<ExecutableStatus>(ExecutableStatus.READY);
		runThread = new AtomicReference<Thread>(null);
	}

	public final void start(int[] expectedReturnCodes) throws org.vcell.util.ExecutableException {
		out = null;
		err = null;
		Process process = null; 
		try {
			runThread.set(Thread.currentThread()); //record for interruption via #stop
			ProcessBuilder pb = new ProcessBuilder(commands);
			ResourceUtil.setEnvForOperatingSystem(pb.environment());
			pb.directory(workingDir);
			process = pb.start();
			process.getOutputStream().close(); //close standard in
			Reader sOut = new Reader(process.getInputStream(), "Executable2 " + commands[0] + " out");
			Reader sErr = new Reader(process.getErrorStream(), "Executable2 " + commands[0] + " err");
			sOut.start();
			sErr.start();
			status.set(ExecutableStatus.RUNNING);
			exitCode = process.waitFor( );
			sOut.close();
			sErr.close();
			sOut.join( );
			sErr.join( );
			out = sOut.getString();
			err = sErr.getString();
			if (sOut.getReadException() != null) {
				throw sOut.getReadException(); 
			}
			if (sErr.getReadException() != null) {
				throw sErr.getReadException(); 
			}
			if (codeInSet(exitCode, expectedReturnCodes)) {
				status.set(ExecutableStatus.COMPLETE);
			}else{
				status.set(ExecutableStatus.getError("executable failed, return code = " + getExitValue() + "\nstderr = '" + err + "'"));				
			}
		} catch (InterruptedException ie)  {
			if (getStatus( ) == ExecutableStatus.STOPPED) {
				if (process != null) {
					process.destroy();
					exitCode = process.exitValue();
				}
				return;
			}
			convertException(ie);
		} catch (Exception e)  {
			convertException(e);
		}
	}
	
	/**
	 * set status and convert e into {@link ExecutableException}
	 * @param e
	 * @throws ExecutableException
	 */
	private void convertException(Exception e) throws ExecutableException  {
		status.set(ExecutableStatus.getError(e.getMessage()));
		throw new ExecutableException("Exception executing " + Arrays.toString(commands),e);
	}
	
	/**
	 * is code in set
	 * @param code
	 * @param set
	 * @return true if is
	 */
	private static boolean codeInSet(int code, int[] set)  {
		for (int i : set) {
			if (i == code) {
				return true;
			}
		}
		return false;
	}

	public final void start() throws org.vcell.util.ExecutableException {
		start(DEFAULT_RETURN);
	}

	public String getStdoutString() {
		return out;
	}

	public String getStderrString() {
		return err;
	}

	public synchronized ExecutableStatus getStatus() {
		return status.get( );
	}

	public java.lang.Integer getExitValue() {
		return exitCode; 
	}

	public String getCommand() {
		return Arrays.toString(commands);
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}
	
	@Override
	public synchronized final void stop() {
		if (status.get() != ExecutableStatus.STOPPED) {
			status.set(ExecutableStatus.STOPPED);
			Thread t = runThread.getAndSet(null);
			if (t == null) {
				throw new RuntimeException("logic error, Executable2");
			}
			t.interrupt();
		}
	}

	public static class Reader extends Thread {
		public static final int STARTING__BUFFER = 2000;
		private InputStream in;
		private ByteArrayOutputStream oStream;
		private Exception readException;
		private AtomicBoolean closed;
		public Reader(InputStream in, String threadName) {
			super(threadName);
			this.in = in;
			oStream = new ByteArrayOutputStream(STARTING__BUFFER);
			readException = null;
			closed = new AtomicBoolean(false);
			setDaemon(true);
		}
		
		@Override
		public void run() {
			try {
				byte buffer[] = new byte[STARTING__BUFFER];
				for (;;) {
					int read = in.read(buffer);
					if (read > 0) {
						oStream.write(buffer, 0, read);
						continue;
					}
					return;
				}  
			} catch (Exception e) {
				if (closed.get() && e instanceof IOException && e.getMessage().toLowerCase().contains("stream closed")) {
					return;
				}
				readException = e;
			}
		}
		
		public void close( ) throws IOException {
			closed.set(true);
			in.close( );
		}
		
		public String getString( ) {
			return oStream.toString();
		}

		public Exception getReadException() {
			return readException;
		}
	}
}
