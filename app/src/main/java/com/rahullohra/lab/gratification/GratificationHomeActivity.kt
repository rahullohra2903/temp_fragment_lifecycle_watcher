package com.rahullohra.lab.gratification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.rahullohra.lab.R

const val TAG = "Gratif"

class GratificationHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gratification_home)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fm_activity_gratification, GratificationHomeFragment())
            .commit()
    }
}

class GratificationHomeFragment : Fragment() {

    lateinit var viewPager: ViewPager
    lateinit var pagerAdapter: HomePagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_gratification_home, container, false)
        viewPager = v.findViewById(R.id.view_pager)
        setAdapter()
        return v
    }

    fun setAdapter() {

        val fragmentList = arrayListOf<CmFragment>(
            TabFragment.newInstance(name = "One", fragmentName = "One", inflater = FragmentInflater.VIEW_PAGER),
            TabFragment.newInstance(name = "Two", fragmentName = "Two", inflater = FragmentInflater.VIEW_PAGER),
            TabFragment.newInstance(name = "Three", inflater = FragmentInflater.VIEW_PAGER)
        )
        pagerAdapter = HomePagerAdapter(childFragmentManager, fragmentList)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(TAG, "HomeFragment onPage:$position")
            }
        })

        viewPager.adapter = pagerAdapter

    }
}

class HomePagerAdapter(fm: FragmentManager, override var fragmentList: ArrayList<CmFragment>) : FragmentStatePagerAdapter(fm),
    CmPagerAdapter {

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }
}

class TabFragment : CmFragment() {

    lateinit var tvTitle: TextView
    lateinit var title: String
    lateinit var btnThankYou: Button

    companion object {
        fun newInstance(
            name: String,
            @FragmentInflater inflater: String = FragmentInflater.DEFAULT,
            fragmentName: String? = null
        ): TabFragment {
            val fragment = TabFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_ARGS_NAME, name)
            bundle.putString(BUNDLE_ARGS_INFLATER, inflater)
            bundle.putString(BUNDLE_ARGS_FRAGMENT_NAME, fragmentName)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_tab, container, false)
        tvTitle = v.findViewById(R.id.tv_title)
        btnThankYou = v.findViewById(R.id.btn_go_to_thankyou)
        title = arguments?.getString(BUNDLE_ARGS_NAME) ?: ""
        tvTitle.text = title

        btnThankYou.setOnClickListener {
            startActivity(Intent(it.context, ThankyouActivity::class.java))
        }
        return v
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "TabFragment onResume:$title")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "TabFragment onStop:$title")
    }
}
