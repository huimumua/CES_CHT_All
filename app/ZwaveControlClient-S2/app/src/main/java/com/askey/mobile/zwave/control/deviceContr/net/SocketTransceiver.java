package com.askey.mobile.zwave.control.deviceContr.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 * 
 */
public abstract class SocketTransceiver implements Runnable {

	protected Socket socket;
	protected InetAddress addr;
	protected BufferedReader in;
	protected PrintWriter out;
	private boolean runFlag;

	/**
	 * 实例化
	 * 
	 * @param socket
	 *            已经建立连接的socket
	 */
	public SocketTransceiver(Socket socket) {
		this.socket = socket;
		this.addr = socket.getInetAddress();
	}

	/**
	 * 获取连接到的Socket地址
	 * 
	 * @return InetAddress对象
	 */
	public InetAddress getInetAddress() {
		return addr;
	}

	/**
	 * 开启Socket收发
	 * <p>
	 * 如果开启失败，会断开连接并回调{@code onDisconnect()}
	 */
	public void start() {
		runFlag = true;
		new Thread(this).start();
	}

	/**
	 * 断开连接(主动)
	 * <p>
	 * 连接断开后，会回调{@code onDisconnect()}
	 */
	public void stop() {
		runFlag = false;
		try {
			socket.shutdownInput();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.onDisconnect(addr);
	}

	/**
	 * 发送字符串
	 * 
	 * @param s
	 *            字符串
	 * @return 发送成功返回true
	 */
	public void send(final String s) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (out != null) {
					try {
						out.println(s);
						out.flush();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * 监听Socket接收的数据(新线程中运行)
	 */
	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));
			out = new PrintWriter(
					this.socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
			runFlag = false;
		}
		while (runFlag) {
		/*	try {
				final String s = in.readUTF();
				this.onReceive(addr, s);
			} catch (IOException e) {
				// 连接被断开(被动)
				runFlag = false;
			}*/


			char []buffer;
			int leng = 0;
			try {
				buffer = new char[1024];
				if ((leng = in.read(buffer)) != -1) {
					CharBuffer b = CharBuffer.wrap(buffer, 0, leng);
					String s = b.toString();
					Log.i("TCPConnect", "========TCPConnect========" + s);
					this.onReceive(addr, s);
				}
			} catch (Exception e) {
				// 连接被断开(被动)
				Log.e("TCPConnect", "=====Exception======" + e.getMessage());
				runFlag = false;
			}
		}
		// 断开连接
		try {
			in.close();
			out.close();
			socket.close();
			in = null;
			out = null;
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.onDisconnect(addr);
	}

	/**
	 * 接收到数据
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param addr
	 *            连接到的Socket地址
	 * @param s
	 *            收到的字符串
	 */
	public abstract void onReceive(InetAddress addr, String s );

	/**
	 * 连接断开
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param addr
	 *            连接到的Socket地址
	 */
	public abstract void onDisconnect(InetAddress addr);






}
