package ru.alcereo;

import com.codahale.metrics.MetricRegistry;
import com.readytalk.metrics.StatsDReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by alcereo on 20.07.17.
 */
@Configuration
@EnableMetrics
public class MetricsConfiguration extends MetricsConfigurerAdapter{

    @Override
    public void configureReporters(MetricRegistry metricRegistry) {

        StatsDReporter.forRegistry(metricRegistry)
                .build("localhost", 8125)
                .start(2, TimeUnit.SECONDS);
    }
}
