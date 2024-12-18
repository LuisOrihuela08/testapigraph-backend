package com.example.apigraph.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.apigraph.model.Post;


@Service
public class PostService {

	
	@Value("${file.dir}")
	private String uploadDir;
	
	private List<Post> posts = new ArrayList<>();
	
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
	
	private void resizeImage(File input, File output, int width, int height) throws IOException {
        net.coobird.thumbnailator.Thumbnails.of(input)
                //.size(width, height)
                //.crop(net.coobird.thumbnailator.geometry.Positions.CENTER)//Esto recorta desde el centro
                .forceSize(width, height)//Para usar esto, deshabilitar la linea 50. Esto fuerza a las dimensiones pre definidas, lo ajusta
                .toFile(output);
    }
	
	public List<Post> getAllPosts() {
        return posts;
    }
	
}
