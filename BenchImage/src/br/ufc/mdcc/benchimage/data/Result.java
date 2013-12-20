package br.ufc.mdcc.benchimage.data;

import java.util.Date;

import android.graphics.Bitmap;

/**
 * @author Philipp
 */
public final class Result {

	private int id;
	private Date date;
	private long executionCPUTime;
	private long uploadTime;
	private long donwloadTime;
	private long totalTime;
	private String uploadTx, downloadTx;
	private Bitmap bitmap;

	private Config config;

	public Result() {
		config = new Config();
		date = new Date();
		uploadTx = "0.00";
		downloadTx = "0.00";
	}

	public final int getId() {
		return id;
	}

	public final void setId(int id) {
		this.id = id;
	}

	public final Date getDate() {
		return date;
	}

	public final void setDate(Date date) {
		this.date = date;
	}

	public final long getExecutionCPUTime() {
		return executionCPUTime;
	}

	public final void setExecutionCPUTime(long executionTime) {
		this.executionCPUTime = executionTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public final Config getConfig() {
		return config;
	}

	public final void setConfig(Config config) {
		this.config = config;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public long getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(long uploadTime) {
		this.uploadTime = uploadTime;
	}

	public long getDonwloadTime() {
		return donwloadTime;
	}

	public void setDonwloadTime(long donwloadTime) {
		this.donwloadTime = donwloadTime;
	}

	public String getUploadTx() {
		return uploadTx;
	}

	public void setUploadTx(String uploadTx) {
		this.uploadTx = uploadTx;
	}

	public String getDownloadTx() {
		return downloadTx;
	}

	public void setDownloadTx(String downloadTx) {
		this.downloadTx = downloadTx;
	}

	@Override
	public String toString() {
		StringBuilder print = new StringBuilder();

		print.append("filtro: ").append(config.getFilter()).append("\n");
		print.append("img: ").append(config.getImage()).append("\n");
		print.append("executionCPUTime: ").append(executionCPUTime).append("\n");
		print.append("uploadTime: ").append(uploadTime).append("\n");
		print.append("downloadTime: ").append(donwloadTime).append("\n");
		print.append("uploadTx: ").append(uploadTx).append("\n");
		print.append("downloadTx: ").append(downloadTx).append("\n");
		print.append("TotalTime: ").append(totalTime).append("\n");

		return print.toString();
	}

}
