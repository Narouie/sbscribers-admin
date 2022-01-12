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
 * The {@code SubscribersController} class provides web methods to create subscribers,get list of subscribers and basic Auth,key Auth
 * get list of credentials,acl,ip restrictions and get list of ip white list and rate limitting about {@code subscriber}
 * <p>
 * Also, It includes saving the history of changes in the Audit Trail Management System.
 * <p>
 * API specification in {@code SubscribersController} using OpenAPI.
 *
 * @author tahbaz
 */

@RestController
@Tag(name = "Subscribers", description = "Endpoints for managing subscribers")
public class SubscribersController {

    @Autowired
    RestTemplate restTemplate;

    @Value("${subscribersAdmin.subscribers.url}")
    private String subscribersUrl;

    @Value("${subscribersAdmin.subscribers.basic-auth.url}")
    private String basicAuthUrl_c;

    @Value("${subscribersAdmin.usernameOrId.basic-auth.url}")
    private String basicAuthUrl_u;

    @Value("${subscribersAdmin.subscribers.key-auth.url}")
    private String keyAuthUrl_c;

    @Value("${subscribersAdmin.usernameOrId.key-auth.url}")
    private String keyAuthUrl_u;

    @Value("${subscribersAdmin.keyOrId.key-auth.url}")
    private String keyAuthUrl_k;

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
     * Creates a new subscriber in kong database(postgres database).
     * Creates a new key in Redis database in the form of "ACT:" + username
     * Response status codes include 201,204,409,404,400,500
     * 201 status code:Created , 204 status code:subscriber does not create in database , 500 status code:Internal Server Error
     * 409 status code:subscriber created before , 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param username    the {@code String} subscriber or user of a service,The unique username of the subscriber. You must send either this field or custom_id with the request.
     * @param custom_id   the {@code String} Field for storing an existing unique ID for the subscriber - useful for mapping Kong with users in your existing database. You must send either this field or username with the request.
     * @param reference   the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,204,409,404,400,500
     */
    @RequestMapping(value = {"/subscribers"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Create subscriber", description = "Returns created subscriber", operationId = "createsubscriber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "409", description = "subscriber created before"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> subscribers(@RequestParam String username, @RequestParam String custom_id, @RequestParam String reference, @RequestParam String description) {

        var operations = redisTemplate.opsForHash();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("id", UUID.randomUUID().toString());
        body.add("created_at", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        body.add("username", username);
        body.add("custom_id", custom_id);
        body.add("tags", "");

        try {
            if (Objects.nonNull(username) && !username.isEmpty()) {

                ResponseEntity<String> response = restTemplate.postForEntity(subscribersUrl, new HttpEntity<>(body), String.class);
                String endUserId = "ACT:" + username;
                if (response.getStatusCodeValue() == 201) {

                    if (!redisTemplate.hasKey(endUserId)) {
                        operations.put(endUserId, "created_at", String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

                        ResponseEntity<String> audit = subscribersChanges(reference, description, username, "create_subscriber");

                        return response;
                    }
                }

            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 409)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    /**
     * Get subscriber list, page by page.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param size   the {@code Integer} size of each page.
     * @param offset the {@code String} start of point for next page.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/subscribers"}, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get list of all subscribers", description = "Returns list of all subscribers", operationId = "getListAllsubscriber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> subscribers(@RequestParam(defaultValue = "3") Integer size, @RequestParam(required = false) String offset) {
        ResponseEntity<String> response = null;

        try {
            if (offset == null || offset.isEmpty()) {
                response = restTemplate.getForEntity(subscribersUrl + "?size=" + size, String.class);
            } else
                response = restTemplate.getForEntity(subscribersUrl + "?size=" + size + "&offset=" + offset, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Associate a basicAuth's credential to an existing subscriber object. A subscriber can have many credentials.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param subscriber  the {@code String} The id or username property of the subscriber entity to associate the credentials to.
     * @param username    the {@code String} The username to use in the basic authentication credential.
     * @param password    the {@code String} The password to use in the basic authentication credential.
     * @param reference   the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/subscribers/{subscriber}/basic-auth"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Associate a credential to an existing subscriber object. A subscriber can have many credentials.", description = "Create basic authentication for existing subscriber", operationId = "basic-auth_subscriber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> basicAuth(@RequestParam String subscriber, @RequestParam String username, @RequestParam String password, @RequestParam String reference, @RequestParam String description) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("username", username);
        body.add("password", password);
        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(basicAuthUrl_c.replace("{CONSUMER}", subscriber), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(reference, description, subscriber, "create_basicAuth");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 409)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Retrieve basicAuth credentials associated with a subscriber, page by page.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param usernameOrId the {@code String} The username or id of the subscriber whose credentials need to be listed.
     * @param size         the {@code Integer} size of each page.
     * @param offset       the {@code String} start of point for next page.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/subscribers/{usernameOrId}/basic-auth"}, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Retrieve basicAuth credentials associated with a subscriber, page by page", description = "Returns list of all basicAuth associated with a subscriber", operationId = "basic-auth_usernameOrId")
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
     * Associate a keyAuth's credential to an existing subscriber object.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param subscriber  the {@code String} The id or username property of the subscriber entity to associate the credentials to.
     * @param reference   the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/subscribers/{subscriber}/key-auth"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Associate a credential to an existing subscriber object", description = "Associate a keyAuth's credential to an existing subscriber object", operationId = "key-auth_subscriber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> keyAuth(@RequestParam String subscriber, @RequestParam String reference, @RequestParam String description) {

        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(keyAuthUrl_c.replace("{consumer}", subscriber), null, String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(reference, description, subscriber, "create_keyAuth");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            exception.printStackTrace();
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Retrieve key Auth credentials associated with a subscriber, page by page.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param usernameOrId the {@code String} The username or id of the subscriber whose credentials need to be listed.
     * @param size         the {@code Integer} size of each page.
     * @param offset       the {@code String} start of point for next page.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/subscribers/{usernameOrId}/key-auth"}, method = RequestMethod.GET,
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
     * Retrieve a subscriber associated with an API key by making the following request.
     * Response status codes include 200,500.
     * 200 status code:Successful , 500 status code:Internal Server Error
     *
     * @param keyOrId the {@code String} The id or key property of the API key for which to get the associated subscriber.
     * @return status code the {@code ResponseEntity<String>} object representing 200,500
     */

    @RequestMapping(value = {"/key-auths/{keyOrId}/subscriber"}, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Retrieve a subscriber associated with an API key by making the following request", description = "Returns a subscriber associated with an API key by making the following request", operationId = "key-auth_keyOrId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> keyAuth(@RequestParam String keyOrId) {
        ResponseEntity<String> response = null;

        try {

            response = restTemplate.getForEntity(keyAuthUrl_k.replace("{key or id}", keyOrId), String.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Restrict access to a Service or a Route by adding subscribers to allowed or denied lists using arbitrary ACL groups.
     * Response status codes include 201,404,400,500
     * 201 status code:Created , 500 status code:Internal Server Error ,
     * 404 status code:Audit trail management path not found , 400 status code:Bad request
     *
     * @param subscriber  the {@code String} The username or id of the subscriber.
     * @param group       the {@code String} The arbitrary group name to associate with the subscriber.
     * @param reference   the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/subscribers/{subscriber}/acls"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Restrict access to a Service or a Route by adding subscribers to allowed or denied lists using arbitrary ACL groups.", description = "Access control list", operationId = "acl")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> acls(@RequestParam String subscriber, @RequestParam String group, @RequestParam String reference, @RequestParam String description) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("group", group);
        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(aclsUrl.replace("{CONSUMER}", subscriber), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(reference, description, subscriber, "create_acl");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
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
     * @param subscriber  the {@code String} The username or id of the subscriber.
     * @param ipWhiteList the {@code String} The ip white list or allowed ip's.
     * @param reference   the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/subscribers/{subscriber}/ip-restriction"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Restrict access to a Service or a Route by either allowing or denying IP addresses", description = "Restrict subscriber by ip addresses", operationId = "ip-restriction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> plugins(@RequestParam String subscriber, @RequestParam String ipWhiteList, @RequestParam String reference, @RequestParam String description) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("name", "ip-restriction");
        String[] ips = ipWhiteList.split(",");
        for (String ip : ips)
            body.add("config.allow", ip);
        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(plugins.replace("{CONSUMER}", subscriber), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(reference, description, subscriber, "create_ipRestriction");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 409)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
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
     * @param subscriber  the {@code String} The username or id of the subscriber.
     * @param second      the {@code int} The number of HTTP requests that can be made per second.
     * @param minute      the {@code int} The number of HTTP requests that can be made per minute.
     * @param hour        the {@code int} The number of HTTP requests that can be made per hour.
     * @param reference   the {@code String} Refrence number for saving changes in postgres database by audit trail management system.
     * @param description the {@code String} description for saving changes in postgres database by audit trail management system.
     * @return status code the {@code ResponseEntity<String>} object representing 201,404,400,500
     */

    @RequestMapping(value = {"/subscribers/{subscriber}/rate-limiting"}, method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Rate limit how many HTTP requests can be made in a given period of seconds, minutes, hours", description = "Restrict subscriber by number of requests", operationId = "rate-limit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created"),
            @ApiResponse(responseCode = "404", description = "Audit trail management path not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public @ResponseBody
    ResponseEntity<String> plugins(@RequestParam String subscriber, @RequestParam int second, @RequestParam int minute, @RequestParam int hour, @RequestParam String reference, @RequestParam String description) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("name", "rate-limiting");
        body.add("config.second", second);
        body.add("config.minute", minute);
        body.add("config.hour", hour);

        ResponseEntity<String> response = null;
        try {

            response = restTemplate.postForEntity(plugins.replace("{CONSUMER}", subscriber), new HttpEntity<>(body), String.class);
            if (response.getStatusCodeValue() == 201) {
                subscribersChanges(reference, description, subscriber, "create_RateLimitting");
            }

        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 404)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            if (exception.getStatusCode().value() == 400)
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private ResponseEntity<String> subscribersChanges(String reference, String description, String auditResource, String auditAction /*, HttpServletRequest request*/) {
        ResponseEntity<String> changes = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            AddingAuditTrailDto addingAuditTrailDto = new AddingAuditTrailDto();
            addingAuditTrailDto.setAuditAction(auditAction);
            addingAuditTrailDto.setAuditResource(auditResource);
            addingAuditTrailDto.setAuditUser("subscribers-admin-user");
            addingAuditTrailDto.setApplicCd(auditTrailApplicCd);
            addingAuditTrailDto.setRefrence(reference);
            addingAuditTrailDto.setDescription(description);
            addingAuditTrailDto.setLastValue("");
            changes = restTemplate.postForEntity(addAuditTrailDtoUrl, new HttpEntity<AddingAuditTrailDto>(addingAuditTrailDto, headers), String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return changes;
    }

}
