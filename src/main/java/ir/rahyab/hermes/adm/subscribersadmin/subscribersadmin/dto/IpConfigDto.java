package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author tahbaz
 */
public class IpConfigDto implements Serializable {
    private static final long serialVersionUID = 3900160117050160356L;

    private int status;

    private String[] deny;

    private String message;

    private String[] allow;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String[] getDeny() {
        return deny;
    }

    public void setDeny(String[] deny) {
        this.deny = deny;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getAllow() {
        return allow;
    }

    public void setAllow(String[] allow) {
        this.allow = allow;
    }
}
