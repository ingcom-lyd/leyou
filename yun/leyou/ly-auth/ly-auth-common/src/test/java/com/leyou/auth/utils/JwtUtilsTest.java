package com.leyou.auth.utils;

import com.leyou.auth.entity.UserInfo;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;

@SpringBootTest
public class JwtUtilsTest {
    private static final String publicKeyPath = "C:/Users/Administrator/Desktop/复习/rsa/rsa.pub";
    private static final String privateKeyPath = "C:/Users/Administrator/Desktop/复习/rsa/rsa.pri";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(publicKeyPath,privateKeyPath,"ly@Login(Auth}*^31)&heiMa%");
    }

    @Test
    public void testGetRsa() throws Exception{
        this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(privateKeyPath);
    }

    @Test
    public void test() throws Exception {
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);

        UserInfo userInfo = new UserInfo(1L, "liyande");
        int expireMinutes = 10;

        String token = JwtUtils.generateToken(userInfo, privateKey, expireMinutes);
        System.out.println("token = " + token);

//        token = "LY_TOKEN=eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjgsInVzZXJuYW1lIjoibGlzaSIsImV4cCI6MTU2MjQyMjU0NH0.hR9AU1gSjcrzXmoQowEPxoFnHCZkXRFjoghN_9IXjVBoKcEQX7oV_U4Vr7V92-UdBGK-2xUjfT4XiLor-W2wCUU8IAne1Hxw78WIXR_WivQ_DL6nijo_LKKOd5qUm4MmS3G9juBY9gj3U0CUD52zwEOfEbbOdub-mWfvE8UzEHQ; Domain=leyou.com; Path=/";
        token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjgsInVzZXJuYW1lIjoibGlzaSIsImV4cCI6MTU2MjQyMTkzOX0.UeC176HUsvMXpzUiVMK1Gnw4Hd_pFqy0UdAxYaGReygOGj6_DMl-GZvf064zUGs-8xxI5WyhlXgsdy2rtvBugq4ku4SLZlo8afApNKQMMTp25EHHnT9uVJdk0t03dd3zucXlB-WiUPzWLu3wSs8e2KGZJDyCn4LN-7He1gFoZF4";
        //testParseToken(token);
    }

//    @Test
    public void testParseToken(String token) throws Exception {
//        testGetRsa();

//        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJsaXlhbmRlIiwiZXhwIjoxNTYyNDAyMjk2fQ.YOL13oCKHkBm78TyqnCpSI-7c-kivq5CZb0px3wqzvE6557GRMCdWCgUrj9sAd44x94zEP4wuSPYdlTC0GTKJnbtD0x7U5fbWuoSGTKUttduQYvzczrXfxGZLC3VQHVf1yPDtYLo23SZX8mA3R6P7BQgfnLwsRxWd8tZadLyWAY";

        PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);
        UserInfo infoFromToken = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("infoFromToken = " + infoFromToken);
    }
}