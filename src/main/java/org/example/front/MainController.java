package org.example.front;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class MainController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(@RequestHeader(value = "User-Agent") String userAgent) throws IOException {
        ClassPathResource htmlFile;
        if (isMobile(userAgent)) {
            htmlFile = new ClassPathResource("templates/index.html");
        } else {
            htmlFile = new ClassPathResource("templates/index.html");
        }
        return StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
    }

    private boolean isMobile(String userAgent) {
        if (userAgent == null) return false;
        String ua = userAgent.toLowerCase();
        return ua.contains("android") || ua.contains("iphone") || ua.contains("ipad") || ua.contains("mobile");
    }
}