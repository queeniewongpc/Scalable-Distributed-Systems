import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            if (urlParts.length == 3) {
                String resort = req.getParameter("resort");
                String season = req.getParameter("season");

                if (resort == null || resort.isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    res.getWriter().write("missing required parameters: resort");
                    return;
                }
            }

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

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
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

            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().write(jsonBody.toString());
        } catch (IOException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("invalid JSON body");
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlPath.length == 8) {
            try {
                int resortID = Integer.parseInt(urlPath[1]);
                if (resortID < 1 || resortID > 10) {
                    System.out.println("Invalid Resort ID");
                    return false;
                }

                if (!urlPath[2].equals("seasons")) {
                    System.out.println("param 2");
                    return false;
                }

                String seasonID = urlPath[3];
                if (!seasonID.equals("2024")) {
                    System.out.println("Invalid Season ID");
                    return false;
                }

                if (!urlPath[4].equals("days")) {
                    System.out.println("param 4");
                    return false;
                }

                String dayID = urlPath[5];
                if (!dayID.equals("1")) {
                    System.out.println("Invalid Day ID");
                    return false;
                }

                if (!urlPath[6].equals("skiers")) {
                    System.out.println("param 6");
                    return false;
                }

                int skierID = Integer.parseInt(urlPath[7]);
                if (skierID < 1 || skierID > 100000) {
                    System.out.println("Invalid Skier ID");
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        } else if (urlPath.length == 3 && urlPath[2].startsWith("vertical")) {
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
}
