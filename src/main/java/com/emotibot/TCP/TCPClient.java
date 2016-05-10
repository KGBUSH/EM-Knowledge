package com.emotibot.TCP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

//com.emotibot.TCP.TCPClient
public class TCPClient {
	private String ip = "";
	private int port = 0;

	public TCPClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String Transmit(String reqMessage) throws Exception {
		String result = "";
		try{
		Socket socket = new Socket(ip, port);
		BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter os = new PrintWriter(socket.getOutputStream());
		BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    os.println(reqMessage);
		os.flush();
		result=is.readLine();
		os.close(); // 关闭Socket输出流
		is.close(); // 关闭Socket输入流
		socket.close(); // 关闭Socket
		System.out.println("Server:" + result);
		}catch(Exception e)
		{
		  e.printStackTrace();
		}
		return result;
	}

	public static void main(String args[]) throws Exception {
		TCPClient tcp = new TCPClient("192.168.1.73", 16413);
		tcp.Transmit("清华同方上海交大");
	}
}
