package ecomm.payments.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Value("${uri}")
    private String uri;

    @Value("${user}")
    private String user;

    @Value("${password}")
    private String password;

    @Value("${host}")
    private String host;

    @Value("${portMongo}")
    private String port;

    @Value("${db}")
    private String db;

    @Value("${authdb}")
    private String authdb;

    @Bean
    public MongoClient mongo() {
        //mongodb://root:toor@mongo:27017/test?authSource=admin
        ConnectionString connectionString = new ConnectionString(uri+user+":"+password+"@"+host+":"+port+"/"+db+"?authSource="+authdb);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
        return MongoClients.create(mongoClientSettings);
    }

}



