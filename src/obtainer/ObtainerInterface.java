package obtainer;

public interface ObtainerInterface {

    boolean infoReceived(final String p0, final int p1);

    void fileReceived(final int p0);

    void errorHappened(final int p0);
}