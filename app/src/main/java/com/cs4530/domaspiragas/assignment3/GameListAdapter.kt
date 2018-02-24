package com.cs4530.domaspiragas.assignment3

import android.content.Context
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter

class GameListAdapter() : BaseAdapter() {
    constructor(c: Context) : this() {
        _c = c
        _inflater = LayoutInflater.from(c)
    }
    private lateinit var _c: Context
    private lateinit var _inflater: LayoutInflater

    /**Our Relevant games contains the number of games that we should display to the player,
     *  we'll have an entry for each game*/
    override fun getCount(): Int {
        return NetworkState.relevantGames.size
    }

    /**Unimplemented since I don't have any need for this.*/
    override fun getItem(position: Int): Any {
        return 0
    }

    /**Return 1 all the time, since I don't have any need for this*/
    override fun getItemId(position: Int): Long {
        return 1
    }

    /**The logic behind how each item is displayed*/
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var vi: View? = convertView
        if (vi == null) {
            vi = _inflater.inflate(R.layout.list_item, null)
        }
        // get the 4 text fields for the list item so we can populate their values
        val status_label :TextView = vi!!.findViewById(R.id.status_label)
        val turn_label : TextView = vi.findViewById(R.id.turn_label)
        val player1_ships_label : TextView = vi.findViewById(R.id.player1_ships_label)
        val player2_ships_label : TextView = vi.findViewById(R.id.player2_ships_label)

        val currentGame:SerializableGameState = NetworkState.relevantGames[position]

        // Our NetworkState singleton has relevant games indexed by the position they will be displayed
        // in the list. We use this position to query the relevant games and pull the information for
        // the labels
        status_label.text = currentGame.status

        val turnPlaceholder: String
        when(currentGame.playerTurn) {
            1 -> turnPlaceholder = currentGame.player1Name + "'s Turn"
            else -> turnPlaceholder = currentGame.player2Name + "'s Turn"
        }
        turn_label.text = turnPlaceholder

        val player1ShipsPlaceholder = currentGame.player1Name + "'s Ships: " + currentGame.player1.getNumShipsRemaining()
        player1_ships_label.text = player1ShipsPlaceholder

        val player2ShipsPlaceholder = currentGame.player2Name + "'s Ships: " + currentGame.player2.getNumShipsRemaining()
        player2_ships_label.text = player2ShipsPlaceholder

      return vi
    }
}