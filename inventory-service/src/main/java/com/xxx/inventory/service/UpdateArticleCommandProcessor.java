package com.xxx.inventory.service;

import com.xxx.inventory.api.UpdateArticleCommand;
import com.xxx.inventory.api.UpdateArticleResult;
import com.xxx.inventory.domain.ArticleCommand;
import com.xxx.inventory.domain.InventoryException.WrongStockQuantityException;
import com.xxx.inventory.domain.InventoryService;
import io.grpc.stub.StreamObserver;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class UpdateArticleCommandProcessor implements StreamObserver<UpdateArticleCommand> {

    private final InventoryService inventoryService;
    private final StreamObserver<UpdateArticleResult> responseObserver;

    public UpdateArticleCommandProcessor(
        InventoryService inventoryService,
        StreamObserver<UpdateArticleResult> responseObserver) {

        this.inventoryService = inventoryService;
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(UpdateArticleCommand command) {
        try {
            inventoryService.changeArticle(new ArticleCommand.CreateOrUpdateArticle(
                command.getId(),
                command.getName(),
                command.getAvailable()
            ));
            responseObserver.onNext(UpdateArticleResult.newBuilder()
                .setId(command.getId())
                .setStatus(UpdateArticleResult.Status.SUCCESS).build());
        } catch (WrongStockQuantityException e) {
            responseObserver.onNext(UpdateArticleResult.newBuilder()
                .setId(command.getId())
                .setStatus(UpdateArticleResult.Status.WRONG_STOCK_QUANTITY).build());
        }
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
