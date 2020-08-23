package com.app.hyperpay

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings
import com.oppwa.mobile.connect.exception.PaymentError
import com.oppwa.mobile.connect.provider.Connect
import com.oppwa.mobile.connect.provider.Transaction
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity() {

    var checkout_id : String = "YOUR_GENERATED_CHECKOUT_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //generate checkout id
        //Your app should request a checkout ID from your server.
        // This example uses hyperpay sample integration server;
        // please adapt it to use your own backend API.
        generateCheckoutId()

        //Define the settings for the checkout screen.
        // Initialize CheckoutSettings with received checkout ID,
        // it controls the information that is shown to the shopper.
        setCheckoutSettings(checkout_id)
    }

    private fun generateCheckoutId() {
        var retrofit = Retrofit
            .Builder()
            .baseUrl(MyInterface.Base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var myInterface = retrofit.create(MyInterface::class.java)

        val headers = HashMap<Any, Any>()
        headers["Content-Type"] = "application/json"
        headers["Token"] = "TOKEN"

        var jsonObject = JSONObject()
        jsonObject.put("amount","50.00")
        jsonObject.put("currency","EUR")
        jsonObject.put("paymentType","DB")

        val call: Call<ResponseBody> = myInterface.generate_id(headers, jsonObject)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                System.out.println(response.body())
                try {
                    val responseString = String(response.body()!!.bytes())
                    println(responseString)
                    checkout_id = response.body().toString()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                println(t.message)
            }
        })

    }

    private fun setCheckoutSettings(checkoutId: String?) {
        val paymentBrands: MutableSet<String> =
            LinkedHashSet()

        paymentBrands.add("VISA")
        paymentBrands.add("MASTER")
        paymentBrands.add("MADA")

        val checkoutSettings = CheckoutSettings(
            checkoutId!!,
            paymentBrands,
            Connect.ProviderMode.TEST
        ).setShopperResultUrl("PACKAGE_NAME://result")

        val intent = checkoutSettings.createCheckoutActivityIntent(this)

        startActivityForResult(
            intent,
            CheckoutActivity.REQUEST_CODE_CHECKOUT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        System.out.println("RESULT "+resultCode)
        when (resultCode) {
            CheckoutActivity.RESULT_OK -> {
                System.out.println("RESULT OK "+resultCode)
                /* transaction completed */
                val transaction: Transaction =
                    data!!.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION)
                /* resource path if n eeded */
                val resourcePath =
                    data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH)

                /** Action after success **/

            }
            CheckoutActivity.RESULT_CANCELED -> {
                System.out.println("RESULT CANCEL"+resultCode)
                Toast.makeText(this , resources.getString(R.string.payment_cancelled),Toast.LENGTH_LONG).show()
            }
            CheckoutActivity.RESULT_ERROR ->  {
                /* error occurred */
                val error: PaymentError =
                    data!!.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_ERROR)
                System.out.println("RESULT ERROR "+error.errorCode+" "+error.errorInfo+" "+error.errorMessage)
            }

        }
    }

}
