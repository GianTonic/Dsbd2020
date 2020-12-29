package ecomm.payments.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import payments.Payments;

import java.util.List;

public interface PaymentsRepository extends MongoRepository<Payments, String> {
    List<Payments> findByCreationDateTimeBetween(Long fromTimestamp,Long endTimestamp);
    //
}
