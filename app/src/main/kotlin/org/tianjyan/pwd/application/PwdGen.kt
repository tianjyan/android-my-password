package org.tianjyan.pwd.application

import org.apache.commons.lang3.StringUtils
import java.util.*

object PwdGen {

    private val LOWERS = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
    private val UPPERS = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    private val NUMBERS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val SPECIALS = charArrayOf('!', '@', '#', '$', '%', '^', '&', '*', '_', '~')
    private val DISCOURAGED = charArrayOf('i', 'l', 'o', 'I', 'O', '0', '1')

    enum class Optionality {
        PROHIBITED,
        MANDATORY
    }

    fun generatePassword(length: Int,
                         includeLows: Optionality,
                         includeCaps: Optionality,
                         includeNumbers: Optionality,
                         includeSpecials: Optionality): String {
        val charList = buildCharList(includeLows, includeCaps, includeNumbers, includeSpecials)
        val random = Random()
        var sb: StringBuilder
        do {
            sb = StringBuilder()
            for (i in 0 until length) {
                val index = random.nextInt(charList.size)
                val character = charList[index]
                sb.append(character)
            }
        } while (containsDiscouragedCharacters(sb.toString()) || !isPasswordValid(sb.toString(),
                length, includeLows,
                includeCaps,
                includeNumbers,
                includeSpecials))

        return sb.toString()
    }

    private fun buildCharList(includeLows: Optionality,
                              includeCaps: Optionality,
                              includeNumbers: Optionality,
                              includeSpecials: Optionality): List<Char> {
        val charList = ArrayList<Char>()
        if (Optionality.MANDATORY == includeLows) {
            for (c in LOWERS) {
                charList.add(c)
            }
        }

        if (Optionality.MANDATORY == includeCaps) {
            for (c in UPPERS) {
                charList.add(c)
            }
        }

        if (Optionality.MANDATORY == includeNumbers) {
            for (c in NUMBERS) {
                charList.add(c)
            }
        }

        if (Optionality.MANDATORY == includeSpecials) {
            for (c in SPECIALS) {
                charList.add(c)
            }
        }
        return charList
    }

    fun containsDiscouragedCharacters(password: String): Boolean {
        return StringUtils.containsAny(password, *DISCOURAGED)
    }

    private fun isPasswordValid(password: String,
                                length: Int,
                                includeLows: Optionality,
                                includeCaps: Optionality,
                                includeNumbers: Optionality,
                                includeSpecials: Optionality): Boolean {
        var result = true
        if (password.length != length) {
            result = false
        } else if (Optionality.MANDATORY == includeLows && StringUtils.containsNone(password, *LOWERS)) {
            result = false
        } else if (Optionality.PROHIBITED == includeLows && StringUtils.containsAny(password, *LOWERS)) {
            result = false
        } else if (Optionality.MANDATORY == includeCaps && StringUtils.containsNone(password, *UPPERS)) {
            result = false
        } else if (Optionality.PROHIBITED == includeCaps && StringUtils.containsAny(password, *UPPERS)) {
            result = false
        } else if (Optionality.MANDATORY == includeNumbers && StringUtils.containsNone(password, *NUMBERS)) {
            result = false
        } else if (Optionality.PROHIBITED == includeNumbers && StringUtils.containsAny(password, *NUMBERS)) {
            result = false
        } else if (Optionality.MANDATORY == includeSpecials && StringUtils.containsNone(password, *SPECIALS)) {
            result = false
        } else if (Optionality.PROHIBITED == includeSpecials && StringUtils.containsAny(password, *SPECIALS)) {
            result = false
        }

        return result
    }
}