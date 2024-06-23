package projectB;

/**
 * 
 * Author: 7417834
 */
class Data {
    int A1, A2, A3, B1, B2, B3;
    boolean goFunA1 = false;
    boolean goFunA2 = false;
    boolean goFunA3 = false;
    boolean goFunB1 = false;
    boolean goFunB2 = false;
    boolean goFunB3 = false;
}

public class TaskB {

    /**
     * @param args 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        Data my_sample = new Data();
        int test_size = 4;

        // Loop to create and run threads multiple times
        for (int i = 0; i < test_size; i++) {
            System.out.println("Loop " + i + "\n");

            // Reset flags before each loop
            my_sample.goFunB2 = false;
            my_sample.goFunB3 = false;
            my_sample.goFunA1 = false;
            my_sample.goFunA2 = false;
            my_sample.goFunA3 = false;

            // Create and start threads
            ThreadA ta = new ThreadA(my_sample);
            ThreadB tb = new ThreadB(my_sample);

            ta.start();
            tb.start();

            // Wait for threads to finish
            ta.join();
            tb.join();
        }
    }
}

class ThreadA extends Thread {
    private Data sample;
    public ThreadA(Data sample) {
        this.sample = sample;
    }

    public void run() {
        // Calculate A1 and signal ThreadB to proceed
        synchronized (sample) {
            int n = 500;
            sample.A1 = n * (n + 1) / 2;
            System.out.println("A1 value is: " + sample.A1);
            sample.goFunB2 = true;
            sample.notify();
        }

        // Wait until ThreadB has finished B2 calculation
        synchronized (sample) {
            while (!sample.goFunA2) {
                System.out.println("First run B2");
                try {
                    sample.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Calculate A2 and signal ThreadB to proceed with B3
        synchronized (sample) {
            if (sample.goFunA2) {
                int n = 300;
                sample.A2 = sample.B2 + n * (n + 1) / 2;
                System.out.println("A2 value is: " + sample.A2);
                sample.goFunB3 = true;
                sample.notify();
            }
        }

        // Wait until ThreadB has finished B3 calculation
        synchronized (sample) {
            while (!sample.goFunA3) {
                try {
                    sample.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Calculate A3 and notify any waiting thread
        synchronized (sample) {
            int n = 400;
            sample.A3 = sample.B3 + n * (n + 1) / 2;
            System.out.println("A3 value is: " + sample.A3);
            sample.notify();
        }
    }
}

class ThreadB extends Thread {
    private Data sample;
    public ThreadB(Data sample) {
        this.sample = sample;
    }

    public void run() {
        // Calculate B1
        synchronized (sample) {
            int n = 250;
            sample.B1 = n * (n + 1) / 2;
            System.out.println("B1 value is: " + sample.B1);
            sample.notify();
        }

        // Wait until ThreadA has finished A1 calculation
        synchronized (sample) {
            while (!sample.goFunB2) {
                System.out.println("First run A1");
                try {
                    sample.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Calculate B2 and signal ThreadA to proceed with A2
        synchronized (sample) {
            if (sample.goFunB2) {
                int n = 200;
                sample.B2 = sample.A1 + n * (n + 1) / 2;
                System.out.println("B2 value is: " + sample.B2);
                sample.goFunA2 = true;
                sample.notify();
            }
        }

        // Wait until ThreadA has finished A2 calculation
        synchronized (sample) {
            while (!sample.goFunB3) {
                try {
                    sample.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Calculate B3 and notify any waiting thread
        synchronized (sample) {
            if (sample.goFunB3) {
                int n = 400;
                sample.B3 = sample.A2 + n * (n + 1) / 2;
                System.out.println("B3 value is: " + sample.B3);
                sample.goFunA3 = true;
                sample.notify();
            }
        }
    }
}
