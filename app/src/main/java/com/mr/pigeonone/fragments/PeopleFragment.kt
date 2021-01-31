package com.mr.pigeonone.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.mr.pigeonone.*
import com.mr.pigeonone.activities.ChatActivity
import com.mr.pigeonone.activities.USER_ID
import com.mr.pigeonone.activities.USER_NAME
import com.mr.pigeonone.activities.USER_THUMB_IMAGE
import com.mr.pigeonone.models.User


private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2
class PeopleFragment : Fragment() {

    private lateinit var mAdapter: FirestorePagingAdapter<User, RecyclerView.ViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
    }
    private val auth by lazy {
        Firebase.auth
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewManager = LinearLayoutManager(requireContext())
        setupAdapter()
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }
    private fun setupAdapter() {
        // Init Paging Configuration
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(8)
            .build()

        // Init Adapter Configuration
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(database, config, User::class.java)
            .build()

        // Instantiate Paging Adapter
        mAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val inflater = layoutInflater
                return when (viewType) {
                    NORMAL_VIEW_TYPE -> {
                        UsersViewHolder(inflater.inflate(R.layout.list_item, parent, false))
                    }
                    else -> EmptyViewHolder(inflater.inflate(R.layout.empty_view, parent, false))

                }
            }

            override fun onBindViewHolder(
                viewHolder: RecyclerView.ViewHolder,
                position: Int,
                user: User
            ) {
                // Bind to ViewHolder
                if (viewHolder is UsersViewHolder)
                    if (auth.uid == user.uid) {
                        currentList?.snapshot()?.removeAt(position)
                        notifyItemRemoved(position)
                    } else
                        viewHolder.bind(user) { name: String, photo: String, id: String ->//onclick continued
                            val intent=Intent(requireContext(), ChatActivity::class.java)
                            intent.putExtra(USER_ID, id)
                            intent.putExtra(USER_NAME, name)
                            intent.putExtra(USER_THUMB_IMAGE, photo)
                            startActivity(intent)
                        }
            }

            override fun onError(e: Exception) {
                super.onError(e)
                e.message?.let { Log.e("MainActivity", it) }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if (auth.uid == item!!.uid) {
                    DELETED_VIEW_TYPE
                } else {
                    NORMAL_VIEW_TYPE
                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                    }
                    LoadingState.LOADING_MORE -> {
                    }
                    LoadingState.LOADED -> {
                    }
                    LoadingState.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    LoadingState.FINISHED -> {
                    }
                }
            }

        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = mAdapter
        }

    }

}