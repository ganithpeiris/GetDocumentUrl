package com.Testing;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OpenLinks4 {
    private static final String BASE_URL = "https://hnbf.enadocapp.com/enadocHnbmigration/api/v3/documents/";
    private static final String AUTH_TOKEN = "Bearer CDEF57F19C6A446C851BEF320507DCFC7759C78A2AA448A09CB02E2DDDBD4C8695A6EFFA928C4C7BAB8333528D89887E1FA0531BE7AE46B294863E64492DA7CB";

    private static final int numberOfUsers = 3;  // Adjust number of users
    private static final int iterationsPerUser = 2; // Adjust iterations per user
    private static final int tabsPerIteration = 10;  // Number of tabs per iteration

    @Test
    public void loadTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);

        for (int i = 0; i < numberOfUsers; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerUser; j++) {
                        runIteration(j + 1);  // Pass iteration number for logging
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    private void runIteration(int iteration) throws InterruptedException, IOException {
        System.out.println("Starting Iteration: " + iteration);

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        try {
            List<String> documentUrls = fetchDocumentUrls(tabsPerIteration);
            openTabs(driver, documentUrls);
        } finally {
            driver.quit();  // Ensure browser closes before next iteration
            System.out.println("Iteration " + iteration + " Completed.\n");
        }
    }

    private List<String> fetchDocumentUrls(int limit) throws IOException {
        List<String> documentTokens = List.of(
                "pnI75AJwuC2pKxsMKA1ADcANgPADYAMwA0ADEAMgPxs",
                "FeMFb5uuaDa6r96bmoFJ22vhDk1UTYUMKPADcANAPADcAMwA1ADIANAPMFb5uu",
                "MLvGUsrLex3EXn8tGU1gMtMNKAGADEAMwAGADEANAA4APyGU1gM",
                "MVIr5CrYwkiKNNdTaLCobNKA1ADKANAA0ADcANgPADgANdTa",
                "YcMyfIm5FEvUZXgOAA1ADYANAAzADcANKAGADYAIm5F",
                "bdLUCDuqRxwN5eXBJbMKAGADcANKPADAANAA2ADIAMAPCDuqRx",
                "816zTF98R1ZHZq5ggLGisZ3ftk76MZ3BKtinEQoKmAmuRMKA0ADcANwAGADkANAA1ADAAOAPmAmuR",
                "4xLgLRRAkIHbbZcNRGAvUr9Kb9HeRo6ePaMKA3ADcAOKAGADgANAAzADkANgPRR",
                "U71wVWhPzuIUZ1URyKAhaH5YEpNvJcfycGMgAwADkANwA5ADUAMKA1ADAAMAPEpNv",
                "fsPXAfce4nuWhJdHZXK35FR9MfSxIIwMKA4ADcAOAPADKAOAA2ADkAMgPSxIIw"
        );

        List<String> documentUrls = new ArrayList<>();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            for (int i = 0; i < limit && i < documentTokens.size(); i++) {
                String token = documentTokens.get(i);
                String apiUrl = BASE_URL + token + "/url";
                HttpGet request = new HttpGet(apiUrl);
                request.setHeader("Authorization", AUTH_TOKEN);

                HttpResponse response = httpClient.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String jsonResponse = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    documentUrls.add(jsonObject.getString("url"));
                } else {
                    System.out.println("Error: Status " + statusCode + " for token " + token);
                }
            }
        }
        return documentUrls;
    }

    private void openTabs(WebDriver driver, List<String> urls) throws InterruptedException {
        for (String url : urls) {
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(url);
            Thread.sleep(1000);  // Small delay to simulate real-world behavior
        }
        Thread.sleep(5000);  // Keep tabs open for a few seconds before closing
    }
}
