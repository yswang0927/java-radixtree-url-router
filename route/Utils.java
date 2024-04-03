package route;

public class Utils {
    private static final int rune1Max = 127;
    private static final int rune2Max = 2047;
    private static final int rune3Max = 65535;

    private static final int t1 = 0;
    private static final int tx = 128;
    private static final int t2 = 192;
    private static final int t3 = 224;
    private static final int t4 = 240;
    private static final int t5 = 248;

    private static final int maskx = 63;
    private static final int mask2 = 31;
    private static final int mask3 = 15;
    private static final int mask4 = 7;

    private static final int MaxRune = 65535; // Maximum valid Unicode code point.
    private static final int surrogateMin = 0xD800;
    private static final int surrogateMax = 0xDFFF;

    public static int encodeRune(byte[] p, char r) {
        int i = toUint32(r);
        if (i <= rune1Max) {
            p[0] = (byte) i;
            return 1;
        }

        if (i <= rune2Max) {
            p[0] = (byte) (t2 | (byte)(r>>6));
            p[1] = (byte) (tx | ((byte)r) & maskx);
            return 2;
        }

        if (i > MaxRune || (surrogateMin <= i && i <= surrogateMax)) {
            throw new RuntimeException("");
        }

        if (i <= rune3Max) {
            p[0] = (byte) (t3 | (r>>12));
            p[1] = (byte) (tx | ((r>>6) & maskx));
            p[2] = (byte) (tx | (r & maskx));
            return 3;
        }

        p[0] = (byte) (t4 | (r>>18));
        p[1] = (byte) (tx | ((r>>12) & maskx));
        p[2] = (byte) (tx | ((r>>6) & maskx));
        p[3] = (byte) (tx | ((r) & maskx));
        return 4;
    }

    public static int toUint32(char c) {
        // 如果字符为负数，需要进行修正，因为Java的char是无符号的16位整数
        int uint32Value = (int) c;
        if (uint32Value < 0) {
            uint32Value += 0x10000;
        }
        return uint32Value;
    }

    public static void main(String[] args) {
        System.out.println(0b00000111);
    }

}
