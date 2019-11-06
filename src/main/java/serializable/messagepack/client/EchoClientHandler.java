package serializable.messagepack.client;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import serializable.messagepack.UserInfo;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class EchoClientHandler extends ChannelHandlerAdapter {

    private final int sendNumber;

    EchoClientHandler(int sendNumber) {
        this.sendNumber = sendNumber;
    }

    /**
     * 当客户端返回应答消息时，channelRead方法会被调用
     * 从ByteBuf中读取并打印应答消息
     *
     * @param ctx
     *            ChannelHandlerContext
     * @param msg
     *            Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Client receive the messagePack message : " + msg);
    }

    /**
     * 当客户端和服务器端TCP链路建立成功之后，Netty的NIO线程会调用channelActive方法
     * 发送查询时间的指令给服务器端，调用ChannelHandlerContext的writeAndFlush方法将请求消息发送给服务器
     *
     * @param ctx
     *            ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // //发送sendNumber个UserInfo给服务器，由于启用了粘包/拆包支持，所以这里连续发送多个也不会出现粘包的现象。
        for (int i = 0; i < sendNumber; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setAge(i);
            userInfo.setUserName("ABCDEFG ---->" + i);
            ctx.write(userInfo);
        }
        ctx.flush();
        System.out.println("-----------------send over-----------------" + System.currentTimeMillis());
    }

    /**
     * 发生异常时，打印异常日志，释放客户端资源
     *
     * @param ctx
     *            ChannelHandlerContext
     * @param cause
     *            Throwable
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("error");
    }
}
