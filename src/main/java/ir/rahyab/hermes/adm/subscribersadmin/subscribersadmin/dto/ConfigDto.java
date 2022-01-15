package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto;

import java.io.Serializable;

/**
 * @author tahbaz
 */
public class ConfigDto implements Serializable {
    private static final long serialVersionUID = -3969833558092698270L;

    //Ip config
    private int status;

    private String[] deny;

    private String message;

    private String[] allow;

    //Rate config
    private int redis_timeout;

    private boolean redis_ssl;

    private boolean redis_ssl_verify;

    private String redis_server_name;

    private int redis_database;

    private String redis_host;

    private String limit_by;

    private String redis_password;

    private int second;

    private int minute;

    private String policy;

    private int day;

    private int month;

    private int year;

    private boolean hide_client_headers;

    private String path;

    private int hour;

    private int redis_port;

    private String header_name;

    private boolean fault_tolerant;

    private IpConfigDto ipConfigDto;

    private RateConfigDto rateConfigDto;

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

    public int getRedis_timeout() {
        return redis_timeout;
    }

    public void setRedis_timeout(int redis_timeout) {
        this.redis_timeout = redis_timeout;
    }

    public boolean isRedis_ssl() {
        return redis_ssl;
    }

    public void setRedis_ssl(boolean redis_ssl) {
        this.redis_ssl = redis_ssl;
    }

    public boolean isRedis_ssl_verify() {
        return redis_ssl_verify;
    }

    public void setRedis_ssl_verify(boolean redis_ssl_verify) {
        this.redis_ssl_verify = redis_ssl_verify;
    }

    public String getRedis_server_name() {
        return redis_server_name;
    }

    public void setRedis_server_name(String redis_server_name) {
        this.redis_server_name = redis_server_name;
    }

    public int getRedis_database() {
        return redis_database;
    }

    public void setRedis_database(int redis_database) {
        this.redis_database = redis_database;
    }

    public String getRedis_host() {
        return redis_host;
    }

    public void setRedis_host(String redis_host) {
        this.redis_host = redis_host;
    }

    public String getLimit_by() {
        return limit_by;
    }

    public void setLimit_by(String limit_by) {
        this.limit_by = limit_by;
    }

    public String getRedis_password() {
        return redis_password;
    }

    public void setRedis_password(String redis_password) {
        this.redis_password = redis_password;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isHide_client_headers() {
        return hide_client_headers;
    }

    public void setHide_client_headers(boolean hide_client_headers) {
        this.hide_client_headers = hide_client_headers;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getRedis_port() {
        return redis_port;
    }

    public void setRedis_port(int redis_port) {
        this.redis_port = redis_port;
    }

    public String getHeader_name() {
        return header_name;
    }

    public void setHeader_name(String header_name) {
        this.header_name = header_name;
    }

    public boolean isFault_tolerant() {
        return fault_tolerant;
    }

    public void setFault_tolerant(boolean fault_tolerant) {
        this.fault_tolerant = fault_tolerant;
    }

    public IpConfigDto getIpConfigDto() {
        return ipConfigDto;
    }

    public void setIpConfigDto(IpConfigDto ipConfigDto) {
        this.ipConfigDto = ipConfigDto;
    }

    public RateConfigDto getRateConfigDto() {
        return rateConfigDto;
    }

    public void setRateConfigDto(RateConfigDto rateConfigDto) {
        this.rateConfigDto = rateConfigDto;
    }
}
