package com.xxx.inventory.service;

import com.xxx.inventory.api.ProductArticleLink;
import com.xxx.inventory.api.UpdateProductCommand;
import com.xxx.inventory.api.UpdateProductResult;
import com.xxx.inventory.domain.InventoryService;
import com.xxx.inventory.domain.ProductCommand.CreateOrUpdateProduct;
import io.grpc.stub.StreamObserver;

import java.util.stream.Collectors;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class UpdateProductCommandProcessor implements StreamObserver<UpdateProductCommand> {

    private final InventoryService inventoryService;
    private final StreamObserver<UpdateProductResult> responseObserver;

    UpdateProductCommandProcessor(
        InventoryService inventoryService,
        StreamObserver<UpdateProductResult> responseObserver) {

        this.inventoryService = inventoryService;
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(UpdateProductCommand command) {
        inventoryService.changeProduct(new CreateOrUpdateProduct(
            command.getId(),
            command.getName(),
            command.getPrice(),
            command.getProductArticlesList()
                .stream()
                .collect(Collectors.toMap(ProductArticleLink::getArticleId, ProductArticleLink::getQuantity))
        ));
        responseObserver.onNext(UpdateProductResult.newBuilder()
            .setId(command.getId())
            .setStatus(UpdateProductResult.Status.SUCCESS).build());
    }

    @Override
    public void onError(Throwable t) {
        responseObserver.onError(t);
    }

    @Override
    public void onCompleted() {
        responseObserver.onCompleted();
    }
}
