package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Server {
        private final List<Integer> integers = new ArrayList<>();
        public static void main(String[] args) {
            Server server = new Server();
            final ReentrantLock thread1Lock = new ReentrantLock();
            final ReentrantLock thread2Lock = new ReentrantLock();
            AtomicBoolean valueSet = new AtomicBoolean(true);

            Thread thread1 = new Thread(() -> {
                synchronized (server) {
                    thread1Lock.lock();
                    for (int i = 0; i < 50; i += 2) {
                        while (!valueSet.get()) {
                            try {
                                server.wait();
                            }
                            catch (InterruptedException e) {
                                System.out.println(e.getMessage());
                            }}
                        server.addNumber(i);
                        valueSet.set(false);
                        server.notify();
                    }
                thread1Lock.unlock();
            }});

            Thread thread2 = new Thread(() -> {
                synchronized (server) {
                thread2Lock.lock();
                    for (int i = 1; i < 50; i += 2) {
                        while (valueSet.get()) {
                            try {
                                server.wait();
                            }
                            catch (InterruptedException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    server.addNumber(i);
                        valueSet.set(true);
                        server.notify();
                    }
                thread2Lock.unlock();
            }});

            thread1.start();
            thread2.start();

            thread1Lock.lock();
            thread2Lock.lock();

            try {
                thread1.join();
                thread2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            server.show();
        }

        private void show() {
            String array = integers.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            System.out.println(array);
        }

        public synchronized void addNumber(int i) {
            integers.add(i);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(200, 500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }
