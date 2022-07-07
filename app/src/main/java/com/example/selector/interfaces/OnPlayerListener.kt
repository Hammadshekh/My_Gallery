package com.example.selector.interfaces

interface OnPlayerListener {
    /**
     * player error
     */
    fun onPlayerError()

    /**
     * playing
     */
    fun onPlayerReady()

    /**
     * preparing to play
     */
    fun onPlayerLoading()

    /**
     * end of playback
     */
    fun onPlayerEnd()
}

