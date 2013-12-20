package br.ufc.mdcc.benchimage.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.PowerManager;
import br.ufc.mdcc.benchimage.data.Config;
import br.ufc.mdcc.benchimage.data.PerformanceDAO;
import br.ufc.mdcc.benchimage.data.Result;
import br.ufc.mdcc.benchimage.util.ImageFilter;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;

/**
 * @author Philipp
 */
public abstract class BitmapProcessTask extends AsyncTask<Void, String, Result> {

	private TaskResultAdapter<Result> results;
	private PowerManager.WakeLock wakeLock;

	protected Config config;
	protected String outputDir;
	protected Context context;
	protected PerformanceDAO performanceDao;
	protected ImageFilter imageFilter;
	protected long total = 0L;
	
	protected Result result = new Result();
	protected long endTime = 0L;

	public BitmapProcessTask(TaskResultAdapter<Result> results, String outputDir, Context context, Config config) {
		this.results = results;
		this.outputDir = outputDir;
		this.context = context;
		this.config = config;

		performanceDao = new PerformanceDAO(context);
		imageFilter = new ImageFilter();
	}

	abstract Result applyFilter(String filter, boolean bench) throws Exception;
	
	@Override
	protected void onPreExecute() {
		preventSleep();
	}

	protected String sizeToPath(String size) {
		if (size.equals("8MP")) {
			return "images/8mp/";
		} else if (size.equals("6MP")) {
			return "images/6mp/";
		} else if (size.equals("4MP")) {
			return "images/4mp/";
		} else if (size.equals("2MP")) {
			return "images/2mp/";
		} else if (size.equals("1MP")) {
			return "images/1mp/";
		} else if (size.equals("0.7MP")) {
			return "images/0_7mp/";
		} else if (size.equals("0.3MP")) {
			return "images/0_3mp/";
		}

		return null;
	}

	protected String photoFileName(String filter) {

		StringBuilder sb = new StringBuilder();

		sb.append(config.getImage().replace(".jpg", "")).append("_").append(filter).append("_");

		if (config.getSize().equals("0.7MP")) {
			sb.append("0_7mp.jpg");
		} else if (config.getSize().equals("0.3MP")) {
			sb.append("0_3mp.jpg");
		} else {
			sb.append(config.getSize()).append(".jpg");
		}

		return sb.toString();
	}

	// salva no external storage do aparelho
	protected void saveResult(Bitmap bitmap, String fileName) throws IOException {
		OutputStream output = new FileOutputStream(new File(outputDir, fileName));

		// gera o arquivo da imagem com qualidade de 100 e JPEG
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
		output.flush();
		output.close();
	}

	@Override
	protected void onProgressUpdate(String... values) {
		results.taskOnGoing(0, values[0]);
	}

	// após finalizada a operação
	@Override
	protected void onPostExecute(Result result) {
		wakeLock.release();
		results.completedTask(result);
	}

	private void preventSleep() {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PicFilter CPU");
		wakeLock.acquire();
	}

}
