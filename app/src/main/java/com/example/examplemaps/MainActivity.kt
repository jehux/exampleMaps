package com.example.examplemaps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.examplemaps.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnGetLocation.setOnClickListener {
            getLocation()
        }
        binding.btnGoToMap.setOnClickListener {
            openMap(latitude, longitude)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Nececitas dar permisos de ubicacion", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Don't have permissions, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            val task: Task<Location> = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    getAddress(this, it)
                }
            }
        }
    }

    fun getAddress(context: Context, location: Location) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val alladdress = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (alladdress != null) {
                if (alladdress.isNotEmpty()) {
                    val address = alladdress[0]
                    val street = address.thoroughfare ?: "Nombre de calle no disponible"
                    val zipCode = address.postalCode ?: "Código postal no disponible"
                    val locality = address.locality ?: "Localidad no existe"
                    val colony = address.subLocality ?: "Colonia no existe"

                    binding.tvLocation.text =
                        "Latitud ${location.latitude}\n" +
                        "Longitud ${location.longitude}\n" +
                        "Nombre de la calle: $street\n" +
                        "Colonia: $colony" +
                        "Localidad: $locality" +
                        "Código postal: $zipCode\n"
                } else {
                    Toast.makeText(this, "No se encontraron direcciones", Toast.LENGTH_LONG).show()

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun openMap(lat: Double, lon: Double) {
        val uri = "geo:$lat,$lon"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        intent.setPackage("com.waze")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No hay aplicacion de mapas instalada", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

}