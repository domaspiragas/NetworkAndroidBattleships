package com.cs4530.domaspiragas.assignment3

/**A player object used for updating and pushing to DB and tracking relevant games*/
class Player {
    private var _games: MutableList<String> = arrayListOf()
    private var _id: String = ""
    private var _username: String = ""

    fun addGame(gameId: String) {
        _games.add(gameId)
    }

    fun removeGame(gameId: String) {
        _games.remove(gameId)
    }

    fun getUsername(): String {
        return _username
    }
    fun setUsername(username:String){
        _username = username
    }
    fun getId(): String {
        return _id
    }
    fun setId(id:String){
        _id = id
    }
}