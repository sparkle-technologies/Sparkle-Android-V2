package com.cyberflow.sparkle.register.widget.searchplace

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.sparkle.databinding.DialogSearchPlaceBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.cyberflow.sparkle.R

private const val TAG = "SearchPlaceDialog"

class SearchPlaceDialog(val title: String = "") : DialogFragment(), PlaceResultCallback {

    private val placeListView: RecyclerView get() = binding.rvPlaceResult
    private var placeList = arrayListOf<PlaceResult>()
    private lateinit var adapter: PlaceListAdapter
    private var icb: ICallBack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            DialogFragment.STYLE_NORMAL,
            com.cyberflow.base.resources.R.style.AppTheme_FullScreenDialog
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.dialog_search_place, container, false)
        initView(view)
        return view
    }

    private var _binding: DialogSearchPlaceBinding? = null
    private val binding: DialogSearchPlaceBinding get() = _binding!!

    private fun initView(view: View) {
        _binding = DialogSearchPlaceBinding.bind(view)
        adapter = PlaceListAdapter(placeList, this)
        placeListView?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        placeListView?.adapter = adapter
        binding.tvTitle.text = title
        binding.ivBtnBack.setOnClickListener { dismiss() }
        addEditTextListener()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setDimAmount(0.0f)
            dialog.window!!.setWindowAnimations(com.cyberflow.base.resources.R.style.Theme_CenterDialog)
        }
    }

    @SuppressLint("LongLogTag")
    private fun addEditTextListener() {
        binding.edtSearchPlace.requestFocus()
        binding.edtSearchPlace.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                binding.ivClear.visibility = View.VISIBLE
                binding.rvPlaceResult.visibility = View.VISIBLE
                searchPlace(it.toString())
            } else {
                binding.ivClear.visibility = View.GONE
            }
        }

        binding.ivClear.setOnClickListener {
            binding.edtSearchPlace.setText("")
        }
    }

    private fun searchPlace(query: String) {
        val placeList = arrayListOf<PlaceResult>()
        val placesClient = Places.createClient(context)   // create place client here
        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    val placeName = prediction.getPrimaryText(null).toString()
                    val placeId = prediction.placeId
                    val placeResultModel = PlaceResult(placeName, placeId)
                    placeList.add(placeResultModel)
                    adapter.setList(placeList)
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }
    }

    fun setCallBack(cb: ICallBack) {
        icb = cb
    }

    override fun onPlaceClicked(place: String, placeId: String) {
        //设置内容，并清除焦点
        binding.edtSearchPlace.setText(place)
        binding.edtSearchPlace.clearFocus()
        binding.rvPlaceResult.visibility = View.INVISIBLE
        getPlaceDetails(place, placeId)
    }

    private fun getPlaceDetails(place: String, placeId: String) {
        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        val placesClient = Places.createClient(context)
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val mPlace = response.place
                val latLng = mPlace.latLng
                val latitude = latLng?.latitude.toString()
                val longitude = latLng?.longitude.toString()
                icb?.callback(place, latitude, longitude)
                Log.i(TAG, "Place found: ${mPlace.latLng}")
                dismiss()
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                }
            }
    }

    interface ICallBack {
        fun callback(placeStr: String?, latitude: String?, longitude: String?)
    }
}