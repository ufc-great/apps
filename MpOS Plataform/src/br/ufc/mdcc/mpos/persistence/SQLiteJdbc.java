package br.ufc.mdcc.mpos.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

/**
 * @author Philipp
 */
public abstract class SQLiteJdbc {
	
	protected final Logger logger;
	protected Semaphore mutex = new Semaphore(1);
	
	protected SQLiteJdbc(Class<?> cls) {
		logger = Logger.getLogger(cls);
	}
	
	protected Connection openNewConnection() {
		Connection conn = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:mpos.data");
			
			conn.setAutoCommit(false);
			
			logger.debug("Banco de dados Conectado com sucesso");
			
			return conn;
			
		} catch (ClassNotFoundException e) {
			logger.error("Classe do sqlite não encontrada", e);
		} catch (SQLException e) {
			logger.error("Ao abrir banco de dados", e);
		}
		
		return null;
	}
	
	protected void closeConnection(Connection conn) {
		try {
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			logger.error("Ao comitar e fechar o banco de dados", e);
		}
	}
	
	protected void closeStatement(Statement stmt) {
		try {
			stmt.close();
		} catch (SQLException e) {
			logger.error("Ao fechar o Statement banco de dados", e);
		}
	}
}
