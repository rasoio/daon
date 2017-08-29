package daon.analysis.ko.util;

import java.util.HashMap;
import java.util.Map;

import daon.analysis.ko.config.POSTag;

import daon.analysis.ko.model.Keyword;

public class Utils {

    /**
     * http://jrgraphix.net/r/Unicode/AC00-D7AF
     */
    //한글 시작 문자
    public final static int KOR_START = 0xAC00;
    //한글 종료 문자
    public final static int KOR_END = 0xD7A3;

    /**
     * http://jrgraphix.net/r/Unicode/3130-318F
     */
    public final static int JAMO_START = 0x3130;

    public final static int JAMO_END = 0x318F;

    public final static int HANJA_START = 0x4E00;
    public final static int HANJA_END = 0x9FFF;

    public final static int INDEX_NOT_FOUND = -1;

    public static long hashCode(String string){

        long h = 98764321261L;
        int l = string.length();
        char[] chars = string.toCharArray();

        for (int i = 0; i < l; i++) {
            h = 31 * h + chars[i];
        }
        return h;

    }

    public static boolean isTag(POSTag a, POSTag b){
        boolean is = false;

        long tagBit = a.getBit();
        // 사전의 tag 정보와 포함여부 tag 의 교집합 구함.
        long result = tagBit & b.getBit();

        if(result > 0){
            is = true;
        }

        return is;
    }

    public static int getSeq(POSTag tag){
        int seq = 0;

        if(tag == POSTag.SN){
            seq = 1;
        }else if(tag == POSTag.SL || tag == POSTag.SH){
            seq = 2;
        }
        return seq;
    }

    public static int getSeq(String tag){

        return getSeq(POSTag.valueOf(tag));
    }

    public static int getIdx(String tag){

        return POSTag.valueOf(tag).getIdx();
    }


}
