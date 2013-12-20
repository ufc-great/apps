package br.ufc.mdcc.benchimage.util;

import java.io.Serializable;

/**
 * @author Philipp
 */
public final class ServerCommand implements Serializable {

	private static final long serialVersionUID = -3727321327320977036L;

	private String filter;
	private long size;

	public ServerCommand(String filter, long size) {
		this.filter = filter;
		this.size = size;
	}

	public ServerCommand() {
	}

	public final String getFilter() {
		return filter;
	}

	public final void setFilter(String filter) {
		this.filter = filter;
	}

	public final long getSize() {
		return size;
	}

	public final void setSize(long size) {
		this.size = size;
	}
}
