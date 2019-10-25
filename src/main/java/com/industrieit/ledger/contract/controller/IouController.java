package com.industrieit.ledger.contract.controller;

import com.industrieit.ledger.contract.entity.Iou;
import com.industrieit.ledger.contract.entity.IouTransfer;
import com.industrieit.ledger.contract.model.TransferRequest;
import com.industrieit.ledger.contract.service.IouService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/iou")
public class IouController {
    private final IouService iouService;

    public IouController(IouService iouService) {
        this.iouService = iouService;
    }

    @PutMapping(value = "/",
            produces = {"application/json"},
            consumes = {"application/json"})
    @ResponseBody
    public Iou createIou(@RequestBody Iou iou) {
        return iouService.createIou(iou);
    }

    @GetMapping(value = "/{id}",
            produces = {"application/json"},
            consumes = {"application/json"})
    @ResponseBody
    public Iou getIoU(@PathVariable String id) {
        return iouService.getIoU(id).orElse(null);
    }

    @GetMapping(value = "/",
            produces = {"application/json"},
            consumes = {"application/json"})
    @ResponseBody
    public List<Iou> getIoUs() {
        return iouService.getIoUs();
    }

    @PostMapping(value = "/transfer",
            produces = {"application/json"},
            consumes = {"application/json"})
    @ResponseBody
    public IouTransfer transferIou(@RequestBody TransferRequest transferRequest) {
        return iouService.transfer(transferRequest);
    }


}
