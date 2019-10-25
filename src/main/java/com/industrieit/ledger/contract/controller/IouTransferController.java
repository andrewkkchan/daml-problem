package com.industrieit.ledger.contract.controller;

import com.industrieit.ledger.contract.entity.IouTransfer;
import com.industrieit.ledger.contract.service.IouTransferService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transfer")
public class IouTransferController {
    private final IouTransferService iouTransferService;

    public IouTransferController(IouTransferService iouTransferService) {
        this.iouTransferService = iouTransferService;
    }

    @GetMapping(value = "/",
            produces = {"application/json"},
            consumes = {"application/json"})
    @ResponseBody
    public List<IouTransfer> getIouTransfers() {
        return iouTransferService.getTransfers();
    }

    @PostMapping(value = "/accept",
            produces = {"application/json"},
            consumes = {"application/json"})
    @ResponseBody
    public IouTransfer accept(@RequestBody IouTransfer iouTransfer) {
        return iouTransferService.accept(iouTransfer);
    }

}
