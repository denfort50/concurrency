package course.concurrency.m2_async.minPrice;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        ExecutorService executor = Executors.newFixedThreadPool(shopIds.size());
        List<Callable<Double>> tasks = shopIds.stream()
                .map(shopId -> (Callable<Double>) () -> {
                    try {
                        return priceRetriever.getPrice(itemId, shopId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .collect(Collectors.toList());

        List<Future<Double>> futures;
        try {
            futures = executor.invokeAll(tasks, 2750, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            return Double.NaN;
        }

        executor.shutdownNow(); // важно завершить задачи

        List<Double> prices = futures.stream()
                .filter(Future::isDone) // задача завершилась (вовремя или нет)
                .map(future -> {
                    try {
                        return future.get(); // если завершилась — get безопасен
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return prices.stream()
                .min(Double::compareTo)
                .orElse(Double.NaN);
    }

}
