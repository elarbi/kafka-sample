import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by elarbiaboussoror on 14/02/2017.
 */
public class AsyncSample {

    public static void main(String[] args) {
        long start = System.nanoTime();
        List<Shop> shops = Arrays.asList(new Shop("Carrefour"), new Shop("Monoprix"),
                new Shop("Toy Shop"), new Shop("Ecna Shop")
                , new Shop("New Shop"));

        /*takes 6 secondes since the streams are lazy evaluated !
        Stream<CompletableFuture<Double>> futureStream =
                shops.parallelStream()
                        .map(s -> s.asyncGetPrice("Biscuit sucré"));
        List<Double> prices = futureStream.map(f -> f.join()).collect(toList());
        */
        //this version is faster (4s) ! since we force the ComputableFutures to be evaluated (collect)

        List<CompletableFuture<Double>> completableFutureList = shops.parallelStream()
                .map(s -> s.asyncGetPrice("Biscuit sucré")).collect(toList());
        List<Double> prices = completableFutureList.parallelStream().map(f -> f.join()).collect(toList());


        System.out.println(prices);
        long end = System.nanoTime();
        System.err.println("elapsed time: " + TimeUnit.NANOSECONDS.toSeconds(end - start));
    }

    private static class Shop {
        public final String name;

        public Shop(String name) {
            this.name = name;
        }

        public double getPrice(String productName) {
            delay(2_000);
            double v = new Random().nextDouble();
            return v;
        }

        public CompletableFuture<Double> asyncGetPrice(String productName) {
            CompletableFuture<Double> completableFuture = CompletableFuture.supplyAsync(() -> getPrice(productName));
            return completableFuture;
        }

        private void delay(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
