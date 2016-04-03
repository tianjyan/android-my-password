package young.home.com.mypassword;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import young.home.com.mypassword.application.PwdGen;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @SmallTest
    public  void testPwdGen(){
        String password = PwdGen.generatePassword(16, PwdGen.Optionality.MANDATORY, PwdGen.Optionality.MANDATORY, PwdGen.Optionality.MANDATORY, PwdGen.Optionality.PROHIBITED);
        assertNotNull(password);
    }
}