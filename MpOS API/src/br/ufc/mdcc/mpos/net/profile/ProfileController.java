package br.ufc.mdcc.mpos.net.profile;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import br.ufc.mdcc.mpos.config.Profile;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.model.Cloudlet;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.net.service.MulticastDnsService;
import br.ufc.mdcc.mpos.persistence.ProfileNetworkDAO;
import br.ufc.mdcc.mpos.util.TaskResult;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import br.ufc.mdcc.mpos.util.Util;

/**
 * Aonde vai executar boa parte da logica do framework!
 * 
 * @author hack
 */
public final class ProfileController {
	/**
	 * ServiceSingleton is loaded on the first execution of
	 * Singleton.getInstance() or the first access to ServiceSingleton.INSTANCE,
	 * not before.
	 */
	private static class ServiceSingleton {
		public static final ProfileController INSTANCE = new ProfileController();
	}

	private final String nameCloudletService = "srvCloudlet";
	private Thread discoveryCloudletService;
	private Cloudlet cloudlet;

	private Activity activity;
	private Profile profile;

	private ProfileController() {
		Log.i(ProfileController.class.getName(), "MpOS Profile SubSystem started!");
	}

	public static ProfileController getInstance() {
		return ServiceSingleton.INSTANCE;
	}

	public synchronized void setCloudlet(Cloudlet cloudlet) {
		this.cloudlet = cloudlet;
	}

	public synchronized boolean cloudletReady() {
		if (cloudlet != null) {
			return true;
		}

		return false;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Cloudlet getCloudlet() {
		return cloudlet;
	}

	public void discoveryCloudlet() {
		if (activity != null) {
			if (discoveryCloudletService != null) {
				try {
					discoveryCloudletService.join();
				} catch (InterruptedException e) {
				}
			}

			// esse procedimento server apenas para detectar principalmente o ip
			// do cloudlet
			// na rede local, apos isso a descoberta dos outros serviços é via
			// TCP, o motivo
			// disso é que o JmDNS não é um meio confiavel para fazer a
			// descoberta de vários
			// serviços na rede local...
			discoveryCloudletService = new Thread(new MulticastDnsService(activity, nameCloudletService));
			discoveryCloudletService.start();
		}
	}

	/**
	 * Realiza analise da rede usando o serviço do cloudlet
	 * 
	 * @throws NetworkException
	 *             - Não achou cloudlet na rede local ou não está pronto
	 * @throws MissedEventException
	 *             - Instancia de TaskResult não passado para Network Analysis
	 */
	public void networkAnalysis(TaskResultAdapter<Network> result) throws NetworkException, MissedEventException {
		if (cloudlet == null) {
			throw new NetworkException("Cloudlet didn't found!");
		}
		networkAnalysis(result, cloudlet.getIp());
	}

	/**
	 * Realiza analise da rede, baseado em serviços remotos definidos pelo
	 * usuario, onde possivelmente pode se encontrar o MpOS Cloudlet.
	 * 
	 * @param ip
	 *            - um IP do serviço remoto
	 * @throws NetworkException
	 *             - O endereço IP não é valido
	 * @throws MissedEventException
	 */
	public void networkAnalysis(final TaskResultAdapter<Network> result, String ip) throws NetworkException, MissedEventException {

		if (!Util.validateIpAddress(ip)) {
			throw new NetworkException("Invalid IP Address");
		}

		switch (profile) {
			case LIGHT:
				executeProfileNetworkTask(new ProfileNetworkLight(persistNetworkResults(result), ip));
				break;

			case DEFAULT:
				executeProfileNetworkTask(new ProfileNetworkDefault(persistNetworkResults(result), ip));
				break;

			case FULL:
				executeProfileNetworkTask(new ProfileNetworkFull(persistNetworkResults(result), ip));
				break;

			default:
				break;
		}
	}

	/**
	 * Manda persistir localmente os resultados do objeto Network, atraves da
	 * interceptação de uma interface que foi instancia fora do controlador.
	 * 
	 * @param interceptedResults
	 *            - resultados de um TaskResult<Network> interceptados para
	 *            persistencia local.
	 */
	private TaskResult<Network> persistNetworkResults(final TaskResultAdapter<Network> interceptedResults) {
		// retorna a instancia da Interface sendo interceptada
		return new TaskResultAdapter<Network>() {
			@Override
			public void completedTask(final Network network) {

				if (network != null) {
					// intercepta para persistencia local em paralelo
					new Thread(new Runnable() {
						@Override
						public void run() {
							ProfileNetworkDAO profileDao = new ProfileNetworkDAO(activity);
							profileDao.add(network);
						}
					}).start();
				}

				// continua o fluxo pela interface...
				interceptedResults.completedTask(network);
			}

			@Override
			public void taskOnGoing(int completed) {
				interceptedResults.taskOnGoing(completed);
			}
		};
	}

	/**
	 * Sem usar um thread pool, a AsyncTask executa serialmente, apenas na
	 * configuração do Android 3.0.x para cima, que executa em paralelo a
	 * AsyncTask.
	 * 
	 * @param networkTask
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void executeProfileNetworkTask(ProfileNetworkTask networkTask) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			networkTask.execute();
	}

}
