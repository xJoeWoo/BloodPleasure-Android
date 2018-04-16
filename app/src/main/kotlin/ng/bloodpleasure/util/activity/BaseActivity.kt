package ng.bloodpleasure.util.activity

import android.app.Application
import android.support.v7.app.AppCompatActivity
import ng.bloodpleasure.MainApplication

/**
 * Created by Ng on 16/04/2018.
 */
abstract class BaseActivity : AppCompatActivity() {
    val bpApplication: Application get() = application as MainApplication
}