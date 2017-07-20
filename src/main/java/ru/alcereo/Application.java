package ru.alcereo;

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

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by alcereo on 30.06.17.
 */
@SpringBootApplication
@RestController
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private MetricRegistry metricRegistry;

//    @Bean
//    public Timer timer(){
//        return metricRegistry.timer(name(Application.class, "business-requests"));
//    }
//
//    @Autowired
//    private Timer timer;

//    @Bean(destroyMethod = "stop")
//    StatsDReporter graphiteReporter() {
//        // add some JVM metrics (wrap in MetricSet to add better key prefixes)
////        MetricSet jvmMetrics = new MetricSet() {
////
////            @Override
////            public Map<String, com.codahale.metrics.Metric> getMetrics() {
////
////                Map<String, com.codahale.metrics.Metric> metrics = new HashMap<String, Metric>();
////                metrics.put("gc", new GarbageCollectorMetricSet());
////                metrics.put("file-descriptors", new FileDescriptorRatioGauge());
////                metrics.put("memory-usage", new MemoryUsageGaugeSet());
////                metrics.put("threads", new ThreadStatesGaugeSet());
////                return metrics;
////            }
////        };
////        metricRegistry.registerAll(jvmMetrics);
//
//        // create and start reporter
//        StatsDReporter reporter = StatsDReporter.forRegistry(metricRegistry)
//                .build("localhost", 8125);
//
//        reporter.start(2, TimeUnit.SECONDS);
//
//        return reporter;
//    }

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

        final Timer.Context context =
                metricRegistry.timer(
                        name(Application.class,
                                "business")
                ).time();

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

        }finally {
            context.stop();
        }

        return "ok";
    }

    public void deserialize(@RequestBody Map<String, String> event) {

        final Timer.Context context =
                metricRegistry.timer(
                        name(Application.class,
                                "deserialize")
                ).time();

        try {
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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

//    @Timed
    public void alarm(@RequestBody Map<String, String> event) {

        final Timer.Context context =
                metricRegistry.timer(
                        name(Application.class,
                                "alarm")
                ).time();

        try {

            try {
                Thread.sleep(new Random().nextInt(800));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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

        }finally {
            context.stop();
        }
    }

//    @Timed
    public void defaultEvent(@RequestBody Map<String, String> event) {

        final Timer.Context context =
                metricRegistry.timer(
                        name(Application.class,
                                "defaultEvent")
                ).time();


        try {

            try {
                Thread.sleep(new Random().nextInt(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
        }finally {
            context.stop();
        }
    }

//    @Timed
    public void command(@RequestBody Map<String, String> event) {

        final Timer.Context context =
                metricRegistry.timer(
                        name(Application.class,
                                "command")
                ).time();

        try {

            try {
                Thread.sleep(new Random().nextInt(600));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
        }finally {
            context.stop();
        }
    }

//    @Timed
    public void persist(Map<String, String> event) {

        final Timer.Context context =
                metricRegistry.timer(
                        name(Application.class,
                                "persist")
                ).time();

        try {

            try {
                Thread.sleep(new Random().nextInt(850));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
        }finally {
            context.stop();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
