package com.cyberflow.sparkle.register.widget.searchplace

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.DialogSearchPlaceBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse

private const val TAG = "SearchPlaceDialog"

class SearchPlaceDialog(val title: String = "") : DialogFragment(), PlaceResultCallback {

    private var _binding: DialogSearchPlaceBinding? = null
    private val binding: DialogSearchPlaceBinding get() = _binding!!
    private var placeList = arrayListOf<PlaceResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.cyberflow.base.resources.R.style.AppTheme_FullScreenDialog)
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

    private var selectIdx = -1

    private fun initView(view: View) {
        _binding = DialogSearchPlaceBinding.bind(view)
        binding.rvPlaceResult.linear().setup {
            addType<PlaceResult>(R.layout.item_google_place)
            onBind {
                findView<TextView>(R.id.tv_place_result).text = getModel<PlaceResult>().placeName
                findView<TextView>(R.id.tv_place_result).background =
                    if (selectIdx == layoutPosition) ResourcesCompat.getDrawable(
                        resources,
                        com.cyberflow.base.resources.R.drawable.register_bg_gender_light,
                        null
                    ) else null
                findView<View>(R.id.line).visibility =
                    if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
            }
            R.id.tv_place_result.onClick {
                val model = getModel<PlaceResult>()
                selectIdx = layoutPosition
                notifyItemChanged(layoutPosition)
                Log.e(TAG, "you choose:  $model")
                onPlaceClicked(model.placeName, model.placeId)
            }
        }
        binding.tvTitle.text = title
        binding.llBack.setOnClickListener { dismiss() }
        binding.tvCancel.setOnClickListener { dismiss() }

        binding.edtSearchPlace.apply {

            addTextChangedListener {
                if (!it.isNullOrEmpty()) {
                    binding.rvPlaceResult.visibility = View.VISIBLE
                    searchPlace(it.toString())
                } else {
                    binding.rvPlaceResult.visibility = View.INVISIBLE
                }
            }
            setOnFocusChangeListener { v, hasFocus ->
                Log.e(TAG, "initView: hasFocus = $hasFocus")
                if (!hasFocus) return@setOnFocusChangeListener

                post {
                    KeyboardUtil.show(this)
                }
            }
            requestFocus()
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
                    selectIdx = -1
                    binding.rvPlaceResult.models = placeList
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }
    }

    override fun onPlaceClicked(place: String, placeId: String) {
        //设置内容，并清除焦点
        binding.edtSearchPlace.apply {
            setText(place)
            clearFocus()
            KeyboardUtil.hide(this)
        }
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

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.apply {
                setLayout(width, height)
                setDimAmount(0.0f)
                setWindowAnimations(com.cyberflow.base.resources.R.style.Theme_CenterDialog)
            }
        }
    }


    override fun dismiss() {
        KeyboardUtil.hide(binding.edtSearchPlace)
        super.dismiss()
    }

    private var icb: ICallBack? = null

    fun setCallBack(cb: ICallBack) {
        icb = cb
    }

    interface ICallBack {
        fun callback(placeStr: String?, latitude: String?, longitude: String?)
    }
}