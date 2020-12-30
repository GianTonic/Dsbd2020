package ecomm.payments.controller;

import com.google.gson.Gson;
import ecomm.payments.data.HeartBeat;
import ecomm.payments.data.LoggingError;
import ecomm.payments.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import payments.Payments;
import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PaymentsController {

    @Value("${myPaypalAccount}")
    String myAccount;

    @Value("${hostPing}")
    String pingHost;

    @Value("${paypalHost}")
    String paypalHost;

    @Autowired
    PaymentsService paymentsService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic,String key ,String msg) {
        kafkaTemplate.send(topic,key,msg);
    }

    @PostMapping(path="/paypal")  //this url should map which you configured in step 5
    public @ResponseBody void success(HttpServletRequest httpServletRequest){

        Map<String,String[]> params =httpServletRequest.getParameterMap();
        Boolean checkPayments=checkIfVerified(params);
        Boolean checkMail=checkEmail(Arrays.stream(params.get("receiver_email")).findFirst().get());

        System.out.println(checkMail);
        System.out.println(checkPayments);
        return;
    }


    @PostMapping(path="/ipn")
    public @ResponseBody void ipn(@RequestParam Payments payments){
        Boolean checkMail=this.checkEmail(payments.getBusiness());
        if(checkMail) {
            sendMessage("orders","order_payd",new Gson().toJson(payments));
            System.out.println((payments.toString()));
            paymentsService.addPayments(payments);
        }
        return;
    }
    @GetMapping(path="/transactions")
    public @ResponseBody
    Optional<Iterable<Payments>> getAll(@RequestHeader("X-User-ID") Integer id, @RequestParam Long fromTimestamp,@RequestParam Long endTimestamp){
        Optional<Iterable<Payments>> payments = null;
        if(id==0){
            payments=Optional.ofNullable(paymentsService.fromTimestamp_endTimestamp(fromTimestamp,endTimestamp));
        }
        return payments;
    }
    
    @Scheduled(fixedDelayString = "${timer}")
    public void heartbeat() {

        HeartBeat heartbeat=new HeartBeat();
        heartbeat.setService("payments");
        heartbeat.setServiceStatus("up");
        heartbeat.setDbStatus("up");
        RestTemplate restTemplate=new RestTemplate();

        try {
            restTemplate.postForEntity(pingHost, heartbeat, String.class);
        }

        catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
                // handle 5xx errors
                // raw http status code e.g `500'
                setLogging500(ex,this.pingHost);

            } else if (ex.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
                // handle 4xx errors
                // raw http status code e.g `404`
                setLogging404(ex,this.pingHost);

            }
        }

    }

    private Boolean checkEmail(String email){
        return email.replace("\"","").equals(myAccount.replace("\"",""));
    }

    private Boolean checkIfVerified(Map<String,String[]> params)  {
        String result = params.entrySet().stream()
                .map(e -> e.getKey().toString() + "=" + Arrays.stream(e.getValue()).findFirst().get().toString())
                .collect(Collectors.joining("&"));
        String uriTest=paypalHost+result;
        RestTemplate restTemplate=new RestTemplate();
        Boolean verified=false;
        try {
            ResponseEntity answer = restTemplate.postForEntity(uriTest, null, String.class);

            if(answer.getBody().toString().equals("VERIFIED")){
                verified=true;
            }
            else{
                verified=false;
            }

        }
        catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
                // handle 5xx errors
                // raw http status code e.g `500'
                verified=false;
                setLogging500(ex,this.paypalHost);


            } else if (ex.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
                // handle 4xx errors
                // raw http status code e.g `404`
                verified=false;
                setLogging404(ex,this.paypalHost);

            }

        }
        catch (Exception e) {
            verified=false;
            setLogging500(e,this.paypalHost);
        }
        return verified;

    }

    private void setLogging500(Exception ex,String host)  {
        LoggingError loggingError = new LoggingError();
        loggingError.setTimestamp(System.currentTimeMillis() / 1000L);

        loggingError.setService("payments");
        loggingError.setRequest(host);
        StackTraceElement[] stack = ex.getStackTrace();
        String exception = "";
        for (StackTraceElement s : stack) {
            exception = exception + s.toString() + "\n\t\t";
        }
        loggingError.setError(exception);
        try {
            loggingError.setSourceIp(Inet4Address.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException unknownHostException){
            loggingError.setSourceIp("unknown");
        }
        finally {
            sendMessage("logging", "http_errors", new Gson().toJson(loggingError));

        }
    }

    private void setLogging404(HttpStatusCodeException ex,String host) {
        LoggingError loggingError = new LoggingError();
        loggingError.setTimestamp(System.currentTimeMillis() / 1000L);
        loggingError.setService("payments");
        loggingError.setRequest(host);
        loggingError.setError(ex.getStatusCode().toString());
        try {
            loggingError.setSourceIp(Inet4Address.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException HostException){
            loggingError.setSourceIp("unknown");
        }
        finally {
            sendMessage("logging", "http_errors", new Gson().toJson(loggingError));
        }
    }

}
