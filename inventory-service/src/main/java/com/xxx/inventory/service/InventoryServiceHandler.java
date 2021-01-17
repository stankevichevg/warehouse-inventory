package com.xxx.inventory.service;

import com.google.inject.Inject;
import com.xxx.inventory.api.InventoryServiceGrpc;
import com.xxx.inventory.api.ListAllProductsCommand;
import com.xxx.inventory.api.ProductArticleLink;
import com.xxx.inventory.api.ProductView;
import com.xxx.inventory.api.SellProductCommand;
import com.xxx.inventory.api.SellProductResult;
import com.xxx.inventory.api.UpdateArticleCommand;
import com.xxx.inventory.api.UpdateArticleResult;
import com.xxx.inventory.api.UpdateProductCommand;
import com.xxx.inventory.api.UpdateProductResult;
import com.xxx.inventory.domain.InventoryException;
import com.xxx.inventory.domain.InventoryException.InsufficientArticlesInStockException;
import com.xxx.inventory.domain.InventoryException.ProductNotFoundException;
import com.xxx.inventory.domain.InventoryService;
import com.xxx.inventory.domain.Product;
import io.grpc.stub.StreamObserver;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class InventoryServiceHandler extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final InventoryService inventoryService;

    @Inject
    public InventoryServiceHandler(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void listProducts(ListAllProductsCommand request, StreamObserver<ProductView> responseObserver) {
        inventoryService.listProducts().map(this::mapToView).forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void sellProduct(SellProductCommand request, StreamObserver<SellProductResult> responseObserver) {
        try {
            inventoryService.sellProduct(request.getId());
            responseObserver.onNext(SellProductResult.newBuilder()
                .setStatus(SellProductResult.Status.SUCCESS).build());
        } catch (InsufficientArticlesInStockException e) {
            responseObserver.onNext(SellProductResult.newBuilder()
                .setStatus(SellProductResult.Status.NOT_ENOUGH_ARTICLES).build());
        } catch (ProductNotFoundException e) {
            responseObserver.onNext(SellProductResult.newBuilder()
                .setStatus(SellProductResult.Status.PRODUCT_NOT_FOUND).build());
        } catch (InventoryException e) {
            responseObserver.onNext(SellProductResult.newBuilder()
                .setStatus(SellProductResult.Status.UNRECOGNIZED).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<UpdateArticleCommand> updateArticles(StreamObserver<UpdateArticleResult> responseObserver) {
        return new UpdateArticleCommandProcessor(inventoryService, responseObserver);
    }

    @Override
    public StreamObserver<UpdateProductCommand> updateProducts(StreamObserver<UpdateProductResult> responseObserver) {
        return new UpdateProductCommandProcessor(inventoryService, responseObserver);
    }

    private ProductView mapToView(Product product) {
        final ProductView.Builder builder = ProductView.newBuilder()
            .setId(product.getId())
            .setName(product.getName())
            .setPrice(product.getPrice())
            .setQuantity(product.calculateQuantity());
        product.getArticles().forEach(productArticle ->
            builder.addProductArticles(ProductArticleLink.newBuilder()
                .setArticleId(productArticle.getArticle().getId())
                .setQuantity(productArticle.getQuantity())
                .build())
        );
        return builder.build();
    }

}
