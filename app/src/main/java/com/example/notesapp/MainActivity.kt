package com.example.notesapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room

import com.example.notesapp.Adapter.NotesAdapter
import com.example.notesapp.Database.NoteDatabase
import com.example.notesapp.Models.Note
import com.example.notesapp.Models.NoteViewModel
import com.example.notesapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(),NotesAdapter.NotesClickListener,PopupMenu.OnMenuItemClickListener{

    private lateinit var binding : ActivityMainBinding
    private lateinit var database : NoteDatabase


    lateinit var viewModel: NoteViewModel
    lateinit var adapter : NotesAdapter
    lateinit var selectedNote:Note
    private val updateNote=registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    // Yeni not ekleme veya mevcut notu güncelleme sonucunu işlemek için kullanılan ActivityResultLauncher işlevi
    { result ->
        // Eğer sonuç başarılıysa
        if(result.resultCode==Activity.RESULT_OK)
        {
            // Dönen veriler arasından "note" adlı Serializable veriyi alıyoruz
            val note=result.data?.getSerializableExtra("note") as? Note
            // Eğer not null değilse
            if(note!=null){
                // ViewModel sınıfındaki updateNote işlevini kullanarak notu veritabanında günceller
                viewModel.updateNote(note)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        // ViewModel sınıfını oluştur
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(NoteViewModel::class.java)

        // Tüm notları izleyen LiveData nesnesini izleyerek RecyclerView'a bağlan
        viewModel.allnotes.observe(this) { list ->
            // list null değilse, RecyclerView'a notları bağla
            list?.let {
                adapter.updateList(list)
            }
        }
        database=NoteDatabase.getDatabase(this)



        val nightModeFlags = this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            binding.toolbar.setBackgroundColor(Color.parseColor("#292929"))
        }

    }




    private fun initUI() {
        // RecyclerView ayarları
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        binding.recyclerView.setHasFixedSize(true)
        adapter = NotesAdapter(this, this)
        binding.recyclerView.adapter = adapter

        // Not ekleme aktivitesini başlatmak için ActivityResultLauncher oluşturma
        val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                // Eklenen notu ViewModel'e ekle
                val note = result.data?.getSerializableExtra("note") as? Note
                if(note != null) {
                    viewModel.insertNote(note)
                }
            }
        }


        // Scroll dinleyicisi ekle
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Eğer aşağı kaydırıyorsak ve FloatingActionButton'un alpha değeri 1 ise
                if (dy > 0 && binding.floatingActionButton.alpha == 1f) {
                    // FloatingActionButton'un alpha değerini 0.2'ye animasyonlu bir şekilde düşür
                    binding.floatingActionButton.animate().alpha(0.2f).start()
                    // Eğer yukarı kaydırıyorsak ve FloatingActionButton'un alpha değeri 1'den küçük ise
                } else if (dy < 0 && binding.floatingActionButton.alpha < 1f) {
                    // FloatingActionButton'un alpha değerini animasyonlu bir şekilde 1'e yükselt
                    binding.floatingActionButton.animate().alpha(1f).start()
                }
            }
        })

        // FloatingActionButton'a tıklandığında AddNote aktivitesini başlat
        binding.floatingActionButton.setOnClickListener{
            val intent= Intent (this,AddNote::class.java)
            getContent.launch(intent)
        }

        // Arama kutusuna metin girildiğinde filtreleme işlemini gerçekleştir
        binding.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0:String?): Boolean{
                return false
            }
            override fun onQueryTextChange(newText:String?): Boolean{
                if(newText!=null)
                {
                    adapter.filterList(newText)
                }
                return true
            }
        })
    }

    override fun onItemClicked(note: Note) {
        // Seçilen notu güncellemek için AddNote activity'sine geçiş yap
        val intent = Intent(this@MainActivity, AddNote::class.java)
        // Geçiş yaparken güncellenecek notu intent'e ekleyin
        intent.putExtra("current_note", note)
        // startActivityForResult() metodu yerine updateNote.launch() metodu çağrılır
        // updateNote, registerForActivityResult() metoduyla oluşturulan ActivityResultLauncher objesidir
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: Note, cardView: CardView) {
        // Not silmek için alert dialog oluştur
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Bu notu silmek istediğinize emin misiniz?")
            .setCancelable(false)
            .setPositiveButton("Evet") { _, _ ->
                // Eğer kullanıcı evet'e tıklarsa notu sil
                lifecycleScope.launch {
                    NoteViewModel(application).deleteNote(note)
                    Toast.makeText(context, "Not silindi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hayır") { dialog, _ ->
                // Eğer kullanıcı hayır'a tıklarsa dialog penceresini kapat
                dialog.dismiss()
            }
        // Alert dialog penceresini göster
        val alert = builder.create()
        alert.show()
    }


    private  fun popUpDisplay(cardView: CardView)
    {
        val popup=PopupMenu(this,cardView)
        popup.setOnMenuItemClickListener(this@MainActivity)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
       if(item?.itemId== R.id.delete_note)
       {

           viewModel.deleteNote(selectedNote)
           return true

       }
        return false
    }
}