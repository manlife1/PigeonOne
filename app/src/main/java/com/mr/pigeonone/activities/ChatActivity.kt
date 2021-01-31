package com.mr.pigeonone.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.trimmedLength
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.mr.pigeonone.R
import com.mr.pigeonone.adapters.ChatAdapter
import com.mr.pigeonone.databinding.ActivityChatBinding
import com.mr.pigeonone.models.ChatEvent
import com.mr.pigeonone.models.DateHeader
import com.mr.pigeonone.models.Inbox
import com.mr.pigeonone.models.Message
import com.mr.pigeonone.models.User
import com.mr.pigeonone.utils.KeyboardVisibilityUtil
import com.mr.pigeonone.utils.isSameDayAs
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val USER_ID = "userId"
const val USER_THUMB_IMAGE = "thumbImage"
const val USER_NAME = "userName"

class ChatActivity : AppCompatActivity() {
    private lateinit var binding:ActivityChatBinding

    private lateinit var friendId: String
    private lateinit var name: String
    private lateinit var image: String

    private val mCurrentUid: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    lateinit var currentUser: User
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    private val mutableItems: MutableList<ChatEvent> = mutableListOf()
    private val mLinearLayout: LinearLayoutManager by lazy { LinearLayoutManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        friendId= intent.getStringExtra(USER_ID).toString()
        name=intent.getStringExtra(USER_NAME).toString()
        image=intent.getStringExtra(USER_THUMB_IMAGE).toString()

        EmojiManager.install(GoogleEmojiProvider())
        binding=DataBindingUtil.setContentView(this, R.layout.activity_chat)

        val toolbar: MaterialToolbar =binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title=""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true);

        keyboardVisibilityHelper = KeyboardVisibilityUtil(binding.rootView) {
            binding.msgRv.scrollToPosition(mutableItems.size - 1)
        }

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
                .addOnSuccessListener {
                    currentUser = it.toObject(User::class.java)!!
                }

        chatAdapter = ChatAdapter(mutableItems, mCurrentUid)

        binding.msgRv.apply {
            layoutManager = mLinearLayout
            adapter = chatAdapter
        }

        binding.nameTv.text = name
        Picasso.get().load(image).into(binding.userImgView)

        val emojiPopup = EmojiPopup.Builder.fromRootView(binding.rootView).build(binding.msgEdtv)
        binding.smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }
        binding.swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                binding.swipeToLoad.isRefreshing = false
            }
        }


        binding.sendBtn.setOnClickListener {
            if(binding.msgEdtv.text?.trimmedLength()!=0) {
                binding.msgEdtv.text?.let {
                    sendMessage(it.toString())
                    it.clear()
                }
            }
            binding.msgRv.scrollToPosition(mutableItems.size-1)
        }

        listenMessages(){ msg, update ->
            if (update) {
                updateMessage(msg)
            } else {
                addMessage(msg)
            }
        }

        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }
        updateReadCount()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun updateReadCount() {
        getInbox(mCurrentUid, friendId).child("count").setValue(0)
    }

    private fun updateHighFive(id: String, status: Boolean) {
        getMessages(friendId).child(id).updateChildren(mapOf("liked" to status))
    }

    private fun addMessage(event: Message) {
        val eventBefore = mutableItems.lastOrNull()

        // Add date header if it's a different day
        if ((eventBefore != null
                        && !eventBefore.sentAt.isSameDayAs(event.sentAt))
                || eventBefore == null
        ) {
            mutableItems.add(
                    DateHeader(
                            event.sentAt, this
                    )
            )
        }
        mutableItems.add(event)

        chatAdapter.notifyItemInserted(mutableItems.size)
        binding.msgRv.scrollToPosition(mutableItems.size-1)
    }

    private fun updateMessage(msg: Message) {
        val position = mutableItems.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        mutableItems[position] = msg

        chatAdapter.notifyItemChanged(position)
    }

    private fun listenMessages(newMsg: (msg: Message, update: Boolean) -> Unit) {
        getMessages(friendId)
                .orderByKey()
                .addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                    }

                    override fun onChildChanged(data: DataSnapshot, p1: String?) {
                        val msg = data.getValue(Message::class.java)!!
                        newMsg(msg, true)
                    }

                    override fun onChildAdded(data: DataSnapshot, p1: String?) {
                        val msg = data.getValue(Message::class.java)!!
                        newMsg(msg, false)
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                    }

                })

    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId).push().key
        checkNotNull(id) { "Cannot be null" }
        val msgMap = Message(msg, mCurrentUid, id)
        getMessages(friendId).child(id).setValue(msgMap)
        updateLastMessage(msgMap, mCurrentUid)
    }

    private fun updateLastMessage(message: Message, mCurrentUid: String) {
        val inboxMap = Inbox(
                message.msg,
                friendId,
                name,
                image,
                message.sentAt,
                0
        )

        getInbox(mCurrentUid, friendId).setValue(inboxMap)

        getInbox(friendId, mCurrentUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.getValue(Inbox::class.java)
                inboxMap.apply {
                    from = message.senderId
                    name = currentUser.name
                    image = currentUser.thumbImage
                    count = 1
                }
                if (value?.from == message.senderId) inboxMap.count = value.count + 1

                getInbox(friendId, mCurrentUid).setValue(inboxMap)
            }
        })
    }

    private fun getMessages(friendId: String?) = db.reference.child("messages/${getId(friendId)}")

    private fun getInbox(toUser: String?, fromUser: String?) =
            db.reference.child("chats/$toUser/$fromUser")


    private fun getId(friendId: String?): String {// id for the messages
        return if (friendId!! > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }

    override fun onResume() {
        super.onResume()
        binding.rootView.viewTreeObserver
                .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }


    override fun onPause() {
        super.onPause()
        binding.rootView.viewTreeObserver
                .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

}
