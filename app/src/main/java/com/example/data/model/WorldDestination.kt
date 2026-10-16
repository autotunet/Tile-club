package com.example.data.model

import com.example.R

data class WorldDestination(
    val id: String,
    val country: String,
    val monument: String,
    val flag: String,
    val startLevel: Int,
    val endLevel: Int,
    val drawableResId: Int?,
    val skyGradient: List<Long>,
    val ambientColor: Long,
    val quote: String
)

object WorldDestinations {
    val DESTINATIONS = listOf(
        WorldDestination(
            id = "japan",
            country = "Japón",
            monument = "Monte Fuji & Pagoda Sakura",
            flag = "🇯🇵",
            startLevel = 1,
            endLevel = 50,
            drawableResId = R.drawable.bg_japan_fuji_1792032920385,
            skyGradient = listOf(0xFF2E1065, 0xFFBE185D, 0xFFFDE047),
            ambientColor = 0xFFFDA4AF,
            quote = "Bajo los cerezos en flor al pie del monte sagrado Fuji."
        ),
        WorldDestination(
            id = "france",
            country = "Francia",
            monument = "Torre Eiffel al Atardecer",
            flag = "🇫🇷",
            startLevel = 51,
            endLevel = 100,
            drawableResId = R.drawable.bg_paris_eiffel_1792032940114,
            skyGradient = listOf(0xFF1E1B4B, 0xFF4338CA, 0xFFF59E0B),
            ambientColor = 0xFFFCD34D,
            quote = "La ciudad de la luz dorada y el río Sena."
        ),
        WorldDestination(
            id = "egypt",
            country = "Egipto",
            monument = "Grandes Pirámides de Giza & Esfinge",
            flag = "🇪🇬",
            startLevel = 101,
            endLevel = 150,
            drawableResId = R.drawable.bg_egypt_pyramids_1792032957041,
            skyGradient = listOf(0xFF312E81, 0xFF9A3412, 0xFFF59E0B),
            ambientColor = 0xFFFDBA74,
            quote = "Eternidad tallada en la arena dorada de los faraones."
        ),
        WorldDestination(
            id = "italy",
            country = "Italia",
            monument = "Coliseo Romano",
            flag = "🇮🇹",
            startLevel = 151,
            endLevel = 200,
            drawableResId = null,
            skyGradient = listOf(0xFF0F172A, 0xFF1E3A8A, 0xFFF59E0B),
            ambientColor = 0xFFFBBF24,
            quote = "Ecos de gloria y leyendas de la antigua Roma."
        ),
        WorldDestination(
            id = "china",
            country = "China",
            monument = "La Gran Muralla China",
            flag = "🇨🇳",
            startLevel = 201,
            endLevel = 250,
            drawableResId = null,
            skyGradient = listOf(0xFF1E293B, 0xFF991B1B, 0xFFEA580C),
            ambientColor = 0xFFF87171,
            quote = "El dragón de piedra que serpentea entre nieblas milenarias."
        ),
        WorldDestination(
            id = "greece",
            country = "Grecia",
            monument = "Partenón de la Acrópolis",
            flag = "🇬🇷",
            startLevel = 251,
            endLevel = 300,
            drawableResId = null,
            skyGradient = listOf(0xFF0C4A6E, 0xFF0284C7, 0xFFBAE6FD),
            ambientColor = 0xFF38BDF8,
            quote = "La cuna del pensamiento clásico sobre el mar Egeo."
        ),
        WorldDestination(
            id = "mexico",
            country = "México",
            monument = "Pirámide de Chichén Itzá",
            flag = "🇲🇽",
            startLevel = 301,
            endLevel = 350,
            drawableResId = null,
            skyGradient = listOf(0xFF064E3B, 0xFF059669, 0xFFF59E0B),
            ambientColor = 0xFF34D399,
            quote = "La serpiente emplumada de Kukulcán en la selva maya."
        ),
        WorldDestination(
            id = "india",
            country = "India",
            monument = "Taj Mahal de Mármol Blanco",
            flag = "🇮🇳",
            startLevel = 351,
            endLevel = 400,
            drawableResId = null,
            skyGradient = listOf(0xFF4C1D95, 0xFF7C3AED, 0xFFFDE047),
            ambientColor = 0xFFA78BFA,
            quote = "Un poema de amor eterno esculpido en mármol."
        ),
        WorldDestination(
            id = "peru",
            country = "Perú",
            monument = "Santuario de Machu Picchu",
            flag = "🇵🇪",
            startLevel = 401,
            endLevel = 450,
            drawableResId = null,
            skyGradient = listOf(0xFF14532D, 0xFF15803D, 0xFF86EFAC),
            ambientColor = 0xFF4ADE80,
            quote = "La ciudad sagrada oculta en las nubes de los Andes."
        ),
        WorldDestination(
            id = "uk",
            country = "Reino Unido",
            monument = "Big Ben & Westminster",
            flag = "🇬🇧",
            startLevel = 451,
            endLevel = 500,
            drawableResId = null,
            skyGradient = listOf(0xFF1E293B, 0xFF334155, 0xFF64748B),
            ambientColor = 0xFF94A3B8,
            quote = "El latido solemne de la campana histórica de Londres."
        ),
        WorldDestination(
            id = "brazil",
            country = "Brasil",
            monument = "Cristo Redentor en Río",
            flag = "🇧🇷",
            startLevel = 501,
            endLevel = 550,
            drawableResId = null,
            skyGradient = listOf(0xFF065F46, 0xFF0D9488, 0xFFFACC15),
            ambientColor = 0xFF2DD4BF,
            quote = "Brazos abiertos sobre las playas y morros de Río de Janeiro."
        ),
        WorldDestination(
            id = "usa",
            country = "Estados Unidos",
            monument = "El Gran Cañón del Colorado",
            flag = "🇺🇸",
            startLevel = 551,
            endLevel = 600,
            drawableResId = null,
            skyGradient = listOf(0xFF7C2D12, 0xFFC2410C, 0xFFFDE047),
            ambientColor = 0xFFFB923C,
            quote = "La majestuosidad esculpida por el viento y el río."
        ),
        WorldDestination(
            id = "australia",
            country = "Australia",
            monument = "Ópera de Sídney",
            flag = "🇦🇺",
            startLevel = 601,
            endLevel = 650,
            drawableResId = null,
            skyGradient = listOf(0xFF0C4A6E, 0xFF0284C7, 0xFFE0F2FE),
            ambientColor = 0xFF38BDF8,
            quote = "Velas arquitectónicas sobre las aguas resplandecientes."
        ),
        WorldDestination(
            id = "spain",
            country = "España",
            monument = "La Alhambra de Granada",
            flag = "🇪🇸",
            startLevel = 651,
            endLevel = 700,
            drawableResId = null,
            skyGradient = listOf(0xFF701A75, 0xFFBE185D, 0xFFFBBF24),
            ambientColor = 0xFFF472B6,
            quote = "Fuentes cristalinas y arcos nazaríes de ensueño."
        ),
        WorldDestination(
            id = "turkey",
            country = "Turquía",
            monument = "Globos de Capadocia",
            flag = "🇹🇷",
            startLevel = 701,
            endLevel = 750,
            drawableResId = null,
            skyGradient = listOf(0xFF1E1B4B, 0xFF6D28D9, 0xFFFB923C),
            ambientColor = 0xFFC084FC,
            quote = "Cientos de globos al amanecer sobre chimeneas de hadas."
        ),
        WorldDestination(
            id = "jordan",
            country = "Jordania",
            monument = "El Tesoro de Petra",
            flag = "🇯🇴",
            startLevel = 751,
            endLevel = 800,
            drawableResId = null,
            skyGradient = listOf(0xFF881337, 0xFFBE123C, 0xFFFBBF24),
            ambientColor = 0xFFFB7185,
            quote = "La ciudad de roca rosa esculpida en el desfiladero."
        ),
        WorldDestination(
            id = "germany",
            country = "Alemania",
            monument = "Castillo Neuschwanstein",
            flag = "🇩🇪",
            startLevel = 801,
            endLevel = 850,
            drawableResId = null,
            skyGradient = listOf(0xFF0F172A, 0xFF1E3A8A, 0xFF93C5FD),
            ambientColor = 0xFF60A5FA,
            quote = "El castillo de cuento de hadas entre los picos de Baviera."
        ),
        WorldDestination(
            id = "thailand",
            country = "Tailandia",
            monument = "Templo Blanco de Chiang Rai",
            flag = "🇹🇭",
            startLevel = 851,
            endLevel = 900,
            drawableResId = null,
            skyGradient = listOf(0xFF047857, 0xFF10B981, 0xFFFEF08A),
            ambientColor = 0xFF6EE7B7,
            quote = "Brillo de espejos y arquitectura sagrada oriental."
        ),
        WorldDestination(
            id = "iceland",
            country = "Islandia",
            monument = "Danza de Auroras Boreales",
            flag = "🇮🇸",
            startLevel = 901,
            endLevel = 950,
            drawableResId = null,
            skyGradient = listOf(0xFF022C22, 0xFF059669, 0xFF34D399),
            ambientColor = 0xFF6EE7B7,
            quote = "Luces esmeralda danzando en la noche ártica polar."
        ),
        WorldDestination(
            id = "celestial",
            country = "Reino de las Nubes",
            monument = "El Olimpo de las 1000 Fichas",
            flag = "👑",
            startLevel = 951,
            endLevel = 1000,
            drawableResId = null,
            skyGradient = listOf(0xFF312E81, 0xFF6366F1, 0xFFFDE047),
            ambientColor = 0xFFFDE047,
            quote = "La cima suprema para los verdaderos maestros de Tile Club."
        )
    )

    fun getDestinationForLevel(level: Int): WorldDestination {
        val clampedLevel = level.coerceIn(1, 1000)
        return DESTINATIONS.find { clampedLevel in it.startLevel..it.endLevel } ?: DESTINATIONS.last()
    }
}
