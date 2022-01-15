package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author tahbaz
 */
public class JsonDto implements Serializable {
    private static final long serialVersionUID = -3019090893538778089L;
    private List<RestrictionsDto> data;

    private String next;

    private String offset;

    public List<RestrictionsDto> getData() {
        return data;
    }

    public void setData(List<RestrictionsDto> data) {
        this.data = data;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }
}
