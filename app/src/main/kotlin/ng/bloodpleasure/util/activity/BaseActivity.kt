package ng.bloodpleasure.util.activity

import android.app.Activity
import android.app.Application
import ng.bloodpleasure.MainApplication

/**
 * Created by Ng on 16/04/2018.
 */
abstract class BaseActivity : Activity() {
    val bpApplication: Application get() = application as MainApplication
}