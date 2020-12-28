package ecomm.payments.controller;

import com.google.gson.Gson;
import ecomm.payments.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
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


    @Value("${port-ping}")
    private static String portPing;

    @Value("${host-ping}")
    private static String hostPing;

    @Value("${my-paypal-account}")
    String myAccount;

    private static String endpoint="/ping";

    private static String uri="http://"+hostPing+":"+portPing+endpoint;

    static final int timer=1000;

    @Autowired
    PaymentsService paymentsService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String msg) {
        kafkaTemplate.send("orders", msg);
    }
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
            sendMessage(new Gson().toJson(payments));
            System.out.println((payments.toString()));
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

    @Scheduled(fixedRate = timer)
    public void heartbeat() throws Exception {
        String answer = new RestTemplate().postForObject("http://payments:2222/ping","up", String.class);
        System.out.println(answer);

    }
    private Boolean checkEmail(String email){
        return email.replace("\"","").equals(myAccount.replace("\"",""));
    }

}
