package com.example.selrepro;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v117.network.Network;
import org.openqa.selenium.devtools.v117.network.model.Headers;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SelReproApplication {

    private static final ChromeOptions CHROME_OPTIONS = new ChromeOptions().addArguments(List.of(
            "--ignore-certificate-errors",
            "--start-maximized",
            "--no-sandbox",
            "--disable-setuid-sandbox",
            "--no-zygote",
            "--dns-prefetch-disable",
            "--disable-gpu"
    ));


    public static void main(String[] args) throws Exception {
        String gridUrl = Objects.requireNonNull(System.getenv("GRID_URL"));
        String jdkHttpClientEnabled = Objects.requireNonNull(System.getenv("JDK_HTTPCLIENT_ENABLED"));
        Duration runDuration = Duration.parse(Objects.requireNonNull(System.getenv("RUN_DURATION")));

        if (Boolean.TRUE.toString().equals(jdkHttpClientEnabled)) {
            System.out.println("JDK http client is enabled");
            System.setProperty("webdriver.http.factory", "jdk-http-client");
        }

        Instant finish = Instant.now().plus(runDuration);
        while (finish.isAfter(Instant.now())) {

            WebDriver driver = null;
            try {

                driver = RemoteWebDriver.builder()
                        .augmentUsing(new Augmenter())
                        .oneOf(CHROME_OPTIONS)
                        .address(gridUrl)
                        .build();

                try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
                    devTools.createSession();
                    devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
                    devTools.send(Network.setExtraHTTPHeaders(new Headers(Map.of("test", Boolean.TRUE.toString()))));
                    devTools.addListener(Network.requestWillBeSent(), event -> {

                    });
                }
            } catch (Exception e) {
                System.err.println("Exception while running selenium" + e.getMessage());
            } finally {
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception e) {
                        System.err.println("Can not close driver session" + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Finished running");
        while (true) {
            TimeUnit.SECONDS.sleep(10);
        }
    }
}
