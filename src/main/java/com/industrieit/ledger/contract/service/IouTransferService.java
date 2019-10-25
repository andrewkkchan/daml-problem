package com.industrieit.ledger.contract.service;

import com.daml.ledger.javaapi.data.*;
import com.google.protobuf.Empty;
import com.industrieit.ledger.contract.entity.Iou;
import com.industrieit.ledger.contract.entity.IouTransfer;
import com.industrieit.ledger.contract.repo.IouRepo;
import com.industrieit.ledger.security.consumer.service.ConsumerTokenService;
import io.reactivex.disposables.CompositeDisposable;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class IouTransferService {
    public static final String ENTITY_NAME = "IouTransfer";
    private static final String ACCEPT_ENTITY = "IouTransfer_Accept";
    private static final String REJECT_ENTITY = "IouTransfer_Reject";
    private static final String CANCEL_ENTITY = "IouTransfer_Cancel";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Logger logger;
    private final LedgerService ledgerService;
    private final ConsumerTokenService consumerTokenService;
    private final IouRepo iouRepo;

    public IouTransferService(Logger logger, LedgerService ledgerService,
                              ConsumerTokenService consumerTokenService, IouRepo iouRepo) {
        this.logger = logger;
        this.ledgerService = ledgerService;
        this.consumerTokenService = consumerTokenService;
        this.iouRepo = iouRepo;
    }

    @PreDestroy
    public void preDestory() {
        compositeDisposable.clear();
    }

    public List<IouTransfer> getTransfers() {
        List<IouTransfer> iouTransfers = new ArrayList<>();
        Identifier iouIdentifier = new Identifier(ledgerService.getPackageId(), LedgerService.MODULE_NAME, ENTITY_NAME);
        TransactionFilter iouFilter = ledgerService.filterFor(iouIdentifier, consumerTokenService.getCurrentUserName());
        AtomicReference<LedgerOffset> acsOffset = new AtomicReference<>(LedgerOffset.LedgerBegin.getInstance());
        ledgerService.getActiveContracts(iouFilter)
                .blockingForEach(response -> {
                    acsOffset.set(new LedgerOffset.Absolute(response.getOffset()));
                    for (CreatedEvent event : response.getCreatedEvents()) {
                        IouTransfer iouTransfer = IouTransfer.fromRecord(event.getArguments());
                        iouTransfer.setId(event.getContractId());
                        iouTransfers.add(iouTransfer);
                    }
                });
        return iouTransfers;
    }

    public IouTransfer accept(IouTransfer iouTransfer) {
        Identifier templateId = new Identifier(ledgerService.getPackageId(), LedgerService.MODULE_NAME, ENTITY_NAME);
        Identifier choiceId = new Identifier(ledgerService.getPackageId(), LedgerService.MODULE_NAME, ACCEPT_ENTITY);
        String workflowId = UUID.randomUUID().toString();
        Iou resultingIou = new Iou();
        resultingIou.setId(workflowId);
        resultingIou.setAmount(iouTransfer.getIou().getAmount());
        resultingIou.setCurrency(iouTransfer.getIou().getCurrency());
        resultingIou.setIssuer(iouTransfer.getIou().getIssuer());
        resultingIou.setOwner(iouTransfer.getNewOwner());
        iouRepo.save(resultingIou);
        Empty submit = ledgerService.submit(workflowId,
                consumerTokenService.getCurrentUserName(),
                new ExerciseCommand(
                        templateId,
                        iouTransfer.getId(),
                        ACCEPT_ENTITY,
                        new Record(choiceId)
                ));
        return iouTransfer;
    }
}
