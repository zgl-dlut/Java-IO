package com.anxpp.io.calculator.nio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author zgl
 * @date 2020/1/14 下午4:54
 */
public class IOMultiple {
	public static void main(String[] args) {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8000));
			//不设置阻塞队列
			ssc.configureBlocking(false);

			Selector selector = Selector.open();
			// 注册 channel，并且指定感兴趣的事件是 Accept
			ssc.register(selector, SelectionKey.OP_ACCEPT);

			ByteBuffer readBuff = ByteBuffer.allocate(1024);
			ByteBuffer writeBuff = ByteBuffer.allocate(128);
			writeBuff.put("received".getBytes());
			writeBuff.flip();

			while (true) {
				int nReady = selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();

				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();

					if (key.isAcceptable()) {
						// 创建新的连接，并且把连接注册到selector上，而且，
						// 声明这个channel只对读操作感兴趣。
						SocketChannel socketChannel = ssc.accept();
						socketChannel.configureBlocking(false);
						socketChannel.register(selector, SelectionKey.OP_READ);
					}
					else if (key.isReadable()) {
						SocketChannel socketChannel = (SocketChannel) key.channel();
						readBuff.clear();
						socketChannel.read(readBuff);

						readBuff.flip();
						System.out.println("received : " + new String(readBuff.array()));
						key.interestOps(SelectionKey.OP_WRITE);
					}
					else if (key.isWritable()) {
						writeBuff.rewind();
						SocketChannel socketChannel = (SocketChannel) key.channel();
						socketChannel.write(writeBuff);
						key.interestOps(SelectionKey.OP_READ);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 创建一个 ServerSocketChannel, 和一个 Selector, 并且把这个 server channel 注册到 selector 上, 注册的时间指定,
	 * 这个 channel 所感觉兴趣的事件是 SelectionKey.OP_ACCEPT, 这个事件代表的是有客户端发起TCP连接请求.
	 *
	 * 使用 select 方法阻塞住线程, 当 select 返回的时候, 线程被唤醒. 再通过 selectedKeys 方法得到所有可用 channel 的集合.
	 *
	 * 遍历这个集合, 如果其中 channel 上有连接到达, 就接受新的连接, 然后把这个新的连接也注册到 selector 中去.
	 *
	 *  如果有 channel 是读, 那就把数据读出来, 并且把它感兴趣的事件改成写. 如果是写, 就把数据写出去, 并且把感兴趣的事件改成读.
	 *
	 * Selector.open 在不同的系统里实现方式不同
	 * sunOS 使用 DevPollSelectorProvider, Linux就会使用 EPollSelectorProvider, 而默认则使用 PollSelectorProvider
	 *
	 *  也就是说 selector.select() 用来阻塞线程, 直到一个或多个 channle 进行 io 操作. 比如 SelectionKey.OP_ACCEPT.
	 * 然后使用 selector.selectedKeys() 方法获取出, 这些通道.
	 *
	 * 那么 selector.select() 是怎么直到已经有 io 操作了呢?
	 * 原因是因为 poll
	 */
}