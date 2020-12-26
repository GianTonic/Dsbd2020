package ecomm.payments.controller;

import ecomm.payments.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import payments.Payments;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Optional;

@Controller

public class PaymentsController {
    private static String EXAMPLE="http://localhost:2222";
    @Value("${MY_PAYPAL_ACCOUNT}")
    String myAccount;

    @Autowired
    PaymentsService paymentsService;
    @PostMapping(path="/paypal")  //this url should map which you configured in step 5
    public @ResponseBody void success(ModelMap modelMap, HttpServletRequest httpServletRequest, Model uiModel) throws Exception{
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {

            String paramName = parameterNames.nextElement();
            System.out.println("paramName : "+paramName);
            String[] paramValues = httpServletRequest.getParameterValues(paramName);

            for (int i = 0; i < paramValues.length; i++) {
                String paramValue = paramValues[i];
                System.out.println("paramName : " + paramName+ ",paramValue : " + paramValue);
                //your logic goes here

            }
        }
    return;
    }

    @PostMapping(path="/ipn")
    public @ResponseBody void ipn(@RequestBody Payments payments){
        Boolean checkmail=this.checkEmail(payments.getBusiness());
        if(checkmail) {
            paymentsService.addPayments(payments);
        }
        return;
    }
    @GetMapping(path="/all")
    public @ResponseBody
    Optional<Iterable<Payments>> getAll(@RequestHeader("X-User-ID") Integer id){
        Optional<Iterable<Payments>> payments = null;
        if(id==0){
            payments=Optional.ofNullable(paymentsService.getAll());
        }
        return payments;
    }
    @PostMapping(path="/ping")
    public @ResponseBody void ping(@RequestBody String example) throws Exception {


    }
    @Scheduled(fixedRate = 1000)
    public void heartbeat() throws Exception {
        new RestTemplate().postForObject(EXAMPLE + "/ping","ok", String.class);

    }
    private Boolean checkEmail(String email){
        return email.replace("\"","").equals(myAccount.replace("\"",""));
    }

}
