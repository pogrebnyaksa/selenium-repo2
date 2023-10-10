package com.example.selrepro.runner;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v117.network.Network;
import org.openqa.selenium.devtools.v117.network.model.Headers;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppRunner implements CommandLineRunner {

    private static final ChromeOptions chromeOptions = new ChromeOptions().addArguments(List.of(
            "--ignore-certificate-errors",
            "--start-maximized",
            "--no-sandbox",
            "--disable-setuid-sandbox",
            "--no-zygote",
            "--dns-prefetch-disable",
            "--disable-gpu"
    ));


    @Value("${GRID_URL}")
    private String gridUrl;


    @Value("${JDK_HTTPCLIENT_ENABLED}")
    private Boolean jdkhttpClicentEnabled;


    @Value("${RUN_DURATION}")
    private Duration runDuration;


    @Override
    public void run(String... args) {
        if (Boolean.TRUE.equals(jdkhttpClicentEnabled)) {
            log.info("JDK http client is enabled");
            System.setProperty("webdriver.http.factory", "jdk-http-client");
        }
        runSelenium();
    }


    @SneakyThrows
    private void runSelenium() {
        Instant finish = Instant.now().plus(runDuration);
        while (finish.isAfter(Instant.now())) {

            WebDriver driver = null;
            try {

                driver = RemoteWebDriver.builder()
                        .augmentUsing(new Augmenter())
                        .oneOf(chromeOptions)
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
                log.info("Exception while running selenium", e);
            } finally {
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception e) {
                        log.info("Can not close driver session", e);
                    }
                }
            }
        }
        log.info("Finished running");
        while (true) {
            TimeUnit.SECONDS.sleep(10);
        }
    }
}
