import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class SkierLiftRideConsumer {
    private static final String QUEUE_NAME = "CS6650Assignment2Step1";
    private static final String RABBITMQ_HOST = "ec2-44-225-164-194.us-west-2.compute.amazonaws.com";
    private static final int THREAD_POOL_SIZE = 4;
    private static final int BATCH_SIZE = 1;

    private static final BasicDataSource dataSource = new BasicDataSource();
    private static final BlockingQueue<List<LiftRide>> writeQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws Exception {
        // Initialize RabbitMQ Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername("guest");
        factory.setPassword("guest");

        com.rabbitmq.client.Connection rabbitConnection = factory.newConnection();
        Channel channel = rabbitConnection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println("[*] Waiting for messages. To exit press CTRL+C");

        // Initialize Database Connection Pool
        initializeDatabaseConnectionPool();

        // Initialize Write Thread
        initializeWriteThread();

        // Thread Pool for Concurrent Processing
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            executorService.submit(() -> processMessage(message));
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    private static void initializeDatabaseConnectionPool() {
        dataSource.setUrl("jdbc:mysql://localhost:3306/assignment3");
        dataSource.setUsername("admin");
        dataSource.setPassword("admin");
        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxTotal(20);
        dataSource.setInitialSize(5);
        System.out.println("[*] Database connection pool initialized.");
    }

    private static void initializeWriteThread() {
        Thread writerThread = new Thread(() -> {
            while (true) {
                try {
                    List<LiftRide> batch = writeQueue.take();
                    saveBatchToDatabase(batch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("[!] Write thread interrupted.");
                } catch (Exception e) {
                    System.err.println("[!] Error in write thread.");
                    e.printStackTrace();
                }
            }
        });
        writerThread.start();
    }

    private static void processMessage(String message) {
        try {
            if (message.contains("}, \"urlParams\":")) {
                message = message.replace("}, \"urlParams\":", ", \"urlParams\":");
            }

            message = message.trim() + "}";
            System.out.println("Received message: " + message);

            // Parse JSON Message
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode messageNode = objectMapper.readTree(message);

            JsonNode urlParamsNode = messageNode.get("urlParams");
            if (urlParamsNode != null) {
                String skierId = urlParamsNode.get("skierID").asText();
                String resortId = urlParamsNode.get("resortID").asText();
                String seasonId = urlParamsNode.get("seasonID").asText();
                String dayId = urlParamsNode.get("dayID").asText();
                int liftId = messageNode.get("liftID").asInt();
                int time = messageNode.get("time").asInt();
                int vertical = liftId * 10;

                LiftRide liftRide = new LiftRide(skierId, resortId, seasonId, dayId, liftId, time, vertical);

                synchronized (writeQueue) {
                    List<LiftRide> batch = new ArrayList<>(BATCH_SIZE);
                    batch.add(liftRide);

                    if (batch.size() >= BATCH_SIZE) {
                        enqueueBatch(batch);
                    }
                }
            } else {
                System.err.println("[!] Invalid message format: " + message);
            }
        } catch (IOException e) {
            System.err.println("[!] Failed to process message: " + message);
            e.printStackTrace();
        }
    }

    private static void enqueueBatch(List<LiftRide> batch) {
        try {
            writeQueue.put(new ArrayList<>(batch));
            batch.clear();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[!] Failed to enqueue batch.");
        }
    }

    private static void saveBatchToDatabase(List<LiftRide> batch) {
        String insertQuery = "INSERT INTO LiftRides (skier_id, resort_id, season_id, day_id, lift_id, time, vertical) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(insertQuery)) {

            conn.setAutoCommit(false);

            for (LiftRide ride : batch) {
                preparedStatement.setString(1, ride.getSkierId());
                preparedStatement.setString(2, ride.getResortId());
                preparedStatement.setString(3, ride.getSeasonId());
                preparedStatement.setString(4, ride.getDayId());
                preparedStatement.setInt(5, ride.getLiftId());
                preparedStatement.setInt(6, ride.getTime());
                preparedStatement.setInt(7, ride.getVertical());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            conn.commit();
            System.out.println("[*] Batch of " + batch.size() + " records inserted into database.");
        } catch (SQLException e) {
            System.err.println("[!] Failed to save batch to database.");
            e.printStackTrace();
        }
    }
}

// LiftRide Class to Represent Each Record
class LiftRide {
    private final String skierId;
    private final String resortId;
    private final String seasonId;
    private final String dayId;
    private final int liftId;
    private final int time;
    private final int vertical;

    public LiftRide(String skierId, String resortId, String seasonId, String dayId, int liftId, int time, int vertical) {
        this.skierId = skierId;
        this.resortId = resortId;
        this.seasonId = seasonId;
        this.dayId = dayId;
        this.liftId = liftId;
        this.time = time;
        this.vertical = vertical;
    }

    public String getSkierId() { return skierId; }
    public String getResortId() { return resortId; }
    public String getSeasonId() { return seasonId; }
    public String getDayId() { return dayId; }
    public int getLiftId() { return liftId; }
    public int getTime() { return time; }
    public int getVertical() { return vertical; }
}