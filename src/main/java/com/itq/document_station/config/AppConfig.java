package com.itq.document_station.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AppConfig {

    @Getter
    @Value("${app.partition:250}")
    private int PARTITION_SIZE;
    @Getter
    @Value("${app.worker.submit.batch-size:1000}")
    private int SUBMIT_SIZE;
    @Getter
    @Value("${app.worker.approve.batch-size:1000}")
    private int APPROVE_SIZE;
    @Getter
    @Value("${app.worker.submit.core-pool-size:4}")
    private int submitCorePoolSize;
    @Getter
    @Value("${app.worker.submit.max-pool-size:8}")
    private int submitMaxPoolSize;
    @Getter
    @Value("${app.worker.approve.core-pool-size:4}")
    private int approveCorePoolSize;
    @Getter
    @Value("${app.worker.approve.max-pool-size:8}")
    private int approveMaxPoolSize;

    @Bean(name = "taskSubmitExecutor")
    public ThreadPoolTaskExecutor taskSubmitExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(submitCorePoolSize); // минимум потоков
        executor.setMaxPoolSize(submitMaxPoolSize);   // максимум
        executor.setQueueCapacity(200);  // размер очереди
        executor.setThreadNamePrefix("submit-exec-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "taskApproveExecutor")
    public ThreadPoolTaskExecutor taskApproveExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(approveCorePoolSize); // минимум потоков
        executor.setMaxPoolSize(approveMaxPoolSize);   // максимум
        executor.setQueueCapacity(200);   // размер очереди
        executor.setThreadNamePrefix("approve-exec-");
        executor.initialize();
        return executor;
    }


}
