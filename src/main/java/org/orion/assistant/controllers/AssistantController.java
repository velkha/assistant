package org.orion.assistant.controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.orion.assistant.persistence.models.User;
import org.orion.assistant.persistence.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AssistantController {

    private ProcessService processService;
    private static final Logger LOG = LogManager.getLogger(AssistantController.class);
    private static String testSession;
    @Autowired
    public AssistantController(ProcessService processService) {
        this.processService = processService;
        Properties properties = new Properties();
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream("src/main/resources/temporal.properties");
            properties.load(fileInputStream);
            testSession = properties.getProperty("session");
        } catch (IOException e) {
            LOG.error("Error loading Temporal properties file", e);
        }
        
    }

    /**
     * Test API
     * @param message
     * @return
     */
    //TODO: change to POST & return type to JSON or HTTPResponse like
    @GetMapping("/test")
    public String test(@RequestParam("message") String message) {
        /**
         * 1. Recreate the user
         * 2. Check if the user have an active sesion
         * 3. If not, create a new session
         * 4. Send the message to the Watson Assistant
         * 5. Return the response
         */

        return processService.process(createTemporalTestUser(), testSession, message);
    }

    @PostMapping("/restartSession")
    public String restartSession() {
        /**
         * 1. Recreate the user
         * 2. Assing a new session
         */
        return "Session restarted";
    }

    public User createTemporalTestUser() {
        return new User("name", "mail", null, "null");
    }
}
