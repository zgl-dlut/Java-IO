package com.anxpp.io.calculator.aio.client;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
public class AsyncClientHandler implements CompletionHandler<Void, AsyncClientHandler>, Runnable {
	private AsynchronousSocketChannel clientChannel;
	private String host;
	private int port;
	private CountDownLatch latch;
	public AsyncClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			//??????????
			clientChannel = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		//??CountDownLatch??
		latch = new CountDownLatch(1);
		//??????????????????????????????completed??
		clientChannel.connect(new InetSocketAddress(host, port), this, this);
		try {
			latch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			clientChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//???????
	//???TCP??????
	@Override
	public void completed(Void result, AsyncClientHandler attachment) {
		System.out.println("???????????...");
	}
	//???????
	@Override
	public void failed(Throwable exc, AsyncClientHandler attachment) {
		System.err.println("???????...");
		exc.printStackTrace();
		try {
			clientChannel.close();
			latch.countDown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//????????
	public void sendMsg(String msg){
		byte[] req = msg.getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		//???
		clientChannel.write(writeBuffer, writeBuffer,new WriteHandler(clientChannel, latch));
	}
}