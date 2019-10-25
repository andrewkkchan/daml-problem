package com.industrieit.ledger.contract.repo;

import com.industrieit.ledger.contract.entity.Iou;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IouRepo extends CrudRepository<Iou, String> {
    Optional<Iou> findByContractId(String s);
}
