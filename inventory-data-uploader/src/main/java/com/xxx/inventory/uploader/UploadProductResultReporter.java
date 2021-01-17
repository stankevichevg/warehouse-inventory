package com.xxx.inventory.uploader;

import com.xxx.inventory.api.UpdateProductResult;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class UploadProductResultReporter implements StreamObserver<UpdateProductResult> {

    final CountDownLatch finishedLatch;

    public UploadProductResultReporter(CountDownLatch finishedLatch) {
        this.finishedLatch = finishedLatch;
    }

    @Override
    public void onNext(UpdateProductResult result) {
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(t.getMessage());
    }

    @Override
    public void onCompleted() {
        finishedLatch.countDown();
    }
}
