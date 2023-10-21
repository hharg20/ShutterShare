package ca.unb.mobiledev.loginscreen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailAddressField:EditText = findViewById(R.id.editTextTextEmailAddress)
        val passwordAddressField:EditText = findViewById(R.id.editTextTextPassword)
        val buttonAddressField:Button = findViewById(R.id.button)

        buttonAddressField.setOnClickListener {
            if (emailAddressField.text.isNullOrBlank() && passwordAddressField.text.isNullOrBlank()) {
                Toast.makeText(this, "Please fill the required fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "${emailAddressField.text} is logged in!", Toast.LENGTH_SHORT)
            }
        }
    }
}