package io.trainners.raily_backend.domain.korail.client;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 코레일 anti-macro(DynaPath) 토큰 생성기.
 * korail2 PR #54의 파이썬 DynaPathMasterEngine 로직을 자바로 그대로 포팅한 것.
 * 유저 로그인과 무관하게, 기기값/시각/랜덤값만으로 토큰을 만든다.
 */
public class DynaPathEngine {

    private static final String APP_ID = "com.korail.talk";
    private static final String AS_VALUE = "%5B38ff229cb34c7dda8e28220a2d750cce%5D";
    private static final String DEVICE_MODEL = "SM-S928N";
    private static final String OS_TYPE = "Android";
    private static final String SDK_VERSION = "v1";
    private static final String TABLE =
            "3FE9jgRD4KdCyuawklqGJYmvfMn15P7US8XbxeLQtWT6OicBAopINs2Vh0HZrz";
    private static final int I8 = 161, I9 = 30, I10 = 2;

    private final String appStartTs;

    public DynaPathEngine() {
        this.appStartTs = String.valueOf(System.currentTimeMillis());
    }

    private int[] string2xA1s(String s) {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            int cp = s.charAt(i);
            if (cp < 128) {
                r.add(cp);
            } else if (cp < 2048) {
                r.add(128 | ((cp >> 7) & 15));
                r.add(cp & 127);
            } else if (cp >= 262144) {
                r.add(160);
                r.add((cp >> 14) & 127);
                r.add((cp >> 7) & 127);
                r.add(cp & 127);
            } else if ((63488 & cp) != 55296) {
                r.add(((cp >> 14) & 15) | 144);
                r.add((cp >> 7) & 127);
                r.add(cp & 127);
            }
        }
        int[] arr = new int[r.size()];
        for (int i = 0; i < r.size(); i++) arr[i] = r.get(i);
        return arr;
    }

    private BigInteger makeKey(String s) {
        BigInteger acc = BigInteger.ZERO;
        for (int i = 0; i < s.length(); i++) {
            int cp = s.charAt(i);
            int bit = 32768;
            for (int k = 0; k < 16; k++) {
                if ((bit & cp) != 0) break;
                bit >>= 1;
            }
            acc = acc.multiply(BigInteger.valueOf((long) bit << 1)).add(BigInteger.valueOf(cp));
        }
        return acc;
    }

    private char internalI(String table, int rem, String sb) {
        int j = 0;
        for (int k = 0; k < table.length(); k++) {
            char ch = table.charAt(k);
            if (sb.indexOf(ch) == -1) {
                if (j == rem) return ch;
                j++;
            }
        }
        return ' ';
    }

    private String makeEncodeTable(BigInteger num, int size, String table) {
        StringBuilder sb = new StringBuilder();
        BigInteger temp = num;
        for (int i = 0; i < size; i++) {
            BigInteger div = BigInteger.valueOf(size - i);
            int rem = temp.mod(div).intValue();
            sb.append(internalI(table, rem, sb.toString()));
            temp = temp.divide(div);
        }
        return sb.toString();
    }

    private String encodeNormalBe(String s, String table, int i8, int i9, int i10) {
        int[] list = string2xA1s(s);
        StringBuilder sb = new StringBuilder();
        int[] iArr = new int[i10 + 1];
        int idx = 0;
        int size = list.length % i10;
        int size2 = list.length - size;
        while (idx < size2) {
            int val = 0;
            for (int n = 0; n < i10; n++) { val = val * i8 + list[idx]; idx++; }
            for (int i = 0; i < i10 + 1; i++) { iArr[i] = val % i9; val = val / i9; }
            for (int i = i10; i >= 0; i--) sb.append(table.charAt(iArr[i]));
        }
        if (size > 0) {
            int val = 0;
            for (int n = 0; n < size; n++) { val = val * i8 + list[idx]; idx++; }
            for (int i = 0; i < size + 1; i++) { iArr[i] = val % i9; val = val / i9; }
            while (size >= 0) { sb.append(table.charAt(iArr[size])); size--; }
        }
        return sb.toString();
    }

    /** device_id, 밀리초 ts, 랜덤 4글자로 x-dynapath-m-token 값을 만든다. */
    public String generateToken(String deviceId, long ts, String rand) {
        String plaintext = "ai=" + APP_ID + "&di=" + deviceId + "&as=" + AS_VALUE
                + "&su=false&dbg=false&emu=false&hk=false&it=" + appStartTs
                + "&ts=" + ts + "&rt=0&os=13&dm=" + DEVICE_MODEL
                + "&st=" + OS_TYPE + "&sv=" + SDK_VERSION;

        String dynKey = "v1+" + rand + "+" + ts;
        String keyEnc = encodeNormalBe(dynKey, TABLE, I8, I9, I10);
        BigInteger bigKey = makeKey(dynKey);
        String customTable = makeEncodeTable(bigKey, I9, TABLE);
        String bodyEnc = encodeNormalBe(plaintext, customTable, I8, I9, I10);
        return "bEeEP" + TABLE.charAt(keyEnc.length()) + keyEnc + bodyEnc;
    }

    /** 랜덤 4글자(A-Z0-9) 생성 */
    public static String randomRand() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}