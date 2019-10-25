package com.industrieit.ledger.contract.model;

public class TransferRequest {
    private String newOwner;
    private String id;

    public String getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(String newOwner) {
        this.newOwner = newOwner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
