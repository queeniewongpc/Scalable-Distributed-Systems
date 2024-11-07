import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@WebServlet(value = "/skiers/*")
//@WebServlet(value = "/skiers/*/person/*/resorts/*")
public class SkierServlet extends HttpServlet {
    private static final String QUEUE_NAME = "CS6650Assignment2Step1"; // Replace with your actual queue name
    private static final String RABBITMQ_HOST = "ec2-44-225-164-194.us-west-2.compute.amazonaws.com"; // Replace with your EC2 IP or DNS

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts, res)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            res.setStatus(HttpServletResponse.SC_OK);

            System.out.print("The URL parts received is: ");
            System.out.println(urlPath);
            res.getWriter().write("It works!");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

        // Check if URL path is valid
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts, res)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Parse the JSON body of the request
            BufferedReader reader = req.getReader();
            StringBuilder jsonBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }

            String body = jsonBody.toString();
            if (!isValidLiftRide(body)) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("Invalid request body.");
                return;
            }

            // Add URL parameters (resortID, seasonID, dayID, skierID) to the request payload
            StringBuilder payloadWithParams = new StringBuilder(body);
            payloadWithParams.append(", \"urlParams\": {");
            payloadWithParams.append("\"resortID\": \"" + urlParts[1] + "\", ");
            payloadWithParams.append("\"seasonID\": \"" + urlParts[3] + "\", ");
            payloadWithParams.append("\"dayID\": \"" + urlParts[5] + "\", ");
            payloadWithParams.append("\"skierID\": \"" + urlParts[7] + "\"}");

            boolean success = publishToRabbitMQ(payloadWithParams.toString());

            if (success) {
                res.setStatus(HttpServletResponse.SC_CREATED);
                res.getWriter().write("{\"message\": \"Message successfully sent to RabbitMQ.\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                res.getWriter().write("{\"message\": \"Failed to send message to RabbitMQ.\"}");
            }

        } catch (IOException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("invalid JSON body");
        }
        catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("{\"message\": \"An error occurred: " + e.getMessage() + "\"}");
        }
    }

    private boolean isValidLiftRide(String body) {
        try {
            // Manually parse and validate the JSON string
            int time = getValueFromJson(body, "time");
            int liftID = getValueFromJson(body, "liftID");

            // Validate int16 range (assuming Java 'short' range for int16)
            return isInRange(time, Short.MIN_VALUE, Short.MAX_VALUE) &&
                    isInRange(liftID, Short.MIN_VALUE, Short.MAX_VALUE);
        } catch (Exception e) {
            return false;
        }
    }

    private int getValueFromJson(String json, String key) throws Exception {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) throw new Exception("Key not found");

        startIndex += searchKey.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) endIndex = json.indexOf("}", startIndex);

        if (endIndex == -1) throw new Exception("Invalid JSON format");

        String valueString = json.substring(startIndex, endIndex).trim();
        return Integer.parseInt(valueString);
    }

    private boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    private boolean isUrlValid(String[] urlPath, HttpServletResponse res) {
        // urlPath = "/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skier/{skierID}"
        // urlParts = [, {resortID}, seasons, {seasonID}, days, {dayID}, skier, {skierID}]
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        //
        // skierID - between 1 and 100000
        // resortID - between 1 and 10
        // liftID - between 1 and 40
        // seasonID - 2024
        // dayID - 1
        // time - between 1 and 360

        if (urlPath.length == 8) {
            try {
                int resortID = Integer.parseInt(urlPath[1]);
                if (resortID < 1 || resortID > 10) {
                    System.err.println("invalid resortID");
                    res.getWriter().write("invalid resortID - expected between 1 and 10");
                    return false;
                }

                if (!"seasons".equals(urlPath[2])) {
                    System.err.println("invalid url - expected 'seasons'");
                    res.getWriter().write("invalid url - expected 'seasons'");
                    return false;
                }

                int seasonID = Integer.parseInt(urlPath[3]);
                if (seasonID != 2024) {
                    System.err.println("invalid seasonID");
                    res.getWriter().write("invalid seasonID - expected '2024'");
                    return false;
                }

                if (!"days".equals(urlPath[4])) {
                    System.err.println("invalid url - expected 'days'");
                    res.getWriter().write("invalid url - expected 'days'");
                    return false;
                }

                int dayID = Integer.parseInt(urlPath[5]);
                if (dayID < 1 || dayID > 366) {
                    System.err.println("invalid dayID");
                    res.getWriter().write("invalid dayID - expected between 1 and 366");
                    return false;
                }

                if (!"skier".equals(urlPath[6])) { // Singular "skier" as per the request
                    System.err.println("invalid url - expected 'skier'");
                    res.getWriter().write("invalid url - expected 'skier'");
                    return false;
                }

                int skierID = Integer.parseInt(urlPath[7]);
                if (skierID < 1 || skierID > 100000) {
                    System.err.println("invalid skierID");
                    res.getWriter().write("invalid skierID - expected between 1 and 100000");
                    return false;
                }
            } catch (NumberFormatException | IOException e) {
                return false;
            }

            return true;
        } else if (urlPath.length == 3 && "vertical".equals(urlPath[2])) {
            try {
                Integer.parseInt(urlPath[1]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean publishToRabbitMQ(String message) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST); // Set the RabbitMQ server host
        factory.setUsername("guest"); // Set RabbitMQ username (default is "guest")
        factory.setPassword("guest"); // Set RabbitMQ password (default is "guest")

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare the queue (idempotent)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            // Publish the message to the queue
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
