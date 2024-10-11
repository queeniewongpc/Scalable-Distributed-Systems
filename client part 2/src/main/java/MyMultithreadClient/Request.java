package MyMultithreadClient;

import io.swagger.client.model.LiftRide;

class Request {
    private int resortID;
    private int skierID;
    private String seasonID;
    private String dayID;
    private LiftRide body;

    Request(LiftRide body, int resortID, String seasonID, String dayID, int skierID) {
        this.body = body;
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.skierID = skierID;
    }

    public int getResortID() {
        return resortID;
    }

    public String getSeasonID() {
        return seasonID;
    }

    public String getDayID() {
        return dayID;
    }

    public int getSkierID() {
        return skierID;
    }

    public LiftRide getBody() {
        return body;
    }
}
