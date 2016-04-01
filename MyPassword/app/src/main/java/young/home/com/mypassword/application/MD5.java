package young.home.com.mypassword.application;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by YOUNG on 2016/4/1.
 */
public class MD5 {
    public static  String getMD5(String value) throws NoSuchAlgorithmException{
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(value.getBytes());
        byte[] m = md5.digest();
        return  getString(m);
    }

    private  static  String getString(byte[] bytes){
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<bytes.length;i++){
            sb.append(bytes[i]);
        }
        return  sb.toString();
    }
}
