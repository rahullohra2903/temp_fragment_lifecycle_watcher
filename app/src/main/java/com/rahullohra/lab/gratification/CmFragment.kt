package com.rahullohra.lab.gratification

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringDef
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.rahullohra.lab.gratification.FragmentInflater.Companion.ACTIVITY
import com.rahullohra.lab.gratification.FragmentInflater.Companion.DEFAULT
import com.rahullohra.lab.gratification.FragmentInflater.Companion.VIEW_PAGER
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext


open class CmFragment : Fragment() {
    @FragmentInflater
    open var fragmentInflater: String = DEFAULT
    var fragmentName: String = javaClass.name
    var hasReadArguments = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }


    override fun onStart() {
        super.onStart()
        if (!hasReadArguments) {
            fragmentInflater = arguments?.getString(BUNDLE_ARGS_INFLATER) ?: fragmentInflater
            if (!arguments?.getString(BUNDLE_ARGS_FRAGMENT_NAME).isNullOrEmpty())
                fragmentName = fragmentName + "_" + arguments?.getString(BUNDLE_ARGS_FRAGMENT_NAME) ?: fragmentName
            hasReadArguments = true
        }
    }

    override fun onResume() {
        super.onResume()

        view?.post {
            if (isVisible && fragmentInflater == ACTIVITY) {
                CmFragmentLifecycleObserver.onFragmentResumed(this)
            } else if (isVisible && fragmentInflater == VIEW_PAGER) {
                val arr = IntArray(2)
                view?.getLocationOnScreen(arr)
                if (arr[0] == 0) {
                    CmFragmentLifecycleObserver.onFragmentResumed(this)
                }
//                Log.d("NOOB", "onResume fragment = ${fragmentName}, x = ${view?.x},y = ${view?.y}, [${arr[0]}],[${arr[1]}]")

            }
        }
    }

    override fun onStop() {
        super.onStop()
        CmFragmentLifecycleObserver.onFragmentStop(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        CmFragmentLifecycleObserver.onFragmentDestroyed(this)
    }

    companion object {
        const val BUNDLE_ARGS_INFLATER = "inflater"
        const val BUNDLE_ARGS_NAME = "name"
        const val BUNDLE_ARGS_FRAGMENT_NAME = "fragmentName"
    }
}

class CmViewPager : ViewPager {

    var pageChangeListener: ViewPager.SimpleOnPageChangeListener? = null
    var pageSwapped = false

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup(attrs)
    }

    constructor(context: Context) : super(context) {
        setup(null)
    }

    fun setup(attrs: AttributeSet?) {
        pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageSwapped = true

                if (this@CmViewPager.adapter is CmPagerAdapter) {
                    val adapter = this@CmViewPager.adapter as CmPagerAdapter
                    val activeFragment = adapter.fragmentList[position]
                    CmFragmentLifecycleObserver.onFragmentSelected(activeFragment)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        pageChangeListener?.let {
            addOnPageChangeListener(it)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pageChangeListener?.let {
            removeOnPageChangeListener(it)
        }
    }

    //Need to protect it from Firebase
    override fun setAdapter(adapter: PagerAdapter?) {
        super.setAdapter(adapter)
        post {
            if (adapter is CmPagerAdapter && !pageSwapped) {
                if (currentItem == 0) {
                    val activeFragment = adapter.fragmentList[currentItem]
                    CmFragmentLifecycleObserver.onFragmentSelected(activeFragment)
                }
            }
        }
    }
}

object CmFragmentLifecycleObserver {

    var weakFragment: WeakReference<CmFragment>? = null
    var notifPresenter: NotificationsPresenter? = null
    val notificationUseCase = NotificationsUseCase()

    fun onFragmentSelected(fragment: CmFragment) {
        checkNotifications(fragment)
    }

    fun onFragmentResumed(fragment: CmFragment) {
        checkNotifications(fragment)
    }

    fun onFragmentStop(fragment: CmFragment) {
        if (weakFragment != null && weakFragment?.get() == fragment) {
            notifPresenter?.flush()
        }
    }

    fun onFragmentDestroyed(fragment: CmFragment) {
        if (weakFragment != null && weakFragment?.get() == fragment) {
            notifPresenter?.flush()
        }
    }

    private fun checkNotifications(fragment: CmFragment) {
        try {
            notifPresenter?.flush()

            weakFragment = WeakReference(fragment)
            notifPresenter = NotificationsPresenter(notificationUseCase, fragmentName = fragment.fragmentName)
            notifPresenter?.let { presenter ->
                fragment.lifecycle.addObserver(presenter)
                presenter.liveDataNotifications.observe(fragment.viewLifecycleOwner, Observer {
                    when (it) {
                        is Success -> {
                            weakFragment?.get()?.let {
                                val title = presenter.fragmentName
                                it.context?.let { context ->
                                    createAlertDialog(context, title)
                                }
                            }
                        }
                    }
                })
                presenter.getNotifications(fragment.fragmentName)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    fun createAlertDialog(context: Context, title: String) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(title)
            .setCancelable(true)
        builder.create().show()
    }
}

class NotificationsPresenter(
    val useCase: NotificationsUseCase,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    val fragmentName: String
) : CoroutineScope, LifecycleObserver {

    val liveDataNotifications = MutableLiveData<LiveDataResult<Notification>>()
    private val ceh = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
    }
    private val masterJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = masterJob + dispatcher

    fun getNotifications(screenName: String) {
        launch(ceh) {
            try {
                val notif = useCase.getNotifications(screenName)
                if (notif != null) {
                    liveDataNotifications.postValue(Success(notif))
                } else {
                    liveDataNotifications.postValue(Fail(Throwable("No record found")))
                }
            } catch (ex: Exception) {
                liveDataNotifications.postValue(Fail(ex))
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        flush()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        flush()
    }

    fun flush() {
        coroutineContext.cancel(cause = CancellationException("task cancelled for $fragmentName"))
    }
}

class NotificationsUseCase {
    companion object {
        const val INVALID_NOTIFICATION = -1
    }

    suspend fun getNotifications(screenName: String): Notification? {
        delay(3000L)

        val map = HashMap<Int, ArrayList<String>>()

        val matchedNotifications = DataSource.notifications.filter { it.screenName.contains(screenName) }

        matchedNotifications.forEach {
            var list = map[it.id]
            if (list.isNullOrEmpty()) {
                list = arrayListOf()
                map[it.id] = list
            }
            val screenNamesArray = it.screenName.split(",")
            list.addAll(screenNamesArray)
            if (list.isNotEmpty() && list.last().isEmpty()) {
                list.removeAt(list.size - 1)
            }
        }

        var finalNotificationId: Int = INVALID_NOTIFICATION

        map.forEach {
            val key = it.key
            val list = it.value

            val screenName = list.find { it == screenName }
            if (!screenName.isNullOrEmpty()) {
                finalNotificationId = key
                return@forEach
            }
        }

        if (finalNotificationId != -INVALID_NOTIFICATION) {
            return matchedNotifications.first { it.id == finalNotificationId }
        }
        return null
    }
}

data class Notification(val screenName: String, val id: Int)

class DataSource {
    companion object {
        val notifications = arrayListOf<Notification>(
            Notification("com.rahullohra.lab.gratification.TabFragment_One", 1),
            Notification("com.rahullohra.lab.gratification.TabFragment_Two", 2),
            Notification("com.rahullohra.lab.gratification.TabFragment", 3),
            Notification("com.rahullohra.lab.gratification.ThankYouFragment_2", 4),
            Notification(
                "com.rahullohra.lab.gratification.TabFragment_One," +
                        "com.rahullohra.lab.gratification.TabFragment_Two", 5
            ),
            Notification(
                "com.rahullohra.lab.gratification.TabFragment_One," +
                        "com.rahullohra.lab.gratification.TabFragment", 6
            ),
            Notification("A,B,C", 5)
        )
    }
}

interface CmPagerAdapter {
    var fragmentList: ArrayList<CmFragment>
}

@Retention(AnnotationRetention.SOURCE)
@StringDef(VIEW_PAGER, ACTIVITY, DEFAULT)
annotation class FragmentInflater {
    companion object {
        const val VIEW_PAGER = "vp"
        const val ACTIVITY = "activity"
        const val DEFAULT = "default"
    }
}

sealed class LiveDataResult<out T>
data class Success<out T>(val data: T) : LiveDataResult<T>()
data class Fail<out T>(val ex: Throwable) : LiveDataResult<T>()