import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

public class SkierLiftRideConsumer {
    private static final String QUEUE_NAME = "CS6650Assignment2Step1";
    private static final String RABBITMQ_HOST = "ec2-44-225-164-194.us-west-2.compute.amazonaws.com";
    private static final int THREAD_POOL_SIZE = 10;

    // Thread-safe hash map to store lift rides for each skier
    private static final ConcurrentHashMap<String, Integer> skierLiftRides = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println("[*] Waiting for messages. To exit press CTRL+C");

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            Runnable task = () -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                processMessage(message);
            };
            executorService.submit(task);
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }

    private static void processMessage(String message) {
        try {
            // Fix the JSON format if it contains an extraneous comma and merging objects
            if (message.contains("}, \"urlParams\":")) {
                message = message.replace("}, \"urlParams\":", ", \"urlParams\":");
            }

            // Add a missing closing brace if necessary
            message = message.trim() + "}";

            System.out.println("Received message: " + message);

            // Parse message in JSON format
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode messageNode = objectMapper.readTree(message);

            // Print out the parsed JSON to verify the structure
            System.out.println("Parsed JSON Node: " + messageNode.toPrettyString());

            // Extracting "urlParams" node
            JsonNode urlParamsNode = messageNode.get("urlParams");
            if (urlParamsNode != null) {
                System.out.println("Found urlParams: " + urlParamsNode.toPrettyString());
                String skierId = urlParamsNode.get("skierID").asText();

                // Assume each message has "liftID" which indicates one lift ride for that skier
                skierLiftRides.merge(skierId, 1, Integer::sum);
                System.out.println("[x] Processed message for skier " + skierId + ", total rides: " + skierLiftRides.get(skierId));
            } else {
                System.err.println("[!] urlParams not found in the message. Invalid message format: " + message);
            }
        } catch (IOException e) {
            System.err.println("[!] Failed to process message: " + message);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[!] Unexpected error while processing message: " + message);
            e.printStackTrace();
        }catch (Error err) {
            System.err.println("[!] Error occurred: " + err.getMessage());
            err.printStackTrace();
        }
    }

}