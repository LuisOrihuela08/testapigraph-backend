package com.example.apigraph.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.apigraph.model.Post;


@Service
public class PostService {

	
	//@Value("${file.dir}")
	//private String uploadDir;
	
	@Value("${cloudinary.cloud_name}")
	private String cloudName;

	@Value("${cloudinary.api_key}")
	private String apiKey;

	@Value("${cloudinary.api_secret}")
	private String apiSecret;
	
	private Cloudinary cloudinary;
	
	private List<Post> posts = new ArrayList<>();
	
	public void initializeCloudinary() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
	
	
	/*
	public String createPost(String caption, MultipartFile imageName) throws IOException{
		
		File dir = new File(uploadDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}	
		
		//String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		String filename = imageName.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-_]", "_");
		File destFile = new File(uploadDir + "/" + filename);
		
		// Guardar temporalmente y redimensionar la imagen
        File tempFile = File.createTempFile("temp_", null);
        imageName.transferTo(tempFile);
        resizeImage(tempFile, destFile, 1380, 1080);  // Redimensionar a 1080x1080
        tempFile.delete(); // Eliminar archivo temporal
               
        Post post = new Post(caption, filename);
        posts.add(post);
        
        return filename;
	}
	*/
	
	public String createPost(String caption, MultipartFile imageName) throws IOException {
        // Inicializamos el cliente de Cloudinary
        initializeCloudinary();

        // Crear archivo temporal
        File tempFile = File.createTempFile("temp_", ".jpg");
        imageName.transferTo(tempFile);
        
        // Redimensionar la imagen
        File resizedFile = new File(tempFile.getParent(), "resized_" + tempFile.getName());
        resizeImage(tempFile, resizedFile, 1280, 1080);  // Redimensionar a 1380x1080
        tempFile.delete(); // Eliminar archivo temporal

        // Subir la imagen redimensionada a Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(resizedFile, ObjectUtils.asMap(
                "folder", "Images",
                "public_id", imageName.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-_]", "_")
        ));

        // Obtener la URL de la imagen subida
        String imageUrl = uploadResult.get("url").toString();

        // Crear el post con la descripción y la URL de la imagen
        Post post = new Post(caption, imageUrl);
        posts.add(post);

        // Eliminar el archivo redimensionado después de subirlo
        resizedFile.delete();

        return imageUrl; // Devolvemos la URL de la imagen subida
    }
	
	
	private void resizeImage(File input, File output, int width, int height) throws IOException {
        net.coobird.thumbnailator.Thumbnails.of(input)
                //.size(width, height)
                //.crop(net.coobird.thumbnailator.geometry.Positions.CENTER)//Esto recorta desde el centro
                .forceSize(width, height)//Para usar esto, deshabilitar la linea 50. Esto fuerza a las dimensiones pre definidas, lo ajusta
                .outputFormat("jpg")  // Especificar el formato de salida
                .toFile(output);
    }
	
	public List<Post> getAllPosts() {
        return posts;
    }
	
}
