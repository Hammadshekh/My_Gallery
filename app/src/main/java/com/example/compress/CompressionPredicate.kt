package com.example.compress

interface CompressionPredicate {
    /**
     * Determine the given input path should be compressed and return a boolean.
     * @param path input path
     * @return the boolean result
     */
    fun apply(path: String?): Boolean
}
