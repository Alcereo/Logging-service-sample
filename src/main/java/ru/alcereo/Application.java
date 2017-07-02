package ru.alcereo;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

/**
 * Created by alcereo on 30.06.17.
 */
@SpringBootApplication
@RestController
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @GetMapping("/")
    public String testGet(){
        Integer id = new Random().nextInt(100);
        String msg = "Some logs id: " + id;
        logger.debug(msg);

        return msg;
    }

    @GetMapping("/exc")
    public String getException(){
        Integer id = new Random().nextInt(100);
        String msg = "Some logs id: " + id;
        logger.debug(msg);

        if (true)
            throw new RuntimeException("Exception!");

        return msg;
    }

    @PostMapping("/business")
    public String getEvent(@RequestBody Map<String, String> event){

        MDC.put("event", event);
        MDC.put("stage", "get_event");
        logger.debug("get event");

        switch (event.get("event")){
            case "success":
                logger.debug("success read event");
                deserialize(event);
                break;

            default:
                logger.error("get event error");
                throw new RuntimeException("get event error. with code: "+event.get("event"));
        }

        return "ok";
    }

    private void deserialize(@RequestBody Map<String, String> event) {
        MDC.put("stage", "deserialize");

        switch (event.get("deserialize")){
            case "alarm":
                logger.debug("is alarm event");
                alarm(event);
                break;

            case "default":
                logger.debug("is default event");
                defaultEvent(event);
                break;

            default:
                logger.error("deserialize error");
                throw new RuntimeException("deserialize error. with code: "+event.get("deserialize"));
        }
    }

    private void alarm(@RequestBody Map<String, String> event) {
        MDC.put("stage", "alarm_event");
        switch (event.get("alarm")){
            case "persist":
                logger.debug("try to persist alarm");
                persist(event);
                break;

            case "command":
                logger.debug("try to run command for alarm");
                command(event);
                break;

            case "com-pers":
                logger.debug("try to run command and persist for alarm");
                persist(event);
                command(event);
                break;

            default:
                logger.error("cant find strategy for alarm");
                throw new RuntimeException("cant find strategy for alarm. with code: "+event.get("alarm"));

        }
    }

    private void defaultEvent(@RequestBody Map<String, String> event) {
        MDC.put("stage", "default_event");
        logger.debug("is default event");

        switch (event.get("default")){
            case "ok":
                logger.debug("success handle default event");break;

            default:
                logger.error("cant find strategy for default");
                throw new RuntimeException("cant find strategy for default. with code: "+event.get("default"));
        }
    }

    private void command(@RequestBody Map<String, String> event) {
        MDC.put("stage", "run_command");
        logger.debug("try to run command for alarm");

        switch (event.get("command")){
            case "ok":
                logger.debug("command success send");break;

            default:
                logger.error("command running fail");
                throw new RuntimeException("command running fail. with code: "+event.get("command"));
        }
    }

    private void persist(Map<String, String> event) {
        MDC.put("stage", "persist");
        logger.debug("try to persist alarm");

        switch (event.get("persist")){
            case "ok":
                logger.debug("success persist alarm");break;

            default:
                logger.error("alarm persist fail");
                throw new RuntimeException("alarm persist fail. with code: "+event.get("persist"));
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
