package pl.doriansobanski.jastarniaapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

open class MainActivity : AppCompatActivity() {

    //----------------------------------------------------------------------------------------------// Zmienne globalne
    private val app: ConstraintLayout by lazy { findViewById(R.id.app) }
    private val appLogo: ImageButton by lazy { findViewById(R.id.i0) }
    private val appName: TextView by lazy { findViewById(R.id.t0) }
    private val now: LocalDate by lazy { LocalDate.now() }
    private val drawer: DrawerLayout by lazy { findViewById(R.id.main) }
    private val nav: NavigationView by lazy { findViewById(R.id.nav) }
    private val menu: ImageButton by lazy { findViewById(R.id.menu) }
    private val frame: ViewFlipper by lazy { findViewById(R.id.view) }
    private val posts: LinearLayout by lazy { findViewById(R.id.posts) }
    private val events: LinearLayout by lazy { findViewById(R.id.events) }
    private val questions: LinearLayout by lazy { findViewById(R.id.questions) }

    private val screens: List<ConstraintLayout> by lazy {
        listOf(
            findViewById(R.id.aktualnosci),
            findViewById(R.id.odkryj),
            findViewById(R.id.atrakcje),
            findViewById(R.id.kalendarz),
            findViewById(R.id.pytanie),
            findViewById(R.id.mieszk)
        )
    }
    private val images: List<ImageView> by lazy {
        listOf(
            findViewById(R.id.i1),
            findViewById(R.id.i2),
            findViewById(R.id.i3),
            findViewById(R.id.i4),
            findViewById(R.id.i5),
            findViewById(R.id.i6)
        )
    }
    private val texts: List<TextView> by lazy {
        listOf(
            findViewById(R.id.t1),
            findViewById(R.id.t2),
            findViewById(R.id.t3),
            findViewById(R.id.t4),
            findViewById(R.id.t5),
            findViewById(R.id.t6)
        )
    }

    private var isAdmin = false
    private var indexAdmin = 0
    private val codeAdmin = listOf(
        "8${now.monthValue}",
        "4${now.monthValue}",
        "5${now.monthValue}",
        "4${now.monthValue}",
        "7${now.monthValue}",
        "5${now.monthValue}",
        "9${now.monthValue}",
        "30${now.monthValue}"
    )
    private var isDestroyer = false
    private var indexDestroyer = 0
    private val codeDestroyer = listOf(
        "2${now.monthValue}",
        "1${now.monthValue}",
        "3${now.monthValue}",
        "7${now.monthValue}",
        "4${now.monthValue}",
        "2${now.monthValue}",
        "9${now.monthValue}",
        "30${now.monthValue}"
    )


    //----------------------------------------------------------------------------------------------// Kod wewnętrzny
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pozyskiwane atrybuty
        val theme = this.theme
        val tv1 = TypedValue()
        val tv2 = TypedValue()

        theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimaryDark, tv1, true
        )
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimaryVariant, tv2, true
        )

        // Zmienne wewnętrzne
        val fadeOut = AlphaAnimation(0f, 1f)
        val moveDown = TranslateAnimation(0f, 0f, 100f, 0f)
        val set = AnimationSet(true)
        val off = tv1.data
        val on = tv2.data

        val refresh = findViewById<SwipeRefreshLayout>(R.id.f5)
        val eventMenu = findViewById<LinearLayout>(R.id.eventwydarzenia)
        val eventMenuImage = findViewById<ImageView>(R.id.event)
        val allMenu = findViewById<LinearLayout>(R.id.allwszystko)
        val allMenuImage = findViewById<ImageView>(R.id.all)
        val infoMenu = findViewById<LinearLayout>(R.id.infoinformacje)
        val infoMenuImage = findViewById<ImageView>(R.id.info)

        val calendarWindow = findViewById<CalendarView>(R.id.kalendarzokno)
        val sunrise = findViewById<TextView>(R.id.wschod)
        val sunset = findViewById<TextView>(R.id.zachod)
        val calendarDay = findViewById<TextView>(R.id.dzien)
        val calendarWeek = findViewById<TextView>(R.id.dzientyg)
        val calendarMonth = findViewById<TextView>(R.id.mies)

        val search = findViewById<TextInputLayout>(R.id.textInput)
        val ask = findViewById<TextInputLayout>(R.id.zadajPytanie)

        //Konfiguracja animacji
        fadeOut.duration = 300
        moveDown.duration = 150
        set.addAnimation(fadeOut)
        set.addAnimation(moveDown)

        // Funkcje wewnętrzne
        fun bottomMenu(option: View) {
            eventMenu.background = ContextCompat.getDrawable(this, R.color.transparent)
            allMenu.background = ContextCompat.getDrawable(this, R.color.transparent)
            infoMenu.background = ContextCompat.getDrawable(this, R.color.transparent)
            eventMenuImage.setColorFilter(off)
            allMenuImage.setColorFilter(off)
            infoMenuImage.setColorFilter(off)
            eventMenu.isClickable = true
            allMenu.isClickable = true
            infoMenu.isClickable = true

            option.background = ContextCompat.getDrawable(this, R.drawable.gradient)
            option.isClickable = false

            fun filter(category: String = "all") {
                posts.children.forEachIndexed { index, child ->
                    if (category == "all") child.visibility = View.VISIBLE else child.visibility =
                        if (child.contentDescription == category) View.VISIBLE else View.GONE

                    if (index == 0) child.findViewById<View>(R.id.border).visibility = View.GONE
                }
            }

            when (option) {
                eventMenu -> {
                    eventMenuImage.setColorFilter(ContextCompat.getColor(this, R.color.white))
                    filter("event")
                }
                allMenu -> {
                    allMenuImage.setColorFilter(ContextCompat.getColor(this, R.color.white))
                    filter()
                }
                infoMenu -> {
                    infoMenuImage.setColorFilter(ContextCompat.getColor(this, R.color.white))
                    filter("info")
                }
            }

            posts.startAnimation(set)
        }

        fun setDay(date: LocalDate = now) {
            calendarDay.text = date.format(DateTimeFormatter.ofPattern("d"))
            calendarMonth.text = date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pl")))
            calendarDay.startAnimation(set)

            val location = Location(54.6861, 18.6714)
            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
            val calculator =
                SunriseSunsetCalculator(location, TimeZone.getTimeZone("Europe/Warsaw"))

            sunrise.text = calculator.getOfficialSunriseForDate(calendar)
            sunset.text = calculator.getOfficialSunsetForDate(calendar)
            calendarWeek.text =
                calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        }

        fun adminEvent() {
            isAdmin = true

            calendarDay.text = "$"
            calendarDay.startAnimation(set)
            calendarWeek.text = "Administrator"
            calendarMonth.text =
                "Możesz edytować posty z poziomu aplikacji, poprzez kliknięcie na wybrany tekst."
            sunrise.text = "0:00"
            sunset.text = "0:00"

            Toast.makeText(
                this,
                "Włączono tryb administratora",
                Toast.LENGTH_SHORT
            ).show()
        }

        fun destroyerEvent() {
            isDestroyer = true

            calendarDay.text = "$"
            calendarDay.startAnimation(set)
            calendarWeek.text = "Okrutnik"
            calendarMonth.text = "Możesz zniszczyć każdy element, poprzez kliknięcie na niego"
            sunrise.text = "4:20"
            sunset.text = "21:37"

            Toast.makeText(
                this,
                "Włączono tryb okrutnika",
                Toast.LENGTH_SHORT
            ).show()
        }

        fun screenRun(i: Int) {
            when (i) {
                0 -> {

                }
                1 -> {

                }
                2 -> {

                }
                3 -> {

                }
                4 -> {

                }
                5 -> {

                }
            }
        }

        fun menu(option: View) {
            screens.forEach { it.isClickable = false }

            fun clear(exceptImage: ImageView, exceptText: TextView) {
                images.forEach { it.setColorFilter(off) }
                texts.forEach { it.setTextColor(off) }

                exceptImage.setColorFilter(on)
                exceptText.setTextColor(on)
            }

            for (i in screens.indices) {
                if (option == screens[i]) {
                    clear(images[i], texts[i])
                    screenRun(i)
                    frame.displayedChild = i
                    break
                }
            }

            app.postDelayed({ drawer.closeDrawer(nav) }, 400)
            app.postDelayed({
                screens.forEach { it.isClickable = true }
                option.isClickable = false
            }, 600)
        }


        //------------------------------------------------------------------------------------------// Interfejs
        menu.setOnClickListener { toggleNavigationDrawer() }
        appLogo.setOnClickListener { openDrawerAndPerformClick(screens[0]) }
        appName.setOnClickListener { openDrawerAndPerformClick(screens[0]) }
        screens.forEachIndexed { index, screen ->
            screen.setOnClickListener {
                menu(screens[index])
            }
        }

        screens[0].isClickable = false


        //------------------------------------------------------------------------------------------// Aktualności
        infoMenuImage.setColorFilter(off)
        eventMenuImage.setColorFilter(off)

        load(Type.POST)

        refresh.setProgressBackgroundColorSchemeColor(on)
        refresh.overScrollMode = View.OVER_SCROLL_NEVER

        eventMenu.setOnClickListener { bottomMenu(it) }
        allMenu.setOnClickListener { bottomMenu(it) }
        infoMenu.setOnClickListener { bottomMenu(it) }
        refresh.setOnRefreshListener {
            posts.removeAllViews()
            load(Type.POST)

            eventMenu.background = ContextCompat.getDrawable(this, R.color.transparent)
            allMenu.background = ContextCompat.getDrawable(this, R.drawable.gradient)
            infoMenu.background = ContextCompat.getDrawable(this, R.color.transparent)
            eventMenuImage.setColorFilter(off)
            allMenuImage.setColorFilter(ContextCompat.getColor(this, R.color.white))
            infoMenuImage.setColorFilter(off)
            eventMenu.isClickable = true
            allMenu.isClickable = false
            infoMenu.isClickable = true

            posts.children.forEachIndexed { index, child ->
                child.visibility = View.VISIBLE
                child.findViewById<View>(R.id.border).visibility =
                    if (index == 0) View.GONE else View.VISIBLE
            }

            posts.startAnimation(set)
            app.postDelayed({ refresh.isRefreshing = false }, 600)
        }


        //------------------------------------------------------------------------------------------// Odkryj Jastarnię
        val context = applicationContext
        val userAgent = context.packageName
        Configuration.getInstance().userAgentValue = userAgent

        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setCenter(GeoPoint(54.701216996215734, 18.67763421703975))
        mapView.controller.setZoom(15.0)


        //------------------------------------------------------------------------------------------// Polecane Atrakcje
        findViewById<CardView>(R.id.surfszkola).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/place/Surf+Szko%C5%82a+-+kursy+kitesurfingu+i+nauka+windsurfingu/@54.6977,18.6699956,18z/data=!3m1!4b1!4m6!3m5!1s0x46fdaa45a0c18ff9:0xb64d8640c323b8f0!8m2!3d54.6977!4d18.6707611!16s%2Fg%2F1tdc8h8c")
                )
            )
        }
        findViewById<CardView>(R.id.magdalena).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/place/Tramwaj+wodny+Jastarnia-Hel/@54.697279,18.6688489,16.75z/data=!4m14!1m7!3m6!1s0x46fdaa45a0c18ff9:0xb64d8640c323b8f0!2sSurf+Szko%C5%82a+-+kursy+kitesurfingu+i+nauka+windsurfingu!8m2!3d54.6977!4d18.6707611!16s%2Fg%2F1tdc8h8c!3m5!1s0x46fdab0c74eae53b:0xa498ab4a7e5c362f!8m2!3d54.6961963!4d18.6743763!16s%2Fg%2F11hzfnnfsk")
                )
            )
        }
        findViewById<CardView>(R.id.boisko).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/place/Orlik/@54.7053774,18.655214,18.21z/data=!4m14!1m7!3m6!1s0x46fdaa45a0c18ff9:0xb64d8640c323b8f0!2sSurf+Szko%C5%82a+-+kursy+kitesurfingu+i+nauka+windsurfingu!8m2!3d54.6977!4d18.6707611!16s%2Fg%2F1tdc8h8c!3m5!1s0x46fdaa52953f2d57:0xb95d7ee9023abbf5!8m2!3d54.7055642!4d18.6555972!16s%2Fg%2F11c809__7g")
                )
            )
        }
        findViewById<CardView>(R.id.zeglowanie).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/place/Akademia+Jachtingu+-+Solidna+Szko%C5%82a+%C5%BBeglarstwa/@54.696491,18.673753,18.75z/data=!4m6!3m5!1s0x46fda729cd9c99e7:0x99c3ba692d830b87!8m2!3d54.6964736!4d18.6743726!16s%2Fg%2F1td1zz2s")
                )
            )
        }
        findViewById<CardView>(R.id.motor).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/place/Akademia+Jachtingu+-+Solidna+Szko%C5%82a+%C5%BBeglarstwa/@54.696491,18.673753,18.75z/data=!4m6!3m5!1s0x46fda729cd9c99e7:0x99c3ba692d830b87!8m2!3d54.6964736!4d18.6743726!16s%2Fg%2F1td1zz2s")
                )
            )
        }


        //------------------------------------------------------------------------------------------// Kalendarz Imprez
        calendarWindow.date = Calendar.getInstance().timeInMillis
        setDay()

        load(Type.EVENT)

        calendarWindow.setOnDateChangeListener { _, year, month, day ->
            setDay(LocalDate.of(year, month + 1, day))

            val click = "$day${month + 1}"
            if (!isAdmin && click == codeAdmin[indexAdmin]) indexAdmin++ else indexAdmin = 0
            if (!isDestroyer && click == codeDestroyer[indexDestroyer]) indexDestroyer++ else indexDestroyer =
                0
            if (indexAdmin == codeAdmin.size) adminEvent()
            if (indexDestroyer == codeDestroyer.size) destroyerEvent()

            events.children.forEachIndexed { _, child ->
                val contentDescription = child.contentDescription.toString().toInt()

                child.visibility = if (contentDescription < LocalDate.of(2000, 1, 1)
                        .until(LocalDate.of(year, month + 1, day), ChronoUnit.DAYS)
                ) View.GONE else View.VISIBLE
            }
        }


        //------------------------------------------------------------------------------------------// Zadaj Pytanie
        load(Type.QUESTION)

        val questionList = mutableListOf<View>()
        questions.children.forEach { questionList.add(it) }

        search.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(input: Editable?) {
                filterPhrases(input.toString(), questionList)
                ask.editText?.text = input
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        findViewById<ImageButton>(R.id.send).setOnClickListener {
            ask.editText?.text?.clear()
            search.editText?.text?.clear()
            Toast.makeText(this, "Zapytanie zostało wysłane", Toast.LENGTH_LONG).show()
        }
    }


    //----------------------------------------------------------------------------------------------// Funkcje globalne
    private fun toggleNavigationDrawer() {
        if (drawer.isDrawerOpen(nav)) {
            drawer.closeDrawer(nav)
        } else {
            drawer.openDrawer(nav)
        }
    }

    private fun openDrawerAndPerformClick(view: View) {
        if (screens[0].isClickable) {
            drawer.openDrawer(nav)
            app.postDelayed({
                screens[0].isPressed = true
                view.performClick()
                screens[0].isPressed = false
            }, 200)
        }
    }

    private fun showMore(view: View) {
        val layout: LinearLayout = view.findViewById(R.id.show)
        val content: TextView = layout.findViewById(R.id.content)
        val arrow: ImageView = layout.findViewById(R.id.arrow)
        val lines = content.maxLines

        layout.setOnClickListener {
            if (content.maxLines == Integer.MAX_VALUE) {
                content.maxLines = lines
                arrow.setImageResource(R.drawable.down_arrow)
            } else {
                content.maxLines = Integer.MAX_VALUE
                arrow.setImageResource(R.drawable.up_arrow)
            }
        }

        content.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (content.layout != null) {
                    if (content.layout.lineCount >= lines) {
                        arrow.visibility = View.VISIBLE
                    } else {
                        arrow.visibility = View.GONE
                        layout.isClickable = false
                    }
                    content.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    private fun compareTexts(text1: String, text2: String): Double {
        val text1WithoutSpaces = text1.replace("\\s".toRegex(), "")
        val text2WithoutSpaces = text2.replace("\\s".toRegex(), "")

        val set1 = text1WithoutSpaces.toSet()
        val set2 = text2WithoutSpaces.toSet()

        val similarityCount = set1.intersect(set2).count()
        val length1 = text1WithoutSpaces.length

        return (similarityCount.toDouble() / length1.toDouble()) * 100
    }

    private fun filterPhrases(query: String, list: MutableList<View>) {
        list.forEach { child ->
            val title = child.findViewById<TextView>(R.id.title).text.toString()
            val content = child.findViewById<TextView>(R.id.content).text.toString()
            val similarity = compareTexts(query, title) + (compareTexts(query, content) / 10)

            child.visibility = if (query.isEmpty() || similarity > 75) View.VISIBLE else View.GONE
        }

        list.sortedByDescending { child ->
            val title = child.findViewById<TextView>(R.id.title).text.toString()
            val content = child.findViewById<TextView>(R.id.content).text.toString()

            compareTexts(query, title) + (compareTexts(query, content) / 10)
        }.forEach { view ->
            if (view.visibility != View.GONE) {
                questions.removeView(view)
                questions.addView(view)
            }
        }
    }

    private fun load(type: Type) {
        when (type) {
            Type.POST -> {
                new(
                    Category.INFORMATION,
                    "Oto aplikacja \"Gmina Jastarnia\"!",
                    "  Z przyjemnością informuję, że stworzyliśmy aplikację dla mieszkańców i turystów Gminy Jastarnia! Dzięki niej będziecie mieli łatwy dostęp do informacji o ciekawych wydarzeniach, atrakcjach i trasach turystycznych, które znajdują się w naszej pięknej okolicy.\n\n  Nasza aplikacja jest obecnie dostępna na platformie Android i zapewnia prosty i intuicyjny interfejs, który umożliwia łatwe wyszukiwanie informacji oraz korzystanie z funkcji nawigacyjnych. Dzięki niej będziecie mogli łatwo planować swoje wycieczki i odkrywać nowe miejsca, a także być na bieżąco z wydarzeniami kulturalnymi, sportowymi i rozrywkowymi w naszej okolicy.\n\n  Chcielibyśmy również podkreślić, że nasza aplikacja jest przydatna nie tylko dla turystów, ale również dla mieszkańców Gminy Jastarnia. Dzięki niej będziecie mieli dostęp do wszystkich informacji na temat naszej okolicy w jednym miejscu, co ułatwi Wam planowanie czasu wolnego.\n\n  Obecnie pracujemy również nad tym, aby nasza aplikacja była dostępna na platformie iOS, dzięki czemu będzie mogła być używana na różnych urządzeniach mobilnych.\n\n  Zapraszamy wszystkich mieszkańców i turystów do pobrania naszej aplikacji i odkrywania piękna Gminy Jastarnia!",
                    R.drawable.baner2,
                    LocalDate.of(2023, 3, 5)
                )
                new(
                    Category.EVENT,
                    "50-lecie Miasta na wodzie",
                    "  W bieżącym roku Jastarnia obchodzi 50 – lecie nadania jej praw miejskich, otrzymanych 1 stycznia 1973 roku dekretem Rady Państwa. Ponieważ niemal wszystkie bardziej doniosłe uroczystości przekładano wówczas na 22 lipca, uroczysta sesja Miejskiej Rady Narodowej w Jastarni, na której odczytano dekret, także została przesunięta na ten dzień. Od tego czasu Jastarnia wraz z sąsiednim Borem stanowią miasto. 1 stycznia 2017 Jastarnia zmieniła charakter z gminy miejskiej na miejsko-wiejską.\n\n   Stało się tak poprzez wydzielenie osobnych miejscowości Juraty i Kuźnicy z Syberią. Choć gmina Jastarnia uchodzi za cichą i spokojną,  to w sezonie letnim diametralnie zmienia swój charakter. Dzieje się tak za sprawą turystów, którzy tłumnie odwiedzają trzy miejscowości, by korzystać z uroków plaży i wszystkiego, co oferują tutejsze tereny. To właśnie turystyka stała się motorem napędowym do jej rozwoju. Unikatowe na skalę światową położenie sprawia, że jest to idealne miejsce do uprawiania sportów wodnych. Szlaki przyrodnicze dają szansę na obcowanie z naturą. Sąsiedztwo Morza Bałtyckiego i Zatoki Puckiej sprawiają, że jest ona z dwóch stron otoczona wodą. To właśnie woda ukształtowała charakter tej gminy, który przez dziesięciolecia oparty był na rybołówstwie. Tu tradycja przenika się z nowoczesnością. Kaszubskie dziedzictwo sprawia, że na każdym kroku można spotkać namacalne dowody przywiązania do regionalizmu. Choć trzy nadmorskie miejscowości stanowią jedną gminę, to każda z nich posiada swoistą odrębność. Jastarnia to centrum ruchu turystycznego. Kuźnica to rybacka osada, którą cechują malownicze krajobrazy i dziewiczy charakter. Jurata to z kolei od lat miejsce spotkań ludzi biznesu, kultury i sztuki, którzy latem zjeżdżają się do kurortu, by spotkać się i w luźnej atmosferze wymienić doświadczeniami. Gmina Jastarnia  swoim urokiem przyciąga osoby spragnione kontaktu z naturą w najpiękniejszym wydaniu. Od 50 lat, już jako miasto z każdym rokiem zmienia swoje oblicze, a w 2023 roku ma wyjątkowe powody do świętowania. Zapraszamy do udziału w zaplanowanych wydarzeniach.",
                    R.drawable.baner1,
                    LocalDate.of(2023, 3, 7)
                )
                new(
                    Category.EVENT,
                    "Warsztaty plastyczne z Kamą Kuik",
                    "w Kuźnicy\nZapraszam na warsztaty organizowane w Kuźnicy w Stacji Informacji Turystycznej przez MOKSiR JASTRANIA\nGrupa 6-9 lat 10.00-12.00\nGrupa 10-16 12.00-14.00\nZapisy MOKSiR Jastarnia tel. 58 6752097\nIlość miejsc ograniczona\nDecyduje kolejność zgłoszeń",
                    R.drawable.baner3,
                    LocalDate.of(2023, 1, 10)
                )
                new(
                    Category.INFORMATION,
                    "Informacja Starosty Puckiego",
                    "o wyłożeniu projektu operatu opisowo-kartograficznego do wglądu osób fizycznych, osób prawych i jednostek organizacyjnych nieposiadających osobowości prawnej z obrębu Jastarnia w gminie miasta Jastarni.",
                    R.drawable.baner4,
                    LocalDate.of(2022, 12, 27)
                )

                posts.children.forEachIndexed { index, child ->
                    child.visibility = View.VISIBLE
                    child.findViewById<View>(R.id.border).visibility =
                        if (index == 0) View.GONE else View.VISIBLE

                    showMore(child)
                }
            }
            Type.PIN -> {

            }
            Type.ATTRACTION -> {

            }
            Type.EVENT -> {
                new(
                    "50-lecie Miasta na wodzie",
                    "  W bieżącym roku Jastarnia obchodzi 50 – lecie nadania jej praw miejskich, otrzymanych 1 stycznia 1973 roku dekretem Rady Państwa. Ponieważ niemal wszystkie bardziej doniosłe uroczystości przekładano wówczas na 22 lipca, uroczysta sesja Miejskiej Rady Narodowej w Jastarni, na której odczytano dekret, także została przesunięta na ten dzień. Od tego czasu Jastarnia wraz z sąsiednim Borem stanowią miasto. 1 stycznia 2017 Jastarnia zmieniła charakter z gminy miejskiej na miejsko-wiejską.\n\n   Stało się tak poprzez wydzielenie osobnych miejscowości Juraty i Kuźnicy z Syberią. Choć gmina Jastarnia uchodzi za cichą i spokojną,  to w sezonie letnim diametralnie zmienia swój charakter. Dzieje się tak za sprawą turystów, którzy tłumnie odwiedzają trzy miejscowości, by korzystać z uroków plaży i wszystkiego, co oferują tutejsze tereny. To właśnie turystyka stała się motorem napędowym do jej rozwoju. Unikatowe na skalę światową położenie sprawia, że jest to idealne miejsce do uprawiania sportów wodnych. Szlaki przyrodnicze dają szansę na obcowanie z naturą. Sąsiedztwo Morza Bałtyckiego i Zatoki Puckiej sprawiają, że jest ona z dwóch stron otoczona wodą. To właśnie woda ukształtowała charakter tej gminy, który przez dziesięciolecia oparty był na rybołówstwie. Tu tradycja przenika się z nowoczesnością. Kaszubskie dziedzictwo sprawia, że na każdym kroku można spotkać namacalne dowody przywiązania do regionalizmu. Choć trzy nadmorskie miejscowości stanowią jedną gminę, to każda z nich posiada swoistą odrębność. Jastarnia to centrum ruchu turystycznego. Kuźnica to rybacka osada, którą cechują malownicze krajobrazy i dziewiczy charakter. Jurata to z kolei od lat miejsce spotkań ludzi biznesu, kultury i sztuki, którzy latem zjeżdżają się do kurortu, by spotkać się i w luźnej atmosferze wymienić doświadczeniami. Gmina Jastarnia  swoim urokiem przyciąga osoby spragnione kontaktu z naturą w najpiękniejszym wydaniu. Od 50 lat, już jako miasto z każdym rokiem zmienia swoje oblicze, a w 2023 roku ma wyjątkowe powody do świętowania. Zapraszamy do udziału w zaplanowanych wydarzeniach.",
                    R.drawable.baner1,
                    LocalDate.of(2023, 4, 21)
                )
                new(
                    "Sobótka w Jastarni",
                    "  Jak co roku w Jastarni 23 czerwca, wigilia św. Jana jest obchodzona bardzo uroczyście. Głównymi jej bohaterami oraz organizatorami jest rocznik poborowy, z którego wszyscy chłopcy w danym roku mają zostać powołani do służby wojskowej.\n\n  Historia Sobótki w Jastarni sięga bardzo dawnych czasów. Jej tradycja jest niepamiętna, a opowiadania powtarzane z pokolenia na pokolenie świadczą, że Sobótkę świętowano już przez cały wiek XIX. Ogień świętojański wiąże się z kultem słońca, a obrzędy palenia ognia sięgają czasów przed naszą erą. Jego kult jest elementem dawnych religii i kultur.\n\n  Dlatego, aby tradycji stało się zadość w ten sobótkowy wieczór rocznik 2004 wraz z Neptunem i Proserpiną rusza w barwnym korowodzie przez ulice miasta. Pochód prowadzi orkiestra. Za orkiestrą podążają mieszkańcy gminy oraz rzesze turystów, dla których ta impreza stanowi niezwykłą atrakcję. Konny zaprzęg ciągnie długi pal sosnowy o długości 10m i 30 cm, na końcu którego znajduje się beczka pełna drewna nasączonego materiałem łatwopalnym. Co jakiś czas padają okrzyki \" niech żyje rocznik 2004 - niech żyje!\".\n\n  Korowód dociera na specjalnie przygotowany plac sobótkowy nad Zatoką Pucką. Tu następuje podniesienie pala. Chłopcy z rocznika 2004 muszą poradzić sobie sami z tym niełatwym zadaniem. Tego niezwykłego wieczoru czeka ich jeszcze jedno trudne zadanie - zapalenie beczki. Wszyscy zgromadzeni widzowie kibicują niezmordowanym bohaterom Sobótki. Burmistrz Miasta w języku kaszubskim wita wszystkich zebranych, a w szczególności rocznik organizatorów. Neptunowi i Proserpinie wręczony zostaje symboliczny klucz do bram miasta na znak objęcia w świętojańską noc rządów w Jastarni. Po tych niezwykłych emocjach Miejski Ośrodek Kultury, Sportu i Rekreacji w Jastarni zaprasza wszystkich na zabawę sobótkową, która trwa do późnych godzin nocnych. O godzinie 23.00 odbywa się pokaz ogni sztucznych.",
                    R.drawable.baner7,
                    LocalDate.of(2023, 6, 23)
                )
                new(
                    "Salt Wave Festival",
                    "  Wybrzeże Bałtyku mieni się bursztynami✨ Zobaczcie kogo spotkamy już 4-5 sierpnia na plaży w Jastarni!\n -Sam Tompkins\n -Lola Marsh\n -KACPERCZYK\n -Crooked Colours\n -Agar Agar\n -BOKKA\n -Rosalie.\n -Bass Astral\n -Jann\n -Jack Botts Music\n -Monster DJ\n -Safario\n -Young Majli",
                    R.drawable.baner5,
                    LocalDate.of(2023, 8, 4)
                )

                events.children.forEachIndexed { _, child ->
                    val contentDescription = child.contentDescription.toString().toInt()

                    child.visibility = if (contentDescription < LocalDate.of(2000, 1, 1)
                            .until(now, ChronoUnit.DAYS)
                    ) View.GONE else View.VISIBLE

                    showMore(child)
                }
            }
            Type.QUESTION -> {
                new(
                    "Na którym wejściu jest plaża dla psów?",
                    "Na wejściach o numerach 31 (w Kuźnicy) i 55 (między Jastarnią, a Juratą)"
                )
                new(
                    "Co oznacza brak flagi na plaży?",
                    "Brak flagi sygnalizuje, że na kąpielisku nie ma ratowników."
                )
                new(
                    "Co oznacza żółta i czerwona boja?",
                    "Żółta boja na wodzie oznacza kąpielisko dla osób umiejących dobrze pływać, natomiast czerwona boja oznacza, że jest to strefa zakazu wstępu."
                )
                new(
                    "Gdzie jest otwarty sklep w nocy?",
                    "W nocy otwarta jest Biedronka, Legendarny Sklep Miki i Delikatesy \"abo\"."
                )
                new(
                    "Jak dostać się z Jastarni na Hel?",
                    "Można skorzystać z komunikacji miejskiej, wynająć rower lub skuter wodny, lub wziąć prywatny transport."
                )
                new(
                    "Gdzie jest darmowy parking?", "Największy darmowy parking jest obok Biedronki."
                )
            }
        }
    }

    enum class Type {
        POST, PIN, ATTRACTION, EVENT, QUESTION
    }

    enum class Category {
        EVENT, INFORMATION, OTHER
    }

    // Post
    private fun new(
        category: Category,
        title: String,
        content: String,
        image: Int,
        date: LocalDate,
        context: Context = this
    ) {
        val post = LayoutInflater.from(context).inflate(R.layout.post, posts, false) as CardView
        val icon: Int
        when (category) {
            Category.EVENT -> {
                icon = R.drawable.star
                post.contentDescription = "event"
            }
            Category.INFORMATION -> {
                icon = R.drawable.info
                post.contentDescription = "info"
            }
            Category.OTHER -> {
                icon = R.drawable.grid
                post.contentDescription = "other"
            }
        }

        post.findViewById<ImageView>(R.id.icon).setImageResource(icon)
        post.findViewById<TextView>(R.id.title).text = title
        post.findViewById<TextView>(R.id.content).text = content
        post.findViewById<ImageView>(R.id.image).setImageResource(image)
        post.findViewById<TextView>(R.id.date).text = date.format(
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
        )

        posts.addView(post)
    }

    // Pinezka
    /*
    private fun new(
        context: Context = this,
        type: Type = Type.PIN,
        location: Location,
        image: Int
    ) {

    }
    */

    // Atrakcja
    /*
    private fun new(
        context: Context = this,
        type: Type = Type.ATTRACTION,
        title: String,
        image: Int
    ) {

    }
    */

    // Wydarzenie
    private fun new(
        title: String, content: String, image: Int, date: LocalDate, context: Context = this
    ) {
        val event = LayoutInflater.from(context).inflate(R.layout.event, events, false) as CardView

        event.contentDescription = LocalDate.of(2000, 1, 1).until(date, ChronoUnit.DAYS).toString()

        event.findViewById<TextView>(R.id.title).text = title
        event.findViewById<TextView>(R.id.content).text = content
        event.findViewById<ImageView>(R.id.image).setImageResource(image)
        event.findViewById<TextView>(R.id.date).text = date.format(
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
        )

        events.addView(event)
    }

    // Pytanie
    private fun new(
        title: String, content: String, context: Context = this
    ) {
        val question = LayoutInflater.from(context)
            .inflate(R.layout.question, questions, false) as ConstraintLayout
        val titleLayout = question.findViewById<TextView>(R.id.title)
        val contentLayout = question.findViewById<TextView>(R.id.content)

        titleLayout.text = title
        question.findViewById<View>(R.id.border).scaleX = 10f - titleLayout.text.length
        contentLayout.text = content
        contentLayout.visibility = View.GONE
        titleLayout.setOnClickListener {
            if (contentLayout.isVisible) {
                contentLayout.visibility = View.GONE
            } else contentLayout.visibility = View.VISIBLE
        }

        questions.addView(question)
    }
}