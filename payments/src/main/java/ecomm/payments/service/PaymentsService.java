package ecomm.payments.service;

import ecomm.payments.data.PaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import payments.Payments;

import java.util.Date;

@Service
public class PaymentsService {
    @Autowired
    PaymentsRepository transactionRepository;

    public Iterable<Payments> getAll(){
        return transactionRepository.findAll();
    }
    public Payments addPayments(Payments payments){
        payments.setCreationDateTime(System.currentTimeMillis() / 1000L);
        return transactionRepository.save(payments);
    }
}
