package payments;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

@Entity
public class Payments {
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private String kafkaMessage;
    private String ipnMessage;
    private Long creationDateTime;

    @Override
    public String toString() {
        return "Payments{" +
                "id='" + id + '\'' +
                ", kafkaMessage='" + kafkaMessage + '\'' +
                ", ipnMessage='" + ipnMessage + '\'' +
                ", creationDateTime=" + creationDateTime +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKafkaMessage() {
        return kafkaMessage;
    }

    public void setKafkaMessage(String kafkaMessage) {
        this.kafkaMessage = kafkaMessage;
    }

    public String getIpnMessage() {
        return ipnMessage;
    }

    public void setIpnMessage(String ipnMessage) {
        this.ipnMessage = ipnMessage;
    }

    public Long getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Long creationDateTime) {
        this.creationDateTime = creationDateTime;
    }


}
