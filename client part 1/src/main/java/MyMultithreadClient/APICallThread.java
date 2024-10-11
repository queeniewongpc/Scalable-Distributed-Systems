package MyMultithreadClient;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class APICallThread implements Runnable {
    public AtomicInteger successCount;
    public AtomicInteger unsuccessCount;
    private int id;
    private List<Request> request;
    private SkiersApi api;
    private int count;

    APICallThread(int id, List<Request> req, SkiersApi api, int count, AtomicInteger successCount, AtomicInteger unsuccessCount) {
        this.id = id;
        this.request = req;
        this.api = api;
        this.count = count;
        this.successCount = successCount;
        this.unsuccessCount = unsuccessCount;
    }

    @Override
    public void run() {
        for (int req = 0; req < this.count; ++req) {

            try {
                ApiResponse<Void> statusCode = api.writeNewLiftRideWithHttpInfo(request.get(req).getBody(),
                request.get(req).getResortID(), request.get(req).getSeasonID(), request.get(req).getDayID(),
                                request.get(req).getSkierID());
                if (statusCode.getStatusCode() == 201) {
                    // System.out.println("SUCCESS");
                    successCount.incrementAndGet();
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
    }
}
