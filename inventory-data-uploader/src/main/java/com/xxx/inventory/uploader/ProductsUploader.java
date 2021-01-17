package com.xxx.inventory.uploader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxx.inventory.api.InventoryServiceGrpc;
import com.xxx.inventory.api.ProductArticleLink;
import com.xxx.inventory.api.UpdateProductCommand;
import com.xxx.inventory.uploader.model.ProductArticleModel;
import com.xxx.inventory.uploader.model.ProductModel;
import com.xxx.inventory.uploader.model.ProductsCollectionModel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class ProductsUploader {

    public static void main(String[] args) throws IOException, InterruptedException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final ProductsCollectionModel products = objectMapper.readValue(
            new URL("file:/Users/estankevich/Documents/work/warehouse-inventory/inventory-data-uploader/src/main/resources/products.json"),
            ProductsCollectionModel.class
        );
        InventoryServiceGrpc.InventoryServiceStub client = InventoryServiceGrpc.newStub(
            ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build()
        );
        CountDownLatch finishedLatch = new CountDownLatch(1);
        final StreamObserver<UpdateProductCommand> productsUploadStream = client
            .updateProducts(new UploadProductResultReporter(finishedLatch));

        for (ProductModel product : products.getProducts()) {
            final UpdateProductCommand.Builder builder = UpdateProductCommand.newBuilder()
                .setId(product.getId())
                .setPrice(product.getPrice())
                .setName(product.getName());
            for (ProductArticleModel article : product.getArticles()) {
                builder.addProductArticles(
                    ProductArticleLink.newBuilder()
                        .setArticleId(article.getId())
                        .setQuantity(article.getQuantity())
                        .build()
                );
            }
            productsUploadStream.onNext(builder.build());
        }
        productsUploadStream.onCompleted();
        finishedLatch.await();
    }

}
