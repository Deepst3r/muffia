package com.deepster.mafiaparty.game


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepster.mafiaparty.R
import com.deepster.mafiaparty.model.entities.Game
import com.deepster.mafiaparty.model.entities.Role
import com.deepster.mafiaparty.model.itemview.UserItemView
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_lobby.*

class LobbyFragment : Fragment() {

    private lateinit var viewModel: GameViewModel
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lobby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(activity!!).get(GameViewModel::class.java)

        val currentUser = viewModel.currentUser.value!!

        val adapter = GroupAdapter<ViewHolder>()
        recycler_players.adapter = adapter
        recycler_players.layoutManager = LinearLayoutManager(context)
        db = FirebaseFirestore.getInstance()

        viewModel.game.observe(this, Observer { game ->
            // Set the player's role
            viewModel.role.value = game.players[currentUser.username]

            // Enable start button if enough players joined
            if (viewModel.role.value == Role.OWNER) {
                button_start_game.isEnabled = game.players.size == 7
            } else {
                //todo Show something else for normals players
            }

            // Update UI player list
            adapter.clear()
            adapter.addAll(game.players.map { player ->
                UserItemView(player.key)
            })
        })

        val roomID = viewModel.game.value!!.roomID

        // Update game object
        db.collection("games").document(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val game = snapshot.toObject(Game::class.java)!!
                viewModel.game.value = game
            }
        }
    }
}
