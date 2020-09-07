package com.rahullohra.lab.gratification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rahullohra.lab.R
import kotlinx.android.synthetic.main.activity_thankyou.*

class ThankyouActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thankyou)

        btn_thankyou_1.setOnClickListener {
            setFragment(ThankYouFragment_1())
        }
        btn_thankyou_2.setOnClickListener {
            setFragment(ThankYouFragment_2())
        }
    }

    fun setFragment(fm: Fragment) {
        supportFragmentManager.popBackStack()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fm_fragment_container, fm)
            .commit()
    }
}

class ThankYouFragment_1 : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_thankyou_1, container, false)
        return v
    }
}

class ThankYouFragment_2 : CmFragment() {
    override var fragmentInflater = FragmentInflater.ACTIVITY

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_thankyou_2, container, false)
        return v
    }
}