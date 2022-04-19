import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class SlaveA {
    //this slave will perform one task that takes it 10 seconds, and another that takes 2 seconds
    public static void main(String[] args) throws IOException {
        ArrayList<Job> slaveAJobs = new ArrayList<>();//array of job for slave a
        Object slaveAJobsLock = new Object();//lock object for it
        Socket slaveASocket = new Socket("127.0.0.1", 43341);//new client socket
        SharedNum nextJob = new SharedNum(0);//sharedNum for next job
        Object nextJobLock = new Object();//lock object

        //new thread
        Thread in = new SlaveAReadsFromMasterThread(slaveASocket,slaveAJobs, slaveAJobsLock, nextJob, nextJobLock);
        in.start();//start thread

        try
        {
            in.join();
        }
        catch (Exception e)
        {
            System.out.println("Join Failed");
        }

    }
}
