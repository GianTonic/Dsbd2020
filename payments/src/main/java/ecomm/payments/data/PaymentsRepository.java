package ecomm.payments.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import payments.Payments;

public interface PaymentsRepository extends MongoRepository<Payments, String> {

    //
}
