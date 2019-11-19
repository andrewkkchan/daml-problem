package com.industrieit.ledger.contract.service;

import com.daml.ledger.javaapi.DamlLedgerClient;
import com.daml.ledger.javaapi.LedgerClient;
import com.daml.ledger.javaapi.PackageClient;
import com.daml.ledger.javaapi.data.*;
import com.digitalasset.daml_lf.DamlLf;
import com.digitalasset.daml_lf.DamlLf1;
import com.digitalasset.ledger.api.v1.PackageServiceOuterClass;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.industrieit.ledger.contract.entity.Iou;
import com.industrieit.ledger.contract.entity.IouTransfer;
import com.industrieit.ledger.contract.repo.IouRepo;
import com.industrieit.ledger.contract.repo.IouTransferRepo;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;

@Service
public class LedgerService {
    private final Logger logger;
    private DamlLedgerClient client;
    private String packageId;
    public static final String APP_NAME = "IouApp";
    public static final String MODULE_NAME = "Iou";
    private final IouRepo iouRepo;
    private final IouTransferRepo iouTransferRepo;

    public LedgerService(Logger logger, IouRepo iouRepo, IouTransferRepo iouTransferRepo) {
        this.logger = logger;
        this.iouRepo = iouRepo;
        this.iouTransferRepo = iouTransferRepo;
    }

    public String getPackageId() {
        return packageId;
    }

    @PostConstruct
    public void postConstruct() {
        client = DamlLedgerClient.forHostWithLedgerIdDiscovery("localhost", 6865, Optional.empty());
        client.connect();
        String ledgerId = client.getLedgerId();
        logger.info("ledger-id: {}", ledgerId);
        packageId = detectIouPackageId(client, MODULE_NAME);
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("Alice", NoFilter.instance);
        filterMap.put("Bob", NoFilter.instance);
        Disposable ignore = client.getTransactionsClient().getTransactions(LedgerOffset.LedgerEnd.getInstance(),
                new FiltersByParty(filterMap), true)
                .forEach(t -> {
                    for (Event event : t.getEvents()) {
                        if (event instanceof CreatedEvent) {
                            CreatedEvent createdEvent = (CreatedEvent) event;
                            if (createdEvent.getTemplateId().getEntityName().equals(IouService.ENTITY_NAME)) {
                                Optional<Iou> iou = iouRepo.findById(t.getWorkflowId());
                                if (iou.isPresent()) {
                                    iou.get().setContractId(event.getContractId());
                                    iouRepo.save(iou.get());
                                }
                            }
                            if (createdEvent.getTemplateId().getEntityName().equals(IouTransferService.ENTITY_NAME)) {
                                Optional<IouTransfer> iouTransfer = iouTransferRepo.findById(t.getWorkflowId());
                                if (iouTransfer.isPresent()) {
                                    iouTransfer.get().setContractId(event.getContractId());
                                    iouTransferRepo.save(iouTransfer.get());
                                }
                            }
                            logger.info("Created Event: {}", createdEvent);

                        } else if (event instanceof ArchivedEvent) {
                            logger.info("Archived Event: {}", event);


                        } else if (event instanceof ExercisedEvent) {
                            logger.info("Exercised Event: {}", event);
                        }
                    }
                });
    }

    public TransactionFilter filterFor(Identifier templateId, String party) {
        InclusiveFilter inclusiveFilter = new InclusiveFilter(Collections.singleton(templateId));
        Map<String, Filter> filter = Collections.singletonMap(party, inclusiveFilter);
        return new FiltersByParty(filter);
    }

    /**
     * Inspects all DAML packages that are registered on the ledger and returns the id of the package that contains the PingPong module.
     * This is useful during development when the DAML model changes a lot, so that the package id doesn't need to be updated manually
     * after each change.
     *
     * @param client the initialized client object
     * @return the package id of the example DAML module
     */
    private String detectIouPackageId(LedgerClient client, String moduleName) {
        PackageClient packageService = client.getPackageClient();

        // fetch a list of all package ids available on the ledger
        Flowable<String> packagesIds = packageService.listPackages();

        // fetch all packages and find the package that contains the PingPong module
        String packageId = packagesIds
                .flatMap(p -> packageService.getPackage(p).toFlowable())
                .filter(getPackageResponse -> containsModule(getPackageResponse, moduleName))
                .map(PackageServiceOuterClass.GetPackageResponse::getHash)
                .firstElement().blockingGet();

        if (packageId == null) {
            // No package on the ledger contained the PingPong module
            throw new RuntimeException("Module Iou is not available on the ledger");
        }
        return packageId;
    }

    private boolean containsModule(PackageServiceOuterClass.GetPackageResponse getPackageResponse, String moduleName) {
        try {
            // parse the archive payload
            DamlLf.ArchivePayload payload = DamlLf.ArchivePayload.parseFrom(getPackageResponse.getArchivePayload());
            // get the DAML LF package
            DamlLf1.Package lfPackage = payload.getDamlLf1();
            // check if the PingPong module is in the current package package
            Optional<DamlLf1.Module> iouModule = lfPackage.getModulesList().stream()
                    .filter(m -> m.getName().getSegmentsList().contains(moduleName)).findFirst();

            if (iouModule.isPresent())
                return true;

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return false;
    }

    public Empty submit(String workflowId, String party, Command c) {
        Empty empty = client.getCommandSubmissionClient().submit(
                workflowId,
                APP_NAME,
                UUID.randomUUID().toString(),
                party,
                Timestamp.newBuilder().setSeconds(Instant.EPOCH.toEpochMilli() / 1000).build(),
                Timestamp.newBuilder().setSeconds(Instant.EPOCH.plusSeconds(10).toEpochMilli() / 1000).build(),
                Collections.singletonList(c))
                .blockingGet();
        return empty;


    }

    public Flowable<GetActiveContractsResponse> getActiveContracts(TransactionFilter iouFilter) {
        return client.getActiveContractSetClient().getActiveContracts(iouFilter, true);
    }

}
