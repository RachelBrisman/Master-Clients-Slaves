import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MasterWritesToSlaveAThread extends Thread{
	Socket slaveA;
	private ArrayList<Job> jobs;//array list of jobs
	private Object jobLock;//dummy object to synchronize on
	private SharedNum nextJobForSlave;//a shared number object to hold the next job that the slave will need
	private Object nextJFSLock;//dummy object to lock on for the next job the slave need

	//constructor
	public MasterWritesToSlaveAThread (Socket socket,ArrayList<Job>jobs, Object jobLock, SharedNum NJS, Object NJSL) {
		this.slaveA=socket;
		this.jobs=jobs;
		this.jobLock=jobLock;
		this.nextJobForSlave = NJS;
		this.nextJFSLock = NJSL;
	}

	public void run() {
		try (
				OutputStream outputStream = slaveA.getOutputStream();
				PrintWriter out1=new PrintWriter(outputStream,true);

		) {

			//loops until the nextJobForSlave changes, which indicates that it can start
			int canIstart;
			while(true)
			{
				synchronized(nextJFSLock)
				{
					canIstart = nextJobForSlave.getNum();
				}
				if(canIstart != 0)
				{
					break;
				}
			}

			// master sends jobs objects to slave a
			// if nextJobforSlave's slave number is 1, sends it to slave A
			int nextNum;
			int prevNum = -3;
			Job currentJob;

			while(true)
			{
				synchronized (nextJFSLock)
				{
					nextNum = nextJobForSlave.getNum() - 1;//next job index
				}
				if(nextNum == -2)//forsure less than -1
				{
					break;
				}
				if(nextNum != prevNum)
				{
					synchronized (jobLock)
					{
						currentJob = jobs.get(nextNum);//job number
					}
					if(currentJob.getSlave() == 1)
					{
						out1.println(currentJob.getType());//write to slave a  job type
						out1.println(currentJob.getId());//write to slave a job id
						out1.println(currentJob.getClientFrom());//write to slave a which client sent
						System.out.println("Sent to Slave A: " + currentJob);
					}
					prevNum = nextNum;
				}
			}

			System.out.println("MasterWriteToSA is done");
			out1.println(-1);//indicate it's done
		}
		catch (IOException e)
		{
			System.out.println("Exception caught when trying to listen on port or listening for a connection mwsa");
			System.out.println(e.getMessage());
		}
	}
}
