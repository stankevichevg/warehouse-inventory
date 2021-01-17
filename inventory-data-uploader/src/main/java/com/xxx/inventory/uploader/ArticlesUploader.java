package com.xxx.inventory.uploader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxx.inventory.api.InventoryServiceGrpc;
import com.xxx.inventory.api.UpdateArticleCommand;
import com.xxx.inventory.uploader.model.ArticleModel;
import com.xxx.inventory.uploader.model.InventoryCollectionModel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class ArticlesUploader {

    public static void main(String[] args) throws IOException, InterruptedException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final InventoryCollectionModel articles = objectMapper.readValue(
            new URL("file:/Users/estankevich/Documents/work/warehouse-inventory/inventory-data-uploader/src/main/resources/inventory.json"), InventoryCollectionModel.class
        );
        InventoryServiceGrpc.InventoryServiceStub client = InventoryServiceGrpc.newStub(
            ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build()
        );
        CountDownLatch finishedLatch = new CountDownLatch(1);
        final StreamObserver<UpdateArticleCommand> articleUploadStream = client.updateArticles(new UploadArticleResultReporter(finishedLatch));
        for (ArticleModel article : articles.getInventory()) {
            articleUploadStream.onNext(
                UpdateArticleCommand.newBuilder()
                    .setId(article.getId())
                    .setName(article.getName())
                    .setAvailable(article.getStock())
                    .build()
            );
        }
        articleUploadStream.onCompleted();
        finishedLatch.await();
    }

}
