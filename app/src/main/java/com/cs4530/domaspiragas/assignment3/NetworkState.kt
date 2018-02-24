package com.cs4530.domaspiragas.assignment3

/**Storage for all games that the logged in user has access to*/
object NetworkState {
    var relevantGames : MutableList<SerializableGameState> = arrayListOf()
}