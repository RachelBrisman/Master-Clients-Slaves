public class SharedNum {
    private int num;

    //constructor
    public SharedNum(int n){
        this.num = n;
    }

    //setter and getter
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return String.valueOf(num);
    }
}
