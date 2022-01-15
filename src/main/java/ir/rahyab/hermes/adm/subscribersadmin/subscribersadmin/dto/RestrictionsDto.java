package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto;

import java.io.Serializable;

/**
 * @author tahbaz
 */

public class RestrictionsDto implements Serializable {
    private static final long serialVersionUID = 6480252158642660355L;

    private String service;

    private String id;

    private String name;

    private boolean enabled;

    private long created_at;

    private ConfigDto config;

    private String[] tags;

    private String route;

    private String[] protocols;

    private ConsumerDto consumer;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String[] getProtocols() {
        return protocols;
    }

    public void setProtocols(String[] protocols) {
        this.protocols = protocols;
    }

    public ConsumerDto getConsumer() {
        return consumer;
    }

    public void setConsumer(ConsumerDto consumer) {
        this.consumer = consumer;
    }

    public ConfigDto getConfig() {
        return config;
    }

    public void setConfig(ConfigDto config) {
        this.config = config;
    }
}
