package br.ufc.mdcc.benchimage.task;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import br.ufc.mdcc.benchimage.data.Config;
import br.ufc.mdcc.benchimage.data.Result;
import br.ufc.mdcc.benchimage.util.ServerCommand;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;

/**
 * @author Philipp
 */
public final class BitmapProcessRemoteTask extends BitmapProcessTask {

	private String ip;
	private int port = 35000;

	private final String LOG = BitmapProcessRemoteTask.class.getSimpleName();

	public BitmapProcessRemoteTask(TaskResultAdapter<Result> results, String outputDir, Context context, Config config, String ip) {
		super(results, outputDir, context, config);
		this.ip = ip;
	}

	@Override
	protected Result doInBackground(Void... params) {

		try {
			if (config.getFilter().equals("Benchmark")) {
				Log.i(LOG, "Iniciou Benchmark do Aplicativo");

				// bota os sizes...
				String sizes[] = { "8MP", "4MP", "2MP", "1MP", "0.3MP" };

				int count = 1;
				Result last = null;

				for (String size : sizes) {
					// executa 5 vezes para cada size.
					for (int i = 0; i < 5; i++) {
						publishProgress("Benchmark [" + count + "/25]");

						result.setDate(new Date());
						config.setSize(size);
						last = applyFilter("Cartoonizer", true);
						count++;

						total += endTime;

						performanceDao.add(last);
						// quando for o ultimo deixa a foto no bitmap
						if (count != 26) {
							last.setBitmap(null);
							last = null;
							System.gc();
						}

						// para da tempo o gc trabalhar...
						Thread.sleep(2000);
					}
					// um tempo entre um size e outro...
					Thread.sleep(3500);
				}

				// pq é para persistir os dados do Benchmark
				last.setTotalTime(total);
				last.setExecutionCPUTime(0L);
				last.setDonwloadTime(0L);
				last.setUploadTime(0L);
				last.setDownloadTx("0.00");
				last.setUploadTx("0.00");
				last.getConfig().setFilter("Benchmark");
				last.getConfig().setSize("Todos");

				performanceDao.add(last);

				System.gc();
				publishProgress("Benchmark Completo!");

				return last;
			} else {

				Result res = applyFilter(config.getFilter(), false);

				if (!res.getConfig().getFilter().equals("Original")) {
					performanceDao.add(res);
				}

				return res;

			}
		} catch (Exception e) {
			Log.i(LOG, "Deu algum error", e);
		}

		return null;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Result applyFilter(String filter, boolean bench) throws IOException, ClassNotFoundException {

		if (!bench) {
			publishProgress("Iniciou " + filter);
		}

		// seta o objeto de resultados
		config.setFilter(filter);
		result.setConfig(config);

		// image path for size
		String imagePath = sizeToPath(config.getSize());

		// initial time
		long initialTime = System.currentTimeMillis();

		// compose filename
		String fileName = photoFileName(filter);

		// made Bitmap mutable
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			options.inMutable = true;
		}

		// carrega a imagem selecionada dos assets
		InputStream inputStream = context.getAssets().open(imagePath + config.getImage());

		if (filter.equals("Original")) {

			Bitmap bitmapOriginal = imageFilter.decodeSampledBitmapFromResource(inputStream, config.getSize());

			result.setTotalTime(System.currentTimeMillis() - initialTime);
			result.setBitmap(bitmapOriginal);

			if (!bench) {
				publishProgress("Filtro Original, não pode ser enviado para o servidor!");
			} else {
				Log.i(LOG, "Filtro Original, não pode ser enviado para o servidor!");
			}

			return result;

		}

		ServerCommand serverCommand = new ServerCommand(config.getFilter(), context.getAssets().openFd(imagePath + config.getImage()).getDeclaredLength());

		// recebe os resultados pelo socket e salva na pasta temp
		// imediatamente...
		saveResult(remotableData(inputStream, serverCommand, bench), fileName);

		// OBS
		// endtime tempo total calculado pelo cliente de dispositivo móvel
		// inclui também persistir no arquivo no aparelho!
		endTime = System.currentTimeMillis() - initialTime;

		// ler do storage
		FileInputStream fis = new FileInputStream(new File(outputDir, fileName));

		// manda reduzir a foto para exibir na tela...
		Bitmap bitmap = imageFilter.decodeSampledBitmapFromResource(fis, config.getSize());
		result.setBitmap(bitmap);
		result.setTotalTime(endTime);

		if (!bench) {
			publishProgress("Terminou Processamento!");
		} else {
			Log.i(LOG, "Terminou Processamento!");
		}

		// System.out.println(result);

		return result;
	}

	private Bitmap remotableData(InputStream inputStream, ServerCommand serverCommand, boolean bench) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket connection = new Socket(ip, port);

		if (!bench) {
			publishProgress("Enviando Foto...");
		}

		ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
		oos.writeObject(serverCommand);

		// passagem da foto
		DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
		byte buffer[] = new byte[8 * 1024];
		int read = 0;
		while ((read = inputStream.read(buffer)) != -1) {
			dos.write(buffer, 0, read);
			dos.flush();
			Arrays.fill(buffer, (byte) 0);
		}

		if (!bench) {
			publishProgress("Esperando foto...");
		}

		// Bitmap bitmapResult = null;

		// ler apenas os 100 primeiros bytes do stream! para receber os dados de
		// upload
		read = connection.getInputStream().read(buffer, 0, 100);
		String uploadData = new String(buffer, 0, 100);
		Arrays.fill(buffer, (byte) 0);

		StringBuilder extract = new StringBuilder(uploadData);
		extract.delete(0, 12);
		String getUploadData[] = extract.toString().split(";");

		result.setUploadTx(getUploadData[0]);
		result.setUploadTime(Long.parseLong(getUploadData[1]));
		result.setExecutionCPUTime(Long.parseLong(getUploadData[2]));// quanto
																		// tempo
																		// a cpu
																		// remota
																		// gastou

		double fileDownloadSize = Double.parseDouble(getUploadData[3]);
		long downloadTime = 0L, totalSize = 0L;

		if (!bench) {
			publishProgress("Recebendo foto...");
		}

		ByteArrayOutputStream baos = null;

		// vai receber apenas a foto do servidor!!!
		if (uploadData.contains("uploadData:")) {

			baos = new ByteArrayOutputStream(2 * 1024 * 1024);
			long timeInit = System.currentTimeMillis();
			while ((read = connection.getInputStream().read(buffer)) != -1) {
				baos.write(buffer, 0, read);
				totalSize += read;

				if (fileDownloadSize == totalSize) {
					Arrays.fill(buffer, (byte) 0);
					baos.flush();
					break;
				}
			}

			downloadTime = System.currentTimeMillis() - timeInit;

			// long timeInit = System.currentTimeMillis();
			// bitmapResult =
			// BitmapFactory.decodeStream(connection.getInputStream());
			// result.setDonwloadTime(System.currentTimeMillis() - timeInit);
		}

		// TODO: tranformar KB/s para kbps (kilobit per second)
		// seta a taxa de download...
		result.setDownloadTx(String.format("%.3f", (fileDownloadSize / (((double) downloadTime / 1000.0) * 1024.0))));
		result.setDonwloadTime(downloadTime);

		connection.close();

		byte data[] = baos.toByteArray();
		baos.close();

		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
}
