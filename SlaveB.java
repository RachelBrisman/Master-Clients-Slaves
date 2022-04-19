import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class SlaveB{

    //this slave will perform one task that takes it 10 seconds, and another that takes 2 seconds
    public static void main(String[] args) throws IOException {

        ArrayList<Job> slaveBJobs = new ArrayList<>();//saray list of jobs for slave b
        Object slaveBJobsLock = new Object();//lock object
        Socket slaveBSocket = new Socket("127.0.0.1", 43351);//client socket
        SharedNum nextJob = new SharedNum(0);//sharedNum object for next job
        Object nextJobLock = new Object();//lock object for next job

        //new thread
        Thread in = new SlaveBReadsFromMasterThread(slaveBSocket,slaveBJobs, slaveBJobsLock, nextJob, nextJobLock);
        in.start();//start thread

        try
        {
            in.join();
        }
        catch(Exception e)
        {
            System.out.println("Join Failed sbrm");
        }
    }
}
