package com.cs4530.domaspiragas.assignment3
/**Used to represent each piece of the 10x10 game board grid*/
class Tile{
    private var _type : String = "blank" // initial state of all tiles

    /**A ship was in this tile, now it has been shot*/
    fun hit(){
        _type = "hit"
    }
    /**No ship was in this tile, now it has been shot*/
    fun miss(){
        _type = "miss"
    }
    /**A ship was in this tile, then it was hit, then the other segments were hit, now it has sunk*/
    fun sunk(){
        _type = "sunk"
    }
    /**A ship was placed consuming this tile*/
    fun ship(){
        _type = "ship"
    }
    /**Get which tile type so the appropriate action can be taken when shot*/
    fun getType(): String {
        return _type
    }
}
