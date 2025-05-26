package com.atilsamancioglu.yemekkitabi9.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.atilsamancioglu.yemekkitabi9.databinding.FragmentTarifBinding
import com.atilsamancioglu.yemekkitabi9.model.Tarif
import com.atilsamancioglu.yemekkitabi9.roomdb.TarifDAO
import com.atilsamancioglu.yemekkitabi9.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException

class TarifFragment : Fragment() {

    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //izin istemek için.
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> //galeriye gitmek için.
    private var secilenGorsel: Uri? = null  //Uri(Uniform resource identifier) kaynağımızın yerini belirtir. mesela URL de bir Uri dır.
    //Uri =   data/data/media/image.jpg
    private var secilenBitmap: Bitmap? = null //Uri alıp görsele çevirmek için bitmap yapmışlar. android.graphics kütüphanesinde yer alır.
    private val mDisposable = CompositeDisposable() //devamlı bir istek yapıldığında özellikle internetle çalışırken bir sürü istek geldiğinde bunlar hafızada
    //birikmesin diye bir disposable çöpü içerisinde biriktirip hafızadan temizleyebiliyoruz. Bunu RxJava da yapmak zorundayız
    //disposable = tek kullanımlık
    private var secilenTarif : Tarif? = null

    private lateinit var db : TarifDatabase
    private lateinit var tarifDao: TarifDAO

    /**
    Uri (Uniform Resource Identifier),
    Android'de kaynakları (dosyalar, içerik sağlayıcılar, ağ kaynakları vb.)
    benzersiz şekilde tanımlamak ve erişmek için kullanılan bir yapıdır.
    Veri paylaşımı, intent'ler aracılığıyla işlemler gerçekleştirme ve
    sistem kaynaklarına güvenli erişim sağlama gibi senaryolarda kritik rol oynar.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
                                                                            //.allowMainThreadQueries() yaparsak main thread de çalıştırabiliriz ama bunu yapmamalıyız.
        db = Room.databaseBuilder(requireContext(), TarifDatabase::class.java, "Tarifler").build()
        tarifDao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener { gorselSec(it) }
        binding.kaydetButton.setOnClickListener { kaydet(it) }
        binding.silButton.setOnClickListener { sil(it) }

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            //normalde profesyonel dünyada stringler ile kontrol işlemi pek yapılmaz. çünkü yanlış string girilebilir.
            //enum ile ya da id atayarak kontrol edebiliriz.
            if (bilgi == "yeni") {
                //Yeni tarif eklenecek
                secilenTarif = null
                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled = true
                binding.isimText.setText("")
                binding.malzemeText.setText("")
            } else {
                //Eski eklenmiş tarif gösteriliyor.
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled = false
                val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(

                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)

                )


            }
        }

    }

    private fun handleResponse(tarif: Tarif) {
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel, 0, tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        secilenTarif = tarif
    }

    fun kaydet(view: View) {
        val isim = binding.isimText.text.toString()
        val malzeme = binding.malzemeText.text.toString()

        if(secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, 300)

            //kucukBitmap i byte dizisine çevirdik
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim,malzeme,byteDizisi)
            //tarifDao.insert(tarif) //artık bunu RxJava yolu ile yapıyoruz.

            // RxJava

            mDisposable.add(
                tarifDao.insert(tarif)
                .subscribeOn(Schedulers.io()) //işlemi arka planda yapacak
                .observeOn(AndroidSchedulers.mainThread()) //işlemi ön planda gösterecek
                .subscribe(this::handleResponseForInsert) //bu işlemlerin sonucunu bir fonksiyona aktarabiliyoruz. (sonucunda ne olacağını belirliyoruz)
            )





            //Threading
            //bizim hem kullanıcı arayüzü işlemlerimiz var hem de background işlemlerimiz var. işlemci bunları paralel olarak yapabilir ve birbirine karıştırmaması gerekir.
            //eğer her şeyi aynı yerde yapmaya kalkarsak veritabanına gidip veri çekip gelmek uzun sürerse fragmentımız, kullanıcı arayüzümüz kilitlenebilir.
            //zaten mantıklı olarak burada fragmentın içerisinde işlemler yapılırken internetten bir veri çektiğimizi düşünelim,
            //veri gelene kadar diğer işleme geçemeyiz, böyle olursa da fragment kilitlenir kullanıcı arayüzü donar. uygulama çöker ve potential lock the ui hatası verir. android buna karşı önlem alır.
            //Bu yüzden bu tür işlemleri main thread de yapmayız. Threading ile yapmalıyız. başka bir thread de bu işlemi yapıp main thread de göstermeyi sağlamalıyız.
            // bu işlemleri kotlin coroutines veya RxJava ile asenkron olarak yapabiliriz.
            //RxJava hem kotlinde hem de javada, coroutines sadece kotlinde.

            //Reactive programming (observable)
            //bir veri kaynağından gelen veri değiştiğinde bunu otomatik olarak kullanıcı arayüzünde göstermek için reactive programlama yapıları kullanırız.
            //bunu da RxJava ile yapabiliriz.
        }



    }

    private fun handleResponseForInsert() { //handleResponse = cevabı ele al
        //bir önceki fragment a dön
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    fun sil(view: View) {

        if (secilenTarif != null) {

            mDisposable.add(
                tarifDao.delete(tarif = secilenTarif!!) //null kontrolü yaptığımız için böyle yapmamızda sorun yok.
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert) //silinince geri gideceğiz (bir önceki fragmenta)
            )

        }

    }


    fun gorselSec(view: View) {

        /**
         IZINLER = PERMISSIONS

         Izinleri manifest dosyasında belirtmeliyiz. Kullanıcıdan alsak da, yüklerken oto alsak da burada belirtmemiz gerekiyor.
         Normal izinleri(yüklerken kabul edilen) burada belirtiyoruz, kullanıcıdan alınan izinleri(runtime) ise hem burada belirtiyoruz, hem de tekrar kullanıcıya soruyoruz.
         Eskiden uygulama yüklenirken manifest dosyasına izinleri ekleyip geçebiliyormuşuz.
         Ancak bu şirketler tarafından kötüye kullanılmış bu yüzden önemli izinleri(protection level : dangerous) Manifest.permission dökümantasyonundan takip etmeliyiz.
         Protection level: dangerous olan izinleri kullanıcıdan istememiz gerekiyor.
         Mesela READ_MEDIA_IMAGES, READ_MEDIA_AUDIO, READ_MEDIA_VIDEO, READ_EXTERNAL_STORAGE gibi izinleri kesinlikle kullanıcıya sorarak runtime da almalıyız.
         Bu izinlerin eklenme şekli yeni güncellenmiş bu yüzden yeni ve eski telefonlarda farklı şekilde izin istememiz gerekiyor.

         */

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //Yenilerde READ_MEDIA_IMAGES, eskilerde READ_EXTERNAL_STORAGE



            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view,
                        "Galeriye ulaşıp fotoğraf yüklememiz gerek, bunun için izin vermelisiniz.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "İzin ver",
                        View.OnClickListener {

                            //izin isteyeceğiz.
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                        }).show() //.show() u unutursak gösterilmez.
                } else {
                    //izin isteyeceğiz.
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            } else {
                //izin verilmiş, galeriye gidebilirim.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }



        } else {



            //Compat = compatible = uyumlu . Önceki versiyonlar ile uyumlu mu diye kontrol ediyor. mesela API 19 öncesiyse direkt izin alabiliyoruz vs.
            //checkSelfPermission = izin durumunu kontrol eder.
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //PERMISSION_GRANTED = izin verildi
                //izin verilmemiş, izin istememiz gerekiyor.
                //shouldShowRequestPermissionRationale() kullanıcı izin vermemişse android bu fonksiyonla bunu kontrol eder ve tekrar izin ister.
                //neden izin istediğini de kullanıcıya açıklar = Rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //snackbar göstermemiz lazım. kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin istemeliyiz. Snackbar = toast mesajının butonlu hali gibi düşünebiliriz. LENGTH_INDEFINITE = basmadan gitmeyecek.
                    Snackbar.make(view,
                        "Galeriye ulaşıp fotoğraf yüklememiz gerek, bunun için izin vermelisiniz.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "İzin ver",
                        View.OnClickListener {

                            //izin isteyeceğiz.
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                        }).show() //.show() u unutursak gösterilmez.
                } else {
                    //izin isteyeceğiz.
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            } else {
                //izin verilmiş, galeriye gidebilirim.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }



        }


    }





    //launcherları kayıt etme fonksiyonu oluşturduk.
    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if(result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if(intentFromResult != null) {
                    secilenGorsel = intentFromResult.data //bize Uri döndürüyor. Bu da kullanıcının seçtiği görselin nerede kayıtlı olduğunu gösterir.

                    //URI YI BITMAP E CEVIRME YONTEMLERI
                    //Bu işlemler manuel olduğu için hatalarla karşılaşabiliriz. mesela sd kart çıkar görsel ondadır vs. vs.
                    //Bu yüzden uygulamanın çökmemesi için try catch bloğu kullanırız.

                    try {

                        if(Build.VERSION.SDK_INT >= 28) {

                            //Yeni Yöntem
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, secilenGorsel!!) //Kotlin'de !! operatörü, null güvenliğini zorla geçersiz kılma amacıyla kullanılır.
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)

                        } else {

                            //Eski Yöntem
                            //Bu yöntem(getBitMap) deprecated(kullanım dışı) edilmiş. Üstünün çizili olması bu anlama geliyor.
                            //Bu da demek oluyor ki eski telefonlarda çalışır ancak yenilerde çalışmaz.
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)

                        }

                    } catch (e: IOException) {

                        println(e.localizedMessage)

                    }


                }
            }

        }

        //bizden ActivityResultCallback isteniyor. bu bir interface olduğu için object tanımlamamız lazım ama boolean döndürdüğü için lambda ile halledebiliriz.
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

            if(result) {
                //izin verildi galeriye gidebiliriz.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //izin verilmedi
                Toast.makeText(requireContext(), "İzin Verilmedi!", Toast.LENGTH_LONG).show()
            }

        }


    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int): Bitmap {
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        //kullanıcının seçtiği resmi küçültürken oranının bozulmaması için bitmapOrani na göre kenarları düzenleyip küçültüyoruz.
        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if(bitmapOrani > 1) {
            //gorsel yatay
            width = maximumBoyut
            val kisaltilmisYukseklik = width / bitmapOrani
            height = kisaltilmisYukseklik.toInt()
        } else {
            //gorsel dikey
            height = maximumBoyut
            val kisaltilmisGenislik = height * bitmapOrani
            width = kisaltilmisGenislik.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width, height, true)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }


}