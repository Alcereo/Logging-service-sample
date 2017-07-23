package ru.alcereo;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by alcereo on 30.06.17.
 */
@SpringBootApplication
@RestController
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private MetricRegistry registry;

//    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private ExecutorService threadPool = Executors.newFixedThreadPool(5000);

    @Autowired
    public Application(MetricRegistry metricRegistry) {
        this.registry = metricRegistry;

        registry.register(
                name(Application.class, "tread-pool-job-count"),
                (Gauge<Integer>) () -> ((ThreadPoolExecutor)threadPool).getQueue().size()
        );
    }

//    private ExecutorService threadPool = Executors.newCachedThreadPool();


    @GetMapping("/")
    public String testGet() {
        Integer id = new Random().nextInt(100);
        String msg = "Some logs id: " + id;
        logger.debug(msg);

        return msg;
    }

    @GetMapping("/exc")
    public String getException() {
        Integer id = new Random().nextInt(100);
        String msg = "Some logs id: " + id;
        logger.debug(msg);

        if (true)
            throw new RuntimeException("Exception!");

        return msg;
    }

    @PostMapping("/business")
    public String getEvent(@RequestBody Map<String, String> event) {

        registry.meter("business").mark();
        registry.counter("business-counter").inc();

        Counter counter = registry.counter("business-worker-counter");

        threadPool.submit(
                () -> timedFunction(
                        "business",
                        () -> {

                            counter.inc();

                            try {

                                MDC.put("event", event);
                                MDC.put("stage", "get_event");
                                logger.debug("get event");

                                switch (event.get("event")) {
                                    case "success":
                                        logger.debug("success read event");
                                        deserialize(event);
                                        break;

                                    default:
                                        logger.error("get event error");
                                        throw new RuntimeException("get event error. with code: " + event.get("event"));
                                }

                                MDC.clear();

                                return "finish";

                            }finally {
                                counter.dec();
                            }
                        }
                )
        );

        return "ok";
    }

    public <RESULT> RESULT timedFunction(String function_name, Supplier<RESULT> function){

        final Timer.Context context =
                registry.timer(
                        name(Application.class,
                                function_name)
                ).time();

        try {
            return function.get();
        } finally {
            context.stop();
        }

    }

    public void deserialize(@RequestBody Map<String, String> event) {

        final Timer.Context context =
                registry.timer(
                        name(Application.class,
                                "deserialize")
                ).time();

        try {
            sleep(1000);

            MDC.put("stage", "deserialize");

            switch (event.get("deserialize")) {
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
                    throw new RuntimeException("deserialize error. with code: " + event.get("deserialize"));
            }

        } finally {
            context.stop();
        }

    }

    private static void sleep(int bound) {
        try {
            Thread.sleep(new Random().nextInt(bound));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void alarm(@RequestBody Map<String, String> event) {

        timedFunction(
                "alarm",
                () -> {
                    sleep(800);

                    MDC.put("stage", "alarm_event");
                    switch (event.get("alarm")) {
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
                            throw new RuntimeException("cant find strategy for alarm. with code: " + event.get("alarm"));

                    }
                    return null;
                });
    }

    //    @Timed
    public void defaultEvent(@RequestBody Map<String, String> event) {

        timedFunction("defaultEvent",
                () -> {
                    sleep(500);

                    MDC.put("stage", "default_event");
                    logger.debug("is default event");

                    switch (event.get("default")) {
                        case "ok":
                            logger.debug("success handle default event");
                            break;

                        default:
                            logger.error("cant find strategy for default");
                            throw new RuntimeException("cant find strategy for default. with code: " + event.get("default"));
                    }
                    return null;
                }
        );
    }

    public void command(@RequestBody Map<String, String> event) {

        timedFunction("command",() -> {

            sleep(600);

            MDC.put("stage", "run_command");
            logger.debug("try to run command for alarm");

            switch (event.get("command")) {
                case "ok":
                    logger.debug("command success send");
                    break;

                default:
                    logger.error("command running fail");
                    throw new RuntimeException("command running fail. with code: " + event.get("command"));
            }

            return null;
        });
    }

    //    @Timed
    public void persist(Map<String, String> event) {

        timedFunction("persist",
                () -> {
                    sleep(850);

                    MDC.put("stage", "persist");
                    logger.debug("try to persist alarm");

                    switch (event.get("persist")) {
                        case "ok":
                            logger.debug("success persist alarm");
                            break;

                        default:
                            logger.error("alarm persist fail");
                            throw new RuntimeException("alarm persist fail. with code: " + event.get("persist"));
                    }

                    return null;
                }
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
