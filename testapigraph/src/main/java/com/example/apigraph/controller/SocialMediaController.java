package com.example.apigraph.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@CrossOrigin(origins = "http://localhost:4200/",methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/api/social-media")
public class SocialMediaController {

	@Value("${instagram.access-token}")
	private String instagramAccessToken;
	
	@Value("${instagram.account-id}")
	private String instagramAccountId;
	
	@Value("${facebook.access-token}")
	private String access_token;
	
	@Value("${facebook.page-id}")
	private String facebookPageId;
	
	private final RestTemplate restTemplate;

	public SocialMediaController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	// INSTAGRAM
	// Este endpoint es para publicar en Instagram
	@PostMapping("/publicar/instagram")
	public ResponseEntity<Map<String, Object>> publicarInstagram(@RequestParam("image_url") String image_url,
															     @RequestParam("caption") String caption) {

		String mediaEndpoint = "https://graph.facebook.com/v21.0/" + instagramAccountId + "/media";
		String publishEndpoint = "https://graph.facebook.com/v21.0/" + instagramAccountId + "/media_publish";

		Map<String, Object> responseBody = new HashMap<>();

		try {
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			// Cuerpo para el contenedor de media
	        Map<String, Object> mediaBody = new HashMap<>();
	        mediaBody.put("access_token", access_token);
	        mediaBody.put("image_url", image_url);
	        mediaBody.put("caption", caption);
	        
	        HttpEntity<Map<String, Object>> mediaEntity = new HttpEntity<>(mediaBody, headers);

			ResponseEntity<Map> mediaResponse = restTemplate.exchange(mediaEndpoint, HttpMethod.POST,
					mediaEntity, Map.class);		
			
			if (mediaResponse.getStatusCode() != HttpStatus.OK || !mediaResponse.getBody().containsKey("id")) {
				throw new RuntimeException("No se pudo crear el contenedor de medios.");
			}

			String container_id = mediaResponse.getBody().get("id").toString();

			//***************************************************************************************/
			// Aca Publicamos el contenedor
			Map<String, Object> publishBody = new HashMap<>();
	        publishBody.put("access_token", access_token);
	        publishBody.put("creation_id", container_id);
	        
	        HttpEntity<Map<String, Object>> publishEntity = new HttpEntity<>(publishBody, headers);

			ResponseEntity<Map> publishResponse = restTemplate.exchange(publishEndpoint, HttpMethod.POST,
					publishEntity, Map.class);

			if (publishResponse.getStatusCode() != HttpStatus.OK || !publishResponse.getBody().containsKey("id")) {
				throw new RuntimeException("No se pudo publicar el contenedor.");
			}

			String publish_id = publishResponse.getBody().get("id").toString();

			// Construir respuesta final
			responseBody.put("status", "success");
			responseBody.put("message", "Publicación creada y publicada exitosamente");
			responseBody.put("container_id", container_id);
			responseBody.put("publish_id", publish_id);
			responseBody.put("image_url", image_url);
			responseBody.put("caption", caption);

			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			// Manejo de errores
			responseBody.put("status", "error");
			responseBody.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);

		}

	}
	//Para traer todas las publicaciones de Instagram
	@GetMapping("/publicaciones/instagram")
	public ResponseEntity<?> obtenerPublicacionesInstagram(){
		
		String instagramPublicacionesURL = "https://graph.instagram.com/me/media";
		
		try {
			
			//Construyo la URL con los parametros a traer de nuestra publicacion
			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(instagramPublicacionesURL)
                    .queryParam("fields", "id,caption,media_type,media_url,permalink,timestamp")
                    .queryParam("access_token", instagramAccessToken);
			
			RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Error al obtener publicaciones de Instagram");
            }
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
		}
	}
	
	
	
	//Para traer todas las historias de Instagram
	@GetMapping("/historias/instagram")
	public ResponseEntity<?> obtenerHistoriasInstagram(){
		
		String instagramHistoriasURL = "https://graph.instagram.com/me/stories";
		
		try {
			// Construir la URL con los parámetros
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(instagramHistoriasURL)
                    .queryParam("fields", "id,media_type,media_url,permalink,timestamp")
                    .queryParam("access_token", instagramAccessToken);
			
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Error al obtener las historias de Instagram");
            }
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
		}
	}
	
		

	/*
	 * // Este endpoint es para publicar un mensaje en Facebook
	 * 
	 * @PostMapping("/publicar/facebook") public ResponseEntity<String>
	 * publicarFacebook(@RequestParam(name = "message") String message) {
	 * 
	 * String facebookPostURL = "https://graph.facebook.com/v21.0/" + facebookPageId
	 * + "/feed";
	 * 
	 * UriComponentsBuilder builder =
	 * UriComponentsBuilder.fromHttpUrl(facebookPostURL) .queryParam("access_token",
	 * access_token).queryParam("message", message);
	 * 
	 * HttpHeaders headers = new HttpHeaders();
	 * headers.setContentType(MediaType.APPLICATION_JSON); HttpEntity<String> entity
	 * = new HttpEntity<>(headers);
	 * 
	 * try { ResponseEntity<String> response =
	 * restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity,
	 * String.class);
	 * 
	 * if (response.getStatusCode() == HttpStatus.OK) { return
	 * ResponseEntity.ok("Publicación exitosa"); } else { return
	 * ResponseEntity.status(response.getStatusCode()).body("Error al publicar"); }
	 * 
	 * } catch (Exception e) { return
	 * ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " +
	 * e.getMessage()); } }
	 */
	
	//FACEBOOK
	// Este endpoint es para publicar en Facebook con imagen
	@PostMapping("/publicar/facebook")
	public ResponseEntity<Map<String, String>> publicarFacebook(@RequestParam(name = "message") String message,
												   @RequestParam(name = "url", required = false) String url) {

		
		String publicarImagenMessageURL = "https://graph.facebook.com/v21.0/" + facebookPageId + "/photos";

		try {
			
			HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
         // Construir el cuerpo de la solicitud
            Map<String, String> body = new HashMap<>();
            
            if (url != null && !url.isEmpty()) {
                body.put("url", url);
            }
            if (message != null && !message.isEmpty()) {
                body.put("message", message); // No es necesario codificar en JSON
            }
            body.put("access_token", access_token); // Token de acceso siempre obligatorio
            
            // Crear HttpEntity con cuerpo JSON y cabeceras
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            
         // Realizar la solicitud POST
            ResponseEntity<String> postResponse = restTemplate.exchange(
                publicarImagenMessageURL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Publicación creada con ID: " + postResponse.getBody());
            return ResponseEntity.ok(response);
            //return ResponseEntity.ok("Publicación creada con ID: " + postResponse.getBody());
            			
		} catch (Exception e) {
			//return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		    // Respuesta de error estructurada
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "Ocurrió un error al procesar la solicitud.");
	        errorResponse.put("details", e.getMessage()); // Detalle del error (excepción)
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}
	
	
}
