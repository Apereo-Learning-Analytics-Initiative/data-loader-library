/**
 * 
 */
package unicon.matthews.dataloader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import unicon.matthews.caliper.Envelope;
import unicon.matthews.caliper.Event;
import unicon.matthews.entity.ClassMapping;
import unicon.matthews.entity.DataSync;
import unicon.matthews.entity.UserMapping;
import unicon.matthews.oneroster.Enrollment;
import unicon.matthews.oneroster.LineItem;
import unicon.matthews.oneroster.User;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author ggilbert
 *
 */
public class MatthewsClient {

  private RestTemplate restTemplate;
  private HttpHeaders httpHeaders;
  
  private String key;
  private String secret;
  private String baseUrl;
  
  private static final String LOGIN_URL = "/api/auth/login";
  
  private MatthewsClient() {}
  
  public static class Builder {
    MatthewsClient _matthewsClient = new MatthewsClient();
    
    public Builder withRestTemplate(RestTemplate restTemplate) {
      _matthewsClient.restTemplate = restTemplate;
      return this;
    }
    
    public Builder withHttpHeaders(HttpHeaders httpHeaders) {
      _matthewsClient.httpHeaders = httpHeaders;
      return this;
    }
    
    public Builder withBaseUrl(String baseUrl) {
      if (StringUtils.endsWith(baseUrl, "/")) {
        baseUrl = StringUtils.substringBeforeLast(baseUrl, "/");
      }

      _matthewsClient.baseUrl = baseUrl;
      
      return this;
    }
    
    public Builder withKey(String key) {
      _matthewsClient.key = key;
      return this;
    }
    
    public Builder withSecret(String secret) {
      _matthewsClient.secret = secret;
      return this;
    }
    
    public MatthewsClient build() {
      if (StringUtils.isBlank(_matthewsClient.key) || 
          StringUtils.isBlank(_matthewsClient.secret) ||
          StringUtils.isBlank(_matthewsClient.baseUrl) ||
          _matthewsClient.restTemplate == null ||
          _matthewsClient.httpHeaders == null) {
        throw new IllegalStateException();
      }
      
      _matthewsClient.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
      _matthewsClient.httpHeaders.add("X-Requested-With", "XMLHttpRequest");
      
      _matthewsClient.httpHeaders.add("Authorization", "Bearer "+_matthewsClient.getToken());
      
      return _matthewsClient;
    }
  }
  
  @SuppressWarnings("rawtypes")
  public String getToken() {
    
    JsonObject request = new JsonObject();
    request.add("username", new JsonPrimitive(this.key));
    request.add("password", new JsonPrimitive(this.secret));

    HttpEntity<String> entity = new HttpEntity<String>(request.toString(), httpHeaders);
    
    ResponseEntity<Map> loginResponse 
      = restTemplate
        .exchange(this.baseUrl + LOGIN_URL, 
            HttpMethod.POST, 
            entity, 
            Map.class);
    
    return (String)loginResponse.getBody().get("token");
  }
  
  public void postEnrollment(Enrollment enrollment) {
    HttpEntity<Enrollment> he = new HttpEntity<Enrollment>(enrollment, this.httpHeaders);
    
    String path = "/api/classes/{classSourcedId}/enrollments";
    String url = this.baseUrl + StringUtils.replace(path, "{classSourcedId}", enrollment.getKlass().getSourcedId());
    
    restTemplate
    .exchange(url, HttpMethod.POST, he, JsonObject.class);
  }
  
  public void postUser(User user) {
    HttpEntity<User> he = new HttpEntity<User>(user, this.httpHeaders);
    
    String path = "/api/users";
    String url = this.baseUrl + path;

    ResponseEntity<JsonObject> response =
      restTemplate
        .exchange(url, HttpMethod.POST, he, JsonObject.class);
    
    
    if (response != null && response.getStatusCode() == HttpStatus.FORBIDDEN) {
      httpHeaders.add("Authorization", "Bearer "+ getToken());
      restTemplate
      .exchange(url, HttpMethod.POST, he, JsonObject.class);      
    }
    
  }
  
  public void postUserMapping(UserMapping userMapping) {
    HttpEntity<UserMapping> he = new HttpEntity<UserMapping>(userMapping, this.httpHeaders);
    
    String path = "/api/users/mapping";
    String url = this.baseUrl + path;

    restTemplate
        .exchange(url, HttpMethod.POST, he, JsonObject.class);

  }
  
  public void postLineItem(LineItem lineItem) {
    HttpEntity<LineItem> he = new HttpEntity<LineItem>(lineItem, this.httpHeaders);
    
    String path = "/api/classes/{classSourcedId}/lineitems";
    String url = this.baseUrl + StringUtils.replace(path, "{classSourcedId}", lineItem.getKlass().getSourcedId());

    restTemplate
        .exchange(url, HttpMethod.POST, he, JsonObject.class);

  }
 
  public void postClass(unicon.matthews.oneroster.Class klass) {
    HttpEntity<unicon.matthews.oneroster.Class> he = new HttpEntity<unicon.matthews.oneroster.Class>(klass, this.httpHeaders);
    
    String path = "/api/classes";
    String url = this.baseUrl + path;

    restTemplate
        .exchange(url, HttpMethod.POST, he, JsonObject.class);
  }
  
  public void postCourse(unicon.matthews.oneroster.Course course) {
    HttpEntity<unicon.matthews.oneroster.Course> he = new HttpEntity<unicon.matthews.oneroster.Course>(course, this.httpHeaders);
    
    String path = "/api/courses";
    String url = this.baseUrl + path;

    restTemplate
        .exchange(url, HttpMethod.POST, he, JsonObject.class);
  }
  
  public void postClassMapping(ClassMapping classMapping) {
    HttpEntity<ClassMapping> he = new HttpEntity<ClassMapping>(classMapping, this.httpHeaders);
    
    String path = "/api/classes/mapping";
    String url = this.baseUrl + path;

    restTemplate
        .exchange(url, HttpMethod.POST, he, JsonObject.class);

  }

  public void postEvent(Event event, String sensorName) {
    Envelope envelope
    = new Envelope.Builder()
      .withData(Collections.singletonList(event))
      .withSendTime(LocalDateTime.now())
      .withSensor(sensorName)
      .build();

    HttpEntity<Envelope> h = new HttpEntity<Envelope>(envelope, this.httpHeaders);
    
    String path = "/api/caliper";
    String url = this.baseUrl + path;

    restTemplate
        .exchange(url, HttpMethod.POST, h, String.class);
  }
  
  public void postEvents(Collection<Event> events, String sensorName) {
    Envelope envelope
    = new Envelope.Builder()
      .withData(new ArrayList<>(events))
      .withSendTime(LocalDateTime.now())
      .withSensor(sensorName)
      .build();

    HttpEntity<Envelope> h = new HttpEntity<Envelope>(envelope, this.httpHeaders);
    
    String path = "/api/caliper";
    String url = this.baseUrl + path;

    restTemplate
        .exchange(url, HttpMethod.POST, h, String.class);
  }
  
  public void postDataSync(DataSync dataSync) {
    HttpEntity<DataSync> he = new HttpEntity<DataSync>(dataSync, this.httpHeaders);
    
    String path = "/api/sync";
    String url = this.baseUrl + path;

    restTemplate
        .exchange(url, HttpMethod.POST, he, JsonObject.class);

  }
  
  public DataSync getLatestDataSyncForType(unicon.matthews.entity.DataSync.DataSyncType dataSyncType) {
    DataSync dataSync = null;
    
    String path = "/api/sync/".concat(dataSyncType.toString()).concat("/latest");
    String url = this.baseUrl + path;
    
    ResponseEntity<DataSync> response = null;
    try {
      response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(this.httpHeaders), DataSync.class);
    } 
    catch (RestClientException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    if (response != null && response.getStatusCode() == HttpStatus.OK) {
      dataSync = response.getBody();
    }
  
    return dataSync;
  }
}
