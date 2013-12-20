package br.ufc.mdcc.benchimage.util;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Classe que realiza a aplicação dos filtros!.
 * 
 * @author Philipp
 */
public final class ImageFilter {
	
	public BufferedImage mapFilter(BufferedImage source, BufferedImage map) {
		
		int imgHeight = source.getHeight();
		int imgWidth = source.getWidth();
		
		int filterHeight = map.getHeight();
		
		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				
				Color color = new Color(source.getRGB(x, y));
				
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				
				if (filterHeight == 1) {
					// para cada cor de cada ponto da foto original
					// pega a tonalidade dessa cor no map
					r = map.getRGB(r, 0);
					g = map.getRGB(g, 0);
					b = map.getRGB(b, 0);
				} else {
					// para cada cor de cada ponto da foto original
					// pega a tonalidade dessa cor no map
					r = map.getRGB(r, 0);
					g = map.getRGB(g, 1);
					b = map.getRGB(b, 2);
				}
				
				// e seta a tonalidade referente ao canal que está
				// previamente mapeado
				source.setRGB(x, y, new Color(new Color(r).getRed(), new Color(g).getGreen(), new Color(b).getBlue()).getRGB());
			}
		}
		
		return source;
		
	}
	
	public BufferedImage filterApply(BufferedImage source, double filter[][], double factor, double offset) {
		
		int imgHeight = source.getHeight();// altura da imagem
		int imgWidth = source.getWidth();// largura da imagem
		
		int filHeight = filter.length;
		int filWidth = filter.length;
		
		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				
				int r = 0, g = 0, b = 0;
				
				// for dos filtros e aplicando sobre cada pixel da foto
				for (int filterX = 0; filterX < filWidth; filterX++) {
					for (int filterY = 0; filterY < filHeight; filterY++) {
						
						// pega pixel da imagem que o filtro vai calcular
						int imageX = (x - (filWidth / 2) + filterX + imgWidth)
								% imgWidth;
						int imageY = (y - (filHeight / 2) + filterY + imgHeight)
								% imgHeight;
						
						int cor = source.getRGB(imageX, imageY);
						
						Color color = new Color(cor);
						
						// vlr do filtro
						double vlr = filter[filterX][filterY];
						
						// canais de cor
						r += (color.getRed() * vlr);
						g += (color.getGreen() * vlr);
						b += (color.getBlue() * vlr);
						
					}
				}
				
				// para trucar os valor para não passar de 255
				r = Math.min(Math.max((int) (factor * r + offset), 0), 255);
				g = Math.min(Math.max((int) (factor * g + offset), 0), 255);
				b = Math.min(Math.max((int) (factor * b + offset), 0), 255);
				
				source.setRGB(x, y, new Color(r, g, b).getRGB());
			}
		}
		
		return source;
	}
	
	// source: http://en.wikipedia.org/wiki/Grayscale
	public BufferedImage greyScaleImage(BufferedImage source) {
		
		BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		
		// constant factors
		final double GS_RED = 0.299;
		final double GS_GREEN = 0.587;
		final double GS_BLUE = 0.114;
		
		// pixel information
		int R, G, B;
		
		// get image size
		int width = source.getWidth();
		int height = source.getHeight();
		
		// scan through every single pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				Color color = new Color(source.getRGB(x, y));
				
				// retrieve color of all channels
				R = color.getRed();
				G = color.getGreen();
				B = color.getBlue();
				
				// take conversion up to one single value
				R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
				
				// set new pixel color to output bitmap
				output.setRGB(x, y, new Color(R, G, B).getRGB());
			}
		}
		
		// return final image
		return output;
	}
	
	private int colordodge(int in1, int in2) {
		float image = (float) in2;
		float mask = (float) in1;
		return ((int) ((image == 255) ? image
				: Math.min(255, (((long) mask << 8) / (255 - image)))));
	}
	
	// by phil! =)
	// aplica o layer na foto!
	public BufferedImage colorDodgeBlendOptimized(BufferedImage source, BufferedImage layer) {
		
		int width = source.getWidth();
		int height = source.getHeight();
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				
				// ler primeiro a largura para cada coluna fixa!
				int filterInt = layer.getRGB(j, i);
				int srcInt = source.getRGB(j, i);
				
				Color colorFilter = new Color(filterInt);
				Color colorSrc = new Color(srcInt);
				
				// get rgb
				int redValueFilter = colorFilter.getRed();
				int greenValueFilter = colorFilter.getGreen();
				int blueValueFilter = colorFilter.getBlue();
				
				int redValueSrc = colorSrc.getRed();
				int greenValueSrc = colorSrc.getGreen();
				int blueValueSrc = colorSrc.getBlue();
				
				int redValueFinal = colordodge(redValueFilter, redValueSrc);
				int greenValueFinal = colordodge(greenValueFilter, greenValueSrc);
				int blueValueFinal = colordodge(blueValueFilter, blueValueSrc);
				
				int pixel = new Color(redValueFinal, greenValueFinal, blueValueFinal).getRGB();
				
				source.setRGB(j, i, pixel);
			}
		}
		
		return source;
	}
	
	public BufferedImage invertColors(BufferedImage source) {
		
		BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		
		int R, G, B;
		int height = source.getHeight();
		int width = source.getWidth();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = new Color(source.getRGB(x, y));
				
				R = 255 - color.getRed();
				G = 255 - color.getGreen();
				B = 255 - color.getBlue();
				
				output.setRGB(x, y, new Color(R, G, B).getRGB());
			}
		}
		
		return output;
	}
	
}
