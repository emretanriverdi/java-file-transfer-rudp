package sender;

import io.IOExecuter;
import io.IOFactory;
import io.IOManager;

import java.net.SocketTimeoutException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.io.File;
import java.net.DatagramSocket;

public class SenderManager implements Runnable {
    private String destinationIP;
    private DatagramSocket socket;
    private File file;
    private byte[] fileData;
    private int seqNo, packageNo;
    private SenderInterface listener;
    private boolean continuation;

    public void setFileSenderListener(final SenderInterface l) {
        this.listener = l;
    }
    public SenderManager(final String destinationIP, final File file) throws IOException {
        this.destinationIP = destinationIP;
        this.file = file;
        this.fileData = IOFactory.getDestinationSource(file);
        this.seqNo = 0;
        this.packageNo = (int) Math.ceil(this.fileData.length / 1024.0);
        this.continuation = false;
        this.socket = new DatagramSocket(26000);
    }

    @Override
    public void run() {
        this.packageWait();
        final int response = this.initialize();
        if (response == 1) {
            return;
        }
        this.seqNo = 0;
        this.continuation = true;
        this.listener.fileSent(0);
        try {
            this.socket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[1024];
        while (this.seqNo < this.packageNo && this.continuation) {
            int length = data.length;
            if (this.seqNo == this.packageNo - 1) {
                length = this.fileData.length - this.seqNo * 1024;
                data = new byte[length];
            }
            System.arraycopy(this.fileData, this.seqNo * 1024, data, 0, length);
            this.senderWait(data);
            this.obtain();
        }
        this.finish();
    }

    private void packageWait() {
        final String initMsg = this.file.getName() + "|" + this.fileData.length;
        final byte[] data = initMsg.getBytes();
        try {
            final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.destinationIP), 26100);
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int initialize() {
        final byte[] buffer = new byte[64];
        try {
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
            } catch (SocketTimeoutException ste) {
                this.listener.errorHappened(3);
                return 3;
            } catch (SocketException se) {
                return -1;
            }
            final String msg = new String(packet.getData()).trim();
            if (msg.equals("x_ACCEPT_x"))
                return 0;
            if (msg.equals("x_REFUSE_x")) {
                this.listener.errorHappened(1);
                return 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private synchronized void senderWait(final byte[] bytes) {
        final byte[] seqBytes = IOManager.getBytes(this.seqNo);
        final byte[] lengthBytes = IOManager.getBytes(bytes.length);
        final byte[] data = IOExecuter.execute(seqBytes, lengthBytes, bytes);
        try {
            final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.destinationIP), 26100);
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean obtain() {
        try {
            while (true) {
                final byte[] seqBytes = new byte[4];
                final DatagramPacket packet = new DatagramPacket(seqBytes, seqBytes.length);
                try {
                    this.socket.receive(packet);
                } catch (SocketTimeoutException | SocketException ste) {
                    return false;
                }
                final int receivedSeqNo = IOManager.getBytes(packet.getData());
                if (receivedSeqNo == this.seqNo) {
                    ++this.seqNo;
                    this.listener.fileSent((int) (100.0 * (this.seqNo / this.packageNo)));
                    return true;
                }
                if (receivedSeqNo == -1) {
                    this.continuation = false;
                    this.listener.errorHappened(2);
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isContinuation() {
        return this.continuation;
    }

    public void send() {
        new Thread(this).start();
    }

    public void finish() {
        this.continuation = false;
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
    }

    public void decline() {
        if (!this.continuation) {
            return;
        }
        this.seqNo = -1;
        this.senderWait(new byte[0]);
        this.continuation = false;
    }
}
