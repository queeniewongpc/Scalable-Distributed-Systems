package MyMultithreadClient;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public class MyClient {

    private static String url = "http://52.91.109.10:8080/JavaServletsLab_war/";

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger unsuccessCount = new AtomicInteger(0);

        // Create an instance of HttpClient.
        ApiClient client = new ApiClient();
        client.setBasePath(url);

        // Create a method instance.
        SkiersApi apiInstance = new SkiersApi(client);

        List<List<Request>> requestsForThread = new ArrayList<>();
        for (int i = 0; i < 200; ++i) {
            GenerateEvents events = new GenerateEvents();
            Thread thread = new Thread(events);
            thread.start();
            thread.join();
            requestsForThread.add(events.getRequests());
        }

        long startTime, endTime, totalTime;
        List<Thread> postRequests = new ArrayList<>();

        startTime = System.currentTimeMillis();
        for (int i = 0; i < 32; ++i) {
            try {
                List<Request> reqs = requestsForThread.get(i);
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, successCount, unsuccessCount));
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
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, successCount, unsuccessCount));
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
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, successCount, unsuccessCount));
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
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, successCount, unsuccessCount));
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
                Thread thread = new Thread(new APICallThread(i + 1, reqs, apiInstance, 1000, successCount, unsuccessCount));
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


        endTime = System.currentTimeMillis();
        totalTime = (endTime - startTime);

        System.out.println("SUCCESS REQUESTS: " + successCount);
        System.out.println("UNSUCCESS REQUESTS: " + unsuccessCount);
        System.out.println("Total Wall Time (ms): " + totalTime);
        System.out.println("Average: " + totalTime / successCount.get());

    }
}