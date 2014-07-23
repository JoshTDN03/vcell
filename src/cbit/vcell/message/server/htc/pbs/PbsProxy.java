package cbit.vcell.message.server.htc.pbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vcell.util.ExecutableException;
import org.vcell.util.FileUtils;
import org.vcell.util.PropertyLoader;

import cbit.vcell.message.server.cmd.CommandService;
import cbit.vcell.message.server.cmd.CommandService.CommandOutput;
import cbit.vcell.message.server.htc.HtcException;
import cbit.vcell.message.server.htc.HtcJobID;
import cbit.vcell.message.server.htc.HtcJobNotFoundException;
import cbit.vcell.message.server.htc.HtcJobStatus;
import cbit.vcell.message.server.htc.HtcProxy;

public final class PbsProxy extends HtcProxy {
	private static final int QDEL_JOB_NOT_FOUND_RETURN_CODE = 153;
	
	private final static String UNKNOWN_JOB_ID_QSTAT_RESPONSE = "Unknown Job Id";
	protected final static String PBS_SUBMISSION_FILE_EXT = ".pbs.sub";
	
	// note: full commands use the PropertyLoader.htcPbsHome path.
	private final static String JOB_CMD_SUBMIT = "qsub";
	private final static String JOB_CMD_DELETE = "qdel";
	private final static String JOB_CMD_STATUS = "qstat";
	private Pattern statPattern = null;
	private String jobPrefix = null; 

	public PbsProxy(CommandService commandService, String htcUser){
		super(commandService, htcUser);
	}
	
	@Override
	public HtcJobStatus getJobStatus(HtcJobID htcJobId) throws HtcException, ExecutableException {		
		if (!(htcJobId instanceof PbsJobID)){
			throw new HtcException("jobID ("+htcJobId.toDatabase()+") from another queuing system");
		}
		PbsJobID pbsJobID = (PbsJobID)htcJobId;
		
		HtcJobStatus iStatus = null;

		String PBS_HOME = PropertyLoader.getRequiredProperty(PropertyLoader.htcPbsHome);
		if (!PBS_HOME.endsWith("/")){
			PBS_HOME += "/";
		}
		
		String[] cmd = new String[]{PBS_HOME + JOB_CMD_STATUS, "-s", Long.toString(pbsJobID.getPbsJobNumber())};
		CommandOutput commandOutput = commandService.command(cmd, new int[] { 0, 153 });

		String output = commandOutput.getStandardOutput();
		StringTokenizer st = new StringTokenizer(output, "\r\n"); 
		String strStatus = "";
		while (st.hasMoreTokens()) {
			if (st.nextToken().toLowerCase().trim().startsWith("job id")) {
				if (st.hasMoreTokens()) {
					st.nextToken();
				}
				if (st.hasMoreTokens()) {
					strStatus = st.nextToken();
				}
				break;
			}			
		}
		if (strStatus.length() == 0) {
			return iStatus;
		}
		/*

	pbssrv: 
	                                                            Req'd  Req'd   Elap
	Job ID          Username Queue    Jobname    SessID NDS TSK Memory Time  S Time
	--------------- -------- -------- ---------- ------ --- --- ------ ----- - -----
	29908.pbssrv    vcell    workqAlp S_32925452  30022   1   1  100mb   --  R 00:29
	   Job run at Mon Apr 27 at 08:28 on (dll-2-6-6:ncpus=1:mem=102400kb)

		 */		
		st = new StringTokenizer(strStatus, " ");
		String token = "";
		for (int i = 0; i < 10 && st.hasMoreTokens(); i ++) {
			token = st.nextToken();
		}
		PBSJobStatus pbsJobStatus = PBSJobStatus.fromPBSCommandLetter(token);
		if (pbsJobStatus!=null){
			return new HtcJobStatus(pbsJobStatus);
		}
		throw new HtcException("unknown PBS status letter '"+token+"'");
	}


	@Override
	public void killJob(HtcJobID htcJobId) throws ExecutableException, HtcException {
		if (!(htcJobId instanceof PbsJobID)){
			throw new HtcException("jobID ("+htcJobId.toDatabase()+") from another queuing system");
		}
		PbsJobID pbsJobID = (PbsJobID)htcJobId;

		String PBS_HOME = PropertyLoader.getRequiredProperty(PropertyLoader.htcPbsHome);
		if (!PBS_HOME.endsWith("/")){
			PBS_HOME += "/";
		}
		String[] cmd = new String[]{PBS_HOME + JOB_CMD_DELETE, Long.toString(pbsJobID.getPbsJobNumber())};
		try {
			CommandOutput commandOutput = commandService.command(cmd, new int[] { 0, QDEL_JOB_NOT_FOUND_RETURN_CODE });
			Integer exitStatus = commandOutput.getExitStatus();
			String standardError = commandOutput.getStandardError();
			String standardOut = commandOutput.getStandardOutput();
			System.err.println(standardOut);
			
			if (exitStatus!=null && exitStatus==QDEL_JOB_NOT_FOUND_RETURN_CODE && standardError!=null && standardError.toLowerCase().contains(UNKNOWN_JOB_ID_QSTAT_RESPONSE.toLowerCase())){
				throw new HtcJobNotFoundException(standardError);
			}
		}catch (ExecutableException e){
			e.printStackTrace();
			if (!e.getMessage().toLowerCase().contains(UNKNOWN_JOB_ID_QSTAT_RESPONSE.toLowerCase())){
				throw e;
			}else{
				throw new HtcJobNotFoundException(e.getMessage());
			}
		}
	}

	@Override
	protected PbsJobID submitJob(String jobName, String sub_file, String[] command, int ncpus, double memSize, String[] secondCommand, String[] exitCommand, String exitCodeReplaceTag) throws ExecutableException{	
		if (lg.isInfoEnabled()) {
			char space=' ';
			lg.info("submit: " + jobName + space + sub_file + space + Arrays.toString(command) 
				 + space + ncpus + space + memSize + " second cmd:  " + Arrays.toString(secondCommand)
				 + " exit cmd:  " + Arrays.toString(exitCommand) + " ecrt:  " + exitCodeReplaceTag);
		}
		try {

			String htcLogDirString = PropertyLoader.getRequiredProperty(PropertyLoader.htcLogDir);
		    if (!(htcLogDirString.endsWith("/"))){
		    	htcLogDirString = htcLogDirString+"/";
		    }

		    StringWriter sw = new StringWriter();
			int JOB_MEM_OVERHEAD_MB = Integer.parseInt(PropertyLoader.getRequiredProperty(PropertyLoader.jobMemoryOverheadMB));

			sw.append("# Generated without file template. assuming /bin/bash shell\n");
			sw.append("#PBS -N " + jobName+"\n");
			sw.append("#PBS -l mem=" + (int)(memSize + JOB_MEM_OVERHEAD_MB) + "mb\n");
			String pbsQueueName = PropertyLoader.getProperty(PropertyLoader.htcBatchSystemQueue,null);
			if (pbsQueueName!=null && pbsQueueName.trim().length()>0){
				sw.append("#PBS -q "+pbsQueueName+"\n");
			}
			sw.append("#PBS -m a\n");
			sw.append("#PBS -M schaff@uchc.edu\n");
			sw.append("#PBS -o "+htcLogDirString+jobName+".pbs.log\n");
			sw.append("#PBS -j oe\n");
//			sw.append("#PBS -k oe\n");
			sw.append("#PBS -r n\n");
			sw.append("#PBS -l nice=10\n");
			if (ncpus > 1) {
				char newline = '\n';
				sw.append("#PBS -l nodes=1:ppn=" + ncpus + newline); 
			}
			sw.append("export PATH=/cm/shared/apps/torque/2.5.5/bin/:$PATH\n");
			sw.append("echo\n");
			sw.append("echo\n");
			sw.append("echo \"command1 = '"+CommandOutput.concatCommandStrings(command)+"'\"\n");
			sw.append("echo\n");
			sw.append("echo\n");
		    sw.append(CommandOutput.concatCommandStrings(command)+"\n");
		    sw.append("retcode1=$?\n");
		    sw.append("echo\n");
		    sw.append("echo\n");
		    sw.append("echo command1 returned $retcode1\n");
			if (secondCommand!=null){
				sw.append("if [ $retcode1 = 0 ] ; then\n");
				sw.append("		echo\n");
				sw.append("		echo\n");
				sw.append("     echo \"command2 = '"+CommandOutput.concatCommandStrings(secondCommand)+"'\"\n");
				sw.append("		echo\n");
				sw.append("		echo\n");
				sw.append("     "+CommandOutput.concatCommandStrings(secondCommand)+"\n");
				sw.append("     retcode2=$?\n");
				sw.append("		echo\n");
				sw.append("		echo\n");
				sw.append("     echo command2 returned $retcode2\n");
				sw.append("     echo returning return code $retcode2 to PBS\n");
				if (exitCommand!=null && exitCodeReplaceTag!=null){
					sw.append("		echo\n");
					sw.append("		echo\n");
					sw.append("     echo \"exitCommand = '"+CommandOutput.concatCommandStrings(exitCommand).replace(exitCodeReplaceTag,"$retcode2")+"'\"\n");
					sw.append("		echo\n");
					sw.append("		echo\n");
					sw.append("     "+CommandOutput.concatCommandStrings(exitCommand).replace(exitCodeReplaceTag,"$retcode2")+"\n");
					sw.append("		echo\n");
					sw.append("		echo\n");
				}
				sw.append("     exit $retcode2\n");
				sw.append("else\n");
				sw.append("		echo \"command1 failed, skipping command2\"\n");
				sw.append("     echo returning return code $retcode1 to PBS\n");
				if (exitCommand!=null && exitCodeReplaceTag!=null){
					sw.append("		echo\n");
					sw.append("		echo\n");
					sw.append("     echo \"exitCommand = '"+CommandOutput.concatCommandStrings(exitCommand).replace(exitCodeReplaceTag,"$retcode1")+"'\"\n");
					sw.append("		echo\n");
					sw.append("		echo\n");
					sw.append("     "+CommandOutput.concatCommandStrings(exitCommand).replace(exitCodeReplaceTag,"$retcode1")+"\n");
					sw.append("		echo\n");
					sw.append("		echo\n");
				}
				sw.append("     exit $retcode1\n");
				sw.append("fi\n");
			}else{
				sw.append("     echo returning return code $retcode1 to PBS\n");
				if (exitCommand!=null && exitCodeReplaceTag!=null){
					sw.append("		echo\n");
					sw.append("		echo\n");
					sw.append("     echo \"exitCommand = '"+CommandOutput.concatCommandStrings(exitCommand).replace(exitCodeReplaceTag,"$retcode1")+"'\"\n");
					sw.append("		echo\n");
					sw.append("		echo\n");
					sw.append("     "+CommandOutput.concatCommandStrings(exitCommand).replace(exitCodeReplaceTag,"$retcode1")+"\n");
					sw.append("		echo\n");
					sw.append("		echo\n");
				}
				sw.append("     exit $retcode1\n");
			}
			
			File tempFile = File.createTempFile("tempSubFile", ".sub");

			writeUnixStyleTextFile(tempFile, sw.getBuffer().toString());
			
			// move submission file to final location (either locally or remotely).
			System.out.println("<<<SUBMISSION FILE>>> ... moving local file '"+tempFile.getAbsolutePath()+"' to remote file '"+sub_file+"'");
			commandService.pushFile(tempFile,sub_file);
			System.out.println("<<<SUBMISSION FILE START>>>\n"+FileUtils.readFileToString(tempFile)+"\n<<<SUBMISSION FILE END>>>\n");
			tempFile.delete();
		} catch (IOException ex) {
			ex.printStackTrace(System.out);
			return null;
		}
		
		String PBS_HOME = PropertyLoader.getRequiredProperty(PropertyLoader.htcPbsHome);
		if (!PBS_HOME.endsWith("/")){
			PBS_HOME += "/";
		}
		String[] completeCommand = new String[] {PBS_HOME + JOB_CMD_SUBMIT, sub_file};
		CommandOutput commandOutput = commandService.command(completeCommand);
		String jobid = commandOutput.getStandardOutput().trim();
		
		return new PbsJobID(jobid);
	}
	
	@Override
	public PbsProxy cloneThreadsafe() {
		return new PbsProxy(getCommandService().clone(),htcUser);
	}

	@Override
	public String getSubmissionFileExtension() {
		return PBS_SUBMISSION_FILE_EXT;
	}
	
	
	@Override
	public List<HtcJobID> getRunningJobIDs(String jobNamePrefix) throws ExecutableException {
		if (!jobNamePrefix.equals(jobPrefix)) { //if jobNamePrefix changes, rebuild pattern
			jobPrefix = jobNamePrefix; 
			statPattern = Pattern.compile( 	"^(\\d*)\\S*\\s*\\S*\\s*\\S*\\s*" + jobPrefix + ".*", Pattern.DOTALL);
			/*
			 * \\d -- digits, collect in group 1
			 * \\S non-space
			 * \\s space
			 * .* rest of the line,   DOTALL option required to match newline
			 */
		}
		try {
			String PBS_HOME = PropertyLoader.getRequiredProperty(PropertyLoader.htcPbsHome);
			if (!PBS_HOME.endsWith("/")){
				PBS_HOME += "/";
			}
			String[] cmd = constructShellCommand(commandService, new String[]{PBS_HOME + JOB_CMD_STATUS, "-u",  htcUser});
			CommandOutput commandOutput = commandService.command(cmd);
			ArrayList<HtcJobID> pbsJobIDs = new ArrayList<HtcJobID>();
			BufferedReader br = new BufferedReader(new StringReader(commandOutput.getStandardOutput()));
			String line = null;
			while((line = br.readLine()) != null){
				Matcher m = statPattern.matcher(line);
				if (m.matches()) {
					String idStr = m.group(1);
					pbsJobIDs.add(new PbsJobID(idStr));
				}
			}
			return pbsJobIDs;
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ExecutableException){
				throw (ExecutableException)e;
			}else{
				throw new ExecutableException("Error getRunningJobs(): "+e.getMessage());
			}
		}
	}

	@Override
	public Map<HtcJobID,HtcJobInfo> getJobInfos(List<HtcJobID> htcJobIDs) throws ExecutableException {
		
		try{
			String PBS_HOME = PropertyLoader.getRequiredProperty(PropertyLoader.htcPbsHome);
			if (!PBS_HOME.endsWith("/")){
				PBS_HOME += "/";
			}
			HashMap<HtcJobID,HtcJobInfo> jobInfoMap = new HashMap<HtcJobID,HtcJobInfo>();
			ArrayList<String> cmdV = new ArrayList<String>();
			cmdV.add(PBS_HOME + JOB_CMD_STATUS);
			cmdV.add("-f");
			for(HtcJobID htcJobID : htcJobIDs){
				cmdV.add(Long.toString(((PbsJobID)htcJobID).getPbsJobNumber()));
			}
			CommandOutput commandOutput = commandService.command(cmdV.toArray(new String[0]),new int[] { 0, 153 });
			BufferedReader br = new BufferedReader(new StringReader(commandOutput.getStandardOutput()));
			String line = null;
			PbsJobID latestpbsJobID = null;
			String latestJobName = null;
			String latestErrorPath = null;
			while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line," \t");
				if(line.startsWith("Job Id:")){
					st.nextToken();st.nextToken();
					latestpbsJobID = new PbsJobID(st.nextToken());
				}else if(latestpbsJobID != null){
					if(line.trim().startsWith("Job_Name =")){
						st.nextToken();st.nextToken();
						latestJobName = st.nextToken();
					}else if(line.trim().startsWith("Error_Path = ")){
						st.nextToken();st.nextToken();
						latestErrorPath = st.nextToken();
					}else if(line.trim().startsWith("Output_Path =")){
						st.nextToken();st.nextToken();
						String latestOutputPath = st.nextToken();
						jobInfoMap.put(latestpbsJobID, new HtcJobInfo(latestpbsJobID,true,latestJobName,latestErrorPath,latestOutputPath));
					}
				}
			}
			return jobInfoMap;
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ExecutableException){
				throw (ExecutableException)e;
			}else{
				throw new ExecutableException("Error getServiceJobIDs: "+e.getMessage());
			}
		}
	}
	

}
