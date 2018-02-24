package com.cs4530.domaspiragas.assignment3

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView


class TileAdapter() : BaseAdapter() {
    /**This is the constructor we will use for the assignment
     * we don't need to pass any more data, other than the context*/
    constructor(c:Context, h:Int, play:Boolean, player:Int): this(){
        _c = c
        _h = h
        _play = play
        _player = player
    }
    private lateinit var _c:Context
    private var _h:Int = 0
    private var _play:Boolean = false
    private var _player:Int = 0


    /**The logic behind creating the imageView for our gridView is here*/
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var imageView:ImageView
        if(p1 == null){
            imageView = ImageView(_c)
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, _h)
            imageView.scaleType = ImageView.ScaleType.FIT_XY // We want our image to fill as much room as available
        }else{
            imageView = p1 as ImageView
        }
        var gb : GameBoard
        if(_player == 1){
            gb = GameState.player2
        }else{
            gb = GameState.player1
        }

        if(_play){
            if(GameState.selectedPos != p0) {
                when (gb.getTile(p0).getType()) {
                    "blank" ->
                        imageView.setImageResource(R.drawable.blank)
                    "ship" ->
                        imageView.setImageResource(R.drawable.blank)
                    "hit" ->
                        imageView.setImageResource(R.drawable.hit)
                    "miss" ->
                        imageView.setImageResource(R.drawable.miss)
                    "sunk" ->
                        imageView.setImageResource(R.drawable.sunk)
                }
            } else {
                imageView.setImageResource(R.drawable.highlight)
            }
        }else{
                when(gb.getTile(p0).getType()){
                    "blank" ->
                        imageView.setImageResource(R.drawable.blank)
                    "ship" ->
                        imageView.setImageResource(R.drawable.ship)
                    "hit" ->
                        imageView.setImageResource(R.drawable.hit)
                    "miss" ->
                        imageView.setImageResource(R.drawable.miss)
                    "sunk" ->
                        imageView.setImageResource(R.drawable.sunk)
                }
            }
        return imageView
    }

    /**I don't really need this functionality, so all items just return 1*/
    override fun getItem(p0: Int): Any {
        return 1
    }

    /**I don't really need this functionality, so all items have id of 0*/
    override fun getItemId(p0: Int): Long {
        return 0
    }

    /**Using The number of drawings we have as the count
     * since we will have one grid entry per drawing*/
    override fun getCount(): Int {
        return 100
    }
}