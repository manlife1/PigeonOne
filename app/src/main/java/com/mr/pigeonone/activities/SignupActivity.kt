package com.mr.pigeonone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.text.trimmedLength
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.mr.pigeonone.models.User
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mr.pigeonone.R
import com.mr.pigeonone.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignupBinding
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        Firebase.auth
    }
    private val database by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var downloadUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_signup)
        //Assignment 2 - Add Crop Image option along with a thumbnail using firebase extension - Video at end of series
        binding.userImgView.setOnClickListener {
            checkPermissionForImage()
        }
        binding.nextBtn.setOnClickListener {
            val name = binding.nameEt.text.toString()
            if (!::downloadUrl.isInitialized) {
                toast("Photo cannot be empty")
            } else if (name.trimmedLength()==0) {
                toast("Name cannot be empty")
            } else {
                val user = User(name, downloadUrl, downloadUrl/*Needs to thumbnai url*/, auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    binding.nextBtn.isEnabled = true
                }
            }
        }

    }

    override fun onBackPressed() {

    }


    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    1001
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(
                    permissionWrite,
                    1002
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        ) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data?.let {
                binding.userImgView.setImageURI(it)
                startUpload(it)
            }
        }
    }

    private fun startUpload(filePath: Uri) {
        binding.nextBtn.isEnabled = false
        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(filePath)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                binding.nextBtn.isEnabled = true
            } else {
                binding.nextBtn.isEnabled = true
                // Handle failures
            }
        }.addOnFailureListener {

        }
    }

}