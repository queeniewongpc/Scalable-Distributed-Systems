package MyMultithreadClient;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class APICallThread implements Runnable {
    public AtomicInteger successCount;
    public AtomicInteger unsuccessCount;
    private final BufferedWriter writer;
    private List<Request> request;
    private SkiersApi api;
    private int count;
    private ConcurrentHashMap<Integer, Long> timeStamps;
    String csvFile;

    APICallThread(BufferedWriter writer, List<Request> req, SkiersApi api, int count,
                  ConcurrentHashMap<Integer, Long> timeStamps,
                  AtomicInteger successCount, AtomicInteger unsuccessCount, String csvFile) {
        this.writer = writer;
        this.request = req;
        this.api = api;
        this.count = count;
        this.timeStamps = timeStamps;
        this.successCount = successCount;
        this.unsuccessCount = unsuccessCount;
        this.csvFile = csvFile;
    }

    @Override
    public void run() {
        try (writer) {
            for (int req = 0; req < this.count; ++req) {
                long timeElapsed = 0;

                try {
                    long startTime = System.currentTimeMillis();
                    ApiResponse<Void> statusCode = api.writeNewLiftRideWithHttpInfo(request.get(req).getBody(),
                            request.get(req).getResortID(), request.get(req).getSeasonID(), request.get(req).getDayID(),
                            request.get(req).getSkierID());
                    if (statusCode.getStatusCode() == 201) {
                        // System.out.println("SUCCESS");
                        successCount.incrementAndGet();
                        long endTime = System.currentTimeMillis();
                        timeElapsed = endTime - startTime;
                        timeStamps.put(req, timeElapsed);
                        synchronized (writer) {
                            writer.write(startTime + ", POST, " + timeElapsed + ", " + statusCode.getStatusCode());
                            writer.newLine();
                            writer.flush();
                        }
                        continue;
                    } else if (statusCode.toString().startsWith("4") ||
                            statusCode.toString().startsWith("5")) {
                        System.out.println("Web server or Servlet error");
                        int retry = 0;
                        while (statusCode.getStatusCode() != 201 || retry < 5) {
                            statusCode = api.writeNewLiftRideWithHttpInfo(request.get(req).getBody(),
                                    request.get(req).getResortID(), request.get(req).getSeasonID(), request.get(req).getDayID(),
                                    request.get(req).getSkierID());

                            if (statusCode.getStatusCode() == 201) {
                                successCount.incrementAndGet();
                                long endTime = System.currentTimeMillis();
                                timeElapsed = endTime - startTime;
                                timeStamps.put(req, timeElapsed);
                                synchronized (writer) {
                                    writer.write(startTime + ", POST, " + timeElapsed + ", " + statusCode.getStatusCode());
                                    writer.flush();
                                }
                                break;
                            }

                            ++retry;
                        }

                        if (retry >= 5) {
                            unsuccessCount.incrementAndGet();
                        }
                    }
                } catch (ApiException e) {
                    System.out.println("Exception when calling SkiersAPI@writeNewLiftRideWithHttpInfo");
                    unsuccessCount.incrementAndGet();
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error Writing to CSV File");
            e.printStackTrace();
        }
    }
}
