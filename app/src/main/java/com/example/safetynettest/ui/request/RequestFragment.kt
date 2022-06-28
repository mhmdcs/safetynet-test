package com.example.safetynettest.ui.request

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.safetynettest.API_KEY
import com.example.safetynettest.databinding.FragmentRequestBinding
import com.example.safetynettest.databinding.FragmentResultBinding
import com.example.safetynettest.model.SafetynetResultModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.json.webtoken.JsonWebSignature
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.util.*


class RequestFragment : Fragment() {


    //The request sent on Safetynet API depends on initially the availability of Google Play Services.
    //So the first and foremost thing that needs to be done is setting up the check for the availability of Google Play Services.
    //Then we can send a request to API with a generated nonce which is needed by API to recheck it while data is returned.
    //Data is returned in JsonWebSignature which needs to be parsed into Kotlin object to be displayed.
    //Google suggests verifying returned data by the backend to avoid irregular attacking on the API system.
    //Here we will just test the application and will not implement it by backend which is required to be done while making production-ready applications.

    private lateinit var binding: FragmentRequestBinding
    private val random: Random = SecureRandom()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentRequestBinding.inflate(inflater)

        binding.checkStatusBtn.setOnClickListener {
            checkIfGooglePlayExists()
        }

        return binding.root
    }

    // Checking Google Play Services availability before sending a request
    private fun checkIfGooglePlayExists() {
        if((GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext(), 13000000)) == ConnectionResult.SUCCESS){
            sendSafetyNetRequest()
        } else {
            Toast.makeText(context, "Please update or install Google Play Services", Toast.LENGTH_SHORT).show()
        }

    }

    private fun sendSafetyNetRequest() {
        // generate nonce
        val nonceData = "SafetyNet Data: ${System.currentTimeMillis()}"
        val nonce = getRequestNonce(nonceData)
        Log.d("RequestFragment","Original nonce1: $nonce")

        // sending the request
        SafetyNet.getClient(requireContext()).attest(nonce!!, API_KEY)
            .addOnSuccessListener {
                Log.d("RequestFragment", "JWS as string: ${it.jwsResult!!}")
                Log.d("RequestFragment", "JWS as string: ${decodeResultToken(it.jwsResult!!)}")
                val jws: JsonWebSignature = decodeResultToken(it.jwsResult!!)
                Log.d("RequestFragment","Original nonce2: $nonce")
                Log.d("RequestFragment","Attested nonce: ${jws.payload["nonce"].toString()}")
                Log.d("RequestFragment", "data: ${jws.payload["apkPackageName"].toString()}")
                val data = SafetynetResultModel(
                    basicIntegrity = jws.payload["basicIntegrity"].toString(),
                    evaluationType = jws.payload["evaluationType"].toString(),
                    profileMatch = jws.payload["ctsProfileMatch"].toString()
                )
                binding.checkStatusBtn.isClickable = true
                val directions = RequestFragmentDirections.actionRequestFragmentToResultFragment(data)
                findNavController().navigate(directions)
            }
            .addOnFailureListener{
                if(it is ApiException){
                    val apiException = it as ApiException
                    Log.d("RequestFragment", "data: ${apiException.message.toString()}")
                } else {
                    Log.d("RequestFragment", "data: ${it.message.toString()}")
                }
            }

    }

    // this is to decodes a string to a JWS (JsonWebSignature) object
    private fun decodeResultToken(JwsResultToken: String): JsonWebSignature{
        var jws: JsonWebSignature? = null
        return try {
            jws = JsonWebSignature.parser(JacksonFactory.getDefaultInstance()).parse(JwsResultToken)
            jws!!
        } catch (e: IOException){
            jws!!
        }
    }

    //One good way to create a nonce is to create a large (16 bytes or longer) random number on your server (here we'll generate the nonce inside our sample app instead)
    //using a cryptographically-secure random function. The SafetyNet attestation response includes the nonce you set
    //so make sure you verify that the returned nonce matches the one you included in the request you made.

    // nonce generator to get a nonce of 24 bytes length
    private fun getRequestNonce(data: String): ByteArray? {
        val byteStream = ByteArrayOutputStream()
        val byteArray = ByteArray(24)
        random.nextBytes(byteArray)
        try {
            byteStream.write(byteArray)
            byteStream.write(data.toByteArray())
        } catch (e: IOException){
            return null
        }
        return byteStream.toByteArray()
    }
}