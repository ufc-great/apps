package br.ufc.mdcc.benchimage.task;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import br.ufc.mdcc.benchimage.data.Config;
import br.ufc.mdcc.benchimage.data.Result;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;

/**
 * @author Philipp
 */
public final class BitmapProcessLocalTask extends BitmapProcessTask {

	private final String LOG = BitmapProcessLocalTask.class.getSimpleName();

	public BitmapProcessLocalTask(TaskResultAdapter<Result> results, String outputDir, Context context, Config config) {
		super(results, outputDir, context, config);
	}

	// executa uma decodificação para reduzir a foto
	@Override
	protected Result doInBackground(Void... params) {

		try {
			if (config.getFilter().equals("Benchmark")) {
				Log.i(LOG, "Iniciou Benchmark do Aplicativo");

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

				Result result = applyFilter(config.getFilter(), false);

				if (!result.getConfig().getFilter().equals("Original")) {
					performanceDao.add(result);
				}

				return result;
			}
		} catch (Exception e) {
			Log.i(LOG, "Deu algum error", e);
		}

		return null;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Result applyFilter(String filter, boolean bench) throws Exception {
		Log.i(LOG, "Iniciou aplicação do filtro " + filter);

		if (!bench) {
			publishProgress("Iniciou " + filter);
		}

		Result result = new Result();
		config.setFilter(filter);
		result.setConfig(config);

		// image path for size
		String imagePath = sizeToPath(config.getSize());

		// initial time
		long initialTime = System.currentTimeMillis();
		long endTime = 0L;

		// compose filename
		String fileName = photoFileName(filter);

		// made Bitmap mutable
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			options.inMutable = true;
		}

		Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(imagePath + config.getImage()), null, options);

		// process image by filter options
		if (filter.equals("Original")) {

			Bitmap bitmapOriginal = imageFilter.decodeSampledBitmapFromResource(context.getAssets().open(imagePath + config.getImage()), config.getSize());

			result.setTotalTime(System.currentTimeMillis() - initialTime);
			result.setBitmap(bitmapOriginal);

			if (!bench) {
				publishProgress("Recarregou Imagem!");
			} else {
				Log.i(LOG, "Recarregou Imagem!");
			}

			return result;

		} else if (filter.equals("Cartoonizer")) {

			// receita
			/*
			 * s = Read-File-Into-Image("/path/to/image") g =
			 * Convert-To-Gray-Scale(s) i = Invert-Colors(g) b =
			 * Apply-Gaussian-Blur(i) result = ColorDodgeBlend(b,g)
			 */

			if (!bench) {
				publishProgress("Aplicando GreyScale");
			} else {
				Log.i(LOG, "Aplicando GreyScale");
			}

			Bitmap bitmapLayer = imageFilter.greyScaleImage(bitmap);

			bitmap = null;
			System.gc();

			if (!bench) {
				publishProgress("Aplicando InvertColors");
			} else {
				Log.i(LOG, "Aplicando InvertColors");
			}

			Bitmap bitmapPic = imageFilter.invertColors(bitmapLayer);

			if (!bench) {
				publishProgress("Aplicando Gaussian Blur");
			} else {
				Log.i(LOG, "Aplicando Gaussian Blur");
			}

			double[][] maskFilter = { { 1, 2, 1 }, { 2, 4, 2 }, { 1, 2, 1 } };
			bitmapPic = imageFilter.filterApply(bitmapPic, maskFilter, 1.0 / 16.020, 0.0);

			if (!bench) {
				publishProgress("Finalizando Cartoonizer");
			} else {
				Log.i(LOG, "Finalizando Cartoonizer");
			}
			bitmapPic = imageFilter.colorDodgeBlendOptimized(bitmapPic, bitmapLayer);

			result.setExecutionCPUTime(System.currentTimeMillis() - initialTime);
			saveResult(bitmapPic, fileName);
			if (!bench) {
				publishProgress("Salvou imagem");
			} else {
				Log.i(LOG, "Salvou imagem");
			}
			// termina o calculo do tempo de execução
			endTime = System.currentTimeMillis() - initialTime;

			bitmapPic = null;
			System.gc();

		} else if (filter.equals("Sharpen")) {

			if (!bench) {
				publishProgress("Aplicando Sharpen");
			} else {
				Log.i(LOG, "Aplicando Sharpen");
			}

			// config
			double filtro[][] = { { -1, -1, -1 }, { -1, 9, -1 }, { -1, -1, -1 } };
			double factor = 1.0, bias = 0.0;

			bitmap = imageFilter.filterApply(bitmap, filtro, factor, bias);
			if (!bench) {
				publishProgress("Terminou Sharpen");
			} else {
				Log.i(LOG, "Terminou Sharpen");
			}

			result.setExecutionCPUTime(System.currentTimeMillis() - initialTime);
			saveResult(bitmap, fileName);
			if (!bench) {
				publishProgress("Salvou imagem");
			} else {
				Log.i(LOG, "Salvou imagem");
			}
			endTime = System.currentTimeMillis() - initialTime;

			bitmap = null;
			System.gc();

		} else {

			// map filters!

			Bitmap mapFilter = null;

			if (!bench) {
				publishProgress("Aplicando " + filter);
			} else {
				Log.i(LOG, "Aplicando " + filter);
			}

			if (filter.equals("Red Ton")) {
				mapFilter = BitmapFactory.decodeStream(context.getAssets().open("filters/map1.png"));
			} else if (filter.equals("Blue Ton")) {
				mapFilter = BitmapFactory.decodeStream(context.getAssets().open("filters/map3.png"));
			} else if (filter.equals("Yellow Ton")) {
				mapFilter = BitmapFactory.decodeStream(context.getAssets().open("filters/map2.png"));
			}

			bitmap = imageFilter.mapFilter(bitmap, mapFilter);
			if (!bench) {
				publishProgress("Terminou " + filter);
			} else {
				Log.i(LOG, "Terminou " + filter);
			}
			result.setExecutionCPUTime(System.currentTimeMillis() - initialTime);

			saveResult(bitmap, fileName);
			if (!bench) {
				publishProgress("Salvou imagem");
			} else {
				Log.i(LOG, "Salvou imagem");
			}
			endTime = System.currentTimeMillis() - initialTime;

			bitmap = null;
			System.gc();
		}

		bitmap = imageFilter.decodeSampledBitmapFromResource(new FileInputStream(new File(outputDir, fileName)), config.getSize());
		result.setBitmap(bitmap);
		result.setTotalTime(endTime);

		if (!bench) {
			publishProgress("Terminou Processamento!");
		}

		return result;
	}
}
