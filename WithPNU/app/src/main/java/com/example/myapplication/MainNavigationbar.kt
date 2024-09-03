package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainNavigationbarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainNavigationbar : AppCompatActivity() {
    private lateinit var binding: ActivityMainNavigationbarBinding
    private val db = FirebaseFirestore.getInstance()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            // 위치 권한이 부여된 경우 MapFragment로 교체
            replaceFragment(MapFragment.newInstance("", ""), R.id.map)
        } else {
            // 위치 권한이 거부된 경우 사용자에게 알림
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNavigationbarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())  // 앱 시작 시 기본 프래그먼트는 HomeFragment

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.detail -> replaceFragment(DetailFragment(), R.id.detail)
                R.id.map -> checkLocationPermissionAndReplaceFragment()
                R.id.mypage -> loadMypageFragmentBasedOnRole()
                else -> {
                }
            }
            true
        }
    }

    private fun loadMypageFragmentBasedOnRole() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("Users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        val fragment = when (role) {
                            "admin" -> MypageAdminFragment()
                            else -> MypageFragment()
                        }
                        replaceFragment(fragment, R.id.mypage)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "사용자 역할을 불러오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkLocationPermissionAndReplaceFragment() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 부여된 경우 MapFragment로 교체
                replaceFragment(MapFragment.newInstance("", ""), R.id.map)
            }
            else -> {
                // 권한 요청
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        }
    }

    private var isNavigating = false

    fun replaceFragment(fragment: Fragment, menuItemId: Int? = null) {
        if (isNavigating) return

        isNavigating = true
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()

        // Navigation Bar의 선택 상태를 업데이트 (menuItemId가 전달된 경우)
        menuItemId?.let {
            binding.bottomNavigationView.selectedItemId = it
        }

        isNavigating = false
    }

}
