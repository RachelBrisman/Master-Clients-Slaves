import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SlaveBWritesToMasterThread extends Thread{
	Socket slaveB;
	private ArrayList<Job> slaveBJobs;//array list of job
	private Object slaveBJobsLock;//lock object for array list
	private SharedNum nextJob;//sharedNum object for next job
	private Object nextJobLock;//dummy object to lock on for the next job the slave need

	//constructor
	public SlaveBWritesToMasterThread(Socket s,ArrayList<Job>jobs, Object lock, SharedNum nj, Object njLock) {
		this.slaveB = s;
		this.slaveBJobs=jobs;
		this.slaveBJobsLock=lock;
		this.nextJob = nj;
		this.nextJobLock = njLock;
	}

	@Override
	public void run() {
		try(
				PrintWriter out=new PrintWriter(slaveB.getOutputStream(),true);
		)
		{
			//loops until the nextJob changes, which indicates that it can start
			int canIstart;
			while(true)
			{
				synchronized(nextJobLock)
				{
					canIstart = nextJob.getNum();//job number
				}
				if(canIstart != 0)
				{
					break;
				}
			}

			// when a job's status is done execution by slave b, let master know
			// send master the job's id to switch to done execution
			int nextNum;
			int prevNum = -3;//for sure less than -1
			Job currentJob;

			while(true)
			{
				synchronized (nextJobLock)
				{
					nextNum = nextJob.getNum() - 1;//get next job number index
				}
				if(nextNum == -2)
				{
					break;
				}
				if(nextNum != prevNum)//new job
				{
					synchronized (slaveBJobsLock)
					{
						currentJob = slaveBJobs.get(nextNum);//set current job to the one more than last number in array list
					}
					out.println(currentJob.getType());//write the type to master
					out.println(currentJob.getId());//write the id to master
					out.println(currentJob.getClientFrom());//write the id of which client it was from to master
					System.out.println("Sent Back to Master: " + currentJob);
					prevNum = nextNum;
				}
			}

			System.out.println("SlaveBWriteToMaster is done");
			out.println(-1);

		}
		catch(UnknownHostException e)
		{
			System.err.println("Don't know about host " );
			System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to sbwm");
			System.exit(1);
		}
	}//end of run method
}