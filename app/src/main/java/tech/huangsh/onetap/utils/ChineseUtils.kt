package tech.huangsh.onetap.utils

import android.icu.text.Collator
import android.os.Build
import java.util.*

/**
 * 中文拼音处理工具类
 */
object ChineseUtils {
    
    /**
     * 判断字符是否为中文
     */
    fun isChinese(char: Char): Boolean {
        val ub = Character.UnicodeBlock.of(char)
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
    }
    
    /**
     * 获取字符串的首字母（用于排序）
     */
    fun getFirstLetter(str: String): String {
        if (str.isEmpty()) return "#"
        
        val firstChar = str.first()
        return if (isChinese(firstChar)) {
            // 对于中文字符，使用Unicode值进行粗略的拼音排序
            when (firstChar.code) {
                in 0x4E00..0x9FFF -> {
                    // 基于Unicode值的简单拼音首字母映射
                    val code = firstChar.code
                    when {
                        code < 0x4F00 -> "A"
                        code < 0x5200 -> "B"
                        code < 0x5400 -> "C"
                        code < 0x5600 -> "D"
                        code < 0x5800 -> "E"
                        code < 0x5A00 -> "F"
                        code < 0x5C00 -> "G"
                        code < 0x5E00 -> "H"
                        code < 0x6000 -> "I"
                        code < 0x6200 -> "J"
                        code < 0x6400 -> "K"
                        code < 0x6600 -> "L"
                        code < 0x6800 -> "M"
                        code < 0x6A00 -> "N"
                        code < 0x6C00 -> "O"
                        code < 0x6E00 -> "P"
                        code < 0x7000 -> "Q"
                        code < 0x7200 -> "R"
                        code < 0x7400 -> "S"
                        code < 0x7600 -> "T"
                        code < 0x7800 -> "U"
                        code < 0x7A00 -> "V"
                        code < 0x7C00 -> "W"
                        code < 0x7E00 -> "X"
                        code < 0x8000 -> "Y"
                        else -> "Z"
                    }
                }
                else -> "#"
            }
        } else if (firstChar.isLetter()) {
            firstChar.uppercaseChar().toString()
        } else {
            "#"
        }
    }
    
    /**
     * 中文拼音比较器
     */
    val pinyinComparator: Comparator<String> = Comparator { str1, str2 ->
        // 使用系统的Collator进行中文排序
        val collator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collator.getInstance(Locale.CHINESE).apply {
                strength = Collator.PRIMARY
            }
        } else {
            java.text.Collator.getInstance(Locale.CHINESE).apply {
                strength = java.text.Collator.PRIMARY
            }
        }
        
        when (collator) {
            is Collator -> collator.compare(str1, str2)
            is java.text.Collator -> collator.compare(str1, str2)
            else -> str1.compareTo(str2, ignoreCase = true)
        }
    }
    
    /**
     * 判断字符串是否包含中文
     */
    fun containsChinese(str: String): Boolean {
        return str.any { isChinese(it) }
    }
    
    /**
     * 按拼音对字符串列表排序
     */
    fun sortByPinyin(list: List<String>): List<String> {
        return list.sortedWith(pinyinComparator)
    }
}
