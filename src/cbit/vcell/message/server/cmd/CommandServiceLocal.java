package cbit.vcell.message.server.cmd;

import java.io.File;
import java.io.IOException;

import org.vcell.util.Executable;
import org.vcell.util.ExecutableException;
import org.vcell.util.FileUtils;

import cbit.vcell.mongodb.VCMongoMessage;

public class CommandServiceLocal extends CommandService {
	
	public CommandServiceLocal(){
		super();
	}

	@Override
	public CommandOutput command(String[] command, int[] allowableReturnCodes) throws ExecutableException {
		if (allowableReturnCodes == null){
			throw new IllegalArgumentException("allowableReturnCodes must not be null");
		}
		long timeMS = System.currentTimeMillis();
		Executable exe = new Executable(command);
		exe.start(allowableReturnCodes);
		long elapsedTimeMS = System.currentTimeMillis() - timeMS;
		CommandOutput commandOutput = new CommandOutput(command, exe.getStdoutString(), exe.getStderrString(), exe.getExitValue(), elapsedTimeMS);

		VCMongoMessage.sendCommandServiceCall(commandOutput);

		System.out.println("Command: " + commandOutput.getCommand());
		if(!bQuiet){System.out.println("Command: stdout = " + commandOutput.getStandardOutput());}
		System.out.println("Command: stderr = " + commandOutput.getStandardError()); 
		System.out.println("Command: exit = " + commandOutput.getExitStatus());
		
		return commandOutput;
	}

	@Override
	public CommandService clone() {
		return new CommandServiceLocal();
	}

	@Override
	public void pushFile(File tempFile, String remotePath) throws IOException {
		FileUtils.copyFile(tempFile, new File(remotePath));
	}

	public void deleteFile(String remoteFilePath) throws IOException {
		FileUtils.deleteFile(remoteFilePath);
	}

	@Override
	public void close() {
	}

}
