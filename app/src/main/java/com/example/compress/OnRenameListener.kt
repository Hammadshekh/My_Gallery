package com.example.compress

interface OnRenameListener {
    /**
     * 压缩前调用该方法用于修改压缩后文件名
     *
     *
     * Call before compression begins.
     *
     * @param filePath 传入文件路径/ file path
     * @return 返回重命名后的字符串/ file name
     */
    fun rename(filePath: String?): String?
}