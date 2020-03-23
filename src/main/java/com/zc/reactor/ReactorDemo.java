package com.zc.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Description:
 *
 * @author Corey Zhang
 * @create 2020-03-10 11:58
 */
public class ReactorDemo {
    public static void main(String[] args) {
        Flux.just(1,2,3,4,5,6).subscribe(System.out::print);
        System.out.println();
        Mono.just(1).subscribe(System.out::print);
        System.out.println();


        System.out.println("------------");

        /**
         * @param consumer the consumer to invoke on each value
         * @param errorConsumer the consumer to invoke on error signal
         *  @param completeConsumer the consumer to invoke on complete signal
         */
        Flux.just(1,2,3,4,5,6)
                .subscribe(
                        System.out::println,
                        System.out::println,
                        () -> System.out.println("Completed")
                );

        System.out.println("------------");

        Mono.error(new Exception("UnKnow Exception."))
                .subscribe(
                        System.out::println,
                        System.err::println,
                        () -> System.out.println("Completed")
                );

        System.out.println("------------");



    }
}
