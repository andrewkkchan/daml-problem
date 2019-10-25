package com.industrieit.ledger.contract.service;

import com.daml.ledger.javaapi.data.*;
import com.industrieit.ledger.contract.entity.Iou;
import com.industrieit.ledger.contract.entity.IouTransfer;
import com.industrieit.ledger.contract.model.TransferRequest;
import com.industrieit.ledger.contract.repo.IouRepo;
import com.industrieit.ledger.contract.repo.IouTransferRepo;
import com.industrieit.ledger.security.consumer.service.ConsumerTokenService;
import io.reactivex.disposables.CompositeDisposable;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class IouService {
    public static final String ENTITY_NAME = "Iou";
    public static final String TRANSFER_ENTITY_NAME = "Iou_Transfer";
    private final IouRepo iouRepo;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Logger logger;
    private final LedgerService ledgerService;
    private final ConsumerTokenService consumerTokenService;
    private final IouTransferRepo iouTransferRepo;


    public IouService(IouRepo iouRepo, Logger logger, LedgerService ledgerService,
                      ConsumerTokenService consumerTokenService, IouTransferRepo iouTransferRepo) {
        this.iouRepo = iouRepo;
        this.logger = logger;
        this.ledgerService = ledgerService;
        this.consumerTokenService = consumerTokenService;
        this.iouTransferRepo = iouTransferRepo;
    }


    @PreDestroy
    public void preDestory() {
        compositeDisposable.clear();
    }

    public Optional<Iou> getIoU(String id) {
        return null;
    }

    public Iou createIou(Iou iou) {
        Identifier iouIdentifier = new Identifier(ledgerService.getPackageId(), LedgerService.MODULE_NAME, ENTITY_NAME);
        String workflowId = UUID.randomUUID().toString();
        iou.setId(workflowId);
        iouRepo.save(iou);
        ledgerService.submit(workflowId, consumerTokenService.getCurrentUserName(), iou.createCommand(iouIdentifier));
        return iou;
    }

    public List<Iou> getIoUs() {
        List<Iou> ious = new ArrayList<>();
        Identifier iouIdentifier = new Identifier(ledgerService.getPackageId(), LedgerService.MODULE_NAME, ENTITY_NAME);
        TransactionFilter iouFilter = ledgerService.filterFor(iouIdentifier, consumerTokenService.getCurrentUserName());
        AtomicReference<LedgerOffset> acsOffset = new AtomicReference<>(LedgerOffset.LedgerBegin.getInstance());
        ledgerService.getActiveContracts(iouFilter)
                .blockingForEach(response -> {
                    acsOffset.set(new LedgerOffset.Absolute(response.getOffset()));
                    for (CreatedEvent event : response.getCreatedEvents()) {
                        Iou iou = Iou.fromRecord(event.getArguments());
                        iou.setId(event.getContractId());
                        iou.setContractId(event.getContractId());
                        ious.add(iou);
                    }
                });
        return ious;
    }


    public IouTransfer transfer(TransferRequest transferRequest) {
        Identifier templateId = new Identifier(ledgerService.getPackageId(), LedgerService.MODULE_NAME, ENTITY_NAME);
        Identifier choiceId = new Identifier(ledgerService.getPackageId(), LedgerService.MODULE_NAME, TRANSFER_ENTITY_NAME);
        String workflowId = UUID.randomUUID().toString();
        Optional<Iou> iou = iouRepo.findByContractId(transferRequest.getId());
        IouTransfer iouTransfer = new IouTransfer();
        iouTransfer.setId(workflowId);
        iouTransfer.setNewOwner(transferRequest.getNewOwner());
        if (iou.isPresent()) {
            iouTransfer.setIou(iou.get());
            iouTransferRepo.save(iouTransfer);
        }
        ledgerService.submit(workflowId,
                consumerTokenService.getCurrentUserName(),
                new ExerciseCommand(
                        templateId,
                        transferRequest.getId(),
                        TRANSFER_ENTITY_NAME,
                        new Record(choiceId, new Record.Field("newOwner", new Party(transferRequest.getNewOwner())))
                ));
        return iouTransfer;

    }
}

