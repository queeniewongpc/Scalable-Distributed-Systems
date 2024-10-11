package MyMultithreadClient;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.awt.geom.CubicCurve2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class MyClient {

    private static String url = "http://35.153.139.145:8080/JavaServletsLab_war/";
    private ConcurrentHashMap<Integer, Long> timestamps;
    private static  String csvFilePath = "/Users/queeniewong/Downloads/Assignment 1/client part " +
            "2/src/main/java/MyMultithreadClient/output.csv";

    public MyClient() {
        this.timestamps = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        //AtomicInteger successCount = new AtomicInteger(0);
        //AtomicInteger unsuccessCount = new AtomicInteger(0);
        //String csvFilePath = "/Users/queeniewong/Downloads/Assignment 1/client part " +
                //"2/src/main/java/MyMultithreadClient/output.csv";
        //ConcurrentHashMap<Integer, Long> timestamps = new ConcurrentHashMap<>();

        // Create an instance of HttpClient.
        ApiClient client = new ApiClient();
        client.setBasePath(url);

        // Create a method instance.
        SkiersApi apiInstance = new SkiersApi(client);

        MyClient myClient = new MyClient();
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
        try (writer) {
            writer.write("Start Time, Request Type, Elapsed Time (ms), Status Code\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        myClient.runBatch(writer, csvFilePath, 32, apiInstance, myClient.timestamps);
        myClient.runBatch(writer, csvFilePath, 32, apiInstance, myClient.timestamps);
        myClient.runBatch(writer, csvFilePath, 32, apiInstance, myClient.timestamps);
        myClient.runBatch(writer, csvFilePath, 32, apiInstance, myClient.timestamps);
        myClient.runBatch(writer, csvFilePath, 72, apiInstance, myClient.timestamps);

        writer.close();

        /*
        List<List<Request>> requestsForThread = new ArrayList<>();

        for (int i = 0; i < 200; ++i) {
            GenerateEvents events = new GenerateEvents();
            Thread thread = new Thread(events);
            thread.start();
            thread.join();
            requestsForThread.add(events.getRequests());
        }

        List<Thread> postRequests = new ArrayList<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            writer.write("Start Time, Request Type, Elapsed Time (ms), Status Code\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 32; ++i) {
            try {
                List<Request> reqs = requestsForThread.get(i);
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, timestamps,
                        successCount, unsuccessCount, csvFilePath));
                postRequests.add(thread);
                thread.start();

            } catch (Exception e) {
                System.err.println("Exception when Thread " + i + " is sending POST request");
                e.printStackTrace();
            }
        }

        for (Thread thread : postRequests) {
            thread.join();
        }

        for (int i = 32; i < 64; ++i) {
            try {
                List<Request> reqs = requestsForThread.get(i);
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, timestamps,
                        successCount, unsuccessCount, csvFilePath));
                postRequests.add(thread);
                thread.start();

            } catch (Exception e) {
                System.err.println("Exception when Thread " + i + " is sending POST request");
                e.printStackTrace();
            }
        }

        for (Thread thread : postRequests.subList(32, 64)) {
            thread.join();
        }

        for (int i = 64; i < 96; ++i) {
            try {
                List<Request> reqs = requestsForThread.get(i);
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, timestamps,
                        successCount, unsuccessCount, csvFilePath));
                postRequests.add(thread);
                thread.start();

            } catch (Exception e) {
                System.err.println("Exception when Thread " + i + " is sending POST request");
                e.printStackTrace();
            }
        }

        for (Thread thread : postRequests.subList(64, 96)) {
            thread.join();
        }

        for (int i = 96; i < 128; ++i) {
            try {
                List<Request> reqs = requestsForThread.get(i);
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, timestamps,
                        successCount, unsuccessCount, csvFilePath));
                postRequests.add(thread);
                thread.start();

            } catch (Exception e) {
                System.err.println("Exception when Thread " + i + " is sending POST request");
                e.printStackTrace();
            }
        }

        for (Thread thread : postRequests.subList(96, 128)) {
            thread.join();
        }

        for (int i = 128; i < 200; ++i) {
            try {
                List<Request> reqs = requestsForThread.get(i);
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, timestamps,
                        successCount, unsuccessCount, csvFilePath));
                postRequests.add(thread);
                thread.start();

            } catch (Exception e) {
                System.err.println("Exception when Thread " + i + " is sending POST request");
                e.printStackTrace();
            }
        }

        for (Thread thread : postRequests.subList(128, 200)) {
            thread.join();
        }

         */

        /*
        List<Long> elapsedTimes = new ArrayList<>(timestamps.values());

        // Calculate the total time
        long total = elapsedTimes.stream().mapToLong(Long::longValue).sum();

        //  Calculate the mean time
        double mean = total / (double) elapsedTimes.size();

        // Calculate the median time
        Collections.sort(elapsedTimes);  // Sort the times to find the median
        double median;
        int size = elapsedTimes.size();
        if (size % 2 == 0) {
            // Even number of elements, median is the average of the two middle values
            median = (elapsedTimes.get(size / 2 - 1) + elapsedTimes.get(size / 2)) / 2.0;
        } else {
            // Odd number of elements, median is the middle value
            median = elapsedTimes.get(size / 2);
        }

        // Calculate the P99 (99th percentile)
        int p99Index = (int) Math.ceil(99 / 100.0 * size) - 1;  // P99 index (99% of the way through the sorted list)
        long p99 = elapsedTimes.get(p99Index);  // P99 response time

        // Display the results
        System.out.println("Total Elapsed Time: " + total + " ms");
        System.out.println("Mean Elapsed Time: " + mean + " ms");
        System.out.println("Median Elapsed Time: " + median + " ms");
        System.out.println("p99")

         */
    }

    public void runBatch(BufferedWriter writer, String csvFilePath, int threadCount,
                         SkiersApi apiInstance, ConcurrentHashMap<Integer, Long> timestamps) throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger unsuccessCount = new AtomicInteger(0);

        List<List<Request>> requestsForThread = new ArrayList<>();

        for (int i = 0; i < threadCount; ++i) {
            GenerateEvents events = new GenerateEvents();
            Thread thread = new Thread(events);
            thread.start();
            thread.join();
            requestsForThread.add(events.getRequests());
        }

        List<Thread> postRequests = new ArrayList<>();

        for (int i = 0; i < threadCount; ++i) {
            try {
                List<Request> reqs = requestsForThread.get(i);
                Thread thread = new Thread(new APICallThread(writer, reqs, apiInstance, 1000, timestamps,
                        successCount, unsuccessCount, csvFilePath));
                postRequests.add(thread);
                thread.start();

            } catch (Exception e) {
                System.err.println("Exception when Thread " + i + " is sending POST request");
                e.printStackTrace();
            }
        }

        for (Thread thread : postRequests) {
            thread.join();
        }

        calculateStats(threadCount, successCount, unsuccessCount);
    }

    public void calculateStats(int threadCount, AtomicInteger successCount, AtomicInteger unsuccessCount) {
        List<Long> elapsedTimes = new ArrayList<>(timestamps.values());

        // Calculate the total time
        long total = elapsedTimes.stream().mapToLong(Long::longValue).sum();

        //  Calculate the mean time
        double mean = total / (double) elapsedTimes.size();

        // Calculate the median time
        Collections.sort(elapsedTimes);  // Sort the times to find the median
        double median;
        int size = elapsedTimes.size();
        if (size % 2 == 0) {
            // Even number of elements, median is the average of the two middle values
            median = (elapsedTimes.get(size / 2 - 1) + elapsedTimes.get(size / 2)) / 2.0;
        } else {
            // Odd number of elements, median is the middle value
            median = elapsedTimes.get(size / 2);
        }

        // Calculate the P99 (99th percentile)
        int p99Index = (int) Math.ceil(99 / 100.0 * size) - 1;  // P99 index (99% of the way through the sorted list)
        long p99 = elapsedTimes.get(p99Index);  // P99 response time

        long totalRequests = threadCount * 1000L;  // Assuming each thread performs 1000 requests
        double throughput = totalRequests / (total / 1000.0);  // Throughput in requests per second

        // Display the results
        System.out.println("Total Success Requests: " + successCount + " " + timestamps.size());
        System.out.println("Total Unsuccess Requests: " + unsuccessCount);
        System.out.println("Total Elapsed Time: " + total + " ms");
        System.out.println("Mean Elapsed Time: " + mean + " ms");
        System.out.println("Median Elapsed Time: " + median + " ms");
        System.out.println("Throughput: " + throughput + " rps");
        System.out.println("p99 Tile Response Time: " + p99 + " ms");
        System.out.println("Min Elapsed Time: " + elapsedTimes.get(0));
        System.out.println("Max Elapsed Time: " + elapsedTimes.get(elapsedTimes.size() - 1));
        System.out.flush();

        timestamps.clear();

        try {
            Thread.sleep(10000);  // Pause for 10 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}