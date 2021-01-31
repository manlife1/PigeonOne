package com.mr.pigeonone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.text.trimmedLength
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mr.pigeonone.R
import com.mr.pigeonone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    private lateinit var alertDialogBuilder: MaterialAlertDialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.phoneNumberEt.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.nextBtn.isEnabled=!(s?.trimmedLength()==0 || s!!.length<10)
            }

            override fun afterTextChanged(s: Editable?) {
                // TODO("Not yet implemented")
            }
        })
        binding.nextBtn.setOnClickListener {
            checkNumber()
        }
    }

    private fun checkNumber() {
        countryCode = binding.ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + binding.phoneNumberEt.text.toString()

        if (validatePhoneNumber(binding.phoneNumberEt.text.toString())) {
            notifyUserBeforeVerify(
                "We will be verifying the phone number:$phoneNumber\n" +
                        "Is this OK, or would you like to edit the number?"
            )
        } else {
            toast("Please enter a valid number to continue!")
        }
    }
    private fun validatePhoneNumber(phone: String): Boolean {
        if (phone.trimmedLength()==0) {
            return false
        }
        return true
    }

    private fun notifyUserBeforeVerify(message: String) {
        alertDialogBuilder = MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Edit") { dialog, _ ->
                dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()
        }
    }


    private fun showLoginActivity() {
        startActivity(
            Intent(this, OtpActivity::class.java).putExtra(PHONE_NUMBER, phoneNumber)
        )
        finish()
    }
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}