package ecomm.payments.service;

import ecomm.payments.data.PaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import payments.Payments;

import java.util.Date;

@Service
@Transactional
public class PaymentsService {

    @Autowired
    PaymentsRepository transactionRepository;

    public Iterable<Payments> fromTimestamp_endTimestamp(Long fromTimestamp, Long endTimestamp){
        return transactionRepository.findByCreationDateTimeBetween(fromTimestamp,endTimestamp);
    }
    public Payments addPayments(Payments payments){
        payments.setCreationDateTime(System.currentTimeMillis() / 1000L);
        return transactionRepository.save(payments);
    }

}
