package edu.gatech.cs7210.p2pmapreduce.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.uniba.wiai.lspi.chord.data.URL;
import edu.gatech.cs7210.p2pmapreduce.ApplicationContext;
import edu.gatech.cs7210.p2pmapreduce.task.ITask;

public class MasterNode implements INode {

	public enum MasterType {
		NAME_NODE, JOB_TRACKER
	}
	
	private MasterType type;
	
	public MasterNode(MasterType type) {
		this.type = type;
	}
	
	public MasterType getType() {
		return this.type;
	}
	
	public boolean run() {
		try {
			Process p = Runtime.getRuntime().exec(
					ApplicationContext.getInstance().getBinDir() + File.separator + "start-all.sh");
			InputStream s = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(s));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			return true;
		} catch (IOException e) {
			System.err.println("Failed to run master");
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}
	
	public boolean update(URL url) {
		shutdownMaster();
		updateSlaveConfiguration(url);
		return run();
	}
	
	public boolean executeTask(ITask task) {
		try {
			Process p = Runtime.getRuntime().exec(task.getCommand());
			InputStream s = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(s));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			return true;
		} catch (IOException e) {
			System.err.println("Failed to execute task [" + task.getTaskName() + "]");
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}
	
	public void shutdownMaster() {
		try {
			Process p = Runtime.getRuntime().exec(
					ApplicationContext.getInstance().getBinDir() + File.separator + "stop-all.sh");
			InputStream s = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(s));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println("Failed to shutdown master");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void updateSlaveConfiguration(URL url) {
		// update the Slave configuration file to include all Workers present
		// in the Chord overlay which are delegated to this Master server
		try {
			ApplicationContext appContext = ApplicationContext.getInstance();
			File slaveConfigFile = new File(appContext.getConfigDir() + 
					File.separator + appContext.getSlaveConfigFile());
			BufferedWriter writer = new BufferedWriter(new FileWriter(slaveConfigFile, true));
			writer.write("\n" + url.getPath());
			writer.close();
		} catch (IOException e) {
			System.err.println("Failed to update slave config file");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}