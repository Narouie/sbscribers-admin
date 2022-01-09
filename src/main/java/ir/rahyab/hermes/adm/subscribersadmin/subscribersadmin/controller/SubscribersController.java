package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto.AddingAuditTrailDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * The {@code SubscribersController} class provides web methods to create consumers,get list of consumers and basic Auth,key Auth
 * get list of credentials,acl,ip restrictions and get list of ip white list and rate limitting about {@code Consumer}
 *
 * Also, It includes saving the history of changes in the Audit Trail Management System.
 *
 * API specification in {@code SubscribersController} using OpenAPI.
 *
 * @author tahbaz
 */

@RestController
@Tag(name = "Subscribers", description = "Endpoints for managing subscribers")
public class SubscribersController {

    @Autowired
    RestTemplate restTemplate;

    @Value("${subscribersAdmin.consumers.url}")
    private String consumersUrl;

    @Value("${subscribersAdmin.consumers.basic-auth.url}")
    private String basicAuthUrl_c;

    @Value("${subscribersAdmin.usernameOrId.basic-auth.url}")
    private String basicAuthUrl_u;

    @Value("${subscribersAdmin.consumers.key-auth.url}")
    private String keyAuthUrl_c;

    @Value("${subscribersAdmin.usernameOrId.key-auth.url}")
    private String keyAuthUrl_u;

    @Value("${subscribersAdmin.acls.url}")
    private String aclsUrl;

    @Value("${subscribersAdmin.plugins.url}")
    private String plugins;

    @Value("${auditTrailManagementSystem.addAuditTrailDto.url}")
    private String addAuditTrailDtoUrl;

    @Value("${auditTrail.applicCd}")
    private String auditTrailApplicCd;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    /**
     * Creates a new consumer in kong database(postgres database).
     * Creates a new key in Redis database in the form of "ACT:" + username
     * Response status codes include 201,204,409,404,400,500
     * 201 status code:Created , 204 status code:Consumer does not create in database , 500 status code:Internal Server Error
     * 409 status code:Consumer created before , 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param username the {@code String} consumer or user of a service,The unique username of the Consumer. You must send either this field or custom_id with the request.
     * @param custom_id the {@code String} Field for storing an existing unique ID for the Consumer - useful for mapping Kong with users in your existing database. You must send either this field or username with the request.
     * @param refrence the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,204,409,404,400,500
     */
    @RequestMapping(value = {"/consumers"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Create consumer", description = "Returns created consumer", operationId = "createConsumer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "409", description = "Consumer created before"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> consumers(@RequestParam String username, @RequestParam String custom_id, @RequestParam String refrence, @RequestParam String description) {

        var operations = redisTemplate.opsForHash();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("id", UUID.randomUUID().toString());
        body.add("created_at", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        body.add("username", username);
        body.add("custom_id", custom_id);
        body.add("tags", "");

        try {
            if (Objects.nonNull(username) && !username.isEmpty()) {

                ResponseEntity<String> response = restTemplate.postForEntity(consumersUrl, new HttpEntity<>(body), String.class);
                String endUserId = "ACT:" + username;
                if (response.getStatusCodeValue() == 201) {

                    if (!redisTemplate.hasKey(endUserId)) {
                        operations.put(endUserId, "created_at", String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

                        ResponseEntity<String> audit = subscribersChanges(refrence, description, username + ":" + "create", "create_consumer");

                        return response;
                    }
                }

            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            exception.printStackTrace();
            if (exception.getStatusCode().value() == 409)
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    /**
     * Get consumer list, page by page.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param size the {@code Integer} size of each page.
     * @param offset the {@code String} start of point for next page.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/consumers"}, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get list of all consumers", description = "Returns list of all consumers", operationId = "getListAllConsumer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> consumers(@RequestParam(defaultValue = "3") Integer size, @RequestParam(required = false) String offset) {
        ResponseEntity<String> response = null;

        try {
            if (offset == null || offset.isEmpty()) {
                response = restTemplate.getForEntity(consumersUrl + "?size=" + size, String.class);
            } else
                response = restTemplate.getForEntity(consumersUrl + "?size=" + size + "&offset=" + offset, String.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Associate a basicAuth's credential to an existing Consumer object. A Consumer can have many credentials.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param consumer the {@code String} The id or username property of the Consumer entity to associate the credentials to.
     * @param username the {@code String} The username to use in the basic authentication credential.
     * @param password the {@code String} The password to use in the basic authentication credential.
     * @param refrence the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/consumers/{CONSUMER}/basic-auth"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Associate a credential to an existing Consumer object. A Consumer can have many credentials.", description = "Create basic authentication for existing consumer", operationId = "basic-auth_consumer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> basicAuth(@RequestParam String consumer, @RequestParam String username, @RequestParam String password, @RequestParam String refrence, @RequestParam String description) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("username", username);
        body.add("password", password);
        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(basicAuthUrl_c.replace("{CONSUMER}", consumer), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(refrence, description, consumer + ":" + "basicAuth", "create_basicAuth");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            exception.printStackTrace();
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Retrieve basicAuth credentials associated with a consumer, page by page.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param usernameOrId the {@code String} The username or id of the consumer whose credentials need to be listed.
     * @param size the {@code Integer} size of each page.
     * @param offset the {@code String} start of point for next page.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/consumers/{USERNAME_OR_ID}/basic-auth"}, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Retrieve basicAuth credentials associated with a consumer, page by page", description = "Returns list of all basicAuth associated with a consumer", operationId = "basic-auth_usernameOrId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> basicAuth(@RequestParam String usernameOrId, @RequestParam(defaultValue = "3") Integer size, @RequestParam(required = false) String offset) {
        ResponseEntity<String> response = null;

        try {
            if (offset == null || offset.isEmpty()) {
                response = restTemplate.getForEntity(basicAuthUrl_u.replace("{USERNAME_OR_ID}", usernameOrId) + "?size=" + size, String.class);
            } else
                response = restTemplate.getForEntity(basicAuthUrl_u.replace("{USERNAME_OR_ID}", usernameOrId) + "?size=" + size + "&offset=" + offset, String.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Associate a keyAuth's credential to an existing Consumer object.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param consumer the {@code String} The id or username property of the Consumer entity to associate the credentials to.
     * @param refrence the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/consumers/{consumer}/key-auth"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Associate a credential to an existing Consumer object", description = "Associate a keyAuth's credential to an existing Consumer object", operationId = "key-auth_consumer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> keyAuth(@RequestParam String consumer, @RequestParam String refrence, @RequestParam String description) {

        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(keyAuthUrl_c.replace("{consumer}", consumer), null, String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(refrence, description, consumer + ":" + "keyAuth", "create_keyAuth");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            exception.printStackTrace();
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Retrieve key Auth credentials associated with a consumer, page by page.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param usernameOrId the {@code String} The username or id of the consumer whose credentials need to be listed.
     * @param size the {@code Integer} size of each page.
     * @param offset the {@code String} start of point for next page.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/consumers/{usernameOrId}/key-auth"}, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get list of all credentials for username or id", description = "Returns list of all credentials for username or id", operationId = "key-auth_usernameOrId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> keyAuth(@RequestParam String usernameOrId, @RequestParam(defaultValue = "3") Integer size, @RequestParam(required = false) String offset) {
        ResponseEntity<String> response = null;

        try {
            if (offset == null || offset.isEmpty()) {
                response = restTemplate.getForEntity(keyAuthUrl_u.replace("{usernameOrId}", usernameOrId) + "?size=" + size, String.class);
            } else
                response = restTemplate.getForEntity(keyAuthUrl_u.replace("{usernameOrId}", usernameOrId) + "?size=" + size + "&offset=" + offset, String.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Restrict access to a Service or a Route by adding Consumers to allowed or denied lists using arbitrary ACL groups.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param consumer the {@code String} The username or id of the Consumer.
     * @param group the {@code String} The arbitrary group name to associate with the consumer.
     * @param refrence the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/consumers/{CONSUMER}/acls"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Restrict access to a Service or a Route by adding Consumers to allowed or denied lists using arbitrary ACL groups.", description = "Access control list", operationId = "acl")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> acls(@RequestParam String consumer, @RequestParam String group, @RequestParam String refrence, @RequestParam String description) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("group", group);
        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(aclsUrl.replace("{CONSUMER}", consumer), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(refrence, description, consumer + ":" + "acl", "create_acl");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            exception.printStackTrace();
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Restrict access to a Service or a Route by either allowing or denying IP addresses.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param consumer the {@code String} The username or id of the Consumer.
     * @param ipWhiteList the {@code String} The ip white list or allowed ip's.
     * @param refrence the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/consumers/{CONSUMER}/plugins"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Restrict access to a Service or a Route by either allowing or denying IP addresses", description = "Restrict consumer by ip addresses", operationId = "ip-restriction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> plugins(@RequestParam String consumer, @RequestParam String ipWhiteList, @RequestParam String refrence, @RequestParam String description) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("name", "ip-restriction");
        String[] ips = ipWhiteList.split(",");
        for (String ip : ips)
            body.add("config.allow", ip);
        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(plugins.replace("{CONSUMER}", consumer), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(refrence, description, consumer + ":" + ipWhiteList, "create_ipRestriction");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            exception.printStackTrace();
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Retrieve allowed ip list for a consumer, page by page.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param usernameOrId the {@code String} The username or id of the consumer.
     * @param size the {@code Integer} size of each page.
     * @param offset the {@code String} start of point for next page.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/consumers/{CONSUMER}/plugins"}, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get list of all ip white list for consumer", description = "Returns all allowed ip list for consumer", operationId = "ip-restrictions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> plugins(@RequestParam String usernameOrId, @RequestParam(defaultValue = "3") Integer size, @RequestParam(required = false) String offset) {
        ResponseEntity<String> response = null;

        try {
            if (offset == null || offset.isEmpty()) {
                response = restTemplate.getForEntity(plugins.replace("{CONSUMER}", usernameOrId) + "?size=" + size, String.class);
            } else
                response = restTemplate.getForEntity(plugins.replace("{CONSUMER}", usernameOrId) + "?size=" + size + "&offset=" + offset, String.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Rate limit how many HTTP requests can be made in a given period of seconds, minutes, hours.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param consumer the {@code String} The username or id of the Consumer.
     * @param second the {@code int} The number of HTTP requests that can be made per second.
     * @param minute the {@code int} The number of HTTP requests that can be made per minute.
     * @param hour the {@code int} The number of HTTP requests that can be made per hour.
     * @param refrence the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/consumers/{CONSUMER}/plugins_rate_limitting"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Rate limit how many HTTP requests can be made in a given period of seconds, minutes, hours", description = "Restrict consumer by number of requests", operationId = "rate-limit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> plugins(@RequestParam String consumer, @RequestParam int second, @RequestParam int minute, @RequestParam int hour, @RequestParam String refrence, @RequestParam String description) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("name", "rate-limiting");
        body.add("config.second", second);
        body.add("config.minute", minute);
        body.add("config.hour", hour);

        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(plugins.replace("{CONSUMER}", consumer), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(refrence, description, consumer + ":" + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second), "create_RateLimitting");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            exception.printStackTrace();
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private ResponseEntity<String> subscribersChanges(String refrence, String description, String auditResource, String auditAction /*, HttpServletRequest request*/) {
        ResponseEntity<String> changes = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            AddingAuditTrailDto addingAuditTrailDto = new AddingAuditTrailDto();
            addingAuditTrailDto.setAuditAction(auditAction);
            addingAuditTrailDto.setAuditResource(auditResource);
            addingAuditTrailDto.setAuditUser("subscribers-admin-user");
            addingAuditTrailDto.setApplicCd(auditTrailApplicCd);
            addingAuditTrailDto.setRefrence(refrence);
            addingAuditTrailDto.setDescription(description);
            addingAuditTrailDto.setLastValue("");
            changes = restTemplate.postForEntity(addAuditTrailDtoUrl, new HttpEntity<AddingAuditTrailDto>(addingAuditTrailDto, headers), String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return changes;
    }

}
