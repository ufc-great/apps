package br.ufc.mdcc.benchimage.data;

import java.io.Serializable;

/**
 * @author Philipp
 */
public final class Config implements Serializable {

	private static final long serialVersionUID = -4952715397013448772L;
	private String local, image, filter, size;

	public Config() {

	}

	public Config(String local, String image, String filter, String size) {
		this.local = local;
		this.image = image;
		this.filter = filter;
		this.size = size;
	}

	public final String getLocal() {
		return local;
	}

	public final void setLocal(String local) {
		this.local = local;
	}

	public final String getImage() {
		return image;
	}

	public final void setImage(String image) {
		this.image = image;
	}

	public final String getFilter() {
		return filter;
	}

	public final void setFilter(String filter) {
		this.filter = filter;
	}

	public final String getSize() {
		return size;
	}

	public final void setSize(String size) {
		this.size = size;
	}

}
