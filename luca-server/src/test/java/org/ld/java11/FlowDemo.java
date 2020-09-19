package org.ld.java11;

import java.util.Arrays;

import java.util.concurrent.Flow.*;
import java.util.concurrent.SubmissionPublisher;

public class FlowDemo {

    public static void main(String[] args) {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        MySubscriber<String> subscriber = new MySubscriber<>();
        publisher.subscribe(subscriber);
        System.out.println("Publishing data items...");
        String[] items = {
                "jan", "feb", "mar", "apr", "may", "jun",
                "jul", "aug", "sep", "oct", "nov", "dec"
        };
        Arrays.stream(items).forEach(publisher::submit);
        publisher.close();
        try {
            synchronized ("A") {
                "A".wait();
            }
        } catch (InterruptedException ignored) {
        }
    }

    static class MySubscriber<T> implements Subscriber<T> {
        private Subscription subscription;

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T item) {
            System.out.println("Received: " + item);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
            synchronized ("A") {
                "A".notifyAll();
            }
        }

        @Override
        public void onComplete() {
            System.out.println("Done");
            synchronized ("A") {
                "A".notifyAll();
            }
        }
    }
}
