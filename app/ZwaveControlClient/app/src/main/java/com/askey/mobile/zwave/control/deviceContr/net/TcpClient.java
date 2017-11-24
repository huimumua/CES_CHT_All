package com.askey.mobile.zwave.control.deviceContr.net;

import com.askey.mobile.zwave.control.util.Logg;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * TCP Socket客户端
 *
 */
public abstract class TcpClient implements Runnable {
	private static String TAG = "TcpClient";
	private int port;
	private String hostIP;
	private boolean connect = false;
	private SocketTransceiver transceiver;
	private static ArrayList <TCPReceive> meaasgemap = new ArrayList<TCPReceive>();

	public static TcpClient getInstance() {
		return SingleInstanceHolder.sInstance;
	}
	private static class SingleInstanceHolder {
		private static TcpClient sInstance = new TcpClient() {
			@Override
			public void onConnect(SocketTransceiver transceiver) {
                meaasgemap.get(0).onConnect(transceiver);
				Logg.i(TAG,"===onConnect==");
			}

			@Override
			public void onConnectFailed() {
				Logg.i(TAG,"===onConnectFailed==");
//                meaasgemap.get(0).onConnectFailed();
			}

			@Override
			public void onReceive(SocketTransceiver transceiver, String s) {
				Logg.i(TAG,"===onReceive=Message="+s);
				meaasgemap.get(0).receiveMessage(transceiver,s);
			}

			@Override
			public void onDisconnect(SocketTransceiver transceiver) {
				Logg.i(TAG,"===onDisconnect==");
//                meaasgemap.get(0).onDisconnect(transceiver);
			}
		};
	}




	public void rigister(TCPReceive callback){
		meaasgemap.add(callback);
	}

	public void unrigister(TCPReceive callback){
		meaasgemap.remove(callback);
	}

	/**
	 * 建立连接
	 * <p>
	 * 连接的建立将在新线程中进行
	 * <p>
	 * 连接建立成功，回调{@code onConnect()}
	 * <p>
	 * 连接建立失败，回调{@code onConnectFailed()}
	 * 
	 * @param hostIP
	 *            服务器主机IP
	 * @param port
	 *            端口
	 */
	public void connect(String hostIP, int port) {
		this.hostIP = hostIP;
		this.port = port;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			Socket socket = new Socket(hostIP, port);
			transceiver = new SocketTransceiver(socket) {

				@Override
				public void onReceive(InetAddress addr, String s) {
					TcpClient.this.onReceive(this, s);
				}

				@Override
				public void onDisconnect(InetAddress addr) {
					connect = false;
					TcpClient.this.onDisconnect(this);
				}
			};
			transceiver.start();
			connect = true;
			this.onConnect(transceiver);
		} catch (Exception e) {
			e.printStackTrace();
			this.onConnectFailed();
		}
	}

	/**
	 * 断开连接
	 * <p>
	 * 连接断开，回调{@code onDisconnect()}
	 */
	public void disconnect() {
		if (transceiver != null) {
			transceiver.stop();
			transceiver = null;
		}
	}

	/**
	 * 判断是否连接
	 * 
	 * @return 当前处于连接状态，则返回true
	 */
	public boolean isConnected() {
		return connect;
	}

	/**
	 * 获取Socket收发器
	 * 
	 * @return 未连接则返回null
	 */
	public SocketTransceiver getTransceiver() {
		return isConnected() ? transceiver : null;
	}

	/**
	 * 连接建立
	 * 
	 * @param transceiver
	 *            SocketTransceiver对象
	 */
	public abstract void onConnect(SocketTransceiver transceiver);

	/**
	 * 连接建立失败
	 */
	public abstract void onConnectFailed();

	/**
	 * 接收到数据
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param transceiver
	 *            SocketTransceiver对象
	 * @param s
	 *            字符串
	 */
	public abstract void onReceive(SocketTransceiver transceiver, String s);

	/**
	 * 连接断开
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param transceiver
	 *            SocketTransceiver对象
	 */
	public abstract void onDisconnect(SocketTransceiver transceiver);
}
