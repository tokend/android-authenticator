package org.tokend.authenticator.base.util

object SearchUtil {
    private val WHITESPACE_REGEX = "\\s+".toRegex()

    fun isMatchGeneralCondition(query: String, vararg fields: String?): Boolean {
        val unmatchedFieldsParts = fields.fold(mutableSetOf<String>()) { acc, item ->
            if (item != null) {
                acc.addAll(splitByWhitespace(item.toLowerCase()))
            }
            acc
        }

        val unmatchedQueryParts = splitByWhitespace(query.toLowerCase()).toMutableList()
        var unmatchedChanged = true
        while (unmatchedFieldsParts.isNotEmpty()
                && unmatchedQueryParts.isNotEmpty()
                && unmatchedChanged) {
            val unmatchedFieldsPartsIterator = unmatchedFieldsParts.iterator()
            unmatchedChanged = false
            while (unmatchedFieldsPartsIterator.hasNext()) {
                val fieldPart = unmatchedFieldsPartsIterator.next()

                val partsIterator = unmatchedQueryParts.iterator()
                while (partsIterator.hasNext()) {
                    val queryPart = partsIterator.next()

                    if (fieldPart.startsWith(queryPart, true)) {
                        partsIterator.remove()
                        unmatchedFieldsPartsIterator.remove()
                        unmatchedChanged = true
                        break
                    }
                }
            }
        }

        return unmatchedQueryParts.isEmpty()
    }

    private fun splitByWhitespace(text: String): Collection<String> {
        return text.split(WHITESPACE_REGEX)
    }
}