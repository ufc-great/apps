package br.ufc.mdcc.benchimage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import br.ufc.mdcc.benchimage.util.ImageFilter;
import br.ufc.mdcc.benchimage.util.ServerCommand;
import br.ufc.mdcc.mpos.net.TcpServer;

/**
 * @author Philipp
 */
public final class PhotoTcpServer extends TcpServer {
	
	public PhotoTcpServer() {
		super(35000, "PhotoTcpServer", PhotoTcpServer.class);
	}
	
	@Override
	public void clientRequest(Socket connection) throws IOException {
		
		ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
		
		try {
			logger.info("Recebe as instrucoes do mobile");
			ServerCommand cmd = (ServerCommand) ois.readObject();
			
			// System.out.println(cmd.getFilter());
			// System.out.println(cmd.getSize());
			
			DataInputStream dis = new DataInputStream(
					connection.getInputStream());
			int read = 0, size = 8 * 1024;
			long totalSize = 0L;
			
			logger.info("Recebe a foto do mobile");
			// 8mb!
			ByteArrayOutputStream baos = new ByteArrayOutputStream(
					8 * 1024 * 1024);
			byte buffer[] = new byte[size];
			
			long timeInit = System.currentTimeMillis();
			while ((read = dis.read(buffer)) != -1) {
				baos.write(buffer, 0, read);
				totalSize += read;
				
				if (cmd.getSize() == totalSize) {
					Arrays.fill(buffer, (byte) 0);
					baos.flush();
					break;
				}
			}
			
			long uploadTime = System.currentTimeMillis() - timeInit;
			logger.info("Terminou de receber a foto...");
			
			// KByte/s
			double uploadTx = (double) totalSize
					/ (((double) uploadTime / 1000.0) * 1024.0);
			
			// StringBuilder debug = new StringBuilder();
			// debug.append("[DEBUG]\n");
			// debug.append("Foto de Tamanho: ").append(totalSize).append(" bytes").append("\n");
			// debug.append("Tempo de transferencia: ").append(uploadTime).append(" ms | ").append((double)uploadTime
			// / 1000.0).append("s").append("\n");
			// double band = (double)totalSize / (((double)uploadTime / 1000.0)
			// * 1024.0);
			// debug.append("Velocidade: ").append(band).append(" KB/s");
			// logger.info(debug);
			
			byte data[] = baos.toByteArray();
			baos.close();
			baos = null;
			
			// aplica o filtro, na foto baixada...
			timeInit = System.currentTimeMillis();
			BufferedImage imgFiltered = filterApply(cmd,
					new ByteArrayInputStream(data));
			long executionTime = System.currentTimeMillis() - timeInit;
			logger.info("Terminou de aplicar o filtro na foto...");
			
			// reusa buffer de dados para uploadData...
			data = null;
			data = uploadData(uploadTx, uploadTime, executionTime, imgFiltered);
			
			// envia os dados do servidor para o mobile...
			connection.getOutputStream().write(data, 0, data.length);
			connection.getOutputStream().flush();
			logger.info("faz upload dos dados de upload e do size da foto filtrada");
			
			// envia a imagem filtrada para o mobile!
			sentImage(imgFiltered, connection.getOutputStream());
			logger.info("[FIM] Enviou a foto filtrada para o mobile...");
			
			imgFiltered = null;
			data = null;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	private byte[] uploadData(double uploadTx, long uploadTime,
			long executionTime, BufferedImage imgFiltered) throws IOException {
		// é a unica forma para capturar o tamanho da foto encodificada em jpeg
		// que serão transmitido nessa seção de upload: servidor -> mobile...
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4 * 1024 * 1024);
		sentImage(imgFiltered, baos);
		
		// padrao de separacao é ponto-virgula
		StringBuffer data = new StringBuffer("uploadData: "
				+ String.format("%.3f", uploadTx) + ";" + uploadTime + ";"
				+ executionTime + ";" + baos.size() + ";");
		
		char padding[] = new char[100 - data.toString().length()];
		Arrays.fill(padding, ' ');
		data.append(padding);
		
		// marca para ser removido da memoria...
		baos.close();
		baos = null;
		
		return data.toString().getBytes();
	}
	
	private BufferedImage filterApply(ServerCommand cmd, InputStream is)
			throws IOException {
		
		ImageFilter imageFilter = new ImageFilter();
		BufferedImage bufferedImage = ImageIO.read(is);
		
		is.close();
		is = null;
		
		if (cmd.getFilter().equals("Cartoonizer")) {
			
			BufferedImage bitmapLayer = imageFilter
					.greyScaleImage(bufferedImage);
			BufferedImage bitmapPic = imageFilter.invertColors(bitmapLayer);
			double[][] maskFilter = { { 1, 2, 1 }, { 2, 4, 2 }, { 1, 2, 1 } };
			bitmapPic = imageFilter.filterApply(bitmapPic, maskFilter,
					1.0 / 16.020, 0.0);
			bitmapPic = imageFilter.colorDodgeBlendOptimized(bitmapPic,
					bitmapLayer);
			
			bitmapLayer = null;
			
			return bitmapPic;
		} else if (cmd.getFilter().equals("Sharpen")) {
			
			double filtro[][] = { { -1, -1, -1 }, { -1, 9, -1 }, { -1, -1, -1 } };
			double factor = 1.0, bias = 0.0;
			
			return imageFilter.filterApply(bufferedImage, filtro, factor, bias);
			
		} else {
			
			BufferedImage mapFilter = null;
			
			if (cmd.getFilter().equals("Red Ton")) {
				mapFilter = ImageIO.read(getClass().getResourceAsStream(
						Main.packagePath + "map1.png"));
			} else if (cmd.getFilter().equals("Yellow Ton")) {
				mapFilter = ImageIO.read(getClass().getResourceAsStream(
						Main.packagePath + "map2.png"));
			} else if (cmd.getFilter().equals("Blue Ton")) {
				mapFilter = ImageIO.read(getClass().getResourceAsStream(
						Main.packagePath + "map3.png"));
			}
			
			return imageFilter.mapFilter(bufferedImage, mapFilter);
		}
	}
	
	private void sentImage(BufferedImage image, OutputStream os)
			throws IOException {
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(1.0f);
		
		// seta uma saida...
		writer.setOutput(new MemoryCacheImageOutputStream(os));
		
		// escreve a imagem...
		writer.write(image);
	}
	
}
