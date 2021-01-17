package com.xxx.inventory.uploader;

import com.xxx.inventory.api.UpdateArticleResult;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class UploadArticleResultReporter implements StreamObserver<UpdateArticleResult> {

    final CountDownLatch finishedLatch;

    public UploadArticleResultReporter(CountDownLatch finishedLatch) {
        this.finishedLatch = finishedLatch;
    }

    @Override
    public void onNext(UpdateArticleResult value) {
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
