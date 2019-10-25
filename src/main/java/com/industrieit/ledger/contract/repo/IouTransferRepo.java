package com.industrieit.ledger.contract.repo;

import com.industrieit.ledger.contract.entity.IouTransfer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
public interface IouTransferRepo extends CrudRepository<IouTransfer, String> {
}
