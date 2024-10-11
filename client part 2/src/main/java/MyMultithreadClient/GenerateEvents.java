package MyMultithreadClient;

import io.swagger.client.model.LiftRide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GenerateEvents implements Runnable {
    private List<Request> requests = new ArrayList<>();

    @Override
    public void run() {
        try {
            // System.out.println("Generating 1000 requests!");

            for (int i = 0; i < 1000; ++i) {
                Random rand = new Random();
                int resortID = rand.nextInt(10) + 1;
                String seasonID = "2024";
                String dayID = "1";
                int skierID = rand.nextInt(100000) + 1;
                Integer time = rand.nextInt(360) + 1;
                Integer liftID = rand.nextInt(40) + 1;

                LiftRide body = new LiftRide();
                body.setTime(time);
                body.setLiftID(liftID);

                Request req = new Request(body, resortID, seasonID, dayID, skierID);

                requests.add(req);
            }
            // System.out.println("SIZE: " + requests.size());
        } catch (Exception e) {
            System.out.println("Exception generating requests!");
            e.printStackTrace();
        }
    }

    public List<Request> getRequests() {
        return this.requests;
    }
}
