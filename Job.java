public class Job {
    private int type;
    private int id;
    private int clientFrom;
    private int slave;

    //constructor
    public Job(int t, int i, int c) {
        this.type = t;
        this.id = i;
        this.clientFrom = c;
        this.slave = 0;
    }

    //setters and getters
    public void setId(int id) {
        this.id = id;
    }

    public int getSlave() {
        return slave;
    }

    public void setSlave(int slave) {
        this.slave = slave;
    }

    public int getId() {
        return id;
    }

    public int getType() { return type; }

    public void setType(int type) {
        this.type = type;
    }

    public int getClientFrom() {
        return clientFrom;
    }

    @Override
    public String toString() {
        return "Job " + id + ", type " + type + " from client " + clientFrom;
    }
}
