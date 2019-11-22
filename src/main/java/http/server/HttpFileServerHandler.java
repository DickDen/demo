package http.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9.]*");

	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	private final String url;

	HttpFileServerHandler(String url) {
		this.url = url;
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		// 对HTTP请求消息的解码结果进行判断
		if (!request.getDecoderResult().isSuccess()) {
			// 如果解码失败构造HTTP 400错误返回
			sendError(ctx, BAD_REQUEST);
			return;
		}
		// 如果不是从浏览器或者表单设置为GET发起的请求就构造HTTP 405错误返回
		if (request.getMethod() != HttpMethod.GET) {
			sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}
		final String uri = request.getUri();
		final String path = sanitizeUri(uri);
		// 如果构造的路径不合法就返回HTTP 403错误
		if (path == null) {
			sendError(ctx, FORBIDDEN);
			return;
		}
		// 使用新的URI路径构造File对象，如果是文件不存在或是隐藏文件就返回HTTP 404错误
		File file = new File(path);
		if (file.isHidden() || !file.exists()) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		// 如果是目录就发送目录的链接给客户端
		if (file.isDirectory()) {
			if (uri.endsWith("/")) {
				sendListing(ctx, file);
			} else {
				sendRedirect(ctx, uri + "/");
			}
			return;
		}
		// 如果用户在浏览器上点击超链接直接打开或者下载文件，文件不合法返回HTTP 403错误
		if (!file.isFile()) {
			sendError(ctx, FORBIDDEN);
			return;
		}
		RandomAccessFile randomAccessFile;
		try {
			// 使用随机文件读写类以只读的方式打开文件，如果打开失败返回HTTP 404错误
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		// 获取文件的长度，构造成功的HTTP应答消息，在消息头中设置contentLength和contentType
		long fileLength = randomAccessFile.length();
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		setContentLength(response, fileLength);
		setContentTypeHeader(response, file);
		// 判断是否是keepAlive，如果是就在响应头中设置CONNECTION为keepAlive
		if (isKeepAlive(request)) {
			response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}
		// 发送响应消息
		ctx.write(response);
		// 通过Netty的ChunkedFile对象直接将文件写入到发送缓冲区中
		ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
		// 为sendFileFuture添加监听器，如果发送完成打印发送完成的日志
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {

			@Override
			public void operationComplete(ChannelProgressiveFuture future) {
				System.out.println("Transfer complete.");
			}

			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) {
					System.err.println("Transfer progress: " + progress);
				} else {
					System.err.println("Transfer progress: " + progress + "/" + total);
				}
			}
		});
		// 如果使用chunked编码，最后需要发送一个编码结束的空消息体，将LastHttpContent.EMPTY_LAST_CONTENT发送到缓冲区中
		// 标识示所有的消息体已经发送完成，同时调用flush方法将之前在发送缓冲区的消息刷新到SocketChannel中发送发送给对方
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		// 如果是非keepAlive的，最后一包消息发送完成后，服务端要主动关闭连接
		if (!isKeepAlive(request)) {
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private String sanitizeUri(String uri) {
		try {
			// 使用JDK的URLDecoder对URL进行解码，使用UTF-8字符集
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (Exception e) {
			try {
				// 解码失败就使用ISO-8859-1进行解码
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (Exception e2) {
				// 仍然失败就返回错误
				throw new Error();
			}
		}
		// 解码成功后对uri进行合法性判断，URI与允许访问的URI一致或者是其子目录（文件），则合法
		if (!uri.startsWith(url)) {
			return null;
		}
		if (!uri.startsWith("/")) {
			return null;
		}
		// 将硬编码的文件路径分隔符替换为本地操作系统的文件路径分隔符
		uri = uri.replace('/', File.separatorChar);
		// 对新的URI做二次合法性校验
		if (uri.contains(File.separator + ".") || uri.contains('.' + File.separator) || uri.startsWith(".") || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
			return null;
		}
		// 使用当前运行程序所在的工程目录+URI构造绝对路径
		return System.getProperty("user.dir") + File.separator + uri;
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void sendListing(ChannelHandlerContext ctx, File file) {
		// 创建成功的http响应消息
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		// 设置消息头的类型是html文件，不要设置为text/plain，客户端会当做文本解析
		response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
		// 构造返回的html页面内容：展示根目录下的所有文件和文件夹，同时使用超链接来标识
		StringBuilder buf = new StringBuilder();
		String dirPath = file.getPath();
		buf.append("<!DOCTYPE html>\r\n");
		buf.append("<html><head><title>");
		buf.append(dirPath);
		buf.append("目录：");
		buf.append("</title></head><body>\r\n");
		buf.append("<h3>");
		buf.append(dirPath).append("目录：");
		buf.append("</h3>\r\n");
		buf.append("<ul>");
		buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
		for (File f : Objects.requireNonNull(file.listFiles())) {
			if (f.isHidden() || !f.canRead()) {
				continue;
			}
			String name = f.getName();
			if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
				continue;
			}
			buf.append("<li>链接：<a href=\"");
			buf.append(name);
			buf.append("\">");
			buf.append(name);
			buf.append("</a></li>\r\n");
		}
		buf.append("</ul></body></html>\r\n");
		// 分配消息缓冲对象
		ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
		// 将缓冲区中的响应消息存放到HTTP应答消息中，并释放缓冲区
		response.content().writeBytes(buffer);
		buffer.release();
		// 调用writeAndFlush将响应消息发送到缓冲区并刷新到SocketChannel中
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
		response.headers().set(HttpHeaders.Names.LOCATION, newUri);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimetypesTypeMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE, mimetypesTypeMap.getContentType(file.getPath()));
	}
}
