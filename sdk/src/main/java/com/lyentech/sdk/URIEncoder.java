package com.lyentech.sdk;

import java.io.UnsupportedEncodingException;

public class URIEncoder {
    public static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

    //java测试
    public static void main(String args[]) {
        String arg = "v=0.0.1&tp=ev&ak=352CEF51DE68B0B7EBE1F4955A339A93&u=http%3a//www.baidu.com&rf=http%3a//www.baidu.com&sys=Android%2025&br=&brv=&sr=1080x2039&uuid=F4911E698141&rnd=1650792084&ev=_stay&evv=%7b%22_default%22%3a29%7d";
        String arg2 = "%7b%22_default%22%3a29%7d"; //能行 result>{"_default":29}
        String arg3 = "%7b%22_default%22%3a%2229%22%7d"; // result>{"_default":"29"}
        String arg4 = "https://video_b.redocn.com/video/201806/20180611/Redcon_201806110358281555251449_big.mp4";
        String arg5 = "https://video_b.redocn.com/video/201806/20180611/Redcon_2018061102033615552514733_big.mp4";
        String arg6 = "%7b%22_default%22%3a4%7d"; //-1 4
        String arg7 = "%7b%22_default%22%3a2%7d"; //-1 2
        String arg8 = "%7b%22_default%22%3a18%7d"; //1 18
        String result = decodeURIComponent(arg8);
//        String result = null;
        try {
//            result = encodeURI(arg5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("result>" + result);
    }

    public static String encodeURI(String str)
            throws UnsupportedEncodingException {
        String isoStr = new String(str.getBytes("UTF8"), "ISO-8859-1");
        char[] chars = isoStr.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            if ((chars[i] <= 'z' && chars[i] >= 'a') || (chars[i] <= 'Z' && chars[i] >= 'A')
                    || chars[i] == '-' || chars[i] == '_' || chars[i] == '.' || chars[i] == '!'
                    || chars[i] == '~' || chars[i] == '*' || chars[i] == '\'' || chars[i] == '('
                    || chars[i] == ')' || chars[i] == ';' || chars[i] == '/' || chars[i] == '?'
                    /*|| chars[i] == ':' */ || chars[i] == '@' || chars[i] == '&' || chars[i] == '='
                    || chars[i] == '+' || chars[i] == '$' || chars[i] == ',' || chars[i] == '#'
                    || (chars[i] <= '9' && chars[i] >= '0')
                    /*|| (chars[i] == '"')*/) {
                sb.append(chars[i]);
            } else {
                sb.append("%");
                sb.append(Integer.toHexString(chars[i]));
            }
        }
        return sb.toString();
    }

    /**
     * @see
     */
    public static String encodeURIComponent(String input) {
        if (null == input || "".equals(input.trim())) {
            return input;
        }

        int l = input.length();
        StringBuilder o = new StringBuilder(l * 3);
        try {
            for (int i = 0; i < l; i++) {
                String e = input.substring(i, i + 1);
                if (ALLOWED_CHARS.indexOf(e) == -1) {
                    byte[] b = e.getBytes("utf-8");
                    o.append(getHex(b));
                    continue;
                }
                o.append(e);
            }
            return o.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return input;
    }

    private static String getHex(byte buf[]) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        for (int i = 0; i < buf.length; i++) {
            int n = (int) buf[i] & 0xff;
            o.append("%");
            if (n < 0x10) {
                o.append("0");
            }
            o.append(Long.toString(n, 16).toUpperCase());
        }
        return o.toString();

    }

    //解码url
    public static String decodeURIComponent(String encodedURI) {
        char actualChar;

        StringBuffer buffer = new StringBuffer();

        int bytePattern, sumb = 0;

        for (int i = 0, more = -1; i < encodedURI.length(); i++) {
            actualChar = encodedURI.charAt(i);

            switch (actualChar) {
                case '%': {
                    actualChar = encodedURI.charAt(++i);
                    int hb = (Character.isDigit(actualChar) ? actualChar - '0' : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                    actualChar = encodedURI.charAt(++i);
                    int lb = (Character.isDigit(actualChar) ? actualChar - '0' : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                    bytePattern = (hb << 4) | lb;
                    break;
                }
                case '+': {
                    bytePattern = ' ';
                    break;
                }
                default: {
                    bytePattern = actualChar;
                }
            }

            if ((bytePattern & 0xc0) == 0x80) { // 10xxxxxx
                sumb = (sumb << 6) | (bytePattern & 0x3f);
                if (--more == 0) buffer.append((char) sumb);
            } else if ((bytePattern & 0x80) == 0x00) { // 0xxxxxxx
                buffer.append((char) bytePattern);
            } else if ((bytePattern & 0xe0) == 0xc0) { // 110xxxxx
                sumb = bytePattern & 0x1f;
                more = 1;
            } else if ((bytePattern & 0xf0) == 0xe0) { // 1110xxxx
                sumb = bytePattern & 0x0f;
                more = 2;
            } else if ((bytePattern & 0xf8) == 0xf0) { // 11110xxx
                sumb = bytePattern & 0x07;
                more = 3;
            } else if ((bytePattern & 0xfc) == 0xf8) { // 111110xx
                sumb = bytePattern & 0x03;
                more = 4;
            } else { // 1111110x
                sumb = bytePattern & 0x01;
                more = 5;
            }
        }
        return buffer.toString();
    }
}
