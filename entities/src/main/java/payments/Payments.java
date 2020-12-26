package payments;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.Date;
@Entity
public class Payments {
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private Integer invoice;
    private Integer item_id;
    private Float mc_gross;
    private String business;
    private Long creationDateTime;

    @Override
    public String toString() {
        return "Payments{" +
                "id='" + id + '\'' +
                ", invoice=" + invoice +
                ", item_id=" + item_id +
                ", mc_gross=" + mc_gross +
                ", business='" + business + '\'' +
                ", creationDateTime=" + creationDateTime +
                '}';
    }

    public Long getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Long creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getInvoice() {
        return invoice;
    }

    public void setInvoice(Integer invoice) {
        this.invoice = invoice;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public Float getMc_gross() {
        return mc_gross;
    }

    public void setMc_gross(Float mc_gross) {
        this.mc_gross = mc_gross;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }


}
