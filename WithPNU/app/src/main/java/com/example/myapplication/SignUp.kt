package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity



class SignUp : AppCompatActivity() {

    private lateinit var spinnerColleges: Spinner
    private lateinit var spinnerDepartments: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        spinnerColleges = findViewById(R.id.spinner_colleges)
        spinnerDepartments = findViewById(R.id.spinner_departments)
        spinnerColleges.prompt="전체"
        spinnerDepartments.prompt="전체"

        val colleges = arrayOf("전체", "인문대학", "사회과학대학", "자연과학대학","공과대학","사범대학","경제통상대학","경영대학","약학대학","생활과학대학","예술대학","나노과학기술대학","간호대학","의과대학","정보의생명공학대학")
        val departmentsMap = mapOf(
            "전체" to arrayOf("전체"),
            "인문대학" to arrayOf("국어국문학과","일어일문학과","불어불문학과","노어노문학과","중어중문학과","영어영문학과","독어독문학과","한문학과","언어정보학과","사학과","철학과","고고학과"),
            "사회과학대학" to arrayOf("행정학과","정치외교학과","사회복지학과","사회학과","심리학과","문헌정보학과","미디어커뮤니케이션학과"),
            "자연과학대학" to arrayOf("수학과","통계학과","물리학과","화학과","생명과학과","미생물학과","분자생물학과","지질환경과학과","대기환경과학과","해양학과"),
            "공과대학" to arrayOf("기계공학부","고분자공학과","유기소재시스템공학과","화공생명·환경공학부 - 화공생명공학전공","화공생명·환경공학부 - 환경공학전공","전기전자공학부 - 전자공학전공","전기전자공학부 - 전기공학전공","전기전자공학부 - 반도체공학전공","조선해양공학과","재료공학부","산업공학과","항공우주공학과","건축공학과","건축학과","도시공학과","사회기반시스템공학과"),
            "사범대학" to arrayOf("국어교육과", "영어교육과", "독어교육과", "불어교육과", "교육학과", "유아교육과", "특수교육과", "일반사회교육과", "역사교육과", "지리교육과", "윤리교육과", "수학교육과", "물리교육과", "화학교육과", "생물교육과", "지구과학교육과", "체육교육과"),
            "경제통상대학" to arrayOf("무역학부", "경제학부", "관광컨벤션학과", "국제학부", "공공정책학부"),
            "경영대학" to arrayOf("경영학과"),
            "약학대학" to arrayOf("약학대학 - 약학전공","약학대학 - 제약학전공"),
            "생활과학대학" to arrayOf("아동가족학과", "의류학과", "식품영양학과", "실내환경디자인학과", "스포츠과학과"),
            "예술대학"  to arrayOf("음악학과", "한국음악학과", "미술학과", "조형학과", "디자인학과", "무용학과", "예술문화영상학과"),
            "나노과학기술대학"  to arrayOf("나노메카트로닉스공학과", "나노에너지공학과", "광메카트로닉스공학과"),
            "간호대학"  to arrayOf("간호학과"),
            "의과대학"  to arrayOf("의예과","의학과"),
            "정보의생명공학대학"  to arrayOf("정보컴퓨터공학부 - 컴퓨터공학전공","정보컴퓨터공학부 - 인공지능 전공","의생명융합공학부")

        )


        val collegeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colleges)
        collegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColleges.adapter = collegeAdapter

        spinnerColleges.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCollege = colleges[position]
                if (departmentsMap.containsKey(selectedCollege)) {
                    val departments = departmentsMap[selectedCollege]!!
                    val departmentAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, departments)
                    departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDepartments.adapter = departmentAdapter
                    spinnerDepartments.visibility = View.VISIBLE
                } else {
                    spinnerDepartments.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                spinnerDepartments.visibility = View.GONE
            }
        }
    }



    }

