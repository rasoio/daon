package daon.analysis.ko;

import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.util.Utils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPOSTag {
	
	private Logger logger = LoggerFactory.getLogger(TestPOSTag.class);
	
	@Test
	public void tags(){
		
		
//		0x8000000000000000l = 다 0
//		0xffffffffffffffffl = 다 1
		
		/*
		System.out.println(2 & 1); // 10 & 01 => 00 => 0
		System.out.println(2 & 2); // 10 & 10 => 10 => 2 => 둘다 1인경우만 1
		System.out.println(2 | 1); // 10 | 01 => 11 => 3 => 어느쪽이나 1이면 1
		System.out.println(2 ^ 1); // 10 ^ 01 => 11 => 3 => 어느한쪽이 다른 경우만 1
		System.out.println(1 ^ 1); // 01 ^ 01 => 00 => 0 => 어느한쪽이 다른 경우만 1
//		System.out.println(1<<1); // 10 => 01 => 1 
		System.out.println(1<<1); // 1 => 10 => 2 
		System.out.println(1<<2); // 1 => 100 (2의2제곱) => 4 
		*/
		
		
//		System.out.println(0xffffffffffffffffl & 0x8000000000000000l);
//		System.out.println((POSTag.JKM));
//		System.out.println((POSTag.JKM & POSTag.COMPOSED)); // 무조건 0
//		System.out.println((POSTag.COMPOSED)); // 하나라도 1이면 1
//		System.out.println((POSTag.COMPOSED | POSTag.COMPOSED)); // 하나라도 1이면 1
//		System.out.println((POSTag.JKM & POSTag.MASK_TAG)); // 원본
//		System.out.println((POSTag.JKM | POSTag.MASK_TAG)); // 다 1
//		System.out.println(((POSTag.VC & POSTag.MASK_TAG) & POSTag.V));
//		System.out.println(((POSTag.EPH) & POSTag.V));
//		System.out.println(((POSTag.EPH) & POSTag.V));
//		System.out.println(V);
//		System.out.println((ECD | JKM | JKS | MDN | NNG | VV | XPN) & MA);
//		
//		System.out.println(Long.toBinaryString(POSTag.TAG_HASH.get("E")));
//		System.out.println(Long.toBinaryString(N));
//		
		
//		System.out.println("NNA : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("NNA").getBit()), 64,"0"));
//		System.out.println("NN  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("NN").getBit()), 64,"0"));
//		System.out.println("N   : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("N").getBit()), 64,"0"));
//		
//		System.out.println("VX  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("VX").getBit()), 64,"0"));
//		System.out.println("VP  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("VP").getBit()), 64,"0"));
//		System.out.println("VC  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("VC").getBit()), 64,"0"));
//		System.out.println("V   : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("V").getBit()), 64,"0"));
//		
//		System.out.println("MD  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("MD").getBit()), 64,"0"));
//		System.out.println("MA  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("MA").getBit()), 64,"0"));
//		System.out.println("M   : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("M").getBit()), 64,"0"));
//		
//		System.out.println("JK  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("JK").getBit()), 64,"0"));
//		System.out.println("J   : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("J").getBit()), 64,"0"));
//		
//		
//		
//		System.out.println("EP  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("EP").getBit()), 64,"0"));
//		System.out.println("EF  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("EF").getBit()), 64,"0"));
//		System.out.println("EC  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("EC").getBit()), 64,"0"));
//		System.out.println("ET  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("ET").getBit()), 64,"0"));
//		System.out.println("EM  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("EM").getBit()), 64,"0"));
//		System.out.println("E   : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("E").getBit()), 64,"0"));
//
//		System.out.println("XP  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("XP").getBit()), 64,"0"));
//		System.out.println("XS  : " + StringUtils.leftPad(Long.toBinaryString(POSTag.valueOf("XS").getBit()), 64,"0"));
		
		
		String[] tags = new String[] { "EFI","JKC","JKS","JX","MDN","MDT","NNB","NNG","NNM","NP","NR","VCP","VV","XSN", "TT", "na"};
		
		long checkMask = 0l;
		
		for(String tag : tags){
			
			try{
				POSTag tagType = POSTag.valueOf(tag);
				
				checkMask |= tagType.getBit();
			}catch(IllegalArgumentException e){
				logger.error("['{}'] - 존재하지않는 tag 값입니다.", tag, e);
			}
		}
		

		System.out.println("cm  : " + StringUtils.leftPad(Long.toBinaryString(checkMask), 64,"0"));
		
		long result = checkMask & POSTag.n.getBit();
		
		POSTag tag = Utils.getMatchPOSTag(checkMask, POSTag.n);
		
		System.out.println("rs  : " + StringUtils.leftPad(Long.toBinaryString(result), 64,"0"));

		// 체크하고자하는 tag가 존재하면 0보다 큼.
		System.out.println("XP  : " + StringUtils.leftPad(Long.toBinaryString(checkMask & POSTag.x.getBit()), 64,"0"));

//		TagType tag1 = TagType.valueOf("EFI");
//		TagType efi = TagType.valueOf("EFI");
		
		
	}
}
