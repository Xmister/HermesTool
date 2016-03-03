package hu.xmister.hermestool;


public class DownloaderService extends com.google.android.vending.expansion.downloader.impl.DownloaderService {
    @Override
    public String getPublicKey() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhJ1YTEg8H8jHr1+tF9em3FMo8kB25+GThwwk0ooJi58Mr9XNYPq8PMHQeU1uvf79Gn0PwiVtyM+HBN0nht2SGILY86squ04Guq7FdHrPV5Gg1qVOwweNR95cI7JrA+fR5VlmKHF9r57rAqUfr8ITuXCLygA7jN1okUGvZ7Ez9Sc+jTYyMPPg0NytLIsrrn1B4MW4zSmArR4e8CIdFzlb/46kIFuPTNYGJdWqkeoztO62T2fTYY9zaDNy3GQHtpia6H+UhklvqjLoV+6TMxpR+5zM653k4laipXt7Q3DTLo1XG9ukbdMmW8nBE7BS7w/aBdOJLRwJLlkrba2qxARC5QIDAQAB";
    }

    @Override
    public byte[] getSALT() {
        return new byte[] { 1, 22, -12, -1, 54, 98,
                -120, -12, 43, 4, -48, -14, 9, 5, -114, -107, -32, 15, -1, 84
        };
    }

    @Override
    public String getAlarmReceiverClassName() {
        return AlarmReceiver.class.getName();
    }
}
