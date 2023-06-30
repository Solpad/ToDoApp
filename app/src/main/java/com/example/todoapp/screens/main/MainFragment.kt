package com.example.todoapp.screens.main


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentMainBinding
import com.example.todoapp.model.Resourse
import com.example.todoapp.model.TodoItem
import com.example.todoapp.screens.adapter.MainAdapter
import com.example.todoapp.screens.adapter.MainItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk


class MainFragment : Fragment() {


    private val mBinding get() = _binding!!
    private lateinit var adapterToDo: MainAdapter

    private lateinit var mViewModel: MainFragmentViewModel
    private var _binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(this)[MainFragmentViewModel::class.java]
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialization(view)
        mBinding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addingFragment)
        }

        mViewModel.getListTodoItems()
        mViewModel.getTodoItemsLiveData().observe(viewLifecycleOwner){
            Log.e("observe","change")
            adapterToDo.submitList(it)
            adapterToDo.notifyDataSetChanged()
        }
    }
    //Инициализация всего в одной функции.
    private fun initialization(view: View) {
        setRecyclerView()
        setItemTouchHelper(view)
        setResourseObserver()
        setSwipeRefresh()
    }

    // инициализация ресайклера
    private fun setRecyclerView() {
        mBinding?.apply {
            adapterToDo = MainAdapter(
                MainAdapter.OnClickListener { setOnClickListenerRV(it) },
                MainAdapter.OnLongClickListener { item, view -> setOnLongClickListener(item, view) })
            var linearLayoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
            }

            recyclerviewDo.layoutManager = linearLayoutManager
            recyclerviewDo.adapter = adapterToDo
        }
    }

    // clickListener на элемент ресайклера, тут же отправляем Bundle в AddingFragment
    private fun setOnClickListenerRV(item: TodoItem) {
        val bundle = bundleOf("item" to item)
        findNavController().navigate(
            R.id.action_mainFragment_to_addingFragment,
            bundle
        )
    }

    private fun setOnLongClickListener(item: TodoItem, view: View) {
        showPopUpMenu(item,view)
    }

    private fun showPopUpMenu(item: TodoItem,view: View){

        var popupMenu = context?.let { PopupMenu(it, view) }
        popupMenu?.inflate(R.menu.popup_menu);
        popupMenu?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_edit -> {
                    val bundle = bundleOf("item" to item)
                    findNavController().navigate(
                        R.id.action_mainFragment_to_addingFragment,
                        bundle
                    )
                    true
                }
                R.id.menu_delete -> {
                    mViewModel.deleteTodoItem(item,item.id)
                    true
                }
                else -> false
            }
        }
        popupMenu?.show();
    }

    private fun setResourseObserver(){
        mViewModel.getResourseLiveData().observe(viewLifecycleOwner){
            when(it){
                is Resourse.Error -> {
                    Snackbar.make(mBinding.recyclerviewDo,it.message.toString(), Snackbar.LENGTH_LONG)
                    .setAction("Повторить",View.OnClickListener {
                    }).show()
                }
                is Resourse.Success -> {
                    Log.e("good","Good")
                }
            }
        }
    }
    private fun setItemTouchHelper(view: View){
        var simpleItemTouchCallback = MainItemTouchHelper(adapterToDo,mViewModel,view)
        var itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mBinding.recyclerviewDo)

    }

    private fun setSwipeRefresh(){
        mBinding.swipeRefresh.setOnRefreshListener {

            mViewModel.getListTodoItems()

            if (!mViewModel.checkInternetConnection()){
                Snackbar.make(mBinding.recyclerviewDo,"Отсутствует интернет соединение",Snackbar.LENGTH_LONG)
                    .setAction("Обновить",View.OnClickListener {
                        mViewModel.getListTodoItems()
                    }).show()
            }
            mBinding.swipeRefresh.isRefreshing = false
        }
    }
    private fun setYandexAuth(){


    }

}