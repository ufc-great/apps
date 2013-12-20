package br.ufc.mdcc.netester.util;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import br.ufc.mdcc.mpos.net.model.Network;

public final class ParcelableNetwork implements Parcelable {
	private Network network;
	
	public ParcelableNetwork(Network network) {
		super();
		this.network = network;
	}
	
	//onde ler deserializa o Parcel
	private ParcelableNetwork(Parcel in){
		this.network = new Network();
		network.setJitter(in.readInt());
		network.setLossPacket(in.readInt());
		network.setBandwidthDownload(in.readString());
		network.setBandwidthUpload(in.readString());
		
		long pingResult[] = new long[7];
		in.readLongArray(pingResult);
		network.setResultPingTcp(pingResult);
		pingResult = new long[7];
		in.readLongArray(pingResult);
		network.setResultPingUdp(pingResult);
		
		network.setType(in.readString());
		network.setDate(new Date(in.readLong()));
		network.setType(in.readString());
	}
	
	public Network getNetwork() {
		return network;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(network.getJitter());
		dest.writeInt(network.getLossPacket());
		dest.writeString(network.getBandwidthDownload());
		dest.writeString(network.getBandwidthUpload());
		dest.writeLongArray(network.getResultPingTcp());
		dest.writeLongArray(network.getResultPingUdp());
		dest.writeString(network.getType());
		dest.writeLong(network.getDate().getTime());
		dest.writeString(network.getType());
	}

	public static final Parcelable.Creator<ParcelableNetwork> CREATOR = new Parcelable.Creator<ParcelableNetwork>() {
		public ParcelableNetwork createFromParcel(Parcel in) {
			return new ParcelableNetwork(in);
		}

		public ParcelableNetwork[] newArray(int size) {
			return new ParcelableNetwork[size];
		}
	};

}
