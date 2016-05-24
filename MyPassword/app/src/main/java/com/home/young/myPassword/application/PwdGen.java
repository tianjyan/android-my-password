package com.home.young.myPassword.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by YOUNG on 2016/4/3.
 */
public class PwdGen {

    private static final char[] LOWERS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    private static final char[] UPPERS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    private static final char[] SPECIALS = { '!', '@', '#', '$', '%', '^', '&', '*', '_', '~' };
    private static final char[] DISCOURAGED = { 'i', 'l', 'o', 'I', 'O', '0', '1' };

    public enum Optionality {
        PROHIBITED,
        MANDATORY;
    }

    public  static  String generatePassword(int length,
                                            Optionality includeLows,
                                            Optionality includeCaps,
                                            Optionality includeNumbers,
                                            Optionality includeSpecials){
        List<Character> charList = buildCharList(includeLows,includeCaps,includeNumbers,includeSpecials);
        Random random = new Random();
        StringBuilder sb;
        do {
            sb = new StringBuilder();
            for (int i = 0; i< length;i++) {
                int index = random.nextInt(charList.size());
                Character character = charList.get(index);
                sb.append(character.charValue());
            }
        } while(containsDiscouragedCharacters(sb.toString()) || !isPasswordValid(sb.toString(), length, includeLows,includeCaps,includeNumbers,includeSpecials));

        return sb.toString();
    }

    private static List<Character> buildCharList(Optionality includeLows,
                                                 Optionality includeCaps,
                                                 Optionality includeNumbers,
                                                 Optionality includeSpecials) {
        List<Character> charList = new ArrayList<Character>();
        if(Optionality.MANDATORY == includeLows){
            for(char c : LOWERS){
                charList.add(Character.valueOf(c));
            }
        }

        if(Optionality.MANDATORY == includeCaps) {
            for (char c : UPPERS){
                charList.add(Character.valueOf((c)));
            }
        }

        if(Optionality.MANDATORY == includeNumbers) {
            for (char c: NUMBERS) {
                charList.add(Character.valueOf(c));
            }
        }

        if(Optionality.MANDATORY == includeSpecials) {
            for (char c : SPECIALS) {
                charList.add(Character.valueOf(c));
            }
        }
        return charList;
    }

    public static boolean containsDiscouragedCharacters(String password) {
        return StringUtils.containsAny(password, DISCOURAGED);
    }

    private static boolean  isPasswordValid(String password,
                                            int length,
                                            Optionality includeLows,
                                            Optionality includeCaps,
                                            Optionality includeNumbers,
                                            Optionality includeSpecials) {
        boolean result = true;
        if(password.length() != length){
            result = false;
        } else if (Optionality.MANDATORY == includeLows && StringUtils.containsNone(password, LOWERS)){
            result = false;
        } else if(Optionality.PROHIBITED == includeLows && StringUtils.containsAny(password, LOWERS)) {
            result = false;
        } else if(Optionality.MANDATORY == includeCaps && StringUtils.containsNone(password, UPPERS)){
            result = false;
        } else if(Optionality.PROHIBITED == includeCaps && StringUtils.containsAny(password, UPPERS)){
            result = false;
        } else if(Optionality.MANDATORY == includeNumbers && StringUtils.containsNone(password, NUMBERS)){
            result = false;
        } else if(Optionality.PROHIBITED == includeNumbers && StringUtils.containsAny(password, NUMBERS)){
            result = false;
        } else if(Optionality.MANDATORY == includeSpecials && StringUtils.containsNone(password, SPECIALS)){
            result = false;
        } else if(Optionality.PROHIBITED == includeSpecials && StringUtils.containsAny(password, SPECIALS)){
            result = false;
        }

        return result;
    }
}
