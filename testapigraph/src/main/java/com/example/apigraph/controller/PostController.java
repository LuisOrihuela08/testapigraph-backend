package com.example.apigraph.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMethod;


import com.example.apigraph.config.PostService;
import com.example.apigraph.model.Post;

@RestController
@CrossOrigin(origins = "http://localhost:4200/",methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/api/post")
public class PostController {

	@Autowired
	private PostService postService;
	
	@Value("${file.dir}")
	private String uploadDir;
	
	@PostMapping("/create")
	public ResponseEntity<Map<String, String>> createPost (@RequestParam("caption") String caption,
											  				@RequestParam("imageName") MultipartFile  imageName){
		
		try {
			String filename = postService.createPost(caption, imageName);
			
			//String imageURL = "http://localhost:8080/imagen/" + filename;
			String imagen = filename;
			
			Map<String, String> response = new HashMap<>();
			response.put("message", "Post creado con exito");
			response.put("caption", caption);
			response.put("imageName", imagen);
			
			// Responde con el nombre del archivo guardado
            return ResponseEntity.ok(response);
            
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Error al crear el post: " + e.getMessage()));		}
	}
	
	//endpoint para servir la imagen a la web
		@GetMapping("/imagen/{imageName:.+}")
		public ResponseEntity<Resource> obtenerImagen (@PathVariable ("imageName") String imageName){
			try {
				//Ruta del directorio donde se la almacena la imagen del voucher
				Path path = Paths.get(uploadDir).resolve(imageName);
				
				// Imprimir la ruta de la imagen que se está buscando
		        //System.out.println("Accediendo a la imagen en: " + path);
				
				
				Resource resource = new UrlResource(path.toUri());
				
				//Verificar si el recurso existe
				if (resource.exists() || resource.isReadable()) {
					MediaType mediaType;
					String extension = imageName.substring(imageName.lastIndexOf('.') + 1).toLowerCase();
					
					switch (extension) {
					case "jpg":
					case "jpeg":
						mediaType = MediaType.IMAGE_JPEG;
						break;
					case "png":
						mediaType = MediaType.IMAGE_PNG;
						break;
					default:
						mediaType = MediaType.APPLICATION_OCTET_STREAM; // tipo generico
						break;
					}
					return ResponseEntity.ok().contentType(mediaType).body(resource);
				} else {
					return ResponseEntity.notFound().build();
				}
				
			} catch (MalformedURLException e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}
	
		
	
	@GetMapping("/list")
	public ResponseEntity<List<Post>> getAllPosts(){
		try {
			List<Post> posts = postService.getAllPosts();
			posts.forEach(image -> {
				String nombreImagen = image.getImageName();
				if (!nombreImagen.startsWith("/api/post/imagen/")) {
					image.setImageName("/api/post/imagen/" + nombreImagen);
				}
				
			});		
			return ResponseEntity.ok(posts);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(null);
		}
	}
}
