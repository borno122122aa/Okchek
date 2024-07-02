import java.io.*;
import java.net.*;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

public class LoadTest implements Runnable {

    private static final List<String> USER_AGENTS = Arrays.asList(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36"
    );

    private static int amount = 0;
    private static String url = "";
    private int seq;
    private int type;

    public LoadTest(int seq, int type) {
        this.seq = seq;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(1000); // Add delay to avoid rate-limiting
                switch (this.type) {
                    case 1:
                        postRequest(url);
                        break;
                    case 2:
                        sslPostRequest(url);
                        break;
                    case 3:
                        getRequest(url);
                        break;
                    case 4:
                        sslGetRequest(url);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter URL: ");
        url = scanner.nextLine();

        System.out.println("\nStarting load test on URL: " + url);

        String[] parsedUrl = url.split("://");
        if (parsedUrl[0].equals("http")) {
            checkConnection(url);
        } else {
            sslCheckConnection(url);
        }

        System.out.print("Number of threads: ");
        String amountInput = scanner.nextLine();
        amount = (amountInput.isEmpty()) ? 2000 : Integer.parseInt(amountInput);

        System.out.print("HTTP method (get/post): ");
        String method = scanner.nextLine();
        int requestType = determineRequestType(method, parsedUrl[0]);

        System.out.println("Starting load test with " + amount + " threads");
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Thread thread = new Thread(new LoadTest(i, requestType));
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Load test completed");
    }

    private static void checkConnection(String url) throws Exception {
        System.out.println("Checking connection to " + url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", getRandomUserAgent());

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("Connected to website");
        } else {
            System.err.println("Error connecting to website. Response code: " + responseCode);
        }
        LoadTest.url = url;
    }

    private static void sslCheckConnection(String url) throws Exception {
        System.out.println("Checking SSL connection to " + url);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", getRandomUserAgent());

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("Connected to website");
        } else {
            System.err.println("Error connecting to website. Response code: " + responseCode);
        }
        LoadTest.url = url;
    }

    private void postRequest(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", getRandomUserAgent());
        connection.setRequestProperty("Accept-Language", "en-US,en;");
        connection.setDoOutput(true);
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.writeBytes("load test payload");
            out.flush();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("POST request sent successfully, response code: " + responseCode + " Thread: " + seq);
        } else {
            System.err.println("Error in POST request, response code: " + responseCode + " Thread: " + seq);
        }
    }

    private void getRequest(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", getRandomUserAgent());

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("GET request sent successfully, response code: " + responseCode + " Thread: " + seq);
        } else {
            System.err.println("Error in GET request, response code: " + responseCode + " Thread: " + seq);
        }
    }

    private void sslPostRequest(String url) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", getRandomUserAgent());
        connection.setRequestProperty("Accept-Language", "en-US,en;");
        connection.setDoOutput(true);
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.writeBytes("load test payload");
            out.flush();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("SSL POST request sent successfully, response code: " + responseCode + " Thread: " + seq);
        } else {
            System.err.println("Error in SSL POST request, response code: " + responseCode + " Thread: " + seq);
        }
    }

    private void sslGetRequest(String url) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", getRandomUserAgent());

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("SSL GET request sent successfully, response code: " + responseCode + " Thread: " + seq);
        } else {
            System.err.println("Error in SSL GET request, response code: " + responseCode + " Thread: " + seq);
        }
    }

    private static String getRandomUserAgent() {
        return USER_AGENTS.get(new Random().nextInt(USER_AGENTS.size()));
    }

    private static int determineRequestType(String method, String protocol) {
        if (method.equalsIgnoreCase("get")) {
            return protocol.equals("http") ? 3 : 4;
        } else {
            return protocol.equals("http") ? 1 : 2;
        }
    }
    }
              
