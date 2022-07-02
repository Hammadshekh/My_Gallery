package com.example.mygallery.listener

interface DragListener {
    /**
     * Whether to drag the item to the delete place and change the color according to the state
     *
     * @param isDelete
     */
    fun deleteState(isDelete: Boolean)
    fun dragState(isStart: Boolean)
}