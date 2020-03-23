package com.zc.reactor;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author Corey Zhang
 * @create 2020-03-10 14:38
 */
public class ReactorDemoTest {

    private Flux<Integer> generateFluxFromOneToSix(){
        return Flux.just(1,2,3,4,5,6);
    }

    private Mono<Integer> generateMonoWithError(){
        return Mono.error(new Exception("UnKnow Exception."));
    }


    @Test
    public void testViaStepVerifier(){
        StepVerifier.create(generateFluxFromOneToSix())
                .expectNext(1,2,3,4,5,6)
                .expectComplete()
                .verify();

        StepVerifier.create(generateMonoWithError())
                .expectErrorMessage("UnKnow Exception.")
                .verify();

    }


    @Test
    public void testMap(){
        StepVerifier.create(Flux.range(1,6).map(i -> i*i))
                .expectNext(1,4,9,16,25,36)
                .verifyComplete();

    }

    @Test
    public void testFlatMap(){
        StepVerifier.create(
                Flux.just("flux","mono")
                .flatMap(s -> Flux.fromArray(s.split("\\s*")))
                .delayElements(Duration.ofMillis(100))
                .doOnNext(System.out::println)
        )
                .expectNext("f","l","u","x","m","o","n","o")
//                .expectNextCount(8)
                .verifyComplete();

    }

    @Test
    public void testFilter() {
        StepVerifier.create(Flux.range(1, 6)
                .filter(i -> i % 2 == 1)    // 1
                .map(i -> i ))
                .expectNext(1, 3, 5)   // 2
                .verifyComplete();

    }

    private Flux<String> getZipDescFlux() {
        String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
        return Flux.fromArray(desc.split("\\s+"));  // 1
    }

    @Test
    public void testSimpleOperarors() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.zip(
                getZipDescFlux(),
                Flux.interval(Duration.ofMillis(200))
        ).subscribe(
                t -> System.out.println(t.getT1()),
                null,
//                () -> System.out.println("Completed")
                countDownLatch::countDown
                );

        countDownLatch.await(10,TimeUnit.SECONDS);

    }

    private String getStringSync(){
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "Complete.";
    }

    @Test
    public void testSyncToAsync() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono.fromCallable(() -> getStringSync())
                .subscribeOn(Schedulers.elastic())
                .subscribe(
                        System.out::println,
                        null,
                        countDownLatch::countDown);
        System.out.println("Mono finally.");
        countDownLatch.await(20,TimeUnit.SECONDS);
    }

    @Test
    public void testDrrorHandling(){
        Flux.range(1,6)
                .map(i -> 10/(i-3))
                .map(i -> i*i)
                .subscribe(
                        System.out::println,
                        System.err::println
                );

        System.out.println("---------");

        Flux.range(1,6)
                .map(i -> 10/(i-3))
                .onErrorReturn(0)
                .map(i -> i*i)
                .subscribe(
                        System.out::println,
                        System.err::println
                );

        System.out.println("---------");

        Flux.range(1,6)
                .map(i -> 10/(i-3))
                .onErrorResume(e -> Mono.just(1))
                .map(i -> i*i)
                .subscribe(
                        System.out::println,
                        System.err::println
                );
    }



    @Test
    public void testBackpressure(){
        Flux.range(1,6)
                .doOnRequest(a -> System.out.println("Request "+ a +" values...."))
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        System.out.println("Subscribed and make a request...");
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Get value ["+value+"].");
                        request(1);
                    }
                });
    }



}
