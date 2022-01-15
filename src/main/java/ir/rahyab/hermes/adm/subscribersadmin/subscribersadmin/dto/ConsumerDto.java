package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto;

import java.io.Serializable;

/**
 * @author tahbaz
 */
public class ConsumerDto implements Serializable {
    private static final long serialVersionUID = 3664059100062408271L;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
