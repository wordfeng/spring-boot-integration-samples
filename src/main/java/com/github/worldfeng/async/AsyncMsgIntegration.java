package com.github.worldfeng.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
public class AsyncMsgIntegration {

    @Autowired
    private ThreadPoolTaskExecutor testTaskExecutor;

    @Autowired
    @Qualifier("customErrorChannel")
    private MessageChannel customErrorChannel;

    @Bean("inputChannel")
    public MessageChannel inputChannel(@Autowired
                                       ThreadPoolTaskExecutor testTaskExecutor) {
        return new ExecutorChannel(testTaskExecutor);
    }

    @Bean(name = "customErrorChannel")
    public MessageChannel customErrorChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel outputChannel() {
        return new ExecutorChannel(testTaskExecutor());
    }

    @Bean
    public IntegrationFlow inputFlow() {
        return IntegrationFlows.from("inputChannel")
//                .errorChannel("customErrorChannel") // 低版本不支持
//                .channel(MessageChannels.executor(Executors.newSingleThreadExecutor()))
//                .handle("fuckService", "fuck")
                .handle(message -> {
                    try {
                        Thread.sleep(5000);
                        log.info("thread：{} , handle message: {}, size：{}", Thread.currentThread().getName(), message.getPayload(), testTaskExecutor.getThreadPoolExecutor().getQueue().size());
                        if ("error".equals(message.getPayload())) {
                            throw new RuntimeException("fuck!");
                        }
                    } catch (Exception e) {
                        customErrorChannel.send(new ErrorMessage(e, new MessageHeaders(null), message));
                    }
                })
//                .channel("outputChannel")
                .get();
    }


    @Bean
    public ThreadPoolTaskExecutor testTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.initialize();
        return executor;
    }

    @Bean
    public IntegrationFlow outputFlow() {
        return IntegrationFlows.from("outputChannel")
                .handle(message -> {
                            try {
//                                Thread.sleep(5000);
                                if ("error".equals(message.getPayload())) {
                                    throw new RuntimeException("fuck!");
                                }
                                log.info("fuck {} over!!", message.getPayload());
                                log.info("{} fucking", testTaskExecutor.getThreadPoolExecutor().getQueue().size());
                            } catch (Throwable e) {
                                customErrorChannel.send(new ErrorMessage(e));
                                throw new RuntimeException(e);
                            }
                        }
//                , e -> e.advice(retryAdvice)  //低版本不支持
                )
                .get();
    }


    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlows.from("customErrorChannel")
                .handle(ErrorMessage.class, (errorMessage, headers) -> {
                    Message<?> originalMessage = errorMessage.getOriginalMessage();
                    Throwable payload = errorMessage.getPayload();
                    log.error("异常入参：{}", originalMessage, payload);
                    return null;
                })
//                .handle(new GenericHandler<Object>() {
//                    @Override
//                    public Object handle(Object o, MessageHeaders messageHeaders) {
//                        System.out.println(o);
//                        System.out.println(messageHeaders);
//                        return null;
//                    }
//                })
                .get();
    }

//    @Bean
//    public RequestHandlerRetryAdvice retryAdvice() {
//        RequestHandlerRetryAdvice retryAdvice = new RequestHandlerRetryAdvice();
//
//        RetryTemplate retryTemplate = new RetryTemplate();
//        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3)); // 重试3次
//        retryTemplate.setBackOffPolicy(new FixedBackOffPolicy().withSleeper(new SleepingBackOffPolicy<>())); // 每次重试之间延迟1000毫秒
//
//        retryAdvice.setRetryTemplate(retryTemplate);
//        return retryAdvice;
//    }

}
