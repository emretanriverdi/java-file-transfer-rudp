package obtainer;

import io.IOFactory;
import io.IOManager;

import java.net.InetAddress;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.io.File;

public class ObtainerManager implements Runnable {
    private String sourceIP;
    private File name;
    private byte[] fileData;
    private DatagramSocket datagram;
    private int seqNo, packageNo;
    private boolean continuation;
    private ObtainerInterface administrator;

    public void setFileReceiverListener(final ObtainerInterface l) { this.administrator = l; }
    public ObtainerManager() throws SocketException {
        this.datagram = new DatagramSocket(26100);
    }

    @Override
    public void run() {
        if (!this.initialize())
            return;
        this.seqNo = 0;
        this.continuation = true;
        this.administrator.fileReceived(0);
        this.packageWait();
        this.send();
        while (this.seqNo < this.packageNo - 1 && this.continuation) {
            this.packageWait();
            this.send();
        }
        if (this.continuation)
            IOFactory.getSourceBytes(this.fileData, this.name);
        this.finish();
    }

    private void packageWait() {
        final byte[] buffer = new byte[1032];
        try {
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.datagram.receive(packet);
            } catch (SocketException se) {
                return;
            }
            final byte[] seqBytes = new byte[4];
            System.arraycopy(packet.getData(), 0, seqBytes, 0, seqBytes.length);
            final byte[] lengthBytes = new byte[4];
            System.arraycopy(packet.getData(), 4, lengthBytes, 0, lengthBytes.length);
            this.seqNo = IOManager.getBytes(seqBytes);
            final int bytesLength = IOManager.getBytes(lengthBytes);
            final byte[] bytes = new byte[bytesLength];
            System.arraycopy(packet.getData(), 8, bytes, 0, bytes.length);
            if (this.seqNo == -1 && bytesLength == 0) {
                this.continuation = false;
                this.administrator.errorHappened(2);
                return;
            }
            System.arraycopy(bytes, 0, this.fileData, this.seqNo * 1024, bytesLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean initialize() {
        final byte[] buffer = new byte[128];
        try {
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.datagram.receive(packet);
            } catch (SocketException se) {
                return false;
            }
            final String msg = new String(packet.getData()).trim();
            String fileName = msg.substring(0, msg.indexOf(124));
            final int fileSize = Integer.parseInt(msg.substring(msg.indexOf(124) + 1));
            this.sourceIP = packet.getAddress().getHostAddress();
            this.packageNo = (int) Math.ceil(fileSize / 1024.0);
            this.fileData = new byte[fileSize];
            final boolean choice = this.administrator.infoReceived(fileName, fileSize);
            if (choice) {
                this.responseWait("x_ACCEPT_x");
                return true;
            }
            this.responseWait("x_REFUSE_x");
            this.finish();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void responseWait(final String message) {
        final byte[] data = message.getBytes();
        try {
            final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.sourceIP), 26000);
            this.datagram.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void send() {
        final byte[] seqBytes = IOManager.getBytes(this.seqNo);
        try {
            final DatagramPacket packet = new DatagramPacket(seqBytes, seqBytes.length, InetAddress.getByName(this.sourceIP), 26000);
            try {
                this.datagram.send(packet);
            } catch (SocketException ex) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.administrator.fileReceived((int) (100.0 * ((this.seqNo + 1) / this.packageNo)));
    }

    public void listen() {
        new Thread(this).start();
    }

    public void setSource(final File name) {
        this.name = name;
    }

    public void finish() {
        this.continuation = false;
        if (!this.datagram.isClosed())
            this.datagram.close();
    }

    public void decline() {
        this.seqNo = -1;
        this.send();
        this.finish();
    }
}
