package br.ufc.mdcc.mpos.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import br.ufc.mdcc.mpos.model.Network;
import br.ufc.mdcc.mpos.model.NetworkResult;

/**
 * @author Philipp
 */
public final class NetProfileDao extends SQLiteJdbc {
	
	NetProfileDao() {
		super(NetProfileDao.class);
	}
	
	public void adicionar(NetworkResult results) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		String date = dateFormat.format(results.getNetwork().getDate());
		
		Statement stmt = null;
		Connection conn = null;
		
		try {
			mutex.acquire();
			
			conn = openNewConnection();
			stmt = conn.createStatement();
			
			StringBuilder sql = new StringBuilder();
			
			// foi add carrier, ip!
			sql.append("INSERT INTO netprofile (mobileId, carrier, deviceName, appName, latitude, longitude, date, ip, tcp, udp, loss, jitter, down, up, net) VALUES (");
			sql.append("'").append(results.getDevice().getMobileId()).append("'").append(",");
			sql.append("'").append(results.getDevice().getCarrier()).append("'").append(",");
			sql.append("'").append(results.getDevice().getDeviceName()).append("'").append(",");
			sql.append("'").append(results.getNetwork().getAppName()).append("'").append(",");
			sql.append("'").append(results.getDevice().getLatitude()).append("'").append(",");
			sql.append("'").append(results.getDevice().getLongitude()).append("'").append(",");
			
			sql.append("'").append(date).append("'").append(",");
			sql.append("'").append(results.getIp()).append("'").append(",");
			sql.append("'").append(Network.arrayToString(results.getNetwork().getResultPingTcp())).append("'").append(",");
			sql.append("'").append(Network.arrayToString(results.getNetwork().getResultPingUdp())).append("'").append(",");
			sql.append("'").append(results.getNetwork().getLossPacket()).append("'").append(",");
			sql.append("'").append(results.getNetwork().getJitter()).append("'").append(",");
			sql.append("'").append(results.getNetwork().getBandwidthDownload()).append("'").append(",");
			sql.append("'").append(results.getNetwork().getBandwidthUpload()).append("'").append(",");
			sql.append("'").append(results.getNetwork().getType()).append("')");
			
			// System.out.println(sql);
			
			stmt.executeUpdate(sql.toString());
			
			logger.debug("Persistiu com sucesso");
			
		} catch (SQLException e) {
			logger.error("Ao abrir banco de dados", e);
		} catch (InterruptedException e) {
			logger.error("Interrupção no semaforo mutex", e);
		} finally {
			closeStatement(stmt);
			closeConnection(conn);
			mutex.release();
		}
	}
}
