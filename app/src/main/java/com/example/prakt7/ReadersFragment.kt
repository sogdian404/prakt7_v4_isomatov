package com.example.prakt7


import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadersFragment : Fragment(R.layout.fragment_readers) {

    private lateinit var database: LibraryDatabase
    private lateinit var adapter: ReaderAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = LibraryDatabase.getDatabase(requireContext())
        recyclerView = view.findViewById(R.id.recyclerViewReaders)

        // Настраиваем RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Создаём адаптер
        adapter = ReaderAdapter(
            emptyList(),
            onItemClick = { reader ->
                // Показать информацию о читателе
                AlertDialog.Builder(requireContext())
                    .setTitle(reader.fullName)
                    .setMessage("Email: ${reader.email}\nРоль: ${reader.role}\nСтатус: ${if (reader.isBlocked) "Заблокирован" else "Активен"}")
                    .setPositiveButton("OK", null)
                    .show()
            },
            onItemLongClick = { readerToDelete ->
                // Удалить читателя
                AlertDialog.Builder(requireContext())
                    .setTitle("🗑️ Удалить читателя?")
                    .setMessage("Вы уверены?")
                    .setPositiveButton("Удалить") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                database.readerDao().delete(readerToDelete)
                            }
                            Toast.makeText(requireContext(), "Читатель удалён", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter

        // Наблюдаем за данными из БД
        viewLifecycleOwner.lifecycleScope.launch {
            database.readerDao().getAllReaders().collect { readers ->
                adapter.updateReaders(readers)
            }
        }

        // Кнопка добавления читателя
        view.findViewById<FloatingActionButton>(R.id.fabAddReader)?.setOnClickListener {
            AddReaderDialogFragment().show(childFragmentManager, "AddReaderDialog")
        }
    }
}