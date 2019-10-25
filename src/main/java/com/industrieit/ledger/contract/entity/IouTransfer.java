package com.industrieit.ledger.contract.entity;

import com.daml.ledger.javaapi.data.Record;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "iou_transfer")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IouTransfer {
    @Id
    @Column(name = "id")
    private String id;
    private String newOwner;
    private String contractId;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name="iouId")
    private Iou iou;
    public static final int IOU = 0;
    public static final int NEW_OWNER = 1;

    public static IouTransfer fromRecord(Record record) {
        Record.Field iouField = record.getFields().get(IOU);
        Record iouRecord = iouField.getValue().asRecord().orElseThrow(() ->
                new IllegalStateException("New Owner should be of type Party, found "
                        + iouField.toString()));
        Iou iou = Iou.fromRecord(iouRecord);
        Record.Field newOwnerField = record.getFields().get(NEW_OWNER);
        String newOwner = newOwnerField.getValue().asParty().orElseThrow(() ->
                new IllegalStateException("New Owner should be of type Party, found "
                        + newOwnerField.toString())).getValue();
        return new IouTransfer(iou, newOwner);
    }

    public String getId() {
        return id;
    }

    public void setId(String contractId) {
        this.id = contractId;

    }

    public IouTransfer() {

    }

    public IouTransfer(Iou iou, String newOwner) {
        this.iou = iou;
        this.newOwner = newOwner;
    }

    public String getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(String newOwner) {
        this.newOwner = newOwner;
    }

    public Iou getIou() {
        return iou;
    }

    public void setIou(Iou iou) {
        this.iou = iou;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
}
