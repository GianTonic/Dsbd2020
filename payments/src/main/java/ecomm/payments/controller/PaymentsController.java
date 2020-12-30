package ecomm.payments.controller;

import com.google.gson.Gson;
import ecomm.payments.data.HeartBeat;
import ecomm.payments.data.LoggingError;
import ecomm.payments.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import payments.Payments;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PaymentsController {

    @Value("${my-paypal-account}")
    String myAccount;

    @Value("${host-ping}")
    String host;


    @Autowired
    PaymentsService paymentsService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic,String key ,String msg) {
        kafkaTemplate.send(topic,key,msg);
    }

    @PostMapping(path="/paypal")  //this url should map which you configured in step 5
    public @ResponseBody void success(HttpServletRequest httpServletRequest) throws Exception{

        Map<String,String[]> params =httpServletRequest.getParameterMap();
        String result = params.entrySet().stream()
            .map(e -> e.getKey().toString() + "=" + Arrays.stream(e.getValue()).findFirst().get().toString())
            .collect(Collectors.joining("&"));
        String uriTest="https://ipnpb.sandbox.paypal.com/cgi-bin/webscr?cmd=_notify-validate&"+result;
        RestTemplate restTemplate=new RestTemplate();
        ResponseEntity answer=restTemplate.postForEntity(uriTest,null,String.class);
        System.out.println(answer.getBody());
    return;
    }

    @PostMapping(path="/ipn")
    public @ResponseBody void ipn(@RequestParam Payments payments){
        Boolean checkmail=this.checkEmail(payments.getBusiness());
        if(checkmail) {
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
    public void heartbeat() throws Exception {

        HeartBeat heartbeat=new HeartBeat();
        heartbeat.setService("payments");
        heartbeat.setServiceStatus("up");
        heartbeat.setDbStatus("up");
        RestTemplate restTemplate=new RestTemplate();

        try {
            restTemplate.postForEntity(host, heartbeat, String.class);
        }

        catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
                // handle 5xx errors
                // raw http status code e.g `500'
                LoggingError loggingError=new LoggingError();
                loggingError.setTimestamp(System.currentTimeMillis() / 1000L);
                loggingError.setSourceIp(Inet4Address.getLocalHost().getHostAddress());
                loggingError.setService("payments");
                loggingError.setRequest(this.host);
                StackTraceElement[] stack = ex.getStackTrace();
                String exception = "";
                for (StackTraceElement s : stack) {
                    exception = exception + s.toString() + "\n\t\t";
                }
                loggingError.setError(exception);
                sendMessage("logging","http_errors",new Gson().toJson(loggingError));

            } else if (ex.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
                // handle 4xx errors
                // raw http status code e.g `404`
                LoggingError loggingError=new LoggingError();
                loggingError.setTimestamp(System.currentTimeMillis() / 1000L);
                loggingError.setSourceIp(Inet4Address.getLocalHost().getHostAddress());
                loggingError.setService("payments");
                loggingError.setRequest(this.host);
                loggingError.setError(ex.getStatusCode().toString());
                sendMessage("logging","http_errors",new Gson().toJson(loggingError));

            }
        }

    }

    private Boolean checkEmail(String email){
        return email.replace("\"","").equals(myAccount.replace("\"",""));
    }


}
