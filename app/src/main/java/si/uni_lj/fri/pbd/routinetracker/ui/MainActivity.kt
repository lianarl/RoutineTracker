package si.uni_lj.fri.pbd.routinetracker.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import si.uni_lj.fri.pbd.routinetracker.R
import android.widget.Toast
import android.view.MenuItem
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import si.uni_lj.fri.pbd.routinetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var drawerLayout: DrawerLayout? = null
    var toggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // drawer layout setup
        drawerLayout = binding.activityMain
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close)

        drawerLayout?.addDrawerListener(toggle!!)
        toggle?.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // tell nav drawer where to go when a certain item is pressed
        binding.nv.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.routines -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.routineListFragment) // go to my routines
                    Toast.makeText(this, "My Routines", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.settings -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment) // go to settings
                    Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle?.onOptionsItemSelected(item)!!)
            return true
        return super.onOptionsItemSelected(item)
    }
}