package course.concurrency.m3_shared;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {

    static Lock lock = new ReentrantLock();
    static Condition condition = lock.newCondition();
    static int state = 1; // 1 - печатаем 1, 2 - печатаем 2, 3 - печатаем 3

    public static void printNumber(int threadNumber) {
        for (int i = 0; i < 5; i++) { // печатаем по 5 раз каждый
            lock.lock();
            try {
                while (state != threadNumber) {
                    condition.await();
                }
                System.out.println(threadNumber);
                Thread.sleep(500); // чтобы визуально было видно
                // переходим к следующему потоку
                state = threadNumber % 3 + 1;
                condition.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void first() {
        printNumber(1);
    }

    public static void second() {
        printNumber(2);
    }

    public static void third() {
        printNumber(3);
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> first());
        Thread t2 = new Thread(() -> second());
        Thread t3 = new Thread(() -> third());
        t1.start();
        t2.start();
        t3.start();
    }
}
