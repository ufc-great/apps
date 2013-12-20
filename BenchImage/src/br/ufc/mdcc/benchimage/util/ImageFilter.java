package br.ufc.mdcc.benchimage.util;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

/**
 * Classe que realiza a aplicação dos filtros!.
 * 
 * @author Philipp
 */
public final class ImageFilter {

	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// altura e largura da imagem
		final int height = options.outHeight;
		final int width = options.outWidth;

		int inSampleSize = 1;

		// se a imagem carregada for maior que a imagem requerida, não faz nada
		if (height > reqHeight || width > reqWidth) {

			// calcula a proporção entre a imagem original e a reduzida.
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Escolhe a proporção menor para garantir que vai caber na tela
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Bitmap decodeSampledBitmapFromResource(InputStream is, String size) throws IOException {

		// Cria um Bitmap Factory para checar as dimensões!
		final BitmapFactory.Options options = new BitmapFactory.Options();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			options.inMutable = true;
		}

		// Calcula a proporção baseada nas dimensões passadas
		if (size.equals("1MP") || size.equals("2MP")) {
			options.inSampleSize = 2;
		} else if (size.equals("4MP")) {
			options.inSampleSize = 4;
		} else if (size.equals("6MP")) {
			options.inSampleSize = 6;
		} else if (size.equals("8MP")) {
			options.inSampleSize = 8;
		} else {
			options.inSampleSize = 1;
		}

		// decodifica a imagem baseado nas novas proporções
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(is, null, options);
	}

	/**
	 * Como o funciona o filtro de Map: Para o mapa de filtro que tem três cores
	 * RGB e 256 de largura, é a mesma logica para o mapa que tem apenas uma
	 * faixa de 256 de largura a diferença, que a cor agora é extraida de acordo
	 * com a sua faixa. Exemplo: Na imagem original o ponto 100x100 tem essa cor
	 * no RGB (130,215,100), então vou buscar na faixa vermelha na posição 130,
	 * o vermelho desse ponto e substituir pelo da imagem original, mesmo
	 * procedimento para ser feito para a faixa verde e azul.
	 * 
	 * @param source
	 * @param map
	 * @return
	 */
	public Bitmap mapFilter(Bitmap source, Bitmap map) {

		int imgHeight = source.getHeight();
		int imgWidth = source.getWidth();

		int filterHeight = map.getHeight();

		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {

				int cor = source.getPixel(x, y);

				int r = Color.red(cor);
				int g = Color.green(cor);
				int b = Color.blue(cor);

				if (filterHeight == 1) {
					// para cada cor de cada ponto da foto original
					// pega a tonalidade dessa cor no map
					r = map.getPixel(r, 0);
					g = map.getPixel(g, 0);
					b = map.getPixel(b, 0);
				} else {
					// para cada cor de cada ponto da foto original
					// pega a tonalidade dessa cor no map
					r = map.getPixel(r, 0);
					g = map.getPixel(g, 1);
					b = map.getPixel(b, 2);
				}

				// e seta a tonalidade referente ao canal que está
				// previamente mapeado
				source.setPixel(x, y, Color.rgb(Color.red(r), Color.green(g), Color.blue(b)));
			}
		}

		return source;

	}

	public Bitmap filterApply(Bitmap source, double filter[][], double factor, double offset) {

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
						int imageX = (x - (filWidth / 2) + filterX + imgWidth) % imgWidth;
						int imageY = (y - (filHeight / 2) + filterY + imgHeight) % imgHeight;

						int cor = source.getPixel(imageX, imageY);

						// vlr do filtro
						double vlr = filter[filterX][filterY];

						// canais de cor
						r += (Color.red(cor) * vlr);
						g += (Color.green(cor) * vlr);
						b += (Color.blue(cor) * vlr);

					}
				}

				// para trucar os valor para não passar de 255
				r = Math.min(Math.max((int) (factor * r + offset), 0), 255);
				g = Math.min(Math.max((int) (factor * g + offset), 0), 255);
				b = Math.min(Math.max((int) (factor * b + offset), 0), 255);

				source.setPixel(x, y, Color.rgb(r, g, b));
			}
		}

		return source;
	}

	// source: http://en.wikipedia.org/wiki/Grayscale
	public Bitmap greyScaleImage(Bitmap source) {

		Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

		// constant factors
		final double GS_RED = 0.299;
		final double GS_GREEN = 0.587;
		final double GS_BLUE = 0.114;

		// pixel information
		int R, G, B;
		int pixel;

		// get image size
		int width = source.getWidth();
		int height = source.getHeight();

		// scan through every single pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				pixel = source.getPixel(x, y);

				// retrieve color of all channels
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);

				// take conversion up to one single value
				R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);

				// set new pixel color to output bitmap
				output.setPixel(x, y, Color.rgb(R, G, B));
			}
		}

		// return final image
		return output;
	}

	private int colordodge(int in1, int in2) {
		float image = (float) in2;
		float mask = (float) in1;
		return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << 8) / (255 - image)))));
	}

	// by phil! =)
	// aplica o layer na foto!
	public Bitmap colorDodgeBlendOptimized(Bitmap source, Bitmap layer) {

		int width = source.getWidth();
		int height = source.getHeight();

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				// ler primeiro a largura para cada coluna fixa!
				int filterInt = layer.getPixel(j, i);
				int srcInt = source.getPixel(j, i);

				// get rgb
				int redValueFilter = Color.red(filterInt);
				int greenValueFilter = Color.green(filterInt);
				int blueValueFilter = Color.blue(filterInt);

				int redValueSrc = Color.red(srcInt);
				int greenValueSrc = Color.green(srcInt);
				int blueValueSrc = Color.blue(srcInt);

				int redValueFinal = colordodge(redValueFilter, redValueSrc);
				int greenValueFinal = colordodge(greenValueFilter, greenValueSrc);
				int blueValueFinal = colordodge(blueValueFilter, blueValueSrc);

				int pixel = Color.rgb(redValueFinal, greenValueFinal, blueValueFinal);

				source.setPixel(j, i, pixel);
			}
		}

		return source;
	}

	public Bitmap invertColors(Bitmap source) {

		Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

		int R, G, B;
		int pixelColor;
		int height = source.getHeight();
		int width = source.getWidth();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixelColor = source.getPixel(x, y);

				R = 255 - Color.red(pixelColor);
				G = 255 - Color.green(pixelColor);
				B = 255 - Color.blue(pixelColor);

				output.setPixel(x, y, Color.rgb(R, G, B));
			}
		}

		return output;
	}

}
