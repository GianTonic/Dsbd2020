package ecomm.payments.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ecomm.payments.data.HeartBeat;
import ecomm.payments.data.LoggingError;
import ecomm.payments.data.OrderPaid;
import ecomm.payments.data.OrderPaidFailure;
import ecomm.payments.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import payments.Payments;
import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PaymentsController {

    //email beneficiario transazione paypal
    @Value("${myPaypalAccount}")
    String myAccount;
    //host e metodo fault detector
    @Value("${hostPing}")
    String pingHost;
    //uri sandbox per verifica transazione
    @Value("${paypalHost}")
    String paypalHost;
    //dependency injection
    @Autowired
    PaymentsService paymentsService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    //invio su kafka
    public void sendMessage(String topic,String key ,String msg) {
        kafkaTemplate.send(topic,key,msg);
    }
    //end-point per la ricezione della notifica di pagamento
    @PostMapping(path="/ipn")
    public @ResponseBody void ipn(HttpServletRequest httpServletRequest)  {
        //get parametri notifica paypal
        Map<String,String[]> map =httpServletRequest.getParameterMap();
        //encoding parametri notifica paypal
        String params=encoder(map);
        //verifica se la mail del beneficiario del pagamento corrisponde alla mail dell e-commerce
        Boolean checkMail=checkEmail(Arrays.stream(map.get("receiver_email")).findFirst().get());
        //verifica che la notifica provenga effettivamente da paypal
        Boolean checkPayments=checkIfVerified(params);
        if(!checkMail){
            //invio su kafka le informazioni relative al fallimento del controllo della mail
            OrderPaidFailure orderPaidFailure=new OrderPaidFailure();
            orderPaidFailure.setTimeStamp(System.currentTimeMillis() / 1000L);
            orderPaidFailure.setParams(map);
            sendMessage("logging","received_wrong_business_paypal_payment",new Gson().toJson(orderPaidFailure));
            return;
        }
        if(!checkPayments){
            //invio su kafka le informazioni relative al fallimento della verifica dell'host sorgente della notifica
            OrderPaidFailure orderPaidFailure=new OrderPaidFailure();
            orderPaidFailure.setTimeStamp(System.currentTimeMillis() / 1000L);
            orderPaidFailure.setParams(map);
            sendMessage("logging","bad_ipn_error",new Gson().toJson(orderPaidFailure));
            return;
        }
        OrderPaid orderPaid=new OrderPaid();
        orderPaid.setUserId(Arrays.stream(map.get("custom")).findFirst().get());
        orderPaid.setOrderId(Arrays.stream(map.get("item_number")).findFirst().get());
        orderPaid.setAmountPaid(Float.parseFloat(Arrays.stream(map.get("mc_gross")).findFirst().get()));
        //invio su kafka le informazioni relative al successo della transazione
        sendMessage("orders","order_paid",new Gson().toJson(orderPaid));
        Payments payments=new Payments();
        payments.setCreationDateTime(System.currentTimeMillis() / 1000L);
        payments.setIpnMessage(params);
        payments.setKafkaMessage(orderPaid.toString());
        //aggiungo sul db il pagamento effettuato
        paymentsService.addPayments(payments);
    }

    @GetMapping(path="/transactions")
    public @ResponseBody
    Optional<Iterable<Payments>> getAll(@RequestHeader("X-User-ID") Integer id, @RequestParam Long fromTimestamp,@RequestParam Long endTimestamp){
        Optional<Iterable<Payments>> payments = Optional.empty();
        //verifico che la richiesta proviene da un admin
        if(id==0){
            //faccio retrieve delle transazioni con TimeStamp tra from/end timestamp
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
            //faccio una post al fault-detector per comunicare lo stato del mio servizio e del mio db
            restTemplate.postForEntity(pingHost, heartbeat, String.class);
        }
        catch (HttpStatusCodeException ex) {
            //gestisco le eccezioni sollevate da un errore di tipo 5xx
            if (ex.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
                //invio su kafka le informazioni relative all'errore della richiesta http
                setLogging500(ex,this.pingHost);
                //gestisco le eccezioni sollevate da un errore di tipo 4xx
            } else if (ex.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
                //invio su kafka le informazioni relative all'errore della richiesta http
                setLogging400(ex,this.pingHost);
            }
        }
    }
    //verifico l'uguaglianza tra la mail del beneficiario della transazione e l'email dell'ecommerce
    private Boolean checkEmail(String email){
        return email.equals(myAccount);
    }
    //codifico i parametri ricevuti su /ipn
    private String encoder(Map<String,String[]> params){
        String result = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + Arrays.stream(e.getValue()).findFirst().get())
                .collect(Collectors.joining("&"));
        return result;
    }
    private Boolean checkIfVerified(String result)  {
        String uriTest=paypalHost+result;
        RestTemplate restTemplate=new RestTemplate();
        boolean verified=false;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            //effettuo una post all'endpoint della sandbox di paypal per verificare la veridicità della notifica ricevuta
            ResponseEntity<String> answer = restTemplate.postForEntity(uriTest, entity, String.class);

            if(Objects.equals(answer.getBody(), "VERIFIED")){
                verified=true;
            }
        }
        //intercetto errori di tipo 5xx e 4xx
        catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
                //invio info relative all'errore
                setLogging500(ex,this.paypalHost);
            } else if (ex.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
                //invio info relative all'errore
                setLogging400(ex,this.paypalHost);
            }
        }
        //intercetto eccezioni generiche, come il timeout dell'attesa della risposta
        catch (Exception e) {
            //invio info relative all'errore
            setLogging500(e,this.paypalHost);
        }
        return verified;
    }

    private void setLogging500(Exception ex,String host)  {
        LoggingError loggingError = new LoggingError();
        loggingError.setTimestamp(System.currentTimeMillis() / 1000L);

        loggingError.setService("payments");
        loggingError.setRequest(host);
        //codifico lo stacktrace in una stringa
        StackTraceElement[] stack = ex.getStackTrace();
        String exception = "";
        for (StackTraceElement s : stack) {
            exception = exception + s.toString() + "\n\t\t";
        }
        loggingError.setError(exception);
        try {
            //tentativo di ottenere l'indirizzo ip del richiedente
            loggingError.setSourceIp(Inet4Address.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException unknownHostException){
            loggingError.setSourceIp("unknown");
        }
        finally {
            sendMessage("logging", "http_errors", new Gson().toJson(loggingError));
        }
    }

    private void setLogging400(HttpStatusCodeException ex,String host) {
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
