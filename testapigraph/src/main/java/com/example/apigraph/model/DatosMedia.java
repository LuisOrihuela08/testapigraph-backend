package com.example.apigraph.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatosMedia {

	private String image_url;//Esto es para la imagen en instagram
	private String caption;//Esto es para el texto en Instagram
	private String message;//Esto es para el texto en Facebook
	private String url;//Esto es para la imagen en facebook
}
