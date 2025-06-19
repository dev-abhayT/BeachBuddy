package com.example.beachbuddy.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.beachbuddy.FragmentBeachList
import com.example.beachbuddy.FragmentMapContainer

class MapPagerAdapter(fragment: FragmentActivity, private val fragments : List<Fragment> ) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentMapContainer()
            1 -> FragmentBeachList()
            else -> FragmentMapContainer()
        }
    }
}