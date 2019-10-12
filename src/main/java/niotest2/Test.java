package niotest2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-08
 **/
public class Test {
    public static void main(String[] args) throws IOException {
        RandomAccessFile aFile     = new RandomAccessFile("data/nio-data.txt", "rw");
        FileChannel channel = aFile.getChannel();

        // Reading Data from a FileChannel
        ByteBuffer buf = ByteBuffer.allocate(48);
        int bytesRead = channel.read(buf);

        // Writing Data to a FileChannel
        String newData = "New String to write to file..." + System.currentTimeMillis();
        buf.clear();
        buf.put(newData.getBytes());
        buf.flip();

        while(buf.hasRemaining()) {
            channel.write(buf);
        }

        channel.close();

        long fileSize = channel.size();
    }
}
